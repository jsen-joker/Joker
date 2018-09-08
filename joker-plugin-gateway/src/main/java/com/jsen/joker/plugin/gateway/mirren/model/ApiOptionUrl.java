package com.jsen.joker.plugin.gateway.mirren.model;

import com.jsen.joker.plugin.login.entity.EntityBase;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * <p>
 *     一条 api 可能对应多条url
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
@DataObject(generateConverter = true)
public class ApiOptionUrl extends EntityBase {
    private String url;

    public String getUrl() {
        return url;
    }

    public ApiOptionUrl setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return Objects.equals(url, ((ApiOptionUrl) obj).url);
    }


    public ApiOptionUrl() {
    }

    public ApiOptionUrl(JsonObject json) {
        ApiOptionUrlConverter.fromJson(json, this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        ApiOptionUrlConverter.toJson(this, json);
        return json;
    }
}
