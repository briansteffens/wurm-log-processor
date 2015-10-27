package net.coldplace.wurm.logparser.impl;

import java.util.*;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class MeditationHandler extends ExtractorBase 
{

    List<DateTime> meditations = new ArrayList<DateTime>();

    Uptime uptime;

    public MeditationHandler(Uptime uptime) 
    {
        this.uptime = uptime;
    }

    public int getPriority() { return 0; }

    DateTime ignoreFinishUntil = null;

    public void handle(Event e) 
    {
        if (e.getText().equals("You recently meditated here and need to find new insights somewhere else."))
            ignoreFinishUntil = e.getTimestamp().plusMinutes(2).plusSeconds(1);

        if (e.getText().equals("You finish your meditation.") &&
            (ignoreFinishUntil == null ||
            ignoreFinishUntil.isBefore(e.getTimestamp())))
            meditations.add(e.getTimestamp());
    }

    Boolean tickPossible;
    Boolean first = true;

    public void pulse() 
    {
        while (meditations.size() > 0) 
        {
            if (meditations.get(0).isAfter(uptime.getLastSkillReset()))
                break;

            meditations.remove(0);
        }
        
        if (meditations.size() == 0)
            return;

        int ticks = 0;
        DateTime cooldownUntil = null;

        for (int i = 0; i < meditations.size(); i++) 
        {
            if (cooldownUntil != null &&
                meditations.get(i).isBefore(cooldownUntil)) 
            {
                meditations.remove(i);
                i--;
                continue;
            }
            //System.out.println("meditation: " + meditations.get(i));
            ticks++;
            int cooldown = ticks < 5 ? 30 : 180;
            cooldownUntil = meditations.get(i).plusMinutes(cooldown);
        }

        DateTime nextSkillReset = uptime.getLastSkillReset().plusDays(1);
        //System.out.println("nextSkillReset: " + nextSkillReset);
        //System.out.println("cooldownUntil: " + cooldownUntil);
        if (cooldownUntil.isAfter(nextSkillReset))
            cooldownUntil = nextSkillReset;
        //System.out.println("cooldownUntil: " + cooldownUntil);

        Boolean temp = new DateTime().isAfter(cooldownUntil);

        if (temp == tickPossible && !first)
            return;
        first = false;

        tickPossible = temp;
        
        getSession().raiseEvent(this, new MeditationEventArgs(cooldownUntil));
    }

}
