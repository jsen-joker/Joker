package com.jsen.joker.boot.loader.cloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 *     加载lib目录的资源
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 *
 * 用于加载Joker项目资源
 */
public class JokerClassLoader extends URLClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(JokerClassLoader.class);


    private static JokerClassLoader jokerClassLoader;
    public static CompletableFuture<Boolean> init(URL[] urls) {
        CompletableFuture<Boolean> com = new CompletableFuture<>();
        jokerClassLoader = new JokerClassLoader(urls);
        Thread.currentThread().setContextClassLoader(jokerClassLoader);

        /*
        init joker context
         */
        try {
            Class<?> bootClazz = jokerClassLoader.loadClass("com.jsen.joker.boot.Boot");
            Method method = bootClazz.getMethod("boot");
            @SuppressWarnings("Unchecked")
            CompletableFuture<Boolean> result = (CompletableFuture<Boolean>)method.invoke(bootClazz.newInstance());
            try {
                if (result.get()) {
                    com.complete(true);
                } else {
                    com.complete(false);
                }
            } catch (InterruptedException | ExecutionException e) {

                e.printStackTrace();
                com.complete(false);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            com.complete(false);
        }
        return com;
    }
    public static JokerClassLoader getDefaultLoader() {
        return jokerClassLoader;
    }


    private JokerClassLoader(URL[] urls) {
        super(urls, findParentClassLoader());
    }
    @Deprecated
    private JokerClassLoader(URL[] urls, ClassLoader classLoader) {
        super(urls, classLoader);
    }

    /**
     * 定位基于当前上下文的父类加载器
     * @return 返回可用的父类加载器.
     */
    private static ClassLoader findParentClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

}
