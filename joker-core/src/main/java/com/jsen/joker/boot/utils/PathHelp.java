package com.jsen.joker.boot.utils;

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
    public static String getProjectPath() {

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
            filePath = filePath.substring(filePath.indexOf(":") + 1, filePath.lastIndexOf("/") + 1);
        }
        java.io.File file = new java.io.File(filePath);
        filePath = file.getAbsolutePath();
        return filePath;
    }

}