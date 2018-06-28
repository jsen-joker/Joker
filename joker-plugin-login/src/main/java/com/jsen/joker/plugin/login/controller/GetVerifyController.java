package com.jsen.joker.plugin.login.controller;

import com.jsen.joker.plugin.login.utils.verify.VerifyCodeUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;
import org.hsqldb.lib.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>
 *     多种验证码获取方式，图像验证码，移动验证码，邮箱验证码，（阿里短信验证）
 *     所有验证码统一存储在redis中
 * </p>
 *
 * @author jsen
 * @since 2018/5/31
 */
public class GetVerifyController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(GetVerifyController.class);

    public static final String VERIFY_PREFIX = "verify:";
    private RedisClient redisClient;

    public GetVerifyController(Router router, RedisClient redisClient) {
        super(router);
        this.redisClient = redisClient;
        init();
    }

    private void init() {
        router.route("/verify/image/:clientID").handler(this::getVerifyImage);
        router.route("/verify/email/:clientID").handler(this::getVerifyImage);
    }

    private void getVerifyImage(RoutingContext routingContext) {
        String clientID = routingContext.request().getParam("clientID");
        if (StringUtil.isEmpty(clientID)) {
            resultData(routingContext, new JsonObject().put("code0", 1).put("msg", "缺少登入参数"));
        } else {

            String vCode = VerifyCodeUtils.generateVerifyCode(4);

            redisClient.setex(VERIFY_PREFIX + clientID, 600, vCode, r -> {
                logger.debug("set result:" + r.result());
                if (r.succeeded() && "OK".equals(r.result())) {
                    int w = 200, h = 80;
                    try {
                        Buffer buffer = Buffer.buffer(VerifyCodeUtils.outputBytes(w, h, vCode));
                        routingContext.response().write(buffer).end();
                    } catch (IOException e) {
                        resultData(routingContext, new JsonObject().put("code0", 1).put("msg", "获取验证码IO出错").put("error", e.getMessage()));
                        e.printStackTrace();
                    }
                } else {
                    resultData(routingContext, new JsonObject().put("code0", 1).put("msg", "保存到验证信息出错"));
                }
            });
        }

    }


}
