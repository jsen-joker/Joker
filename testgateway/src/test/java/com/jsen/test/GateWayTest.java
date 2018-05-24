package com.jsen.test;

import org.junit.Test;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/19
 */
public class GateWayTest {
    @Test
    public void testPath() {
        String path = "/login/jsen/32";
        String prefix = "/" + (path.substring(7)
                .split("/"))[0]; // /boot

        System.err.println(path.substring(7));
        System.err.println(path.substring(7));
    }
}
