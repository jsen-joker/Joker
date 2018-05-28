package com.jsen.test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/27
 */
public class EntryClassLoader extends URLClassLoader {

    public static class InnerClassLoader extends URLClassLoader {

        public InnerClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }


        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }
    private static final List<InnerClassLoader> loaders = new ArrayList<>();


    public EntryClassLoader(ClassLoader parent) {

        super(new URL[]{}, parent);
        ClassLoader classLoader = this;

        while (classLoader != null) {
            System.out.println(classLoader.getClass().getName());
            classLoader = classLoader.getParent();
        }
    }

    public InnerClassLoader load(URL url) {
        InnerClassLoader innerClassLoader = new InnerClassLoader(new URL[]{url}, this);
        loaders.add(innerClassLoader);
        return innerClassLoader;
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        for (InnerClassLoader innerClassLoader:loaders) {
            clazz = innerClassLoader.findClass(name);
            if (clazz != null) {
                return clazz;
            }
        }
        return super.findClass(name);
    }
}
