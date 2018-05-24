package com.jsen.joker.boot.cloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 插件类加载器，在插件目录中搜索jar包，并为发现的资源(jar)构造一个类加载器,将对应的jar添加到classpath中
 * @author strawxdl
 */
public class EntryClassLoader extends URLClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(EntryClassLoader.class);

    private Map<String, JarURLConnection> cachedJarFiles = new HashMap<>();
    public EntryClassLoader(ClassLoader classLoader) {
        super(new URL[] {}, classLoader);
    }

    /**
     * 将指定的文件url添加到类加载器的classpath中去，并缓存jar connection，方便以后卸载jar
     * 指定支持jar协议的URL
     * @param file 一个可想类加载器的classpath中添加的文件url
     */
    public void addURLFile(URL file) {
        try {
            // 打开并缓存文件url连接

            URLConnection uc = file.openConnection();
            if (uc instanceof JarURLConnection) {
                uc.setUseCaches(true);
                ((JarURLConnection) uc).getManifest();
                cachedJarFiles.put(file.getPath(), (JarURLConnection) uc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to cache plugin JAR file: " + file.toExternalForm());
        }
        addURL(file);
    }
    public void addDir(URL file) {
        addURL(file);
    }

    /**
     * 卸载jar包
     */
    public void unloadJarFiles() {
        for (Map.Entry<String, JarURLConnection> entry : cachedJarFiles.entrySet()) {
            try {
                logger.debug("Unloading plugin JAR file " + entry.getValue().getJarFile().getName());
                entry.getValue().getJarFile().close();
            } catch (Exception e) {
                logger.error("Failed to unload JAR file\n"+e);
            }
        }
    }






    /**
     * 更新时要新建Loader 继承原来的loader的JAR
     * @param entryClassLoader
     */
    @Deprecated
    public void patch(EntryClassLoader entryClassLoader) {
        for (Map.Entry<String, JarURLConnection> entry : entryClassLoader.cachedJarFiles.entrySet()) {
            if (!cachedJarFiles.containsKey(entry.getKey())) {
                addURLFile(entry.getValue().getJarFileURL());
            }
        }
    }
}