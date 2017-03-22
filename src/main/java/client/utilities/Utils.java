package client.utilities;

import java.util.regex.Pattern;

/**
 * Created by amriksadhra on 21/03/2017.
 */
public class Utils {
    static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }


    public static String buildIPAddress(String serverIP, int serverPort){
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(serverIP);
        sb.append(":");
        sb.append(serverPort);
        sb.append("/");

        return sb.toString();
    }
}
