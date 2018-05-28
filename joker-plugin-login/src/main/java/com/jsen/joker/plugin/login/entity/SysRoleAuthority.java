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
public class SysRoleAuthority extends EntityBase {
    private Integer id;
    private Integer roleId;
    private Integer authorityId;



    public SysRoleAuthority() {
        // Empty constructor
    }

    public SysRoleAuthority(JsonObject json) {
        SysRoleAuthorityConverter.fromJson(json, this);
    }
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        SysRoleAuthorityConverter.toJson(this, json);
        return json;
    }

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }

    public Integer getId() {
        return id;
    }

    public SysRoleAuthority setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public SysRoleAuthority setRoleId(Integer roleId) {
        this.roleId = roleId;
        return this;
    }

    public Integer getAuthorityId() {
        return authorityId;
    }

    public SysRoleAuthority setAuthorityId(Integer authorityId) {
        this.authorityId = authorityId;
        return this;
    }
}