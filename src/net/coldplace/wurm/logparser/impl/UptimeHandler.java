package net.coldplace.wurm.logparser.impl;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class UptimeHandler extends ExtractorBase implements Uptime 
{

    public int getPriority() { return 10; }

    public Duration getUptime() 
    { 
        return new Duration(lastRestart, new DateTime()); 
    }

    DateTime lastRestart;
    public DateTime getLastRestart() { return lastRestart; }

    public DateTime getLastSkillReset() 
    {
        DateTime ret = lastRestart;
        while (ret == null || ret.isBefore(new DateTime()))
            ret = ret.plusDays(1);
        return ret.plusDays(-1);
    }

    public void handle(Event e) 
    {
        final String prefix = "The server has been up ";

        if (e.getText().startsWith(prefix)) 
        {
            DateTime lr = e.getTimestamp();

            lastRestart = e.getTimestamp().plus(Util.parseSpelledOutDuration(e.getText().replace(prefix, "")));
        }
    }

}
