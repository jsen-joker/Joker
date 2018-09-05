package com.jsen.joker.core.plugin.manager.master;

import com.jsen.joker.annotation.annotation.Entry;
import com.jsen.joker.core.plugin.manager.master.vertx.CombineServiceDiscoveryRestEndpoint;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.joker.JokerStaticHandlerImpl;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/19
 */
@Entry(priority = 10)
public class JokerCoreManagerMaster extends RestVerticle {
    private static final Logger logger = LoggerFactory.getLogger(JokerCoreManagerMaster.class);

    // private MetricsService service;

    private String entryServerHost;
    private Integer entryServerPort;

    private WebClient webClient;
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);


        /*
        用于 socket js 连接 获取 服务的统计信息
         */
        /*
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.monitor.metrics"))
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.log"))
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.discovery"))
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.hb"))
                .addOutboundPermitted(new PermittedOptions().setAddress("events.log"));

        sockJSHandler.bridge(options);
        */

        //service = MetricsService.create(vertx);


        // send metrics message to the event bus
        /*
        vertx.setPeriodic(10000, t -> {
            JsonObject metrics = service.getMetricsSnapshot(vertx);
            vertx.eventBus().send("to.joker.manager.master.monitor.metrics", metrics);
        });*/

        // router.route("/eventbus/*").handler(sockJSHandler);

        webClient = WebClient.create(vertx);
        entryServerHost = config().getString("enter.server.host");
        entryServerPort = config().getInteger("enter.server.port");

        CombineServiceDiscoveryRestEndpoint.create(router, vertx, serviceDiscovery);
        StaticHandler staticHandler = new JokerStaticHandlerImpl(this.getClass());

        router.route("/entry/list").handler(this::entryList);
        router.route("/isCluster").handler(rc -> {
            rc.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("code", "yes".equalsIgnoreCase(config().getString("core.cluster", "no"))).toString());
        });
        router.route("/*").handler(staticHandler);

        // ServiceDiscoveryRestEndpoint.create(router, serviceDiscovery);

        startServer(startFuture, config().getInteger("manager.master.port", 9090));
        // startServer(startFuture, 9999, "localhost");

    }

    private void  entryList(RoutingContext routingContext) {
        webClient.get(entryServerPort, entryServerHost,  "/list").send(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).putHeader("Content-Type", "application/json")
                        .end(ar.result().body());
            } else {
                routingContext.fail(ar.cause());
            }
        });
    }



}
