package com.jsen.joker.boot.joker.help;

import com.jsen.joker.boot.joker.Config;
import io.vertx.core.json.JsonObject;

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
    public static boolean conf() {

        Map<String, Object> config = new HashMap<>();
        String pattern = ".*\\.properties";
        String number = "\\d+";

        // 创建 Pattern 对象
        Pattern tail = Pattern.compile(pattern);
        Pattern isNumber = Pattern.compile(number);

        String root = Config.projectRoot;
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
                        return false;
                    }
                }
            }
        }

        Config.confs = config;
        return true;
    }

    public static JsonObject getJsonConfFile(String name) {
        String root = Config.projectRoot;
        File file = new File(new File(root, "conf"), name);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
            return new JsonObject(data.toString());
        } catch (Exception e) {
            return null;
        }

    }
}
