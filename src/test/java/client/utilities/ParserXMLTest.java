/**
 * Created by habl on 26/02/2017.
 */
package client.utilities;

        import static org.junit.Assert.*;

        import java.util.ArrayList;

        import client.presentationElements.*;
        import org.junit.After;
        import org.junit.Before;
        import org.junit.Test;

public class ParserXMLTest {

    private static final double ERROR_MARGIN = 0.01;
    private ParserXML parserXML;
    private Presentation presentation;
    private Theme theme;
    private ArrayList<Slide> slideArray;
    private Slide slide1;
    private Slide slide2;
    private Slide slide3;
    private Slide slide4;
    private ArrayList slideElementArray1;
    private ArrayList slideElementArray2;
    private ArrayList slideElementArray3;
    private ArrayList slideElementArray4;

    @Before
    public void setUp() throws Exception {
        parserXML = new ParserXML("externalResources/sampleXml.xml");
        presentation = parserXML.parsePresentation();
        theme = presentation.getTheme();
        slideArray = (ArrayList<Slide>) presentation.getSlideList();

        slide1 = slideArray.get(0);
        slide2 = slideArray.get(1);
        slide3 = slideArray.get(2);
        slide4 = slideArray.get(3);

        slideElementArray1 = (ArrayList) slide1.getSlideElementList();
        slideElementArray2 = (ArrayList) slide2.getSlideElementList();
        slideElementArray3 = (ArrayList) slide3.getSlideElementList();
        slideElementArray4 = (ArrayList) slide4.getSlideElementList();
    }

    @Test
    public void verifyCreationOfPresentation() {
        assertNotNull(presentation);
        assertTrue(presentation instanceof Presentation);
    }

    @Test
    public void verifyCreationOfSlides () {
        for (int i = 0; i < slideArray.size(); i++) {
            assertNotNull(slideArray.get(i));
        }
    }

    @Test
    public void verifyCreationOfSlideElements () {
        for (int i = 0; i < slideElementArray1.size(); i++) assertNotNull(slideElementArray1.get(i));
        for (int i = 0; i < slideElementArray2.size(); i++) assertNotNull(slideElementArray2.get(i));
        for (int i = 0; i < slideElementArray3.size(); i++) assertNotNull(slideElementArray3.get(i));
        for (int i = 0; i < slideElementArray4.size(); i++) assertNotNull(slideElementArray4.get(i));
    }


    @Test
    public void verifyDocumentID () {
        assertEquals("sampleinput", presentation.getDocumentID());
    }

    @Test
    public void verifySpecificDocumentDetails () {
        assertEquals("Joe Bloggs", presentation.getAuthor());
        assertEquals(1.0, presentation.getVersion(), ERROR_MARGIN);
        assertEquals( 1.78, presentation.getDocumentAspectRatio(), ERROR_MARGIN);
        assertEquals("Sample XML input", presentation.getDescription());
        assertEquals("sample,xml,input" , presentation.getTags());
        assertEquals(2, presentation.getGroupFormat(), ERROR_MARGIN);
    }

    @Test
    public void verifySlideshowDefaultsAndTheme () {
        assertNotNull(theme);
        assertEquals("#FF2304", theme.getBackgroundColour());
        assertEquals("Arial", theme.getFont());
        assertEquals(12, theme.getFontSize(), ERROR_MARGIN);
        assertEquals("#F00D33FF", theme.getFontColour() );
        assertEquals("#F40D33FF", theme.getGraphicsColour());
        assertEquals(true, presentation.isAutoplayMedia());
    }

    @Test
    public void verifySlideArrayAndIds () {
        for (int i = 0; i < slideArray.size(); i++) assertEquals((i + 1), slideArray.get(i).getSlideID(), ERROR_MARGIN);
    }

    @Test
    public void verifyElementArraySizes () {
        assertEquals(2, slideElementArray1.size(), ERROR_MARGIN );
        assertEquals(3, slideElementArray2.size(), ERROR_MARGIN );
        assertEquals(2, slideElementArray3.size(), ERROR_MARGIN );
        assertEquals(1, slideElementArray4.size(), ERROR_MARGIN );
    }

    @Test
    public void verifySlide1ElementContent () {
        for (int i = 0; i < slideElementArray1.size(); i++) assertNotNull(slideElementArray1.get(i));
        ArrayList<TextElement> slide1TextElementArray = (ArrayList)slide1.getTextElementList();
        for (int i = 0; i < slide1TextElementArray.size(); i++) assertNotNull(slide1TextElementArray.get(i));

        assert(slideElementArray1.get(0) instanceof TextElement);
        assert(slide1TextElementArray.get(0) instanceof TextElement);
        //Assert the two object are the same
        assertEquals(slideElementArray1.get(0), slide1TextElementArray.get(0));

        TextElement slide1TextElement1 = slide1TextElementArray.get(0);


        assertEquals(1, slide1TextElement1.getElementID(), ERROR_MARGIN);
        assertEquals(1 , slide1TextElement1.getLayer());
        assertTrue(slide1TextElement1.isVisibility());
        assertEquals( 2, slide1TextElement1.getStartSequence(), ERROR_MARGIN);
        assertEquals( 4, slide1TextElement1.getEndSequence(), ERROR_MARGIN);
        assertEquals( 20.5, slide1TextElement1.getDuration(), ERROR_MARGIN);
        assertEquals("#EEEEEEFF", slide1TextElementArray.get(0).getFontColour());
        assertEquals(1.2, slide1TextElementArray.get(0).getElementAspectRatio(), ERROR_MARGIN);


//        assertEquals( , );
//        assertEquals( , );
//        assertEquals( , );
//        assertEquals( , );
    }

    @Test
    public void verifyOtherContent () {
        for (int i = 0; i < slideElementArray2.size(); i++) assertNotNull(slideElementArray2.get(i));

        assert(slideElementArray2.get(0) instanceof GraphicElement);
        assert(slideElementArray2.get(1) instanceof GraphicElement);
        assert(slideElementArray2.get(2) instanceof AudioElement);

        assertNotNull(slideElementArray3.get(1));
        assert(slideElementArray2.get(0) instanceof GraphicElement);
        assert(slideElementArray2.get(1) instanceof GraphicElement);
        assert(slideElementArray2.get(2) instanceof AudioElement);
    }


    @After
    public void tearDown() throws Exception {
        System.out.println(""); //Space out log
        // Do nothing
    }
}
