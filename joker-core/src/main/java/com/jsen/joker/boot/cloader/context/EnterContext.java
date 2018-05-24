package com.jsen.joker.boot.cloader.context;

import com.jsen.joker.boot.cloader.EntryClassLoader;
import io.vertx.core.Verticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     Verticle 加载 context
 * </p>
 *
 * @author jsen
 * @since 2018/5/16
 */
public class EnterContext {

    private static final Logger logger = LoggerFactory.getLogger(EnterContext.class);

    EntryClassLoader entryClassLoader;

    private static EnterContext enterContext;
    public static EnterContext getDefaultEnterContext() {
        return enterContext;
    }

    public EnterContext() {
        enterContext = this;
        entryClassLoader = JokerContext.getDefaultJokerContext().getPluginClassLoader();
    }

    /**
     * 加载JAR 创建新loader
     * @param files 所有JAR包
     */
    public boolean reloadJars(File ...files) {
        EntryClassLoader nP = JokerContext.getDefaultJokerContext().getPluginClassLoader();
        try {
            for (File file: files) {
                nP.addURLFile(new URL("jar:file:" + file.getAbsolutePath() + "!/"));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        if (entryClassLoader != null) {
            entryClassLoader.unloadJarFiles();
        }
        entryClassLoader = nP;
        return true;
    }
    public boolean reloadJars(List<File> files) {
        EntryClassLoader nP = JokerContext.getDefaultJokerContext().getPluginClassLoader();
        try {
            for (File file: files) {
                if (file.getName().endsWith(".jar")) {
                    nP.addURLFile(new URL("jar:file:" + file.getAbsolutePath() + "!/"));
                } else {
                    nP.addDir(file.toURI().toURL());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        if (entryClassLoader != null) {
            entryClassLoader.unloadJarFiles();
        }
        entryClassLoader = null;
        entryClassLoader = nP;
        return true;
    }

    /**
     * 加载JAR 创建新loader
     * @param urls 所有JAR包
     */
    public boolean reloadJars(URL[] urls) {
        EntryClassLoader nP = JokerContext.getDefaultJokerContext().getPluginClassLoader();
        for (URL url:urls) {
            nP.addURLFile(url);
        }
        if (entryClassLoader != null) {
            entryClassLoader.unloadJarFiles();
        }
        entryClassLoader = nP;
        return true;
    }

    /**
     * 继承原来的loader
     * @param files
     */
    @Deprecated
    public boolean reloadJarsExtends(File ...files) {
        EntryClassLoader nP = JokerContext.getDefaultJokerContext().getPluginClassLoader();
        try {
            for (File file: files) {
                nP.addURLFile(new URL("jar:file:" + file.getAbsolutePath() + "!/"));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        if (entryClassLoader != null) {
            nP.patch(entryClassLoader);
            entryClassLoader.unloadJarFiles();
        }
        entryClassLoader = nP;
        return true;
    }

    /**
     * 加载JAR
     * @param files 所有JAR包
     */
    public boolean addJars(File ...files) {
        if (entryClassLoader == null) {
            return false;
        }
        try {
            for (File file: files) {
                if (file.getName().endsWith(".jar")) {
                    entryClassLoader.addURLFile(new URL("jar:file:" + file.getAbsolutePath() + "!/"));
                } else {
                    entryClassLoader.addDir(file.toURI().toURL());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean addJars(List<File> files) {
        if (entryClassLoader == null) {
            return false;
        }
        try {
            for (File file: files) {
                if (file.getName().endsWith(".jar")) {
                    entryClassLoader.addURLFile(new URL("jar:file:" + file.getAbsolutePath() + "!/"));
                } else {
                    entryClassLoader.addDir(file.toURI().toURL());
                }            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 清空当前loader的所有JAR connection
     */
    public void unloadJars() {
        if (entryClassLoader != null) {
            entryClassLoader.unloadJarFiles();
        }
    }

    public Verticle getVerticle(String clazzName) {
        if (entryClassLoader == null) {
            return null;
        }
        try {
            Class<?> clazz= entryClassLoader.loadClass(clazzName);
            return (Verticle) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Class<? extends Verticle> getVerticleClazz(String clazzName) {
        if (entryClassLoader == null) {
            return null;
        }
        try {
           return (Class<? extends Verticle>) entryClassLoader.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 加载jar classpath。
     */
    @Deprecated
    public void loadClasspath(String workDir) {
        logger.info("*** 开始加载jar 模块 ***");

        List<File> fileL = new ArrayList<>();
        List<String> files = getJarFiles(workDir);
        if (files != null) {
            for (String f : files) {
                File file = new File(f);
                loopFiles(file, fileL);
            }
        }
        File[] fs = new File[fileL.size()];
        fileL.toArray(fs);
        reloadJars(fs);
        logger.info("*** 结束加载jar 模块 ***");
    }
    /**
     * 循环遍历目录，找出所有的jar包。
     * @param file 当前遍历文件
     */
    @Deprecated
    private void loopFiles(File file, List<File> fileL) {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp, fileL);
            }
        }
        else {
            if (file.getAbsolutePath().endsWith(".jar")  && !file.getAbsolutePath().endsWith("-fat.jar")) {
                fileL.add(file);
            }
        }
    }

    /**
     * 从配置文件中得到配置的需要加载到classpath里的路径集合。
     * @return
     */
    @Deprecated
    private List<String> getJarFiles(String root) {
        // TODO 从properties文件中读取配置信息略
        File r = new File(root);
        File files[] = r.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        List<String> jars = new ArrayList<>();
        for (File jar : files) {
            jars.add(jar.getAbsolutePath());
            logger.info(jar.getAbsolutePath());
        }
        return jars;
    }

}
