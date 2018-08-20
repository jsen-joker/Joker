package com.jsen.joker.core.plugin.manager;

import com.jsen.joker.boot.RootVerticle;
import com.jsen.joker.boot.joker.Config;
import com.jsen.joker.core.plugin.manager.utils.Downloader;
import com.jsen.joker.core.plugin.manager.utils.EntryStructureAnalyse;
import com.jsen.test.common.RestVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/19
 */
public class JokerCoreManager extends RestVerticle {

    private static final String GET_METRICS = "joker.manager.master.get.monitor.metrics";
    private static final String GET_LOG = "joker.manager.master.get.monitor.logs";
    private static final String GET_HB = "joker.manager.master.get.monitor.hb";
    private static final String REMOVE_HB = "joker.manager.master.get.monitor.hb.remove";
    // private static final String GET_DISCOVERY = "joker.manager.master.get.monitor.discovery";
    private static final String GET_ENTRIES = "joker.manager.master.get.monitor.entries";

    private static final String ADD_JAR = "joker.manager.master.get.monitor.add.jar";
    private static final String DEL_JAR = "joker.manager.master.get.monitor.del.jar";
    private static final String JAR_LIST = "joker.manager.master.get.monitor.jar.list";

    private static final Logger logger = LoggerFactory.getLogger(JokerCoreManager.class);

    WebClient webClient;

    private Downloader downloader;
    private MetricsService service;
    // private ServiceDiscoveryManager serviceDiscoveryManager;

    private String selfID;
    private String localAddress = "";

    private boolean isStopped = false;
    public JokerCoreManager() {
        selfID = RootVerticle.getDefaultRootVerticle().getSelfId();
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        InetAddress inetAddress = getLocalHostLANAddress();
        if (inetAddress != null) {
            localAddress = inetAddress.getHostAddress();
        }

        // downloader port
        int port = config().getInteger("manager.port", config().getInteger("port", 9091));

        /*
        用于 socket js 连接 获取 服务的统计信息
         */
        /*
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.monitor.metrics"))
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.log"))
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.discovery"))
                .addInboundPermitted(new PermittedOptions().setAddress("joker.manager.master.get.hb"))
                .addOutboundPermitted(new PermittedOptions().setAddress("events.log"));*/

        // sockJSHandler.bridge(options);

        webClient = WebClient.create(vertx);
        service = MetricsService.create(vertx);
        downloader = new Downloader(this, webClient);
        // serviceDiscoveryManager = new ServiceDiscoveryManager(serviceDiscovery);


        // send metrics message to the event bus
        /*
        vertx.setPeriodic(10000, t -> {
            JsonObject metrics = service.getMetricsSnapshot(vertx);
            vertx.eventBus().send("to.joker.manager.master.monitor.metrics", metrics);
        });*/

        // router.route("/eventbus/*").handler(sockJSHandler);
        router.route("/status/stop").handler(this::coreStop);

        // router.route("/entry/remove/:groupId/:artifactId/:version").handler(this::remove);
        // router.route("/entry/add/:groupId/:artifactId/:version").handler(this::add);

        router.route("/entry/analyse").handler(this::ana);

        // ServiceDiscoveryRestEndpoint.create(router, serviceDiscovery);

        registerManager();

        startServer(startFuture, port, "localhost");

    }

    private void registerManager() {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(localAddress + "-" + selfID + GET_METRICS, h -> h.reply(service.getMetricsSnapshot(vertx)));
        eventBus.consumer(localAddress + "-" + selfID + GET_LOG, this::listLogs);
        eventBus.consumer(localAddress + "-" + selfID + GET_ENTRIES, h -> h.reply(RootVerticle.getDefaultRootVerticle().allEntries()));

        eventBus.consumer(localAddress + "-" + selfID + ADD_JAR, this::addJar);
        eventBus.consumer(localAddress + "-" + selfID + DEL_JAR, this::delJar);
        eventBus.consumer(localAddress + "-" + selfID + JAR_LIST, this::jarList);


        vertx.setPeriodic(10000, t -> {
            if (!isStopped) {
                // logger.debug("register self by event bus:" + selfID);
                vertx.eventBus().send(GET_HB, localAddress + "-" + selfID);
            }
        });
    }

    private void coreStop(RoutingContext routingContext) {
        routingContext.response().end("0");
        StaticStop.stop();
    }


    private void delJar(Message<Object> m) {
        if (m.body() == null) {
            m.reply("need file name");
            return;
        }
        String fileName = m.body().toString();
        File jarFile = new File(new File(Config.projectRoot, "entry"), fileName);

        if (!jarFile.exists() || !jarFile.isFile()) {
            m.reply("文件不存在");
            return;
        }

        vertx.fileSystem().delete(jarFile.getAbsolutePath(), a -> {
            if (a.succeeded()) {
                m.reply("ok");
            } else {
                m.reply(a.cause().getMessage());
            }
        });
    }

    private void jarList(Message<Object> m) {
        File entryRoot = new File(Config.projectRoot, "entry");
        String[] fs = entryRoot.list();
        if (fs == null) {
            m.reply(new JsonArray());
        } else {
            JsonArray array = new JsonArray();
            for (String f : fs) {
                array.add(f);
            }
            m.reply(array);
        }
    }


    private void addJar(Message<Object> m) {
        if (m.body() == null) {
            m.reply("need file name");
            return;
        }
        String fileName = m.body().toString();
        File jarFile = new File(new File(Config.projectRoot, "entry"), fileName);
        if (jarFile.exists()) {
            m.reply("文件存在");
            return;
        }
        Future<Void> result=Future.future();

        downloader.loadData(fileName, jarFile, result);

        result.setHandler(a -> {
            if (a.succeeded()) {
                m.reply("ok");
            } else {
                m.reply(a.cause().getMessage());
            }
        });
    }


    @Deprecated
    private void ana(RoutingContext routingContext) {
        resultData(routingContext, new JsonObject().put("code", 0).put("data", EntryStructureAnalyse.parser()));
    }

    private void listLogs(Message<Object> m) {
        File root = new File(Config.projectRoot, "logs");
        String[] files = root.list();
        JsonArray result = new JsonArray();
        if (files != null) {
            for (String f:files) {
                if (f.endsWith(".log") || f.endsWith(".zip")) {
                    result.add(f);
                }
            }
        }
        m.reply(result);
    }

    private InetAddress getLocalHostLANAddress() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Stop the verticle.<p>
     * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
     * If your verticle does things in its shut-down which take some time then you can override this method
     * and call the stopFuture some time later when clean-up is complete.
     *
     * @param stopFuture a future which should be called when verticle clean-up is complete.
     * @throws Exception
     */
    @Override
    public void stop(Future<Void> stopFuture) {
        isStopped = true;
        webClient.close();
        logger.debug("SEND REMOVE ITEM");


        vertx.eventBus().send(REMOVE_HB, localAddress + "-" + selfID, r -> {
            if (r.succeeded()) {
                logger.debug("SEND REMOVE ITEM reply succeed");
            } else {
                logger.debug("SEND REMOVE ITEM reply failed");
                logger.debug(r.cause().getMessage());
            }

            try {
                super.stop(stopFuture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
