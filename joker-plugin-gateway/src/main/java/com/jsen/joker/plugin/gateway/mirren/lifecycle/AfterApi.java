package com.jsen.joker.plugin.gateway.mirren.lifecycle;

import com.jsen.joker.plugin.gateway.mirren.ApplicationVerticle;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class AfterApi extends Pipline<ApplicationVerticle, Api, Router, List<Route>> {
    @Override
    protected boolean handle(ApplicationVerticle applicationVerticle, Api api, Router router, List<Route> routeChain) {
        return false;
    }
}
