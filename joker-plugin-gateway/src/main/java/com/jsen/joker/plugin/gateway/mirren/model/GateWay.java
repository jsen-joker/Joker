package com.jsen.joker.plugin.gateway.mirren.model;

import com.hazelcast.util.StringUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *     代表一个gateway项目
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class GateWay {
    private String name;
    private String path;
    private Integer port = 8888;
    private String host = "localhost";
    private Set<Api> apis;


    public String getName() {
        return name;
    }

    public GateWay setName(String name) {
        this.name = name;
        return this;
    }

    public String getPath() {
        if (StringUtil.isNullOrEmpty(path)) {
            return "/" + name;
        } else {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (path.startsWith("/")) {
                return path;
            }
            return path;
        }
    }

    public GateWay setPath(String path) {
        this.path = path;
        return this;
    }

    public Set<Api> getApis() {
        return apis;
    }

    public GateWay setApis(Set<Api> apis) {
        this.apis = apis;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public GateWay setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public GateWay setHost(String host) {
        this.host = host;
        return this;
    }

    public static GateWay fromJson(JsonObject obj) {
        GateWay gateWay = new GateWay()
                .setName(obj.getString("name"))
                .setPort(obj.getInteger("port", 8888))
                .setHost(obj.getString("host", "localhost"))
                .setPath(obj.getString("path"));
        Object arr = obj.getValue("apis");
        if (arr instanceof JsonArray) {
            gateWay.setApis(((JsonArray) arr).stream().filter(item -> item instanceof JsonObject)
                    .map(item -> Api.fromJson(((JsonObject) item)))
                    .collect(Collectors.toSet()));
        }
        return gateWay;
    }
}
