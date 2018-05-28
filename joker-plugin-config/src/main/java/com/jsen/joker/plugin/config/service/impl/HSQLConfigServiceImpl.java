package com.jsen.joker.plugin.config.service.impl;

import com.jsen.joker.plugin.config.service.HSQLConfigService;
import com.jsen.test.common.ds.JdbcRepositoryWrapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/27
 */
public class HSQLConfigServiceImpl extends JdbcRepositoryWrapper implements HSQLConfigService {

    private static final Logger logger = LoggerFactory.getLogger(HSQLConfigServiceImpl.class.getName());

    private static final String INSERT_STATEMENT = "INSERT INTO joker_plugin_config (uuid, comment, endpoint, data, update_time, create_time) VALUES (?,?,?,?,?,?)";
    private static final String SELECT_ALL = "SELECT * FROM joker_plugin_config";
    private static final String SELECT_BY_ENDPOINT = "SELECT * FROM joker_plugin_config WHERE endpoint=?";

    private static final String DELETE_BY_UUID = "DELETE FROM joker_plugin_config WHERE uuid = ?";

    private static final String UPDATE_STATEMENT = "UPDATE joker_plugin_config SET comment=?,endpoint=?,data=?,update_time=? WHERE uuid=?";

    SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HSQLConfigServiceImpl(Vertx vertx, JsonObject config, Future<Void> initer) {
        super(HSQLConfigServiceImpl.class.getName(), vertx, config);
        checkExists(initer);
    }

    private void checkExists(Future<Void> initer) {
        String exists = "CREATE TABLE IF NOT EXISTS joker_plugin_config (uuid varchar(128),comment varchar(128),endpoint varchar(128),data LONGVARCHAR,update_time datetime,create_time datetime)";
        // String exists = "DROP TABLE IF EXISTS joker_plugin_config";

        client.getConnection(connHandler(initer.completer(), connection -> {
            connection.execute(exists, r -> {
                if (r.succeeded()) {
                    logger.info("检查、创建数据库成功");
                    initer.complete();
                } else {
                    logger.info("检查、创建数据库失败");
                    r.cause().printStackTrace();
                    initer.fail(r.cause());
                }
                connection.close();
            });
        }));
    }

    @Override
    public void create(String comment, String endpoint, String data, Handler<AsyncResult<JsonObject>> resultHandler) {
        Date d = new Date();
        JsonArray array = buildParams().add(UUID.randomUUID().toString()).add(comment).add(endpoint).add(data)
                .add(dateTime.format(d))
                .add(dateTime.format(d));
        effHelp(array, INSERT_STATEMENT, resultHandler);
    }

    @Override
    public void list(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        this.list(SELECT_ALL)
                .setHandler(resultHandler);
    }

    @Override
    public void get(String endpoint, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.findOne(new JsonArray().add(endpoint), SELECT_BY_ENDPOINT).setHandler(a -> {
            if (a.succeeded()) {
                Optional<JsonObject> r = a.result();
                if (r.isPresent()) {
                    resultHandler.handle(Future.succeededFuture(r.get()));
                } else {
                    resultHandler.handle(Future.succeededFuture(new JsonObject()));
                }
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonObject()));
            }
        });
    }

    @Override
    public void update(String uuid, String comment, String endpoint, String data, Handler<AsyncResult<JsonObject>> resultHandler) {
        Date d = new Date();
        JsonArray array = buildParams().add(comment).add(endpoint).add(data)
                .add(dateTime.format(d)).add(uuid);
        effHelp(array, UPDATE_STATEMENT, resultHandler);

    }

    @Override
    public void del(String uuid, Handler<AsyncResult<JsonObject>> resultHandler) {

        this.delete(new JsonArray().add(uuid), DELETE_BY_UUID, ar -> {
            if (ar.succeeded()) {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 0)));
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 1).put("msg", "删除失败")));
            }
        });
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
