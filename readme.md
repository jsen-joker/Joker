# Joker

#
#
一个**快速开发**调试**微服务**的服务**容器**

## 快速开始
git clone https://github.com/jsen-joker/Joker.git
cd Joker/joker/bin

./joker.sh install

./startup.sh

./shutdown.sh



## 一、Joker是什么（理念）

joker 是一个轻量级的微服务容器，专为微服务开发而生。作为一个容器，他与tomcat，docker等有什么不同。首先joker的宗旨是快速实现想要的业务模块，快速编程、快速部署。其次相较于tomcat，tomcat中发布的单位是一个个独立的网站，相互之间想要有关量，只能通过http请求和基于java的消息服务等，而joker的目标是开发一个微服务框架，所有的微服务可以在这个框架上运行，可以相互间直接调用（这里的直接调用包括但不局限于、直接的java调用，vertx的代理、eventbus调用），二docker作为一个粗粒度的容器，同样无法在通讯方面有很好的作为，joker基于vertx开发，继承了vertx的集群模式下可以直接通过eventbus访问的特点，可以非常容易的部署服务集群、并在服务间交互。而相交于spring体系，joker、vertx有着全新的不同开发模式，完全的异步化，对于熟悉node、promise的人来说非常易于上手、对于不熟悉异步开发的人，其实异步只要入门，可以非常方便的开发，而且在joker下开发，你会喜欢上jdk8的特性。
joker的核心原理就是将所有的java字节码加载在同一个classloader下。

#
#
## 二、joker plugin 截图
![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/joker_001.png)
#
![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/joker_002.png)
#
![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/joker_003.png)
#
![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/joker_004.png)
#
![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/joker_005.png)
#
![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/joker_006.png)
#
![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/joker_007.png)

## 三、Get Start

#
#
## 1、Joker目录结构：

#### bin/ 

命令工具 其中joker.sh是核心命令工具 joker.sh 需要joker-core-plugin-manager 插件的支持

### cache/

目前是保存hsqldb数据用

#### conf/ 

核心配置文件 目前 该目录下所有配置文件都不是必须的，joker启动时会加载所有properties文件到一个Map下，verticle可以通过config()获取该属性，joker.properties 配置集群信息口，core.plugin.properties文件配joker-core-plugin-manager服务器端， joker-core-plugin-entry-server插件的属性，用于上传和下载文件，在joker下载文件的joker-core-plugin-manager插件中也会读取这里的地址属性来下载文件。config.json 是配置文件服务器插件的配置文件。除了集群信息，所有属性文件都是配置插件使用的，不是joker必须的。
##### conf/cluster 

配置 集群manager的属性

#### entry/ 

类似于 tomcat的webapps 这里存放jar包、js格式的微服务模块，一般来说，一个jar包，一个js文件就应该是一个可独立运行的微服务。放入这里的entry joker将自动检测并加载，建议如果entry包更新，可以重启服务器，如果添加jar包，不用重启，由于jar包的加载没有顺序（还未支持），所以可能还要考虑服务间的依赖关系。

#### joker-boot.jar

joker bootstrap 包

#### lib/

joker 运行的依赖包

#### logs/

joker 日志

#### static/

joker 静态资源目录

#### third/

一些说明和辅助资源





#
#
## 2、Joker项目组成：
### joker-boot 
joker boot启动器，负责joker命令行解释处理，环境配置，启动运行core
### joker-core
joker 核心，负责joker主循环，监听joker entry变化，处理生命周期变化，所有的核心如加载卸载entry都在这里处理
### joker-program-api
joker 提供的简单的编程api，主要提供了适合于joker项目结构路径的一些适配，如获取joker根目录，特定的static静态资源handler，通用数据库适配等等
### 其他的一些core-plugin
主要是用来监听joker运行环境，并提供界面便于查看当前的joker实例信息
### 其他的一些非core的plugin
可以参考学习joker和vertx的编程知识。

#
#
## 四、性能测试 

性能测试采用wrk测试

由于内网网络不稳定，顾采用本地测试，尽可能去除网络本身的影响

