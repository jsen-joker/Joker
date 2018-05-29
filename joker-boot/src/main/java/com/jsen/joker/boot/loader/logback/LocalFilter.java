package com.jsen.joker.boot.loader.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/28
 */
public abstract class LocalFilter {
    public abstract void filter(ILoggingEvent iLoggingEvent);
}
