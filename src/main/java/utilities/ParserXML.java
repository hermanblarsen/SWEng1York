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
            System.out.println("Path valid");
        } else {
            System.out.println("Path not valid");
            //Path remains default
        }

        //Create a DOMParser, try parsing XML
        xmlParser = new DOMParser();
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

    public Presentation parsePresentation() {
        Presentation myPresentation = new Presentation();

        //Set the root element of the XML to the "document"
        NodeList documentRootList = xmlDocument.getElementsByTagName("documentdetails");

        Element presentationDocument = (Element) xmlDocument.getDocumentElement();
        System.out.println("NodeName: " + presentationDocument.getNodeName().toString());
        NamedNodeMap presentationDocumentAttributes = null;

//        System.out.println("DocumentID: " + presentationDocument.getAttribute("documentID").toString());
//
//        System.out.println("ChildNodes Number: " + presentationDocument.getChildNodes().getLength());
//        for(int i = 0; i <5; i++) {
//            System.out.println("ChildNodes Number: " + presentationDocument.getChildNodes().item(i).getNodeName());
//        }

        Element documentDetails = (Element) documentRootList.item(0);
        Element slideShow = (Element) documentRootList.item(1);

//        for (int i=0;i<; i++) {
//            System.out.print();
//        }

            //TODO add attributes to documentdetails root presentation


                //TODO add document details to presentation


            //TODO add slideshow to slideshow root of presentation

                //TODO add default to presentation theme

                    //TODO add default elements to theme


                //TODO loop through all slides and add to slide array

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
