package com.jsen.joker.plugin.gateway.mirren.lifecycle;

import com.jsen.joker.plugin.gateway.mirren.ApplicationVerticle;
import com.jsen.joker.plugin.gateway.mirren.lifecycle.hock.AppExceptionHandler;
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
public class LifeCycle {

    private static BeforeApi beforeApi = new BeforeApi();
    private static HandleApi handleApi = new HandleApi();
    private static AfterApi afterApi = new AfterApi();

    private static Pipline<ApplicationVerticle, Api, Router, List<Route>> _defaultChain = beforeApi;
    static {
        _defaultChain.setChild(handleApi).setChild(afterApi);

        addAfterHockBefore(new AppExceptionHandler());
    }
    public static Pipline<ApplicationVerticle, Api, Router, List<Route>> defaultChain() {
        return _defaultChain;
    }

    public static void addBeforeHockBefore(Hock<ApplicationVerticle, Api, Router, List<Route>> hock) {
        beforeApi.registerBeforeHock(hock);
    }
    public static void addBeforeHockAfter(Hock<ApplicationVerticle, Api, Router, List<Route>> hock) {
        beforeApi.registerAfterHock(hock);
    }

    public static void addHandleHockBefore(Hock<ApplicationVerticle, Api, Router, List<Route>> hock) {
        handleApi.registerBeforeHock(hock);
    }
    public static void addHandleHockAfter(Hock<ApplicationVerticle, Api, Router, List<Route>> hock) {
        handleApi.registerAfterHock(hock);
    }

    public static void addAfterHockBefore(Hock<ApplicationVerticle, Api, Router, List<Route>> hock) {
        afterApi.registerBeforeHock(hock);
    }
    public static void addAfterHockAfter(Hock<ApplicationVerticle, Api, Router, List<Route>> hock) {
        afterApi.registerAfterHock(hock);
    }

}
