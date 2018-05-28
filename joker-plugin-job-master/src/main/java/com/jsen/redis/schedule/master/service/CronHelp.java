package com.jsen.redis.schedule.master.service;

import com.jsen.redis.schedule.master.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
public class CronHelp {
    public static Date getScheduleTime(String cron, Date date) {

        if (cron != null && !"".equals(cron)) {
            try {
                return new CronExpression(cron).getNextValidTimeAfter(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

}
