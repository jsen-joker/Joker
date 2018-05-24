import com.jsen.joker.core.plugin.manager.JokerCoreManager;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/19
 */
public class TestBoot {

    public static void main(String[] args) {

        VertxOptions vO = new VertxOptions();
        vO.setEventLoopPoolSize(16);

        Vertx vertx = Vertx.vertx(vO);

        vertx.deployVerticle(new JokerCoreManager(), ar -> {
            if (ar.succeeded()) {
                System.err.println(ar.result());
            } else {
                System.err.println("failed:" + ar.cause().getMessage());
            }
        });
    }
}
