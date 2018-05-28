import com.jsen.redis.schedule.master.service.CronHelp;
import org.junit.Test;

import java.util.Date;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/25
 */
public class TestCron {

    @Test
    public void test() {
        String cron = "*/10 * * * * ? *";
        Date date = CronHelp.getScheduleTime(cron, new Date());
        System.out.println(date);
        System.out.println(new Date());
        System.out.println((date.getTime() - System.currentTimeMillis()) / 1000);
        date = CronHelp.getScheduleTime(cron, date);
        System.out.println(date);
        date = CronHelp.getScheduleTime(cron, date);
        System.out.println(date);
        date = CronHelp.getScheduleTime(cron, date);
        System.out.println(date);
        date = CronHelp.getScheduleTime(cron, date);
        System.out.println(date);
        date = CronHelp.getScheduleTime(cron, date);
        System.out.println(date);

    }

}
