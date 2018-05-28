package com.jsen.joker.plugin.login.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@DataObject(generateConverter = true)
public class SysUser extends EntityBase {


    private Integer id;
    private String name;
    private String password;
    private Integer sex;

    public SysUser(JsonObject jsonObject) {
        SysUserConverter.fromJson(jsonObject, this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        SysUserConverter.toJson(this, json);
        return json;
    }

    public Integer getId() {
        return id;
    }

    public SysUser setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SysUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SysUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getSex() {
        return sex;
    }

    public SysUser setSex(Integer sex) {
        this.sex = sex;
        return this;
    }
}
