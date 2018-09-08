package com.jsen.joker.core.plugin.manager;

import com.jsen.joker.boot.RootVerticle;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/8/20
 */
class StaticStop {
    private static final Logger logger = LoggerFactory.getLogger(StaticStop.class);

    static void stop() {
        Future<Void> future = Future.future();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                logger.info("*********************************************\n*********************************************\n*********************************************\n*********************************************\n*********************************************\n");
                logger.info("停止Joker成功");
                logger.info("*********************************************\n*********************************************\n*********************************************\n*********************************************\n*********************************************\n");
                System.exit(0);
            } else {
                logger.info("*********************************************\n*********************************************\n*********************************************\n*********************************************\n*********************************************\n");
                logger.error("Stop JokerDeploymentManager failed:" + ar.cause().getMessage());
                logger.info("*********************************************\n*********************************************\n*********************************************\n*********************************************\n*********************************************\n");
                System.exit(1);
            }
        });
        try {
            RootVerticle.getDefaultRootVerticle().exit(future);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
