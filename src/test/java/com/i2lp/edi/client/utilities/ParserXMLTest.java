/**
 * Created by habl on 26/02/2017.
 */
package com.i2lp.edi.client.utilities;
import com.i2lp.edi.client.animation.*;
import com.i2lp.edi.client.presentationElements.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ParserXMLTest {
    private static final double ERROR_MARGIN = 0.01;

    private Logger logger = LoggerFactory.getLogger(ParserXMLTest.class);
    private ParserXML parserXML;
    private Presentation presentation;
    private Theme theme;
    private ArrayList<Slide> slideArray;
    private Slide slide1;
    private Slide slide2;
    private Slide slide3;
    private Slide slide4;
    private ArrayList<SlideElement> slideElementArray1;
    private ArrayList<SlideElement> slideElementArray3;
    private ArrayList<SlideElement> slideElementArray4;
    private ArrayList<SlideElement> slideElementArray2;
    private String examplePath = "projectResources/sampleFiles/xmlTests/testSampleXml.xml";

    @Before
    public void setUp() throws Exception {
        parserXML = new ParserXML(examplePath);

        try {
            presentation = parserXML.parsePresentation();
        } catch (InvalidPathException exception) {
            logger.warn(exception.getMessage() + " due to " + exception.getCause());
        }

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
    public void testCreationOfPresentationAndID() {
        assertNotNull(presentation);
        assertTrue(presentation instanceof Presentation);

        assertEquals("sampleinput", presentation.getDocumentID());
    }


    @Test
    public void testCreationOfSlidesAndIDs() {
        for (Slide slide : slideArray) assertNotNull(slide);

        //Verify IDs
        for (int i = 0; i < slideArray.size(); i++) assertEquals((i + 1), slideArray.get(i).getSlideID(), ERROR_MARGIN);
    }


    @Test
    public void testCreationOfSlideElements () {
        for (SlideElement element : slideElementArray1) assertNotNull(element);
        for (SlideElement element : slideElementArray2) assertNotNull(element);
        for (SlideElement element : slideElementArray3) assertNotNull(element);
        for (SlideElement element : slideElementArray4) assertNotNull(element);
    }

    @Test
    public void testDocumentDetails() {
        assertEquals("Joe Bloggs", presentation.getAuthor());
        assertEquals(1.0, presentation.getVersion(), ERROR_MARGIN);
        assertEquals( 1.78, presentation.getDocumentAspectRatio(), ERROR_MARGIN);
        assertEquals("Sample XML input", presentation.getDescription());
        assertEquals("sample,xml,input" , presentation.getTags());
        assertEquals(2, presentation.getGroupFormat(), ERROR_MARGIN);
    }

    @Test
    public void testPresentationDefaults() {
        assertNotNull(theme);
        assertEquals("#FF2304FF", theme.getBackgroundColour());
        assertEquals("Arial, Helvetica, sans-serif", theme.getFont());
        assertEquals(12, theme.getFontSize(), ERROR_MARGIN);
        assertEquals("#F00D33FF", theme.getFontColour() );
        assertEquals("#F40D33FF", theme.getGraphicsColour());
        assertEquals(true, presentation.isAutoplayMedia());
    }

    @Test
    public void testElementArraySizes () {
        assertEquals(2, slideElementArray1.size(), ERROR_MARGIN );
        assertEquals(3, slideElementArray2.size(), ERROR_MARGIN );
        assertEquals(2, slideElementArray3.size(), ERROR_MARGIN );
        assertEquals(3, slideElementArray4.size(), ERROR_MARGIN );
    }

    @Test
    public void testSlide1TextElement1Content() {
        for (SlideElement element : slideElementArray1) assertNotNull(element);

        ArrayList<TextElement> slide1TextElementArray = (ArrayList)slide1.getTextElementList();
        for (TextElement textElement : slide1TextElementArray) assertNotNull(textElement);

        assert(slideElementArray1.get(0) instanceof TextElement);
        assert(slide1TextElementArray.get(0) instanceof TextElement);
        //Assert the two object in the separate arrays are the same object
        assertEquals(slideElementArray1.get(0), slide1TextElementArray.get(0));

        TextElement slide1TextElement1 = slide1TextElementArray.get(0);


        assertEquals(1, slide1TextElement1.getElementID(), ERROR_MARGIN);
        assertEquals(1 , slide1TextElement1.getLayer());
        assertTrue(slide1TextElement1.isVisibility());
        assertEquals( 2, slide1TextElement1.getStartSequence(), ERROR_MARGIN);
        assertEquals( 4, slide1TextElement1.getEndSequence(), ERROR_MARGIN);
        assertEquals( 20.5, slide1TextElement1.getDuration(), ERROR_MARGIN);
        assertEquals("rgba(238,238,238,1.0)", slide1TextElementArray.get(0).getFontColour());
        assertEquals(1.2, slide1TextElementArray.get(0).getElementAspectRatio(), ERROR_MARGIN);
        assertTrue(slide1TextElement1.getTextContent().contains("<p>Test HTML Paragraph</p>"));
    }

    @Test
    public void testOtherContent () {
        for (SlideElement element : slideElementArray2) assertNotNull(element);
        for (SlideElement element : slideElementArray3) assertNotNull(element);
        for (SlideElement element : slideElementArray4) assertNotNull(element);

        //Slide 2
        assert(slideElementArray2.get(0) instanceof GraphicElement);
        assert(slideElementArray2.get(1) instanceof GraphicElement);
        assert(slideElementArray2.get(2) instanceof AudioElement);

        GraphicElement polygonGraphic = (GraphicElement) slideElementArray2.get(0);
        assertEquals(1, polygonGraphic.getElementID());
        assertEquals(1, polygonGraphic.getStartSequence());
        Float[] xPositions = {0.12f, 0.02f, 0.02f};
        for (int i = 0; i < xPositions.length; i++) {
            assertEquals(xPositions[i], polygonGraphic.getPolyXPositions()[i], ERROR_MARGIN);
        }
        Float[] y = {0.3f, 0.1f, 0.04f};
        for (int i = 0; i < y.length; i++) {
            assertEquals(y[i], polygonGraphic.getPolyYPositions()[i], ERROR_MARGIN);
        }
        assertEquals(Boolean.TRUE, polygonGraphic.isClosed());
        //Check the Start Animation (Simple fade 0-1 1000ms)
        Animation startAnimation = polygonGraphic.getStartAnimation();
        assert(startAnimation instanceof OpacityAnimation);
        assertEquals(0, ((OpacityAnimation) startAnimation).getStartOpacity(), ERROR_MARGIN);
        assertEquals(1, ((OpacityAnimation) startAnimation).getEndOpacity(), ERROR_MARGIN);
        assertEquals(1000, startAnimation.getDuration().toMillis(), ERROR_MARGIN);

        //Check the End Animation (Simple scale 1-0 1000ms)
        Animation endAnimation = polygonGraphic.getEndAnimation();
        assert(endAnimation instanceof ScaleAnimation);
        assertEquals(1, ((ScaleAnimation) endAnimation).getStartScale(), ERROR_MARGIN);
        assertEquals(0, ((ScaleAnimation) endAnimation).getEndScale(), ERROR_MARGIN);
        assertEquals(1000, endAnimation.getDuration().toMillis(), ERROR_MARGIN);


        GraphicElement ovalGraphic = (GraphicElement) slideElementArray2.get(1);
        assertEquals(0.05, ovalGraphic.getrHorizontal(), ERROR_MARGIN);
        assertEquals(0.1, ovalGraphic.getrVertical(), ERROR_MARGIN);
        assertEquals(45f, ovalGraphic.getRotation(), ERROR_MARGIN);
        //Check the Start Animation (Simple scale 0-1 1000ms)
        Animation ovalStartAnimation = ovalGraphic.getStartAnimation();
        assert(ovalStartAnimation instanceof PathAnimation);
        assertEquals("M0 0 l0 100 l100 0 z", ((PathAnimation) ovalStartAnimation).getPathUnscaled().getContent());
        assertEquals(1000, ovalStartAnimation.getDuration().toMillis(), ERROR_MARGIN);
        //Check the End Animation (Simple Translate 100,100->-200-200 1000ms)
        Animation ovalEndAnimation = ovalGraphic.getEndAnimation();
        assert(ovalEndAnimation instanceof TranslationAnimation);
        assertEquals(100, ((TranslationAnimation) ovalEndAnimation).getStartX(), ERROR_MARGIN);
        assertEquals(100, ((TranslationAnimation) ovalEndAnimation).getStartY(), ERROR_MARGIN);
        assertEquals(-200, ((TranslationAnimation) ovalEndAnimation).getEndX(), ERROR_MARGIN);
        assertEquals(-200, ((TranslationAnimation) ovalEndAnimation).getEndY(), ERROR_MARGIN);
        assertEquals(1000, ovalEndAnimation.getDuration().toMillis(), ERROR_MARGIN);


        AudioElement audioElement = (AudioElement) slideElementArray2.get(2);
        assertEquals("projectResources/sampleFiles/example.mp3", audioElement.getPath());
        assertEquals(Boolean.TRUE, audioElement.getLoop());
        assertEquals(Boolean.FALSE, audioElement.getAutoPlay());
        assertEquals(0.001f, audioElement.getStartTime().toSeconds(), ERROR_MARGIN);
        assertEquals(0.005f, audioElement.getEndTime().toSeconds(), ERROR_MARGIN);


        //Slide 3
        assert(slideElementArray3.get(0) instanceof ImageElement);
        assert(slideElementArray3.get(1) instanceof ImageElement);

        ImageElement imageElement = (ImageElement) slideElementArray3.get(0);
        assertEquals("http://www.amp.york.ac.uk/myImage.jpg", imageElement.getPath());
        assertEquals(0.9f, imageElement.getOpacity(), ERROR_MARGIN);

        //Slide 4
        assert(slideElementArray4.get(0) instanceof VideoElement);
        assert(slideElementArray4.get(1) instanceof WordCloudElement);
        assert(slideElementArray4.get(2) instanceof PollElement);
        
        VideoElement videoElement = (VideoElement) slideElementArray4.get(0);
        assertEquals(1, videoElement.getElementID());
        assertEquals(367.43f, videoElement.getDuration(), ERROR_MARGIN);

        WordCloudElement wordCloudElement = (WordCloudElement) slideElementArray4.get(1);
        assertEquals("Insert Words Here", wordCloudElement.getQuestion());
        assertEquals("aCloudPath", wordCloudElement.getCloudShapePath());
        assertEquals(20, wordCloudElement.getTimeLimit());

        PollElement pollElement = (PollElement) slideElementArray4.get(2);
        assertEquals("Insert Longer Question Here", pollElement.getQuestion());
        assertEquals("answer1,answer2,answer3,answer4", pollElement.getAnswers());
        assertEquals(20, pollElement.getTimeLimit());
    }

    @Test
    public void testFaultyXml(){
        ParserXML faultyParserXML = null;
        try {
            faultyParserXML = new ParserXML("projectResources/sampleFiles/xmlTests/faultyTestXml.xml");
        } catch (FileNotFoundException e) {
            //FileNotFound.eat()
        }
        Presentation faultyPresentation;
        logger.info("Testing Faulty XML document: ...");
        faultyPresentation = faultyParserXML.parsePresentation();
        assertTrue(faultyPresentation.getXmlFaults().size() > 0);
    }

    @Test
    public void testEmptyXml(){
        ParserXML faultyParserXML = null;
        try {
            faultyParserXML = new ParserXML("projectResources/sampleFiles/xmlTests/emptyTestXml.xml");
        } catch (FileNotFoundException e) {
            //FileNotFound.eat()
        }
        Presentation faultyPresentation = null;
        logger.info("Testing Empty XML document: ...");
        faultyPresentation = faultyParserXML.parsePresentation();
        assertTrue(faultyPresentation.getXmlFaults().size() > 0);
    }



    @Ignore
    @Test
    public void testWritingXml() {
        assertTrue(true);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(""); //Space out log
    }
}