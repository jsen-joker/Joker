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
public class ApiMap extends HashMap<String, ApiMap.RouteAdapter> {
    private List<String> pathList = Lists.newArrayList();
    public List<Route> createRouteChain(String name, String path) {
        if (pathList.contains(path)) {
            return null;
        }
        RouteAdapter routes = get(name);
        if (routes != null) {
            deleteRouteChain(name);
        }
        routes = new RouteAdapter(Lists.newArrayList(), path);
        put(name, routes, path);
        return routes.routes;
    }

    public boolean deleteRouteChain(String name) {
        RouteAdapter chain = get(name);
        if (chain != null) {
            pathList.remove(chain.path);
            chain.routes.forEach(i -> i.disable().remove());
            remove(name);
            return true;
        }
        return false;
    }

    public void destory() {
        synchronized (this) {
            forEach((key, v) -> v.routes.forEach(i -> i.disable().remove()));
            pathList.clear();
            clear();
        }
    }

    public Route genRoute(List<Route> routeChain, Router router) {
        Route route = router.route();
        routeChain.add(route);
        return route;
    }

    static class RouteAdapter {
        List<Route> routes;
        String path;

        public RouteAdapter(List<Route> routes, String path) {
            this.routes = routes;
            this.path = path;
        }
    }

    private RouteAdapter put(String key, RouteAdapter value, String path) {
        pathList.add(path);
        return super.put(key, value);
    }
}
