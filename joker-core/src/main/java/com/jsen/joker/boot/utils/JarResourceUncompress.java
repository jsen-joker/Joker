package com.jsen.joker.boot.utils;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * <p>
 *     解压Jar包下的webroot目录到 指定目录
 * </p>
 *
 * @author jsen
 * @since 2018/5/20
 */
public class JarResourceUncompress {


    public static void unzip(File zipFile, File unzipFileDir)  {

        //开始解压
        ZipEntry entry;
        int count, bufferSize = 2048;
        byte[] buffer = new byte[bufferSize];
        JarFile zip;
        try {
            zip = new JarFile(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Enumeration<JarEntry> entries = zip.entries();
        //循环对压缩包里的每一个文件进行解压
        while(entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (!entry.getName().startsWith("webroot")) {
                continue;
            }
            File file = new File(unzipFileDir, entry.getName());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                try(BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                    //写入文件
                    while ((count = is.read(buffer, 0, bufferSize)) != -1) {
                        os.write(buffer, 0, count);
                    }
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
