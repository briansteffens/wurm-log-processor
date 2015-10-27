package net.coldplace.wurm.logparser.impl;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class NextHotaEventArgs implements EventArgs {

    public String getEventName() { return "next-hota"; }

    DateTime nextHota;
    public DateTime getNextHota() { return nextHota; }

    public String getMessage()
    {
        String s = "";

        if (nextHota == null)
            s += "(no idea lol)";
        else
            s += Util.toSpelledOutDuration(new Duration(new DateTime(), nextHota));
        
        return "Next hota in " + s;
    }

    public NextHotaEventArgs(DateTime nextHota) {
        this.nextHota = nextHota;
    }

}