![image](https://raw.githubusercontent.com/jsenjobs/joker/master/third/wrk-test.png)


 

#
#
## 五、Version

#### version 0.0.1-SNAPSHOT 

实现动态加载Jar包 自动从服务器下载需要的Jar包

####  version 0.0.2-SNAPSHOT 

将Entry server，和文件下载删除API从joker核心提出，作为两个独立的entry模块，为Entry server 提供默认界面，静态页面在static中

### version 0.0.3-SNAPSHOT

一、将所有非核心代码从joker-core剥离出来

二、实现对静态资源的支持，静态资源必须在jar包的webroot目录下才会在加载的时候被解压，默认解压目录是static，webroot也可放在entry目录，但不建议这样做。建议entry只实现json格式API不要提供任何网页信息，建议直接使用react、angular等实现静态页面，与entry交互。

三、js脚本支持，可以查看vertx上java加载其他语言的examples实现js语言的verticle，部署方式直接拖入即可发现，同jar包保持一致，建议用vertx的trans工具全部转换成jar格式统一加载。

四、集群模式支持，joker.properties添加core.cluster和core.cluster.manager属性，core.cluster为yes表示使用集群模式，core.cluster.manager为集群模式的manager，默认支持hazelcast和zookeeper。

五、vertx-boot-class 节点增加instances属性，对应部署时的副本数量，未设置默认为1个副本。

### version 0.0.3_01-SNAPSHOT

一、自动解压webroot目录，用户想要使用静态资源，要放在webroot目录下，并使用JokerStaticHandlerImpl(在joker-program-api下)。

二、entry-server默认使用hsqldb 所以不用再依赖mysql，可以直接启动。

三、统一修改端口：
 
 1、core-manager-master:9090这个只能localhost去访问，集群应该只有一个master节点， 提供了静态页面，可以登入访问 http://localhost:9090。
 
 2、core-manager:9091 受master节点管理。
 
 3、entry-server:9092 entry下载端口，也提供了一个基本的文件下载上传网页，入口：http://localhost:9092。

四、在ctrl c系统中断下清理joker节点数据，防止数据不准确。

五、添加pom中vertx-boot-class支持 priority属性来设置启动顺序，默认为1，同一个priority下的entry会在一个组中加载。

### version 0.0.3_02-SNAPSHOT
一、修改classloader加载策略，每一个文件、jar包加载一个自己的classloader，他们属于平行关系，他们属于同一个父classloader，修改了该父classloader的加载策略，loadclass时首先检查所有子类。
二、实现更细粒度的entry部署控制，由于新的classlaoder实现，我们可以在卸载的时候只卸载删除了的文件的verticle，不用全部卸载，再更新。
三、一些插件：基于redis的任务调度，将原来基于config文件的文件配置服务器改为使用hsqldb数据库进行配置，默认9000端口，支持在线修改，支持json格式配置。
### version 0.0.3_03-SNAPSHOT
一、修复joker关闭bug，二、修复卸载entry后entry实例全部清除的bug，三、修复部分组件卸载失败和卸载失败后重新加载无法注册entry的bug

#
#
## 六、deploy

cd bin

./joker.sh install

./startup.sh

./shutdown.sh

如果要查看joker执行情况，请使用 ./joker.sh run命令

集群模式：

一、修改core.cluster为yes

二、在2181端口启动zookeeper或者修改joker.properties下的clusterManager类，配置集群管理类文件(/conf/cluster下)

三、启动joker

joker的核心插件：

joker-core-plugin-entry-server-1.0.3-SNAPSHOT.jar entry上传保存到这里。

joker-core-plugin-manager-1.0-SNAPSHOT.jar 调用上面的API可以从entry-server下载jar到entry目录自动部署，目前还未完善，不建议使用。joker的stop命令也依赖于这个插件。

joker-core-plugin-entry-server的Mysql数据库：在third下有sql文件

希望可以建立一些基本的插件库 实现插拔式编程，比如gateway、account、config等微服务模块，在编程的时候引入其jar包，即可调用其API进行编程，在打包时，只需要打包瘦jar即可部署运行。

