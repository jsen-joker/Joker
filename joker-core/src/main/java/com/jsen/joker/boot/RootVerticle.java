package com.jsen.joker.boot;


import com.jsen.joker.boot.cloader.context.EntryContext;
import com.jsen.joker.boot.entity.Entry;
import com.jsen.joker.boot.joker.context.ContextChanger;
import com.jsen.joker.boot.joker.context.EntryManager;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *     joker 入口entry，
 *     该模块放在lib目录下，不在entry目录
 * </p>
 *
 * @author jsen
 * @since 2018/5/10
 */
public class RootVerticle extends io.vertx.reactivex.core.AbstractVerticle {

    private String selfId = "";
    private static RootVerticle rootVerticle;
    public static RootVerticle getDefaultRootVerticle() {
        return rootVerticle;
    }


    public RootVerticle() {
        rootVerticle = this;
    }

    public RootVerticle setSelfId(String selfId) {
        this.selfId = selfId;
        return this;
    }

    public String getSelfId() {
        return selfId;
    }

    private static final Logger logger = LoggerFactory.getLogger(RootVerticle.class);

    private EntryContext entryContext;
    private EntryManager entryManager;

    @Override
    public void start(Future<Void> startFuture) {
        entryContext = new EntryContext();
        entryManager = new EntryManager();

        /*
        创建一个信号处理器
         */
        sun.misc.SignalHandler handler = signal -> {
            Future<Void> future = Future.future();
            exit(future);
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    logger.info("ctrl c 完成停止Joker节点");
                    System.exit(0);
                } else {
                    ar.cause().printStackTrace();
                    logger.error("ctrl c 停止Joker节点失败");
                    System.exit(1);
                }
            });
        };
        // 设置INT信号(Ctrl+C中断执行)交给指定的信号处理器处理，废掉系统自带的功能
        sun.misc.Signal.handle(new sun.misc.Signal("INT"), handler);

        startFuture.complete();
    }
    /**
     * 按优先级卸载所有的Entry
     * @return 完成回调
     */
    public Future<Void> clearAllEntry() {

        Future<Void> start = Future.future();
        Future<Void> current = start;
        TreeMap<Integer, List<Entry>> uGs = entryManager.undeployGroup();
        for (Map.Entry<Integer, List<Entry>> entry: uGs.entrySet()) {
            List<Entry> gList = entry.getValue();

            current = current.compose(a -> {
                Future<Void> r = Future.future();
                CompositeFuture.all(gList.stream().map(e -> {
                    Future future = Future.future();
                    if (e.getState() == EntryManager.STATE.UP) {
                        vertx.undeploy(e.getDeploymentID(), ar -> {
                            if (ar.succeeded()) {
                                logger.info("停止模块：" + e.getEntryClass() + "成功");
                            } else {
                                logger.error("停止模块：" + e.getEntryClass() + "失败");
                            }
                            entryManager.succeedStopEntry(e);
                            future.complete();
                        });
                    } else {
                        future.complete();
                    }
                    return future;
                }).collect(Collectors.toList())).setHandler(cfa -> {
                    logger.info("priority :" + entry.getKey() + " 组卸载完成");
                    r.complete();
                });
                return r;
            });

        }

        start.complete();



        Future<Void> result = Future.future();

        current.compose(a -> {
            entryManager.clearAll();
            CompositeFuture.all(vertx.deploymentIDs().stream().filter(item -> !selfId.equals(item)).map(id -> {
                Future future = Future.future();
                vertx.undeploy(id, ar -> {
                    if (ar.succeeded()) {
                        logger.info("卸载未卸载模块：" + id + "成功");
                    } else {
                        logger.error("卸载未卸载模块：" + id + "失败");
                    }
                    future.complete();
                });
                return future;
            }).collect(Collectors.toList())).setHandler(cfa -> {
                logger.info("通过deploymentIDs卸载模块完成");
                result.complete();
            });
        }, result);
        return result;
    }

    public Future<Void> clearEntries(List<String> entries) {
        List<Entry> entryList = entryManager.getEntryList().stream().filter(item -> entries.contains(item.getFilePath()))
                .collect(Collectors.toList());
        TreeMap<Integer, List<Entry>> uGs = entryManager.undeployGroupPart(entryList);



        Future<Void> start = Future.future();
        Future<Void> current = start;
        for (Map.Entry<Integer, List<Entry>> entry: uGs.entrySet()) {
            List<Entry> gList = entry.getValue();

            current = current.compose(a -> {
                Future<Void> r = Future.future();
                CompositeFuture.all(gList.stream().map(e -> {
                    Future future = Future.future();
                    if (e.getState() == EntryManager.STATE.UP) {
                        vertx.undeploy(e.getDeploymentID(), ar -> {
                            if (ar.succeeded()) {
                                logger.info("停止模块：" + e.getEntryClass() + "成功");
                            } else {
                                logger.error("停止模块：" + e.getEntryClass() + "失败");
                            }
                            entryManager.succeedStopEntry(e);
                            future.complete();
                        });
                    } else {
                        future.complete();
                    }
                    return future;
                }).collect(Collectors.toList())).setHandler(cfa -> {
                    logger.info("priority :" + entry.getKey() + " 组卸载完成");
                    r.complete();
                });
                return r;
            });

        }

        start.complete();



        Future<Void> result = Future.future();

        current.compose(a -> {
            entryManager.clearAll();
            result.complete();
        }, result);
        return result;

    }

    /**
     * 按优先级加载所有entry
     * @param entries 这里可能是增加的entry 也可能是重新加载的entry
     * @return 完成回调
     */
    public Future<Void> loadEntries(List<ContextChanger.Entry> entries) {
        List<Entry> entryList = new ArrayList<>();
        entries.forEach(entry -> {
            if (!entry.isScript) {
                JsonArray all = entry.entries;
                for (int i = 0; i < all.size(); i++) {
                    JsonObject item = all.getJsonObject(i);

                    String enterClass = item.getString("value");
                    JsonObject attrs = item.getJsonObject("attrs", new JsonObject());
                    int instances = attrs.getInteger("instances", 1);
                    int priority = attrs.getInteger("priority", 1);

                    DeploymentOptions dO = new DeploymentOptions();
                    dO.setConfig(config());
                    dO.setInstances(instances);

                    entryList.add(new Entry(entry.fileEntry.file.getAbsolutePath(), enterClass, dO,
                            EntryManager.STATE.STARTING, entry.groupId, entry.artifactId, entry.version,
                            entry.fileEntry.file.getName(), false, priority));
                }
            } else {
                entryList.add(new Entry(entry.fileEntry.file.getAbsolutePath(), entry.fileEntry.file.getAbsolutePath(), new DeploymentOptions(),
                        EntryManager.STATE.STARTING, "", "", "",
                        entry.fileEntry.file.getName(), true, 1));
            }
        });
        entryManager.getEntryList().addAll(entryList);

        Future<Void> start = Future.future();
        Future<Void> current = start;
        TreeMap<Integer, List<Entry>> uGs = entryManager.groupPart(entryList);
        for (Map.Entry<Integer, List<Entry>> entry: uGs.entrySet()) {
            List<Entry> gList = entry.getValue();


            current = current.compose(a -> {
                Future<Void> r = Future.future();
                CompositeFuture.all(gList.stream().map(e -> {
                    Future future = Future.future();

                    if (!e.isScript()) {
                        vertx.getDelegate().deployVerticle(entryContext.getVerticleClazz(e.getEntryClass()),
                                e.getDeploymentOptions(), ar2 -> {
                                    deployResult(future, e, e.getEntryClass(), ar2);
                                });
                    } else {
                        vertx.getDelegate().deployVerticle(e.getEntryClass(), ar2 -> {
                            deployResult(future, e, e.getEntryClass(), ar2);
                        });
                    }

                    return future;
                }).collect(Collectors.toList())).setHandler(cfa -> {
                    logger.debug("priority :" + entry.getKey() + " 组加载完成");
                    r.complete();
                });
                return r;
            });

        }
        start.complete();
        return current;

    }


    private void deployResult(Future f, Entry entry, String enterClass, AsyncResult<String> ar2) {
        if (ar2.succeeded()) {
            logger.info("部署 " + enterClass + " 模块成功...");
            String result = ar2.result();
            entryManager.succeedStartEntry(entry, result);
            logger.info(result);
        } else {
            ar2.cause().printStackTrace();
            logger.error("部署 " + enterClass + " 模块失败...");
            entryManager.failedStartEntry(entry);
            logger.error(ar2.cause().getMessage());
        }
        f.complete();
    }

    /**
     * ./joker.sh stop 调用此函数
     * @param future
     */
    public void exit(Future<Void> future) {
        if (clusterManager != null) {
            clearAllEntry().compose(r -> {
                Future<Void> future1 = Future.future();
                clusterManager.leave(future1.completer());
                return future1;
            }).setHandler(res -> {
                logger.debug("停止除了core的所有模块成功");
                stopSelf(future);
            });
        } else {
            clearAllEntry().setHandler(res -> {
                logger.debug("停止除了core的所有模块成功");
                stopSelf(future);
            });
        }
    }

    private void stopSelf(Future<Void> future) {
        vertx.undeploy(selfId, ar -> {
            if (ar.succeeded()) {
                logger.debug("停止core模块成功");
            } else {
                ar.cause().printStackTrace();
            }
            future.complete();
        });
    }

    public JsonArray allEntries() {
        JsonArray array = new JsonArray();
        entryManager.getEntryList().forEach(entry -> {
            array.add(entry.toJson());
        });
        return array;
    }

    private ClusterManager clusterManager;

    public RootVerticle setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        return this;
    }
}
