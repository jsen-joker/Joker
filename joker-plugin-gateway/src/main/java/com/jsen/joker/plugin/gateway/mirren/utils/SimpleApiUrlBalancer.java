package com.jsen.joker.plugin.gateway.mirren.utils;

import com.jsen.joker.plugin.gateway.mirren.model.ApiUrl;

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
public class SimpleApiUrlBalancer implements Balancer<ApiUrl> {


    private List<String> urls;
    private int _size;
    public SimpleApiUrlBalancer(Set<ApiUrl> apiUrls) {
       urls = apiUrls.stream().map(ApiUrl::getUrl).collect(Collectors.toList());
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
    public ApiUrl balance() {
        return new ApiUrl().setUrl(urls.get(0));
    }

    /**
     * hash均衡等
     *
     * @param key
     * @return
     */
    @Override
    public ApiUrl balance(String key) {
        return new ApiUrl().setUrl(urls.get(0));
    }
}
