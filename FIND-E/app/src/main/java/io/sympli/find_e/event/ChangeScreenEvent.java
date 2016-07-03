package io.sympli.find_e.event;

import io.sympli.find_e.ui.fragment.Screen;

public class ChangeScreenEvent {

    private final Screen newState;

    public ChangeScreenEvent(Screen screen) {
        this.newState = screen;
    }

    public Screen getNewState() {
        return newState;
    }
}
