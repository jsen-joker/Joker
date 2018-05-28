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
public class SysUserRole extends EntityBase {

    private Integer id;
    private Integer userId;
    private Integer roleId;



    public SysUserRole() {
        // Empty constructor
    }

    public SysUserRole(JsonObject json) {
        SysUserRoleConverter.fromJson(json, this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        SysUserRoleConverter.toJson(this, json);
        return json;
    }

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }

    public Integer getId() {
        return id;
    }

    public SysUserRole setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public SysUserRole setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public SysUserRole setRoleId(Integer roleId) {
        this.roleId = roleId;
        return this;
    }
}