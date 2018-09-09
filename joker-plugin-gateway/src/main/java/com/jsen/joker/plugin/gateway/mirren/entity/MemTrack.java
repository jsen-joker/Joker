package com.jsen.joker.plugin.gateway.mirren.entity;

import io.vertx.core.json.JsonObject;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/9
 */
public class MemTrack extends JsonObject {

    public MemTrack appName(String appName) {
        put("appName", appName);
        return this;
    }

    public MemTrack apiName(String apiName) {
        put("apiName", apiName);
        return this;
    }

    public MemTrack msg(String msg) {
        put("msg", msg);
        return this;
    }

    public MemTrack trace(StackTraceElement[] stackTrace) {
        put("trace", stackTrace);
        return this;
    }

    public String getAppName() {
        return getString("appName");
    }

    public String getApiName() {
        return getString("apiName");
    }

    public String getMsg() {
        return getString("msg");
    }

    public StackTraceElement[] getTrace() {
        return (StackTraceElement[]) getValue("trace");
    }
}
