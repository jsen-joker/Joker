package com.jsen.redis.schedule.master.service.keyfire;

import com.jsen.redis.schedule.master.Prefix;
import com.jsen.redis.schedule.master.task.JobConf;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 *     节点任务失败重启，监测到节点失败后（task：），首先检查这个任务是否正在调度，如果在调度，设置一个新的fire
 *     worker节点也监听这个过期，过期后监测任务是否存在，存在则删除任务
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
public class WorkerExp {

    private static final Logger logger = LoggerFactory.getLogger(WorkerExp.class);

    private RedisClient redisClient;

    public WorkerExp(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void exec(String taskID) {

        logger.warn("监测到任务：" + taskID + "可能执行失败");

        Future<Void> res = Future.future();
        redisClient.get(Prefix.schedule + taskID, r -> {
            if (r.succeeded()) {
                String rs = r.result();
                if (rs == null || "".equals(rs)) {
                    res.fail("任务未在执行");
                } else {
                    res.complete();
                }
            } else {
                res.fail(r.cause());
            }
        });
        res.compose(r0 -> {
            Future<JobConf> result = Future.future();

            redisClient.get(taskID, r2 -> {
                if (r2.succeeded()) {
                    String data = r2.result();
                    try {
                        JobConf jobConf = new JobConf(new JsonObject(data));
                        result.complete(jobConf);
                    } catch (Exception e) {
                        result.fail(e);
                    }
                } else {
                    result.fail(r2.cause());
                }
            });

            return result;
        }).compose(jobConf -> {
            Future<String> r = Future.future();

            if (JobConf.isSingleJob(jobConf)) {
                long fireTime = 1;
                redisClient.setex(Prefix.fire + taskID, fireTime, "1", r.completer());
            } else {
                r.fail("该任务是周期任务，忽略失败");
            }
            return r;
        }).setHandler(r -> {
            if (r.succeeded()) {
                logger.info("任务重启成功");
            } else {
                logger.warn(r.cause().getMessage());
            }
        });


    }
}
