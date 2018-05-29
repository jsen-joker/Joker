package com.jsen.redis.schedule.master.log;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/28
 */
public class LoggerMessage {

    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;

    public LoggerMessage(String body, String timestamp, String threadName, String className, String level) {
        this.body = body;
        this.timestamp = timestamp;
        this.threadName = threadName;
        this.className = className;
        this.level = level;
    }


    @Override
    public String toString() {
        return timestamp + " [" + threadName + "] | " + level + " | " + className + " | " + body;
    }
}