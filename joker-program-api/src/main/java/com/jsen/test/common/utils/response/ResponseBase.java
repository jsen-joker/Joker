package com.jsen.test.common.utils.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public class ResponseBase extends JsonObject {

    private ResponseBase() {
        super();
    }
    public static ResponseBase create() {
        return new ResponseBase();
    }

    public ResponseBase code(int code) {
        put("code", code);
        return this;
    }
    public ResponseBase data(Object data) {
        put("data", data);
        return this;
    }
    public ResponseBase msg(String msg) {
        put("msg", msg);
        return this;
    }
    public Future future() {
        return Future.succeededFuture(this);
    }

    public void handle(Handler<AsyncResult<JsonObject>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(this));
    }


    @Override
    public ResponseBase put(String key, Object value) {
        super.put(key, value);
        return this;
    }
    @Override
    public ResponseBase put(String key, Integer value) {
        super.put(key, value);
        return this;
    }
    @Override
    public ResponseBase put(String key, String value) {
        super.put(key, value);
        return this;
    }
}
