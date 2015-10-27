package net.coldplace.wurm.logparser.impl;

import net.coldplace.wurm.logparser.*;

import org.joda.time.*;

public class ConsoleAlertSender implements AlertSender {

    public String getSenderName() {
        return "console";
    }

    public void send(Alert alert) {
        System.out.println("[ALERT] " + alert.getMessage());
    }

}
