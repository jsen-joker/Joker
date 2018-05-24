package com.jsen.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/24
 */
public class TestBootMulCopy extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(10);

        vertx.deployVerticle(TestBootMulCopy.class.getName(), deploymentOptions, ar -> {
            if (ar.succeeded()) {
                System.out.println(ar.result());
            } else {
                System.out.println("FAILED");
            }
        });

    }


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
        System.out.println("deploy");
        startFuture.complete();
    }
}
