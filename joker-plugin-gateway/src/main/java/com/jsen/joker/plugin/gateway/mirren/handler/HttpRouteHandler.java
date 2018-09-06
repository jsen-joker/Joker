package com.jsen.joker.plugin.gateway.mirren.handler;

import com.jsen.joker.plugin.gateway.GateWayStaticInfo;
import com.jsen.joker.plugin.gateway.HttpConstants;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.ApiUrl;
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
    private Balancer<ApiUrl> balancer;

    /**
     * state
     */
    private boolean isFine = true;
    private int prefixLen = 0;

    /**
     *
     * @param gateWayPath gateway path /gateway
     * @param api
     * @param httpClient
     */
    private HttpRouteHandler(String gateWayPath, Api api, HttpClient httpClient) {
        super();
        this.httpClient = httpClient;
        this.api = api;
        prefixLen = gateWayPath.length() + api.getPath().length();
        balancer = new SimpleApiUrlBalancer(api.getApiOptions().getApiUrls());
    }
    /**
     * Something has happened, so handle it.
     *
     * @param event the event to handle
     */
    @Override
    public void handle(RoutingContext event) {
        HttpServerRequest sourceRequest = event.request();

        String suffix = sourceRequest.path();
        suffix = suffix.substring(prefixLen);
        suffix = suffix + "?" + sourceRequest.query();


        ApiUrl apiUrl = balancer.balance();

        LOGGER.debug("request path : " + apiUrl.getUrl());

        HttpClientRequest clientRequest = httpClient.requestAbs(sourceRequest.method(), apiUrl.getUrl() + suffix);
        _dumpHeader(sourceRequest, clientRequest);
        _handleHCException(event, clientRequest);
        _handleHCResponse(event, clientRequest);


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


    private void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }


    public static Handler<RoutingContext> create(String gateWayPath, Api api, HttpClient httpClient) {
        HttpRouteHandler httpRouteHandler = new HttpRouteHandler(gateWayPath, api, httpClient);
        if (httpRouteHandler.isFine) {
            return httpRouteHandler;
        }
        return null;
    }
}
