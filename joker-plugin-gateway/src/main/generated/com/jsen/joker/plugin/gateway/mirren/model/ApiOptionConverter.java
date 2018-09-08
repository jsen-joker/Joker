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
 * Converter for {@link com.jsen.joker.plugin.gateway.mirren.model.ApiOption}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.gateway.mirren.model.ApiOption} original class using Vert.x codegen.
 */
public class ApiOptionConverter {

  public static void fromJson(JsonObject json, ApiOption obj) {
    if (json.getValue("apiOptionUrls") instanceof JsonArray) {
      java.util.LinkedHashSet<com.jsen.joker.plugin.gateway.mirren.model.ApiOptionUrl> list = new java.util.LinkedHashSet<>();
      json.getJsonArray("apiOptionUrls").forEach( item -> {
        if (item instanceof JsonObject)
          list.add(new com.jsen.joker.plugin.gateway.mirren.model.ApiOptionUrl((JsonObject)item));
      });
      obj.setApiOptionUrls(list);
    }
  }

  public static void toJson(ApiOption obj, JsonObject json) {
    if (obj.getApiOptionUrls() != null) {
      JsonArray array = new JsonArray();
      obj.getApiOptionUrls().forEach(item -> array.add(item.toJson()));
      json.put("apiOptionUrls", array);
    }
  }
}