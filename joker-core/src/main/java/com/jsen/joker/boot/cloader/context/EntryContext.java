package com.jsen.joker.boot.cloader.context;

import com.jsen.joker.boot.cloader.EntryClassLoader;
import com.jsen.joker.boot.utils.FileSystemDetector;
import io.vertx.core.Verticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * <p>
 *     Verticle 加载 context
 * </p>
 *
 * @author jsen
 * @since 2018/5/16
 */
public class EntryContext {

    private static final Logger logger = LoggerFactory.getLogger(EntryContext.class);

    private EntryClassLoader entryClassLoader;

    private static EntryContext entryContext;
    public static EntryContext getDefaultEnterContext() {
        return entryContext;
    }

    public EntryContext() {
        entryContext = this;
        entryClassLoader = JokerContext.getDefaultJokerContext().getPluginClassLoader();
    }

    /**
     * 加载JAR
     * @param files 所有JAR包
     */

    public boolean addJars(List<FileSystemDetector.FileEntry> files) {
        if (entryClassLoader == null) {
            return false;
        }
        try {
            for (FileSystemDetector.FileEntry file: files) {
                if (file.file.getName().endsWith(".jar")) {
                    entryClassLoader.addFile(new URL("jar:file:" + file.file.getAbsolutePath() + "!/"), file.id);
                } else {
                    entryClassLoader.addFile(file.file.toURI().toURL(), file.id);
                }            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean delJars(List<FileSystemDetector.FileEntry> files) {
        for (FileSystemDetector.FileEntry file: files) {
            if (file.file.getName().endsWith(".jar")) {
                entryClassLoader.del(file.id);
            } else {
                entryClassLoader.del(file.id);
            }
        }
        return true;
    }


    public Class<? extends Verticle> getVerticleClazz(String clazzName) {
        try {
           return (Class<? extends Verticle>) entryClassLoader.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
