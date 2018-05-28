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

package com.jsen.joker.boot.entity;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.jsen.joker.boot.entity.Entry}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.boot.entity.Entry} original class using Vert.x codegen.
 */
public class EntryConverter {

  public static void fromJson(JsonObject json, Entry obj) {
    if (json.getValue("deploymentID") instanceof String) {
      obj.setDeploymentID((String)json.getValue("deploymentID"));
    }
    if (json.getValue("state") instanceof String) {
      obj.setState(com.jsen.joker.boot.joker.context.EntryManager.STATE.valueOf((String)json.getValue("state")));
    }
  }

  public static void toJson(Entry obj, JsonObject json) {
    if (obj.getDeploymentID() != null) {
      json.put("deploymentID", obj.getDeploymentID());
    }
    if (obj.getDeploymentOptions() != null) {
      json.put("deploymentOptions", obj.getDeploymentOptions().toJson());
    }
    if (obj.getEntryClass() != null) {
      json.put("entryClass", obj.getEntryClass());
    }
    if (obj.getFilePath() != null) {
      json.put("filePath", obj.getFilePath());
    }
    json.put("priority", obj.getPriority());
    json.put("script", obj.isScript());
    if (obj.getState() != null) {
      json.put("state", obj.getState().name());
    }
  }
}