package net.coldplace.wurm.logparser.impl;

import java.io.*;

import org.joda.time.*;
import org.jibble.pircbot.*;

import net.coldplace.wurm.logparser.*;

public class IrcEventHandler extends PircBot implements EventHandler 
{
    Session session;
    MeditationEventArgs me;
    long lastHoursUntilTick = 0;

    LockpickingEventArgs le;
    NextHotaEventArgs hota;

    // The IRC name of the player who owns the bot.
    String playerName;

    // Whether the player is logged in. This isn't really accurate, just a
    // guess to help cut down on undeliverable PMs sent through IRC while
    // the Wurm client isn't even running.
    boolean loggedIn;
    boolean getLoggedIn() { return loggedIn; }
    void setLoggedIn(boolean val) 
    {
        if (loggedIn != val)
            System.out.println("Player logged " + (val ? "in" : "out") + ".");

        loggedIn = val;
    }


    public IrcEventHandler(Session session, String playerName)
    {
        this.session = session;
        this.playerName = playerName;

        this.setName("KirtahBot");
        //this.setVerbose(true);

        try 
        { 
            this.connect("irc.rizon.net"); 
        } 
        catch (IOException ex) 
        { 
            throw new Error(ex); 
        }
        catch (IrcException ex)
        {
            throw new Error(ex);
        }

        this.joinChannel("#kirtah777");
    }

    public void onMessage(String channel, String sender, String login,
                          String hostname, String message)
    {
        System.out.println("--- IRC MESSAGE ---");

        System.out.println("channel: " + channel);
        System.out.println("sender: " + sender);
        System.out.println("login: " + login);
        System.out.println("hostname: " + hostname);
        System.out.println("message: " + message);

        System.out.println("---");
    }

    public void onPrivateMessage(String sender, String login, 
                                 String hostname, String message)
    {
        System.out.println("--- IRC MESSAGE ---");

        System.out.println("sender: " + sender);
        System.out.println("login: " + login);
        System.out.println("hostname: " + hostname);
        System.out.println("message: " + message);

        System.out.println("---");

        // PM sent from an unexpected user
        if (!sender.equals(playerName))
        {
            System.out.println("PM from " + sender + " o.O");
            return;
        }

        setLoggedIn(true);

        message = message.trim().toLowerCase();

        if (message.equals("when med"))
        {
            sendWhenMed();
            return;
        }

        if (message.equals("when hota"))
        {
            send(hota.getMessage());
            return;
        }
    }

    public void onNotice(String sourceNick, String sourceLogin,
                         String sourceHostname, String target, String notice)
    {
        /*System.out.println("--- IRC NOTICE ---");
        
        System.out.println("sourceNick: " + sourceNick);
        System.out.println("sourceLogin: " + sourceLogin);
        System.out.println("sourceHostname: " + sourceHostname);
        System.out.println("target: " + target);
        System.out.println("notice: " + notice);

        System.out.println("---");*/
    }

    public void onUnknown(String line)
    {
        /*System.out.println("--- IRC UNKNOWN ---");

        System.out.println("line: " + line);

        System.out.println("---");*/
    }

    protected void handleLine(String line)
    {
        if (line.endsWith(":No such nick/channel"))
            setLoggedIn(false);

        super.handleLine(line);
    }

    // Send message to the player over IRC as long as they're logged in.
    void send(String message)
    {
        if (getLoggedIn())
            this.sendMessage(playerName, message);

        //System.out.println("IRC MSG: " + message);
    }

    void sendWhenMed()
    {
        if (me == null)
            return;

        if (me.isTickPossible())
            send("Time to meditate?");
        else 
            send("Meditate again in " + 
                Util.toSpelledOutDuration( 
                    new Duration(new DateTime(), me.getNextTick())
                )
            );
    }

    public void handle(Object sender, EventArgs e) 
    {
        if (e.getEventName().equals("client-login"))
        {
            setLoggedIn(true);
            return;
        }

        if (e.getEventName().equals("meditation")) 
        {
            me = (MeditationEventArgs)e;
            sendWhenMed();
        }

        if (e.getEventName().equals("lockpicking"))
        {
            le = (LockpickingEventArgs)e;
            if (le.isTickPossible())
                send("Time to lockpick.");
            else
                send("Lockpick in " + 
                    Util.toSpelledOutDuration(
                        new Duration(new DateTime(), le.getNextTick())
                    )
                );
        }

        if (e.getEventName().equals("next-hota"))
        {
            hota = (NextHotaEventArgs)e;
            send(hota.getMessage());
        }


        if (e.getEventName().equals("pulse")) 
        {
            if (me != null)
            {
                Duration untilNext = new Duration(new DateTime(), me.getNextTick());
                long hoursUntilTick = untilNext.getStandardHours();
                
                if (hoursUntilTick > 0 && 
                    hoursUntilTick == lastHoursUntilTick - 1)
                {
                    send("Next meditation in " + hoursUntilTick + " hours");
                }

                lastHoursUntilTick = hoursUntilTick;
            }
        }

        if (e.getEventName().equals("sleepbonus-can-disable"))
            send("Sleep bonus can be turned off.");

    }

}
