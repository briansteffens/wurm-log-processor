package net.coldplace.wurm.logparser;

import java.util.*;

public class Session {

    String playerName;


    
    Map<String, List<EventHandler>> eventHandlers 
        = new HashMap<String, List<EventHandler>>();
    
    public void registerEventHandler(String eventName, EventHandler ah) {
        if (!eventHandlers.containsKey(eventName))
            eventHandlers.put(eventName, new ArrayList<EventHandler>());

        eventHandlers.get(eventName).add(ah);
    }

    public void raiseEvent(Object sender, EventArgs e) {
        if (eventHandlers.containsKey(e.getEventName()))
            for (EventHandler h : eventHandlers.get(e.getEventName()))
                h.handle(sender, e);
    }
    




    Map<String, AlertSender> alertSenders = new HashMap<String, AlertSender>();

    public void registerAlertSender(AlertSender sender) {
        alertSenders.put(sender.getSenderName(), sender);
    }

    public void sendAlert(String senderName, Alert alert) {
        alertSenders.get(senderName).send(alert);
    }

}
