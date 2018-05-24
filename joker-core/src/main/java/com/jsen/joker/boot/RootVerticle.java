package com.jsen.joker.boot;


import com.jsen.joker.boot.cloader.context.EnterContext;
import com.jsen.joker.boot.entity.Entry;
import com.jsen.joker.boot.joker.context.ContextChanger;
import com.jsen.joker.boot.joker.context.EntryManager;
import io.vertx.core.*;
import io.vertx.core.impl.FutureFactoryImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
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

    private EnterContext enterContext;
    private EntryManager entryManager;

    @Override
    public void start(Future<Void> startFuture) {
        enterContext = new EnterContext();
        entryManager = new EntryManager();

        /*

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Future<Void> future = Future.future();
            exit(future);
            future.setHandler(ar -> {
                logger.error("ctrl c shutdown clean vertx distribution");
            });
        }));*/

        // 创建一个信号处理器
        sun.misc.SignalHandler handler = signal -> {
            Future<Void> future = Future.future();
            exit(future);
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    logger.info("succeed ctrl c shutdown clean vertx distribution");
                    System.exit(0);
                } else {
                    ar.cause().printStackTrace();
                    logger.error("failed ctrl c shutdown clean vertx distribution");
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
                                logger.debug("停止模块：" + e.getEntryClass() + "成功");
                            } else {
                                logger.debug("停止模块：" + e.getEntryClass() + "失败");
                            }
                            entryManager.succeedStopEntry(e);
                            future.complete();
                        });
                    } else {
                        future.complete();
                    }
                    return future;
                }).collect(Collectors.toList())).setHandler(cfa -> {
                    logger.debug("priority :" + entry.getKey() + " group unDeploy finished");
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
                        logger.debug("停止模块：" + id + "成功");
                    } else {
                        logger.debug("停止模块：" + id + "失败");
                    }
                    future.complete();
                });
                return future;
            }).collect(Collectors.toList())).setHandler(cfa -> {
                logger.debug("undeploy entry by deployID finished");
                result.complete();
            });
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

                    entryList.add(new Entry(enterClass, dO,
                            EntryManager.STATE.STARTING, entry.groupId, entry.artifactId, entry.version,
                            entry.fileEntry.file.getName(), false, priority));
                }
            } else {
                entryList.add(new Entry(entry.fileEntry.file.getAbsolutePath(), new DeploymentOptions(),
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
                        vertx.getDelegate().deployVerticle(enterContext.getVerticleClazz(e.getEntryClass()),
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
                    logger.debug("priority :" + entry.getKey() + " group deploy finished");
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
        clearAllEntry().setHandler(res -> {
            logger.debug("stop all not core plugin succeed");
            vertx.undeploy(selfId, ar -> {
                if (ar.succeeded()) {
                    logger.debug("stop core plugin succeed");
                } else {
                    ar.cause().printStackTrace();
                }
                future.complete();
            });
        });
    }
    public JsonArray allEntries() {
        JsonArray array = new JsonArray();
        entryManager.getEntryList().forEach(entry -> {
            array.add(entry.toJson());
        });
        return array;
    }
}
