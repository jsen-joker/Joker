package com.jsen.joker.boot.resloader;

import com.jsen.joker.boot.utils.PathHelp;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/12
 *
 * 更新verticles jar包路径
 * 更新verticles 启动类列表
 *
 * 可以在boot中加上request 请求进行动态更新
 */
@Deprecated
public class RefreshConfig {
    public static JsonObject conf = new JsonObject();
    public static JsonArray entries = new JsonArray();
    public static JsonArray modules = new JsonArray();
    public static String workDir = "";
    public static JsonObject core = new JsonObject();


    public static void refreshConfig(final Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> result = Future.future();
        result.setHandler(handler);

        String root = PathHelp.getProjectPath();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                new File(root + File.separator + "conf" + File.separator + "core.json")
        )))) {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
            JsonObject jsonObject = new JsonObject(data.toString());
            core = jsonObject;
            conf = jsonObject.getJsonObject("conf");
            workDir = conf.getString("application.dir");
            entries = jsonObject.getJsonArray("entries");
            modules = jsonObject.getJsonArray("modules");
            result.complete(true);
        } catch (Exception e) {
            result.fail(e);
        }

    }

    public static List<JsonObject> listJarPaths() {
        List<JsonObject> result = new ArrayList<>();
        String root = PathHelp.getProjectPath();

        JsonArray modules = RefreshConfig.modules;
        for (int i = 0; i < modules.size(); i++) {
            JsonObject module = modules.getJsonObject(i);
            String path = root + File.separator + "modules" + File.separator + module.getString("groupId").replaceAll("\\.", File.separator) +
                    File.separator + module.getString("artifactId") + File.separator + module.getString("version") + File.separator + "verticle.jar";


            JsonObject item = new JsonObject();
            item.put("groupId", module.getString("groupId"));
            item.put("artifactId", module.getString("artifactId"));
            item.put("version", module.getString("version"));
            item.put("path", path);

            result.add(item);
        }

        return result;
    }

    public static void updateModule(String groupId, String artifactId, String version, final Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> result = Future.future();
        result.setHandler(handler);

        readCoreConf(a -> {
            if (a.succeeded()) {
                JsonObject jsonObject = a.result();
                JsonArray modules = jsonObject.getJsonArray("modules");
                for (int i = 0; i < modules.size(); i++) {
                    JsonObject module = modules.getJsonObject(i);
                    if (groupId.equals(module.getString("groupId")) && artifactId.equals(module.getString("artifactId"))) {
                        module.put("version", version);
                        break;
                    }
                }
                saveCoreConf(jsonObject, result);
            } else {
                result.fail(a.cause());
            }
        });
    }


    public static void deleteModule(String groupId, String artifactId, String version, final Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> result = Future.future();
        result.setHandler(handler);

        readCoreConf(a -> {
            if (a.succeeded()) {
                JsonObject jsonObject = a.result();
                JsonArray modules = jsonObject.getJsonArray("modules");
                for (int i = 0; i < modules.size(); i++) {
                    JsonObject module = modules.getJsonObject(i);
                    if (groupId.equals(module.getString("groupId")) && artifactId.equals(module.getString("artifactId"))) {
                        modules.remove(i);
                        break;
                    }
                }
                saveCoreConf(jsonObject, result);
            } else {
                result.fail(a.cause());
            }
        });
    }

    public static Future<Boolean> addModule(String groupId, String artifactId, String version) {
        Future<Boolean> result = Future.future();

        readCoreConf(a -> {
            if (a.succeeded()) {
                JsonObject jsonObject = a.result();
                JsonArray modules = jsonObject.getJsonArray("modules");

                boolean exist = false;
                for (int i = 0; i < modules.size(); i++) {
                    JsonObject module = modules.getJsonObject(i);
                    if (groupId.equals(module.getString("groupId")) && artifactId.equals(module.getString("artifactId"))) {
                        exist = true;
                        break;
                    }
                }

                if (!exist) {
                    jsonObject.getJsonArray("modules").add(new JsonObject().put("groupId", groupId).put("artifactId", artifactId).put("version", version));
                    saveCoreConf(jsonObject, result);
                } else {
                    result.fail("模块已加载");
                }
            } else {
                result.fail(a.cause());
            }
        });
        return result;
    }

    public static void addVerticle(String className, final Handler<AsyncResult<Boolean>> handler) {
        Future<Boolean> result = Future.future();
        result.setHandler(handler);

        readCoreConf(a -> {
            if (a.succeeded()) {
                JsonObject jsonObject = a.result();
                JsonArray entries = jsonObject.getJsonArray("entries");
                boolean exist = false;
                for (int i = 0; i < entries.size(); i++) {
                    JsonObject enter = entries.getJsonObject(i);
                    if (enter.getString("enter").equals(className)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    entries.add(new JsonObject().put("enter", className));
                    saveCoreConf(jsonObject, result);
                } else {
                    result.fail("服务已加载");
                }
            } else {
                result.fail(a.cause());
            }
        });
    }

    public static void deleteVerticle(String className, final Handler<AsyncResult<Boolean>> handler) {

        Future<Boolean> result = Future.future();
        result.setHandler(handler);

        readCoreConf(a -> {
            if (a.succeeded()) {
                JsonObject jsonObject = a.result();
                JsonArray entries = jsonObject.getJsonArray("entries");
                for (int i = 0; i < entries.size(); i++) {
                    JsonObject enter = entries.getJsonObject(i);
                    if (className.equals(enter.getString("enter"))) {
                        entries.remove(i);
                        break;
                    }
                }
                saveCoreConf(jsonObject, result);
            } else {
                result.fail(a.cause());
            }
        });
    }

    private static void  readCoreConf(Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> result = Future.future();
        result.setHandler(handler);

        String root = PathHelp.getProjectPath();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                new File(root + File.separator + "conf" + File.separator + "core.json")
        )))) {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
            JsonObject jsonObject = new JsonObject(data.toString());

            result.complete(jsonObject);
        } catch (Exception e) {
            result.fail(e);
        }
    }

    private static void saveCoreConf(JsonObject core, Future<Boolean> result) {

        String root = PathHelp.getProjectPath();
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(root + File.separator + "conf" + File.separator + "core.json")
        )))) {
            writer.write(core.encodePrettily());
            RefreshConfig.core = core;
            conf = core.getJsonObject("conf");
            workDir = conf.getString("application.dir");
            entries = core.getJsonArray("entries");
            modules = core.getJsonArray("modules");

            result.complete(true);
        } catch (Exception e) {
            result.fail(e);
        }
    }
}
