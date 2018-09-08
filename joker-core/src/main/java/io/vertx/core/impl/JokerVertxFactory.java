package io.vertx.core.impl;

import com.jsen.joker.boot.cloader.context.EntryContext;
import io.vertx.core.*;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.core.spi.VertxFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/9
 */
public class JokerVertxFactory implements VertxFactory {
    private static final Logger log = LoggerFactory.getLogger(JokerVertxFactory.class);

    @Override
    public Vertx vertx() {
        Class<?> clazz =EntryContext.getDefaultEnterContext().getClazz(VertxImpl.class.getName());
        try {
            if (clazz != null) {
                log.debug("AAAAAAAAAAAAAAAA");
                return (Vertx) clazz.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Vertx vertx(VertxOptions options) {
        if (options.isClustered()) {
            throw new IllegalArgumentException("Please use Vertx.clusteredVertx() to create a clustered Vert.x instance");
        }
        Class<?> clazz = EntryContext.getDefaultEnterContext().getClazz(VertxImpl.class.getName());
        try {
            if (clazz != null) {
                log.debug("BBBBBBBBBBBBBBBBBBBB");
                Constructor constructor = clazz.getConstructor(VertxOptions.class);
                return (Vertx) constructor.newInstance(options);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void clusteredVertx(VertxOptions options, final Handler<AsyncResult<Vertx>> resultHandler) {
        // We don't require the user to set clustered to true if they use this method
        options.setClustered(true);
        Class<?> clazz = EntryContext.getDefaultEnterContext().getClazz(VertxImpl.class.getName());
        try {
            if (clazz != null) {
                log.debug("CCCCCCCCCCCCCCCCCCCCC");
                Constructor constructor = clazz.getConstructor(VertxOptions.class, Handler.class);
                constructor.newInstance(options, resultHandler);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context context() {
        return VertxImpl.context();
    }
}
