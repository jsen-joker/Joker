package com.jsen.test;

import com.jsen.test.common.ds.JdbcRepositoryWrapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/23
 */
public class DerbyTest {

    private static final Logger logger = LoggerFactory.getLogger(DerbyTest.class.getName());

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        logger.debug("start");
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(new JsonObject().put("url", "jdbc:hsqldb:file:/Users/jsen/joker_hsqldb/joker_hsqldb?shutdown=true").put("driver_class", "org.hsqldb.jdbcDriver").put("user", "").put("password", ""));
        vertx.deployVerticle(Boot.class.getName(), deploymentOptions, ar -> {
            if (ar.succeeded()) {
                System.out.println("SUCCEED");
            } else {
                ar.cause().printStackTrace();
            }
        });


    }

    public static class Boot extends AbstractVerticle {
        /**
         * Start the verticle.<p>
         * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
         * If your verticle does things in its startup which take some time then you can override this method
         * and call the startFuture some time later when start up is complete.
         *
         * @param startFuture a future which should be called when verticle start-up is complete.
         * @throws Exception
         */
        @Override
        public void start(Future<Void> startFuture) throws Exception {
            JDBCClient jdbcClient = JDBCClient.createShared(vertx, config());

            jdbcClient.getConnection(conn -> {
                if (conn.succeeded()) {
                    SQLConnection sqlConnection = conn.result();
                    sqlConnection.execute("CREATE TABLE vertx_project_server (version varchar(64),artifact_id varchar(128),group_id varchar(128),data LONGVARCHAR,uuid varchar(128),upload_time datetime,name varchar(128),enter varchar(128))", create -> {
                        if (create.failed()) {
                            create.cause().printStackTrace();
                            System.out.println("create db failed");
                        } else {
                            System.out.println("create db succeed");
                        }
                        startFuture.complete();
                    });
                    System.out.println("SUCCEED CONNECT");
                } else {
                    System.out.println("FAILED");
                }
            });
        }
    }
}
