package com.jsen.test.common.service;

import com.jsen.test.common.service.impl.Echo2ServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceProxyBuilder;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/2
 */
@VertxGen
@ProxyGen
public interface Echo2Service {
    public static final String serviceId = "service.echo2";
    // A couple of factory methods to create an instance and a proxy
    static Echo2Service create() {
        return new Echo2ServiceImpl();
    }

    static Echo2Service createProxy(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(serviceId);

        return builder.build(Echo2Service.class);
        // return ProxyHelper.createProxy(EchoService.class, vertx, address);
        // Alternatively, you can create the proxy directly using:
        // return new ProcessorServiceVertxEBProxy(vertx, address);
        // The name of the class to instantiate is the service interface + `VertxEBProxy`.
        // This class is generated during the compilation
    }


    void echo(String key, Handler<AsyncResult<String>> resultHandler);
}
