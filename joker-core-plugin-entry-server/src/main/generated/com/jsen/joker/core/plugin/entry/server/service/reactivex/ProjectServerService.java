/*
 * Copyright 2014 Red Hat, Inc.
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

package com.jsen.joker.core.plugin.entry.server.service.reactivex;

import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import java.util.List;
import io.vertx.reactivex.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * <p>
 * </p>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.core.plugin.entry.server.service.ProjectServerService original} non RX-ified interface using Vert.x codegen.
 */

@io.vertx.lang.reactivex.RxGen(com.jsen.joker.core.plugin.entry.server.service.ProjectServerService.class)
public class ProjectServerService {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProjectServerService that = (ProjectServerService) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final io.vertx.lang.reactivex.TypeArg<ProjectServerService> __TYPE_ARG = new io.vertx.lang.reactivex.TypeArg<>(
    obj -> new ProjectServerService((com.jsen.joker.core.plugin.entry.server.service.ProjectServerService) obj),
    ProjectServerService::getDelegate
  );

  private final com.jsen.joker.core.plugin.entry.server.service.ProjectServerService delegate;
  
  public ProjectServerService(com.jsen.joker.core.plugin.entry.server.service.ProjectServerService delegate) {
    this.delegate = delegate;
  }

  public com.jsen.joker.core.plugin.entry.server.service.ProjectServerService getDelegate() {
    return delegate;
  }

  public static ProjectServerService create(Vertx vertx, JsonObject config) { 
    ProjectServerService ret = ProjectServerService.newInstance(com.jsen.joker.core.plugin.entry.server.service.ProjectServerService.create(vertx.getDelegate(), config));
    return ret;
  }

  public static ProjectServerService createProxy(Vertx vertx) { 
    ProjectServerService ret = ProjectServerService.newInstance(com.jsen.joker.core.plugin.entry.server.service.ProjectServerService.createProxy(vertx.getDelegate()));
    return ret;
  }

  public void saveFile(String artifactId, String groupId, String version, String data, String className, String name, String uuid, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.saveFile(artifactId, groupId, version, data, className, name, uuid, resultHandler);
  }

  public Single<JsonObject> rxSaveFile(String artifactId, String groupId, String version, String data, String className, String name, String uuid) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      saveFile(artifactId, groupId, version, data, className, name, uuid, handler);
    });
  }

  public void download(String fileName, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.download(fileName, resultHandler);
  }

  public Single<JsonObject> rxDownload(String fileName) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      download(fileName, handler);
    });
  }

  public void delete(String fileName, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.delete(fileName, resultHandler);
  }

  public Single<JsonObject> rxDelete(String fileName) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      delete(fileName, handler);
    });
  }

  public void listAll(Handler<AsyncResult<List<JsonObject>>> resultHandler) { 
    delegate.listAll(resultHandler);
  }

  public Single<List<JsonObject>> rxListAll() { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<List<JsonObject>>(handler -> {
      listAll(handler);
    });
  }


  public static  ProjectServerService newInstance(com.jsen.joker.core.plugin.entry.server.service.ProjectServerService arg) {
    return arg != null ? new ProjectServerService(arg) : null;
  }
}
