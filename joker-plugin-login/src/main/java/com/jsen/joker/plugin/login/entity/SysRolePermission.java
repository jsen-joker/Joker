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
public class SysRolePermission extends EntityBase {

    private Integer id;
    private Integer roleId;
    private Integer permissionId;



    public SysRolePermission() {
        // Empty constructor
    }

    public SysRolePermission(JsonObject json) {
        SysRolePermissionConverter.fromJson(json, this);
    }
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        SysRolePermissionConverter.toJson(this, json);
        return json;
    }

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }

    public Integer getId() {
        return id;
    }

    public SysRolePermission setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public SysRolePermission setRoleId(Integer roleId) {
        this.roleId = roleId;
        return this;
    }

    public Integer getPermissionId() {
        return permissionId;
    }

    public SysRolePermission setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
        return this;
    }
}