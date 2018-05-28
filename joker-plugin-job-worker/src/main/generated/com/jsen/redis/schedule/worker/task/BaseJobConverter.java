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

package com.jsen.redis.schedule.worker.task;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.jsen.redis.schedule.worker.task.BaseJob}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.redis.schedule.worker.task.BaseJob} original class using Vert.x codegen.
 */
public class BaseJobConverter {

  public static void fromJson(JsonObject json, BaseJob obj) {
    if (json.getValue("id") instanceof String) {
      obj.setId((String)json.getValue("id"));
    }
    if (json.getValue("jobType") instanceof String) {
      obj.setJobType((String)json.getValue("jobType"));
    }
    if (json.getValue("singleJob") instanceof Boolean) {
      obj.setSingleJob((Boolean)json.getValue("singleJob"));
    }
  }

  public static void toJson(BaseJob obj, JsonObject json) {
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getJobType() != null) {
      json.put("jobType", obj.getJobType());
    }
    json.put("singleJob", obj.isSingleJob());
  }
}