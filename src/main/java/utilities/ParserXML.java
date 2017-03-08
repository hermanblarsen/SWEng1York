package utilities;

//JAXP APIs used by DOMecho
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

//exceptions that can be thrown when parsing
import javafx.util.Duration;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;

//read the sample XML file and manage output:

//Finally, import the W3C definitions for a DOM, DOM exceptions, entities and nodes:
//---


import java.util.ArrayList;

/**
 * Created by hermanblarsen on 23/02/2017.
 */
public class ParserXML {

    private DOMParser xmlParser;
    private Document xmlDocument;
    private String presentationXmlPath = "externalResources/sampleXml.xml";

    public ParserXML(String presentationXmlPath) {
        if (this.validatePath(presentationXmlPath))
        {
            this.presentationXmlPath = presentationXmlPath; //Set the path if valid
        } else {
            System.out.println("Path not valid, default XML loaded");
        }

        //Create a DOMParser, try parsing XML
        xmlParser = new DOMParser(); //Equivalent to DocumentBuilder when not writing to XML
        try {
            xmlParser.parse(presentationXmlPath);
        } catch (SAXException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("SAXExeption when parsing XML");
        } catch (IOException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("IOExeption when parsing XML");
        }
        xmlDocument = xmlParser.getDocument();
    }

    /* XML Sanity Check
    * NodeName of element: actual name of element as per XML
    * NodeName of node:  actual name of element as per XML
    * NodeValue of node: text/value stored in node
    * This:
    *   NamedNodeMap presentationDocumentAttributes = presentationDocument.getAttributes();
    *   Node documentIDNode = presentationDocumentAttributes.item(0);
    * Is equivalent to this:
    *   Node documentIDNode = presentationDocument.getAttributes().item(0);
    * And results in this :
    *   System.out.println(documentIDNode.getNodeName() + " = " + documentIDNode.getNodeValue() + " and type: " + documentIDNode.getNodeType());
    *   (documentid = sampleinput and type: 2)
    *
    * node.item(0).getNodeName() + node.item(0).getFirstChild().getNodeValue() gives xml name and its value  ?
    * */

    public Presentation parsePresentation() {
        Presentation myPresentation = new Presentation();

        //Store the documentID:
        // Find all (root)elements named "document"
        NodeList presentationDocumentList = xmlDocument.getElementsByTagName("document");

        //there is only one (root) element "document", hence choose the first index.
        Node presentationDocumentNode = presentationDocumentList.item(0);

        //Save the attribute of the document node
        String attrDocumentID = presentationDocumentNode.getAttributes().item(0).getNodeValue();
        if(attrDocumentID != null) {
            myPresentation.setDocumentID(attrDocumentID);
        }


        //Store the document details:
        // Find all elements named "documentdetails"
        NodeList documentDetailsList = xmlDocument.getElementsByTagName("documentdetails");

        //There is only one "documentdetails" element, hence choose the first index.
        Node documentDetailsNode = documentDetailsList.item(0);

        // List of all elements under(child of) "documentdetails"
        NodeList documentDetailsChildrenList = documentDetailsNode.getChildNodes();

        //Go through all child elements of "documentdetails" and store them in their respective fields
        // in the presentation.
        for (int i = 0; i < documentDetailsChildrenList.getLength(); i++) {
            //Find the current node
            Node documentDetailsElement = documentDetailsChildrenList.item(i);

            //If the node is an element node, find its nodeName /elementTag and
            // set the respective fields in the presentation.
            if (documentDetailsElement.getNodeType() == Node.ELEMENT_NODE) {
                String elementName = documentDetailsElement.getNodeName();
                String elementContent = documentDetailsElement.getTextContent();

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
                        break;
                    default:
                        System.out.println("Element Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and Type: " + documentDetailsNode.getNodeType());
                }
            }
        }


