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

package com.jsen.joker.plugin.config.entity;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.jsen.joker.plugin.config.entity.Config}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.config.entity.Config} original class using Vert.x codegen.
 */
public class ConfigConverter {

  public static void fromJson(JsonObject json, Config obj) {
    if (json.getValue("comment") instanceof String) {
      obj.setComment((String)json.getValue("comment"));
    }
    if (json.getValue("create_time") instanceof Number) {
      obj.setCreate_time(((Number)json.getValue("create_time")).longValue());
    }
    if (json.getValue("data") instanceof String) {
      obj.setData((String)json.getValue("data"));
    }
    if (json.getValue("endpoint") instanceof String) {
      obj.setEndpoint((String)json.getValue("endpoint"));
    }
    if (json.getValue("update_time") instanceof Number) {
      obj.setUpdate_time(((Number)json.getValue("update_time")).longValue());
    }
  }

  public static void toJson(Config obj, JsonObject json) {
    if (obj.getComment() != null) {
      json.put("comment", obj.getComment());
    }
    json.put("create_time", obj.getCreate_time());
    if (obj.getData() != null) {
      json.put("data", obj.getData());
    }
    if (obj.getEndpoint() != null) {
      json.put("endpoint", obj.getEndpoint());
    }
    json.put("update_time", obj.getUpdate_time());
  }
}