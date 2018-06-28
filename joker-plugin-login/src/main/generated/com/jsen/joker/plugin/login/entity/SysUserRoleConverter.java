/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.jsen.joker.plugin.login.entity;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.jsen.joker.plugin.login.entity.SysUserRole}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.login.entity.SysUserRole} original class using Vert.x codegen.
 */
public class SysUserRoleConverter {

  public static void fromJson(JsonObject json, SysUserRole obj) {
    if (json.getValue("id") instanceof Number) {
      obj.setId(((Number)json.getValue("id")).intValue());
    }
    if (json.getValue("roleId") instanceof Number) {
      obj.setRoleId(((Number)json.getValue("roleId")).intValue());
    }
    if (json.getValue("userId") instanceof Number) {
      obj.setUserId(((Number)json.getValue("userId")).intValue());
    }
  }

  public static void toJson(SysUserRole obj, JsonObject json) {
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getRoleId() != null) {
      json.put("roleId", obj.getRoleId());
    }
    if (obj.getUserId() != null) {
      json.put("userId", obj.getUserId());
    }
  }
}