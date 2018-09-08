package com.jsen.joker.plugin.gateway.mirren.utils;

import com.jsen.joker.plugin.gateway.mirren.model.ApiOptionUrl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class SimpleApiUrlBalancer implements Balancer<ApiOptionUrl> {


    private List<String> urls;
    private int _size;
    public SimpleApiUrlBalancer(Set<ApiOptionUrl> apiOptionUrls) {
       urls = apiOptionUrls.stream().map(ApiOptionUrl::getUrl).collect(Collectors.toList());
       this._size = urls.size();
    }

    public int size() {
        return _size;
    }

    /**
     * 决策均衡
     *
     * @return
     */
    @Override
    public ApiOptionUrl balance() {
        return new ApiOptionUrl().setUrl(urls.get(0));
    }

    /**
     * hash均衡等
     *
     * @param key
     * @return
     */
    @Override
    public ApiOptionUrl balance(String key) {
        return new ApiOptionUrl().setUrl(urls.get(0));
    }
}
