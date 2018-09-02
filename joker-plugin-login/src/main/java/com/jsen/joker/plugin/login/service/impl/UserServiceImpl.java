package com.jsen.joker.plugin.login.service.impl;

import com.jsen.test.common.ds.JdbcRepositoryWrapper;
import com.jsen.test.common.utils.enc.MD5Util;
import com.jsen.test.common.utils.response.ResponseBase;
import com.jsen.joker.plugin.login.entity.SysUser;
import com.jsen.joker.plugin.login.service.UserService;
import com.jsen.joker.plugin.login.utils.TokenUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author ${User}
 * @since 2018/5/3
 */
public class UserServiceImpl extends JdbcRepositoryWrapper implements UserService {
    private static final String INSERT_STATEMENT = "INSERT INTO sys_user (name, password) VALUES (?, ?)";
    private static final String SELECT_ALL = "SELECT * FROM sys_user";
    private static final String SELECT_BY_NAME = "SELECT * FROM sys_user WHERE name = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM sys_user WHERE id = ?";
    private static final String DELETE_STATEMENT_NAME = "DELETE FROM sys_user WHERE name = ?";
    private static final String DELETE_STATEMENT_ID = "DELETE FROM sys_user WHERE id = ?";
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // 4 hour
    public static final long shortExp = 60 * 60 * 4;
    public static final long LongExp = 60 * 60 * 24 * 7;

    public UserServiceImpl(Vertx vertx, JsonObject config) {
        super(UserService.serviceID, vertx, config);
    }


    @Override
    public void createUser(String name, String password, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonArray array = buildParams().add(name)
                .add(MD5Util.generate(password));
        effHelp(array, INSERT_STATEMENT, resultHandler);
    }

    @Override
    public void deleteByName(String name, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonArray array = buildParams().add(name);
        effHelp(array, DELETE_STATEMENT_NAME, resultHandler);
    }

    @Override
    public void deleteById(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonArray array = buildParams().add(id);
        effHelp(array, DELETE_STATEMENT_ID, resultHandler);
    }

    @Override
    public void listUser(int page, int capacity, Handler<AsyncResult<List<SysUser>>> resultHandler) {
        this.list(SELECT_ALL)
                .map(rawList -> rawList.stream()
                        .map(SysUser::new)
                        .collect(Collectors.toList())
                )
                .setHandler(resultHandler);
    }

    @Override
    public void getUserByName(String name, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.findOne(name, SELECT_BY_NAME)
                .map(option -> option.map(SysUser::new).orElse(null)).setHandler(a -> {
            if (a.succeeded()) {
                SysUser user = a.result();
                ResponseBase.create().code(0).put("name", user.getName()).put("password", user.getPassword()).handle(resultHandler);
            } else {
                ResponseBase.create().code(1).msg("没有用户").handle(resultHandler);
            }
        });
    }

    @Override
    public void getUserByID(Integer id, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.findOne(id, SELECT_BY_ID)
                .map(option -> option.map(SysUser::new).orElse(null)).setHandler(a -> {
            if (a.succeeded()) {
                SysUser user = a.result();
                ResponseBase.create().code(0).put("name", user.getName()).put("password", user.getPassword()).handle(resultHandler);
            } else {
                ResponseBase.create().code(1).msg("没有用户").handle(resultHandler);
            }
        });
    }

    @Override
    public void login(String username, String password, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.findOne(username, SELECT_BY_NAME)
                .map(option -> option.map(SysUser::new).orElse(null))
                .setHandler(a -> {
                    if(a.succeeded()) {
                        SysUser user = a.result();
                        try {
                            if (!MD5Util.verify(password, user.getPassword())) {
                                ResponseBase.create().code(1).msg("密码错误").handle(resultHandler);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ResponseBase.create().code(1).msg("加密出错").handle(resultHandler);
                            return;
                        }

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("id", user.getId());
                        jsonObject.put("username", user.getName());
                        jsonObject.put("nickname", user.getName());
                        try {
                            String tk = TokenUtils.genToken(jsonObject, user.getPassword(), shortExp);
                            String rTk = TokenUtils.genToken(jsonObject, user.getPassword(), LongExp);
                            ResponseBase.create().code(0).put("token", tk)
                                    .put("rToken", rTk).put("username", user.getName())
                                    .put("id", user.getId()).put("sex", user.getSex()).handle(resultHandler);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            ResponseBase.create().code(1).msg("获取token失败").handle(resultHandler);
                        }

                    } else {
                        a.cause().printStackTrace();
                        ResponseBase.create().code(1).msg("用户不存在").handle(resultHandler);
                    }
                });
    }

    private void effHelp(JsonArray params, String sql, Handler<AsyncResult<JsonObject>> resultHandler) {
        update(params, sql, a -> {
            if(a.succeeded()) {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 0).put("eff", a.result())));
            } else {
                resultHandler.handle(Future.succeededFuture(new JsonObject().put("code", 1)));
            }
        });
    }
}
