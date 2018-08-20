package com.jsen.joker.boot.loader;


import com.jsen.joker.boot.loader.joker.JokerInit;
import com.jsen.joker.boot.loader.joker.help.ConfHelp;
import com.jsen.joker.boot.loader.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

/**
 * <p>
 *     start
 *     stop
 *     restart
 * </p>
 *
 * @author jsen
 * @since 2018/5/19
 */
public class Boot {
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String RESTART = "restart";

    interface ConfigKey {
        String ManagerPORT = "manager.port";
        String PORT = "port";
    }

    public static void main(String[] args) {


        String command = START;
        if (args.length > 0) {
            command = args[args.length - 1];
        }

        if (START.equals(command)) {
            start();
        } else if (STOP.equals(command)) {
            stop();
        } else if (RESTART.equals(command)) {
            if (stop()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.error("Joker 已关闭，重启Joker失败");
                }
                start();
            }
        } else {
            logger.warn("command \"" + command + "\" does not exist.");
        }

    }
    private static final String Zero = "0";
    private static boolean stop() {
        Map<String, Object> conf = ConfHelp.conf(JokerInit.getJokerRoot());
        Object port;
        if (conf.containsKey(ConfigKey.ManagerPORT)) {
            port = conf.get(ConfigKey.ManagerPORT);
        } else {
            port = conf.getOrDefault(ConfigKey.PORT, 9091);
        }

        try {
            logger.info("正在停止Joker服务器");
            String result = HttpUtils.sendGetRequest("http://localhost:" + port + "/status/stop");
            if (Zero.equals(result.trim())) {
                return true;
            } else {
                logger.error("停止失败");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("连接Joker出错， Joker stop 命令需要Joker core plugin manager 插件");
            return true;
        }
        return false;
    }
    private static void start() {
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Boot.class.getClassLoader().getResourceAsStream("logo.txt")))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            logger.info(">>> Start Joker <<<\n" + builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.info(">>> Start Joker <<<");
        }
        JokerInit.init(new JokerInit.Completer() {
            @Override
            public void succeed() {
                logger.info("<<< Joker succeed >>>");
            }

            @Override
            public void failed() {
                logger.error("<<< Joker failed >>>");
            }
        });
    }

}
