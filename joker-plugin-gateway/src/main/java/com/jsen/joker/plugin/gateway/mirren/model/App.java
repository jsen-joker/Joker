package com.jsen.joker.plugin.gateway.mirren.model;

import com.google.common.collect.Sets;
import com.jsen.joker.plugin.login.entity.EntityBase;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Set;

/**
 * <p>
 *     代表一个gateway项目
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */

@DataObject(generateConverter = true)
public class App extends EntityBase {
    private String name;
    private Integer port = 8888;
    private String host = "localhost";
    private Set<Api> apis;
    private boolean on;
    private String remark;

    private long createTime;
    private long updateTime;


    public String getName() {
        return name;
    }

    public App setName(String name) {
        this.name = name;
        return this;
    }

    public Set<Api> getApis() {
        if (apis == null) {
            apis = Sets.newHashSet();
        }
        return apis;
    }

    public App setApis(Set<Api> apis) {
        this.apis = apis;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public App setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public App setHost(String host) {
        this.host = host;
        return this;
    }

    public long getCreateTime() {
        return createTime;
    }

    public App setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public App setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public boolean isOn() {
        return on;
    }

    public App setOn(boolean on) {
        this.on = on;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public App setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public App() {
    }

    public App(JsonObject json) {
        AppConverter.fromJson(json, this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        AppConverter.toJson(this, json);
        return json;
    }

}
