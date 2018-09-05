# joker-plugin-gateway


本插件借鉴了 http://szmirren.com/ wx-api-gateway

/api 受限请求 token验证
/pb 公共请求 非受限请求

Api网关，网关模块要依赖于 joker-plugin-login模块，用于验证用户是否登入、以及完成用户登入
对于所有/login目录下的请求，会转到登入服务
对于/api下的请求会转到对应的endpoint下，转发规则是/api/endpoint
/endpoint为模块在服务发现中注册的endpoint属性

/api/endpoint/api1 ==> 转发到对应endpoint下 /api/api1
/pb/endpoint/api1  ==> 转发到对应endpoint下 /api1



api gateway 工作模式：
1、基于vertx 的服务发现，每个微服务模块注册微服务，gateway根据路径匹配相应的服务名字，调用相关的服务

2、基于类似于http转发规则，用户创建一个app，app下添加api，每个api都有相应的实际请求地址，请求该api时转到实际地址
