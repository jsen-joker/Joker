package com.jsen.test.common.service.impl;

import com.jsen.test.common.service.EchoService;
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
public class EchoServiceImpl extends ServiceBase implements EchoService {
    public EchoServiceImpl() {
        super(EchoService.serviceID);
    }

    @Override
    public void echo(String key, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture("hello " + key));
    }
}
