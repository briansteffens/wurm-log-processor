package net.coldplace.wurm.logparser.impl;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class LockpickingEventArgs implements EventArgs {

    public String getEventName() { return "lockpicking"; }

    DateTime nextTick;
    public DateTime getNextTick() { return nextTick; }

    public Boolean isTickPossible() { return nextTick.isBefore(new DateTime()); }

    public LockpickingEventArgs(DateTime nextTick) {
        this.nextTick = nextTick;
    }

}
