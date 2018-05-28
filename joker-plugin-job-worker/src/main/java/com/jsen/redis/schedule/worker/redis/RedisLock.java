package com.jsen.redis.schedule.worker.redis;

import com.jsen.redis.schedule.master.Prefix;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class RedisLock {
    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final String LOCK = "1";

    private RedisClient redisClient;

    public RedisLock(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public Future<Void> getLock(String key) {
        Future<Void> future = Future.future();
        getLock(key, future.completer());
        return future;
    }

    public void getLock(String key, Handler<AsyncResult<Void>> handler) {
        String lockKey = Prefix.lock + key;
        redisClient.setnx(lockKey, LOCK, ar -> {
            if (ar.succeeded()) {
                if (ar.result() == 1) {
                    redisClient.expire(lockKey, 1, r2 -> {});
                    handler.handle(Future.succeededFuture());
                } else {
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                        logger.error("sleep 50L exception : " + e.getMessage());
                    }
                    getLock(key, handler);
                }
            } else {
                logger.error("redis error:" + ar.cause().getMessage());
                handler.handle(Future.succeededFuture());
            }
        });
    }

    public void unLock(String key) {
        String lockKey = "lock:" + key;

        redisClient.del(lockKey, ar -> {
            if (ar.succeeded()) {
                if (ar.result() == 1) {
                    logger.info("unlock " + lockKey + " succeed ");
                } else {
                    logger.warn("unlock " + lockKey + " failed ");
                }
            } else {
                logger.error("redis error:" + ar.cause().getMessage());
            }
        });
    }
}
