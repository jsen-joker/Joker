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
 * @since 2018/5/21
 */
public class ComposeFutureTest {
    @Test
    public void test() {
        List<Future> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int ii = i;
            Future future = Future.future();
            futureList.add(future);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    future.fail(e);
                }
                if (!future.isComplete()) {
                    System.out.println("Threan " + ii + "exec");
                    if (ii == 5) {
                        future.tryFail("ERROR");
                    } else {
                        future.tryComplete();
                    }
                }

            }).start();
        }

        CompositeFuture.any(futureList).setHandler(ar -> {
            for (Future future: futureList) {
                future.tryFail("FORCE STOP");
            }
            System.out.println(ar.succeeded());
        });
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
