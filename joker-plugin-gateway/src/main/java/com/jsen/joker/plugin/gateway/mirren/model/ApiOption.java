package com.jsen.joker.plugin.gateway.mirren.model;

import com.google.common.collect.Sets;
import com.jsen.joker.plugin.login.entity.EntityBase;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Set;

/**
 * <p>
 * @ VxApiServerEntranceHttpOptions
 * </p>
 *
 *
 * @author jsen
 * @since 2018/9/6
 */
@DataObject(generateConverter = true)
public class ApiOption extends EntityBase {
    private Set<ApiOptionUrl> apiOptionUrls;

    public Set<ApiOptionUrl> getApiOptionUrls() {
        if (apiOptionUrls == null) {
            apiOptionUrls = Sets.newHashSet();
        }
        return apiOptionUrls;
    }

    public ApiOption setApiOptionUrls(Set<ApiOptionUrl> apiOptionUrls) {
        this.apiOptionUrls = apiOptionUrls;
        return this;
    }

    public ApiOption() {
    }

    public ApiOption(JsonObject json) {
        ApiOptionConverter.fromJson(json, this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        ApiOptionConverter.toJson(this, json);
        return json;
    }
}
