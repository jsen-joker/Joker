import io.vertx.core.json.JsonObject;
import org.junit.Test;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/16
 */
public class DumpConfig {
    // 应该从配置文件读取
    private static JsonObject globalConfig = new JsonObject();
    static {
        globalConfig.put("service1", new JsonObject().put("http.port", 9010).put("endpoint", "/service001").put("app.name", "service001"));
        globalConfig.put("service2", new JsonObject().put("http.port", 9011).put("endpoint", "/service002").put("app.name", "service002"));
        globalConfig.put("user", new JsonObject().put("http.port", 9012).put("endpoint", "/user").put("app.name", "user"));
        globalConfig.put("job", new JsonObject().put("http.port", 9013).put("endpoint", "/job").put("app.name", "job001"));
        globalConfig.put("project_server", new JsonObject().put("http.port", 9014).put("endpoint", "/project/server").put("app.name", "project-server"));
        globalConfig.put("s_gateway", new JsonObject().put("http.port", 8088));

        globalConfig.put("mysql1", new JsonObject().put("url", "jdbc:mysql://localhost/jtest002?characterEncoding=UTF-8&useSSL=false")
                .put("driver_class", "com.mysql.cj.jdbc.Driver").put("user", "root").put("password", ""));
    }
    @Test
    public void dump() {
        System.out.println(globalConfig.encodePrettily());
    }
}
