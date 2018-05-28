package com.jsen.joker.plugin.config.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/27
 */
@DataObject(generateConverter = true)
public class Config {
    private String comment;
    private String endpoint;
    private String data;
    private long create_time;
    private long update_time;


    public Config(JsonObject json) {
        ConfigConverter.fromJson(json, this);
    }
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        ConfigConverter.toJson(this, json);
        return json;
    }

    public String getComment() {
        return comment;
    }

    public Config setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getData() {
        return data;
    }

    public Config setData(String data) {
        this.data = data;
        return this;
    }

    public long getCreate_time() {
        return create_time;
    }

    public Config setCreate_time(long create_time) {
        this.create_time = create_time;
        return this;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public Config setUpdate_time(long update_time) {
        this.update_time = update_time;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Config setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
}
