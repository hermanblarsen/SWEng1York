/**
 * Created by habl on 26/02/2017.
 */
package utilities;

        import static org.junit.Assert.*;

        import java.util.ArrayList;

        import org.junit.After;
        import org.junit.Before;
        import org.junit.Test;

public class ParserXMLTest {

    private ParserXML myParser;
    private Presentation myPresentation;
    private ArrayList<Slide> mySlideArray;
    private ArrayList<SlideElement> mySlideElementArray;

    @Before
    public void setUp() throws Exception {
        myParser = new ParserXML("externalResources/sampleXml.xml");
        myPresentation = myParser.parsePresentation();
    }

    @Test
    public void verifyCreationOfPresentation() {
        assertTrue(myPresentation instanceof Presentation);
    }

    @After
    public void tearDown() throws Exception {
        //Do nothing
    }
}
