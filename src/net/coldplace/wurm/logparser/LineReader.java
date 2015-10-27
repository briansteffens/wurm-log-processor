package net.coldplace.wurm.logparser;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joda.time.*;


public class LineReader implements AutoCloseable
{
    String pathFormat;

    BufferedReader bufferedReader;
    String currentFileName;

    DateTime currentTime;
    DateTime nextTime;

    long currentPosition;
    long nextPosition;

    String previous;
    String current;
    String next;
  

    public String getPrevious() { return previous; }
    public String getCurrent() { return current; }
    public String getNext() { return next; }


    public int getMonth() { return currentTime.getMonthOfYear(); }
    public int getYear() { return currentTime.getYear(); }


    public LineReader(String pathFormat, int startMonth, int startYear)
    {
        this.pathFormat = pathFormat;
        this.nextTime = new DateTime(startYear, startMonth, 1, 0, 0).plusMonths(-1);
    }


    // Returns the next (chronologically) filename based on pathFormat or null
    // if there are no more left.
    String nextFilename()
    {
        String filename = null;
        DateTime now = new DateTime();

        while (true)
        {
            nextTime = nextTime.plusMonths(1);

            filename = String.format(pathFormat,
                                     nextTime.getYear(),
                                     nextTime.getMonthOfYear());

            File f = new File(filename);

            // If file exists, return the filename
            if (f.isFile())
                return filename;

            // Filename wasn't valid. If it was the most recent there are none
            // left to tr.
            if (nextTime.isAfter(now))
                return null;
        }
    }


    // Next time open() is called, it should open the next file instead of
    // trying to reopen the current one.
    void nextFile()
    {
        currentFileName = null;
        close();
    }


    boolean open() throws IOException
    {
        // If we have no currentFileName, generate the next one. Otherwise
        // reopen the current file.
        if (currentFileName == null)
        {
            currentFileName = nextFilename();

            // No next filename available.
            if (currentFileName == null)
                return false;

            // Reset the filePosition only if it's not the most recent file.
            currentPosition = nextPosition = 0;
        }

        try
        {
            bufferedReader = new BufferedReader(
                             new InputStreamReader(
                             new FileInputStream(currentFileName)));
        }
        catch (FileNotFoundException ex)
        {
            // The file was checked in nextFilename(), so if it was deleted 
            // since then not a lot can be done.
            throw new Error(ex);
        }

        bufferedReader.skip(nextPosition);

        return true;
    }


    int readCalls = 0;

    public String read() throws IOException
    {
        //System.out.println(next);

        if (readCalls < 2)
            readCalls++;

        currentTime = nextTime;


        String nextLine = null;

        // Try at most 2 times to read the next line. If the first try returns
        // null, try the next file.
        for (int tries = 1; tries <= 2; tries++)
        {
            // No file open. Could be the first run or a seam between log files.
            if (bufferedReader == null)
                if (!open())
                    break;

            nextLine = bufferedReader.readLine();

            // Got a valid line, break loop
            if (nextLine != null)
                break;

            // End of file. Only advance to the next file if this is not
            // already the most recent log file.
            DateTime now = new DateTime();
            if (nextTime.getMonthOfYear() != now.getMonthOfYear() ||
                nextTime.getYear() != now.getYear())
                nextFile();
        }


        previous = current;
        current = next;
        next = nextLine;

        currentPosition = nextPosition;
        if (next != null)
            nextPosition += next.length() + 1;

        // Since we're always reading ahead, the first call would be null.
        if (readCalls < 2)
            return read();
        
        return current;
    }


    public void close()
    {
        if (bufferedReader != null)
            try 
            {
                bufferedReader.close();
                bufferedReader = null;
            }
            catch (IOException ex) 
            {
                throw new Error(ex);
            }
    }
    

}

