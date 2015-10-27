package net.coldplace.wurm.logparser;

import org.joda.time.*;

public interface AlertSender {

    public String getSenderName();

    public void send(Alert alert);

}
