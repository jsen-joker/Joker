package com.jsen.redis.schedule.master.task.sk;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public abstract class IJob {

    protected abstract void _exec() throws Exception;
    public void exec() {
        try {
            _exec();
            if (lifeCycle != null) {
                lifeCycle.complete();
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                if (lifeCycle != null) {
                    lifeCycle.stop();
                }
            }
            e.printStackTrace();
        }
    }

    protected abstract void _stop();
    public void stop() {
        _stop();
        if (lifeCycle != null) {
            lifeCycle.stop();
        }
    }


    private LifeCycle lifeCycle;

    public IJob setLifeCycle(LifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
        return this;
    }

    public interface LifeCycle {
        void complete();
        void stop();
    }
}
