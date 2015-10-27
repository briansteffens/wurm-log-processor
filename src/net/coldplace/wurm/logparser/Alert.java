package net.coldplace.wurm.logparser;

import org.joda.time.*;

public class Alert {

    String message;
    public String getMessage() { return message; }

    DateTime timestamp;
    public DateTime getTimestamp() { return timestamp; }

    public Alert(DateTime timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

}
