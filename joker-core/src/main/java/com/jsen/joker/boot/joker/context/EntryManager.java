package com.jsen.joker.boot.joker.context;

import com.jsen.joker.boot.RootVerticle;
import com.jsen.joker.boot.entity.Entry;
import io.vertx.core.DeploymentOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *     保存加载的entry的基本信息
 *
 * </p>
 *
 * @author jsen
 * @since 2018/5/23
 */
public class EntryManager {

    private static final Logger logger = LoggerFactory.getLogger(EntryManager.class);

    private List<Entry> entryList;

    public EntryManager() {
        this.entryList = new ArrayList<>();
    }

    public void failedStartEntry(Entry entry) {
        entry.setState(STATE.START_FAILED);
    }
    public void failedStopEntry(Entry entry) {
        entry.setState(STATE.STOP_FAILED);

    }
    public void succeedStopEntry(Entry entry) {
        entryList.remove(entry);
    }
    public void succeedStartEntry(Entry entry, String deploymentID) {
        entry.setState(STATE.UP).setDeploymentID(deploymentID);
    }

    public void clearAll() {
        entryList.clear();
    }

    public void sort() {
        Collections.sort(entryList);
    }

    public TreeMap<Integer, List<Entry>> group() {
        TreeMap<Integer, List<Entry>> ts = new TreeMap<>(Comparator.reverseOrder());
        ts.putAll(entryList.stream().collect(Collectors.groupingBy(Entry::getPriority)));
        return ts;
    }

    public TreeMap<Integer, List<Entry>> groupPart(List<Entry> entryList) {
        TreeMap<Integer, List<Entry>> ts = new TreeMap<>(Comparator.reverseOrder());
        ts.putAll(entryList.stream().collect(Collectors.groupingBy(Entry::getPriority)));
        return ts;
    }

    public TreeMap<Integer, List<Entry>> undeployGroup() {
        TreeMap<Integer, List<Entry>> ts = new TreeMap<>(entryList.stream().collect(Collectors.groupingBy(Entry::getPriority)));
        return ts;
    }

    public TreeMap<Integer, List<Entry>> undeployGroupPart(List<Entry> entryList) {
        TreeMap<Integer, List<Entry>> ts = new TreeMap<>(entryList.stream().collect(Collectors.groupingBy(Entry::getPriority)));
        return ts;
    }

    public List<Entry> getEntryList() {
        return entryList;
    }

    public EntryManager setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
        return this;
    }

    /*

    public static class Entry implements Comparable<Entry> {
        String deploymentID;
        String entryClass;
        DeploymentOptions deploymentOptions;
        STATE state;
        String groupID;
        String artifactId;
        String version;
        String fileName;
        boolean isScript;

        int priority;

        public Entry(String entryClass, DeploymentOptions deploymentOptions, STATE state, String groupID, String artifactId, String version, String fileName, boolean isScript, int priority) {
            this.entryClass = entryClass;
            this.deploymentOptions = deploymentOptions;
            this.state = state;
            this.groupID = groupID;
            this.artifactId = artifactId;
            this.version = version;
            this.fileName = fileName;
            this.isScript = isScript;
            this.priority = priority;
        }

        @Override
        public int compareTo(Entry o) {
            return o.priority - priority;
        }

        public int getPriority() {
            return priority;
        }

        public STATE getState() {
            return state;
        }

        public String getDeploymentID() {
            return deploymentID;
        }

        public String getEntryClass() {
            return entryClass;
        }

        public DeploymentOptions getDeploymentOptions() {
            return deploymentOptions;
        }

        public boolean isScript() {
            return isScript;
        }


    }
    */
    public enum STATE {
        STARTING, // 正在启动
        START_FAILED, // 部署失败
        UP,     // 部署成功
        STOPPING, // 正在停止
        STOP_FAILED, // 正在停止
    }
}
