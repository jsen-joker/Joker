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
 * Converter for {@link com.jsen.joker.plugin.login.entity.SysPermission}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.login.entity.SysPermission} original class using Vert.x codegen.
 */
public class SysPermissionConverter {

  public static void fromJson(JsonObject json, SysPermission obj) {
    if (json.getValue("comment") instanceof String) {
      obj.setComment((String)json.getValue("comment"));
    }
    if (json.getValue("id") instanceof Number) {
      obj.setId(((Number)json.getValue("id")).intValue());
    }
    if (json.getValue("order") instanceof Number) {
      obj.setOrder(((Number)json.getValue("order")).intValue());
    }
    if (json.getValue("permission") instanceof String) {
      obj.setPermission((String)json.getValue("permission"));
    }
    if (json.getValue("type") instanceof Number) {
      obj.setType(((Number)json.getValue("type")).intValue());
    }
  }

  public static void toJson(SysPermission obj, JsonObject json) {
    if (obj.getComment() != null) {
      json.put("comment", obj.getComment());
    }
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getOrder() != null) {
      json.put("order", obj.getOrder());
    }
    if (obj.getPermission() != null) {
      json.put("permission", obj.getPermission());
    }
    if (obj.getType() != null) {
      json.put("type", obj.getType());
    }
  }
}