package com.jsen.joker.plugin.gateway.mirren.handler;

import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class GatewayHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayHandler.class);


    private Router router;
    private EventBus eventBus;

    public GatewayHandler(String prefix, Router router, EventBus eventBus) {
        this.router = router;
        this.eventBus = eventBus;

        router.post(prefix + "deploy/app").handler(this::deployApp);
        router.delete(prefix + "undeploy/app/:name").handler(this::unDeployApp);

        router.post(prefix + "add/api/:appName").handler(this::addApi);
        router.delete(prefix + "del/api/:appName/:apiName").handler(this::delApi);
    }

    private void deployApp(RoutingContext routingContext) {
        doSimple(EventKey.App.DEPLOY, routingContext);
    }
    private void unDeployApp(RoutingContext routingContext) {
        String name = routingContext.request().getParam("name");
        doSimpleString(EventKey.App.UNDEPLOY, name, routingContext);
    }

    private void addApi(RoutingContext routingContext) {
        String appName = routingContext.request().getParam("appName");
        doSimple(appName + ":" + EventKey.App.Api.ADD, routingContext);
    }
    private void delApi(RoutingContext routingContext) {
        String appName = routingContext.request().getParam("appName");
        String apiName = routingContext.request().getParam("apiName");
        doSimpleString(appName + ":" + EventKey.App.Api.DEL, apiName, routingContext);
    }


    private void doSimple(String key, RoutingContext routingContext) {
        eventBus.send(key, routingContext.getBodyAsJson(), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, ResponseBase.create().code(0));
            } else {
                LOGGER.error(r.cause().getMessage());
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }
    private void doSimpleString(String key, String msg, RoutingContext routingContext) {
        eventBus.send(key, msg, r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, ResponseBase.create().code(0));
            } else {
                LOGGER.error(r.cause().getMessage());
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }
    /**
     * 通用http数据返回 返回Object.toString()数据格式
     * @param context
     */
    protected <T> void resultData(RoutingContext context, T data) {
        context.response()
                .putHeader("content-type", "application/json")
                .end(data.toString());
    }

    /**
     * 通用http数据返回 json数据返回
     * @param context
     */
    protected void resultJSON(RoutingContext context, JsonObject data) {
        context.response().setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(data.encode());
    }


    /**
     * http错误处理，400
     * @param context
     */
    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("error", ex.getMessage()).encodePrettily());
    }

    /**
     * http错误处理，404
     * @param context
     */
    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("message", "not_found").encodePrettily());
    }

    /**
     * http错误处理，500
     * @param context
     */
    protected void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("error", ex.getMessage()).encodePrettily());
    }

    /**
     * http错误处理，501
     * @param context
     */
    protected void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("message", "not_implemented").encodePrettily());
    }

}
