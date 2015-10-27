package net.coldplace.wurm.logparser;

import org.joda.time.*;

public class Util 
{

    public static Duration parseSpelledOutDuration(String input) 
    {
        String[] parts = input.split(" ");

        DateTime anchor = new DateTime();
        DateTime target = anchor;

        for (int i = 0; i < parts.length; i += 2) 
        {
            if (parts[i].equals("and"))
                i++;

            int count = Integer.parseInt(parts[i]);
            String unit = parts[i + 1].replace(",", "").replace(".", "").trim();

            if (unit.equals("days"))
                target = target.plusDays(-count);
            else if (unit.equals("hours"))
                target = target.plusHours(-count);
            else if (unit.equals("minute") || unit.equals("minutes"))
                target = target.plusMinutes(-count);
            else if (unit.equals("second") || unit.equals("seconds"))
                target = target.plusSeconds(-count);
            else
                throw new Error("Bad duration unit: " + unit);
        }

        return new Duration(anchor, target);
    }

    public static String toSpelledOutDuration(Duration input) 
    {
        long hours = input.getStandardHours();
        input = input.plus(hours * -3600000);

        long minutes = input.getStandardMinutes();
        input = input.plus(minutes * -60000);

        long seconds = input.getStandardSeconds();
        input = input.plus(seconds * -1000);

        String ret = (hours   > 0 ? hours   + " hours "   : "") +
                     (minutes > 0 ? minutes + " minutes " : "") +
                     (seconds > 0 ? seconds + " seconds " : "");

        return ret != "" ? ret.trim() : "(now)";
    }

}
