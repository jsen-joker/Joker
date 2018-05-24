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

package com.jsen.test.common.service.reactivex;

import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.vertx.reactivex.core.Vertx;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * <p>
 * </p>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link com.jsen.test.common.service.Echo2Service original} non RX-ified interface using Vert.x codegen.
 */

@io.vertx.lang.reactivex.RxGen(com.jsen.test.common.service.Echo2Service.class)
public class Echo2Service {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Echo2Service that = (Echo2Service) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final io.vertx.lang.reactivex.TypeArg<Echo2Service> __TYPE_ARG = new io.vertx.lang.reactivex.TypeArg<>(
    obj -> new Echo2Service((com.jsen.test.common.service.Echo2Service) obj),
    Echo2Service::getDelegate
  );

  private final com.jsen.test.common.service.Echo2Service delegate;
  
  public Echo2Service(com.jsen.test.common.service.Echo2Service delegate) {
    this.delegate = delegate;
  }

  public com.jsen.test.common.service.Echo2Service getDelegate() {
    return delegate;
  }

  public static Echo2Service create() { 
    Echo2Service ret = Echo2Service.newInstance(com.jsen.test.common.service.Echo2Service.create());
    return ret;
  }

  public static Echo2Service createProxy(Vertx vertx) { 
    Echo2Service ret = Echo2Service.newInstance(com.jsen.test.common.service.Echo2Service.createProxy(vertx.getDelegate()));
    return ret;
  }

  public void echo(String key, Handler<AsyncResult<String>> resultHandler) { 
    delegate.echo(key, resultHandler);
  }

  public Single<String> rxEcho(String key) { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<String>(handler -> {
      echo(key, handler);
    });
  }


  public static  Echo2Service newInstance(com.jsen.test.common.service.Echo2Service arg) {
    return arg != null ? new Echo2Service(arg) : null;
  }
}
