package io.sympli.find_e.event;

import io.sympli.find_e.ui.fragment.Screen;

public class ChangeScreenEvent {

    public enum ScreenGroup {
        MAIN, SHADOWING
    }

    private final Screen newState;
    private final ScreenGroup screenGroup;

    public ChangeScreenEvent(Screen screen, ScreenGroup group) {
        this.newState = screen;
        this.screenGroup = group;
    }

    public Screen getNewState() {
        return newState;
    }

    public ScreenGroup getScreenGroup() {
        return screenGroup;
    }
}
