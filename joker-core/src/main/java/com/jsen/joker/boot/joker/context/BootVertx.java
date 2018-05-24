package com.jsen.joker.boot.joker.context;

import com.jsen.joker.boot.joker.Config;
import com.jsen.joker.boot.RootVerticle;
import com.jsen.joker.boot.utils.FileSystemDetector;
import com.jsen.joker.boot.utils.Regex;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.impl.VertxMetricsFactoryImpl;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     vertx
 * </p>
 *
 * @author jsen
 * @since 2018/5/18
 */
public class BootVertx {
    private static final Logger logger = LoggerFactory.getLogger(BootVertx.class);

    public Future<Void> boot() {
        logger.info(">>> start create joker container <<<");
        logger.info(">>> start init joker cluster <<<");
        Future<Void> future = Future.future();

        String cluser = Config.confs.get("core.cluster").toString();
        if (cluser != null && "yes".equals(cluser.toLowerCase())) {
            String clstuerManager = "io.vertx.spi.cluster.hazelcast.HazelcastClusterManager";
            if (Config.confs.containsKey("core.cluster.manager")) {
                clstuerManager = Config.confs.get("core.cluster.manager").toString();
            }
            ClassLoader classLoader = this.getClass().getClassLoader();
            ClusterManager mgr;
            try {
                Class<?> cz = classLoader.loadClass(clstuerManager);
                try {
                    mgr = (ClusterManager) cz.newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                    logger.error("create clusterManager:" + clstuerManager + " failed, turn to single mode");
                    Config.confs.put("core.cluster", "no");
                    return boot();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                logger.error("can not find clusterManager:" + clstuerManager + ", turn to single mode");
                Config.confs.put("core.cluster", "no");
                return boot();
            }

            VertxOptions options = new VertxOptions().setClusterManager(mgr);
            options.setClustered(true);
            try {
                InetAddress inetAddress = getLocalHostLANAddress();
                if (inetAddress == null) {
                    throw new UnknownHostException("无法获取IP地址");
                }
                options.setClusterHost(inetAddress.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
                logger.error("failed to find local host, turn to single mode");
                Config.confs.put("core.cluster", "no");
                return boot();
            }
            DropwizardMetricsOptions dropwizardMetricsOptions = new DropwizardMetricsOptions();
            dropwizardMetricsOptions.setEnabled(true).setJmxDomain("vertx").setJmxEnabled(true).addMonitoredHttpClientEndpoint(
                    new Match().setValue(".*").setType(MatchType.REGEX)).
                    addMonitoredEventBusHandler(
                            new Match().setValue(".*").setType(MatchType.REGEX));
            dropwizardMetricsOptions.setFactory(new VertxMetricsFactoryImpl());

            // options.setEventLoopPoolSize(16);
            options.setMetricsOptions(dropwizardMetricsOptions);


            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    logger.info("<<< init joker cluster succeed >>>");

                    Vertx vertx = res.result();
                    logger.error(vertx.getDelegate().getClass().getName());
                    startVertx(vertx, future);
                } else {
                    logger.error("<<< init joker cluster failed >>>");
                    logger.error(res.cause().getMessage());
                    // failed!
                    future.fail(res.cause());
                }
            });

        } else {
            VertxOptions vO = new VertxOptions();
            // vO.setEventLoopPoolSize(16);

            DropwizardMetricsOptions dropwizardMetricsOptions = new DropwizardMetricsOptions();
            dropwizardMetricsOptions.setEnabled(true).setJmxDomain("vertx").setJmxEnabled(true).addMonitoredHttpClientEndpoint(
                    new Match().setValue(".*").setType(MatchType.REGEX)).
                    addMonitoredEventBusHandler(
                            new Match().setValue(".*").setType(MatchType.REGEX));
            dropwizardMetricsOptions.setFactory(new VertxMetricsFactoryImpl());

            // options.setEventLoopPoolSize(16);
            vO.setMetricsOptions(dropwizardMetricsOptions);
            Vertx vertx = Vertx.vertx(vO);
            startVertx(vertx, future);
        }


        return future;
    }

    private void startVertx(Vertx vertx, Future<Void> future) {

        RootVerticle rootVerticle = new RootVerticle();

        // vertx.setPeriodic(10000, ContextDetect::detect);

        DeploymentOptions dO = new DeploymentOptions();
        dO.setInstances(1);
        JsonObject config = new JsonObject();
        for (Map.Entry<String, Object> entry: Config.confs.entrySet()) {
            config.put(entry.getKey(), entry.getValue());
        }
        dO.setConfig(config);

        RxHelper.deployVerticle(vertx, rootVerticle, dO).subscribe(id -> {
                    logger.info("<<< joker container start succeed >>>");
                    logger.info(">>> start first load entry <<<");

                    FileSystemDetector fileSystemDetector = new FileSystemDetector(new ContextChanger(Config.projectRoot), new File(Config.projectRoot, "entry"), Regex.tailDetectFilter);

                    RootVerticle.getDefaultRootVerticle().setSelfId(id);

                    fileSystemDetector.detect();
                    vertx.setPeriodic(10000, t -> fileSystemDetector.detect());

                    /*
                    List<ContextDetect.Entry> list = listAllEntry();
                    ContextDetect.setLastEntryJars(list);
                    List<File> currentJarFiles = list.stream().filter(item -> !item.isScript).map(item -> item.file).collect(Collectors.toList());
                    EnterContext.getDefaultEnterContext().reloadJars(currentJarFiles);
                    RootVerticle.getDefaultRootVerticle().loadEntry(list).setHandler(ar -> {
                        if (ar.succeeded()) {
                            logger.info(id);
                            logger.info("<<< create joker app succeed >>>");
                        } else {
                            logger.error("<<< create joker app failed >>>");
                            logger.error(ar.cause().getMessage());
                        }
                        future.complete();
                    });*/
                },
                e -> {
                    logger.info("<<< joker container start failed >>>");
                    logger.error(e.getMessage());
                    future.fail(e);
                });
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
}