        //TODO Not needed but keeping for now
//        NodeList slideshowList = xmlDocument.getElementsByTagName("slideshow");
//        Node slideshowNode = slideshowList.item(0);

        //Store the theme and default settings:
        //Find all elements named "defaults"
        Theme theme = new Theme();
        NodeList defaultList = xmlDocument.getElementsByTagName("defaults");
        //TODO if elementnode then?
        //There is only one "defaults" element, hence choose the first index.
        Node defaultNode = defaultList.item(0);
        //List of all elements under(child of) "defaults"
        NodeList defaultsChildrenList = defaultNode.getChildNodes();
        //Go through all child elements of "defaults" and store them in their respective fields
        // in the presentation and/or theme
        for (int i = 0; i < defaultsChildrenList.getLength(); i++) {
            //Find the current node
            Node defaultElementNode = defaultsChildrenList.item(i);

            //If current is an element node, find its nodeName /elementTag and
            // set the respective fields in the presentation.
            if (defaultElementNode.getNodeType() == Node.ELEMENT_NODE ) {
                String elementName = defaultElementNode.getNodeName();
                String elementContent = defaultElementNode.getTextContent();

                switch (elementName) {
                    case "bgcolour":
                        theme.setBackgroundColour(elementContent);
                        break;
                    case "font":
                        theme.setFont(elementContent);
                        break;
                    case "fontsize":
                        theme.setFontSize(Integer.valueOf(elementContent));
                        break;
                    case "fontcolour":
                        theme.setFontColour(elementContent);
                        break;
                    case "graphicscolour":
                        theme.setGraphicsColour(elementContent);
                        break;
                    case "autoplaymedia":
                        myPresentation.setAutoplayMedia(Boolean.valueOf(elementContent));
                        break;
                    default:
                        System.out.println("Element Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and Type: " + defaultElementNode.getNodeType());
                }
            }
        }
        //Set the stored theme to the presentation
        myPresentation.setTheme(theme);


        //Loop through each slide and add elements to every slide and every slide to the presentation:
        //Instantiate an array to add the slides to
        ArrayList<Slide> slideArray = new ArrayList<Slide>();
        //Find all elements named "slide"
        NodeList slideNodeList = xmlDocument.getElementsByTagName("slide");

