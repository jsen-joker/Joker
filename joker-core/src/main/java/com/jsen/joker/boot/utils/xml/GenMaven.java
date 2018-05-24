package com.jsen.joker.boot.utils.xml;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 *     获取Maven group 等信息
 *     获取vertx-boot-class
 * </p>
 *
 * @author jsen
 * @since 2018/5/19
 */
public class GenMaven {

    public static JsonObject parser(String filePath) {
        JsonObject jsonObject = new JsonObject();
        try(JarFile jarFile = new JarFile(filePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith("pom.xml")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    try(InputStream inputStream = jarFile.getInputStream(entry)/*BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)))*/) {
                        /*while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }*/
                        JsonObject json = XmlToJson.xml2JSON(inputStream);
                        ps(jsonObject, json);
                    } catch (JDOMException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static void ps(JsonObject result, JsonObject json) {
        JsonObject data = json.getJsonObject("project");
        if (data.containsKey("parent")) {
            JsonObject parent = data.getJsonArray("parent").getJsonObject(0);
            result.put("groupId", parent.getJsonArray("groupId").getJsonObject(0).getValue("value"));
            result.put("artifactId", parent.getJsonArray("artifactId").getJsonObject(0).getValue("value"));
            result.put("version", parent.getJsonArray("version").getJsonObject(0).getValue("value"));
        }
        if (data.containsKey("groupId")) {
            result.put("groupId", data.getJsonArray("groupId").getJsonObject(0).getValue("value"));
        }
        if (data.containsKey("artifactId")) {
            result.put("artifactId", data.getJsonArray("artifactId").getJsonObject(0).getValue("value"));
        }
        if (data.containsKey("version")) {
            result.put("version", data.getJsonArray("version").getJsonObject(0).getValue("value"));
        }
        if (data.containsKey("properties")) {
            JsonArray properties = data.getJsonArray("properties");
            for (int i = 0; i < properties.size(); i++) {
                JsonObject object = properties.getJsonObject(i);
                if (object.containsKey("vertx-boot-class")) {
                    result.put("entries", object.getJsonArray("vertx-boot-class"));
                }
            }
        }
    }

}
