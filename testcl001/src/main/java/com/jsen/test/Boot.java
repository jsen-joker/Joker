package com.jsen.test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/27
 */
public class Boot {
    public static void main(String[] args) {
        File file = new File("/Users/jsen/Documents/GitProjects/Test/hockvertx/testcl002/target/classes/");
        File file1 = new File("/Users/jsen/Documents/GitProjects/Test/hockvertx/testcl003/target/classes/");
        try {
            EntryClassLoader entryClassLoader = new EntryClassLoader(Boot.class.getClassLoader());
            EntryClassLoader.InnerClassLoader innerClassLoader = entryClassLoader.load(file.toURI().toURL());

            EntryClassLoader entryClassLoader1 = new EntryClassLoader(Boot.class.getClassLoader());
            EntryClassLoader.InnerClassLoader innerClassLoader1 = entryClassLoader1.load(file1.toURI().toURL());

            Class<?> clazz = innerClassLoader.loadClass("com.jsen.cl002.Echo002");
            Method method = clazz.getMethod("echo", String.class);
            method.invoke(clazz.newInstance(), "lucy");
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
