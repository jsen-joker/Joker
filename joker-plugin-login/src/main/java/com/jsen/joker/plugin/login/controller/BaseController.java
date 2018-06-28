package com.jsen.joker.plugin.login.controller;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/31
 */
public class BaseController {

    protected Router router;

    public BaseController(Router router) {
        this.router = router;
    }

    protected <T> Handler<AsyncResult<T>> resultHandlerData(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                if (res != null) {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(res.toString());
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("code", 1).put("msg", "获取信息失败").toString());
                }
            } else {
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("code", 1).put("msg", "内部错误").put("error", ar.cause().getMessage()).toString());
                ar.cause().printStackTrace();
            }
        };
    }

    protected <T> void resultData(RoutingContext context, T data) {
        context.response()
                .putHeader("content-type", "application/json")
                .end(data.toString());
    }

}
