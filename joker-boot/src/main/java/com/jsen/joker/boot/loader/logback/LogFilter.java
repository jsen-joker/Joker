package com.jsen.joker.boot.loader.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/28
 */
public class LogFilter extends Filter<ILoggingEvent> {
    public static final List<LocalFilter> localFilterChannel = Lists.newArrayList();
    @Override
    public FilterReply decide(ILoggingEvent event) {
        for (LocalFilter localFilter : localFilterChannel) {
            localFilter.filter(event);
        }
        return FilterReply.ACCEPT;
    }
}