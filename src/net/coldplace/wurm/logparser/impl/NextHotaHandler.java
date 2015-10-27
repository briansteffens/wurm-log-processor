package net.coldplace.wurm.logparser.impl;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class NextHotaHandler extends ExtractorBase 
{
    DateTime nextHota;
    void setNextHota(DateTime val) 
    {
        if (nextHota != val && !first)
            raiseEvent();

        nextHota = val;
    }

    void raiseEvent()
    {
        getSession().raiseEvent(this, new NextHotaEventArgs(nextHota));
    }

    public void handle(Event e)
    {
        if (e.getText().contains(" has secured victory for ")) 
        {
            setNextHota(e.getTimestamp().plusHours(36));
            return;
        }

        final String[] hotaStarts = {
            "A new Hunt of the Ancients starts in ",
            "A new Hunt of the Ancients begins in ",
            "The hunt of the Ancients will begin in "
        };

        for (String hotaStart : hotaStarts)
            if (e.getText().startsWith(hotaStart))
            {
                String unparsed = e.getText().replace(hotaStart, "");
                Duration until = Util.parseSpelledOutDuration(unparsed);

                // TODO: wtf? why not '.plus(until)'
                setNextHota(e.getTimestamp().plusSeconds(
                            (int)-until.getStandardSeconds()));

                break;
            }
    }

    Boolean first = true;
    public void pulse() 
    {
        if (first)
        {
            raiseEvent();
            first = false;    
        }
    }

}
