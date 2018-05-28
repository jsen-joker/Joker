package com.jsen.redis.schedule.master.task.sk.impl;

import com.jsen.redis.schedule.master.task.sk.IJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
public class StaticSimpleEcho extends IJob {
    private static final Logger logger = LoggerFactory.getLogger(StaticJob001.class);

    @Override
    protected void _exec() throws Exception {
        logger.info("Job echo data");
    }

    @Override
    protected void _stop() {
        logger.info("Static job() stop");
    }
}
