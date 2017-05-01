package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.presentationElements.*;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import javafx.scene.media.MediaException;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;

import static com.i2lp.edi.client.utilities.Utils.*;

/**
 * Created by habl on 23/02/2017.
 */
public class ParserXML {

    private DOMParser xmlParser;
    private Document xmlDocument;
    private String presentationXmlPath = "projectResources/sampleFiles/sampleXml.xml";
    private Logger logger = LoggerFactory.getLogger(ParserXML.class);
    private boolean i2lpFormatted = false;
    private Presentation myPresentation;
    private ArrayList<String> faultsDetected = new ArrayList<>();

    public ParserXML(String presentationXmlPath) throws InvalidPathException {
        if (this.validateExtension(presentationXmlPath))
        {
            this.presentationXmlPath = presentationXmlPath; //Set the path if valid
            logger.info("Path valid...");
        } else {
            logger.warn("Path not valid, sample XML loaded...");
            throw new InvalidPathException(presentationXmlPath, presentationXmlPath.substring(presentationXmlPath.lastIndexOf(".") + 1));
        }

        //Create a DOMParser, try parsing XML
        xmlParser = new DOMParser(); //Equivalent to DocumentBuilder when not writing to XML
        try {
            xmlParser.parse(this.presentationXmlPath);
        } catch (SAXException e) {
            e.printStackTrace();
            logger.warn("SAXExeption when accessing XML");
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("IOExeption when accessing XML");
        }
        xmlDocument = xmlParser.getDocument();
    }

    public Presentation parsePresentation() {
        myPresentation = new Presentation();

        parseDocumentID();
        parseDocumentDetails();
        parseDefaultsAndTheme();
        parseSlidesAndSlideElements();

        if (faultsDetected.size() > 0) {
            myPresentation.setXmlFaults(faultsDetected);
        }

        return myPresentation;
    }

    private void parseDocumentID(){
        //Store the documentID:
        // Find all (root)elements named "document"
        NodeList presentationDocumentList = xmlDocument.getElementsByTagName("document");

        if ( presentationDocumentList.getLength() != 0) {
            //There is only one (root) element "document", hence choose the first indexed node.
            Node presentationDocumentNode = presentationDocumentList.item(0);

            //Save the attribute of the document node
            String attrubuteDocumentIdValue = presentationDocumentNode.getAttributes().item(0).getNodeValue();
            if(attrubuteDocumentIdValue != null) {
                myPresentation.setDocumentID(attrubuteDocumentIdValue);
            }
        } else {
            logger.warn("No document tag found");
            faultsDetected.add("No document tag found");
        }
    }

    private void parseDocumentDetails() {
        //Store the document details:
        // Find elements named "documentdetails"
        NodeList documentDetailsList = xmlDocument.getElementsByTagName("documentdetails");

        if ( documentDetailsList.getLength() != 0 && documentDetailsList.item(0).getNodeType() == Node.ELEMENT_NODE) {
            //There is only one "documentdetails" element, hence choose the first indexed node.
            Node documentDetailsNode = documentDetailsList.item(0);

            // List of all elements under(child of) "documentdetails"
            NodeList documentDetailsChildrenList = documentDetailsNode.getChildNodes();

            //Go through all child elements of "documentdetails" and store them in their respective fields
            // in the presentation.
            for (int i = 0; i < documentDetailsChildrenList.getLength(); i++) {
                //Find the current node
                Node documentDetailsElementNode = documentDetailsChildrenList.item(i);

                //If the node is an element node, find its nodeName /elementTag and
                // set the respective fields in the presentation.
                if (documentDetailsElementNode.getNodeType() == Node.ELEMENT_NODE) {
                    String elementName = documentDetailsElementNode.getNodeName();
                    String elementContent = documentDetailsElementNode.getTextContent();

                    switch (elementName) {
                        case "author":
                            myPresentation.setAuthor(elementContent);
                            break;
                        case "version":
                            myPresentation.setVersion(Float.valueOf(elementContent));
                            break;
                        case "documentaspectratio":
                            myPresentation.setDocumentAspectRatio(Float.valueOf(elementContent));
                            break;
                        case "description":
                            myPresentation.setDescription(elementContent);
                            break;
                        case "tags":
                            myPresentation.setTags(elementContent);
                            break;
                        case "groupformat":
                            myPresentation.setGroupFormat(Integer.valueOf(elementContent));
                            if (Integer.valueOf(elementContent) == 1) {
                                i2lpFormatted = true;
                                myPresentation.setI2lpFormat(i2lpFormatted);
                                logger.info("I2LP format found");
                            }
                            break;
                        default:
                            logger.warn("Document Detail Not Recognised! Name: " + elementName +
                                    ", Value: " + elementContent + ", and XML-Type: " + documentDetailsElementNode.getNodeType());
                            faultsDetected.add("Document Detail Not Recognised! Name: " + elementName +
                                    ", Value: " + elementContent + ", and XML-Type: " + documentDetailsElementNode.getNodeType());
                    }
                }
            }
        } else {
            logger.warn("No Document Details Tag Found!");
            faultsDetected.add("No Document Details Tag Found!");
        }
    }

