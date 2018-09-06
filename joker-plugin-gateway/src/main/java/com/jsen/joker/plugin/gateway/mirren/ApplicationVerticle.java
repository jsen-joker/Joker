package com.jsen.joker.plugin.gateway.mirren;

import com.hazelcast.util.StringUtil;
import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.lifecycle.*;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.GateWay;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    private GateWay gateWay;

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

        gateWay = GateWay.fromJson(config().getJsonObject("app"));

        router.get("/ok").handler(r -> resultJSON(r, ResponseBase.create().code(0)));

        vertx.eventBus().consumer(gateWay.getName() + ":" + EventKey.App.Api.ADD, this::addApi);
        vertx.eventBus().consumer(gateWay.getName() + ":" + EventKey.App.Api.DEL, this::delApi);

        config().put("app.name", gateWay.getName());
        config().put("http.host", gateWay.getHost());
        config().put("http.port", gateWay.getPort());
        startServer(startFuture);
        DeployVerticle.getInstance().registerApp(this);
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
    private void addApi(Message<JsonObject> msg) {
        Api api = Api.fromJson(msg.body());

        addHttpApi(api, res -> {
            if (res.succeeded()) {
                msg.reply(0);
            } else {
                msg.fail(1, res.cause().getMessage());
            }
        });
    }
    private void addHttpApi(Api api, Handler<AsyncResult<Void>> result) {
        doAddApi(gateWay, api, router, apiMap, result);
    }
    private void doAddApi(GateWay gateWay, Api api, Router router, ApiMap apiMap, Handler<AsyncResult<Void>> result) {
        vertx.executeBlocking(f -> {
            List<Route> routeChain = apiMap.createRouteChain(api.getName(), api.getPath());
            if (routeChain == null) {
                f.fail("path route : " + api.getPath() + " is exist in app : " + gateWay.getName());
                return;
            }
            /*
            li
             */
            LifeCycle.defaultChain().start(this, api, router, routeChain);
            f.complete();
        }, result);
    }
    /**
     * eventbus
     */
    private void delApi(Message<String> msg) {
        if (StringUtil.isNullOrEmpty(msg.body())) {
            msg.fail(1, "参数:API名字不能为空");
            return;
        }
        String apiName = msg.body();
        if (apiMap.deleteRouteChain(apiName)) {
            LOGGER.debug("del api : " + apiName + " succeed");
            msg.reply(0);
        } else {
            LOGGER.debug("no api : " + apiName + " exist in app : " + gateWay.getName());
            msg.fail(1, "no api : " + apiName + " exist in app : " + gateWay.getName());
        }
    }

    public GateWay getGateWay() {
        return gateWay;
    }

    public ApiMap getApiMap() {
        return apiMap;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
