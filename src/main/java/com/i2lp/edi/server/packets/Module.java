package com.i2lp.edi.server.packets;

import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created by habl on 19/04/2017.
 * Project: SWEng1York - Package: com.i2lp.edi.server.packets
 */
public class Module {
    int module_id;
    String description;
    String subject;
    Time time_last_updated;
    Timestamp time_created;
    String module_name;


    public Module(int module_id, String description, String subject, Time time_last_updated, Timestamp time_created, String module_name) {
        this.module_id = module_id;
        this.description = description;
        this.subject = subject;
        this.time_last_updated = time_last_updated;
        this.time_created = time_created;
        this.module_name = module_name;
    }
}
