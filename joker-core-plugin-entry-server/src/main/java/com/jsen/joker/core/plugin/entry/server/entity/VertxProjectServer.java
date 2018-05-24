package com.jsen.joker.core.plugin.entry.server.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * <p>
 * 
 * </p>
 *
 * @author jsen
 * @since 2018-04-08
 */
@DataObject(generateConverter = true)
public class VertxProjectServer {


    private String version;
    private String artifactId;
    private String groupId;
    private String data;
    private String uuid;

    public VertxProjectServer(JsonObject json) {
        VertxProjectServerConverter.fromJson(json, this);
    }
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        VertxProjectServerConverter.toJson(this, json);
        return json;
    }

    public String getVersion() {
        return version;
    }

    public VertxProjectServer setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public VertxProjectServer setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public VertxProjectServer setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getData() {
        return data;
    }

    public VertxProjectServer setData(String data) {
        this.data = data;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public VertxProjectServer setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
}
