package com.i2lp.edi.client;

/**
 * Created by amriksadhra on 14/04/2017.
 */
public class Constants {

    /* Development Debug Constants */
    public static final String DEVELOPMENT_MODE = "teacher"; //Change user type
    public static boolean developerOffline = false; //Enable offline Edi

    public static final String localServerAddress = "127.0.0.1";
    public static final String remoteServerAddress = "db.amriksadhra.com";
    public static final boolean localServer = false; //Set if making changes to server logic and wish to test locally

    //Build number and string for use in About dialogues.
    private static final String BUILD_NUMBER = "38";
    public static final String BUILD_STRING = "Version: v0." + Constants.BUILD_NUMBER;

    public static final String BASE_PATH = System.getProperty("java.io.tmpdir") + "Edi/";
    public static final String TEMP_PATH = BASE_PATH + "Temp/";
    public static final String PRESENTATIONS_PATH = BASE_PATH + "Presentations/";

    //FTP details for presentation upload
    public static final String FTP_USER = "bscftp";
    public static final String FTP_PASS = "Combline90+";

    //Slide thumbnail size
    public static final double THUMBNAIL_WIDTH = 320;

    /* XML Input validation */
    //Valid Fonts taken from https://www.w3schools.com/cssref/css_websafe_fonts.asp
    public static final String[] VALID_FONTS= {
            "\"Comic Sans MS\", cursive, sans-serif",
            "\"Courier New\", Courier, monospace",
            "\"Lucida Console\", Monaco, monospace",
            "Verdana, Geneva, sans-serif",
            "\"Trebuchet MS\", Helvetica, sans-serif",
            "Tahoma, Geneva, sans-serif",
            "\"Lucida Sans Unicode\", \"Lucida Grande\", sans-serif",
            "Impact, Charcoal, sans-serif",
            "\"Arial Black\", Gadget, sans-serif",
            "Arial, Helvetica, sans-serif",
            "\"Times New Roman\", Times, serif",
            "\"Palatino Linotype\", \"Book Antiqua\", Palatino, serif",
            "Georgia, serif"};
    public static final String[] VALID_ONCLICK_ACTIONS = {"dynamicmediatoggle", "gotoslide", "openwebsite"};
    public static final String FALLBACK_ONCLICK_ACTION = "none";
    public static final int MAX_FONT_SIZE = 100; //Upper bound for font sizes on presentation
    public static final String FALLBACK_COLOUR = "#000000FF"; //Black used as default colour when Presentation defaults are invalid

    /* Server Interaction Constants */
    public static final String MISSING_DOCUMENT_ID = "NoID"; //Used by getDocumentID function when the XML_URL doesnt contain a valid DocumentID

    /* ELEMENT RENDERING CONSTANTS */
    public static final int TEXT_ELEMENT_ZOOM_FACTOR = 2;

    /* THUMBNAIL RENDER CONSTANTS */
    public static final int THUMBNAIL_GEN_WIDTH = 320;
    public static final int THUMBNAIL_GEN_HEIGHT = 240;
    public static final int PRINT_WIDTH_300DPI = 3508;
    public static final int PRINT_HEIGHT_300DPI = 2480;

}

