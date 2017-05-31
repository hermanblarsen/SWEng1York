package com.i2lp.edi.server.packets;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private String subjectName;
    private Timestamp go_live_timestamp;

    //Temporary
    private String documentID;

    public PresentationMetadata(int presentation_id, int module_id, int current_slide_number, String xml_url, boolean live, Timestamp go_live_timestamp) {
        this.presentation_id = presentation_id;
        this.module_id = module_id;
        this.current_slide_number = current_slide_number;
        this.xml_url = xml_url;
        this.live = live;
        this.subjectName = "Subject " + module_id;
        this.go_live_timestamp = go_live_timestamp;
    }


    /**
     * Use RegEx to retrieve the DocumentId from the XML_URL so DocId doesnt have to be stored on database
     * @return DocumentID of presentation
     */
    public String getDocumentID(){
        String HEX_PATTERN = "https?\\:\\/\\/(?:www\\.)?amriksadhra\\.com\\/Edi\\/([^\\.]+)\\.zip";
        Pattern pattern = Pattern.compile(HEX_PATTERN);
        Matcher matcher = pattern.matcher(xml_url);

        if (matcher.find()) return matcher.group(1);
        else return MISSING_DOCUMENT_ID;
    }

    public String getModuleName() { return "DashModule" + module_id; }

    public int getPresentationID() {
        return presentation_id;
    }

    //Temp hack for Koen offline mode
    public void setDocumentID(String documentID){
        this.documentID = documentID;
    }

    public String getXml_url() {
        return xml_url;
    }

    public String getSubjectName() { return subjectName; }

    public boolean getLive() { return live; }

    public void setLive(boolean live) {
        this.live = live;
    }

    public int getModule_id() { return module_id; }

    public void setGoLiveTimestamp(LocalDateTime goLiveTimeDateTime) { go_live_timestamp = Timestamp.valueOf(goLiveTimeDateTime); }

    public Timestamp getGoLiveTimestamp() { return go_live_timestamp; }
}
