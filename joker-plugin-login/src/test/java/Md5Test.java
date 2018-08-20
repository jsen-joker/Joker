import com.jsen.test.common.utils.enc.MD5Util;
import org.junit.Test;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/20
 */
public class Md5Test {
    @Test
    public void test() {
        System.out.println(MD5Util.generate(MD5Util.MD5("7h*dKf")));
    }
}
