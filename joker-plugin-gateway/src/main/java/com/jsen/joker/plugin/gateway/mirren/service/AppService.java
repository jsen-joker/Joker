package com.jsen.joker.plugin.gateway.mirren.service;

import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
import com.jsen.joker.plugin.gateway.mirren.service.impl.AppServiceImpl;
import com.jsen.joker.plugin.login.entity.SysPermission;
import com.jsen.joker.plugin.login.entity.SysRole;
import com.jsen.joker.plugin.login.entity.SysUser;
import com.jsen.test.common.utils.response.ResponseBase;
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
public interface AppService {
    public static final String serviceID = "service.joker.gateway";
    // A couple of factory methods to create an instance and a proxy
    static AppService create(Vertx vertx, JsonObject config) {
        return new AppServiceImpl(vertx, config);
    }

    void createApp(App app, Handler<AsyncResult<ResponseBase>> resultHandler);
    void deleteApp(String id, Handler<AsyncResult<ResponseBase>> resultHandler);
    void getOneApp(String id, Handler<AsyncResult<ResponseBase>> resultHandler);
    void listApp(Handler<AsyncResult<ResponseBase>> resultHandler);
    void listAppSimple(Handler<AsyncResult<List<JsonObject>>> resultHandler);

    void createApi(String id, Api api, Handler<AsyncResult<ResponseBase>> resultHandler);
    void deleteApi(String id, String name, Handler<AsyncResult<ResponseBase>> resultHandler);


    void updateApp(String id, App app, Handler<AsyncResult<ResponseBase>> resultHandler);
    void updateApi(String appId, Api api, Handler<AsyncResult<ResponseBase>> resultHandler);

    void updateAppState(String name, boolean on, Handler<AsyncResult<ResponseBase>> resultHandler);
    void updateApiState(String appName, String name, boolean on, Handler<AsyncResult<ResponseBase>> resultHandler);
    void updateAllApiState(String appName, boolean on, Handler<AsyncResult<ResponseBase>> resultHandler);

    void close(Handler<AsyncResult<Void>> resultHandler);

}
