package com.jsen.redis.schedule.master.service.endpoint;

import com.jsen.redis.schedule.master.Prefix;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;

import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/26
 */
public class JobStatusEndpoit {
   private final RedisClient redisClient;

    public JobStatusEndpoit(RedisClient redisClient, Router router) {
        this.redisClient = redisClient;

        /*
         * 列出所有添加的Job
         */
        router.route("/api/list/job").handler(this::listJobs);
        /*
         * 列出正在调度的任务
         */
        router.route("/api/list/schedule").handler(this::listTasks);

    }

    private void listJobs(RoutingContext routingContext) {
        list(Prefix.taskConf + "*").setHandler(list -> {
            if (list.succeeded()) {
                JsonObject objs = new JsonObject();
                list.result().forEach(entry -> objs.put(entry.getKey(), new JsonObject(entry.getValue().toString())));
                resultData(routingContext, objs.toString());

            } else {
                resultData(routingContext, new JsonObject().put("code", 1)
                        .put("msg", "获取数据失败：" + list.cause().getMessage()));
            }
        });
    }

    private void listTasks(RoutingContext routingContext) {
        list(Prefix.schedule + "*").setHandler(list -> {
            if (list.succeeded()) {
                JsonObject objs = new JsonObject();
                list.result().forEach(entry -> objs.put(entry.getKey(), entry.getValue()));
                resultData(routingContext, objs.toString());

            } else {
                resultData(routingContext, new JsonObject().put("code", 1)
                        .put("msg", "获取数据失败：" + list.cause().getMessage()));
            }
        });
    }


    private Future<JsonObject> list(String pat) {
        Future<JsonObject> future = Future.future();
        redisClient.keys(pat, r -> {
            if (r.succeeded()) {
                JsonObject rst = new JsonObject();
                CompositeFuture.all(r.result().stream().map(item -> {
                    Future<Void> result = Future.future();
                    redisClient.get(item.toString(), r2 -> {
                        if (r2.succeeded()) {
                            rst.put(item.toString(), r2.result());
                            result.complete();
                        } else {
                            result.fail(r2.cause());
                        }
                    });
                    return result;
                }).collect(Collectors.toList())).setHandler(r3 -> {
                    future.complete(rst);
                });
            } else {
                future.fail(r.cause());
            }
        });
        return future;
    }

    private <T> void resultData(RoutingContext context, T data) {
        context.response()
                .putHeader("content-type", "application/json")
                .end(data.toString());
    }

}
