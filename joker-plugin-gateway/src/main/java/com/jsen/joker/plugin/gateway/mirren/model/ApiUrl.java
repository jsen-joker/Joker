package com.jsen.joker.plugin.gateway.mirren.model;

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
public class ApiUrl {
    private String url;

    public static ApiUrl fromJson(JsonObject obj) {
        return new ApiUrl().setUrl(obj.getString("url"));
    }

    public String getUrl() {
        return url;
    }

    public ApiUrl setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return Objects.equals(url, ((ApiUrl) obj).url);
    }
}
