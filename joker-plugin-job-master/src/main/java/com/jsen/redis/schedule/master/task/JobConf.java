package com.jsen.redis.schedule.master.task;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *     一个Job任务的 全部配置信息
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
@DataObject(generateConverter = true)
public class JobConf {


    private static final Logger logger = LoggerFactory.getLogger(JobConf.class);


    /**
     * 静态任务
     * 和groovy动态编译任务相比较
     */
    private boolean staticJob = true;
    /**
     * 周期任务调度周期
     * 没有设置corn 默认调用一次
     */
    private String cron = null;
    /**
     * 静态任务IJob接口的全名
     * 非静态任务的Job类数据
     */
    private String jobData = "";
    /**
     * Job taskID
     */
    private String taskID;

    /**
     * 周期任务的执行次数记录
     */
    private Long index = 0L;

    /**
     * 完成回调
     */
    // private LifeCycle completer;

    public JobConf(boolean staticJob, String cron, String jobData, String taskID) {
        this.staticJob = staticJob;
        this.cron = cron;
        this.jobData = jobData;
        this.taskID = taskID;
    }

    public JobConf(boolean staticJob, String cron, String jobData, String taskID, Long index) {
        this.staticJob = staticJob;
        this.cron = cron;
        this.jobData = jobData;
        this.taskID = taskID;
        this.index = index;
    }

    public JobConf(JsonObject jsonObject) {
        JobConfConverter.fromJson(jsonObject, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JobConfConverter.toJson(this, json);
        return json;
    }


    public boolean isStaticJob() {
        return staticJob;
    }

    public JobConf setStaticJob(boolean staticJob) {
        this.staticJob = staticJob;
        return this;
    }

    public String getCron() {
        return cron;
    }

    public JobConf setCron(String cron) {
        this.cron = cron;
        return this;
    }

    public String getJobData() {
        return jobData;
    }

    public JobConf setJobData(String jobData) {
        this.jobData = jobData;
        return this;
    }

    public String getTaskID() {
        return taskID;
    }

    public JobConf setTaskID(String taskID) {
        this.taskID = taskID;
        return this;
    }

    public Long getIndex() {
        return index;
    }

    public JobConf setIndex(Long index) {
        this.index = index;
        return this;
    }

    public static boolean isSingleJob(JobConf jobConf) {
        return jobConf.getCron() == null || "".equals(jobConf.getCron());
    }

}
