package com.i2lp.edi.server.packets;

/**
 * Created by amriksadhra on 03/05/2017.
 */
public class PresentationMetadata {
    private int presentation_id;
    private int module_id;
    private int current_slide_number;
    private String xml_url;

    public PresentationMetadata(int presentation_id, int module_id, int current_slide_number, String xml_url) {
        this.presentation_id = presentation_id;
        this.module_id = module_id;
        this.current_slide_number = current_slide_number;
        this.xml_url = xml_url;
    }
}
