package com.jsen.joker.core.plugin.manager.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.io.File;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/16
 */
public class Downloader {

    private String projectServerHost;
    private int projectServerPort;
    private Vertx vertx;
    private WebClient webClient;

    public Downloader(AbstractVerticle verticle, WebClient webClient) {
        this.webClient = webClient;
        this.vertx = verticle.getVertx();
        JsonObject config = verticle.config();
        projectServerHost = config.getString("enter.server.host");
        projectServerPort = config.getInteger("enter.server.port");
    }

    public void loadData(String fileName, File target, Future<Void> future) {
        if (target.exists()) {
            future.complete();
        } else {
            // download file
            webClient.get(projectServerPort, projectServerHost,  "/download/" + fileName).send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
                    vertx.fileSystem().writeFile(target.getAbsolutePath(), response.body(), ar2 -> {
                        future.complete();
                    });
                } else {
                    future.fail(ar.cause());
                }
            });
        }
    }

}
