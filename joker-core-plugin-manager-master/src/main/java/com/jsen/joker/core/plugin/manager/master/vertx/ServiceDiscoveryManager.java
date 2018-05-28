package com.jsen.joker.core.plugin.manager.master.vertx;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/22
 */
public class ServiceDiscoveryManager {
    // public static final String DEFAULT_ROOT = "/discovery";
    private final ServiceDiscovery discovery;

    /*
    public static ServiceDiscoveryManager create(Router router, ServiceDiscovery discovery) {
        return new ServiceDiscoveryManager(router, discovery, "/discovery");
    }

    public static ServiceDiscoveryManager create(Router router, ServiceDiscovery discovery, String root) {
        return new ServiceDiscoveryManager(router, discovery, root);
    }

    private ServiceDiscoveryManager(Router router, ServiceDiscovery discovery, String root) {
        Objects.requireNonNull(router);
        Objects.requireNonNull(discovery);
        Objects.requireNonNull(root);
        this.discovery = discovery;
        this.registerRoutes(router, root);
    }
    */

    public ServiceDiscoveryManager(ServiceDiscovery serviceDiscovery) {
        Objects.requireNonNull(serviceDiscovery);
        this.discovery = serviceDiscovery;
    }

    /*
    private void registerRoutes(Router router, String root) {
        router.get(root).handler(this::all);
        router.get(root + "/:uuid").handler(this::one);
        router.delete(root + "/:uuid").handler(this::unpublish);
        router.route().handler(BodyHandler.create());
        router.post(root).handler(this::publish);
        router.put(root + "/:uuid").handler(this::update);
    }
    */




    public Future<JsonArray> all(String query) {
        Future<JsonArray> result = Future.future();

        JsonObject filter = new JsonObject();
        if (query != null && !"".equals(query)) {
            try {
                String decoded = URLDecoder.decode(query, "UTF-8");
                filter = new JsonObject(decoded);
            } catch (UnsupportedEncodingException var5) {
                result.fail(var5);
            }
        }
        this.discovery.getRecords(new JsonObject(), ar -> {
            System.out.println(ar.result().size());
        });
        this.discovery.getRecords(filter, (ar) -> {
            if (ar.succeeded()) {
                result.complete(new JsonArray(ar.result().stream().map(Record::toJson).collect(Collectors.toList())));
            } else {
                ar.cause().printStackTrace();
                result.fail(ar.cause());
            }
        });
        return result;
    }

    public Future<JsonObject> one(String uuid) {
        Future<JsonObject> result = Future.future();
        this.discovery.getRecord((new JsonObject()).put("registration", uuid), (ar) -> {
            if (ar.failed()) {
                result.fail(ar.cause());
            } else if (ar.result() == null) {
                result.fail("没有数据");
            } else {
                result.complete(ar.result().toJson());
            }

        });
        return result;
    }

    public Future<JsonObject> unpublish(String uuid) {
        Future<JsonObject> result = Future.future();

        this.discovery.unpublish(uuid, (ar) -> {
            if (ar.failed()) {
                result.fail(ar.cause());
            } else {
                result.complete();
            }
        });
        return result;
    }

    public Future<JsonObject> publish(JsonObject json) {
        Future<JsonObject> result = Future.future();
        Record record = new Record(json);
        this.discovery.publish(record, (ar) -> {
            if (ar.failed()) {
                result.fail(ar.cause());
            } else {
                result.complete(ar.result().toJson());
            }
        });
        return result;
    }

    public Future<JsonObject> update(String uuid, JsonObject json) {
        Future<JsonObject> result = Future.future();
        Record record = new Record(json);
        if (!uuid.equals(record.getRegistration())) {
            result.fail("没有服务注册");
        } else {
            this.discovery.update(record, (ar) -> {
                if (ar.failed()) {
                    result.fail(ar.cause());
                } else {
                    result.complete(ar.result().toJson());
                }

            });
        }
        return result;
    }
}
