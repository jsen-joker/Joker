package com.jsen.joker.boot.loader.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * <p>
 *     添加log filter
 *     注意在模块卸载的时候移除filter，不然会发生异常
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