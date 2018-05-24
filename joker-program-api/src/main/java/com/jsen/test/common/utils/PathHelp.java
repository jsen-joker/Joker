package com.jsen.test.common.utils;

import java.io.File;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/16
 */
public class PathHelp {

    /**
     * 获取项目所在路径(包括jar)
     *
     * @return
     */
    public static String getJokerRoot() {

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

}