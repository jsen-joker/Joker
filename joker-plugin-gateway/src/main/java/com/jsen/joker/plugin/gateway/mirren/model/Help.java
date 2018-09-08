package com.jsen.joker.plugin.gateway.mirren.model;

import com.hazelcast.util.StringUtil;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/8
 */
public class Help {
    public static String getPath(String path, String defaultPath) {
        if (StringUtil.isNullOrEmpty(path)) {
            return "/" + defaultPath;
        } else {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (path.startsWith("/")) {
                return path;
            }
            return "/" + path;
        }

    }
}