    private void parseDefaultsAndTheme() {
        //Store the theme and default settings:
        //Find all elements named "defaults"
        Theme theme = new Theme();
        NodeList defaultsList = xmlDocument.getElementsByTagName("defaults");

        if ( defaultsList.getLength() != 0 && defaultsList.item(0).getNodeType() == Node.ELEMENT_NODE) {
            //There is only one "defaults" element, hence choose the first index.
            Node defaultsNode = defaultsList.item(0);
            //List of all elements under(child of) "defaults"
            NodeList defaultsChildrenList = defaultsNode.getChildNodes();
            //Go through all child elements of "defaults" and store them in their respective fields
            // in the presentation and/or theme
            for (int i = 0; i < defaultsChildrenList.getLength(); i++) {
                //Find the current node
                Node defaultsElementNode = defaultsChildrenList.item(i);

                //If current is an element node, find its nodeName /elementTag and
                // set the respective fields in the presentation.
                if (defaultsElementNode.getNodeType() == Node.ELEMENT_NODE) {
                    String elementName = defaultsElementNode.getNodeName();
                    String elementContent = defaultsElementNode.getTextContent();

                    switch (elementName) {
                        case "bgcolour":
                            theme.setBackgroundColour(checkValidColour(elementContent, null));
                            break;
                        case "font":
                            theme.setFont(checkValidFont(elementContent, null));
                            break;
                        case "fontsize":
                            theme.setFontSize(checkValidFontSize(Integer.parseInt(elementContent), null));
                            break;
                        case "fontcolour":
                            theme.setFontColour(checkValidColour(elementContent, null));
                            break;
                        case "graphicscolour":
                            theme.setGraphicsColour(checkValidColour(elementContent, null));
                            break;
                        case "autoplaymedia":
                            myPresentation.setAutoplayMedia(Boolean.valueOf(elementContent));
                            break;
                        default:
                            logger.warn("Default or Theme Element Not Recognised! Name: " + elementName +
                                    ", Value: " + elementContent + ", and XML-Type: " + defaultsElementNode.getNodeType());
                            faultsDetected.add("Default or Theme Element Not Recognised! Name: " + elementName +
                                    ", Value: " + elementContent + ", and XML-Type: " + defaultsElementNode.getNodeType());
                    }
                }
            }
        } else {
            logger.warn("No Defaults Found!");
            faultsDetected.add("No Defaults Found!");
        }
        myPresentation.setTheme(theme); //Set the stored theme to the presentation
    }

