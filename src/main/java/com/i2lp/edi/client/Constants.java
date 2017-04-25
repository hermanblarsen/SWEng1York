package com.i2lp.edi.client;

import javafx.stage.Screen;

/**
 * Created by amriksadhra on 14/04/2017.
 */
public class Constants {
    //TODO? we could break the version into majorVersion, minorVersion, incrementalVersion, buildNumber and qualifier,
     // see https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#MAVEN8903

    //Build number and string for use in About dialogues.
    private static final String BUILD_NUMBER = "30";
    public static final String BUILD_STRING = "Version: v0." + Constants.BUILD_NUMBER;

    public static final String BASE_PATH = System.getProperty("java.io.tmpdir") + "Edi/";

    /* XML Input validation */
    //Valid Fonts taken from https://www.w3schools.com/cssref/css_websafe_fonts.asp
    public static final String[] VALID_FONTS= {"\"Comic Sans MS\", cursive, sans-serif", "\"Courier New\", Courier, monospace", "\"Lucida Console\", Monaco, monospace", "Verdana, Geneva, sans-serif", "\"Trebuchet MS\", Helvetica, sans-serif", "Tahoma, Geneva, sans-serif", "\"Lucida Sans Unicode\", \"Lucida Grande\", sans-serif", "Impact, Charcoal, sans-serif", "\"Arial Black\", Gadget, sans-serif", "Arial, Helvetica, sans-serif", "\"Times New Roman\", Times, serif", "\"Palatino Linotype\", \"Book Antiqua\", Palatino, serif", "Georgia, serif"};
    public static final String[] VALID_ONCLICK_ACTIONS = {"dynamicmediatoggle", "gotoslide", "openwebsite"};
    public static final String FALLBACK_ONCLICK_ACTION = "none";
    public static final int MAX_FONT_SIZE = 100; //Upper bound for font sizes on presentation
    public static final String FALLBACK_COLOUR = "#000000FF"; //Used as default colour when Presentation defaults are invalid

    /* ELEMENT RENDERING CONSTANTS */
    public static final int TEXT_ELEMENT_ZOOM_FACTOR = 2;
    public static final int SCREEN_WIDTH = (int) Screen.getPrimary().getVisualBounds().getWidth();
}

