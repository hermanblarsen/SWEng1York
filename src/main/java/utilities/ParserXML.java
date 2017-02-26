package utilities;

//JAXP APIs used by DOMecho
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//exceptions that can be thrown when parsing
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
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
//---


import org.w3c.dom.Document;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utilities.Presentation;
import utilities.Slide;
import utilities.Slide;


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

        //Store the documentID
        NodeList presentationDocumentList = xmlDocument.getElementsByTagName("document");
        Element presentationDocument = (Element) presentationDocumentList.item(0);
        myPresentation.setDocumentID(presentationDocument.getAttributes().item(0).getNodeValue());

        //Store the document details
        NodeList documentDetailsList = xmlDocument.getElementsByTagName("documentdetails");
        Node documentDetailsNode = documentDetailsList.item(0);
        Element documentDetails = (Element) documentDetailsNode;
        for (int i = 0; i < documentDetailsNode.getChildNodes().getLength(); i++) {

            Node documentDetailsElement = documentDetailsNode.getChildNodes().item(i);
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

        NodeList slideshowList = xmlDocument.getElementsByTagName("slideshow");
        Node slideshowNode = slideshowList.item(0);

        NodeList defaultList = xmlDocument.getElementsByTagName("defaults");
        Node defaultNode = defaultList.item(0);

        //Store the theme and default settings
        Theme theme = new Theme();
        for (int i = 0; i < defaultNode.getChildNodes().getLength(); i++) {
            Node defaultElementNode = defaultNode.getChildNodes().item(i);
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
        myPresentation.setTheme(theme);

        //System.out.println(documentDetailsNode.getNextSibling().getNextSibling().getNodeName()); //Slideshow

            //TODO add slideshow to slideshow root of presentation

                //TODO add default to presentation theme

                    //TODO add default elements to theme


                //TODO loop through all slides and add to slide array
        NodeList slideList = xmlDocument.getElementsByTagName("slide");

                    //TODO loop add all elements on every slide to slideElement array in slide


        /*
        // for each slide in the file get all child nodes
        NodeList root = document.getElementsByTagName("slide");

        // For all slides:
        for (int i = 0; i < root.getLength(); i++) {
            Slide newSlide = new Slide();

            Element slideInstance = (Element) root.item(i);

            newSlide.setID(slideInstance.getAttribute("id"));

            NodeList slideElements = slideInstance.getChildNodes();
            // depending on tag of child node assign the textContend to
            // different field of the slide
            for (int j = 0; j < slideElements.getLength(); j++) {
                if (slideElements.item(j).getNodeType() == Node.ELEMENT_NODE) {

                    Element slideElement = (Element) slideElements.item(j);
                    String elementTag = slideElement.getTagName();

                    switch (elementTag) {
                        case "something":
                            slide.setTitle(slideElement.getTextContent());
                            break;
                    }
                }
            }

            // Add parsed slide to arraylist.
            myPresentation.addSlide(slide);*/
        return myPresentation;
    }

    public Boolean validatePath(String path) {
        return true;
    }
}
