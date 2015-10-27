import java.util.*;
import java.io.*;

import org.joda.time.*;

import net.coldplace.wurm.logparser.*;
import net.coldplace.wurm.logparser.impl.*;

class ExtractorDetail
{
    Extractor extractor;
    public Extractor getExtractor() { return extractor; }

    String[] players;
    public String[] getPlayers() { return players; }

    public ExtractorDetail(Extractor extractor, String[] players)
    {
        this.extractor = extractor;
        this.players = players;
    }
}

public class Main 
{
    public static void main(String[] args) 
    {
        Session session = new Session();

        LineReader lineReader = new LineReader(
            "/home/brian/wurm/players/kirtah/logs/_Event.%d-%02d.txt", 8, 2014);

        LogReader lr = new LogReader("Kirtah", lineReader, session);


        EventHandler eventHandler = new TempEventHandler(session);
        session.registerEventHandler("meditation", eventHandler);
        session.registerEventHandler("pulse", eventHandler);
        session.registerEventHandler("next-hota", eventHandler);

        EventHandler ircHandler = new IrcEventHandler(session, "kirtah7");
        session.registerEventHandler("meditation", ircHandler);
        session.registerEventHandler("pulse", ircHandler);
        session.registerEventHandler("client-login", ircHandler);
        session.registerEventHandler("lockpicking", ircHandler);
        session.registerEventHandler("next-hota", ircHandler);

        session.registerAlertSender(new ConsoleAlertSender());

        String[] kirtah = new String[] { "Kirtah" };
        String[] allPlayers = new String[] { "Kirtah", "Hatrik", "Tiltar", "Iklam" };

        List<ExtractorDetail> extractors = new ArrayList<ExtractorDetail>();
        UptimeHandler uptime = new UptimeHandler();
        extractors.add(new ExtractorDetail(uptime, allPlayers));
        extractors.add(new ExtractorDetail(new SleepBonusHandler(), allPlayers));
        extractors.add(new ExtractorDetail(new MeditationHandler(uptime), kirtah));
        extractors.add(new ExtractorDetail(new NextHotaHandler(), allPlayers));
        extractors.add(new ExtractorDetail(new LockpickingHandler(), kirtah));

        for (ExtractorDetail e : extractors)
            e.getExtractor().init(session);


        try
        {
            // Historical log data
            while (true)
            {
                Event e = lr.read();

                if (e == null)
                    break;
                
                for (ExtractorDetail h : extractors)
                    if (Arrays.asList(h.getPlayers()).contains(e.getPlayer()))
                        h.getExtractor().handle(e);
            }

            lr.close();

            // Poll for new additions
            while (true)
            {
                while (true)
                {
                    Event e = lr.read();

                    if (e == null)
                        break;

                    for (ExtractorDetail h : extractors)
                        if (Arrays.asList(h.getPlayers()).contains(e.getPlayer()))
                            h.getExtractor().handle(e);
                }

                lr.close();

                for (ExtractorDetail h : extractors)
                    h.getExtractor().pulse();

                session.raiseEvent(null, new GenericEventArgs("pulse"));

                try { Thread.sleep(1000); } catch (InterruptedException ex) { }
            }
        }
        catch (IOException ex)
        {
            throw new Error(ex);
        }

/*
        

        

        try {
            while (true) {
                List<Event> es = lr.read(1);

                if (es == null)
                    break;

                for (Event e : es) {
                    //System.out.println(e.getTimestamp() + " - " + e.getText());
                    for (Extractor h : handlers)
                        h.handle(e);
                    /*if (lr.getTempRollover())
                        if (System.console().readLine().equals("done"))
                            lr.setTempRollover(false);*/
 /*               }

                //System.out.println(es.size());
            }

            while (true) {
                List<Event> es = lr.read(100);

                if (es != null)
                    for (Event e : es)
                        for (Extractor h : handlers)
                            h.handle(e);

                for (Extractor h : handlers)
                    h.pulse();

                session.raiseEvent(null, new GenericEventArgs("pulse"));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) { }
            }
        } 
        catch (IOException ex) {
            throw new Error(ex);
        }
        finally {
            lr.close();
        }
*/
    }
}
