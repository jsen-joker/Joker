package com.jsen.joker.plugin.config;

import com.jsen.joker.plugin.config.service.HSQLConfigService;
import com.jsen.joker.plugin.config.service.impl.HSQLConfigServiceImpl;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.joker.JokerStaticHandlerImpl;
import com.jsen.test.common.utils.PathHelp;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.hsqldb.lib.StringUtil;

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

    private HSQLConfigService hsqlConfigService;

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

        initConfig();

        Future<Void> dbInit = Future.future();
        hsqlConfigService = new HSQLConfigServiceImpl(vertx, config(), dbInit);
        dbInit.setHandler(r -> {
            if (r.succeeded()) {

                router.get("/config/:endpoint").handler(this::getConfig);
                router.post("/config").handler(this::create);
                router.put("/config").handler(this::update);
                router.delete("/config").handler(this::del);
                router.get("/configs").handler(this::list);

                StaticHandler staticHandler = new JokerStaticHandlerImpl(this.getClass());
                router.route("/*").handler(staticHandler);

                vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("config.port", 9000));
                startFuture.complete();

                root = PathHelp.getJokerRoot();
                root = new File(root, "conf").getAbsolutePath();
            } else {
                startFuture.fail(r.cause());
            }
        });
    }



    private static final String e_db_url = "plugin.config.db.url";
    private static final String e_db_class = "plugin.config.db.driver_class";
    private static final String e_db_user = "plugin.config.db.user";
    private static final String e_db_password = "plugin.config.db.password";
    private void initConfig() {


        if (!config().containsKey(e_db_url)) {
            String path = new File(new File(new File(PathHelp.getJokerRoot(), "cache"), "joker_hsqldb"), "joker_hsqldb").getAbsolutePath();
            config().put(e_db_url, "jdbc:hsqldb:file:" + path + "?shutdown=true");
        }
        if (!config().containsKey(e_db_class)) {
            config().put(e_db_class, "org.hsqldb.jdbcDriver");
        }
        if (!config().containsKey(e_db_user)) {
            config().put(e_db_user, "");
        }
        if (!config().containsKey(e_db_password)) {
            config().put(e_db_password, "");
        }
        config().put("url", config().getString(e_db_url));
        config().put("driver_class", config().getString(e_db_class));
        config().put("user", config().getString(e_db_user));
        config().put("password", config().getString(e_db_password));

    }
    private void getConfig(RoutingContext routingContext) {
        String ep = routingContext.request().getParam("endpoint");
        if (ep == null || "".equals(ep)) {
            routingContext.response().putHeader("content-type", "application/json")
                    .end(new JsonObject().encodePrettily());
            return;
        }



        Future<JsonObject> result = Future.future();
        result.setHandler(j -> {
            if (j.succeeded()) {
                routingContext.response().putHeader("content-type", "application/json")
                        .end(new JsonObject(Optional.ofNullable(j.result().getString("DATA")).orElse("{}"))
                                .toString());
            } else {
                j.cause().printStackTrace();
                notFound(routingContext);
            }
        });
        hsqlConfigService.get(ep, result.completer());
        /*

        Future<JsonObject> result = Future.future();
        result.setHandler(j -> {
            if (j.succeeded()) {
                routingContext.response().putHeader("content-type", "application/json")
                        .end(Optional.ofNullable(j.result().getJsonObject(routingContext.request().getParam("endpoint"))).orElse(new JsonObject())
                                .encodePrettily());
            } else {
                j.cause().printStackTrace();
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
         */

    }

    private void create(RoutingContext routingContext) {
        String comment = routingContext.request().getParam("comment");
        String endpoint = routingContext.request().getParam("endpoint");
        String data = routingContext.request().getParam("data");

        if (StringUtil.isEmpty(comment) || StringUtil.isEmpty(endpoint) || StringUtil.isEmpty(data)) {
            resultData(routingContext, new JsonObject().put("code", 1).put("msg", "参数不正确"));
            return;
        }

        hsqlConfigService.create(comment, endpoint, data, simpleResult(routingContext));
    }

    private void update(RoutingContext routingContext) {
        String uuid = routingContext.request().getParam("uuid");
        String comment = routingContext.request().getParam("comment");
        String endpoint = routingContext.request().getParam("endpoint");
        String data = routingContext.request().getParam("data");

        if (StringUtil.isEmpty(uuid) || StringUtil.isEmpty(comment) || StringUtil.isEmpty(endpoint) || StringUtil.isEmpty(data)) {
            resultData(routingContext, new JsonObject().put("code", 1).put("msg", "参数不正确"));
            return;
        }

        hsqlConfigService.update(uuid, comment, endpoint, data, simpleResult(routingContext));
    }

    private void del(RoutingContext routingContext) {
        String uuid = routingContext.request().getParam("uuid");

        if (StringUtil.isEmpty(uuid)) {
            resultData(routingContext, new JsonObject().put("code", 1).put("msg", "参数不正确"));
            return;
        }

        hsqlConfigService.del(uuid, simpleResult(routingContext));
    }

    private void list(RoutingContext routingContext) {
        hsqlConfigService.list(simpleResult(routingContext));
    }

    private <T> Handler<AsyncResult<T>> simpleResult(RoutingContext routingContext) {
        Future<T> result = Future.future();
        result.setHandler(r -> {
            if (r.succeeded()) {
                resultData(routingContext, r.result().toString());
            } else {
                r.cause().printStackTrace();
                notFound(routingContext);
            }
        });
        return result.completer();
    }


}
