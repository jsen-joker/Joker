package com.jsen.joker.core.plugin.manager.master.vertx;

import com.google.common.collect.Lists;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/22
 */
public class CombineServiceDiscoveryRestEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CombineServiceDiscoveryRestEndpoint.class);

    private static final String GET_METRICS = "joker.manager.master.get.monitor.metrics";
    private static final String GET_LOG = "joker.manager.master.get.monitor.logs";
    private static final String GET_HB = "joker.manager.master.get.monitor.hb";
    private static final String REMOVE_HB = "joker.manager.master.get.monitor.hb.remove";
    // private static final String GET_DISCOVERY = "joker.manager.master.get.monitor.discovery";
    private static final String GET_ENTRIES = "joker.manager.master.get.monitor.entries";

    private static final String ADD_JAR = "joker.manager.master.get.monitor.add.jar";
    private static final String DEL_JAR = "joker.manager.master.get.monitor.del.jar";
    private static final String JAR_LIST = "joker.manager.master.get.monitor.jar.list";

    private final EventBus eventBus;

    private ServiceDiscoveryManager serviceDiscoveryManager;
    private final Set<String> jokerManagerSet = new HashSet<>();
    private final Map<String, Long> timeoutMap = new HashMap<>();
    public static CombineServiceDiscoveryRestEndpoint create(Router router, Vertx vertx, ServiceDiscovery serviceDiscovery) {
        return new CombineServiceDiscoveryRestEndpoint(router, vertx, "/discovery", serviceDiscovery);
    }

    public static CombineServiceDiscoveryRestEndpoint create(Router router, Vertx vertx, String root, ServiceDiscovery serviceDiscovery) {
        return new CombineServiceDiscoveryRestEndpoint(router, vertx, root, serviceDiscovery);
    }

    private CombineServiceDiscoveryRestEndpoint(Router router, Vertx vertx, String root, ServiceDiscovery serviceDiscovery) {
        Objects.requireNonNull(router);
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(root);
        this.eventBus = vertx.eventBus();
        this.registerRoutes(router, root);
        this.registerHbHandler();
        this.serviceDiscoveryManager = new ServiceDiscoveryManager(serviceDiscovery);

        eventBus.consumer(REMOVE_HB, msg -> {
            logger.debug(msg.address() + " hb remove " + msg.body().toString());
            jokerManagerSet.remove(msg.body().toString());
            timeoutMap.remove(msg.body().toString());
        });

        vertx.setPeriodic(30000, ar -> {
            long current = System.currentTimeMillis();
            List<String> remove = Lists.newArrayList();
            for (Map.Entry<String, Long> entry:timeoutMap.entrySet()) {
                if (current - entry.getValue() > 30000) {
                    remove.add(entry.getKey());
                    jokerManagerSet.remove(entry.getKey());
                }
            }
            for (String key : remove) {
                timeoutMap.remove(key);
            }
        });
    }

    private void registerRoutes(Router router, String root) {
        // 所有 discovery
        router.get(root).handler(this::all);
        router.get("/logs").handler(this::logs);
        router.get("/metrics").handler(this::metrics);
        // 所有joker节点
        router.get("/jokers").handler(this::jokers);
        router.get("/joker/log/:uuid").handler(this::jokerLog);
        router.get("/joker/metrics/:uuid").handler(this::jokerMetrics);
        router.get("/joker/entries/:uuid").handler(this::jokerEntries);

        router.get("/joker/jar/add/:uuid/:fileName").handler(this::jokerAddJar);
        router.get("/joker/jar/del/:uuid/:fileName").handler(this::jokerDelJar);
        router.get("/joker/jar/list/:uuid").handler(this::jokerListJar);

        router.get(root + "/:uuid").handler(this::one);
        router.delete(root + "/:uuid").handler(this::unpublish);
        router.route().handler(BodyHandler.create());
        router.post(root).handler(this::publish);
        router.put(root + "/:uuid").handler(this::update);

    }

    /**
     *  manager 节点发送心跳在这里保存
      */
    private void registerHbHandler() {
        eventBus.consumer(GET_HB, msg -> {
            logger.debug(msg.address() + " hb " + msg.body().toString());
            jokerManagerSet.add(msg.body().toString());
            timeoutMap.put(msg.body().toString(), System.currentTimeMillis());
        });
    }

    private void update(RoutingContext routingContext) {
        String uuid = routingContext.request().getParam("uuid");
        JsonObject json = routingContext.getBodyAsJson();


        if (uuid == null || json == null) {
            routingContext.fail(new NullPointerException("request body or uuid can not be null"));
            return;
        }

        serviceDiscoveryManager.update(uuid, json).setHandler(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(ar.result().toString());
            } else {
                routingContext.fail(ar.cause());
            }
        });
    }

    private void unpublish(RoutingContext routingContext) {
        String uuid = routingContext.request().getParam("uuid");
        serviceDiscoveryManager.unpublish(uuid != null ? uuid : "").setHandler(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(ar.result().toString());
            } else {
                routingContext.fail(ar.cause());
            }
        });
    }

    private void one(RoutingContext routingContext) {
        String uuid = routingContext.request().getParam("uuid");

        serviceDiscoveryManager.one(uuid != null ? uuid : "").setHandler(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(ar.result().toString());
            } else {
                routingContext.fail(ar.cause());
            }
        });
    }

    private void publish(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();

        if (json == null) {
            routingContext.fail(new NullPointerException("request body can not be null"));
            return;
        }

        serviceDiscoveryManager.publish(json).setHandler(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(ar.result().toString());
            } else {
                routingContext.fail(ar.cause());
            }
        });

    }

    private void all(RoutingContext routingContext) {
        String query = routingContext.request().params().get("query");

        serviceDiscoveryManager.all(query != null ? query : "").setHandler(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(ar.result().toString());
            } else {
                routingContext.fail(ar.cause());
            }
        });
    }

    private void logs(RoutingContext routingContext) {
        getCombinedResult(GET_LOG, "").setHandler(ar -> {
            if (ar.failed()) {
                routingContext.fail(ar.cause());
            } else {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json").end((ar.result()).toString());
            }

        });
    }

    private void jokerLog(RoutingContext routingContext) {
        String uuid = routingContext.request().params().get("uuid");
        logger.error(uuid + GET_LOG);

        eventBus.send(uuid + GET_LOG, "", result -> {
            if (result.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(result.result().body().toString());
            } else {
                // delete
                result.cause().printStackTrace();
                routingContext.fail(result.cause());
            }
        });
    }

    private void metrics(RoutingContext routingContext) {
        getCombinedResult(GET_METRICS, "").setHandler(ar -> {
            if (ar.failed()) {
                routingContext.fail(ar.cause());
            } else {
                routingContext.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end((ar.result()).toString());
            }

        });
    }


    private void jokerMetrics(RoutingContext routingContext) {
        String uuid = routingContext.request().params().get("uuid");
        eventBus.send(uuid + GET_METRICS, "", result -> {
            if (result.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(result.result().body().toString());
            } else {
                // delete
                routingContext.fail(result.cause());
            }
        });
    }


    private void jokerEntries(RoutingContext routingContext) {
        String uuid = routingContext.request().params().get("uuid");
        eventBus.send(uuid + GET_ENTRIES, "", result -> {
            if (result.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(result.result().body().toString());
            } else {
                // delete
                routingContext.fail(result.cause());
            }
        });
    }

    private void jokerAddJar(RoutingContext routingContext) {
        String uuid = routingContext.request().params().get("uuid");
        String fileName = routingContext.request().params().get("fileName");

        eventBus.send(uuid + ADD_JAR, fileName, result -> {
            if (result.succeeded()) {
                String r = result.result().body().toString();
                if ("ok".equals(r)) {
                    routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("code", 0).toString());
                } else {
                    routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("code", 1).put("msg", r).toString());
                }
            } else {
                // delete
                routingContext.fail(result.cause());
            }
        });
    }

    private void jokerDelJar(RoutingContext routingContext) {
        String uuid = routingContext.request().params().get("uuid");
        String fileName = routingContext.request().params().get("fileName");

        eventBus.send(uuid + DEL_JAR, fileName, result -> {
            if (result.succeeded()) {
                String r = result.result().body().toString();
                if ("ok".equals(r)) {
                    routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("code", 0).toString());
                } else {
                    routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("code", 1).put("msg", r).toString());
                }
            } else {
                // delete
                routingContext.fail(result.cause());
            }
        });
    }

    private void jokerListJar(RoutingContext routingContext) {
        String uuid = routingContext.request().params().get("uuid");

        eventBus.send(uuid + JAR_LIST, "", result -> {
            if (result.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(result.result().body().toString());
            } else {
                // delete
                routingContext.fail(result.cause());
            }
        });
    }


    private void jokers(RoutingContext routingContext) {
        JsonArray data = new JsonArray();
        jokerManagerSet.forEach(data::add);
        routingContext.response().setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(data.toString());
    }

    private Future<JsonArray> getCombinedResult(String suffix, Object params) {
        Future<JsonArray> _r = Future.future();
        JsonArray _rs = new JsonArray();

        List<Future> futureList = new ArrayList<>();
        List<String> deleteManagers = new ArrayList<>();
        for (String add : jokerManagerSet) {
            Future future = Future.future();
            futureList.add(future);
            eventBus.send(add + suffix, params, result -> {
                if (result.succeeded()) {
                    _rs.add(result.result().body());
                } else {
                    // delete
                    _rs.add(new JsonObject());
                    deleteManagers.add(add);
                }
                future.complete();
            });
        }
        CompositeFuture.all(futureList).setHandler(ok -> {
            for (String delAdd:deleteManagers) {
                jokerManagerSet.remove(delAdd);
            }
            if (ok.succeeded()) {
                _r.complete(_rs);
            } else {
                _r.fail(ok.cause());
            }
        });
        return _r;
    }
}
