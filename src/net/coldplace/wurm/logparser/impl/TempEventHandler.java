package net.coldplace.wurm.logparser.impl;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class TempEventHandler implements EventHandler 
{
    Session session;
    MeditationEventArgs me;
    long lastHoursUntilTick = 0;

    public TempEventHandler(Session session) 
    {
        this.session = session;
    }

    public void handle(Object sender, EventArgs e) 
    {
        if (e.getEventName().equals("meditation")) 
        {
            me = (MeditationEventArgs)e;
            
            if (me.isTickPossible())
                System.out.println("Meditation tick may be possible.");
            else 
                System.out.println("Meditate again in " + 
                    Util.toSpelledOutDuration( 
                        new Duration(new DateTime(), me.getNextTick())
                    )
                );
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
                    System.out.println("Next meditation in " + hoursUntilTick + " hours");
                }

                lastHoursUntilTick = hoursUntilTick;
            }
        }

        if (e.getEventName().equals("time-to-lockpick"))
        {
            System.out.println("Time to lockpick.");
        }
    }

}
