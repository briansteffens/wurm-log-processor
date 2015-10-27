package net.coldplace.wurm.logparser;

import org.joda.time.*;

public interface Uptime {

    public Duration getUptime();
    public DateTime getLastRestart();
    public DateTime getLastSkillReset();

}
