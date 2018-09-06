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
}
