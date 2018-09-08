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
 * Converter for {@link com.jsen.joker.plugin.gateway.mirren.model.Api}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.gateway.mirren.model.Api} original class using Vert.x codegen.
 */
public class ApiConverter {

  public static void fromJson(JsonObject json, Api obj) {
    if (json.getValue("apiOption") instanceof JsonObject) {
      obj.setApiOption(new com.jsen.joker.plugin.gateway.mirren.model.ApiOption((JsonObject)json.getValue("apiOption")));
    }
    if (json.getValue("apiType") instanceof String) {
      obj.setApiType(com.jsen.joker.plugin.gateway.mirren.model.Api.ApiType.valueOf((String)json.getValue("apiType")));
    }
    if (json.getValue("name") instanceof String) {
      obj.setName((String)json.getValue("name"));
    }
    if (json.getValue("on") instanceof Boolean) {
      obj.setOn((Boolean)json.getValue("on"));
    }
    if (json.getValue("path") instanceof String) {
      obj.setPath((String)json.getValue("path"));
    }
    if (json.getValue("remark") instanceof String) {
      obj.setRemark((String)json.getValue("remark"));
    }
    if (json.getValue("supportContentType") instanceof JsonArray) {
      java.util.LinkedHashSet<java.lang.String> list = new java.util.LinkedHashSet<>();
      json.getJsonArray("supportContentType").forEach( item -> {
        if (item instanceof String)
          list.add((String)item);
      });
      obj.setSupportContentType(list);
    }
    if (json.getValue("supportMethods") instanceof JsonArray) {
      java.util.LinkedHashSet<java.lang.String> list = new java.util.LinkedHashSet<>();
      json.getJsonArray("supportMethods").forEach( item -> {
        if (item instanceof String)
          list.add((String)item);
      });
      obj.setSupportMethods(list);
    }
  }

  public static void toJson(Api obj, JsonObject json) {
    if (obj.getApiOption() != null) {
      json.put("apiOption", obj.getApiOption().toJson());
    }
    if (obj.getApiType() != null) {
      json.put("apiType", obj.getApiType().name());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    json.put("on", obj.isOn());
    if (obj.getPath() != null) {
      json.put("path", obj.getPath());
    }
    if (obj.getRemark() != null) {
      json.put("remark", obj.getRemark());
    }
    if (obj.getSupportContentType() != null) {
      JsonArray array = new JsonArray();
      obj.getSupportContentType().forEach(item -> array.add(item));
      json.put("supportContentType", array);
    }
    if (obj.getSupportMethods() != null) {
      JsonArray array = new JsonArray();
      obj.getSupportMethods().forEach(item -> array.add(item));
      json.put("supportMethods", array);
    }
  }
}