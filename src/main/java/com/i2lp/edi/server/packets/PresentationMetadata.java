package com.i2lp.edi.server.packets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.i2lp.edi.client.Constants.MISSING_DOCUMENT_ID;

/**
 * Created by amriksadhra on 03/05/2017.
 */
public class PresentationMetadata {
    private int presentation_id;
    private int module_id;
    private int current_slide_number;
    private String xml_url;
    private boolean live;

    public PresentationMetadata(int presentation_id, int module_id, int current_slide_number, String xml_url, boolean live) {
        this.presentation_id = presentation_id;
        this.module_id = module_id;
        this.current_slide_number = current_slide_number;
        this.xml_url = xml_url;
        this.live = live;
    }


    /**
     * Use RegEx to retrieve the DocumentId from the XML_URL so DocId doesnt have to be stored on database
     * @return DocumentID of presentation
     */
    public String getDocumentID(){
        String HEX_PATTERN = "https?\\:\\/\\/(?:www\\.)?amriksadhra\\.com\\/Edi\\/([^\\.]+)\\.xml";
        Pattern pattern = Pattern.compile(HEX_PATTERN);
        Matcher matcher = pattern.matcher(xml_url);

        if (matcher.find()) return matcher.group(1);
        else return MISSING_DOCUMENT_ID;
    }
}
