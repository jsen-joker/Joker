package com.jsen.joker.core.plugin.manager.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * <p>
 *     Jar 包 关系解析
 * </p>
 *
 * @author jsen
 * @since 2018/5/17
 */
public class EntryStructureAnalyse {
    public static JsonObject parser() {
        /*

        JsonObject result = new JsonObject();

        JsonArray root = new JsonArray();

        JsonArray modules = core.getJsonArray("modules");

        List<JsonObject> fs = RefreshConfig.listJarPaths();
        for (JsonObject file: fs) {
            String groupId = file.getString("groupId");
            String artifactId = file.getString("artifactId");
            String version = file.getString("version");
            JsonArray jsonArray = new JsonArray();
            try(JarFile jarFile = new JarFile(file.getString("path"))) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith("pom.xml")) {
                        try(InputStream inputStream = jarFile.getInputStream(entry)) {
                            JsonObject json = XmlToJson.xml2JSON(inputStream);
                            ps(modules, jsonArray, json);
                        } catch (JDOMException e) {
                            e.printStackTrace();
                        }
                    }
                }
                result.put(artifactId, new JsonObject().put("dep", jsonArray).put("groupId", groupId).put("artifactId", artifactId).put("version", version));
                root.add(new JsonObject().put("groupId", groupId).put("artifactId", artifactId).put("version", version));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        // result.put("root", new JsonObject().put("dep", root).put("groupId", "root").put("artifactId", "root").put("version", "root"));
        return result;
        */
        return new JsonObject();
    }

    private static void ps(JsonArray modules, JsonArray result, JsonObject json) {
        JsonObject data = json.getJsonObject("project");
        if (data.containsKey("dependencies") && data.getJsonArray("dependencies").size() > 0) {
            JsonArray dependencies = data.getJsonArray("dependencies").getJsonObject(0).getJsonArray("dependency");

            for (int i = 0; i < dependencies.size(); i++) {
                JsonObject dependency = dependencies.getJsonObject(i);
                JsonObject item = new JsonObject()
                        .put("groupId", dependency.getJsonArray("groupId").getString(0))
                        .put("artifactId", dependency.getJsonArray("artifactId").getString(0))
                        .put("version", dependency.getJsonArray("version").getString(0));
                if (contain(modules, item)) {
                    result.add(item);
                }
            }
        }
    }
    private static boolean contain(JsonArray modules, JsonObject item) {
        for (int i = 0; i < modules.size(); i++) {
            JsonObject it = modules.getJsonObject(i);
            if (
                    it.getString("groupId").equals(item.getString("groupId")) &&
                    it.getString("artifactId").equals(item.getString("artifactId"))
                    ) {
                return true;
            }
        }
        return false;
    }

}
