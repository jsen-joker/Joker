package com.jsen.test;


import com.jsen.joker.boot.utils.JarResourceUncompress;
import org.junit.Test;

import java.io.File;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/20
 */
public class JarTest {


    @Test
    public void testUn() {
        String jar = "/Users/jsen/joker/entry/joker-entry-server-1.0-SNAPSHOT.jar";
        File zipFile = new File(jar);
        File targetFile = new File("/Users/jsen/joker", "static");
        System.out.println(targetFile);
        try {
            JarResourceUncompress.unzip(zipFile, targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
