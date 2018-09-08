package com.jsen.joker.plugin.gateway.mirren.handler;

import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
import com.jsen.joker.plugin.gateway.mirren.service.AppService;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * <p>
 *     gateway 的动态设置的 http api
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class GatewayHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayHandler.class);


    private Router router;
    private EventBus eventBus;
    private AppService appService;

    public GatewayHandler(String prefix, Router router, EventBus eventBus, AppService appService) {
        this.router = router;
        this.eventBus = eventBus;
        this.appService = appService;

        router.post(prefix + "app/add").handler(this::addApp);
        router.delete(prefix + "app/del/:id").handler(this::delApp);
        router.put(prefix + "app/update/:id").handler(this::updateApp);
        router.get(prefix + "app/list").handler(this::listApp);
        router.get(prefix + "app/one/:id").handler(this::getOneApp);

        router.post(prefix + "api/add/:id").handler(this::addApi);
        router.delete(prefix + "api/del/:id/:name").handler(this::delApi);
        router.put(prefix + "api/update/:id").handler(this::updateApi);

        router.post(prefix + "deploy/app").handler(this::deployApp);
        router.delete(prefix + "undeploy/app/:name").handler(this::unDeployApp);

        router.post(prefix + "deploy/api/:appName").handler(this::deployApi);
        router.delete(prefix + "undeploy/api/:appName/:apiName").handler(this::unDeployApi);


        router.post(prefix + "mock/login").handler(this::mockLogin);
        router.post(prefix + "mock/logout").handler(this::mockLogout);
        router.get(prefix + "mock/user/info").handler(this::mockUserInfo);
    }

    private void addApp(RoutingContext routingContext) {
        appService.createApp(new App(routingContext.getBodyAsJson()), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void delApp(RoutingContext routingContext) {
        appService.deleteApp(routingContext.request().getParam("id"), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void updateApp(RoutingContext routingContext) {
        JsonObject obj = routingContext.getBodyAsJson();
        appService.updateApp(routingContext.request().getParam("id"), new App(obj), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void listApp(RoutingContext routingContext) {
        appService.listApp(r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void getOneApp(RoutingContext routingContext) {
        appService.getOneApp(routingContext.request().getParam("id"), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void addApi(RoutingContext routingContext) {
        appService.createApi(routingContext.request().getParam("id"), new Api(routingContext.getBodyAsJson()), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void delApi(RoutingContext routingContext) {
        appService.deleteApi(routingContext.request().getParam("id"), routingContext.request().getParam("name"), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void updateApi(RoutingContext routingContext) {
        appService.updateApi(routingContext.request().getParam("id"), new Api(routingContext.getBodyAsJson()), r -> {
            if (r.succeeded()) {
                resultJSON(routingContext, r.result());
            } else {
                resultJSON(routingContext, ResponseBase.create().code(1).msg(r.cause().getMessage()));
            }
        });
    }

    private void deployApp(RoutingContext routingContext) {
        doSimple(EventKey.App.DEPLOY, routingContext);
    }
    private void unDeployApp(RoutingContext routingContext) {
        String name = routingContext.request().getParam("name");
        doSimpleString(EventKey.App.UNDEPLOY, name, routingContext);
    }

    private void deployApi(RoutingContext routingContext) {
        String appName = routingContext.request().getParam("appName");
        doSimple(appName + ":" + EventKey.App.Api.ADD, routingContext);
    }
    private void unDeployApi(RoutingContext routingContext) {
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
    private void mockLogin(RoutingContext routingContext) {
        resultJSON(routingContext, ResponseBase.create().code(0).data(new JsonObject().put("token", "admin")));
    }
    private void mockLogout(RoutingContext routingContext) {
        resultJSON(routingContext, ResponseBase.create().code(0));
    }
    //{"code":20000,"data":{"roles":["admin"],"name":"admin","avatar":
    // "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif"}}
    private void mockUserInfo(RoutingContext routingContext) {
        resultJSON(routingContext, ResponseBase.create().code(0).data(new JsonObject().put("roles", new JsonArray().add("admin"))
                .put("name", "admin").put("avatar", "http://pic.ik123.com/uploads/allimg/170109/3-1F10Z91041.gif")));
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
