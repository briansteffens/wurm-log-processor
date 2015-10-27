package net.coldplace.wurm.logparser;

public abstract class ExtractorBase implements Extractor {

    Session session;
    protected Session getSession() { return session; }

    public int getPriority() { return 0; }

    public void init(Session session) {
        this.session = session;
    }

    public void handle(Event e) { }

    public void pulse() { }

}
