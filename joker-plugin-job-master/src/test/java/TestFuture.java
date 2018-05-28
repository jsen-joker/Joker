import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/26
 */
public class TestFuture {
    @Test
    public void test() {
        List<Future>futures = new ArrayList<>();
        CompositeFuture.all(futures).setHandler(r -> {
            if (r.succeeded()) {
                System.out.println("s");
            } else {
                System.out.println(r.cause().getMessage());
            }
        });
    }
}
