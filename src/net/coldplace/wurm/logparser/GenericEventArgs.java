package net.coldplace.wurm.logparser;

public class GenericEventArgs implements EventArgs {

    String eventName;
    public String getEventName() { return eventName; }

    public GenericEventArgs(String eventName) {
        this.eventName = eventName;
    }

}
