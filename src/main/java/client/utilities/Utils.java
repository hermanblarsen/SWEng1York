package client.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by amriksadhra on 21/03/2017.
 */
public class Utils {
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }


    public static String buildIPAddress(String serverIP, int serverPort) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(serverIP);
        sb.append(":");
        sb.append(serverPort);
        sb.append("/");

        return sb.toString();
    }

    /**
     * Generate the required CSS for a TextElement to fulfil parameters of text in XML
     *
     * @param fontSize   Font size in px
     * @param font       Desired font for text
     * @param fontColour Desired font colour provided as ARGB hex
     * @param bgColor    Desired background colour provided as ARGB hex
     * @return Filename of CSS file that stores the CSS for a given TextElement
     */
    public static String cssGen(int presentationID, int slideID, int elementID, int fontSize, String font, String fontColour, String bgColor, String borderColour, int borderSize) {
        Path tempDirectoryPath = Paths.get(System.getProperty("java.io.tmpdir") + "/EdiPresentationResources/" + "Presentation" + presentationID + "/");

        try {
            tempDirectoryPath = Files.createDirectories(tempDirectoryPath);
        } catch (IOException e) {
            logger.error("Unable to create temporary directory with which to store runtime presentation resources");
        }

        ArrayList<String> lines = new ArrayList<>();

        lines.add("body{");
        //TODO: Check validity of each passed in parameter? This would take a while, would ensure we keep program sanity when XML is invalid. But does our development assume perfect XML?
        //This is the perfect place for default adding for element
        if(bgColor != null) lines.add("   background-color: " + bgColor + ";");
        if(fontColour != null) lines.add("   color: " + fontColour + ";");
        if(font != null) lines.add("   font-family: " + font + ";");
        if(fontSize != 0) lines.add("   font-size: " + fontSize + "px;");

        if(borderSize != 0){
            lines.add("     border-style: solid;");
            lines.add("     border-width: " + borderSize + "px;");
            if(borderColour != null) lines.add("    border-color: " + borderColour + ";");
        }
        lines.add( "}");

        String cssFileName = "Slide" + slideID + "Element" + elementID + "format.css";
        String cssFilePath = tempDirectoryPath + "/" + cssFileName;

        logger.info("Writing runtime created CSS to " + cssFilePath);

        try {

            Files.write(Paths.get(cssFilePath), lines);
        } catch (IOException e) {
            logger.error("Unable to create CSS file for text reference on Slide" + slideID + " element " + elementID);
        }

        return "file:" + cssFilePath;
    }
}
