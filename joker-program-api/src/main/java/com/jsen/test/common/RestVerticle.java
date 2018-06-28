package com.jsen.test.common;

import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public abstract class RestVerticle extends VerticleBase {
    protected static final long SCAN_PERIOD = 20000L;

    protected Router router;

    protected String cH;
    protected int cP;

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     * @param startFuture
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        cH = config().getString("config.host", "localhost");
        cP = config().getInteger("config.port", 9000);
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(this::notFound);
        cros(router.route());
    }

    protected <T> Handler<AsyncResult<T>> resultHandlerData(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                T res = ar.result();
                if (res == null) {
                    notFound(context);
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(res.toString());
                }
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }
    protected  Handler<AsyncResult<Integer>> resultSimpleCode(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                Integer res = ar.result();
                if (res == null) {
                    notFound(context);
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(ResponseBase.create().code(res).encode());
                }
            } else {
                internalError(context, ar.cause());
                ar.cause().printStackTrace();
            }
        };
    }
    protected <T> void resultData(RoutingContext context, T data) {
        context.response()
                .putHeader("content-type", "application/json")
                .end(data.toString());
    }
    protected void resultJSON(RoutingContext context, JsonObject data) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(data.encode());
    }


    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("error", ex.getMessage()).encodePrettily());
    }

    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("message", "not_found").encodePrettily());
    }

    protected void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("error", ex.getMessage()).encodePrettily());
    }

    protected void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("message", "not_implemented").encodePrettily());
    }

    protected void discoverSelf(JsonObject config) {
        discoveryHttpEndpoint(config.getString("app.name", "app"),
                config.getString("http.host", "localhost"), config.getInteger("http.port", 8080),
                config.getString("endpoint", config.getString("app.name", "app")));
    }
    protected  void startServer(Future<Void> startFuture) {
        router.route("/*").handler(this::notFound);
        if (config().containsKey("http.host")) {
            vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8080), config().getString("http.host"));
        } else {
            vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8080));
        }
        startFuture.complete();
    }
    protected  void startServer(Future<Void> startFuture, Integer port) {
        router.route("/*").handler(this::notFound);
        if (config().containsKey("http.host")) {
            vertx.createHttpServer().requestHandler(router::accept).listen(port, config().getString("http.host", "localhost"));
        } else {
            vertx.createHttpServer().requestHandler(router::accept).listen(port);
        }
        startFuture.complete();
    }

    protected  void startServer(Future<Void> startFuture, Integer port, String host) {
        router.route("/*").handler(this::notFound);
        vertx.createHttpServer().requestHandler(router::accept).listen(port, host);
        startFuture.complete();
    }

    private void cros(Route route) {
        Set<String> allowHeaders = new HashSet<>();

        allowHeaders.add("authorization");
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        allowHeaders.add("X-PINGARUNER");

        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);
        allowMethods.add(HttpMethod.PUT);

        route.handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders).allowedMethods(allowMethods));
    }

}
