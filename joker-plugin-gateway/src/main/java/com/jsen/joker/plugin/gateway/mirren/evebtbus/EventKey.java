package com.jsen.joker.plugin.gateway.mirren.evebtbus;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public interface EventKey {
    interface App {
        String DEPLOY = "APP:DEPLOY";
        String UNDEPLOY = "APP:UNDEPLOY";

        interface Api {
            String ADD = "APP:API:ADD";
            String DEL = "APP:API:DEL";
        }
    }
    interface System {
        String SYSTEM_PLUS_ERROR = "SYS:PLUS:ERROR";
        String SYSTEM_API_REQUEST = "SYS:API:REQUEST";
        String SYSTEM_HTTP_REQUEST_SR = "SYS:HTTP:REQUEST:SR";
        String SYSTEM_HTTP_REQUEST_SS = "SYS:HTTP:REQUEST:SS";
    }
}
