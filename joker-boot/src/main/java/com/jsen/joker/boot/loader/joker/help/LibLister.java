package com.jsen.joker.boot.loader.joker.help;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 *     joker
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class LibLister {
    private static final Logger logger = LoggerFactory.getLogger(LibLister.class);

    public static URL[] libs(String root) {

        String pattern = ".*\\.jar|.*\\.zip";

        // 创建 Pattern 对象
        Pattern tail = Pattern.compile(pattern);


        List<URL> result = new ArrayList<>();
        File libDir = new File(root, "lib");
        loop(libDir, result, tail);

        URL[] urls = new URL[result.size()];
        result.toArray(urls);
        return urls;
    }

    private static void loop(File file, List<URL> result, Pattern pattern) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File f:children) {
                    loop(f, result, pattern);
                }
            }
        } else {
            if (pattern.matcher(file.getName()).matches()) {
                try {
                    result.add(new URL("jar:file:" + file.getAbsolutePath() + "!/"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
