package com.jsen.redis.schedule.master.task.sk;


import com.jsen.redis.schedule.master.task.sk.impl.StaticJob001;
import com.jsen.redis.schedule.master.task.sk.impl.StaticJobWithTime;
import com.jsen.redis.schedule.master.task.sk.impl.StaticSimpleEcho;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class Jobs {
    public static Map<String, IJob> jobs = new HashMap<>();

    static {
        jobs.put("001", new StaticJob001());
        jobs.put("time", new StaticJobWithTime());
        jobs.put("echo", new StaticSimpleEcho());
    }
}
