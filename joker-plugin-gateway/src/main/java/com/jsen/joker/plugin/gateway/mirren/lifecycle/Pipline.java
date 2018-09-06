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


    private List<Hock<? extends CONTEXT, ? extends REGEX, ? extends TARGET, ? extends CYCLE>> beforeHocks = Lists.newArrayList();
    private List<Hock<? extends CONTEXT, ? extends REGEX, ? extends TARGET, ? extends CYCLE>> afterHocks = Lists.newArrayList();

    public void registerBeforeHock(Hock<? extends CONTEXT, ? extends REGEX, ? extends TARGET, ? extends CYCLE> hock) {
        beforeHocks.add(hock);
    }
    public void registerAfterHock(Hock<? extends CONTEXT, ? extends REGEX, ? extends TARGET, ? extends CYCLE> hock) {
        afterHocks.add(hock);
    }

    private Pipline child;

    public Pipline setChild(Pipline pipline) {
        this.child = pipline;
        return pipline;
    }

    public void start(CONTEXT context, REGEX regex, TARGET target, CYCLE cycle) {
        if (handle(context, regex, target, cycle) && child != null) {
            child.start(context, regex, target, cycle);
        }
    }

    protected abstract boolean handle(CONTEXT context, REGEX regex, TARGET target, CYCLE cycle);

}
