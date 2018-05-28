package com.jsen.joker.plugin.login.entity;

import io.vertx.core.json.JsonObject;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public abstract class EntityBase {
    @Override
    public String toString() {
        return toJson().encodePrettily();
    }

    public abstract JsonObject toJson();
}
