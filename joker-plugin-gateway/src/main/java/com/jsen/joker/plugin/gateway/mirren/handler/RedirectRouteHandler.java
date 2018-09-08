package com.jsen.joker.plugin.gateway.mirren.handler;

import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.ApiOptionUrl;
import com.jsen.joker.plugin.gateway.mirren.utils.Balancer;
import com.jsen.joker.plugin.gateway.mirren.utils.SimpleApiUrlBalancer;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class RedirectRouteHandler implements Handler<RoutingContext> {

    /**
     * tools
     */
    private Balancer<ApiOptionUrl> balancer;

    public RedirectRouteHandler(Api api) {
        super();
        balancer = new SimpleApiUrlBalancer(api.getApiOption().getApiOptionUrls());
    }

    @Override
    public void handle(RoutingContext rct) {
        rct.response().putHeader("Location", balancer.balance().getUrl()).setStatusCode(302);
        rct.response().end();
    }


    public static Handler<RoutingContext> create(Api api) {
        RedirectRouteHandler redirectRouteHandler = new RedirectRouteHandler(api);
        return redirectRouteHandler;
    }
}
