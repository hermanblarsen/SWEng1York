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
    private Theme myTheme;
    private ArrayList<Slide> mySlideArray;
    private ArrayList<SlideElement> mySlideElementArray1;
    private ArrayList<SlideElement> mySlideElementArray2;
    private ArrayList<SlideElement> mySlideElementArray3;
    private ArrayList<SlideElement> mySlideElementArray4;

    @Before
    public void setUp() throws Exception {
        myParser = new ParserXML("externalResources/sampleXml.xml");
        myPresentation = myParser.parsePresentation();
        myTheme = myPresentation.getTheme();
        mySlideArray = (ArrayList) myPresentation.getSlideList();
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
        assertEquals(1.0, myPresentation.getVersion(), 0.01);
        assertEquals( 1.78, myPresentation.getDocumentAspectRatio(),0.01);
        assertEquals("Sample XML input", myPresentation.getDescription());
        assertEquals("sample,xml,input" ,myPresentation.getTags());
        assertEquals(2, myPresentation.getGroupFormat(), 0.01);
    }

    @Test
    public void verifySlideshowDefaults () {
        assertNotNull(myTheme);
        assertEquals("#FF2304", myTheme.getBackgroundColour());
        assertEquals("Arial", myTheme.getFont());
        assertEquals(12, myTheme.getFontSize(), 0);
        assertEquals("#F00D33FF", myTheme.getFontColour() );
        assertEquals("#F40D33FF", myTheme.getGraphicsColour());
        assertEquals(true, myPresentation.isAutoplayMedia());
    }

    @Test
    public void verifySlideArrayAndId () {
        assertNotNull(myPresentation.getSlideList());

        for (int i = 0; i < mySlideArray.size(); i++) {
            assertEquals((i+1), mySlideArray.get(i).getSlideID(), 0);
        }
    }

    @Test
    public void verifyElementArraysAndSize () {
        assertNotNull(mySlideElementArray1);
        assertNotNull(mySlideElementArray2);
        assertNotNull(mySlideElementArray3);
        assertNotNull(mySlideElementArray4);

        assertEquals(2, mySlideElementArray1.size(), 0 );
        assertEquals(3, mySlideElementArray2.size(), 0 );
        assertEquals(2, mySlideElementArray3.size(), 0 );
        assertEquals(1, mySlideElementArray4.size(), 0 );
    }

    @Test
    public void verifyElementContent () {
        //TODO this needs to downcast the objects somehow from SlideElement to its actual element for us to use specific methods
    }


    @After
    public void tearDown() throws Exception {
        System.out.println(""); //Space out log
        // Do nothing
    }
}
