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

package com.jsen.joker.plugin.login.service.rxjava;

import java.util.Map;
import rx.Observable;
import rx.Single;
import com.jsen.joker.plugin.login.entity.SysRole;
import java.util.List;
import com.jsen.joker.plugin.login.entity.SysUser;
import com.jsen.joker.plugin.login.entity.SysPermission;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * <p>
 * </p>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.login.service.ShiroService original} non RX-ified interface using Vert.x codegen.
 */

@io.vertx.lang.rxjava.RxGen(com.jsen.joker.plugin.login.service.ShiroService.class)
public class ShiroService {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ShiroService that = (ShiroService) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final io.vertx.lang.rxjava.TypeArg<ShiroService> __TYPE_ARG = new io.vertx.lang.rxjava.TypeArg<>(
    obj -> new ShiroService((com.jsen.joker.plugin.login.service.ShiroService) obj),
    ShiroService::getDelegate
  );

  private final com.jsen.joker.plugin.login.service.ShiroService delegate;
  
  public ShiroService(com.jsen.joker.plugin.login.service.ShiroService delegate) {
    this.delegate = delegate;
  }

  public com.jsen.joker.plugin.login.service.ShiroService getDelegate() {
    return delegate;
  }

  public static ShiroService create(Vertx vertx, JsonObject config) { 
    ShiroService ret = ShiroService.newInstance(com.jsen.joker.plugin.login.service.ShiroService.create(vertx.getDelegate(), config));
    return ret;
  }

  public static ShiroService createProxy(Vertx vertx) { 
    ShiroService ret = ShiroService.newInstance(com.jsen.joker.plugin.login.service.ShiroService.createProxy(vertx.getDelegate()));
    return ret;
  }

  public void getRoleByUserId(Integer id, Handler<AsyncResult<List<SysRole>>> resultHandler) { 
    delegate.getRoleByUserId(id, resultHandler);
  }

  public Single<List<SysRole>> rxGetRoleByUserId(Integer id) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      getRoleByUserId(id, fut);
    }));
  }

  public void getPermissionByRoleId(Integer id, Handler<AsyncResult<List<SysPermission>>> resultHandler) { 
    delegate.getPermissionByRoleId(id, resultHandler);
  }

  public Single<List<SysPermission>> rxGetPermissionByRoleId(Integer id) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      getPermissionByRoleId(id, fut);
    }));
  }

  public void createUser(String name, String password, String sex, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.createUser(name, password, sex, resultHandler);
  }

  public Single<JsonObject> rxCreateUser(String name, String password, String sex) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      createUser(name, password, sex, fut);
    }));
  }

  public void deleteByName(String name, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.deleteByName(name, resultHandler);
  }

  public Single<JsonObject> rxDeleteByName(String name) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      deleteByName(name, fut);
    }));
  }

  public void deleteById(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.deleteById(id, resultHandler);
  }

  public Single<JsonObject> rxDeleteById(Integer id) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      deleteById(id, fut);
    }));
  }

  public void listUser(int page, int capacity, Handler<AsyncResult<List<SysUser>>> resultHandler) { 
    delegate.listUser(page, capacity, resultHandler);
  }

  public Single<List<SysUser>> rxListUser(int page, int capacity) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      listUser(page, capacity, fut);
    }));
  }

  public void getUserByName(String name, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.getUserByName(name, resultHandler);
  }

  public Single<JsonObject> rxGetUserByName(String name) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      getUserByName(name, fut);
    }));
  }

  public void getUserByID(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.getUserByID(id, resultHandler);
  }

  public Single<JsonObject> rxGetUserByID(Integer id) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      getUserByID(id, fut);
    }));
  }

  public void login(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.login(username, password, resultHandler);
  }

  public Single<JsonObject> rxLogin(String username, String password) { 
    return Single.create(new io.vertx.rx.java.SingleOnSubscribeAdapter<>(fut -> {
      login(username, password, fut);
    }));
  }


  public static  ShiroService newInstance(com.jsen.joker.plugin.login.service.ShiroService arg) {
    return arg != null ? new ShiroService(arg) : null;
  }
}
