package com.jsen.joker.plugin.gateway.mirren;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jsen.joker.boot.cloader.context.EntryContext;
import com.jsen.joker.plugin.gateway.BootGateWay;
import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
import com.jsen.joker.plugin.gateway.mirren.service.AppService;
import com.jsen.joker.plugin.gateway.mirren.service.impl.AppServiceImpl;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <p>
 *     包括项目、api 部署  启动，gateway的核心eventbus实现
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class DeployVerticle extends RestVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployVerticle.class);

    private Map<String, ApplicationVerticle> applicationVerticleMap = Maps.newHashMap();


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.startSimple(startFuture);
        deployVerticle = this;
        vertx.eventBus().consumer(EventKey.App.DEPLOY, this::deploymentAPP);
        vertx.eventBus().consumer(EventKey.App.UNDEPLOY, this::unDeploymentAPP);
        deployAllApp().setHandler(startFuture.completer());
    }

    private void deploymentAPP(Message<JsonObject> msg) {


        JsonObject js = msg.body();
        App app = new App(js);
        app.setOn(true);
        app.setApis(app.getApis().stream().filter(Api::isOn).collect(Collectors.toSet()));
        AppServiceImpl.appService.updateAppState(app.getName(), true, r0 -> {
            if (r0.succeeded()) {
                _deploymentAPP(app, r -> {
                    if (r.succeeded()) {
                        msg.reply(0);
                    } else {
                        msg.fail(1, r.cause().getMessage());
                    }
                });
            } else {
                msg.fail(1, r0.cause().getMessage());
            }
        });
    }

    private void _deploymentAPP(App app, Handler<AsyncResult<Void>> resultHandler) {


        ApplicationVerticle applicationVerticle = applicationVerticleMap.get(app.getName());
        if (applicationVerticle != null) {
            resultHandler.handle(Future.failedFuture("app with name : " + app.getName() + " exist"));
            return;
        }

        AtomicBoolean founded = new AtomicBoolean(false);
        applicationVerticleMap.forEach((k, v) -> {
            if (v.getApp().getPort().equals(app.getPort())) {
                founded.set(true);
            }
        });
        if (founded.get()) {
            resultHandler.handle(Future.failedFuture("app with port : " + app.getPort() + " exist"));
            return;
        }

        DeploymentOptions options = new DeploymentOptions();
        options.setIsolationGroup(app.getName());
        options.setConfig(new JsonObject().put("app", app.toJson()));
        vertx.deployVerticle(ApplicationVerticle.class.getName(), options, res -> {
            if (res.succeeded()) {
                LOGGER.info("deploy app succeed : " + app.getName());
                resultHandler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("deploy app failed : " + res.cause().getMessage());
                LOGGER.error(app.getName());
                resultHandler.handle(Future.failedFuture(res.cause()));

            }
        });
    }

    private void unDeploymentAPP(Message<String> msg) {
        String name = msg.body();
        ApplicationVerticle applicationVerticle = applicationVerticleMap.get(name);
        if (applicationVerticle == null) {
            msg.reply(0);
            LOGGER.error("app with name : " + name + " not exist");
            return;
        }


        Future<ResponseBase> result = Future.future();
        result.setHandler(r0 -> {
            if (r0.succeeded()) {

                vertx.undeploy(applicationVerticle.deploymentID(), res -> {
                    if (res.succeeded()) {
                        applicationVerticleMap.remove(name);
                        LOGGER.info("unDeploy app succeed : " + name);
                        msg.reply(0);
                    } else {
                        LOGGER.error("unDeploy app failed : " + res.cause().getMessage());
                        msg.fail(1, res.cause().getMessage());
                    }
                });

            } else {
                msg.fail(1, r0.cause().getMessage());
            }
        });

        applicationVerticle.undeployAllApi().compose(o -> {
            AppServiceImpl.appService.updateAppState(name, false, result.completer());
        }, result);
    }

    private Future<Void> deployAllApp() {
        Future<Void> result = Future.future();
        AppService appService = AppServiceImpl.appService;

        appService.listAppSimple(r -> {
            if (r.succeeded()) {
                List<Future> tasks = Lists.newArrayList();

                List<JsonObject> jsonObjects = r.result();
                List<App> apps = jsonObjects.stream().map(item -> new App(new JsonObject(item.getString("metas")))).filter(App::isOn).peek(item ->
                        item.setApis(item.getApis().stream().filter(Api::isOn).collect(Collectors.toSet())))
                        .collect(Collectors.toList());

                if (apps.isEmpty()) {
                    result.complete();
                    return;
                }

                apps.forEach(item -> {
                    Future<Void> future = Future.future();
                    tasks.add(future);
                    _deploymentAPP(item, future.completer());
                });

                CompositeFuture.all(tasks).setHandler(r2 -> {
                    if (r2.succeeded()) {
                        result.complete();
                    } else {
                        result.fail(r.cause());
                    }
                });

            } else {
                result.complete();
            }
        });
        return result;
    }


    public void registerApp(ApplicationVerticle applicationVerticle) {
        applicationVerticleMap.put(applicationVerticle.getApp().getName(), applicationVerticle);
    }

    public Map<String, ApplicationVerticle> getApplicationVerticleMap() {
        return applicationVerticleMap;
    }

    private static DeployVerticle deployVerticle;
    public static DeployVerticle getInstance() {
        return deployVerticle;
    }

}
