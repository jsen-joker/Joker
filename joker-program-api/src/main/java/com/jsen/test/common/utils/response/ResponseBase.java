package com.jsen.test.common.utils.response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.Map;

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


    private ResponseBase(Map<String, Object> map) {
        super(map);
    }
    private ResponseBase(JsonObject jsonObject) {
        super(jsonObject.getMap());
    }

    public static ResponseBase create() {
        return new ResponseBase();
    }
    public static ResponseBase create(Map<String, Object> data) {
        return new ResponseBase(data);
    }
    public static ResponseBase create(JsonObject jsonObject) {
        return new ResponseBase(jsonObject);
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