    private void parseSlidesAndSlideElements() {
        //Loop through each slide and add elements to every slide and every slide to the presentation:
        //Instantiate an array to add the slides to
        ArrayList<Slide> slideArray = new ArrayList<>();

        //Find all elements named "slide"
        NodeList slideNodeList = xmlDocument.getElementsByTagName("slide");

        if (slideNodeList.getLength() != 0) {
            //For all slides:
            for (int i = 0; i < slideNodeList.getLength(); i++) {
                //Create a new slide for every slide element
                Slide mySlide = new Slide();
                //Create a slideElement array to store elements on the current slide
                ArrayList<SlideElement> slideElementArrayList = new ArrayList<>();

                //Find the current slide node
                Node slideNode = slideNodeList.item(i);

                if (slideNode.getAttributes().getLength() != 0) {
                    //A slide only has one attribute, slideID
                    String attributeName = slideNode.getAttributes().item(0).getNodeName();

                    if (attributeName.equals("slideid")) {
                        String attrContent = slideNode.getAttributes().item(0).getNodeValue();
                        mySlide.setSlideID(Integer.valueOf(attrContent));
                    }
                }

                //Find all children of the current slide:
                NodeList slideNodeChildrenList = slideNode.getChildNodes();
                //For all elements on a slide:
                for (int j = 0; j < slideNodeChildrenList.getLength(); j++) {
                    //Find the current slide element (slide child)
                    Node slideElementNode = slideNodeChildrenList.item(j);

                    if (slideElementNode.getNodeType() == Node.ELEMENT_NODE) {
                        String elementName = slideElementNode.getNodeName();
                        switch (elementName) {
                            case "text":
                                TextElement myTextElement = new TextElement();
                                parseElementAttributes(slideElementNode, myTextElement);
                                parseTextElement(slideElementNode, myTextElement);
                                slideElementArrayList.add(myTextElement);
                                break;
                            case "graphic":
                                GraphicElement myGraphicElement = new GraphicElement();
                                parseElementAttributes(slideElementNode, myGraphicElement);
                                parseGraphicsElement(slideElementNode, myGraphicElement);
                                slideElementArrayList.add(myGraphicElement);
                                break;
                            case "image":
                                ImageElement myImageElement = new ImageElement();
                                parseElementAttributes(slideElementNode, myImageElement);
                                parseImageElement(slideElementNode, myImageElement);
                                slideElementArrayList.add(myImageElement);
                                break;
                            case "audio":
                                AudioElement myAudioElement = new AudioElement();
                                parseElementAttributes(slideElementNode, myAudioElement);
                                parseAudioElement(slideElementNode, myAudioElement);
                                slideElementArrayList.add(myAudioElement);
                                break;
                            case "video":
                                VideoElement myVideoElement = new VideoElement();
                                parseElementAttributes(slideElementNode, myVideoElement);
                                parseVideoElement(slideElementNode, myVideoElement);
                                slideElementArrayList.add(myVideoElement);
                                break;
                            default:
                                logger.warn("SlideElement Name Not Recognised! Name: " + elementName);
                                faultsDetected.add("SlideElement Name Not Recognised! Name: " + elementName);
                        }
                    }
                }
                mySlide.setSlideElementList(slideElementArrayList);
                slideArray.add(mySlide);
            }
        } else {
            logger.warn("No slides found!");
            faultsDetected.add("No slides found!");
        }
        myPresentation.setSlideList(slideArray);
        logger.info("Presentation Parsed");
    }

