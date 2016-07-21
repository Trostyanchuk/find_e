package io.sympli.find_e.event;

import io.sympli.find_e.ui.fragment.Screen;

public class AnimationFinishedEvent {

    private Screen screen;

    public AnimationFinishedEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
