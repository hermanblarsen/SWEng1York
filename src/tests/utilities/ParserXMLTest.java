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
        assertNotNull(myPresentation);
        assertTrue(myPresentation instanceof Presentation);
    }

    @Test
    public void verifyDocumentID () {
        assertEquals(myPresentation.getDocumentID(), "sampleinput");
    }

    @Test
    public void verifyDocumentDetails () {
        assertEquals(myPresentation.getAuthor(), "Joe Bloggs");
        assertEquals(myPresentation.getVersion(), 1.0, 0.01);
        assertEquals(myPresentation.getDocumentAspectRatio(), 1.78, 0.01);
        assertEquals(myPresentation.getDescription(), "Sample XML input");
        assertEquals(myPresentation.getTags(), "sample,xml,input");
        assertEquals(myPresentation.getGroupFormat(), 2, 0.01);
    }

    @Test
    public void verifySlideshowDefaults () {
        assertNotNull(myPresentation.getTheme());
        assertEquals(myPresentation.getTheme().getBackgroundColour(), "#FF2304");
        assertEquals(myPresentation.getTheme().getFont(), "Arial");
        assertEquals(myPresentation.getTheme().getFontSize(),12, 0);
        assertEquals(myPresentation.getTheme().getFontColour(), "#F00D33FF");
        assertEquals(myPresentation.getTheme().getGraphicsColour(), "#F40D33FF");
        assertEquals(myPresentation.isAutoplayMedia(), true);
    }

    @Test
    public void verifySlideArray () {
        assertNotNull(myPresentation.getSlideList());
        for (int i = 0; i < 3; i++) {
            assertEquals(myPresentation.getSlideList().get(i).getSlideID(), (i+1), 0);
        }
    }

    @Test
    public void verifyElementArray () {
        assertNotNull(myPresentation.getSlideList().get(0).getSlideElementList());
        //assertEquals(myPresentation.getSlideList().get(0).getSlideElementList().size(), 2, 0 );
    }


    @After
    public void tearDown() throws Exception {
        //Do nothing
    }
}
