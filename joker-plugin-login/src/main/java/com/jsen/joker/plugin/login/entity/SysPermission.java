package com.jsen.joker.plugin.login.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * <p>
 * 
 * </p>
 *
 * @author jsen
 * @since 2018-04-08
 */
@DataObject(generateConverter = true)
public class SysPermission extends EntityBase {

    private Integer id;
    private String permission;

    private String comment;

    private Integer type;
    private Integer order;


    public SysPermission() {
        // Empty constructor
    }

    public SysPermission(JsonObject json) {
        SysPermissionConverter.fromJson(json, this);
    }
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        SysPermissionConverter.toJson(this, json);
        return json;
    }

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }

    public Integer getId() {
        return id;
    }

    public SysPermission setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getPermission() {
        return permission;
    }

    public SysPermission setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public SysPermission setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public SysPermission setType(Integer type) {
        this.type = type;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    public SysPermission setOrder(Integer order) {
        this.order = order;
        return this;
    }
}