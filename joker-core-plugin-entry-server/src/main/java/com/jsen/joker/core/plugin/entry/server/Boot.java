package com.jsen.joker.core.plugin.entry.server;

import com.jsen.joker.core.plugin.entry.server.utils.GenMaven;
import com.jsen.joker.core.plugin.entry.server.utils.MD5Util;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.joker.JokerStaticHandlerImpl;
import com.jsen.test.common.utils.PathHelp;
import com.jsen.test.common.utils.response.ResponseBase;
import com.jsen.joker.core.plugin.entry.server.service.ProjectServerService;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.Set;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/15
 */
public class Boot extends RestVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);
    public static final String e_db_url = "enter.server.db.url";
    public static final String e_db_class = "enter.server.db.driver_class";
    public static final String e_db_user = "enter.server.db.user";
    public static final String e_db_password = "enter.server.db.password";

    ProjectServerService projectServerService;
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

        projectServerService = ProjectServerService.create(vertx, config());
        // registerProxyService(ProjectServerService.class, projectServerService);

        // api dispatcher
        // 上传文件
        router.post("/upload").handler(this::upload);
        // 下载文件
        router.route("/download/:fileName").handler(this::download);
        router.route("/delete/:fileName").handler(this::delete);
        // 列出文件
        router.route("/list").handler(this::list);
        StaticHandler staticHandler = new JokerStaticHandlerImpl(this.getClass());
        router.route("/*").handler(staticHandler);
        discoverSelf(new JsonObject().put("app.name", "EntryJar包服务器")
                .put("http.host", config().getString("enter.server.host", "localhost"))
                .put("endpoint", "entryserver")
                .put("http.port", config().getInteger("enter.server.port", 9092)));

        if (config().containsKey("enter.server.host")) {
            startServer(startFuture, config().getInteger("enter.server.port", 9092), config().getString("enter.server.host", "localhost"));
        } else {
            startServer(startFuture, config().getInteger("enter.server.port", 9092));
        }

    }


    private void upload(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "application/json");
        routingContext.response().setChunked(true);

        Set<FileUpload> fileUploads = routingContext.fileUploads();

        if (fileUploads.size() != 1) {
            resultData(routingContext, ResponseBase.create().code(1).msg("仅支持单个文件上传"));
            return;
        }
        for (FileUpload fU: fileUploads) {
            String fileName = fU.fileName();

            if (fileName.endsWith(".jar")) {
                JsonObject pom = GenMaven.parser(fU.uploadedFileName());
                if (!pom.containsKey("groupId")) {
                    resultData(routingContext, ResponseBase.create().code(1).msg("无法获取POM文件groupId"));
                    return;
                }
                if (!pom.containsKey("artifactId")) {
                    resultData(routingContext, ResponseBase.create().code(1).msg("无法获取POM文件artifactId"));
                    return;
                }
                if (!pom.containsKey("version")) {
                    resultData(routingContext, ResponseBase.create().code(1).msg("无法获取POM文件version"));
                    return;
                }
                vertx.fileSystem().readFile(fU.uploadedFileName(), ar -> {
                    if (ar.succeeded()) {
                        String data = Base64.getEncoder().encodeToString(ar.result().getBytes());
                        try {
                            String md5 = MD5Util.md5HashCode(fU.uploadedFileName());
                            JsonArray entries = pom.getJsonArray("entries", new JsonArray());
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < entries.size(); i++) {
                                JsonObject obj = entries.getJsonObject(i);
                                builder.append(obj.getString("value", "")).append(" ");
                            }
                            projectServerService.saveFile(pom.getString("artifactId"),
                                    pom.getString("groupId"),
                                    pom.getString("version"), data, builder.toString(), fileName, md5, (jsonObjectAsyncResult) -> {
                                        if(!jsonObjectAsyncResult.succeeded()) {
                                            resultData(routingContext, ResponseBase.create().code(1).msg("保存文件出错"));
                                        } else {
                                            resultData(routingContext, jsonObjectAsyncResult.result());
                                        }
                                    });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            resultData(routingContext, ResponseBase.create().code(1).msg("MD5生成出错"));
                        }
                    } else {
                        resultData(routingContext, ResponseBase.create().code(1).msg("读取文件出错"));
                    }
                });
            } else if(fileName.endsWith(".js")) {
                vertx.fileSystem().readFile(fU.uploadedFileName(), ar -> {
                    if (ar.succeeded()) {
                        String data = Base64.getEncoder().encodeToString(ar.result().getBytes());
                        try {
                            String md5 = MD5Util.md5HashCode(fU.uploadedFileName());
                            projectServerService.saveFile("", "", "", data, "", fileName, md5, (jsonObjectAsyncResult) -> {
                                if(!jsonObjectAsyncResult.succeeded()) {
                                    resultData(routingContext, ResponseBase.create().code(1).msg("保存文件出错"));
                                } else {
                                    resultData(routingContext, jsonObjectAsyncResult.result());
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            resultData(routingContext, ResponseBase.create().code(1).msg("MD5生成出错"));
                        }
                    } else {
                        resultData(routingContext, ResponseBase.create().code(1).msg("读取文件出错"));
                    }
                });
            } else {
                resultData(routingContext, ResponseBase.create().code(1).msg("只支持jar和js文件的上传"));
            }

            break;
        }
    }

    private void download(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String fileName = request.getParam("fileName");
        routingContext.response().setChunked(true);

        HttpServerResponse res = routingContext.response();

        res.putHeader("content-type", "application/octet-stream");

        projectServerService.download(fileName, ar -> {
            if (ar.succeeded()) {
                JsonObject datas = ar.result();
                res.putHeader("Content-Disposition", "attachment;filename=" + datas.getString("name"));
                String data = datas.getString("DATA", datas.getString("data"));
                res.end(Buffer.buffer(Base64.getDecoder().decode(data)));
            } else {
                notFound(routingContext);
            }
        });

    }

    private void delete(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String fileName = request.getParam("fileName");

        projectServerService.delete(fileName, ar -> {
            if (ar.succeeded()) {
                resultData(routingContext, ar.result());
            } else {
                notFound(routingContext);
            }
        });

    }

    private void list(RoutingContext routingContext) {
        projectServerService.listAll(ar -> {
            if (ar.succeeded()) {
                resultData(routingContext, ResponseBase.create().code(0).data(ar.result()));
            } else {
                notFound(routingContext);
            }
        });
    }

    public static void main(String[] args) {

        VertxOptions vO = new VertxOptions();
        vO.setEventLoopPoolSize(16);
        Vertx vertx = Vertx.vertx(vO);

        RxHelper.deployVerticle(vertx, new Boot()).subscribe(logger::info,
                e -> logger.error(e.getMessage()));




    }
}
