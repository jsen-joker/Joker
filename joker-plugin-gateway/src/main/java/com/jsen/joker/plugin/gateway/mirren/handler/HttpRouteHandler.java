package com.jsen.joker.plugin.gateway.mirren.handler;

import com.jsen.joker.plugin.gateway.GateWayStaticInfo;
import com.jsen.joker.plugin.gateway.HttpConstants;
import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.ApiOptionUrl;
import com.jsen.joker.plugin.gateway.mirren.utils.Balancer;
import com.jsen.joker.plugin.gateway.mirren.utils.SimpleApiUrlBalancer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
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
public class HttpRouteHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRouteHandler.class);

    private HttpClient httpClient;
    private Api api;


    /**
     * tools
     */
    private Balancer<ApiOptionUrl> balancer;

    /**
     * state
     */
    private boolean isFine = true;
    private int prefixLen = 0;

    /**
     *
     * @param api
     * @param httpClient
     */
    private HttpRouteHandler(Api api, HttpClient httpClient) {
        super();
        this.httpClient = httpClient;
        this.api = api;
        prefixLen = api.getPath().length();
        balancer = new SimpleApiUrlBalancer(api.getApiOption().getApiOptionUrls());
    }
    /**
     * Something has happened, so handle it.
     *
     * @param event the event to handle
     */
    @Override
    public void handle(RoutingContext event) {
        event.vertx().eventBus().send(EventKey.System.SYSTEM_HTTP_REQUEST_SR, null);


        HttpServerRequest sourceRequest = event.request();

        String suffix = sourceRequest.path();
        suffix = suffix.substring(prefixLen);
        suffix = suffix + "?" + sourceRequest.query();


        ApiOptionUrl apiOptionUrl = balancer.balance();

        LOGGER.debug("request path : " + apiOptionUrl.getUrl());

        HttpClientRequest clientRequest = httpClient.requestAbs(sourceRequest.method(), apiOptionUrl.getUrl() + suffix);
        _dumpHeader(sourceRequest, clientRequest);
        _handleHCException(event, clientRequest);
        _handleHCResponse(event, clientRequest);
        _handleEnd(event);


        // send request
        if (event.getBody() == null) {
            clientRequest.end();
        } else {
            clientRequest.end(event.getBody());
        }
    }

    private void _dumpHeader(HttpServerRequest serverRequest, HttpClientRequest clientRequest) {
        clientRequest.putHeader(HttpConstants.Header.USER_AGENT, GateWayStaticInfo.USER_AGENT);

        serverRequest.headers().forEach(header -> clientRequest.putHeader(header.getKey(), header.getValue()));

    }
    private void _handleHCException(RoutingContext event, HttpClientRequest clientRequest) {
        clientRequest.exceptionHandler(e -> notFound(event));
    }
    private void _handleHCResponse(RoutingContext event, HttpClientRequest clientRequest) {
        clientRequest.handler(response -> response.bodyHandler(body -> {
            HttpServerResponse toRsp = event.response().setStatusCode(response.statusCode());
            response.headers().forEach(header -> toRsp.putHeader(header.getKey(), header.getValue()));
            // send response
            toRsp.end(body);
        }));
    }
    private void _handleEnd(RoutingContext event) {
        event.response().endHandler(end -> {
            // 减少当前正在处理的数量
            event.vertx().eventBus().send(EventKey.System.SYSTEM_HTTP_REQUEST_SS, null);
        });
    }


    private void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }


    public static Handler<RoutingContext> create(Api api, HttpClient httpClient) {
        HttpRouteHandler httpRouteHandler = new HttpRouteHandler(api, httpClient);
        if (httpRouteHandler.isFine) {
            return httpRouteHandler;
        }
        return null;
    }
}
