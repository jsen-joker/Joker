package com.jsen.joker.core.plugin.entry.server.service.impl;

import com.jsen.joker.core.plugin.entry.server.service.ProjectServerService;
import com.jsen.test.common.ds.JdbcRepositoryWrapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public class ProjectServerServiceImpl extends JdbcRepositoryWrapper implements ProjectServerService {
    private static final String INSERT_STATEMENT = "INSERT INTO vertx_project_server (artifact_id, group_id, version, data, enter, upload_time, name, uuid) VALUES (?,?,?,?,?,?,?,?)";
    private static final String EXIST_SELECT_BY_FILENAME = "SELECT version FROM vertx_project_server WHERE name = ?";
    private static final String SELECT_BY_FILENAME = "SELECT * FROM vertx_project_server WHERE name = ?";



    private static final String SELECT_ALL = "SELECT artifact_id, group_id, version, uuid, upload_time, name, enter FROM vertx_project_server";

    private static final String DELETE_BY_FILENAME = "DELETE FROM vertx_project_server WHERE name = ?";


    SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 4 hour
    public ProjectServerServiceImpl(Vertx vertx, JsonObject config) {
        super(ProjectServerService.serviceID, vertx, config);
    }



    @Override
    public void saveFile(String artifactId, String groupId, String version, String data, String className, String name, String uuid, Handler<AsyncResult<JsonObject>> resultHandler) {

        this.findOne(new JsonArray().add(name), EXIST_SELECT_BY_FILENAME).setHandler(a -> {
            if (a.succeeded() && !a.result().isPresent()) {
                JsonArray array = buildParams().add(artifactId).add(groupId).add(version).add(data).add(className).add(dateTime.format(new Date())).add(name).add(uuid);
                effHelp(array, INSERT_STATEMENT, resultHandler);
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 1).put("msg", "文件存在，请检查文件名字")));
            }
        });
    }

    @Override
    public void download(String fileName, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.findOne(new JsonArray().add(fileName), SELECT_BY_FILENAME).setHandler(a -> {
            if (a.succeeded() && a.result().isPresent()) {
                resultHandler.handle(Future.succeededFuture(a.result().get()));
            } else {
                resultHandler.handle(Future.failedFuture(a.cause()));
            }
        });
    }

    @Override
    public void delete(String fileName, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.delete(new JsonArray().add(fileName), DELETE_BY_FILENAME, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 0)));
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 1).put("msg", "删除失败")));
            }
        });
    }

    @Override
    public void listAll(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        this.list(SELECT_ALL)
                .setHandler(resultHandler);
    }

    private void effHelp(JsonArray params, String sql, Handler<AsyncResult<JsonObject>> resultHandler) {
        update(params, sql, a -> {
            if(a.succeeded()) {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 0).put("eff", a.result())));
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 1)));
            }
        });
    }
}
