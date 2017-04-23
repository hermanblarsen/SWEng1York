package com.i2lp.edi.client.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.i2lp.edi.client.Constants.*;

/**
 * Created by amriksadhra on 21/03/2017.
 */
public class Utils {
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Simple regex to determine if IP address is valid
     *
     * @param ip IP address to parse
     * @return Boolean corresponding to IP address validity
     * @author Amrik Sadhra
     */
    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }


    /**
     * Builds string for socket.IO connection
     *
     * @param serverIP   Socket.IO com.i2lp.edi.server IP address to connect to
     * @param serverPort Socket.IO com.i2lp.edi.server port to connect to
     * @return String containing concatenated result of serverIP and serverPort
     * @author Amrik Sadhra
     */
    public static String buildIPAddress(String serverIP, int serverPort) {
        return "http://" + serverIP + ":" + serverPort + "/";
    }

    /**
     * Generate the required CSS for a TextElement to fulfil parameters of text in XML
     *
     * @param fontSize   Font size in px
     * @param font       Desired font for text
     * @param fontColour Desired font colour provided as RGBA hex
     * @param bgColor    Desired background colour provided as RGBA hex
     * @return Filename of CSS file that stores the CSS for a given TextElement
     * @author Amrik Sadhra
     */
    public static String cssGen(String presentationID, int slideID, int elementID, int fontSize, String font, String fontColour, String bgColor, String borderColour, int borderSize) {
        File cssFilePath = new File(BASE_PATH + "Presentation" + presentationID + "/" + "Slide" + slideID + "Element" + elementID + "format.css");

        if (cssFilePath.exists()) {
            return "file:" + cssFilePath.getAbsolutePath();
        } else {
            cssFilePath.getParentFile().mkdirs(); //Create directory structure if not present yet
        }

        ArrayList<String> lines = new ArrayList<>();

        lines.add("body {");

        //Assume validity as XML parser has ensured is valid
        //This is the perfect place for default adding for element
        if (bgColor != null) lines.add("   background-color: " + bgColor + ";");
        if (fontColour != null) lines.add("   color: " + fontColour + ";");
        if (font != null) lines.add("   font-family: " + font + ";");
        if (fontSize != 0) lines.add("   font-size: " + fontSize + "px;");

        if (borderSize != 0) {
            lines.add("   border-style: solid;");
            lines.add("   border-width: " + borderSize + "px;");
            if (borderColour != null) lines.add("   border-color: " + borderColour + ";");
        }
        lines.add("}");

        logger.info("Writing runtime created CSS to " + cssFilePath);

        try {
            Files.write(cssFilePath.toPath(), lines);
        } catch (IOException e) {
            logger.error("Unable to create CSS file for text reference on Slide" + slideID + " element " + elementID);
        }

        return "file:" + cssFilePath;
    }

    /**
     * Check font size is valid and return it if it is. Else return XML default.
     *
     * @param size Font size string parsed from XML to check for validity
     * @return Default/Valid slide for presentation
     * @author Amrik Sadhra
     */
    public static int checkValidFontSize(int size, Theme presentationDefaults) {
        if ((size <= MAX_FONT_SIZE) && (size > 0)) return size;
        if (presentationDefaults == null) {
            if (size > MAX_FONT_SIZE) {
                logger.warn("Invalid default font size specified in XML (Exceeds " + MAX_FONT_SIZE + "), defaulting to 12");
                return 12;
            } else {//Size smaller or equal to 0
                logger.warn("Invalid default font size specified in XML (Less than 0), defaulting to 12");
                return 12;
            }
        } else {
            if (size > MAX_FONT_SIZE) {
                logger.warn("Invalid font size specified in XML (Exceeds " + MAX_FONT_SIZE + "), defaulting to XML Default");
                return presentationDefaults.getFontSize();
            } else {//Size smaller or equal to 0
                logger.warn("Invalid font size specified in XML (Less than 0), defaulting to XML Default");
                return presentationDefaults.getFontSize();
            }

        }
    }

    /**
     * Validate hex with regular expression
     *
     * @param colour Hex colour for validation
     * @return true valid hex, false invalid hex
     * @author Amrik Sadhra
     */
    public static String checkValidColour(String colour, Theme presentationDefaults) {
        String HEX_PATTERN = "^#([A-Fa-f0-9]{8})$";
        Pattern pattern = Pattern.compile(HEX_PATTERN);
        Matcher matcher = pattern.matcher(colour);

        //If valid, return colour
        if (matcher.matches()) return colour;

        //If we're checking presentation default, and its invalid
        if (presentationDefaults == null) {
            logger.warn("Invalid default RGBA Colour specified in XML, defaulting to: " + FALLBACK_COLOUR);
            return FALLBACK_COLOUR;
        } else {
            logger.warn("Invalid RGBA Colour specified in XML, defaulting to XML Default");
            return presentationDefaults.getFontColour();
        }
    }

    /**
     * Check font is valid (CSS-able) and return it if it is. If invalid, use theme defaults.
     *
     * @param font                 Font string parsed from XML to check for validity
     * @param presentationDefaults Signify whether we are checking the validity of the default field. If invalid, return NO_DEFAULT_FONT_SIZE constant.
     * @return Valid font
     * @author Amrik Sadhra
     */
    public static String checkValidFont(String font, Theme presentationDefaults) {
        //If font valid, return font
        if (Arrays.asList(VALID_FONTS).contains(font)) return font;

        //If checking presentation defaults and font invalid, return Comic Sans
        if (presentationDefaults == null) {
            logger.warn("Invalid default Font specified in XML, defaulting to " + VALID_FONTS[0]);
            return VALID_FONTS[0]; //Return comic sans xD
        } else {
            logger.warn("Invalid Font specified in XML, defaulting to " + presentationDefaults.getFont());
            return presentationDefaults.getFont(); //Return default presentation font
        }
    }

    public static ArrayList<String> getFilesInFolder(String path){
        ArrayList<String> filesInFolder = new ArrayList<>();
        //Set target path to read list of present files from
        final File folder = new File(path);

        //Generate array of files in folder
        for (File fileEntry : folder.listFiles()) {
            filesInFolder.add(fileEntry.getName());
        }

        return filesInFolder;
    }
}

