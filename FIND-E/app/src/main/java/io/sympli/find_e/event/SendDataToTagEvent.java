package io.sympli.find_e.event;

public class SendDataToTagEvent {

    private TagAction tagAction;

    public SendDataToTagEvent(TagAction action) {
        this.tagAction = action;
    }

    public TagAction getTagAction() {
        return tagAction;
    }
}
