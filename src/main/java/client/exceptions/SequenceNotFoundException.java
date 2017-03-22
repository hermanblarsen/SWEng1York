package client.exceptions;

public class SequenceNotFoundException extends Exception {
    public SequenceNotFoundException() {

    }

    public SequenceNotFoundException (String message) {
        super (message);
    }

    public SequenceNotFoundException (Throwable cause) {
        super (cause);
    }

    public SequenceNotFoundException (String message, Throwable cause) {
        super (message, cause);
    }
}