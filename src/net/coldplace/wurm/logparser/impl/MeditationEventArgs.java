package net.coldplace.wurm.logparser.impl;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class MeditationEventArgs implements EventArgs {

    public String getEventName() { return "meditation"; }

    DateTime nextTick;
    public DateTime getNextTick() { return nextTick; }

    public Boolean isTickPossible() { return nextTick.isBefore(new DateTime()); }

    public MeditationEventArgs(DateTime nextTick) {
        this.nextTick = nextTick;
    }

}
