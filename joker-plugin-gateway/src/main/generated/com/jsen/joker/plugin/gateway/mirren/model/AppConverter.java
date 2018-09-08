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

package com.jsen.joker.plugin.gateway.mirren.model;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.jsen.joker.plugin.gateway.mirren.model.App}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.gateway.mirren.model.App} original class using Vert.x codegen.
 */
public class AppConverter {

  public static void fromJson(JsonObject json, App obj) {
    if (json.getValue("apis") instanceof JsonArray) {
      java.util.LinkedHashSet<com.jsen.joker.plugin.gateway.mirren.model.Api> list = new java.util.LinkedHashSet<>();
      json.getJsonArray("apis").forEach( item -> {
        if (item instanceof JsonObject)
          list.add(new com.jsen.joker.plugin.gateway.mirren.model.Api((JsonObject)item));
      });
      obj.setApis(list);
    }
    if (json.getValue("createTime") instanceof Number) {
      obj.setCreateTime(((Number)json.getValue("createTime")).longValue());
    }
    if (json.getValue("host") instanceof String) {
      obj.setHost((String)json.getValue("host"));
    }
    if (json.getValue("name") instanceof String) {
      obj.setName((String)json.getValue("name"));
    }
    if (json.getValue("on") instanceof Boolean) {
      obj.setOn((Boolean)json.getValue("on"));
    }
    if (json.getValue("port") instanceof Number) {
      obj.setPort(((Number)json.getValue("port")).intValue());
    }
    if (json.getValue("remark") instanceof String) {
      obj.setRemark((String)json.getValue("remark"));
    }
    if (json.getValue("updateTime") instanceof Number) {
      obj.setUpdateTime(((Number)json.getValue("updateTime")).longValue());
    }
  }

  public static void toJson(App obj, JsonObject json) {
    if (obj.getApis() != null) {
      JsonArray array = new JsonArray();
      obj.getApis().forEach(item -> array.add(item.toJson()));
      json.put("apis", array);
    }
    json.put("createTime", obj.getCreateTime());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    json.put("on", obj.isOn());
    if (obj.getPort() != null) {
      json.put("port", obj.getPort());
    }
    if (obj.getRemark() != null) {
      json.put("remark", obj.getRemark());
    }
    json.put("updateTime", obj.getUpdateTime());
  }
}