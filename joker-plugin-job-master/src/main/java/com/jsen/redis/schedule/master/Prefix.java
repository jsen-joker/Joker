package com.jsen.redis.schedule.master;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
public interface Prefix {
    String taskConf = "taskconf:";
    String task = "task:";
    String fire = "fire:";
    String schedule = "schedule:";
    String worker = "worker:";
    String lock = "lock:";
}
