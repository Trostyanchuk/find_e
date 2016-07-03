package io.sympli.find_e.services.impl;

import org.greenrobot.eventbus.EventBus;

import io.sympli.find_e.ApplicationComponent;
import io.sympli.find_e.services.IBroadcast;

public class EventBusBroadcastImpl implements IBroadcast {

    private EventBus eventBus;

    public EventBusBroadcastImpl(EventBus bus) {
        eventBus = bus;
    }

    @Override
    public void register(Object o) {
        eventBus.register(o);
    }

    @Override
    public void unregister(Object o) {
        eventBus.unregister(o);
    }

    @Override
    public void removeStickyEvent(Class<?> c) {
        eventBus.removeStickyEvent(c);
    }

    @Override
    public void postEvent(Object event) {
        eventBus.post(event);
    }

    @Override
    public void postStickyEvent(Object event) {
        eventBus.postSticky(event);
    }
}
