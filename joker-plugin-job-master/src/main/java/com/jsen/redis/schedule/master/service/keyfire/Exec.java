package com.jsen.redis.schedule.master.service.keyfire;

import com.jsen.redis.schedule.master.Prefix;
import com.jsen.redis.schedule.master.service.CronHelp;
import com.jsen.redis.schedule.master.task.JobConf;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *     任务触发
 *     首先检查任务是否正在调度，如果正在调度 检查任务是否为周期任务，周期任务则设置下一个触发fire，最后执行任务(（分发任务）
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
public class Exec {

    private static final Logger logger = LoggerFactory.getLogger(Exec.class);

    private RedisClient redisClient;
    private WebClient webClient;

    public Exec(RedisClient redisClient, WebClient webClient) {
        this.redisClient = redisClient;
        this.webClient = webClient;
    }

    public void exec(String taskID) {
        Future<Void> res0 = Future.future();
        redisClient.get(Prefix.schedule + taskID, r -> {
            if (r.succeeded()) {
                String rs = r.result();
                if (rs == null) {
                    res0.fail("任务未在执行，忽略fire事件");
                } else {
                    res0.complete();
                }
            } else {
                res0.fail(r.cause());
            }
        });

        res0.compose(r0 -> {
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


            long fireTime = 1;
            if (!JobConf.isSingleJob(jobConf)) {
                fireTime = (CronHelp.getScheduleTime(jobConf.getCron(), new Date(System.currentTimeMillis() + 999)).getTime() - System.currentTimeMillis()) / 1000;
            }

            if (fireTime < 1) {
                fireTime = 1;
            }

            logger.debug("下一个执行时间：" + fireTime);

            Future<String> r = Future.future();

            redisClient.setex(Prefix.fire + taskID, fireTime, "1", r.completer());


            return r;
        }).compose(resu -> {
            logger.error("set result:" + resu);
            Future<List<String>> addressList = Future.future();
            redisClient.keys(Prefix.worker + "*", r -> {
                if (r.succeeded()) {
                    addressList.complete(r.result().stream().map(Object::toString).collect(Collectors.toList()));
                } else {
                    addressList.fail(r.cause());
                }
            });
            return addressList;
        }).compose(addressList -> CompositeFuture.any(addressList.stream().map(address -> {
            Future f = Future.future();
            String[] adArray = address.split(":");
            webClient.get(Integer.valueOf(adArray[2]), adArray[1], "/job/start/" + taskID).send(ar -> {
                if (ar.succeeded()) {
                    String res = ar.result().bodyAsString();
                    if ("ok".equals(res)) {
                        f.complete();
                    } else if (!"skip".equals(res)){
                        f.fail(res);
                    } else {
                        f.fail("skip");
                    }
                } else {
                    f.fail("开始任务失败 网络错误:" + ar.cause().getMessage());
                }
            });
            return f;
        }).collect(Collectors.toList()))).setHandler(r -> {
            if (r.succeeded()) {
                logger.info("开始任务成功，任务ID：" + taskID);

            } else {
                logger.error("开始任务失败：" + r.cause().getMessage());
                r.cause().printStackTrace();
            }
        });

    }
}
