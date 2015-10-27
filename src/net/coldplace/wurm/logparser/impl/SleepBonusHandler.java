package net.coldplace.wurm.logparser.impl;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;

public class SleepBonusHandler extends ExtractorBase 
{
    Boolean sleepBonusOn;
    DateTime lastToggle;
    Boolean canToggle;

    public void handle(Event e) 
    {
        if (e.getText().equals("You start using the sleep bonus.")) 
            sleepBonusOn = true;
        else if (e.getText().equals("You refrain from using the sleep bonus.")) 
            sleepBonusOn = false;
        else
            return;

        lastToggle = e.getTimestamp();
        canToggle = false;
    }

    public void pulse() 
    {
        Boolean can = new Period(lastToggle, new DateTime()).getMinutes() >= 5;
        if (can != canToggle) 
        {
            System.out.println("* sb can be toggled");

            if (sleepBonusOn)
                getSession().raiseEvent(this, new GenericEventArgs("sleepbonus-can-disable"));

            canToggle = can;
        }
    }
}
