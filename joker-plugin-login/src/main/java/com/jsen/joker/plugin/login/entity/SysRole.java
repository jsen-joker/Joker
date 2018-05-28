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
public class SysRole extends EntityBase {

    private Integer id;
    private String value;
    private Integer state;


    public SysRole() {
        // Empty constructor
    }

    public SysRole(JsonObject json) {
        SysRoleConverter.fromJson(json, this);
    }
    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        SysRoleConverter.toJson(this, json);
        return json;
    }

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }

    public Integer getId() {
        return id;
    }

    public SysRole setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getValue() {
        return value;
    }

    public SysRole setValue(String value) {
        this.value = value;
        return this;
    }

    public Integer getState() {
        return state;
    }

    public SysRole setState(Integer state) {
        this.state = state;
        return this;
    }

}