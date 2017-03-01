package utilities;

//JAXP APIs used by DOMecho
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//exceptions that can be thrown when parsing
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.*;
import java.io.IOException;

//read the sample XML file and manage output:
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

//Finally, import the W3C definitions for a DOM, DOM exceptions, entities and nodes:
//---


import org.w3c.dom.Document;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                String nodeName = documentDetailsElement.getNodeName();
                String nodeContent = documentDetailsElement.getTextContent();

                switch (nodeName) {
                    case "author":
                        myPresentation.setAuthor(nodeContent);
                        break;
                    case "version":
                        myPresentation.setVersion(Float.valueOf(nodeContent));
                        break;
                    case "documentaspectratio":
                        myPresentation.setDocumentAspectRatio(Float.valueOf(nodeContent));
                        break;
                    case "description":
                        myPresentation.setDescription(nodeContent);
                        break;
                    case "tags":
                        myPresentation.setTags(nodeContent);
                        break;
                    case "groupformat":
                        myPresentation.setGroupFormat(Integer.valueOf(nodeContent));
                        break;
                }
            }
        }


        //TODO Not needed but keeping for now
//        NodeList slideshowList = xmlDocument.getElementsByTagName("slideshow");
//        Node slideshowNode = slideshowList.item(0);

        //Store the theme and default settings:
        //Find all elements named "defaults"
        NodeList defaultList = xmlDocument.getElementsByTagName("defaults");

        //There is only one "defaults" element, hence choose the first index.
        Node defaultNode = defaultList.item(0);

        Theme theme = new Theme();
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
                String nodeName = defaultElementNode.getNodeName();
                String nodeContent = defaultElementNode.getTextContent();

                switch (nodeName) {
                    case "bgcolour":
                        theme.setBackgroundColour(nodeContent);
                        break;
                    case "font":
                        theme.setFont(nodeContent);
                        break;
                    case "fontsize":
                        theme.setFontSize(Integer.valueOf(nodeContent));
                        break;
                    case "fontcolour":
                        theme.setFontColour(nodeContent);
                        break;
                    case "graphicscolour":
                        theme.setGraphicsColour(nodeContent);
                        break;
                    case "autoplaymedia":
                        myPresentation.setAutoplayMedia(Boolean.valueOf(nodeContent));
                        break;
                }
            }
        }
        //Set the stored theme to the presentation
        myPresentation.setTheme(theme);

        //Loop through each slide and add elements to every slide and every slide to the presentation:
        //Find all elements named "slide"
        NodeList slideNodeList = xmlDocument.getElementsByTagName("slide");
        //Instantiate an array to add the slides to
        ArrayList<Slide> slideArray = new ArrayList<Slide>();

        for (int i = 0; i < slideNodeList.getLength(); i++) {
            Slide slide = new Slide();
            ArrayList<SlideElement> slideElementArray = new ArrayList<SlideElement>();
            Node slideNode = slideNodeList.item(i);

            if (slideNode.getAttributes().getLength() != 0) {
                String attrName = slideNode.getAttributes().item(0).getNodeName();
                if (attrName == "slideid") {
                    String attrContent = slideNode.getAttributes().item(0).getNodeValue();
                    int attrSlideID = Integer.valueOf(attrContent);
                    slide.setSlideID(attrSlideID);
                }
            }

            //Find all children of the current slide:
            NodeList slideNodeChildrenList = slideNode.getChildNodes();

            //For all elements on a slide:
            for (int j = 0; j < slideNodeChildrenList.getLength(); j++) {
                //Find the current slide element (slide child)
                Node slideElementNode = slideNodeChildrenList.item(j);

                if (slideElementNode.getNodeType() == Node.ELEMENT_NODE) {
                    String elementMediaType = slideElementNode.getNodeName();
                    switch (elementMediaType) {
                        case "text":
                            TextElement textElement = new TextElement();
                            //parseTextElement();//TODO add this when functional

                            //For all attributes of text element
                            for (int k = 0; k < slideElementNode.getAttributes().getLength(); k++) {
                                Node attribute = slideElementNode.getAttributes().item(k);
                                String attributeTag = attribute.getNodeName();
                                String attributeContent = attribute.getNodeValue();

                                switch (attributeTag) {
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
                                        System.out.println("Attribute: " + attributeContent + " Not Recognised!"); //TODO proper error handling
                                        break;
                                }
                            }

                            //Find all nodes under the text element
                            NodeList textNodeChildrenList = slideElementNode.getChildNodes();
                            for (int k = 0; k < textNodeChildrenList.getLength(); k++) {
                                Node elementNode = textNodeChildrenList.item(k);
                                if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
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
                                            System.out.println("Element: " + elementName + " Not Recognised!"); //TODO proper error handlin
                                            break;
                                    }
                                }
                            }
                            slideElementArray.add(textElement);
                            break;
                        case "graphic":
                            GraphicElement graphicElement = new GraphicElement();
                            //parseGraphicElement();//TODO add this when functional


                            slideElementArray.add(graphicElement);
                            break;
                        case "image":
                            ImageElement imageElement = new ImageElement();
                            //parseImageElement();//TODO add this when functional


                            slideElementArray.add(imageElement);
                            break;
                        case "audio":
                            AudioElement audioElement = new AudioElement();
                            //parseAudioElement();//TODO add this when functional

                            slideElementArray.add(audioElement);
                            break;
                        case "video":
                            VideoElement videoElement = new VideoElement();
                            //parseVideoElement();//TODO add this when functional

                            slideElementArray.add(videoElement);
                            break;
                        default:
                            break;
                    }
                }
                
                

                if(slideElementNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                    System.out.println("attribute");
                }



            }



            slideArray.add(slide);
        }
        myPresentation.setSlideList(slideArray);

        //TODO loop add all elements on every slide to slideElement array in slide



        return myPresentation;
    }

    public Boolean validatePath(String path) {
        return true;
    }
}
