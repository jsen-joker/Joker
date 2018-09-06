package com.jsen.joker.plugin.gateway.mirren.lifecycle;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public interface Hock<CONTEXT, REGEX, TARGET, CYCLE> {
    boolean handle(CONTEXT context, REGEX regex, TARGET target, CYCLE cycle);
}
