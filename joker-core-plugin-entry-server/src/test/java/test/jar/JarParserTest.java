package test.jar;

import com.jsen.joker.core.plugin.entry.server.utils.GenMaven;
import org.junit.Test;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/17
 */

public class JarParserTest {

    @Test
    public void testParser() {
        System.out.append(GenMaven.parser("/Users/jsen/Documents/GitProjects/Test/hockvertx/file-uploads/44041ea3-ffe9-41ea-96c5-bab63c563823").encodePrettily());
    }
}
