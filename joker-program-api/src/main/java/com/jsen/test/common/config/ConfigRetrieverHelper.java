package com.jsen.test.common.config;

import io.reactivex.Observable;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Helper class for Vert.x configuration retriever.
 */
public class ConfigRetrieverHelper {

    private static final Logger logger = LoggerFactory.getLogger(ConfigRetrieverHelper.class);

    private ConfigRetriever configRetriever;
    private ConfigRetrieverOptions options = new ConfigRetrieverOptions();

    public ConfigRetrieverHelper usingScanPeriod(final long scanPeriod) {
        options.setScanPeriod(scanPeriod);
        return this;
    }

    public Observable<JsonObject> rxCreateConfig(final Vertx vertx) {
        configRetriever = ConfigRetriever.create(vertx, options);
        // TODO: improve here.
        Observable<JsonObject> configObservable = Observable.create(subscriber -> {
            configRetriever.getConfig(ar -> {
                if (ar.failed()) {
                    logger.error("获取配置信息失败");
                    subscriber.onError(ar.cause());
                } else {
                    logger.info("获取配置信息成功");
                    final JsonObject config =
                            vertx.getOrCreateContext().config().mergeIn(
                                    Optional.ofNullable(ar.result()).orElse(new JsonObject()));
                    subscriber.onNext(config);
                }
            });

            configRetriever.listen(ar -> {
                final JsonObject config =
                        vertx.getOrCreateContext().config().mergeIn(
                                Optional.ofNullable(ar.getNewConfiguration()).orElse(new JsonObject()));
                subscriber.onNext(config);
            });
        });

        configObservable.onErrorReturn(t -> {
            logger.error("Failed to emit configuration - Returning null", t);
            return null;
        });

        return configObservable.filter(Objects::nonNull);
    }

    public ConfigRetrieverHelper withHttpStore(final String host, final int port, final String path) {
        ConfigStoreOptions httpStore = new ConfigStoreOptions()
                .setType("http")
                .setConfig(new JsonObject()
                        .put("host", host).put("port", port).put("path", path));

        options.addStore(httpStore);
        return this;
    }

    public void close() {
        configRetriever.close();
    }
}