/*
public class LogReader implements AutoCloseable 
{
    String logType;
    String pathFormat;
    DateTime start;
    DateTime time;
    Boolean historical = true;

    BufferedReader bufferedReader;
    long filePosition;

    Pattern startsWithDate;

    final String NL = System.getProperty("line.separator");

    public LogReader(String logType, String pathFormat, DateTime start) 
    {
        this.logType = logType;
        this.pathFormat = pathFormat;
        this.time = this.start = start;

        startsWithDate = Pattern.compile(
            "^\\[[0-9]{2}:[0-9]{2}:[0-9]{2}\\] ");
    }

    public void close() 
    {
        if (bufferedReader != null)
            try 
            {
                bufferedReader.close();
                bufferedReader = null;
            }
            catch (IOException ex) 
            {
                throw new Error(ex);
            }
    }

    String makePath() 
    {
        return String.format(pathFormat, 
                             time.getYear(), 
                             time.getMonthOfYear());
    }

    String storeLine = null;
    DateTime lastEventTimestamp = null;

    Boolean tempRollover = false;
    public Boolean getTempRollover() { return tempRollover; }
    public void setTempRollover(Boolean val) { tempRollover = val; }

    public List<Event> read(int maxEvents) throws IOException 
    {
        int eventsRead = 0;
        List<Event> ret = new ArrayList<Event>();

        while (true) 
        {
            String line = null;

            if (bufferedReader == null) 
            {
                String filename = null;
                DateTime now = new DateTime();

                while (true) 
                {
                    filename = makePath();
                    File f = new File(filename);

                    if (f.isFile() && filePosition != f.length())
                        break;

                    if (now.getMonthOfYear() == time.getMonthOfYear() &&
                        now.getYear() == time.getYear()) 
                    {
                        if (ret.size() > 0)
                            return ret;

                        historical = false;
                        close();

                        return null;
                    }

                    filePosition = 0;
                    time = time.plusMonths(1);
                }


                bufferedReader = new BufferedReader(
                                 new InputStreamReader(
                                 new FileInputStream(filename)));

                bufferedReader.skip(filePosition);
            }
            else if (storeLine != null) 
            {
                line = storeLine;
                storeLine = null;
            }
            
            Boolean fileEnded = false;
                
            while (true) 
            {
                StringBuilder sb = new StringBuilder();

                if (line != null)
                    sb.append(line);

                while (true) 
                {
                    line = bufferedReader.readLine();

                    if (line == null) 
                    {
                        fileEnded = true;
                        break;
                    }

                    if (line.startsWith("Logging started ")) 
                    {
                        // If there's already some data in the buffer, process
                        // that first. Skip this 'Logging started' line and
                        // process it later. Otherwise the previous event will
                        // have the wrong day.
                        if (sb.length() != 0)
                            break;

                        lastEventTimestamp = null;

                        time = new DateTime(
                            Integer.parseInt(line.substring(16, 20)),
                            Integer.parseInt(line.substring(21, 23)),
                            Integer.parseInt(line.substring(24, 26)),
                            0, 0, 0);

                        filePosition += line.length() + 1;

                        continue;
                    }

                    if (sb.length() != 0 && 
                        startsWithDate.matcher(line).find())
                        break;

                    if (sb.length() > 0)
                        sb.append(NL);

                    sb.append(line);
                }

                if (!fileEnded && sb.length() == 0)
                    continue;

                if (sb.length() > 0) {
                    String text = sb.toString();

                    eventsRead++;
                    filePosition += text.length() + 1;
                    
                    time = time.withTime(
                        Integer.parseInt(text.substring(1, 3)),
                        Integer.parseInt(text.substring(4, 6)),
                        Integer.parseInt(text.substring(7, 9)),
                        0);

                    if (lastEventTimestamp != null && 
                        lastEventTimestamp.isAfter(time)) 
                    {
                        tempRollover = true;
                        time = time.plusDays(1);
                    }

                    lastEventTimestamp = time;
                    System.out.println(time + ": " + text.substring(11));
                    ret.add(new Event(logType, 
                                      time, 
                                      text.substring(11), 
                                      historical));
                }

                if (fileEnded) 
                {
                    bufferedReader.close();
                    bufferedReader = null;
                }

                if (eventsRead >= maxEvents) 
                {
                    storeLine = line;
                    return ret;
                }

                if (fileEnded)
                    break;
            }
        }
    }
}*/
