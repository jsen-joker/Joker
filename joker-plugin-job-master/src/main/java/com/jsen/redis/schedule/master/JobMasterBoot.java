package com.jsen.redis.schedule.master;

import com.jsen.redis.schedule.master.service.endpoint.JobStatusEndpoit;
import com.jsen.redis.schedule.master.service.keyfire.KeyFireInit;
import com.jsen.redis.schedule.master.service.endpoint.JobConfEndpoint;
import com.jsen.test.common.joker.JokerStaticHandlerImpl;
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class JobMasterBoot extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(JobMasterBoot.class);
    WebClient webClient;
    private RedisClient redis;

    public JobMasterBoot() {
        this.initSign();
    }

    @Override
    public void start(Future<Void> startFuture) {
        logger.info("*** Vertx start ***");

        RedisOptions config = new RedisOptions().setHost("127.0.0.1");

        redis = RedisClient.create(vertx, config);
        webClient = WebClient.create(vertx);

        KeyFireInit.init(vertx.eventBus(), redis, webClient);


        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        // router.route().failureHandler(this::notFound);
        cros(router.route());

        JobConfEndpoint jobConfEndpoint = new JobConfEndpoint(redis, webClient);

        /*
         * 在Redis中添加或删除 任务
         */
        router.route("/job/add").handler(jobConfEndpoint::add);
        router.route("/job/del/:taskID").handler(jobConfEndpoint::del);

        /*
         * work节点 启动暂停任务
         */
        router.route("/job/start/:taskID").handler(jobConfEndpoint::start);
        router.route("/job/stop/:taskID").handler(jobConfEndpoint::stop);
        StaticHandler staticHandler = new JokerStaticHandlerImpl(this.getClass());

        router.route("/*").handler(staticHandler);

        new JobStatusEndpoit(redis, router);

        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("port", 3001));
        startFuture.complete();

        logger.info("*** Vertx start succeed ***");
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
        sun.misc.Signal.handle(new sun.misc.Signal("INT"), handler);*/
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

        // 不清理 taskconf
        clear(Prefix.task)
                .compose(c -> clear(Prefix.fire))
                .compose(c -> clear(Prefix.schedule))
                .compose(c -> clear(Prefix.worker))
                .compose(c -> clear(Prefix.lock)).setHandler(r -> {
            webClient.close();
            redis.close(stopFuture.completer());
        });
    }
    private Future<CompositeFuture> clear(String prefix) {
        Future<CompositeFuture> ok = Future.future();
        redis.keys(prefix + "*", r -> {
            if (r.succeeded()) {
                CompositeFuture.all(r.result().stream().map(Object::toString).map(key -> {
                    Future<Void> future = Future.future();
                    redis.del(key, res -> {
                        future.complete();
                    });
                    return future;
                }).collect(Collectors.toList())).setHandler(ok.completer());
            }
        });
        return ok;
    }

    public static void main(String[] args) {


        VertxOptions vO = new VertxOptions();
        vO.setEventLoopPoolSize(16);
        Vertx vertx = Vertx.vertx(vO);

        DeploymentOptions dO = new DeploymentOptions();
        dO.setInstances(1);

        RxHelper.deployVerticle(vertx, new JobMasterBoot(), dO).subscribe(logger::info,
                e -> logger.error(e.getMessage()));
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

        route.handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders).allowedMethods(allowMethods));
    }
}
