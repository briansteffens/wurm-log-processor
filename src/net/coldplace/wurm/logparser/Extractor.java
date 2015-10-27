package net.coldplace.wurm.logparser;

public interface Extractor {

    public int getPriority();

    public void init(Session session);

    public void handle(Event e);

    public void pulse();

}
