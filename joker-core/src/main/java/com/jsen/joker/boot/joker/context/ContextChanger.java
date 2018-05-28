package com.jsen.joker.boot.joker.context;

import com.jsen.joker.boot.RootVerticle;
import com.jsen.joker.boot.cloader.context.EntryContext;
import com.jsen.joker.boot.utils.FileSystemDetector;
import com.jsen.joker.boot.utils.JarResourceUncompress;
import com.jsen.joker.boot.utils.Regex;
import com.jsen.joker.boot.utils.xml.GenMaven;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * <p>
 *     每次出现文件变化，会调用
 * </p>
 *
 * @author jsen
 * @since 2018/5/22
 */
public class ContextChanger implements FileSystemDetector.OnFileChangeListener {
    private static Lock lock = new ReentrantLock();
    private static final Logger logger = LoggerFactory.getLogger(ContextChanger.class);

    private final String root;

    public ContextChanger(String root) {
        this.root = root;
    }

    @Override
    public void change(List<FileSystemDetector.FileEntry> add, List<FileSystemDetector.FileEntry> del) {
        if (!lock.tryLock()) {
            logger.debug("joker loop locked return");
            return;
        }
        lock.lock();

        if (del.isEmpty()) {
            extractJarResource(add);
            EntryContext.getDefaultEnterContext().addJars(add);
            List<Entry> addEntries = add.stream().map(Entry::new).collect(Collectors.toList());
            RootVerticle.getDefaultRootVerticle().loadEntries(addEntries).setHandler(ar -> lock.unlock());
        } else {

            // delete
            del.forEach(item -> {
                String fileName = item.file.getName();
                if (fileName.contains(".")) {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                }
                File file = new File(new File(root, "static"), fileName);
                if (file.exists()) {
                    file.delete();
                }
            });
            extractJarResource(add);
            // reload
            RootVerticle.getDefaultRootVerticle().clearEntries(del.stream().map(item -> item.file.getAbsolutePath()).collect(Collectors.toList())).setHandler(ar0 -> {
                EntryContext.getDefaultEnterContext().delJars(del);
                EntryContext.getDefaultEnterContext().addJars(add);
                RootVerticle.getDefaultRootVerticle().loadEntries(add.stream().map(Entry::new).collect(Collectors.toList())).setHandler(ar -> lock.unlock());
            });
        }
    }

    private void extractJarResource(List<FileSystemDetector.FileEntry> add) {
        add.forEach(item -> {
            String fileName = item.file.getName();
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            File file = new File(new File(root, "static"), fileName);
            if (file.exists()) {
                file.delete();
            }
            JarResourceUncompress.unzip(item.file, file);
        });


    }

    public static class Entry{
        public FileSystemDetector.FileEntry fileEntry;
        public JsonArray entries;
        public boolean isScript = false;
        public String groupId;
        public String artifactId;
        public String version;

        public Entry(FileSystemDetector.FileEntry fileEntry) {
            this.fileEntry = fileEntry;
            String fN = fileEntry.file.getName();
            if (Regex.tailJar.matcher(fN).matches()) {
                isScript = false;
                JsonObject conf = GenMaven.parser(fileEntry.file.getAbsolutePath());
                entries = conf.getJsonArray("entries", new JsonArray());
                groupId = conf.getString("groupId", "");
                artifactId = conf.getString("artifactId", "");
                version = conf.getString("version", "");
            } else if (Regex.tailScript.matcher(fN).matches()) {
                isScript = true;
            }

        }
    }
}
