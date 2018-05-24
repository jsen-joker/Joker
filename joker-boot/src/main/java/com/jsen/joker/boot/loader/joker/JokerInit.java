package com.jsen.joker.boot.loader.joker;

import com.jsen.joker.boot.loader.cloader.JokerClassLoader;
import com.jsen.joker.boot.loader.joker.help.LibLister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class JokerInit {

    private static final Logger logger = LoggerFactory.getLogger(JokerInit.class);

    public static void init(Completer completer) {
        String projectRoot = getJokerRoot();

        if (projectRoot == null) {
            completer.failed();
        }
        // 在这下面的才能使用 joker lib
        URL[] urls = LibLister.libs(projectRoot);
        for (URL url:urls) {
            logger.debug(url.getPath());
        }
        try {
            if (JokerClassLoader.init(urls).get()) {
                completer.succeed();
            } else {
                completer.failed();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            completer.failed();
        }
    }

    public static String getJokerRoot() {
        URL url = JokerInit.class.getProtectionDomain().getCodeSource()
                .getLocation();
        String filePath = null;
        try {
            filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filePath == null) {
            return null;
        }
        if (filePath.endsWith("!/")) {
            filePath = filePath.substring(0, filePath.length() - 2);
        }
        if (filePath.endsWith(".jar")) {
            filePath = filePath.substring(filePath.indexOf(":") + 1, filePath.lastIndexOf(File.separator) + 1);
        }
        File libDir = new File(filePath);

        filePath = libDir.getAbsolutePath();
        return filePath;
    }

    public interface Completer {
        void succeed();
        void failed();
    }
}
