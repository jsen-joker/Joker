package com.jsen.joker.plugin.gateway.mirren;

import com.google.common.collect.Lists;
import com.hazelcast.util.StringUtil;
import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.lifecycle.*;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
import com.jsen.joker.plugin.gateway.mirren.service.impl.AppServiceImpl;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *     类型2的api网关服务
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class ApplicationVerticle extends RestVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationVerticle.class);

    private ApiMap apiMap = new ApiMap();
    private App app;

    /** http客户端 */
    private HttpClient httpClient = null;

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     * @param startFuture
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        this.httpClient = vertx.createHttpClient();

        app = new App(config().getJsonObject("app"));

        router.route().handler(this::filterBlackIP);
        router.get("/ok").handler(r -> resultJSON(r, ResponseBase.create().code(0)));

        vertx.eventBus().consumer(app.getName() + ":" + EventKey.App.Api.ADD, this::deployApi);
        vertx.eventBus().consumer(app.getName() + ":" + EventKey.App.Api.DEL, this::undeployApi);

        config().put("app.name", app.getName());
        config().put("http.host", app.getHost());
        config().put("http.port", app.getPort());

        autoDeployAllApi(r -> {
            if (r.succeeded()) {
                startServer(startFuture);
                DeployVerticle.getInstance().registerApp(this);
            } else {
                startFuture.fail(r.cause());
            }
        });

        startServer(startFuture);
    }

    private boolean started = false;
    /**
     * 启动web服务, 设置服务启动标志位
     * @param startFuture
     */
    @Override
    protected  void startServer(Future<Void> startFuture) {
        if (started) {
            return;
        }
        if (config().containsKey("http.host")) {
            vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8080), config().getString("http.host"));
        } else {
            vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8080));
        }
        startFuture.complete();
        started = true;
    }

    /**
     * eventbus
     */
    private void deployApi(Message<JsonObject> msg) {
        Api api = new Api(msg.body());

        deployHttpApi(api, res -> {
            if (res.succeeded()) {
                msg.reply(0);
            } else {
                msg.fail(1, res.cause().getMessage());
            }
        });
    }
    private void deployHttpApi(Api api, Handler<AsyncResult<Void>> result) {
        doDeployApi(app, api, router, apiMap, result);
    }
    private void doDeployApi(App app, Api api, Router router, ApiMap apiMap, Handler<AsyncResult<Void>> result) {
        api.setOn(true);

        AppServiceImpl.appService.updateApiState(app.getName(), api.getName(), api.isOn(), r -> {
            if (r.succeeded()) {

                vertx.executeBlocking(f -> {
                    List<Api> apis = app.getApis().stream().filter(item -> Objects.equals(item.getName(), api.getName())).collect(Collectors.toList());
                    if (apis.isEmpty()) {
                        app.getApis().add(api);
                    } else {
                        app.setApis(app.getApis().stream().map(item -> {
                            if (Objects.equals(item.getName(), api.getName())) {
                                return api;
                            }
                            return item;
                        }).collect(Collectors.toSet()));
                    }
                    List<Route> routeChain = apiMap.createRouteChain(api.getName(), api.getPath());
                    if (routeChain == null) {
                        f.fail("path route : " + api.getPath() + " is exist in app : " + app.getName());
                        return;
                    }
                    LifeCycle.defaultChain().start(this, api, router, routeChain);
                    f.complete();
                }, result);

            } else {
                result.handle(Future.failedFuture(r.cause()));
            }
        });
    }
    private void autoDeployAllApi(Handler<AsyncResult<CompositeFuture>> result) {
        List<Future> tasks = Lists.newArrayList();
        app.getApis().forEach(item -> {
            Future<Void> future = Future.future();
            tasks.add(future);
            deployHttpApi(item, future.completer());
        });
        CompositeFuture.all(tasks).setHandler(result);
    }
    /**
     * eventbus
     */
    private void undeployApi(Message<String> msg) {
        if (StringUtil.isNullOrEmpty(msg.body())) {
            msg.fail(1, "参数:API名字不能为空");
            return;
        }
        String apiName = msg.body();
        app.setApis(app.getApis().stream().filter(item -> !Objects.equals(item.getName(), apiName)).collect(Collectors.toSet()));
        AppServiceImpl.appService.updateApiState(app.getName(), apiName, false, r -> {
            if (r.succeeded()) {
                if (apiMap.deleteRouteChain(apiName)) {
                    LOGGER.debug("del api : " + apiName + " succeed");
                    msg.reply(0);
                } else {
                    LOGGER.debug("no api : " + apiName + " exist in app : " + app.getName());
                    msg.reply(1);
                }
            } else {
                msg.fail(1, r.cause().getMessage());
            }
        });
    }

    public Future<Void> undeployAllApi() {
        Future<Void> result = Future.future();
        AppServiceImpl.appService.updateAllApiState(app.getName(), false, r -> {
            if (r.succeeded()) {
                apiMap.destory();
                result.complete();
            } else {
                result.fail(r.cause());
            }
        });
        return result;
    }

    public void filterBlackIP(RoutingContext rct) {
        // 添加请求到达VX-API的数量
        vertx.eventBus().send(EventKey.System.SYSTEM_API_REQUEST, null);
//        String host = rct.request().remoteAddress().host();
//        if (blackIpSet.contains(host)) {
//            HttpServerResponse response = rct.response();
//            if (appOption.getBlacklistIpContentType() != null) {
//                response.putHeader(CONTENT_TYPE, appOption.getBlacklistIpContentType());
//            }
//            response.setStatusCode(appOption.getBlacklistIpCode());
//            if (appOption.getBlacklistIpResult() != null) {
//                response.setStatusMessage(appOption.getBlacklistIpResult());
//            } else {
//                response.setStatusMessage("you can't access this service");
//            }
//            response.end();
//        } else {
//            rct.next();
//        }
        rct.next();
    }

    public App getApp() {
        return app;
    }

    public ApiMap getApiMap() {
        return apiMap;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
