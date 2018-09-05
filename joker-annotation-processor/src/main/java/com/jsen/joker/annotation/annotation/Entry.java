package com.jsen.joker.annotation.annotation;

import java.lang.annotation.*;

/**
 * <p>
 *
 *     entry
 *     启动器注解
 * </p>
 *
 * @author jsen
 * @since 2018/9/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
@Inherited
public @interface Entry {
    /**
     * 优先级
     * @return
     */
    int priority() default 1;

    /**
     * 部署副本数
     * @return
     */
    int instances() default 1;

}
