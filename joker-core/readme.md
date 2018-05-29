# joker-core

joker的核心模块，由joker-boot驱动运行该模块，一个joker实例中，该模块是不会被卸载的，该模块存在放在lib目录下。

该模块的主要功能是实现joker对环境的监测，加载、卸载entry包下的jar包，js文件，自动删除解压静态目录下的静态资源。

该模块的入口：com.jsen.joker.boot.Boot$boot

在该模块下可以使用所有vertx的api

如有需要，在编程时可以引入该模块，获取一些joker系统能力

对应 joker/lib目录下的joker-core.jar