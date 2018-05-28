package com.jsen.redis.schedule.master.service;

import com.jsen.redis.schedule.master.service.keyfire.Exec;
import com.jsen.redis.schedule.master.service.keyfire.WorkerExp;
import com.jsen.redis.schedule.master.task.JobConf;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
@Deprecated
public class RedisHelp {

    private static final Logger logger = LoggerFactory.getLogger(RedisHelp.class);

    public RedisClient redis;
    public WebClient webClient;

    public RedisHelp(RedisClient redis, WebClient webClient, Vertx vertx) {
        this.redis = redis;
        this.webClient = webClient;

        Exec exec = new Exec(redis, webClient);
        WorkerExp workerExp = new WorkerExp(redis);


        vertx.eventBus().<JsonObject>consumer("io.vertx.redis.__key*__:*", received -> {
            // do whatever you need to do with your message
            JsonObject rec = received.body();
            if ("ok".equals(rec.getString("status", "no"))) {
                String key = rec.getJsonObject("value", new JsonObject()).getString("message", "");

                if (!"".equals(key)) {
                    String head = key.substring(0, key.indexOf(":"));
                    String id = key.substring(key.indexOf(":") + 1, key.length());
                    switch (head) {
                        case "fire":
                            exec.exec(id);
                            break;
                        case "task":
                            workerExp.exec(id);
                            break;
                            default:
                    }
                }
            }
        });
    }

    public Future<List<String>> getAllWorkers() {
        Future<List<String>> result = Future.future();
        redis.keys("worker:*", ar -> {
            if (ar.succeeded()) {
                if (ar.result().isEmpty()) {
                    result.fail("没有可用worker节点");
                } else {
                    result.complete(ar.result().stream().map(Object::toString).collect(Collectors.toList()));
                }
            } else {
                result.fail("无法获取worker节点");
            }
        });
        return result;
    }

    public Future<String> getMinJobNode(List<String> array) {
        Future<String> result = Future.future();
        if (array.size() == 0) {
            result.fail("没有可用worker节点");
            return result;
        }

        AtomicInteger atomicInteger = new AtomicInteger(Integer.MAX_VALUE);
        AtomicReference<String> minAddress = new AtomicReference<>("");
        CompositeFuture.all(array.stream().map(item -> {
            Future future = Future.future();
            redis.get(item, ar -> {
                if (ar.succeeded()) {
                    try {
                        Integer size = Integer.valueOf(ar.result());
                        int current = atomicInteger.get();
                        if (current > size) {
                            minAddress.set(item);
                            atomicInteger.compareAndSet(current, size);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                future.complete();
            });
            return future;
        }).collect(Collectors.toList())).setHandler(ar -> {
            if (ar.succeeded()) {
                if (!"".equals(minAddress.get())) {
                    result.complete(minAddress.get());
                } else {
                    result.complete(array.get(new Random().nextInt(array.size())));
                }
            } else {
                result.complete(array.get(new Random().nextInt(array.size())));
            }
        });
        return result;
    }

    public Future<JobConf> getJobConf(String taskID) {
        Future<JobConf> result = Future.future();
        redis.get(taskID, r -> {
            if (r.succeeded()) {
                try {
                    try {
                        JsonObject jsonObject = new JsonObject(r.result());
                        JobConf jobConf = new JobConf(jsonObject);
                        result.complete(jobConf);
                    } catch (Exception e) {
                        result.fail(e);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result.fail(e);
                }
            } else {
                result.fail(r.cause());
            }
        });
        return result;
    }

    /**
     * 创建fire 过期调用
     * @param taskID
     * @param nextFire
     * @return
     */
    public Future<Void> createJobFire(String taskID, long nextFire) {

        Future<Void> result = Future.future();
        redis.setex("fire:" + taskID, nextFire, taskID, a -> {
            if (a.succeeded()) {
                result.complete();
            } else {
                result.fail(a.cause());
            }
        });
        return result;
    }

    public Future<Void> delFire(String taskID) {
        logger.error("set delFire" + "del:fire:" + taskID);
        Future<Void> future = Future.future();
        redis.set("del:fire:" + taskID, "1", future.completer());
        return future;
    }
}
