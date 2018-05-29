package com.jsen.joker.boot.cloader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jsen
 */
public class EntryClassLoader extends URLClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(EntryClassLoader.class);

    private static final List<InnerClassLoader> classLoaderList = Lists.newArrayList();

    public EntryClassLoader(ClassLoader classLoader) {
        super(new URL[] {}, classLoader);
    }

    /**
     * 将指定的文件url添加到类加载器的classpath中去，并缓存jar connection，方便以后卸载jar
     * 指定支持jar协议的URL
     * @param url 一个可想类加载器的classpath中添加的文件url
     */
    public void addFile(URL url, String md5) {
        logger.debug("add jar with md5:" + md5);
        InnerClassLoader innerClassLoader = new InnerClassLoader(url, md5, this);
        classLoaderList.add(innerClassLoader);
    }
    public void del(String md5) {
        logger.debug("del jar with md5:" + md5);
        logger.debug(classLoaderList.size() + "");
        for (int i = 0; i < classLoaderList.size(); i++) {
            InnerClassLoader innerClassLoader = classLoaderList.get(i);
            if (md5.equals(innerClassLoader.path)) {
                innerClassLoader.delJar();
                classLoaderList.remove(i);
                break;
            }
        }
        logger.debug(classLoaderList.size() + "");
        // maps.remove(url.getPath());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = null;

        for (InnerClassLoader innerClassLoader : classLoaderList) {
            c = innerClassLoader.findClass(name);
            if (c != null) {
                break;
            }
        }
        if (c == null) {
            c = super.loadClass(name, resolve);
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    private static class InnerClassLoader extends URLClassLoader {
        private String path;
        private JarURLConnection jarURLConnection = null;

        InnerClassLoader(URL url, String md5, EntryClassLoader parent) {
            super(new URL[] {}, parent);
            this.path = md5;
            try {
                URLConnection uc = url.openConnection();
                if (uc instanceof JarURLConnection) {
                    jarURLConnection = (JarURLConnection) uc;
                    jarURLConnection.getManifest();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            addURL(url);
        }

        @Override
        protected Class<?> findClass(String name) {
            Class<?> clazz = findLoadedClass(name);
            try {
                if (clazz == null) {
                    clazz = super.findClass(name);
                }
            } catch (ClassNotFoundException e) {
                // e.printStackTrace();
            }
            return clazz;
        }

        protected void delJar() {
            if (jarURLConnection != null) {
                logger.debug("close jar url connection");
                try {
                    jarURLConnection.getJarFile().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public boolean equals(Object obj) {
            return this.path.equals(obj.toString());
        }
    }

    private class Item{
        private String path;

        Item(String path) {
            this.path = path;
        }
        @Override
        public String toString() {
            return path;
        }
        @Override
        public boolean equals(Object o) {
            return path.equals(o.toString());
        }
    }
}