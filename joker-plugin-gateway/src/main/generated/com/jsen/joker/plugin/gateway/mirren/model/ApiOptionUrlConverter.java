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
 * Converter for {@link com.jsen.joker.plugin.gateway.mirren.model.ApiOptionUrl}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.gateway.mirren.model.ApiOptionUrl} original class using Vert.x codegen.
 */
public class ApiOptionUrlConverter {

  public static void fromJson(JsonObject json, ApiOptionUrl obj) {
    if (json.getValue("url") instanceof String) {
      obj.setUrl((String)json.getValue("url"));
    }
  }

  public static void toJson(ApiOptionUrl obj, JsonObject json) {
    if (obj.getUrl() != null) {
      json.put("url", obj.getUrl());
    }
  }
}