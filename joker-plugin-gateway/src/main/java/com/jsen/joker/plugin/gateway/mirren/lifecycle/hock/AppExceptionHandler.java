package com.jsen.joker.plugin.gateway.mirren.lifecycle.hock;

import com.jsen.joker.plugin.gateway.GateWayStaticInfo;
import com.jsen.joker.plugin.gateway.mirren.ApplicationVerticle;
import com.jsen.joker.plugin.gateway.mirren.entity.MemTrack;
import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.handler.HttpRouteHandler;
import com.jsen.joker.plugin.gateway.mirren.handler.RedirectRouteHandler;
import com.jsen.joker.plugin.gateway.mirren.lifecycle.HandleApi;
import com.jsen.joker.plugin.gateway.mirren.lifecycle.Hock;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/9
 */
public class AppExceptionHandler implements Hock<ApplicationVerticle, Api, Router, List<Route>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppExceptionHandler.class);

    @Override
    public boolean handle(ApplicationVerticle applicationVerticle, Api api, Router router, List<Route> routes) {
        initServerHandler(applicationVerticle, applicationVerticle.getApp(), api, applicationVerticle.getApiMap().genRoute(routes, router));
        return true;
    }

    private void initServerHandler(ApplicationVerticle applicationVerticle, App app, Api api, Route route) {
        LOGGER.debug(api.getPath());
        route.path(api.getPath());
        /*
         * 设置支持的方法，空表示支持所有
         */
        api.getSupportMethods().forEach(item -> route.method(HttpMethod.valueOf(item)));
        /*
         * 设置支持的content type，空表示支持所有
         */
        api.getSupportContentType().forEach(route::consumes);

        route.failureHandler(rct -> {
            rct.response().putHeader("Server", GateWayStaticInfo.NAME)
                    .setStatusCode(rct.statusCode()).end(ResponseBase.create().code(1).toString());
            MemTrack memTrack = new MemTrack().appName(app.getName()).apiName(api.getName());
            if (rct.failure() != null) {
                memTrack.msg(rct.failure().getMessage());
                memTrack.trace(rct.failure().getStackTrace());
            } else {
                memTrack.msg("failure is null");
            }
            applicationVerticle.getVertx().eventBus().send(EventKey.System.SYSTEM_PLUS_ERROR, memTrack);
        });
    }
}
