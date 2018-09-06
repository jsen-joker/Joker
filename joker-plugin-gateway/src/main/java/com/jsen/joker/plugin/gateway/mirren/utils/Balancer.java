package com.jsen.joker.plugin.gateway.mirren.utils;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public interface Balancer<T> {
    /**
     * 决策均衡
     * @return
     */
    T balance();

    /**
     * hash均衡等
     * @param key
     * @return
     */
    T balance(String key);
}
