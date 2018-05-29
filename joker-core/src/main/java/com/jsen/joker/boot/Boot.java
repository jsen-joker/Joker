package com.jsen.joker.boot;

import com.jsen.joker.boot.joker.JokerInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/19
 */
public class Boot {
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public CompletableFuture<Boolean> boot() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        logger.info(">>> Start init joker context <<<");

        JokerInit.init(new JokerInit.Completer() {
            @Override
            public void succeed() {
                logger.info("<<< Init joker context succeed >>>");
                future.complete(true);
            }

            @Override
            public void failed() {
                logger.error("<<< Init joker context failed >>>");
                future.complete(false);
            }
        });
        return future;
    }


}
