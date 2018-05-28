package com.jsen.redis.schedule.worker.executer;

import com.jsen.redis.schedule.master.Prefix;
import com.jsen.redis.schedule.master.task.JobConf;
import com.jsen.redis.schedule.master.task.sk.IJob;
import groovy.lang.GroovyClassLoader;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class JExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JExecutor.class);

    private static ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new DThreadFactory("JExecutor-worker-" + UUID.randomUUID().toString()));

    private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(JExecutor.class.getClassLoader());

    /**
     * 为了可以执行定时任务， 这里设置Key为UUID，也就是可以执行两个相同的任务
     */
    private static List<Pair> jobs = new ArrayList<>();

    public static int getPoolSize() {
        return jobs.size();
    }

    public static List<Pair> getJobs() {
        return jobs;
    }

    public static boolean exec(JobConf baseJob, RedisClient redisClient) throws Exception {

        for (Pair pair: jobs) {
            if (pair.taskID.equals(baseJob.getTaskID())) {
                logger.error("任务存在");
                return false;
            }
        }

        logger.debug("开始执行任务：" + baseJob.getTaskID());

        if (baseJob.isStaticJob()) {
            String classPath = baseJob.getJobData();
            try {
                Class<?> clazz = Class.forName(classPath);
                IJob iJob = (IJob) clazz.newInstance();

                Pair pair = new Pair(baseJob.getTaskID(), iJob);
                lifeCycleCall(baseJob, redisClient, iJob, pair);
                pair.future = executorService.submit(iJob::exec);
                jobs.add(pair);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            String classData = baseJob.getJobData();
            try {
                Class<?> clazz = groovyClassLoader.parseClass(classData);
                IJob iJob = (IJob) clazz.newInstance();

                Pair pair = new Pair(baseJob.getTaskID(), iJob);
                lifeCycleCall(baseJob, redisClient, iJob, pair);
                pair.future = executorService.submit(iJob::exec);
                jobs.add(pair);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return true;
    }

    private static void lifeCycleCall(JobConf baseJob, RedisClient redisClient, IJob iJob, Pair pair) {
        iJob.setLifeCycle(new IJob.LifeCycle() {
            @Override
            public void complete() {
                jobs.remove(pair);
                logger.debug("任务：" + baseJob.getTaskID()+ "执行完成");
                if (JobConf.isSingleJob(baseJob)) {
                    redisClient.del(Prefix.schedule + baseJob.getTaskID(), ar -> {});
                }
                redisClient.del(Prefix.task + baseJob.getTaskID(), ar -> {});
                baseJob.setIndex(baseJob.getIndex() + 1);
                redisClient.set(baseJob.getTaskID(), baseJob.toJson().toString(), r -> {});
            }

            @Override
            public void stop() {
                jobs.remove(pair);
                redisClient.del(Prefix.task + baseJob.getTaskID(), ar -> {});
            }
        });
    }

    public static boolean stop(String taskID) {
        List<Pair> remove = new ArrayList<>();
        for (Pair pair:jobs) {
            if (pair.taskID.equals(taskID)) {
                pair.iJob.stop();
                Future future = pair.future;
                future.cancel(true);
                remove.add(pair);
            }
        }
        for (Pair pair:remove) {
            jobs.remove(pair);
        }
        return !remove.isEmpty();
    }

    public static class Pair{
        String taskID;
        IJob iJob;
        Future<?> future;

        Pair(String taskID, IJob iJob) {
            this.taskID = taskID;
            this.iJob = iJob;
        }

        public String getTaskID() {
            return taskID;
        }
    }
    static class DThreadFactory implements ThreadFactory {
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        DThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix+"-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread( r,namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(true);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
