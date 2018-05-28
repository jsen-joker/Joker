package com.jsen.joker.plugin.login.service;

import com.jsen.joker.plugin.login.service.impl.ShiroServiceImpl;
import com.jsen.joker.plugin.login.entity.SysPermission;
import com.jsen.joker.plugin.login.entity.SysRole;
import com.jsen.joker.plugin.login.entity.SysUser;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
@VertxGen
@ProxyGen
public interface ShiroService {
    public static final String serviceID = "service.shiro";
    // A couple of factory methods to create an instance and a proxy
    static ShiroService create(Vertx vertx, JsonObject config) {
        return new ShiroServiceImpl(vertx, config);
    }

    static ShiroService createProxy(Vertx vertx) {
        return new ShiroServiceVertxEBProxy(vertx, serviceID);
        // return ProxyHelper.createProxy(EchoService.class, vertx, address);
        // Alternatively, you can create the proxy directly using:
        // return new ProcessorServiceVertxEBProxy(vertx, address);
        // The name of the class to instantiate is the service interface + `VertxEBProxy`.
        // This class is generated during the compilation
    }


    void getRoleByUserId(Integer id, Handler<AsyncResult<List<SysRole>>> resultHandler);
    void getPermissionByRoleId(Integer id, Handler<AsyncResult<List<SysPermission>>> resultHandler);

    void createUser(String name, String password, String sex,
                    Handler<AsyncResult<JsonObject>> resultHandler);
    void deleteByName(String name, Handler<AsyncResult<JsonObject>> resultHandler);
    void deleteById(Integer id, Handler<AsyncResult<JsonObject>> resultHandler);
    void listUser(int page, int capacity, Handler<AsyncResult<List<SysUser>>> resultHandler);
    void getUserByName(String name, Handler<AsyncResult<JsonObject>> resultHandler);
    void getUserByID(Integer id, Handler<AsyncResult<JsonObject>> resultHandler);

    void login(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler);


}
