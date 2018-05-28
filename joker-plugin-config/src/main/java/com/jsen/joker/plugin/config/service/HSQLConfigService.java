package com.jsen.joker.plugin.config.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/27
 */
public interface HSQLConfigService {


    void create(String comment, String endpoint, String data, Handler<AsyncResult<JsonObject>> resultHandler);
    void list(Handler<AsyncResult<List<JsonObject>>> resultHandler);
    void get(String endpoint, Handler<AsyncResult<JsonObject>> resultHandler);
    void update(String uuid, String comment, String endpoint, String data, Handler<AsyncResult<JsonObject>> resultHandler);
    void del(String uuid, Handler<AsyncResult<JsonObject>> resultHandler);

}
