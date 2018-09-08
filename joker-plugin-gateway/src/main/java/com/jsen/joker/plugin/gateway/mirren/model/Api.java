package com.jsen.joker.plugin.gateway.mirren.model;

import com.google.common.collect.Sets;
import com.jsen.joker.plugin.login.entity.EntityBase;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
@DataObject(generateConverter = true)
public class Api extends EntityBase {

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

    private ApiOption apiOption;
    private ApiType apiType = ApiType.HTTP;

    private boolean on = false;
    private String remark;

    public String getName() {
        return name;
    }

    public Api setName(String name) {
        this.name = name;
        return this;
    }

    public String getPath() {
        return Help.getPath(path, name);
    }

    public Api setPath(String path) {
        this.path = path;
        return this;
    }

    public Set<String> getSupportMethods() {
        if (supportMethods == null) {
            supportMethods = Sets.newHashSet();
        }
        return supportMethods;
    }

    public Api setSupportMethods(Set<String> supportMethods) {
        this.supportMethods = supportMethods;
        return this;
    }

    public Set<String> getSupportContentType() {
        if (supportContentType == null) {
            supportContentType = Sets.newHashSet();
        }
        return supportContentType;
    }

    public Api setSupportContentType(Set<String> supportContentType) {
        this.supportContentType = supportContentType;
        return this;
    }

    public ApiOption getApiOption() {
        return apiOption;
    }

    public Api setApiOption(ApiOption apiOption) {
        this.apiOption = apiOption;
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

    public boolean isOn() {
        return on;
    }

    public String getRemark() {
        return remark;
    }

    public Api setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public Api setOn(boolean on) {
        this.on = on;
        return this;
    }

    public Api() {
    }

    public Api(JsonObject json) {
        ApiConverter.fromJson(json, this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        ApiConverter.toJson(this, json);
        return json;
    }

}
