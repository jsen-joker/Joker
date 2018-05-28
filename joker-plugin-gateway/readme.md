# joker-plugin-gateway

Api网关，网关模块要依赖于 joker-plugin-login模块，用于验证用户是否登入、以及完成用户登入
对于所有/login目录下的请求，会转到登入服务
对于/api下的请求会转到对应的endpoint下，转发规则是/api/endpoint
/endpoint为模块在服务发现中注册的endpoint属性