        for (int i = 0; i < slideNodeList.getLength(); i++) {
            //Create a new slide for every slide element
            Slide mySlide = new Slide();
            //Create a slideElement array to store elements on the current slide
            ArrayList<SlideElement> slideElementArrayList = new ArrayList<SlideElement>();

            //Find the current slide node
            Node slideNode = slideNodeList.item(i);

            if (slideNode.getAttributes().getLength() != 0) {
                String attributeName = slideNode.getAttributes().item(0).getNodeName();
                //TODO: Should this be a proper string comparison using .equals("slideid")? - Amrik
                if (attributeName == "slideid") {
                    String attrContent = slideNode.getAttributes().item(0).getNodeValue();
                    int attrSlideID = Integer.valueOf(attrContent);
                    mySlide.setSlideID(attrSlideID);
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
                            TextElement myTextElement = parseTextElement(slideElementNode);
                            slideElementArrayList.add(myTextElement);
                            break;
                        case "graphic":
                            GraphicElement myGraphicElement = parseGraphicsElement(slideElementNode);
                            slideElementArrayList.add(myGraphicElement);
                            break;
                        case "image":
                            ImageElement myImageElement = parseImageElement(slideElementNode);
                            slideElementArrayList.add(myImageElement);
                            break;
                        case "audio":
                            AudioElement myAudioElement = parseAudioElement(slideElementNode);
                            slideElementArrayList.add(myAudioElement);
                            break;
                        case "video":
                            VideoElement myVideoElement = parseVideoElement(slideElementNode);
                            slideElementArrayList.add(myVideoElement);
                            break;
                        default:
                            System.out.println("Element Not Recognised! Name: " + elementName + " and Type: " + slideElementNode.getNodeType());
                            break;
                    }
                }
            }
            mySlide.setSlideElementList(slideElementArrayList);
            slideArray.add(mySlide);
        }
        myPresentation.setSlideList(slideArray);

        return myPresentation;
    }

    private TextElement parseTextElement(Node textElementNode) {
        //Instantiate a text element to be added to the slide
        TextElement textElement = new TextElement();

        //TODO see block comment for info on duplicate code.
        //parseSlideElementAttributes(textElement, textElementNode);
        //Find and store all attributes of the text element
        for (int k = 0; k < textElementNode.getAttributes().getLength(); k++) {
            //Find the current attribute node
            Node attributeNode = textElementNode.getAttributes().item(k);

            if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                //Find the attribute name and its content, and store this in the textElement
                String attributeName = attributeNode.getNodeName();
                String attributeContent = attributeNode.getNodeValue();

                switch (attributeName) {
                    case "elementid":
                        textElement.setElementID(Integer.valueOf(attributeContent));
                        break;
                    case "layer":
                        textElement.setLayer(Integer.valueOf(attributeContent));
                        break;
                    case "visibility":
                        textElement.setVisibility(Boolean.valueOf(attributeContent));
                        break;
                    case "startsequence":
                        textElement.setStartSequence(Integer.valueOf(attributeContent));
                        break;
                    case "endsequence":
                        textElement.setEndSequence(Integer.valueOf(attributeContent));
                        break;
                    case "duration":
                        textElement.setDuration(Float.valueOf(attributeContent));
                        break;
                    default:
                        System.out.println("Attribute Not Recognised! Name: " + attributeName +
                                ", Value: " + attributeContent + ", and Type: " + attributeNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }

        //Find and store all elements of the text element
        NodeList textNodeChildrenList = textElementNode.getChildNodes();
        for (int k = 0; k < textNodeChildrenList.getLength(); k++) {
            //Find the current element node
            Node elementNode = textNodeChildrenList.item(k);
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
                        textElement.setFont(elementContent);
                        break;
                    case "fontsize":
                        textElement.setFontSize(Integer.valueOf(elementContent));
                        break;
                    case "fontcolour":
                        textElement.setFontColour(elementContent);
                        break;
                    case "bgcolour":
                        textElement.setBgColour(elementContent);
                        break;
                    case "bordercolour":
                        textElement.setBorderColour(elementContent);
                        break;
                    case "onclickaction":
                        textElement.setOnClickAction(elementContent);
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
                    default:
                        System.out.println("Element Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and Type: " + elementNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }
        return textElement;
    }

    private GraphicElement parseGraphicsElement(Node graphicElementNode) {
        //Instantiate a graphic element to be added to the slide
        GraphicElement graphicElement = new GraphicElement();

        //TODO see block comment for info on duplicate code.
        //parseSlideElementAttributes(graphicsElement, graphicElementNode);

        //Find and store all attributes of the graphic element
        for (int k = 0; k < graphicElementNode.getAttributes().getLength(); k++) {
            //Find the current attribute node
            Node attributeNode = graphicElementNode.getAttributes().item(k);

            if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                //Find the attribute name and its content, and store this in the graphicElement
                String attributeName = attributeNode.getNodeName();
                String attributeContent = attributeNode.getNodeValue();

                switch (attributeName) {
                    case "elementid":
                        graphicElement.setElementID(Integer.valueOf(attributeContent));
                        break;
                    case "layer":
                        graphicElement.setLayer(Integer.valueOf(attributeContent));
                        break;
                    case "visibility":
                        graphicElement.setVisibility(Boolean.valueOf(attributeContent));
                        break;
                    case "startsequence":
                        graphicElement.setStartSequence(Integer.valueOf(attributeContent));
                        break;
                    case "endsequence":
                        graphicElement.setEndSequence(Integer.valueOf(attributeContent));
                        break;
                    case "duration":
                        graphicElement.setDuration(Float.valueOf(attributeContent));
                        break;
                    default:
                        System.out.println("Attribute Not Recognised! Name: " + attributeName +
                                ", Value: " + attributeContent + ", and Type: " + attributeNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }

        //Find and store all elements of the element
        NodeList graphicNodeChildrenList = graphicElementNode.getChildNodes();

        for (int k = 0; k < graphicNodeChildrenList.getLength(); k++) {
            //Find the current element node
            Node elementNode = graphicNodeChildrenList.item(k);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                //Find the element name and its content, and store this in the graphicElement
                String elementName = elementNode.getNodeName();
                String elementContent = elementNode.getTextContent();

                switch (elementName) {
                    case "onclickaction":
                        graphicElement.setOnClickAction(elementContent);
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
                        graphicElement.setLineColour(elementContent);
                        break;
                    case "fillcolour":
                        graphicElement.setFillColour(elementContent);
                        break;
                    case "polygon":
                        graphicElement.setPolygon(true);
                        Polygon polygon = new Polygon();
                        NodeList polygonNodeChildrenList = elementNode.getChildNodes();

                        for (int i = 0; i < polygonNodeChildrenList.getLength(); i++) {
                            Node polygonElementNode = polygonNodeChildrenList.item(i);
                            if (polygonElementNode.getNodeType() == Node.ELEMENT_NODE) {
                                String polygonElementName = polygonElementNode.getNodeName();
                                String polygonElementContent = polygonElementNode.getTextContent();
                                switch (polygonElementName) {
                                    case "xpositions":
                                        String[] xPositionsStringArray = polygonElementContent.trim().split(",");
                                        float[] xPositionsArray = new float[xPositionsStringArray.length];

                                        for (int j = 0; j < xPositionsStringArray.length; j++) {
                                            xPositionsArray[j] = Float.valueOf(xPositionsStringArray[j]);
                                        }
                                        polygon.setxPositions(xPositionsArray);
                                        break;
                                    case "ypositions":
                                        String[] yPositionsStringArray = polygonElementContent.trim().split(",");
                                        float[] yPositionsArray = new float[yPositionsStringArray.length];

                                        for (int j = 0; j < yPositionsStringArray.length; j++) {
                                            yPositionsArray[j] = Float.valueOf(yPositionsStringArray[j]);
                                        }
                                        polygon.setyPositions(yPositionsArray);
                                        break;
                                    case "isclosed":
                                        polygon.setClosed(Boolean.valueOf(polygonElementContent));
                                        break;
                                    default:
                                        System.out.println("Element Not Recognised! Name: " + polygonElementName +
                                                ", Value: " + polygonElementContent + ", and Type: " + polygonElementNode.getNodeType());
                                        //TODO proper error handling needed
                                        break;
                                }
                            }
                            graphicElement.setPolygon(polygon);
                        }
                        break;
                    case "oval":
                        graphicElement.setPolygon(false);
                        Oval oval = new Oval();
                        NodeList ovalNodeChildrenList = elementNode.getChildNodes();

                        for (int i = 0; i < ovalNodeChildrenList.getLength(); i++) {
                            Node ovalElementNode = ovalNodeChildrenList.item(i);
                            if (ovalElementNode.getNodeType() == Node.ELEMENT_NODE) {
                                String ovalElementName = ovalElementNode.getNodeName();
                                String ovalElementContent = ovalElementNode.getTextContent();
                                switch (ovalElementName) {
                                    case "xposition":
                                        oval.setxPosition(Float.valueOf(ovalElementContent));
                                        break;
                                    case "yposition":
                                        oval.setyPosition(Float.valueOf(ovalElementContent));
                                        break;
                                    case "rvertical":
                                        oval.setrVertical(Float.valueOf(ovalElementContent));
                                        break;
                                    case "rhorizontal":
                                        oval.setrHorizontal(Float.valueOf(ovalElementContent));
                                        break;
                                    case "rotation":
                                        oval.setRotation(Float.valueOf(ovalElementContent));
                                        break;
                                    default:
                                        System.out.println("Element Not Recognised! Name: " + ovalElementName +
                                                ", Value: " + ovalElementContent + ", and Type: " + ovalElementNode.getNodeType());
                                        //TODO proper error handling needed
                                        break;
                                }
                            }
                            graphicElement.setOval(oval);
                        }

                        break;
                    default:
                        System.out.println("Element Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and Type: " + elementNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }
        return graphicElement;
    }

    private ImageElement parseImageElement(Node imageElementNode) {
        //Instantiate an image element to be added to the slide
        ImageElement imageElement = new ImageElement();

        //TODO see block comment for info on duplicate code.
        //parseSlideElementAttributes(imageElement, imageElementNode);
        
        //Find and store all attributes of the image element
        for (int k = 0; k < imageElementNode.getAttributes().getLength(); k++) {
            //Find the current attribute node
            Node attributeNode = imageElementNode.getAttributes().item(k);

            if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                //Find the attribute name and its content, and store this in the imageElement
                String attributeName = attributeNode.getNodeName();
                String attributeContent = attributeNode.getNodeValue();

                switch (attributeName) {
                    case "elementid":
                        imageElement.setElementID(Integer.valueOf(attributeContent));
                        break;
                    case "layer":
                        imageElement.setLayer(Integer.valueOf(attributeContent));
                        break;
                    case "visibility":
                        imageElement.setVisibility(Boolean.valueOf(attributeContent));
                        break;
                    case "startsequence":
                        imageElement.setStartSequence(Integer.valueOf(attributeContent));
                        break;
                    case "endsequence":
                        imageElement.setEndSequence(Integer.valueOf(attributeContent));
                        break;
                    case "duration":
                        imageElement.setDurationSequence(Float.valueOf(attributeContent));
                        break;
                    default:
                        System.out.println("Attribute Not Recognised! Name: " + attributeName +
                                ", Value: " + attributeContent + ", and Type: " + attributeNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }

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
                        imageElement.setOnClickAction(elementContent);
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
                        System.out.println("Element Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and Type: " + elementNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }
        return imageElement;
    }

    private AudioElement parseAudioElement(Node audioElementNode) {
        //Instantiate an audio element to be added to the slide
        AudioElement audioElement = new AudioElement();

        //TODO see block comment for info on duplicate code.
        //parseSlideElementAttributes(audioElement, audioElementNode);

        //Find and store all attributes of the audio element
        for (int k = 0; k < audioElementNode.getAttributes().getLength(); k++) {
            //Find the current attribute node
            Node attributeNode = audioElementNode.getAttributes().item(k);

            if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                //Find the attribute name and its content, and store this in the audioElement
                String attributeName = attributeNode.getNodeName();
                String attributeContent = attributeNode.getNodeValue();

                switch (attributeName) {
                    case "elementid":
                        audioElement.setElementID(Integer.valueOf(attributeContent));
                        break;
                    case "startsequence":
                        audioElement.setStartSequence(Integer.valueOf(attributeContent));
                        break;
                    case "endsequence":
                        audioElement.setEndSequence(Integer.valueOf(attributeContent));
                        break;
                    case "duration":
                        audioElement.setDurationSequence(Float.valueOf(attributeContent));
                        break;
                    default:
                        System.out.println("Attribute Not Recognised! Name: " + attributeName +
                                ", Value: " + attributeContent + ", and Type: " + attributeNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }

        //Find and store all elements of the audio element
        NodeList audioNodeChildrenList = audioElementNode.getChildNodes();
        for (int k = 0; k < audioNodeChildrenList.getLength(); k++) {
            //Find the current element node
            Node elementNode = audioNodeChildrenList.item(k);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                //Find the element name and its content, and store this in the audioElement
                String elementName = elementNode.getNodeName();
                String elementContent = elementNode.getTextContent();

                switch (elementName){
                    case "path":
                        audioElement.setPath(elementContent);
                        break;
                    case "loop":
                        audioElement.setLoop(Boolean.valueOf(elementContent));
                        break;
                    case "autoplay":
                        audioElement.setAutoplay(Boolean.valueOf(elementContent));
                        break;
                    case "starttime":
                        audioElement.setStartTime(Integer.valueOf(elementContent));
                        break;
                    case "endtime":
                        audioElement.setEndTime(Integer.valueOf(elementContent));
                        break;
                    default:
                        System.out.println("Element Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and Type: " + elementNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }
        return audioElement;
    }

    private VideoElement parseVideoElement(Node videoElementNode) {
        //Instantiate an video element to be added to the slide
        VideoElement videoElement = new VideoElement();

        //TODO see block comment for info on duplicate code.
        //parseSlideElementAttributes(videoElement, videoElementNode);

        //Find and store all attributes of the video element
        for (int k = 0; k < videoElementNode.getAttributes().getLength(); k++) {
            //Find the current attribute node
            Node attributeNode = videoElementNode.getAttributes().item(k);

            if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                //Find the attribute name and its content, and store this in the videoElement
                String attributeName = attributeNode.getNodeName();
                String attributeContent = attributeNode.getNodeValue();

                switch (attributeName) {
                    case "elementid":
                        videoElement.setElementID(Integer.valueOf(attributeContent));
                        break;
                    case "layer":
                        videoElement.setLayer(Integer.valueOf(attributeContent));
                        break;
                    case "visibility":
                        videoElement.setVisibility(Boolean.valueOf(attributeContent));
                        break;
                    case "startsequence":
                        videoElement.setStartSequence(Integer.valueOf(attributeContent));
                        break;
                    case "endsequence":
                        videoElement.setEndSequence(Integer.valueOf(attributeContent));
                        break;
                    case "duration":
                        videoElement.setDuration(Float.valueOf(attributeContent));
                        break;
                    default:
                        System.out.println("Attribute Not Recognised! Name: " + attributeName +
                                ", Value: " + attributeContent + ", and Type: " + attributeNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }

        //Find and store all elements of the video element
        NodeList videoNodeChildrenList = videoElementNode.getChildNodes();
        for (int k = 0; k < videoNodeChildrenList.getLength(); k++) {
            //Find the current element node
            Node elementNode = videoNodeChildrenList.item(k);
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
                        videoElement.setOnClickAction(elementContent);
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
                        videoElement.setStartTime(new Duration(Double.valueOf(elementContent))); //TODO make sure input is in milliseconds
                        break;
                    case "endtime":
                        videoElement.setEndTime(new Duration(Double.valueOf(elementContent))); //TODO make sure input is in milliseconds
                        break;
                    default:
                        System.out.println("Element Not Recognised! Name: " + elementName +
                                ", Value: " + elementContent + ", and Type: " + elementNode.getNodeType());
                        //TODO proper error handling needed
                        break;
                }
            }
        }
        return videoElement;
    }

    //TODO using reflectance to check what element we are dealing with, we can reduce duplicate code parsing it in here.
   /* private void parseSlideElementAttributes(SlideElement slideElement, Node slideElementNode) {
        //Find and store all attributes of the graphic element
        for (int k = 0; k < slideElementNode.getAttributes().getLength(); k++) {
            //Find the current attribute node
            Node attributeNode = slideElementNode.getAttributes().item(k);

            if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                //Find the attribute name and its content, and store this in the graphicElement
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
                        System.out.println("Attribute: " + attributeContent + " Not Recognised!");
                        //TODO proper error handling needed
                        break;
                }
            }
        }
    }*/

    public Boolean validatePath(String path) {
        //TODO validate path somehow (and maybe filetype?)
        return true;
    }
}
