package com.jsen.joker.plugin.login.controller;

import com.jsen.joker.plugin.login.service.UserService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;
import org.hsqldb.lib.StringUtil;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/31
 */
public class UserController extends BaseController {
    private UserService userService;
    private RedisClient redisClient;

    public UserController(Router router, UserService userService, RedisClient redisClient) {
        super(router);
        this.userService = userService;
        this.redisClient = redisClient;
        init();
    }

    private void init() {
        router.route("/login/:name/:password").handler(this::doLogin);
        router.route("/register/:id/:vCode/:name/:password").handler(this::doRegister);
    }


    private void doLogin(RoutingContext routingContext) {
        String name = routingContext.request().getParam("name");
        String password = routingContext.request().getParam("password");
        if (name == null || password == null) {
            resultData(routingContext, new JsonObject().put("code0", 1).put("msg", "缺少登入参数"));
        } else {
            userService.login(name, password, resultHandlerData(routingContext));
        }
    }


    private void doRegister(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        String vCode = routingContext.request().getParam("vCode");
        String name = routingContext.request().getParam("name");
        String password = routingContext.request().getParam("password");
        if (StringUtil.isEmpty(id) || StringUtil.isEmpty(vCode) || StringUtil.isEmpty(name) || StringUtil.isEmpty(password)) {
            resultData(routingContext, new JsonObject().put("code0", 1).put("msg", "缺少注册参数"));
        } else {
            redisClient.get(GetVerifyController.VERIFY_PREFIX + id, r -> {
                if (r.succeeded()) {
                    if (vCode.equals(r.result())) {
                        userService.createUser(name, password, resultHandlerData(routingContext));
                    } else {
                        resultData(routingContext, new JsonObject().put("code", 1).put("msg", "验证码错误"));
                    }
                } else {
                    resultData(routingContext, new JsonObject().put("code", 1).put("msg", "获取验证码错误"));
                }
            });
        }
    }
}
