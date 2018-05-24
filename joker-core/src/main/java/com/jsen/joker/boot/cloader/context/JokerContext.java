package com.jsen.joker.boot.cloader.context;

import com.jsen.joker.boot.cloader.EntryClassLoader;
import com.jsen.joker.boot.joker.context.BootVertx;
import com.jsen.joker.boot.loader.cloader.JokerClassLoader;
import io.vertx.core.Future;

/**
 * <p>
 *     joker Context
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class JokerContext {

    private static JokerContext jokerContext;

    public static JokerContext getDefaultJokerContext() {
        return jokerContext;
    }


    public JokerContext() {
        jokerContext = this;
    }

    public Future<Void> init() {
        return new BootVertx().boot();
    }

    public EntryClassLoader getPluginClassLoader() {
        return new EntryClassLoader(JokerClassLoader.getDefaultLoader());
    }
}
