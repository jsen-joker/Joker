package com.jsen.joker.core.plugin.entry.server.service;

import com.jsen.joker.core.plugin.entry.server.service.impl.ProjectServerServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
@VertxGen
@ProxyGen
public interface ProjectServerService {
    public static final String serviceID = "service.project.server";
    // A couple of factory methods to create an instance and a proxy
    static ProjectServerService create(Vertx vertx, JsonObject config) {
        return new ProjectServerServiceImpl(vertx, config);
    }

    static ProjectServerService createProxy(Vertx vertx) {
        return new ProjectServerServiceVertxEBProxy(vertx, serviceID);
    }


    void saveFile(String artifactId, String groupId, String version, String data, String className, String name, String uuid,
                    Handler<AsyncResult<JsonObject>> resultHandler);

    void download(String fileName, Handler<AsyncResult<JsonObject>> resultHandler);
    void delete(String fileName, Handler<AsyncResult<JsonObject>> resultHandler);

    void listAll(Handler<AsyncResult<List<JsonObject>>> resultHandler);

}
