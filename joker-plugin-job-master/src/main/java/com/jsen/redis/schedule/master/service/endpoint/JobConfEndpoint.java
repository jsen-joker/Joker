package com.jsen.redis.schedule.master.service.endpoint;

import com.jsen.redis.schedule.master.Prefix;
import com.jsen.redis.schedule.master.service.CronHelp;
import com.jsen.redis.schedule.master.task.JobConf;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/26
 */
public class JobConfEndpoint {

    public static final Logger logger = LoggerFactory.getLogger(JobConfEndpoint.class.getName());

    private final RedisClient redisClient;
    private final WebClient webClient;

    public JobConfEndpoint(RedisClient redisClient, WebClient webClient) {
        this.redisClient = redisClient;
        this.webClient = webClient;
    }

    public void add(RoutingContext routingContext) {

        HttpServerRequest request = routingContext.request();
        String name = request.getParam("name");
        String cron = request.getParam("cron");
        String jobData = request.getParam("jobData");
        String staticJob = request.getParam("staticJob");
        String NEW = request.getParam("isNew");
        String ind = request.getParam("index");
        String id = (name == null || "".equals(name)) ? UUID.randomUUID().toString() : name;
        String taskID = Prefix.taskConf + id;

        boolean isNew = "true".equals(NEW);
        long index = 0;
        if (ind != null && !isNew) {
            try {
                index = Long.valueOf(ind);
            } catch (Exception e) {}
        }

        boolean isStaticJob = (!"false".equals(staticJob));

        if (isNull(routingContext, name, "名字不能为空") ||
                isNull(routingContext, jobData, "jobData不能为空")) {
            return;
        }
        if (cron != null && !"".equals(cron)) {
            Date date = CronHelp.getScheduleTime(cron, new Date());
            if (date == null) {
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 1).put("msg", "请输入cron，或非法的Cron数据").toString());
                return;
            }
        }

        // param is ok
        // boolean staticJob, String cron, String jobData, String taskID
        JobConf jobConf = new JobConf(isStaticJob, cron, jobData, taskID, index);
        logger.info("创建任务配置文件：" + jobConf.toJson());
        if (isNew) {
            redisClient.setnx(taskID, jobConf.toJson().toString(), r -> {
                if (r.succeeded()) {
                    if (r.result() == 0) {
                        logger.warn("添加任务到redis失败，任务ID：" + id);
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(new JsonObject().put("code", 1).put("msg", "添加任务失败，请检查任务是否存在").toString());
                    } else {
                        logger.info("添加任务到redis成功，任务ID：" + id);
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(new JsonObject().put("code", 0).put("taskID", id).toString());
                    }
                } else {
                    logger.error("添加任务到redis失败，任务ID：" + id);
                    r.cause().printStackTrace();
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("code", 1).put("msg", r.cause().getMessage()).toString());
                }
            });
        } else {
            redisClient.set(taskID, jobConf.toJson().toString(), r -> {
                if (r.succeeded()) {
                    logger.info("更新任务到redis成功，任务ID：" + id);
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                                .end(new JsonObject().put("code", 0).put("taskID", id).toString());
                } else {
                    logger.error("更新任务到redis失败，任务ID：" + id);
                    r.cause().printStackTrace();
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("code", 1).put("msg", r.cause().getMessage()).toString());
                }
            });
        }
    }

    public void del(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("taskID");
        String taskID = Prefix.taskConf + id;

        Future<Void> future = Future.future();
        doStop(taskID).setHandler(r -> {
            if (r.succeeded()) {
                future.complete();
            } else {
                if ("skip".equals(r.cause().getMessage())) {
                    future.complete();
                } else {
                    future.fail(r.cause());
                }
            }
        });
        future.compose(cf -> {
            Future<Long> f = Future.future();
            redisClient.del(taskID, f.completer());
            return f;
        }).setHandler(r -> {
            if (r.succeeded()) {
                logger.info("删除任务成功，任务ID：" + id);
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 0).put("taskID", id).toString());
            } else {
                logger.error("删除任务失败，任务ID：" + id);
                r.cause().printStackTrace();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 1).put("msg", r.cause().getMessage()).toString());
            }

        });
    }

    /**
     * 获取任务配置文件
     * 生成相应的redis fire
     * @param routingContext
     */
    public void start(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("taskID");
        String taskID = Prefix.taskConf + id;

        Future<Void> result = Future.future();
        redisClient.get(Prefix.schedule + taskID, r -> {
            if (r.succeeded()) {
                if (r.result() != null) {
                    result.fail("任务存在");
                } else {
                    result.complete();
                }
            } else {
                result.fail(r.cause());
            }
        });
        result.compose(r-> {
            Future<JobConf> result2 = Future.future();

            redisClient.get(taskID, r2 -> {
                if (r2.succeeded()) {
                    String data = r2.result();
                    try {
                        JobConf jobConf = new JobConf(new JsonObject(data));
                        result2.complete(jobConf);
                    } catch (Exception e) {
                        result2.fail("获取任务配置信息失败");
                    }
                } else {
                    result2.fail(r2.cause());
                }
            });
            return result2;
        }).compose(jobConf -> {
            long fireTime = 1;
            if (!JobConf.isSingleJob(jobConf)) {
                fireTime = (CronHelp.getScheduleTime(jobConf.getCron(), new Date()).getTime() - System.currentTimeMillis()) / 1000;
            }

            if (fireTime < 1) {
                fireTime = 1;
            }
            long fFireTime = fireTime;

            Future<String> setFire = Future.future();
            Future<Void> setSchedule = Future.future();
            redisClient.setex(Prefix.fire + taskID, fireTime, "1", setFire.completer());
            redisClient.set(Prefix.schedule + taskID, "1", setSchedule.completer());

            Future<Long> rs = Future.future();
            CompositeFuture.all(setFire, setSchedule).setHandler(a -> {
                if (a.succeeded()) {
                    rs.complete(fFireTime);
                } else {
                    redisClient.del(Prefix.fire + taskID, n -> {});
                    redisClient.del(Prefix.schedule + taskID, n -> {});
                    rs.fail(a.cause());
                }
            });
            return rs;
        }).setHandler(r -> {
            if (r.succeeded()) {
                logger.error("启动任务成功， 任务将在" + r.result() + "秒后开启");
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 0).put("taskID", id).toString());

            } else {
                logger.error("启动任务失败，任务ID：" + id);
                r.cause().printStackTrace();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 1).put("msg", r.cause().getMessage()).toString());
            }
        });


        /*
        Future<List<String>> result = Future.future();
        redisClient.keys(Prefix.worker + "*", r -> {
            if (r.succeeded()) {
                result.complete(r.result().stream().map(Object::toString).collect(Collectors.toList()));
            } else {
                result.fail(r.cause());
            }
        });
        result.compose(addressList -> CompositeFuture.any(addressList.stream().map(address -> {
            Future f = Future.future();
            String[] adArray = address.split(":");
            webClient.get(Integer.valueOf(adArray[2]), adArray[1], "/job/start/" + taskID).send(ar -> {
                if (ar.succeeded()) {
                    String res = ar.result().bodyAsString();
                    if ("ok".equals(res)) {
                        logger.info("开始任务成功");
                        f.complete();
                    } else if (!"skip".equals(res)){
                        logger.error("开始任务失败：" + res);
                        f.fail(res);
                    } else {
                        f.fail("skip");
                    }
                } else {
                    logger.error("开始任务失败 网络错误:" + ar.cause().getMessage());
                    f.fail(ar.cause());
                }
            });
            return f;
        }).collect(Collectors.toList()))).setHandler(r -> {
            if (r.succeeded()) {
                logger.error("开始任务成功，任务ID：" + id);
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 0).put("taskID", id).toString());

            } else {
                logger.error("开始任务失败，任务ID：" + id);
                r.cause().printStackTrace();
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 1).put("msg", r.cause().getMessage()).toString());
            }
        });
        */
    }

    public void stop(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("taskID");
        String taskID = Prefix.taskConf + id;

        doStop(taskID).setHandler(r -> {
            if (r.succeeded()) {
                logger.error("停止任务成功，任务ID：" + id);
                routingContext.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 0).put("taskID", id).toString());
            } else {
                if ("skip".equals(r.cause().getMessage())) {
                    logger.warn("没有正在执行的任务");
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("code", 0).put("msg", "没有正在执行的任务").toString());

                } else {
                    logger.error("停止任务失败，任务ID：" + id);
                    r.cause().printStackTrace();
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("code", 1).put("msg", r.cause().getMessage()).toString());
                }

            }
        });

    }

    private Future<CompositeFuture> doStop(String taskID) {

        Future<CompositeFuture> result = Future.future();
        Future<Long> delFire = Future.future();
        redisClient.del(Prefix.fire + taskID, delFire.completer());
        Future<Long> delSchedule = Future.future();
        redisClient.del(Prefix.schedule + taskID, delSchedule.completer());
        CompositeFuture.all(delFire, delSchedule).compose(v -> {
            Future<List<String>> f = Future.future();
            redisClient.keys(Prefix.worker + "*", r -> {
                if (r.succeeded()) {
                    f.complete(r.result().stream().map(Object::toString).collect(Collectors.toList()));
                } else {
                    f.fail(r.cause());
                }
            });
            return f;
        }).compose(addressList -> CompositeFuture.any(addressList.stream().map(address -> {
            Future f = Future.future();
            String[] adArray = address.split(":");
            webClient.get(Integer.valueOf(adArray[2]), adArray[1], "/job/stop/" + taskID).send(ar -> {
                if (ar.succeeded()) {
                    String res = ar.result().bodyAsString();
                    if ("ok".equals(res)) {
                        logger.info("停止任务成功");
                        f.complete();
                    } else if (!"skip".equals(res)){
                        logger.error("停止任务失败：" + res);
                        f.fail(res);
                    } else {
                        f.fail("skip");
                    }
                } else {
                    logger.error("停止任务失败 网络错误:" + ar.cause().getMessage());
                    // f.fail(ar.cause());
                    f.fail("skip");
                }
            });
            return f;
        }).collect(Collectors.toList()))).setHandler(result.completer());
        return result;
    }




    private boolean isNull(RoutingContext routingContext, String s, String comment) {
        if (s == null) {
            routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("code", 1).put("msg", comment).toString());
            return true;
        }
        return false;
    }

}
