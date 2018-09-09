package com.jsen.joker.plugin.gateway.mirren.service.impl;

import com.google.common.collect.Lists;
import com.jsen.joker.plugin.gateway.mirren.ApplicationVerticle;
import com.jsen.joker.plugin.gateway.mirren.DeployVerticle;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
import com.jsen.joker.plugin.gateway.mirren.service.AppService;
import com.jsen.test.common.ds.JdbcRepositoryWrapper;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public class AppServiceImpl extends JdbcRepositoryWrapper implements AppService {

    public static AppService appService;

    private static final String GATEWAY_APP = "jk_gateway_app";

    private static final String INSERT_STATEMENT = "INSERT INTO " + GATEWAY_APP + " (name, host, port, timestamp, u_timestamp, metas) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String DELETE_STATEMENT_ID = "DELETE FROM " + GATEWAY_APP + " WHERE id = ?";
    private static final String SELECT_ALL = "SELECT * FROM " + GATEWAY_APP;
    private static final String SELECT_BY_ID = "SELECT * FROM " + GATEWAY_APP + " WHERE id=?";
    private static final String SELECT_BY_NAME = "SELECT * FROM " + GATEWAY_APP + " WHERE name=?";
    private static final String SELECT_USE_EXIST = "SELECT * FROM " + GATEWAY_APP + " WHERE (host=? AND port=?) OR name=?";
    private static final String UPDATE_STATEMENT_ID = "UPDATE " + GATEWAY_APP + " SET name=?,host=?,port=?,u_timestamp=? WHERE id = ?";
    private static final String UPDATE_APIS = "UPDATE " + GATEWAY_APP + " SET name=?,host=?,port=?,u_timestamp=?,metas=? WHERE id = ?";

    public AppServiceImpl(Vertx vertx, JsonObject config) {
        super(AppService.serviceID, vertx, config);
        appService = this;
    }

    private void effHelp(JsonArray params, String sql, Handler<AsyncResult<ResponseBase>> resultHandler) {
        update(params, sql, a -> {
            if(a.succeeded()) {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(0).put("eff", a.result())));
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(a.cause().getMessage())));
            }
        });
    }

    @Override
    public void createApp(App app, Handler<AsyncResult<ResponseBase>> resultHandler) {
        this.retrieveMany(buildParams().add(app.getHost()).add(app.getPort()).add(app.getName()), SELECT_USE_EXIST).setHandler(r -> {
            if (r.succeeded()) {
                if (!r.result().isEmpty()) {
                    resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg("APP with name : " + app.getName() + " exist")));
                } else {
                    long ts = Instant.now().toEpochMilli();
                    JsonArray array = buildParams().add(app.getName()).add(app.getHost()).add(app.getPort()).add(ts).add(ts)
                            .add(app.toJson().toString());
                    effHelp(array, INSERT_STATEMENT, resultHandler);
                }
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void deleteApp(String id, Handler<AsyncResult<ResponseBase>> resultHandler) {
        JsonArray array = buildParams().add(id);
        effHelp(array, DELETE_STATEMENT_ID, resultHandler);
    }

    @Override
    public void getOneApp(String id, Handler<AsyncResult<ResponseBase>> resultHandler) {
        getById(id).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                ResponseBase responseBase = ResponseBase.create().data(r.result().get()).code(0);
                String appName = r.result().get().getString("name");
                ApplicationVerticle applicationVerticle = DeployVerticle.getInstance().getApplicationVerticleMap().get(appName);
                if (applicationVerticle != null) {
                    responseBase.put("onData", applicationVerticle.getApp().toJson());
                }
                resultHandler.handle(Future.succeededFuture(responseBase));
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void listApp(Handler<AsyncResult<ResponseBase>> resultHandler) {
        this.list(SELECT_ALL).setHandler(r -> {
            if (r.succeeded()) {
                List<JsonObject> datas = r.result();
                Map<String, ApplicationVerticle> applicationVerticleMap = DeployVerticle.getInstance().getApplicationVerticleMap();
                datas = datas.stream().peek((item -> {
                    String name = item.getString("name");
                    if (applicationVerticleMap.containsKey(name)) {
                        item.put("on", true);
                    }
                })).collect(Collectors.toList());
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(0).data(datas)));
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void listAppSimple(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        this.list(SELECT_ALL).setHandler(r -> {
            if (r.succeeded()) {
                List<JsonObject> datas = r.result();
                Map<String, ApplicationVerticle> applicationVerticleMap = DeployVerticle.getInstance().getApplicationVerticleMap();
                datas = datas.stream().peek((item -> {
                    String name = item.getString("name");
                    if (applicationVerticleMap.containsKey(name)) {
                        item.put("on", true);
                    }
                })).collect(Collectors.toList());
                resultHandler.handle(Future.succeededFuture(datas));
            } else {
                resultHandler.handle(Future.failedFuture(r.cause()));
            }
        });
    }

    @Override
    public void createApi(String id, Api api, Handler<AsyncResult<ResponseBase>> resultHandler) {
        getById(id).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                App app = new App(new JsonObject(r.result().get().getString("metas")));
                if (app.getApis().stream().filter(item -> Objects.equals(item.getName(), api.getName())).collect(Collectors.toSet()).size() > 0) {
                    resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg("api名字存在")));
                    return;
                }
                if (app.getApis().stream().filter(item -> Objects.equals(item.getPath(), api.getPath())).collect(Collectors.toSet()).size() > 0) {
                    resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg("api路径存在")));
                    return;
                }
                app.getApis().add(api);
                updateAppAllInfo(id, app, resultHandler);
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void deleteApi(String id, String name, Handler<AsyncResult<ResponseBase>> resultHandler) {
        getById(id).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                App app = new App(new JsonObject(r.result().get().getString("metas")));
                app.setApis(app.getApis().stream().filter(item -> !Objects.equals(item.getName(), name)).collect(Collectors.toSet()));
                updateAppAllInfo(id, app, resultHandler);
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });

    }

    private Future<Optional<JsonObject>> getById(String id) {
        return findOne(buildParams().add(id), SELECT_BY_ID);
    }

    private Future<Optional<JsonObject>> getByName(String name) {
        return findOne(buildParams().add(name), SELECT_BY_NAME);
    }

    @Override
    public void updateApp(String id, App app, Handler<AsyncResult<ResponseBase>> resultHandler) {

        getById(id).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                String metas = r.result().get().getString("metas");
                JsonObject mt = new JsonObject(metas);
                JsonObject nMt = app.toJson();
                nMt.remove("apis");
                nMt.remove("on");
                nMt.remove("createTime");
                nMt.remove("updateTime");
                mt.mergeIn(nMt);
//                mt.put("remark", app.getRemark());
                app.setUpdateTime(Instant.now().toEpochMilli());
                this.update(new JsonArray().add(app.getName()).add(app.getHost()).add(app.getPort()).add(app.getUpdateTime()).add(mt.toString()).add(id), UPDATE_APIS, r2 -> {
                    if (r2.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(0).data(r2.result())));
                    } else {
                        resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r2.cause().getMessage())));
                    }
                });
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });



    }

    @Override
    public void updateApi(String appId, Api api, Handler<AsyncResult<ResponseBase>> resultHandler) {
        getById(appId).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                App app = new App(new JsonObject(r.result().get().getString("metas", "{}")));


                if (app.getApis().stream().filter(item -> Objects.equals(item.getPath(), api.getPath()) && !Objects.equals(item.getName(), api.getName())).collect(Collectors.toSet()).size() > 0) {
                    resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg("api路径存在")));
                    return;
                }

                app.setApis(app.getApis().stream().map(item -> {
                    if (!Objects.equals(item.getName(), api.getName())) {
                        return item;
                    } else {
                        return api;
                    }
                }).collect(Collectors.toSet()));
                updateAppAllInfo(appId, app, resultHandler);
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void updateAppState(String name, boolean on, Handler<AsyncResult<ResponseBase>> resultHandler) {
        getByName(name).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                JsonObject obj = r.result().get();
                String metas = obj.getString("metas");
                JsonObject mt = new JsonObject(metas);
                App app = new App(mt);
                app.setOn(on);

                app.setUpdateTime(Instant.now().toEpochMilli());
                this.update(new JsonArray().add(app.getName()).add(app.getHost()).add(app.getPort()).add(app.getUpdateTime()).add(app.toJson().toString()).add(obj.getValue("id")), UPDATE_APIS, r2 -> {
                    if (r2.succeeded()) {
                        resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(0).data(r2.result())));
                    } else {
                        resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r2.cause().getMessage())));
                    }
                });
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void updateApiState(String appName, String name, boolean on, Handler<AsyncResult<ResponseBase>> resultHandler) {
        getByName(appName).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                JsonObject obj = r.result().get();
                App app = new App(new JsonObject(obj.getString("metas", "{}")));
                app.setApis(app.getApis().stream().map(item -> {
                    if (!Objects.equals(item.getName(), name)) {
                        return item;
                    } else {
                        item.setOn(on);
                        return item;
                    }
                }).collect(Collectors.toSet()));
                updateAppAllInfo(obj.getInteger("id") + "", app, resultHandler);
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void updateAllApiState(String appName, boolean on, Handler<AsyncResult<ResponseBase>> resultHandler) {
        getByName(appName).setHandler(r -> {
            if (r.succeeded() && r.result().isPresent()) {
                JsonObject obj = r.result().get();
                App app = new App(new JsonObject(obj.getString("metas", "{}")));
                app.setApis(app.getApis().stream().peek(item -> item.setOn(on)).collect(Collectors.toSet()));
                updateAppAllInfo(obj.getInteger("id") + "", app, resultHandler);
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    private void updateAppAllInfo(String id, App app, Handler<AsyncResult<ResponseBase>> resultHandler) {
        app.setUpdateTime(Instant.now().toEpochMilli());
        this.update(new JsonArray().add(app.getName()).add(app.getHost()).add(app.getPort()).add(app.getUpdateTime()).add(app.toJson().toString()).add(id), UPDATE_APIS, r -> {
            if (r.succeeded()) {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(0).data(r.result())));
            } else {
                resultHandler.handle(Future.succeededFuture(ResponseBase.create().code(1).msg(r.cause().getMessage())));
            }
        });
    }

    @Override
    public void close(Handler<AsyncResult<Void>> resultHandler) {
        this.client.close(resultHandler);
    }
}
