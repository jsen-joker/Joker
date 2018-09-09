package com.jsen.joker.plugin.gateway.mirren.lifecycle;

import com.google.common.collect.Lists;
import com.jsen.joker.plugin.gateway.mirren.ApplicationVerticle;
import com.jsen.joker.plugin.gateway.mirren.model.Api;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/6
 */
public abstract class Pipline<CONTEXT, REGEX, TARGET, CYCLE> {


    private List<Hock<CONTEXT, REGEX, TARGET, CYCLE>> beforeHocks = Lists.newArrayList();
    private List<Hock<CONTEXT, REGEX, TARGET, CYCLE>> afterHocks = Lists.newArrayList();

    public void registerBeforeHock(Hock<CONTEXT, REGEX, TARGET, CYCLE> hock) {
        beforeHocks.add(hock);
    }
    public void registerAfterHock(Hock<CONTEXT, REGEX, TARGET, CYCLE> hock) {
        afterHocks.add(hock);
    }

    private Pipline<CONTEXT, REGEX, TARGET, CYCLE> child;

    public Pipline<CONTEXT, REGEX, TARGET, CYCLE> setChild(Pipline<CONTEXT, REGEX, TARGET, CYCLE> pipline) {
        this.child = pipline;
        return pipline;
    }

    public void start(CONTEXT context, REGEX regex, TARGET target, CYCLE cycle) {
        for (Hock<CONTEXT, REGEX, TARGET, CYCLE> before : beforeHocks) {
            if (!before.handle(context, regex, target, cycle)) {
                return;
            }
        }

        if (handle(context, regex, target, cycle)) {
            for (Hock<CONTEXT, REGEX, TARGET, CYCLE> before : afterHocks) {
                if (!before.handle(context, regex, target, cycle)) {
                    return;
                }
            }
            if (child != null) {
                child.start(context, regex, target, cycle);
            }
        }
    }

    protected abstract boolean handle(CONTEXT context, REGEX regex, TARGET target, CYCLE cycle);

}
