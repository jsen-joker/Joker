package com.jsen.joker.plugin.gateway.mirren;

import java.util.HashMap;

/**
 * <p>
 *     一个 route chain 代表一个 api
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class GateWayMap extends HashMap<String, ApiMap> {

    public ApiMap createGateway(String name) {
        ApiMap apiMap = new ApiMap();
        put(name, apiMap);
        return apiMap;
    }

    public ApiMap getGateway(String name) {
        ApiMap apiMap = get(name);
        if (apiMap != null) {
            return apiMap;
        }
        return createGateway(name);
    }

    public void deleteGateway(String name) {
        ApiMap apiMap = get(name);
        if (apiMap != null) {
            apiMap.destory();
        }
    }
}
