package com.jsen.joker.boot.loader.joker.help;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * <p>
 *     joker
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class ConfHelp {
    public static Map<String, Object> conf(String root) {

        Map<String, Object> config = new HashMap<>();
        String pattern = ".*\\.properties";
        String number = "\\d+";

        // 创建 Pattern 对象
        Pattern tail = Pattern.compile(pattern);
        Pattern isNumber = Pattern.compile(number);

        File conf = new File(root, "conf");

        File[] children = conf.listFiles();
        if (children != null) {
            for (File file : children) {
                if (tail.matcher(file.getName()).matches()) {
                    try {
                        Properties properties = new Properties();
                        properties.load(new FileInputStream(file));

                        for (String key : properties.stringPropertyNames()) {
                            String value = properties.getProperty(key);
                            if (isNumber.matcher(value).matches()) {
                                config.put(key, Integer.valueOf(value));
                            } else {
                                config.put(key, properties.getProperty(key));
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        return config;
                    }
                }
            }
        }
        return config;
    }
}
