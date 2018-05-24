package com.jsen.joker.boot.entity;

import com.jsen.joker.boot.joker.context.EntryManager;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/24
 */
@DataObject(generateConverter = true)
public class Entry implements Comparable<Entry> {
    /**
     * vertx部署分配的ID
     */
    private String deploymentID;
    private String entryClass;
    private DeploymentOptions deploymentOptions;
    private EntryManager.STATE state;
    private String groupID;
    private String artifactId;
    private String version;
    private String fileName;
    private boolean isScript;

    private int priority;

    public Entry(String entryClass, DeploymentOptions deploymentOptions, EntryManager.STATE state,
                 String groupID, String artifactId, String version, String fileName, boolean isScript, int priority) {
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

    public Entry(JsonObject json) {
        EntryConverter.fromJson(json, this);
    }
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        EntryConverter.toJson(this, json);
        return json;
    }

    @Override
    public int compareTo(Entry o) {
        return o.priority - priority;
    }

    public Entry setState(EntryManager.STATE state) {
        this.state = state;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public EntryManager.STATE getState() {
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

    public Entry setDeploymentID(String deploymentID) {
        this.deploymentID = deploymentID;
        return this;
    }

    public boolean isScript() {
        return isScript;
    }
}
