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

    private static final double ERROR_MARGIN = 0.01;
    private ParserXML myParser;
    private Presentation myPresentation;
    private Theme myTheme;
    private ArrayList<Slide>  mySlideArray;
    private ArrayList mySlideElementArray1;
    private ArrayList mySlideElementArray2;
    private ArrayList mySlideElementArray3;
    private ArrayList mySlideElementArray4;

    @Before
    public void setUp() throws Exception {
        myParser = new ParserXML("externalResources/sampleXml.xml");
        myPresentation = myParser.parsePresentation();
        myTheme = myPresentation.getTheme();
        mySlideArray = (ArrayList<Slide>) myPresentation.getSlideList();
        mySlideElementArray1 = (ArrayList) myPresentation.getSlideList().get(0).getSlideElementList();
        mySlideElementArray2 = (ArrayList) myPresentation.getSlideList().get(1).getSlideElementList();
        mySlideElementArray3 = (ArrayList) myPresentation.getSlideList().get(2).getSlideElementList();
        mySlideElementArray4 = (ArrayList) myPresentation.getSlideList().get(3).getSlideElementList();
    }

    @Test
    public void verifyCreationOfPresentation() {
        assertNotNull(myPresentation);
        assertTrue(myPresentation instanceof Presentation);
    }

    @Test
    public void verifyDocumentID () {
        assertEquals("sampleinput", myPresentation.getDocumentID());
    }

    @Test
    public void verifySpecificDocumentDetails () {
        assertEquals("Joe Bloggs", myPresentation.getAuthor());
        assertEquals(1.0, myPresentation.getVersion(), ERROR_MARGIN);
        assertEquals( 1.78, myPresentation.getDocumentAspectRatio(), ERROR_MARGIN);
        assertEquals("Sample XML input", myPresentation.getDescription());
        assertEquals("sample,xml,input" ,myPresentation.getTags());
        assertEquals(2, myPresentation.getGroupFormat(), ERROR_MARGIN);
    }

    @Test
    public void verifySlideshowDefaults () {
        assertNotNull(myTheme);
        assertEquals("#FF2304", myTheme.getBackgroundColour());
        assertEquals("Arial", myTheme.getFont());
        assertEquals(12, myTheme.getFontSize(), ERROR_MARGIN);
        assertEquals("#F00D33FF", myTheme.getFontColour() );
        assertEquals("#F40D33FF", myTheme.getGraphicsColour());
        assertEquals(true, myPresentation.isAutoplayMedia());
    }

    @Test
    public void verifySlideArrayAndId () {
        assertNotNull(myPresentation.getSlideList());

        for (int i = 0; i < mySlideArray.size(); i++) {
            assertEquals((i+1), mySlideArray.get(i).getSlideID(), ERROR_MARGIN);
        }
    }

    @Test
    public void verifyElementArraysAndSize () {
        assertNotNull(mySlideElementArray1);
        assertNotNull(mySlideElementArray2);
        assertNotNull(mySlideElementArray3);
        assertNotNull(mySlideElementArray4);

        assertEquals(2, mySlideElementArray1.size(), ERROR_MARGIN );
        assertEquals(3, mySlideElementArray2.size(), ERROR_MARGIN );
        assertEquals(2, mySlideElementArray3.size(), ERROR_MARGIN );
        assertEquals(1, mySlideElementArray4.size(), ERROR_MARGIN );
    }

    @Test
    public void verifyElementContent () {
        //TODO this needs to downcast the objects somehow from SlideElement to its actual element for us to use specific methods
        assertNotNull(mySlideElementArray1.get(0));
        assert(mySlideElementArray1.get(0) instanceof TextElement);
        assertEquals(1, ((TextElement) mySlideElementArray1.get(0)).getElementID(), ERROR_MARGIN);
        assertEquals("#EEEEEEFF", ((TextElement) mySlideElementArray1.get(0)).getFontColour());
        assertEquals(1.2, ((TextElement) mySlideElementArray1.get(0)).getElementAspectRatio(), ERROR_MARGIN);

        assertNotNull(mySlideElementArray2.get(1));
        assert(mySlideElementArray2.get(1) instanceof AudioElement);
//        System.out.println(mySlideElementArray2.get(0).getClass().toString());
//        System.out.println(((AudioElement)mySlideElementArray2.get(0)).getElementID());
    }


    @After
    public void tearDown() throws Exception {
        System.out.println(""); //Space out log
        // Do nothing
    }
}
