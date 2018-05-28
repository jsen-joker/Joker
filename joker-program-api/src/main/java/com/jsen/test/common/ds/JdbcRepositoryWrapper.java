package com.jsen.test.common.ds;

import com.jsen.test.common.service.ServiceBase;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Helper and wrapper class for JDBC repository services.
 *
 * @author Eric Zhao
 */
public class JdbcRepositoryWrapper extends ServiceBase {

  protected JDBCClient client;
  private static final Logger logger = LoggerFactory.getLogger(JdbcRepositoryWrapper.class);

  public JdbcRepositoryWrapper(String name, Vertx vertx, JsonObject config) {
    super(name);
    this.client = JDBCClient.createNonShared(io.vertx.reactivex.core.Vertx.newInstance(vertx), config);
  }

  // update

  /**
   * Suitable for `add`, `exists` operation.
   *
   * @param params        query params
   * @param sql           sql
   * @param resultHandler async result handler
   */
  protected void executeNoResult(JsonArray params, String sql, Handler<AsyncResult<Void>> resultHandler) {
    client.getConnection(connHandler(resultHandler, connection -> {
      connection.updateWithParams(sql, params, r -> {
        if (r.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(r.cause()));
        }
        connection.close();
      });
    }));
  }


  protected void update(JsonArray params, String sql, Handler<AsyncResult<Integer>> resultHandler) {
    client.getConnection(connHandler(resultHandler, connection -> {
      connection.updateWithParams(sql, params, r -> {
        if (r.succeeded()) {
          resultHandler.handle(Future.succeededFuture(r.result().getUpdated()));
        } else {
          resultHandler.handle(Future.failedFuture(r.cause()));
        }
        connection.close();
      });
    }));
  }

  // insert
  protected void insert(JsonArray params, String sql, Handler<AsyncResult<Integer>> resultHandler) {
    this.update(params, sql, resultHandler);
  }

  // delete


  protected void delete(JsonArray params, String sql, Handler<AsyncResult<Integer>> resultHandler) {
    this.update(params, sql, resultHandler);
  }

  // list

  protected Future<List<JsonObject>> list(String sql) {
    Future<List<JsonObject>> future = Future.future();
    getConnection().subscribe(connection ->connection.query(sql, r -> {
      if (r.succeeded()) {
        future.complete(r.result().getRows());
      } else {
        future.fail(r.cause());
      }
      connection.close();
    }), future::fail);
    return future;
    /*
    return getConnection().compose(connection -> {
      Future<List<JsonObject>> future = Future.future();
      connection.query(sql, r -> {
        if (r.succeeded()) {
          future.complete(r.result().getRows());
        } else {
          future.fail(r.cause());
        }
        connection.close();
      });
      return future;
    });
    */
  }
  protected <K> Future<Optional<JsonObject>> findOne(K param, String sql) {
    Future<Optional<JsonObject>> future = Future.future();
    getConnection().subscribe(connection ->connection.queryWithParams(sql, new JsonArray().add(param), r -> {
      if (r.succeeded()) {
        List<JsonObject> resList = r.result().getRows();
        if (resList == null || resList.isEmpty()) {
          future.complete(Optional.empty());
        } else {
          future.complete(Optional.of(resList.get(0)));
        }
      } else {
        future.fail(r.cause());
      }
    }), future::fail);
    return future;
    /*
    return getConnection()
            .compose(connection -> {
              Future<Optional<JsonObject>> future = Future.future();
              connection.queryWithParams(sql, new JsonArray().add(param), r -> {
                if (r.succeeded()) {
                  List<JsonObject> resList = r.result().getRows();
                  if (resList == null || resList.isEmpty()) {
                    future.complete(Optional.empty());
                  } else {
                    future.complete(Optional.of(resList.get(0)));
                  }
                } else {
                  future.fail(r.cause());
                }
                connection.close();
              });
              return future;
            });
            */
  }
  protected Future<Optional<JsonObject>> findOne(JsonArray param, String sql) {
    Future<Optional<JsonObject>> future = Future.future();
    getConnection()
            .subscribe(connection ->connection.queryWithParams(sql, param, r -> {
      if (r.succeeded()) {
        List<JsonObject> resList = r.result().getRows();
        if (resList == null || resList.isEmpty()) {
          future.complete(Optional.empty());
        } else {
          future.complete(Optional.of(resList.get(0)));
        }
      } else {
        future.fail(r.cause());
      }
    }), future::fail);
    return future;
    /*
    return getConnection()
            .compose(connection -> {
              Future<Optional<JsonObject>> future = Future.future();
              connection.queryWithParams(sql, new JsonArray().add(param), r -> {
                if (r.succeeded()) {
                  List<JsonObject> resList = r.result().getRows();
                  if (resList == null || resList.isEmpty()) {
                    future.complete(Optional.empty());
                  } else {
                    future.complete(Optional.of(resList.get(0)));
                  }
                } else {
                  future.fail(r.cause());
                }
                connection.close();
              });
              return future;
            });
            */
  }

  /**
   * Suitable for `add`, `exists` operation.
   *
   * @param params        query params
   * @param sql           sql
   * @param resultHandler async result handler
   */
  protected void executeReturnEff(JsonArray params, String sql, Handler<AsyncResult<Integer>> resultHandler) {
    client.getConnection(connHandler(resultHandler, connection -> {
      connection.updateWithParams(sql, params, r -> {
        if (r.succeeded()) {
          resultHandler.handle(Future.succeededFuture(r.result().getUpdated()));
        } else {
          resultHandler.handle(Future.failedFuture(r.cause()));
        }
        connection.close();
      });
    }));
  }

  protected <R> void execute(JsonArray params, String sql, R ret, Handler<AsyncResult<R>> resultHandler) {
    client.getConnection(connHandler(resultHandler, connection -> {
      connection.updateWithParams(sql, params, r -> {
        if (r.succeeded()) {
          resultHandler.handle(Future.succeededFuture(ret));
        } else {
          resultHandler.handle(Future.failedFuture(r.cause()));
        }
        connection.close();
      });
    }));
  }

  protected <K> Future<Optional<JsonObject>> retrieveOne(K param, String sql) {
    Future<Optional<JsonObject>> future = Future.future();
    getConnection().subscribe(connection -> connection.queryWithParams(sql, new JsonArray().add(param), r -> {
      if (r.succeeded()) {
        List<JsonObject> resList = r.result().getRows();
        if (resList == null || resList.isEmpty()) {
          future.complete(Optional.empty());
        } else {
          future.complete(Optional.of(resList.get(0)));
        }
      } else {
        future.fail(r.cause());
      }
    }), future::fail);
    return future;
    /*
    return getConnection()
      .compose(connection -> {
        Future<Optional<JsonObject>> future = Future.future();
        connection.queryWithParams(sql, new JsonArray().add(param), r -> {
          if (r.succeeded()) {
            List<JsonObject> resList = r.result().getRows();
            if (resList == null || resList.isEmpty()) {
              future.complete(Optional.empty());
            } else {
              future.complete(Optional.of(resList.get(0)));
            }
          } else {
            future.fail(r.cause());
          }
          connection.close();
        });
        return future;
      });
      */
  }

  protected int calcPage(int page, int limit) {
    if (page <= 0) {
      return 0;
    }
    return limit * (page - 1);
  }

  protected Future<List<JsonObject>> retrieveByPage(int page, int limit, String sql) {
    JsonArray params = new JsonArray().add(calcPage(page, limit)).add(limit);
    Future<List<JsonObject>> future = Future.future();
    getConnection().subscribe(connection -> connection.queryWithParams(sql, params, r -> {
      if (r.succeeded()) {
        future.complete(r.result().getRows());
      } else {
        future.fail(r.cause());
      }
      connection.close();
    }), future::fail);
    return future;
    /*

    JsonArray params = new JsonArray().add(calcPage(page, limit)).add(limit);
    return getConnection().compose(connection -> {
      Future<List<JsonObject>> future = Future.future();
      connection.queryWithParams(sql, params, r -> {
        if (r.succeeded()) {
          future.complete(r.result().getRows());
        } else {
          future.fail(r.cause());
        }
        connection.close();
      });
      return future;
    });
    */
  }

  protected Future<List<JsonObject>> retrieveMany(JsonArray param, String sql) {
    Future<List<JsonObject>> future = Future.future();
    getConnection().subscribe(connection -> connection.queryWithParams(sql, param, r -> {
      if (r.succeeded()) {
        future.complete(r.result().getRows());
      } else {
        future.fail(r.cause());
      }
      connection.close();
    }), future::fail);
    return future;
    /*
    return getConnection().compose(connection -> {
      Future<List<JsonObject>> future = Future.future();
      connection.queryWithParams(sql, param, r -> {
        if (r.succeeded()) {
          future.complete(r.result().getRows());
        } else {
          future.fail(r.cause());
        }
        connection.close();
      });
      return future;
    });*/
  }

  protected Future<List<JsonObject>> retrieveAll(String sql) {
    Future<List<JsonObject>> future = Future.future();
    getConnection().subscribe(connection -> connection.query(sql, r -> {
      if (r.succeeded()) {
        future.complete(r.result().getRows());
      } else {
        future.fail(r.cause());
      }
      connection.close();
    }), future::fail);
    return future;
    /*
    return getConnection().compose(connection -> {
      Future<List<JsonObject>> future = Future.future();
      connection.query(sql, r -> {
        if (r.succeeded()) {
          future.complete(r.result().getRows());
        } else {
          future.fail(r.cause());
        }
        connection.close();
      });
      return future;
    });*/
  }

  protected <K> void removeOne(K id, String sql, Handler<AsyncResult<Void>> resultHandler) {
    client.getConnection(connHandler(resultHandler, connection -> {
      JsonArray params = new JsonArray().add(id);
      connection.updateWithParams(sql, params, r -> {
        if (r.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(r.cause()));
        }
        connection.close();
      });
    }));
  }

  protected void removeAll(String sql, Handler<AsyncResult<Void>> resultHandler) {
    client.getConnection(connHandler(resultHandler, connection -> {
      connection.update(sql, r -> {
        if (r.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(r.cause()));
        }
        connection.close();
      });
    }));
  }

  /**
   * A helper methods that generates async handler for SQLConnection
   *
   * @return generated handler
   */
  protected <R> Handler<AsyncResult<SQLConnection>> connHandler(Handler<AsyncResult<R>> h1, Handler<SQLConnection> h2) {
    return conn -> {
      if (conn.succeeded()) {
        final SQLConnection connection = conn.result();
        h2.handle(connection);
      } else {
        h1.handle(Future.failedFuture(conn.cause()));
      }
    };
  }

  /*
  protected Future<SQLConnection> getConnection() {
    Future<SQLConnection> future = Future.future();
    client.getConnection(future.completer());
    return future;
  }*/

  protected Single<SQLConnection> getConnection() {
    return client.rxGetConnection().flatMap(conn -> Single.just(conn).doFinally(conn::close));
  }


  // help param
  protected JsonArray buildParams() {
    return new JsonArray();
  }
}
