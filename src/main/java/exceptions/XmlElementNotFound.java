package exceptions;

/**
 * Created by habl on 16/03/2017.
 * Project: SWEng1York - Package: exceptions
 */
public class XmlElementNotFound extends Exception {
    public XmlElementNotFound() {

    }
    public XmlElementNotFound(String message) {
        super (message);
    }

    public XmlElementNotFound(Throwable cause) {
        super (cause);
    }

    public XmlElementNotFound(String message, Throwable cause) {
        super (message, cause);
    }
}

