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

package com.jsen.joker.core.plugin.entry.server.service.rxjava;

import java.util.Map;
import rx.Observable;
import rx.Single;
import java.util.List;
import io.vertx.rxjava.core.Vertx;
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

@io.vertx.lang.rxjava.RxGen(com.jsen.joker.core.plugin.entry.server.service.ProjectServerService.class)
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

  public static final io.vertx.lang.rxjava.TypeArg<ProjectServerService> __TYPE_ARG = new io.vertx.lang.rxjava.TypeArg<>(
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
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      saveFile(artifactId, groupId, version, data, className, name, uuid, fut);
    }));
  }

  public void download(String fileName, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.download(fileName, resultHandler);
  }

  public Single<JsonObject> rxDownload(String fileName) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      download(fileName, fut);
    }));
  }

  public void delete(String fileName, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.delete(fileName, resultHandler);
  }

  public Single<JsonObject> rxDelete(String fileName) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      delete(fileName, fut);
    }));
  }

  public void listAll(Handler<AsyncResult<List<JsonObject>>> resultHandler) { 
    delegate.listAll(resultHandler);
  }

  public Single<List<JsonObject>> rxListAll() { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      listAll(fut);
    }));
  }


  public static  ProjectServerService newInstance(com.jsen.joker.core.plugin.entry.server.service.ProjectServerService arg) {
    return arg != null ? new ProjectServerService(arg) : null;
  }
}
