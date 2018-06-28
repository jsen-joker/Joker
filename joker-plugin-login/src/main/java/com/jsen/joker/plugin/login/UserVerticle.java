package com.jsen.joker.plugin.login;

import com.jsen.joker.plugin.login.controller.GetVerifyController;
import com.jsen.joker.plugin.login.controller.UserController;
import com.jsen.joker.plugin.login.service.UserService;
import com.jsen.test.common.RestVerticle;
import com.jsen.test.common.config.ConfigRetrieverHelper;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *     使用redis作为验证码存储
 *     使用mysql作为用户数据存储
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public class UserVerticle extends RestVerticle {
    private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

    private UserService userService;
    private RedisClient redisClient;


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        new ConfigRetrieverHelper() // TODO: enhance its usage
                .withHttpStore(cH, cP, "/config/login")
                .rxCreateConfig(io.vertx.reactivex.core.Vertx.newInstance(vertx)).doOnError(startFuture::fail).subscribe(config -> {

            discoverSelf(config);

            userService = UserService.create(vertx, config);
            RedisOptions redisOptions = new RedisOptions(config);
            redisClient = RedisClient.create(vertx, redisOptions);



            // registerProxyService(UserService.class, userService);

            // api dispatcher
            new UserController(router, userService, redisClient);
            new GetVerifyController(router, redisClient);

            startServer(startFuture);
        });

    }

    /**
     * Stop the verticle.<p>
     * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
     * If your verticle does things in its shut-down which take some time then you can override this method
     * and call the stopFuture some time later when clean-up is complete.
     *
     * @param stopFuture a future which should be called when verticle clean-up is complete.
     * @throws Exception
     */
    @Override
    public void stop(Future<Void> stopFuture) {
        redisClient.close(stopFuture.completer());
    }
}