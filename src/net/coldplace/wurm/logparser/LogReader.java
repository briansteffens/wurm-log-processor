package net.coldplace.wurm.logparser;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joda.time.*;

public class LogReader implements AutoCloseable
{
    final String NL = System.getProperty("line.separator");

    final Pattern STARTS_WITH_DATE = Pattern.compile(
                                        "^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}\\] ");


    LineReader lineReader;
    Session session;
    String player;



    public LogReader(String player, LineReader lineReader, Session session)
    {
        this.player = player;
        this.lineReader = lineReader;
        this.session = session;
    }


    boolean isLoggingStarted(String test)
    {
        return test.startsWith("Logging started ");
    }


    boolean startsWithDate(String test)
    {
        return STARTS_WITH_DATE.matcher(test).find();
    }


    // Set by "Logging started yyyy-mm-dd" lines
    int day;
    DateTime lastEventTimestamp;

    public Event read() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        while (true)
        {
            String current = lineReader.read();
            
            if (current == null)
                break;

            // "Logging started .." type messages can be used to figure out the
            // day of the following events.
            if (isLoggingStarted(current))
            {
                day = Integer.parseInt(current.substring(24, 26));

                // The auto day rollover code needs to be inhibited.
                lastEventTimestamp = null;

                session.raiseEvent(this, new GenericEventArgs("client-login"));

                continue;
            }

            // Just making sure
            if (sb.length() == 0 && !startsWithDate(current))
                throw new Error("Problem with [" + current + "].");

            sb.append(current);
            sb.append(NL);


            // Peek at the next line to See if we should read another line.
            // If the next line starts with 'Logging started' or a timestamp,
            // break since we're already at the end of this log event.
            String next = lineReader.getNext();
            if (next == null || isLoggingStarted(next) || startsWithDate(next))
                break;
        }
        
        // No event log to return
        if (sb.length() == 0)
            return null;

        String raw = sb.toString().trim();

        // Generate the timestamp
        DateTime timestamp = new DateTime(
            lineReader.getYear(),                   // year
            lineReader.getMonth(),                  // month
            day,                                    // day
            Integer.parseInt(raw.substring(1, 3)),  // hour
            Integer.parseInt(raw.substring(4, 6)),  // minute
            Integer.parseInt(raw.substring(7, 9))   // second
        );

        // If the last timestamp was after the current one (as in time went
        // backwards), we probably just passed midnight so the day needs to
        // to be incremented.
        if (lastEventTimestamp != null && 
            lastEventTimestamp.isAfter(timestamp))
        {
            timestamp = timestamp.plusDays(1);
            day++;
        }

        lastEventTimestamp = timestamp;

        return new Event(player, "?", timestamp, raw.substring(10).trim(), false);
    }


    public void close()
    {
        lineReader.close();
    }

}
