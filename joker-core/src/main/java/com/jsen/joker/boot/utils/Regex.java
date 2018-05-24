package com.jsen.joker.boot.utils;

import java.util.regex.Pattern;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/20
 */
public class Regex {


    private static String pattern = ".*\\.jar";
    private static String patternScript = ".*\\.js";
    private static String detectFilter = ".*\\.jar|.*\\.js";

    // 创建 Pattern 对象

    public static Pattern tailJar = Pattern.compile(pattern);
    public static Pattern tailScript = Pattern.compile(patternScript);
    public static Pattern tailDetectFilter = Pattern.compile(detectFilter);
}
