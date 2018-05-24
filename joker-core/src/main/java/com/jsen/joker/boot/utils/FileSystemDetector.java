package com.jsen.joker.boot.utils;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 *     根据MD5 监测文件变化
 * </p>
 *
 * @author jsen
 * @since 2018/5/22
 */
public class FileSystemDetector {

    private List<FileEntry> lastDetect = Lists.newArrayList();

    private OnFileChangeListener onFileChangeListener;
    private File root;
    private Pattern filter;

    public FileSystemDetector(OnFileChangeListener onFileChangeListener, File root, Pattern filter) {
        this.onFileChangeListener = onFileChangeListener;
        this.root = root;
        this.filter = filter;
    }

    public void detect() {

        File[] children = root.listFiles();

        if (children == null) {
            return;
        }
        List<FileEntry> currentDetect = Arrays.stream(children).filter(File::isFile).filter(f -> filter.matcher(f.getName()).matches()).map(FileEntry::new).filter(fE -> !"0".equals(fE.id)).collect(Collectors.toList());
        List<FileEntry> add = currentDetect.stream().filter(fE -> !lastDetect.contains(fE)).collect(Collectors.toList());
        List<FileEntry> del = lastDetect.stream().filter(fE -> !currentDetect.contains(fE)).collect(Collectors.toList());

        if (!add.isEmpty() || !del.isEmpty()) {
            lastDetect = currentDetect;
            onFileChangeListener.change(currentDetect, add, del);
        }
    }


    public static class FileEntry {
        /**
         * md5
         */
        public String id;
        public File file;

        public FileEntry(File file) {
            this.file = file;
            try {
                this.id = MD5Util.md5HashCode(file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                id = "0";
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String) {
                return obj.equals(id);
            }
            return obj instanceof FileEntry && id.equals(((FileEntry) obj).id);
        }
    }

    public interface OnFileChangeListener {
        void change(List<FileEntry> current, List<FileEntry> add, List<FileEntry> del);
    }

}
