package com.jsen.joker.plugin.gateway.mirren.model;

import com.google.common.collect.Sets;
import com.hazelcast.util.StringUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class Api {

    public enum ApiType {
        // http或https类型
        HTTP,
        // 页面跳转
        REDIRECT,
        // 自定义服务类型
        CUSTOM,
    }

    private String name;
    private String path;

    /**
     * 支持的方法
     */
    private Set<String> supportMethods;
    /**
     * 设置支持的请求数据类型
     */
    private Set<String> supportContentType;

    private ApiOptions apiOptions;
    private ApiType apiType = ApiType.HTTP;

    public static Api fromJson(JsonObject obj) {
        Api api = new Api();
        api.setName(obj.getString("name"));
        api.setPath(obj.getString("path"));
        Object arr = obj.getValue("supportMethods");
        if (arr instanceof JsonArray) {
            api.setSupportMethods(((JsonArray) arr).stream().map(Object::toString).collect(Collectors.toSet()));
        }
        arr = obj.getValue("supportContentType");
        if (arr instanceof JsonArray) {
            api.setSupportContentType(((JsonArray) arr).stream().map(Object::toString).collect(Collectors.toSet()));
        }
        api.setApiOptions(ApiOptions.fromJson(obj.getJsonObject("apiOptions")));
        api.setApiType(ApiType.valueOf(obj.getString("apiType", ApiType.HTTP.name())));
        return api;
    }

    public String getName() {
        return name;
    }

    public Api setName(String name) {
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

    public Api setPath(String path) {
        this.path = path;
        return this;
    }

    public Set<String> getSupportMethods() {
        if (supportMethods != null) {
            return supportMethods;
        }
        return Sets.newHashSet();
    }

    public Api setSupportMethods(Set<String> supportMethods) {
        this.supportMethods = supportMethods;
        return this;
    }

    public Set<String> getSupportContentType() {
        if (supportContentType != null) {
            return supportContentType;
        }
        return Sets.newHashSet();
    }

    public Api setSupportContentType(Set<String> supportContentType) {
        this.supportContentType = supportContentType;
        return this;
    }

    public ApiOptions getApiOptions() {
        return apiOptions;
    }

    public Api setApiOptions(ApiOptions apiOptions) {
        this.apiOptions = apiOptions;
        return this;
    }

    public ApiType getApiType() {
        return apiType;
    }

    public Api setApiType(ApiType apiType) {
        this.apiType = apiType;
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return Objects.equals(name, ((Api)obj).name);
    }
}
