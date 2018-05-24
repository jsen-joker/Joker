package com.jsen.test.common.service.impl;

import com.jsen.test.common.service.Echo2Service;
import com.jsen.test.common.service.ServiceBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/2
 */
public class Echo2ServiceImpl extends ServiceBase implements Echo2Service {
    public Echo2ServiceImpl() {
        super(Echo2Service.serviceId);
    }

    @Override
    public void echo(String key, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture("hello 2 " + key));
    }
}
