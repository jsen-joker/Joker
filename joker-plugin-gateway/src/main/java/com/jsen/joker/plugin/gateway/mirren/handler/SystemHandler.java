package com.jsen.joker.plugin.gateway.mirren.handler;

import com.hazelcast.util.StringUtil;
import com.jsen.joker.plugin.gateway.mirren.ApplicationVerticle;
import com.jsen.joker.plugin.gateway.mirren.DeployVerticle;
import com.jsen.joker.plugin.gateway.mirren.entity.MemTrack;
import com.jsen.joker.plugin.gateway.mirren.evebtbus.EventKey;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import com.jsen.joker.plugin.gateway.mirren.model.App;
import com.jsen.joker.plugin.gateway.mirren.service.AppService;
import com.jsen.test.common.utils.response.ResponseBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *     gateway 的动态设置的 http api
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public class SystemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemHandler.class);
    private LocalDateTime start = LocalDateTime.now();


    private Router router;
    private EventBus eventBus;
    private AppService appService;

    private long requestCount = 0;
    private long requestHttpApiCount = 0;
    private long currentHttpApiProcessingCount = 0;

    public SystemHandler(String prefix, Router router, EventBus eventBus, AppService appService) {
        this.router = router;
        this.eventBus = eventBus;
        this.appService = appService;

        router.get(prefix + "info").handler(this::systemInfo);

        eventBus.consumer(EventKey.System.SYSTEM_PLUS_ERROR, this::plusError);
        eventBus.consumer(EventKey.System.SYSTEM_API_REQUEST, msg -> requestCount++);
        eventBus.consumer(EventKey.System.SYSTEM_HTTP_REQUEST_SR, msg -> {
            requestHttpApiCount++;
            currentHttpApiProcessingCount++;
        });
        eventBus.consumer(EventKey.System.SYSTEM_HTTP_REQUEST_SS, msg -> {
            if (currentHttpApiProcessingCount > 0) {
                currentHttpApiProcessingCount--;
            }
        });
    }

    private void systemInfo(RoutingContext routingContext) {

        ResponseBase result = ResponseBase.create().code(0);


        result.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        result.put("totalMemory", Runtime.getRuntime().totalMemory() / (1024 * 1024));
        result.put("freeMemory", Runtime.getRuntime().freeMemory() / (1024 * 1024));
        result.put("maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        Duration duration = Duration.between(start, LocalDateTime.now());
        result.put("duration", millisToDateTime(duration.toMillis(), "$dD $hH $mMin $sS"));
        Map<String, ApplicationVerticle> applicationVerticles = DeployVerticle.getInstance().getApplicationVerticleMap();


        result.put("appCount", applicationVerticles.size());
        result.put("apiCount", applicationVerticles.values().stream().mapToInt(item -> item.getApiMap().size()).sum());
        result.put("errorCount", errorCount);
        result.put("requestCount", requestCount);
        result.put("requestHttpApiCount", requestHttpApiCount);
        result.put("currentHttpApiProcessingCount", currentHttpApiProcessingCount);

        resultJSON(routingContext, result);
    }

    private int errorCount = 0;
    public void plusError(Message<MemTrack> msg) {
        errorCount += 1;
        if (msg.body() != null) {
            MemTrack infos = msg.body();
            LOGGER.error(MessageFormat.format("应用:{0} , API:{1} ,在执行的过程中发生了异常:{2} ,堆栈信息{3}", infos.getAppName(), infos.getApiName(),
                    infos.getMsg(), infos.getTrace()));
        }
    }

    private String millisToDateTime(long time, String pattern) {
        long day = time / 86400000;
        long hour = (time % 86400000) / 3600000;
        long minute = (time % 86400000 % 3600000) / 60000;
        long second = (time % 86400000 % 3600000 % 60000) / 1000;
        if (!pattern.contains("$y")) {
            pattern = pattern.replace("$d", Long.toString(day));
        } else {
            pattern = pattern.replace("$y", Long.toString(day / 365)).replace("$d", Long.toString(day % 365));
        }
        return pattern.replace("$h", Long.toString(hour)).replace("$m", Long.toString(minute)).replace("$s", Long.toString(second));
    }

    /**
     * 通用http数据返回 返回Object.toString()数据格式
     * @param context
     */
    protected <T> void resultData(RoutingContext context, T data) {
        context.response()
                .putHeader("content-type", "application/json")
                .end(data.toString());
    }

    /**
     * 通用http数据返回 json数据返回
     * @param context
     */
    protected void resultJSON(RoutingContext context, JsonObject data) {
        context.response().setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(data.encode());
    }


    /**
     * http错误处理，400
     * @param context
     */
    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("error", ex.getMessage()).encodePrettily());
    }

    /**
     * http错误处理，404
     * @param context
     */
    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("message", "not_found").encodePrettily());
    }

    /**
     * http错误处理，500
     * @param context
     */
    protected void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("error", ex.getMessage()).encodePrettily());
    }

    /**
     * http错误处理，501
     * @param context
     */
    protected void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("code", 1).put("message", "not_implemented").encodePrettily());
    }

}
