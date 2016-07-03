package io.sympli.find_e.services;

public interface IBroadcast {

    void register(Object o);

    void unregister(Object o);

    void removeStickyEvent(Class<?> c);

    void postEvent(Object event);

    void postStickyEvent(Object event);
}
