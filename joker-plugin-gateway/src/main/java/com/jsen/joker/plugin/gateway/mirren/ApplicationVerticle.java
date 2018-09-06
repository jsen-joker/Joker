package com.jsen.joker.plugin.gateway.mirren;

import com.hazelcast.util.StringUtil;
import com.jsen.joker.plugin.gateway.mirren.handler.HttpRouteHandler;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.GateWay;
import com.jsen.test.common.RestVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

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
        this.httpClient = vertx.createHttpClient();

        gateWay = GateWay.fromJson(config().getJsonObject("app"));

        config().put("app.name", gateWay.getName());
        config().put("http.host", gateWay.getHost());
        config().put("http.port", gateWay.getPort());
        startServer(startFuture);
    }


    /**
     * eventbus
     */
    public void appApi(Message<JsonObject> msg) {
        Api api = Api.fromJson(msg.body());

        addHttpApi(api, res -> {
            if (res.succeeded()) {
                msg.reply(0);
            } else {
                msg.fail(1, res.cause().getMessage());
            }
        });
    }
    private void addHttpApi(Api api, Handler<AsyncResult<Boolean>> result) {}
    private void doAddApi(GateWay gateWay, Api api, Router router, GateWayMap gateWayMap, Handler<AsyncResult<Void>> result) {
        ApiMap apiMap = gateWayMap.getGateway(gateWay.getName());
        List<Route> routeChain = apiMap.createRouteChain(api.getName());
        initServerHandler(gateWay, api, apiMap.genRoute(routeChain, router));
    }
    private void initServerHandler(GateWay gateWay, Api api, Route route) {
        route.path(api.getPath());
        /*
         * 设置支持的方法，空表示支持所有
         */
        api.getSupportMethods().forEach(item -> route.method(HttpMethod.valueOf(item)));
        /*
         * 设置支持的content type，空表示支持所有
         */
        api.getSupportContentType().forEach(route::consumes);


        Handler<RoutingContext> handler = HttpRouteHandler.create(gateWay.getPath(), api, httpClient);

        route.handler(handler);

    }
    /**
     * eventbus
     */
    public void delApi(Message<String> msg) {
        if (StringUtil.isNullOrEmpty(msg.body())) {
            msg.fail(1, "参数:API名字不能为空");
            return;
        }
        String apiName = msg.body();
        apiMap.deleteRouteChain(apiName);
        msg.reply(0);
    }

}
