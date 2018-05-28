package com.jsen.test.common;

import com.jsen.test.common.service.ServiceBase;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *     服务发现
 *     服务、proxy handler注册
 *     错误处理
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public abstract class VerticleBase extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(VerticleBase.class);

    protected ServiceDiscovery serviceDiscovery;

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        // init service discovery instance
        // serviceDiscovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
        serviceDiscovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions()
                .setAnnounceAddress("mbpt-announce")
                .setName("mbpt-name"));
    }

    // proxy

    /**
     * 发布一个远程服务
     * @param interfaceName
     * @param service
     * @param <T>
     */
    protected <T> void registerProxyService(Class<T> interfaceName, T service) {
        // create the service instance
        // EchoService counterService = new EchoServiceImpl();
        // Register the handler
        if (service instanceof ServiceBase) {
            new ServiceBinder(vertx).setAddress(((ServiceBase)service).getName())
                    .register(interfaceName, service);
        }
    }

    // discover

    // http 的服务发现 ：发布一个http的rest服务
    protected Future<Void> discoveryHttpEndpoint(String name, String host, int port, String endpoint) {
        Record record = HttpEndpoint.createRecord(name, host, port, endpoint,
                new JsonObject().put("endpoint", endpoint));
        return publish(record);
    }
    protected Future<Void> discoveryEventBusService(String name, String address, Class serviceClass) {
        Record record = EventBusService.createRecord(name, address, serviceClass);
        return publish(record);
    }

    private Set<Record> registeredRecords = new ConcurrentHashSet<>();
    private Future<Void> publish(Record record) {
        Future<Void> future = Future.future();
        if (serviceDiscovery == null) {
            try {
                start();
            } catch (Exception e) {
                future.fail(new IllegalStateException("Cannot create discovery service"));
                return future;
            }
        }

        serviceDiscovery.getRecords(r -> {
            JsonObject r1 = r.toJson();
            r1.remove("registration");
            JsonObject r2 = record.toJson();
            r2.remove("registration");
            r2.put("status", "UP");
            System.out.println(r1.toString());
            System.out.println(r2.toString());
            return r1.toString().equals(r2.toString());
        }, ar0 -> {
            if (ar0.succeeded() && ar0.result() != null && !ar0.result().isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (Record r0 : ar0.result()) {
                    builder.append(r0.toJson().encodePrettily());
                }
                logger.error("服务存在：" + builder.toString());
                future.complete();
            } else {
                if (ar0.failed()) {
                    logger.error("发布服务失败，获取服务详细信息失败");
                    ar0.cause().printStackTrace();
                    future.fail("发布服务失败，获取服务详细信息失败");
                } else {
                    // publish the service
                    serviceDiscovery.publish(record, ar -> {
                        if (ar.succeeded()) {
                            registeredRecords.add(record);
                            if (record.getLocation().getString("root", "").isEmpty()) {
                                logger.info("*** 服务发现（Service）名字：" + ar.result().getName() + " ***");
                            } else {
                                logger.info("*** 服务发现（HTTP）名字：" + ar.result().getName() + ", endpoint：" + record.getMetadata().getString("endpoint") + " ***");
                            }
                            future.complete();
                        } else {
                            future.fail(ar.cause());
                        }
                    });


                }
            }
        });

        return future;
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
        List<Future> futures = registeredRecords.stream().map(r -> {
            Future<Void> future = Future.future();
            serviceDiscovery.unpublish(r.getRegistration(), future.completer());
            return future;
        }).collect(Collectors.toList());

        logger.debug("records size:" + registeredRecords.size());
        if (futures.isEmpty()) {
            logger.debug("empty records closed");
            serviceDiscovery.close();
            stopFuture.complete();
        } else {
            CompositeFuture.all(futures)
                    .setHandler(ar -> {
                        serviceDiscovery.close();
                        if (ar.failed()) {
                            logger.debug("records close failed");
                            ar.cause().printStackTrace();
                            stopFuture.fail(ar.cause());
                        } else {
                            logger.debug("records close succeed");
                            stopFuture.complete();
                        }
                    });
        }
    }


    // error handle

    protected void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

}
