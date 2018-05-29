package com.jsen.redis.schedule.worker;

import com.jsen.redis.schedule.worker.executer.JExecutor;
import com.jsen.redis.schedule.worker.service.ExecEndpoint;
import com.jsen.redis.schedule.master.Prefix;
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class WorkerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(WorkerVerticle.class);


    private String selfAddress = "null";

    private RedisClient redis;

    public WorkerVerticle() {
        initSign();
    }
    @Override
    public void start(Future<Void> startFuture) {
        logger.info("*** Vertx start ***");

        RedisOptions config = new RedisOptions().setHost("127.0.0.1");

        redis = RedisClient.create(vertx, config);
        new JExecutor();


        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        // router.route().failureHandler(this::notFound);
        cros(router.route());

        try {
            selfAddress = InetAddress.getLocalHost().getHostAddress() + ":" + config().getInteger("port", 9567);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            startFuture.fail(e);
            return;
        }

        vertx.setPeriodic(60000, t -> {
            redis.setex(Prefix.worker + selfAddress, 61, JExecutor.getDefaultJExecutor().getPoolSize() + "", ar -> {});
        });

        vertx.setPeriodic(60000, l -> {
            List<JExecutor.Pair> jobs = JExecutor.getDefaultJExecutor().getJobs();

            synchronized (JExecutor.getDefaultJExecutor().mutex) {
                jobs.forEach(item -> redis.setex(Prefix.task + item.getTaskID(), 6000, selfAddress, r -> {}));
            }
            /*
            JExecutor.getDefaultJExecutor().lock();
            CompositeFuture.all(jobs.stream().map(item -> {
                Future<Void> future = Future.future();
                redis.setex(Prefix.task + item.getTaskID(), 6000, selfAddress, r -> future.complete());
                return future;
            }).collect(Collectors.toList())).setHandler(r -> JExecutor.getDefaultJExecutor().unLock());*/
        });

        // System.out.println(Future.future().getClass());

        ExecEndpoint execEndpoint = new ExecEndpoint(redis, selfAddress);

        /*
         * work节点 启动暂停任务
         */
        router.route("/job/start/:taskID").handler(execEndpoint::start);
        router.route("/job/stop/:taskID").handler(execEndpoint::stop);
        // io.vertx.core.impl.FutureImpl
        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("port", 3003));
        startFuture.complete();

        logger.info("*** Vertx start succeed ***");
    }



    @Deprecated
    public void updateSize() {
        redis.setex("worker:" + selfAddress, 61, JExecutor.getDefaultJExecutor().getPoolSize() + "", ar -> {});
    }

    public static void main(String[] args) {


        VertxOptions vO = new VertxOptions();
        vO.setEventLoopPoolSize(16);
        Vertx vertx = Vertx.vertx(vO);

        DeploymentOptions dO = new DeploymentOptions();
        dO.setInstances(1);
        dO.setConfig(new JsonObject().put("port", 3003));

        WorkerVerticle workerVerticle = new WorkerVerticle();

        RxHelper.deployVerticle(vertx, workerVerticle, dO).subscribe(id -> {
                    logger.info(id);
                    workerVerticle.redis.setex("worker:" + workerVerticle.selfAddress, 61, JExecutor.getDefaultJExecutor().getPoolSize() + "", ar -> {});
                },
                e -> logger.error(e.getMessage()));
    }

    /**
     * 退出清理redis缓存数据
     */
    private void initSign() {

        /*
        // 创建一个信号处理器
        sun.misc.SignalHandler handler = signal -> {
            Future<Void> future = Future.future();
            stop(future);
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    logger.info("succeed ctrl c shutdown clean vertx distribution");
                    System.exit(0);
                } else {
                    ar.cause().printStackTrace();
                    logger.error("failed ctrl c shutdown clean vertx distribution");
                    System.exit(1);
                }
            });
        };
        // 设置INT信号(Ctrl+C中断执行)交给指定的信号处理器处理，废掉系统自带的功能
        sun.misc.Signal.handle(new sun.misc.Signal("INT"), handler);
        */
    }

    /**
     * Stop the verticle.<p>
     * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
     * If your verticle does things in its shut-down which take some time then you can override this method
     * and call the stopFuture some time later when clean-up is complete.
     *
     * @param stopFuture a future which should be called when verticle clean-up is complete.
     * @throws Exception
     */
    @Override
    public void stop(Future<Void> stopFuture) {
        Future<Long> future = Future.future();
        redis.del(Prefix.worker + selfAddress, future.completer());
        future.compose(r -> {
            Future<Void> f = Future.future();
            redis.close(f);
            return f;
        }).setHandler(stopFuture.completer());
    }

    private void cros(Route route) {
        Set<String> allowHeaders = new HashSet<>();

        allowHeaders.add("authorization");
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        allowHeaders.add("X-PINGARUNER");

        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);
        allowMethods.add(HttpMethod.PUT);

        route.handler(CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethods(allowMethods));
    }
}