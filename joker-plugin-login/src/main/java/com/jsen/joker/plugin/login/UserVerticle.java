package com.jsen.joker.plugin.login;

import com.jsen.joker.plugin.login.service.UserService;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.config.ConfigRetrieverHelper;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public class UserVerticle extends RestVerticle {
    private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

    UserService userService;


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        new ConfigRetrieverHelper() // TODO: enhance its usage
                .withHttpStore("localhost", 9000, "/config/mysql1")
                .withHttpStore("localhost", 9000, "/config/user")
                .rxCreateConfig(io.vertx.reactivex.core.Vertx.newInstance(vertx)).doOnError(startFuture::fail).subscribe(config -> {

            discoverSelf(config);

            userService = UserService.create(vertx, config);


            registerProxyService(UserService.class, userService);

            // api dispatcher
            router.route("/login/:name/:password").handler(this::doLogin);
            startServer(startFuture);
        });

    }

    private void doLogin(RoutingContext routingContext) {
        String name = routingContext.request().getParam("name");
        String password = routingContext.request().getParam("password");
        HttpServerResponse response = routingContext.response();
        if (name == null || password == null) {
            sendError(400, response);
        } else {
            userService.login(name, password, resultHandlerData(routingContext));
        }
    }
}