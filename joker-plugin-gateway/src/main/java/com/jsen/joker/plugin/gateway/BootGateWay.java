package com.jsen.joker.plugin.gateway;

import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.config.ConfigRetrieverHelper;
import com.jsen.joker.plugin.login.service.UserService;
import com.jsen.joker.plugin.login.service.impl.UserServiceImpl;
import com.jsen.joker.plugin.login.utils.TokenUtils;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 *
 *
 * APi gate way url 模式：
 * /api 为 所有api代理转发根目录
 * /api/endpoint /endpoint为Record中的root属性，相当于verticle资源描述符
 * /api/endpoint/real-url /real-url 为实际请求路径
 */
public class BootGateWay extends RestVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BootGateWay.class);

    private CircuitBreaker circuitBreaker;

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     * @param startFuture a future which should be called when verticle start-up is complete.
     * @throws Exception
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        logger.error(config().encodePrettily());
        new ConfigRetrieverHelper() // TODO: enhance its usage
                .withHttpStore("localhost", 9000, "/config/s_gateway")
                .rxCreateConfig(io.vertx.reactivex.core.Vertx.newInstance(vertx)).doOnError(startFuture::fail).subscribe(config -> {
            logger.info(config.encodePrettily());

            JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
                    config().getJsonObject("circuit-breaker") : new JsonObject();
            circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
                    new CircuitBreakerOptions()
                            .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                            .setTimeout(cbOptions.getLong("timeout", 10000L))
                            .setFallbackOnFailure(true)
                            .setResetTimeout(cbOptions.getLong("reset-timeout", 30000L))
            );

            // api dispatcher
            router.route("/login/*").handler(this::dispatchLogin);
            router.route("/api/*").handler(this::dispatchRequests);
            startServer(startFuture);
        });


        // vertx.createHttpServer().requestHandler(router::accept).listen(8088);
    }


    /**
     * 获取所有注册的rest api
     * @return
     */
    private Future<List<Record>> getAllEndpoints() {
        Future<List<Record>> future = Future.future();
        serviceDiscovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE),
                future.completer());
        return future;
    }

    private void  dispatchLogin(RoutingContext context) {
        int initialOffset = 7; // length of `/api/`
        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> {
            getAllEndpoints().setHandler(ar -> {
                if (ar.succeeded()) {
                    List<Record> recordList = ar.result();
                    // get relative path and retrieve prefix to dispatch client
                    String path = context.request().uri();

                    if (path.length() <= initialOffset) {
                        notFound(context);
                        future.complete();
                        return;
                    }
                    String prefix = (path.substring(initialOffset)
                            .split("/"))[0]; // /boot

                    // generate new relative path
                    String newPath = path.substring(initialOffset + prefix.length());

                    // get one relevant HTTP client, may not exist
                    Optional<Record> client = recordList.stream().filter(record -> prefix.equals(record.getMetadata().getString("endpoint")))
                            .findAny(); // simple load balance

                    if (client.isPresent()) {
                        if (newPath.startsWith("/login")) {
                            doDispatch(context, newPath, serviceDiscovery.getReference(client.get()).get(), future);
                        } else {
                            notFound(context);
                        }
                    } else {
                        notFound(context);
                        future.complete();
                    }
                } else {
                    future.fail(ar.cause());
                }
            });
        }).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });

    }


    private void dispatchRequests(RoutingContext context) {
        int initialOffset = 5; // length of `/api/`
        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> {
            String token = context.request().getHeader("Authorization");
            if (token == null) {
                noAuth(context);
                future.complete();
            } else {

                UserService userService = UserService.createProxy(vertx);

                int id;
                try {
                    id = TokenUtils.getUserId(token);
                } catch (Exception e) {
                    noAuth(context);
                    future.complete();
                    return;
                }
                userService.getUserByID(id, r -> {
                    if(r.succeeded()) {
                        JsonObject result = r.result();
                        try {
                            TokenUtils.validToken(token, result.getString("password"), UserServiceImpl.shortExp);

                            getAllEndpoints().setHandler(ar -> {
                                if (ar.succeeded()) {
                                    List<Record> recordList = ar.result();
                                    // get relative path and retrieve prefix to dispatch client
                                    String path = context.request().uri();

                                    if (path.length() <= initialOffset) {
                                        notFound(context);
                                        future.complete();
                                        return;
                                    }
                                    String prefix = (path.substring(initialOffset)
                                            .split("/"))[0]; // /boot
                                    // generate new relative path
                                    String newPath = path.substring(initialOffset + prefix.length());
                                    // get one relevant HTTP client, may not exist
                                    Optional<Record> client = recordList.stream().filter(record -> prefix.equals(record.getMetadata().getString("endpoint")))
                                            .findAny(); // simple load balance

                                    if (client.isPresent()) {
                                        doDispatch(context, newPath, serviceDiscovery.getReference(client.get()).get(), future);
                                    } else {
                                        notFound(context);
                                        future.complete();
                                    }
                                } else {
                                    future.fail(ar.cause());
                                }
                            });

                        } catch (Exception e) {
                            noAuth(context);
                            future.complete();
                        }
                    } else {
                        noAuth(context);
                        future.complete();
                    }
                });

            }

        }).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });
    }

    /**
     * Stop the verticle.<p>
     * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
     * If your verticle does things in its shut-down which take some time then you can override this method
     * and call the stopFuture some time later when clean-up is complete.
     *
     * @param stopFuture a future which should be called when verticle clean-up is complete.
     * @throws Exception
     */
    @Override
    public void stop(Future<Void> stopFuture) {

        circuitBreaker.close();
        stopFuture.complete();
    }

    private void doDispatch(RoutingContext context, String path, HttpClient client, Future<Object> cbFuture) {

        logger.error(path);
        HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response()
                                    .setStatusCode(response.statusCode());
                            response.headers().forEach(header -> {
                                toRsp.putHeader(header.getKey(), header.getValue());
                            });
                            // send response
                            toRsp.end(body);
                            cbFuture.complete();
                        }
                        ServiceDiscovery.releaseServiceObject(serviceDiscovery, client);
                    });
                });
        // set headers
        context.request().headers().forEach(header -> {
            toReq.putHeader(header.getKey(), header.getValue());
        });
        if (context.user() != null) {
            toReq.putHeader("user-principal", context.user().principal().encode());
        }
        // send request
        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }
    }
    @Override
    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void noAuth(RoutingContext context) {
        context.response().setStatusCode(401)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("msg", "未登入用户").encodePrettily());
    }

    @Override
    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }
    @Override
    protected void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }
    @Override
    protected void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_implemented").encodePrettily());
    }

    protected void badGateway(Throwable ex, RoutingContext context) {
        ex.printStackTrace();
        context.response()
                .setStatusCode(502)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "bad_gateway")
                        //.put("message", ex.getMessage())
                        .encodePrettily());
    }


    public static void main(String[] args) {

        VertxOptions vO = new VertxOptions();
        vO.setEventLoopPoolSize(16);
        Vertx vertx = Vertx.vertx(vO);
        DeploymentOptions dO = new DeploymentOptions();
        dO.setInstances(1);

        RxHelper.deployVerticle(vertx, new BootGateWay(), dO).subscribe(System.out::println,
                System.err::println);
    }
}
