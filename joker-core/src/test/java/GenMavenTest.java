import com.jsen.joker.boot.utils.xml.GenMaven;
import org.junit.Test;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/20
 */
public class GenMavenTest {
    @Test
    public void test() {
        System.out.append(GenMaven.parser("/Users/jsen/Documents/GitProjects/Test/hockvertx/file-uploads/44041ea3-ffe9-41ea-96c5-bab63c563823").encodePrettily());
    }
}
