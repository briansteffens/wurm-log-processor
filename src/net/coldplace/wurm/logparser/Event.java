package net.coldplace.wurm.logparser;

import java.util.*;

import org.joda.time.*;
import org.joda.time.format.*;

public class Event {

    String player;
    public String getPlayer() { return player; }

    DateTime timestamp;
    public DateTime getTimestamp() { return timestamp; }

    String text;
    public String getText() { return text; }

    String logType;
    public String getLogType() { return logType; }

    Boolean historical;
    public Boolean getHistorical() { return historical; }

    public Event(String player,
                 String logType,
                 DateTime timestamp, 
                 String text, 
                 Boolean historical) {

        this.player = player;
        this.logType = logType;
        this.timestamp = timestamp;
        this.text = text;
        this.historical = historical;
/*
        this.timestamp = new DateTime().withTime(
            Integer.parseInt(raw.substring(1, 3)),
            Integer.parseInt(raw.substring(4, 6)),
            Integer.parseInt(raw.substring(7, 9)),
            0
        );

        this.text = raw.substring(11);*/
    }

    @Override public String toString()
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");

        StringBuilder ret = new StringBuilder();

        ret.append("[");
        ret.append(dtf.print(timestamp));
        ret.append("] ");
        ret.append(text);

        return ret.toString();
    }

}
