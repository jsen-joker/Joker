package com.jsen.redis.schedule.worker.executer;

import com.google.common.collect.Lists;
import com.jsen.redis.schedule.master.Prefix;
import com.jsen.redis.schedule.master.task.JobConf;
import com.jsen.redis.schedule.worker.job.IJob;
import groovy.lang.GroovyClassLoader;
import io.vertx.core.CompositeFuture;
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

    // private static Lock lock = new ReentrantLock();

    public final Object mutex;
    private static JExecutor jExecutor;
    public static JExecutor getDefaultJExecutor() {
        return jExecutor;
    }
    public JExecutor() {
        jExecutor = this;
        this.mutex = this;
    }

    private static final Logger logger = LoggerFactory.getLogger(JExecutor.class);

    private static ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new DThreadFactory("JExecutor-worker-" + UUID.randomUUID().toString()));

    private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(JExecutor.class.getClassLoader());

    /**
     * 为了可以执行定时任务， 这里设置Key为UUID，也就是可以执行两个相同的任务
     */
    private static List<Pair> jobs = Collections.synchronizedList(new ArrayList<>());
    // private static List<Pair> jobs = new ArrayList<>();

    public int getPoolSize() {
        synchronized (mutex) {
            return jobs.size();
        }
    }

    public List<Pair> getJobs() {
        synchronized (mutex) {
            return jobs;
        }
    }

    public boolean exec(JobConf baseJob, RedisClient redisClient) throws Exception {

        synchronized (mutex) {
            for (Pair pair: jobs) {
                if (pair.taskID.equals(baseJob.getTaskID())) {
                    logger.error("任务存在");
                    return false;
                }
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
                synchronized (mutex) {
                    jobs.add(pair);
                }
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
                synchronized (mutex) {
                    jobs.add(pair);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return true;
    }

    private void lifeCycleCall(JobConf baseJob, RedisClient redisClient, IJob iJob, Pair pair) {
        iJob.setLifeCycle(new IJob.LifeCycle() {
            @Override
            public void complete() {
                synchronized (mutex) {
                    jobs.remove(pair);
                }
                logger.debug("任务：" + baseJob.getTaskID()+ "执行完成");
                List<io.vertx.core.Future> futures = Lists.newArrayList();
                if (JobConf.isSingleJob(baseJob)) {
                    io.vertx.core.Future<Void> f = io.vertx.core.Future.future();
                    futures.add(f);
                    redisClient.del(Prefix.schedule + baseJob.getTaskID(), r -> f.complete());
                }
                final io.vertx.core.Future<Void> f = io.vertx.core.Future.future();
                futures.add(f);
                redisClient.del(Prefix.task + baseJob.getTaskID(), r -> f.complete());
                baseJob.setIndex(baseJob.getIndex() + 1);
                final io.vertx.core.Future<Void> f2 = io.vertx.core.Future.future();
                futures.add(f2);
                redisClient.set(baseJob.getTaskID(), baseJob.toJson().toString(), r -> f2.complete());
                CompositeFuture.all(futures).setHandler(r -> {});
            }

            @Override
            public void stop() {
                synchronized (mutex) {
                    jobs.remove(pair);
                }
                redisClient.del(Prefix.task + baseJob.getTaskID(), ar -> {
                });
            }
        });
    }

    public boolean stop(String taskID) {
        List<Pair> remove = new ArrayList<>();
        synchronized (mutex) {
            for (int i = 0; i < jobs.size(); i++) {
                Pair pair = jobs.get(i);
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
