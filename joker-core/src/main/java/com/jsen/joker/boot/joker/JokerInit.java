package com.jsen.joker.boot.joker;

import com.jsen.joker.boot.cloader.context.JokerContext;
import com.jsen.joker.boot.joker.help.ConfHelp;
import com.jsen.joker.boot.utils.PathHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
        Config.projectRoot = getJokerRoot();

        if (Config.projectRoot == null) {
            completer.failed();
        }

        logger.debug("The project root is:" + Config.projectRoot);

        // 加载 joker 配置文件
        if (!ConfHelp.conf()) {
            completer.failed();
        }

        new JokerContext();

        JokerContext.getDefaultJokerContext().init().setHandler(a -> {
            if (a.succeeded()) {
                completer.succeed();
            } else {
                completer.failed();
            }
        });
    }

    private static String getJokerRoot() {

        java.net.URL url = PathHelp.class.getProtectionDomain().getCodeSource()
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
        filePath = libDir.getParentFile().getAbsolutePath();

        return filePath;
    }

    public interface Completer {
        void succeed();
        void failed();
    }
}
