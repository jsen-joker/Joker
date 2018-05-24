package com.jsen.test.common;

import com.jsen.test.common.service.ServiceBase;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
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
public abstract class RxVerticleBase extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RxVerticleBase.class);

    protected ServiceDiscovery serviceDiscovery;

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     */
    @Override
    public void start(Future<Void> startFuture) {
        // init service discovery instance
        // serviceDiscovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
        serviceDiscovery = ServiceDiscovery.create(vertx.getDelegate(), new ServiceDiscoveryOptions()
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
            new ServiceBinder(vertx.getDelegate()).setAddress(((ServiceBase)service).getName())
                    .register(interfaceName, service);
        }
    }

    // discover

    // http 的服务发现 ：发布一个http的rest服务
    protected Future<Void> discoveryHttpEndpoint(String name, String host, int port, String root) {
        Record record = HttpEndpoint.createRecord(name, host, port, root,
                new JsonObject().put("api.name", config().getString("api.name", "")));
        return publish(record);
    }
    protected Future<Void> discoveryEventBusService(String name, String address, Class serviceClass) {
        Record record = EventBusService.createRecord(name, address, serviceClass);
        return publish(record);
    }

    protected Set<Record> registeredRecords = new ConcurrentHashSet<>();
    private Future<Void> publish(Record record) {
        if (serviceDiscovery == null) {
            try {
                start();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot create discovery service");
            }
        }

        Future<Void> future = Future.future();
        // publish the service
        serviceDiscovery.publish(record, ar -> {
            if (ar.succeeded()) {
                registeredRecords.add(record);
                if (record.getLocation().getString("root", "").isEmpty()) {
                    logger.info("*** 服务发现（Service）名字：" + ar.result().getName() + " ***");
                } else {
                    logger.info("*** 服务发现（HTTP）名字：" + ar.result().getName() + ", endpoint：" + record.getLocation().getString("root") + " ***");
                }
                future.complete();
            } else {
                future.fail(ar.cause());
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
    public void stop(Future<Void> stopFuture) throws Exception {
        List<Future> futures = registeredRecords.stream().map(r -> {
            Future<Void> future = Future.future();
            serviceDiscovery.unpublish(r.getRegistration(), future.completer());
            return future;
        }).collect(Collectors.toList());

        if (futures.isEmpty()) {
            serviceDiscovery.close();
            stopFuture.complete();
        } else {
            CompositeFuture.all(futures)
                    .setHandler(ar -> {
                        serviceDiscovery.close();
                        if (ar.failed()) {
                            stopFuture.fail(ar.cause());
                        } else {
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
