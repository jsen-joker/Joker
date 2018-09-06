package com.jsen.joker.plugin.gateway.mirren;

import com.google.common.collect.Maps;
import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.model.GateWay;
import com.jsen.test.common.RestVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p>
 *     包括项目、api 部署  启动
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class DeployVerticle extends RestVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployVerticle.class);

    private Map<String, ApplicationVerticle> applicationVerticleMap = Maps.newConcurrentMap();

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        vertx.eventBus().consumer(EventKey.App.DEPLOY, this::deploymentAPP);
        vertx.eventBus().consumer(EventKey.App.UNDEPLOY, this::unDeploymentAPP);


        super.start(startFuture);
    }

    private void deploymentAPP(Message<JsonObject> msg) {

        JsonObject js = msg.body();
        GateWay gateWay = GateWay.fromJson(js);

        DeploymentOptions options = new DeploymentOptions();
        options.setIsolationGroup(gateWay.getName());
        options.setConfig(new JsonObject().put("app", js));
        vertx.deployVerticle(ApplicationVerticle.class.getName(), options, res -> {
            if (res.succeeded()) {
                LOGGER.info("deploy app succeed : " + gateWay.getName());
            } else {
                LOGGER.error("deploy app failed : " + res.cause().getMessage());
                LOGGER.error(gateWay.getName());
            }
        });
    }

    private void unDeploymentAPP(Message<String> msg) {
        String name = msg.body();
        ApplicationVerticle applicationVerticle = applicationVerticleMap.get(name);
        vertx.undeploy(applicationVerticle.deploymentID(), res -> {
            if (res.succeeded()) {
                LOGGER.info("unDeploy app succeed : " + name);
            } else {
                LOGGER.error("unDeploy app failed : " + res.cause().getMessage());
            }
        });
    }

}
