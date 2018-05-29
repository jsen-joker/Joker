package com.jsen.redis.schedule.worker.job.impl;

import com.jsen.redis.schedule.worker.job.IJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
public class StaticJobWithTime extends IJob {
    private static final Logger logger = LoggerFactory.getLogger(StaticJob001.class);

    @Override
    protected void _exec() throws Exception {
        try {
            logger.info("Job started");
            Thread.sleep(5000);
            logger.info("Job ended");
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void _stop() {
        logger.info("Static job() stop");
    }
}
