package net.coldplace.wurm.logparser.impl;

import java.util.*;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class LockpickingHandler extends ExtractorBase 
{

    DateTime nextLockpick = null;
    boolean eventRaised = false;
    DateTime lastTick = null;

    public LockpickingHandler()
    {
    }

    public int getPriority() { return 0; }

    public void handle(Event e) 
    {
        if (e.getText().equals("You fail to pick the lock of the small chest.") ||
            e.getText().startsWith("You pick the lock of the "))
        {
            nextLockpick = e.getTimestamp().plusMinutes(10);
            eventRaised = false;
        }
    }


    public void pulse() 
    {
        if (nextLockpick == null)
            return;

        if (DateTime.now().isAfter(nextLockpick))
        {
            if (lastTick == null || !lastTick.equals(nextLockpick))
            {
                eventRaised = false;
                lastTick = nextLockpick;
            }
        }

        if (!eventRaised)
        {
            getSession().raiseEvent(this, new LockpickingEventArgs(nextLockpick));
            eventRaised = true;
        }
    }

}
