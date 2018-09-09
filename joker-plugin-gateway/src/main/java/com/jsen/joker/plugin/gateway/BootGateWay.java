package com.jsen.joker.plugin.gateway;

import com.jsen.joker.annotation.annotation.Entry;
import com.jsen.joker.boot.cloader.context.EntryContext;
import com.jsen.joker.plugin.gateway.mirren.DeployVerticle;
import com.jsen.joker.plugin.gateway.mirren.handler.GatewayHandler;
import com.jsen.joker.plugin.gateway.mirren.handler.SystemHandler;
import com.jsen.joker.plugin.gateway.mirren.service.AppService;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.config.ConfigRetrieverHelper;
import com.jsen.joker.plugin.login.service.UserService;
import com.jsen.joker.plugin.login.service.impl.UserServiceImpl;
import com.jsen.joker.plugin.login.utils.TokenUtils;
import com.jsen.test.common.joker.JokerStaticHandlerImpl;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.*;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 *     实现内部网关核心，
 *     实现静态网页访问、及http核心访问api
 *     实现系统参数访问api
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
@Entry
public class BootGateWay extends RestVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BootGateWay.class);

    private CircuitBreaker circuitBreaker;

    // 权限限制访问
    private static final String SEC_PREFIX = "api";
    private static final String PB_PREFIX = "pb";
    private static final String LOGIN_PREFIX = "login";
    private static final String GATEWAY_PREFIX = "gateway";
    private static final String SYSTEM_PREFIX = "system";
    private static final String SP = "/";
    private UserService userService;

    private AppService appService;

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

        new ConfigRetrieverHelper() // TODO: enhance its usage
                .withHttpStore(config().getString("config.host", "localhost"), config().getInteger("config.port", 9000), "/config/s_gateway")
                .rxCreateConfig(io.vertx.reactivex.core.Vertx.newInstance(vertx)).doOnError(startFuture::fail).subscribe(config -> {


            JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
                    config().getJsonObject("circuit-breaker") : new JsonObject();
            circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
                    new CircuitBreakerOptions()
                            .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                            .setTimeout(cbOptions.getLong("timeout", 10000L))
                            .setFallbackOnFailure(true)
                            .setResetTimeout(cbOptions.getLong("reset-timeout", 30000L))
            );
            userService = UserService.createProxy(vertx);
            appService = AppService.create(vertx, config());


            logger.debug(Thread.currentThread().getContextClassLoader().getClass().toString());



            String favicon = new File(JokerStaticHandlerImpl.getJokerRoot(this.getClass()), "favicon.ico").getAbsolutePath();
            logger.debug(favicon);
            router.route().handler(FaviconHandler.create(favicon));
//            router.route().handler(BodyHandler.create().setUploadsDirectory(getUploadsDirectory()));
            router.route().handler(CookieHandler.create());


            // api dispatcher
            router.route("/" + LOGIN_PREFIX + "/*").handler(this::dispatchLogin);
            router.route("/" + SEC_PREFIX + "/*").handler(this::dispatchApi);
            router.route("/" + PB_PREFIX + "/*").handler(this::dispatchPb);

            new GatewayHandler(SP + GATEWAY_PREFIX + SP, router, vertx.eventBus(), appService);
            new SystemHandler(SP + SYSTEM_PREFIX + SP, router, vertx.eventBus(), appService);

            logger.debug("2");
            StaticHandler staticHandler = new JokerStaticHandlerImpl(this.getClass());
            router.route("/*").handler(staticHandler);

            List<Future> futures = new ArrayList<>();
            // 启动系统服务Verticle
            futures.add(Future.<String>future(sysInfo -> {
                vertx.deployVerticle(DeployVerticle.class.getName(), new DeploymentOptions(), sysInfo);
//                vertx.deployVerticle(EntryContext.getDefaultEnterContext().getVerticleClazz(DeployVerticle.class.getName()), new DeploymentOptions(), sysInfo);
            }));

            CompositeFuture.all(futures).setHandler(res -> {
                if (res.succeeded()) {
                    logger.info("gateway start succeed");
                    startServer(startFuture);
                } else {
                    res.cause().printStackTrace();
                    logger.error(res.cause().getMessage());
                    logger.error("gateway start failed");
                    startFuture.fail(res.cause());
                }
            });
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

    /**
     * /login/endpoint/login/parameters
     * @param context
     */
    private void  dispatchLogin(RoutingContext context) {
        // length of `/login/`
        int initialOffset = LOGIN_PREFIX.length() + 2;

        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> getAllEndpoints().setHandler(ar -> {
            if (ar.succeeded()) {
                List<Record> recordList = ar.result();
                // get relative path and retrieve prefix to dispatch client
                String path = context.request().uri();

                if (path.length() <= initialOffset) {
                    notFound(context);
                    future.complete();
                    return;
                }

                // /boot
                String prefix = (path.substring(initialOffset).split(SP))[0];

                // generate new relative path
                String newPath = path.substring(initialOffset + prefix.length());

                // get one relevant HTTP client, may not exist
                Optional<Record> client = recordList.stream().filter(record -> prefix.equals(record.getMetadata().getString("endpoint")))
                        .findAny(); // simple load balance

                if (client.isPresent()) {
                    if (newPath.startsWith(SP + LOGIN_PREFIX)) {
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
        })).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });

    }


    private void dispatchApi(RoutingContext context) {
        // length of `/api/`
        int initialOffset = SEC_PREFIX.length() + 2;
        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> {
            String token = context.request().getHeader("Authorization");
            if (token == null) {
                token = context.request().getParam("Authorization");
                if (token == null) {
                    logger.info("noauth1");
                    noAuth(context);
                    future.complete();
                    return;
                }
            }

            int id;
            try {
                id = TokenUtils.getUserId(token);
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("noauth2");
                noAuth(context);
                future.complete();
                return;
            }
            String finalToken = token;
            userService.getUserByID(id, r -> {
                if(r.succeeded()) {
                    JsonObject result = r.result();
                    try {
                        TokenUtils.validToken(finalToken, result.getString("password"), UserServiceImpl.shortExp);

                        getAllEndpoints().setHandler(ar -> {
                            dispatch(context, initialOffset, future, ar, true);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.info("noauth3");
                        noAuth(context);
                        future.complete();
                    }
                } else {
                    r.cause().printStackTrace();
                    logger.info(r.cause().getMessage());
                    logger.info("noauth4");
                    noAuth(context);
                    future.complete();
                }
            });


        }).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });
    }

    /**
     *
     * @param context
     */
    private void  dispatchPb(RoutingContext context) {
        // length of `/pb/`
        int initialOffset = PB_PREFIX.length() + 2;

        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> getAllEndpoints().setHandler(ar -> {
            dispatch(context, initialOffset, future, ar, false);
        })).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });

    }

    private void dispatch(RoutingContext context, int initialOffset, Future<Object> future, AsyncResult<List<Record>> ar, boolean checkSecurity) {
        if (ar.succeeded()) {
            List<Record> recordList = ar.result();
            // get relative path and retrieve prefix to dispatch client
            String path = context.request().uri();

            if (path.length() <= initialOffset) {
                notFound(context);
                future.complete();
                return;
            }

            // /boot
            String prefix = (path.substring(initialOffset).split(SP))[0];

            // generate new relative path
            String newPath = path.substring(initialOffset + prefix.length());
            if (!checkSecurity && newPath.startsWith(SP + SEC_PREFIX)) {
                noAuth(context);
                future.complete();
                return;
            }

            // get one relevant HTTP client, may not exist
            Optional<Record> client = recordList.stream().filter(record -> prefix.equals(record.getMetadata().getString("endpoint")))
                    .findAny(); // simple load balance

            if (client.isPresent()) {
                if (checkSecurity) {
                    newPath = SP + SEC_PREFIX + newPath;
                }
                doDispatch(context, newPath, serviceDiscovery.getReference(client.get()).get(), future);
            } else {
                notFound(context);
                future.complete();
            }
        } else {
            future.fail(ar.cause());
        }
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
        Future<Void> task1 = Future.future();
        circuitBreaker.close();
        appService.close(task1.completer());
        task1.setHandler(r -> {
            if (r.succeeded()) {
                super.stop(stopFuture);
            } else {
                stopFuture.fail(r.cause());
            }
        });
    }

    private void doDispatch(RoutingContext context, String path, HttpClient client, Future<Object> cbFuture) {
        if (path == null || "".equals(path)) {
            path = "/";
        }
        logger.error(path);
        HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> response.bodyHandler(body -> {
                    // api endpoint server error, circuit breaker should fail
                    if (response.statusCode() >= 500) {
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
                }));
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

    private void noAuth(RoutingContext context) {
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

    private void badGateway(Throwable ex, RoutingContext context) {
        ex.printStackTrace();
        context.response()
                .setStatusCode(502)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "bad_gateway").put("ex", ex.getCause().getMessage())
                        //.put("message", ex.getMessage())
                        .encodePrettily());
    }

    public static void main(String[] args) {

        VertxOptions vO = new VertxOptions();
        vO.setEventLoopPoolSize(16);
        Vertx vertx = Vertx.vertx(vO);
        DeploymentOptions dO = new DeploymentOptions();
        dO.setInstances(1);
        dO.setConfig(new JsonObject().put("config.port", 9100));

        RxHelper.deployVerticle(vertx, new BootGateWay(), dO).subscribe(System.out::println,
                System.err::println);
    }
}
