package com.jsen.vertx.config;

import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.utils.PathHelp;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Optional;


/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/9
 */
public class ConfigVerticle extends RestVerticle {

    private String root;

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     * @param startFuture
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        Router router = Router.router(vertx);

        router.get("/config/:endpoint").handler(this::getConfig);
        vertx.createHttpServer().requestHandler(router::accept).listen(9000);
        startFuture.complete();

        root = PathHelp.getJokerRoot();
        root = new File(root, "conf").getAbsolutePath();
    }

    private void getConfig(RoutingContext routingContext) {
        Future<JsonObject> result = Future.future();
        result.setHandler(j -> {
            if (j.succeeded()) {
                routingContext.response().putHeader("content-type", "application/json")
                        .end(Optional.ofNullable(j.result().getJsonObject(routingContext.request().getParam("endpoint"))).orElse(new JsonObject())
                                .encodePrettily());
            } else {
                notFound(routingContext);
            }
        });

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                new File(root, "config.json")
        )))) {
            String data = "";
            String line;
            while ((line = reader.readLine()) != null) {
                data += line;
            }
            JsonObject jsonObject = new JsonObject(data);
            result.complete(jsonObject);
        } catch (Exception e) {
            result.fail(e);
        }

    }


}
