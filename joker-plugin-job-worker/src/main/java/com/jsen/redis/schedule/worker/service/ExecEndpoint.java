package com.jsen.redis.schedule.worker.service;

import com.jsen.redis.schedule.worker.executer.JExecutor;
import com.jsen.redis.schedule.worker.redis.RedisLock;
import com.jsen.redis.schedule.master.Prefix;
import com.jsen.redis.schedule.master.task.JobConf;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/26
 */
public class ExecEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ExecEndpoint.class.getName());

    private final RedisClient redisClient;
    private final RedisLock redisLock;
    private final String selfAddress;

    public ExecEndpoint(RedisClient redisClient, String selfAddress) {
        this.redisClient = redisClient;
        this.selfAddress = selfAddress;
        redisLock = new RedisLock(redisClient);
    }

    public void start(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String taskID = request.getParam("taskID");
        String key = Prefix.task + taskID;


        Future<String> result=Future.future();
        result.setHandler(a->{
            if (a.succeeded()) {
                routingContext.response().setStatusCode(200).end("ok");
            } else {
                logger.error(a.cause().getMessage());
                a.cause().printStackTrace();
                routingContext.response().setStatusCode(200).end(a.cause().getMessage());
            }
        });


        redisLock.getLock(key)
                .compose(ok -> checkCanExec(taskID, key))
                .compose(ok -> {
                    Future<JobConf> jobConfFuture = Future.future();
                    redisClient.get(taskID, r -> {
                        if (r.succeeded()) {
                            try {
                                JobConf jobConf = new JobConf(new JsonObject(r.result()));
                                jobConfFuture.complete(jobConf);
                            } catch (Exception e) {
                                jobConfFuture.fail(e);
                            }
                        } else {
                            jobConfFuture.fail(r.cause());
                        }
                    });
                    return jobConfFuture;
                }).compose(job -> {
            if (job != null) {
                try {
                    if (JExecutor.getDefaultJExecutor().exec(job, redisClient)) {
                        result.complete("任务：" + taskID + "开始执行");
                    } else {
                        result.fail("任务：" + taskID + "存在");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result.fail(e);
                    redisClient.del(key, ar -> {});
                }
            } else {
                result.fail("请检查Job是否正在执行，ID：" + taskID);
            }
        }, result);
    }

    public void stop(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String taskID = request.getParam("taskID");

        stop(taskID).setHandler(r -> {
            if (r.succeeded()) {
                routingContext.response().setStatusCode(200).end(r.result());
            } else {
                routingContext.response().setStatusCode(200).end(r.cause().getMessage());
            }
        });
    }
    private Future<String> stop(String taskID) {
        String key = Prefix.task + taskID;

        Future<String> result = Future.future();
        redisLock.getLock(key).compose(ok -> checkAndDel(key))
                .compose(ok -> {
                    Future<Long> r = Future.future();
                    JExecutor.getDefaultJExecutor().stop(taskID);
                    redisClient.del(key, r.completer());
                    return r;
                }).setHandler(a -> {
            if (a.succeeded()) {
                result.complete("ok");
            } else {
                logger.error(a.cause().getMessage());
                a.cause().printStackTrace();
                result.complete(a.cause().getMessage());
            }
        });
        return result;

    }

    /**
     * 检查 task:
     * 如果为空 设置为本地址 返回成功
     * 如果和本地址相等 返回 已存在 failed
     * 如果地址不相等 返回 skip failed
     * @param taskID
     * @param key task:taskID:
     * @return
     */
    private Future<String> checkCanExec(String taskID, String key) {
        Future<String> future = Future.future();
        redisClient.get(key, ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
                logger.error("redis错误，尝试获取work失败：" + ar.cause().getMessage());
                return;
            }

            String currentAddress = ar.result();
            if (currentAddress == null) {
                // 十分钟 过期
                redisClient.setex(key, 600, selfAddress, ar2 -> {
                    if (ar2.succeeded()) {
                        future.complete("ok");
                    } else {
                        logger.error("redis错误，无法设置work地址为该worker的地址：" + ar.cause().getMessage());
                        future.complete("redis错误，无法设置work地址为该worker的地址：" + ar.cause().getMessage());
                    }
                });
            } else {
                if (selfAddress.equals(currentAddress)) {
                    stop(taskID).setHandler(r -> {
                        if (r.succeeded()) {
                            future.complete("ok");
                        } else {
                            logger.warn("任务在该主机运行，无法停止该任务，任务将重复运行");
                            r.cause().printStackTrace();
                            future.complete("ok");
                        }
                    });
                } else {
                    future.fail("skip");
                }
            }
        });
        return future;
    }

    /**
     * 检查 task:
     * 如果为空  任务不存在 ok
     * 如果地址不相等 skip failed
     * 如果地址相等 ok succeed
     * @param key task:taskID:
     * @return
     */
    private Future<String> checkAndDel(String key) {
        Future<String> future = Future.future();
        redisClient.get(key, ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
                logger.error("redis错误，尝试获取work失败：" + ar.cause().getMessage());
                return;
            }

            String currentAddress = ar.result();
            if (currentAddress == null) {
                future.complete("ok");
            } else {
                if (selfAddress.equals(currentAddress)) {
                    future.complete("ok");
                } else {
                    future.fail("skip");
                }
            }
        });
        return future;
    }
}
