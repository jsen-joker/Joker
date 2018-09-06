package com.jsen.joker.plugin.gateway.mirren.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * @ VxApiServerEntranceHttpOptions
 * </p>
 *
 *
 * @author jsen
 * @since 2018/9/6
 */
public class ApiOptions {
    private Set<ApiUrl> apiUrls;

    public Set<ApiUrl> getApiUrls() {
        return apiUrls;
    }

    public ApiOptions setApiUrls(Set<ApiUrl> apiUrls) {
        this.apiUrls = apiUrls;
        return this;
    }

    public static ApiOptions fromJson(JsonObject obj) {

        ApiOptions apiOptions = new ApiOptions();

        Object arr = obj.getValue("apiUrls");
        if (arr instanceof JsonArray) {
            apiOptions.setApiUrls(((JsonArray)arr).stream()
                    .filter(item -> item instanceof JsonObject)
                    .map(item -> ApiUrl.fromJson(((JsonObject) item))).collect(Collectors.toSet()));
        }
        return apiOptions;
    }
}
