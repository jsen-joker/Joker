package com.jsen.joker.plugin.gateway.mirren.lifecycle;

import com.jsen.joker.plugin.gateway.mirren.ApplicationVerticle;
import com.jsen.joker.plugin.gateway.mirren.handler.HttpRouteHandler;
import com.jsen.joker.plugin.gateway.mirren.handler.RedirectRouteHandler;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
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
 * @since 2018/9/6
 */
public class HandleApi extends Pipline<ApplicationVerticle, Api, Router, List<Route>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleApi.class);


    @Override
    protected boolean handle(ApplicationVerticle applicationVerticle, Api api, Router router, List<Route> routeChain) {
        initServerHandler(applicationVerticle.getHttpClient(), applicationVerticle.getApp(), api, applicationVerticle.getApiMap().genRoute(routeChain, router));
        return true;
    }


    private void initServerHandler(HttpClient httpClient, App app, Api api, Route route) {
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


        if (api.getApiType() == Api.ApiType.HTTP) {
            Handler<RoutingContext> handler = HttpRouteHandler.create(api, httpClient);
            route.handler(handler);
            LOGGER.debug("add http api : " + api.getName() + " succeed");
        } else if (api.getApiType() == Api.ApiType.REDIRECT) {
            Handler<RoutingContext> handler = RedirectRouteHandler.create(api);
            route.handler(handler);
            LOGGER.debug("add redirect api : " + api.getName() + " succeed");
        } else {
            Handler<RoutingContext> handler = HttpRouteHandler.create(api, httpClient);
            route.handler(handler);
            LOGGER.debug("add http api : " + api.getName() + " succeed");
        }

    }
}
