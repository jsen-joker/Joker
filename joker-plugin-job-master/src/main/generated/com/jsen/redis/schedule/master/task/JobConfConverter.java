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

package com.jsen.redis.schedule.master.task;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link com.jsen.redis.schedule.master.task.JobConf}.
 *
 * NOTE: This class has been automatically generated from the {@link com.jsen.redis.schedule.master.task.JobConf} original class using Vert.x codegen.
 */
public class JobConfConverter {

  public static void fromJson(JsonObject json, JobConf obj) {
    if (json.getValue("cron") instanceof String) {
      obj.setCron((String)json.getValue("cron"));
    }
    if (json.getValue("index") instanceof Number) {
      obj.setIndex(((Number)json.getValue("index")).longValue());
    }
    if (json.getValue("jobData") instanceof String) {
      obj.setJobData((String)json.getValue("jobData"));
    }
    if (json.getValue("staticJob") instanceof Boolean) {
      obj.setStaticJob((Boolean)json.getValue("staticJob"));
    }
    if (json.getValue("taskID") instanceof String) {
      obj.setTaskID((String)json.getValue("taskID"));
    }
  }

  public static void toJson(JobConf obj, JsonObject json) {
    if (obj.getCron() != null) {
      json.put("cron", obj.getCron());
    }
    if (obj.getIndex() != null) {
      json.put("index", obj.getIndex());
    }
    if (obj.getJobData() != null) {
      json.put("jobData", obj.getJobData());
    }
    json.put("staticJob", obj.isStaticJob());
    if (obj.getTaskID() != null) {
      json.put("taskID", obj.getTaskID());
    }
  }
}