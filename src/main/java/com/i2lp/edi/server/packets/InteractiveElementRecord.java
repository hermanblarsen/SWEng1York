package com.i2lp.edi.server.packets;

import java.sql.Time;

/**
 * Created by amriksadhra on 30/05/2017.
 */
public class InteractiveElementRecord {
    int interactive_element_id;//The unique id
    int presentation_id;
    String interactive_element_data;


    int xml_element_id;
    public String getType() {
        return type;
    }
    String type;
    boolean isLive;
    Time response_interval;
    int slide_number;

    public InteractiveElementRecord(int interactive_element_id,
                                    int presentation_id,
                                    String interactive_element_data,
                                    String type, boolean isLive,
                                    Time response_interval,
                                    int slide_number,
                                    int xml_element_id
    ) {
        this.interactive_element_id = interactive_element_id;

        this.presentation_id = presentation_id;
        this.interactive_element_data = interactive_element_data;
        this.type = type;
        this.isLive = isLive;
        this.response_interval = response_interval;
        this.slide_number = slide_number;
        this.xml_element_id = xml_element_id;
    }

    public int getInteractive_element_id() {
        return interactive_element_id;
    }

    public boolean isLive() {
        return isLive;
    }

    public Time getResponse_interval() {
        return response_interval;
    }

    public int getXml_element_id() {
        return xml_element_id;
    }


}
