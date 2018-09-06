package com.jsen.joker.plugin.gateway.mirren;

import com.google.common.collect.Lists;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *     一个 route chain 代表一个 api
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class ApiMap extends HashMap<String, List<Route>> {
    public List<Route> createRouteChain(String name) {
        List<Route> routes = Lists.newArrayList();
        put(name, routes);
        return routes;
    }

    public void deleteRouteChain(String name) {
        List<Route> chain = get(name);
        if (chain != null) {
            chain.forEach(i -> i.disable().remove());
        }
    }

    public void destory() {
        forEach((key, v) -> v.forEach(i -> i.disable().remove()));
    }

    public Route genRoute(List<Route> routeChain, Router router) {
        Route route = router.route();
        routeChain.add(route);
        return route;
    }
}