    private void parseElementAttributes (Node slideElementNode, SlideElement slideElement) {
        for (int i = 0; i < slideElementNode.getAttributes().getLength(); i++) {
            //Find the current attribute node
            Node attributeNode = slideElementNode.getAttributes().item(i);

            if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                //Find the attribute name and its content, and store this in the element
                String attributeName = attributeNode.getNodeName();
                String attributeContent = attributeNode.getNodeValue();

                switch (attributeName) {
                    case "elementid":
                        slideElement.setElementID(Integer.valueOf(attributeContent));
                        break;
                    case "layer":
                        slideElement.setLayer(Integer.valueOf(attributeContent));
                        break;
                    case "visibility":
                        slideElement.setVisibility(Boolean.valueOf(attributeContent));
                        break;
                    case "startsequence":
                        slideElement.setStartSequence(Integer.valueOf(attributeContent));
                        break;
                    case "endsequence":
                        slideElement.setEndSequence(Integer.valueOf(attributeContent));
                        break;
                    case "duration":
                        slideElement.setDuration(Float.valueOf(attributeContent));
                        break;
                    default:
                        logger.warn("Attribute Not Recognised! Name: " + attributeName +
                                ", Value: " + attributeContent + ", and Type: " + attributeNode.getNodeType());
                        faultsDetected.add("Attribute Not Recognised! Name: " + attributeName +
                                ", Value: " + attributeContent + ", and Type: " + attributeNode.getNodeType());
                }
            }
        }
    }

    private void parseTextElement(Node textElementNode, TextElement textElement){
        //Find and store all elements of the text element
        NodeList textNodeChildrenList = textElementNode.getChildNodes();
        for (int i = 0; i < textNodeChildrenList.getLength(); i++) {
            //Find the current element node
            Node elementNode = textNodeChildrenList.item(i);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                //Find the element name and its content, and store this in the textElement
                String elementName = elementNode.getNodeName();
                String elementContent = elementNode.getTextContent();

                switch (elementName){
                    case "textcontent":
                        textElement.setTextContent(elementContent);
                        break;
                    case "textfilepath":
                        textElement.setTextFilepath(elementContent);
                        break;
                    case "textcontentreference":
                        textElement.setTextContentReference(elementContent);
                        break;
                    case "xposition":
                        textElement.setxPosition(Float.valueOf(elementContent));
                        break;
                    case "yposition":
                        textElement.setyPosition(Float.valueOf(elementContent));
                        break;
                    case "xsize":
                        textElement.setxSize(Float.valueOf(elementContent));
                        break;
                    case "ysize":
                        textElement.setySize(Float.valueOf(elementContent));
                        break;
                    case "font":
                        textElement.setFont(checkValidFont(elementContent, myPresentation.getTheme()));
                        break;
                    case "fontsize":
                        textElement.setFontSize(checkValidFontSize(Integer.valueOf(elementContent), myPresentation.getTheme()));
                        break;
                    case "fontcolour":
                        textElement.setFontColour(checkValidColour(elementContent, myPresentation.getTheme()));
                        break;
                    case "bgcolour":
                        textElement.setBgColour(checkValidColour(elementContent, myPresentation.getTheme()));
                        break;
                    case "bordercolour":
                        textElement.setBorderColour(checkValidColour(elementContent, myPresentation.getTheme()));
                        break;
                    case "onclickaction":
                        textElement.setOnClickAction(checkValidOnClickAction(elementContent));
                        break;
                    case "onclickinfo":
                        textElement.setOnClickInfo(elementContent);
                        break;
                    case "aspectratiolock":
                        textElement.setAspectRatioLock(Boolean.valueOf(elementContent));
                        break;
                    case "elementaspectratio":
                        textElement.setElementAspectRatio(Float.valueOf(elementContent));
                        break;
                    case "bordersize":
                        textElement.setBorderSize(Integer.valueOf(elementContent));
                        break;
                    default:
                        logger.warn("Text Element Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                        faultsDetected.add("Text Element Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                }
            }
        }
    }

    private void parseGraphicsElement(Node graphicElementNode, GraphicElement graphicElement){
        //Find and store all elements of the element
        NodeList graphicNodeChildrenList = graphicElementNode.getChildNodes();

        for (int i = 0; i < graphicNodeChildrenList.getLength(); i++) {
            //Find the current element node
            Node elementNode = graphicNodeChildrenList.item(i);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                //Find the element name and its content, and store this in the graphicElement
                String elementName = elementNode.getNodeName();
                String elementContent = elementNode.getTextContent();

                switch (elementName) {
                    case "onclickaction":
                        graphicElement.setOnClickAction(checkValidOnClickAction(elementContent));
                        break;
                    case "onclickinfo":
                        graphicElement.setOnClickInfo(elementContent);
                        break;
                    case "aspectratiolock":
                        graphicElement.setAspectRatioLock(Boolean.valueOf(elementContent));
                        break;
                    case "elementaspectratio":
                        graphicElement.setElementAspectRatio(Float.valueOf(elementContent));
                        break;
                    case "linecolour":
                        graphicElement.setLineColour(checkValidColour(elementContent, myPresentation.getTheme()));
                        break;
                    case "fillcolour":
                        graphicElement.setFillColour(checkValidColour(elementContent, myPresentation.getTheme()));
                        break;
                    case "polygon":
                        NodeList polygonNodeChildrenList = elementNode.getChildNodes();

                        for (int j = 0; j < polygonNodeChildrenList.getLength(); j++) {
                            Node polygonElementNode = polygonNodeChildrenList.item(j);
                            if (polygonElementNode.getNodeType() == Node.ELEMENT_NODE) {
                                String polygonElementName = polygonElementNode.getNodeName();
                                String polygonElementContent = polygonElementNode.getTextContent();
                                switch (polygonElementName) {
                                    case "xpositions":
                                        String[] xPositionsStringArray = polygonElementContent.trim().split(",");
                                        float[] xPositionsArray = new float[xPositionsStringArray.length];

                                        for (int k = 0; k < xPositionsStringArray.length; k++) {
                                            xPositionsArray[k] = Float.valueOf(xPositionsStringArray[k]);
                                        }
                                        graphicElement.polySetXPoints(xPositionsArray);
                                        break;
                                    case "ypositions":
                                        String[] yPositionsStringArray = polygonElementContent.trim().split(",");
                                        float[] yPositionsArray = new float[yPositionsStringArray.length];

                                        for (int k = 0; k < yPositionsStringArray.length; k++) {
                                            yPositionsArray[k] = Float.valueOf(yPositionsStringArray[k]);
                                        }
                                        graphicElement.polySetYPoints(yPositionsArray);
                                        break;
                                    case "isclosed":
                                        graphicElement.setClosed(Boolean.valueOf(polygonElementContent));
                                        break;
                                    default:
                                        logger.info("Polygon Element Property Not Recognised! Name: " + polygonElementName +
                                                ", Value: " + polygonElementContent + ", and XML-Type: " + polygonElementNode.getNodeType());
                                }
                            }
                        }
                        graphicElement.setPolygon(true);
                        break;
                    case "oval":
                        NodeList ovalNodeChildrenList = elementNode.getChildNodes();

                        for (int j = 0; j < ovalNodeChildrenList.getLength(); j++) {
                            Node ovalElementNode = ovalNodeChildrenList.item(j);
                            if (ovalElementNode.getNodeType() == Node.ELEMENT_NODE) {
                                String ovalElementName = ovalElementNode.getNodeName();
                                String ovalElementContent = ovalElementNode.getTextContent();
                                switch (ovalElementName) {
                                    case "xposition":
                                        graphicElement.setOvalXPosition(Float.valueOf(ovalElementContent));
                                        break;
                                    case "yposition":
                                        graphicElement.setOvalYPosition(Float.valueOf(ovalElementContent));
                                        break;
                                    case "rvertical":
                                        graphicElement.setrVertical(Float.valueOf(ovalElementContent));
                                        break;
                                    case "rhorizontal":
                                        graphicElement.setrHorizontal(Float.valueOf(ovalElementContent));
                                        break;
                                    case "rotation":
                                        graphicElement.setRotation(Float.valueOf(ovalElementContent));
                                        break;
                                    default:
                                        logger.info("Oval Element Property Not Recognised! Name: " + ovalElementName +
                                                ", Value: " + ovalElementContent + ", and XML-Type: " + ovalElementNode.getNodeType());
                                }
                            }
                        }
                        graphicElement.setPolygon(false);
                        break;
                    default:
                        logger.warn("Graphics Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                        faultsDetected.add("Graphics Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                }
            }
        }
    }

    private void parseImageElement(Node imageElementNode, ImageElement imageElement) {
        //Find and store all elements of the image element
        NodeList imageNodeChildrenList = imageElementNode.getChildNodes();
        for (int k = 0; k < imageNodeChildrenList.getLength(); k++) {
            //Find the current element node
            Node elementNode = imageNodeChildrenList.item(k);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                //Find the element name and its content, and store this in the imageElement
                String elementName = elementNode.getNodeName();
                String elementContent = elementNode.getTextContent();

                switch (elementName){
                    case "xposition":
                        imageElement.setxPosition(Float.valueOf(elementContent));
                        break;
                    case "yposition":
                        imageElement.setyPosition(Float.valueOf(elementContent));
                        break;
                    case "xsize":
                        imageElement.setxSize(Float.valueOf(elementContent));
                        break;
                    case "ysize":
                        imageElement.setySize(Float.valueOf(elementContent));
                        break;
                    case "path":
                        imageElement.setPath(elementContent);
                        break;
                    case "onclickaction":
                        imageElement.setOnClickAction(checkValidOnClickAction(elementContent));
                        break;
                    case "onclickinfo":
                        imageElement.setOnClickInfo(elementContent);
                        break;
                    case "opacity":
                        imageElement.setOpacity(Float.valueOf(elementContent));
                        break;
                    case "aspectratiolock":
                        imageElement.setAspectRatioLock(Boolean.valueOf(elementContent));
                        break;
                    case "elementaspectratio":
                        imageElement.setElementAspectRatio(Float.valueOf(elementContent));
                        break;
                    default:
                        logger.warn("Image Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                        faultsDetected.add("Image Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                }
            }
        }
    }

    private void parseAudioElement(Node audioElementNode, AudioElement audioElement) {
        //Find and store all elements of the audio element
        NodeList audioNodeChildrenList = audioElementNode.getChildNodes();
        for (int i = 0; i < audioNodeChildrenList.getLength(); i++) {
            //Find the current element node
            Node elementNode = audioNodeChildrenList.item(i);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                //Find the element name and its content, and store this in the audioElement
                String elementName = elementNode.getNodeName();
                String elementContent = elementNode.getTextContent();

                switch (elementName){
                    case "path":
                        try {
                            audioElement.setPath(elementContent);
                        } catch (MediaException e){
                            logger.warn("Auto element error: " + e.getMessage());
                            faultsDetected.add("Media Error: " + e.getMessage());
                        }
                        break;
                    case "loop":
                        audioElement.isLoop(Boolean.valueOf(elementContent));
                        break;
                    case "autoplay":
                        audioElement.isAutoPlay(Boolean.valueOf(elementContent));
                        break;
                    case "starttime":
                        audioElement.setStartTime(Duration.seconds(Integer.valueOf(elementContent)));
                        break;
                    case "endtime":
                        audioElement.setEndTime(Duration.seconds(Integer.valueOf(elementContent)));
                        break;
                    default:
                        logger.warn("Audio Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                        faultsDetected.add("Audio Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                }
            }
        }
    }

    private void parseVideoElement(Node videoElementNode, VideoElement videoElement) {
        //Find and store all elements of the video element
        NodeList videoNodeChildrenList = videoElementNode.getChildNodes();
        for (int i = 0; i < videoNodeChildrenList.getLength(); i++) {
            //Find the current element node
            Node elementNode = videoNodeChildrenList.item(i);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                //Find the element name and its content, and store this in the videoElement
                String elementName = elementNode.getNodeName();
                String elementContent = elementNode.getTextContent();

                switch (elementName){
                    case "xposition":
                        videoElement.setxPosition(Float.valueOf(elementContent));
                        break;
                    case "yposition":
                        videoElement.setyPosition(Float.valueOf(elementContent));
                        break;
                    case "xsize":
                        videoElement.setxSize(Float.valueOf(elementContent));
                        break;
                    case "ysize":
                        videoElement.setySize(Float.valueOf(elementContent));
                        break;
                    case "path":
                        videoElement.setPath(elementContent);
                        break;
                    case "onclickaction":
                        videoElement.setOnClickAction(checkValidOnClickAction(elementContent));
                        break;
                    case "onclickinfo":
                        videoElement.setOnClickInfo(elementContent);
                        break;
                    case "loop":
                        videoElement.setLoop(Boolean.valueOf(elementContent));
                        break;
                    case "aspectratiolock":
                        videoElement.setAspectRatioLock(Boolean.valueOf(elementContent));
                        break;
                    case "elementaspectratio":
                        videoElement.setElementAspectRatio(Float.valueOf(elementContent));
                        break;
                    case "autoplay":
                        videoElement.setAutoplay(Boolean.valueOf(elementContent));
                        break;
                    case "starttime":
                        videoElement.setStartTime(new Duration(Double.valueOf(elementContent)));
                        break;
                    case "endtime":
                        videoElement.setEndTime(new Duration(Double.valueOf(elementContent)));
                        break;
                    default:
                        logger.warn("Video Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                        faultsDetected.add("Video Property Name Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and XML-Type: " + elementNode.getNodeType());
                }
            }
        }
    }

    public Boolean validateExtension(String path) {
        Boolean validated = false;
        if(path.lastIndexOf(".") != -1 && path.lastIndexOf(".") != 0) {
            String extension = path.substring(path.lastIndexOf(".") + 1);
            if ((extension).equals("xml")) validated = true;
            else logger.warn("File Extension " + extension + " not accepted");
        }
        return validated;
    }
}