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

package com.jsen.joker.plugin.login.service.reactivex;

import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import java.util.List;
import com.jsen.joker.plugin.login.entity.SysUser;
import io.vertx.reactivex.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * <p>
 * </p>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link com.jsen.joker.plugin.login.service.UserService original} non RX-ified interface using Vert.x codegen.
 */

@io.vertx.lang.reactivex.RxGen(com.jsen.joker.plugin.login.service.UserService.class)
public class UserService {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserService that = (UserService) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final io.vertx.lang.reactivex.TypeArg<UserService> __TYPE_ARG = new io.vertx.lang.reactivex.TypeArg<>(
    obj -> new UserService((com.jsen.joker.plugin.login.service.UserService) obj),
    UserService::getDelegate
  );

  private final com.jsen.joker.plugin.login.service.UserService delegate;
  
  public UserService(com.jsen.joker.plugin.login.service.UserService delegate) {
    this.delegate = delegate;
  }

  public com.jsen.joker.plugin.login.service.UserService getDelegate() {
    return delegate;
  }

  public static UserService create(Vertx vertx, JsonObject config) { 
    UserService ret = UserService.newInstance(com.jsen.joker.plugin.login.service.UserService.create(vertx.getDelegate(), config));
    return ret;
  }

  public static UserService createProxy(Vertx vertx) { 
    UserService ret = UserService.newInstance(com.jsen.joker.plugin.login.service.UserService.createProxy(vertx.getDelegate()));
    return ret;
  }

  public void createUser(String name, String password, String sex, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.createUser(name, password, sex, resultHandler);
  }

  public Single<JsonObject> rxCreateUser(String name, String password, String sex) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      createUser(name, password, sex, handler);
    });
  }

  public void deleteByName(String name, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.deleteByName(name, resultHandler);
  }

  public Single<JsonObject> rxDeleteByName(String name) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      deleteByName(name, handler);
    });
  }

  public void deleteById(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.deleteById(id, resultHandler);
  }

  public Single<JsonObject> rxDeleteById(Integer id) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      deleteById(id, handler);
    });
  }

  public void listUser(int page, int capacity, Handler<AsyncResult<List<SysUser>>> resultHandler) { 
    delegate.listUser(page, capacity, resultHandler);
  }

  public Single<List<SysUser>> rxListUser(int page, int capacity) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<List<SysUser>>(handler -> {
      listUser(page, capacity, handler);
    });
  }

  public void getUserByName(String name, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.getUserByName(name, resultHandler);
  }

  public Single<JsonObject> rxGetUserByName(String name) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      getUserByName(name, handler);
    });
  }

  public void getUserByID(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.getUserByID(id, resultHandler);
  }

  public Single<JsonObject> rxGetUserByID(Integer id) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      getUserByID(id, handler);
    });
  }

  public void login(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.login(username, password, resultHandler);
  }

  public Single<JsonObject> rxLogin(String username, String password) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonObject>(handler -> {
      login(username, password, handler);
    });
  }


  public static  UserService newInstance(com.jsen.joker.plugin.login.service.UserService arg) {
    return arg != null ? new UserService(arg) : null;
  }
}
