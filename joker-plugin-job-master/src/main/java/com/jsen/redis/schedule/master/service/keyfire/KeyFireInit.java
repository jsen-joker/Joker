package com.jsen.redis.schedule.master.service.keyfire;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/26
 */
public class KeyFireInit {
    public static final Logger logger = LoggerFactory.getLogger(KeyFireInit.class.getName());

    public static void init(EventBus eventBus, RedisClient redis, WebClient webClient) {
        redis.psubscribe("__key*__:*", ar -> {
            if (ar.succeeded()) {
                logger.info("成功订阅redis key过期事件");
            } else {
                logger.error("订阅redis key过期事件失败");
                logger.error(ar.cause().getMessage());
            }
        });

        WorkerExp workerExp = new WorkerExp(redis);
        Exec exec = new Exec(redis, webClient);


        eventBus.<JsonObject>consumer("io.vertx.redis.__key*__:*", received -> {
            // do whatever you need to do with your message
            JsonObject rec = received.body();
            if ("ok".equals(rec.getString("status", "no"))) {
                String key = rec.getJsonObject("value", new JsonObject()).getString("message", "");

                if (!"".equals(key)) {
                    String head = key.substring(0, key.indexOf(":"));
                    String taskID = key.substring(key.indexOf(":") + 1);
                    switch (head) {
                        case "fire":
                            exec.exec(taskID);
                            break;
                        case "task":
                            workerExp.exec(taskID);
                            break;
                        default:
                    }
                }
            }
        });

    }

}
