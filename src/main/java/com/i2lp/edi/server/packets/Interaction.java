package com.i2lp.edi.server.packets;

import java.sql.Timestamp;

/**
 * Created by amriksadhra on 27/05/2017.
 */
public class Interaction {
    private int interaction_id;
    private int user_id;
    private int interactive_element_id;
    private String interaction_data;
    private Timestamp time_created;

    public Interaction(int interaction_id, int user_id, int interactive_element_id, String interaction_data, Timestamp time_created) {
        this.interaction_id = interaction_id;
        this.user_id = user_id;
        this.interactive_element_id = interactive_element_id;
        this.interaction_data = interaction_data;
        this.time_created = time_created;
    }


    public int getInteraction_id() {
        return interaction_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getInteractive_element_id() {
        return interactive_element_id;
    }

    public String getInteraction_data() {
        return interaction_data;
    }

    public Timestamp getTime_created() {
        return time_created;
    }

}
