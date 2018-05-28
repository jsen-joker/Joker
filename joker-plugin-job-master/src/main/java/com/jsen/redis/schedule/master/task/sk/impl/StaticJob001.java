package com.jsen.redis.schedule.master.task.sk.impl;

import com.jsen.redis.schedule.master.task.sk.IJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class StaticJob001 extends IJob {
    private static final Logger logger = LoggerFactory.getLogger(StaticJob001.class);

    @Override
    protected void _exec() throws Exception {
        while (true) {
            if (Thread.interrupted()) {
                logger.info("任务成功被中断， 抛出InterruptedException异常");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.info("任务成功被中断， 抛出InterruptedException异常");
                throw e;
            }
            logger.info("Static job() exec");
        }
    }

    @Override
    protected void _stop() {
        logger.info("Static job() stop");
    }
}
