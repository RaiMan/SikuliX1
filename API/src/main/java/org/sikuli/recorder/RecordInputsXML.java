package org.sikuli.recorder;

import org.sikuli.script.Mouse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.time.Duration;
import java.time.LocalDateTime;

public class RecordInputsXML implements RecordInputs {

    private Document doc;
    private Element rootElement;
    private LocalDateTime startTime;

    public void initDocument() {
        startTime = LocalDateTime.now();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        // root elements
        doc = docBuilder.newDocument();
        rootElement = doc.createElement("recording");
        doc.appendChild(rootElement);
    }

    /**
     * Add recorded action to xml document
     * @param name - name of the element
     * @param key - key of the Native Event output
     * @param value - the full output of the Native Event
     */
    public void addElement(String name, String key, String value) {
        if (doc == null || rootElement == null) return;
        Element child = doc.createElement(name);
        rootElement.appendChild(child);
        // add xml attributes
        child.setAttribute("x", Mouse.at().x + "");
        child.setAttribute("y", Mouse.at().y + "");
        child.setAttribute("key", key);
        child.setAttribute("millis", String.valueOf(Duration.between(startTime, LocalDateTime.now()).toMillis()));
        child.setAttribute("nativeEventOutput", value);
    }

    /** Copy the action details to a new child elemnt in this doc
     * @param other the action node to copy
     */
    public void addElement(Node other) {
        if (doc == null || rootElement == null) return;
        Element child = doc.createElement(other.getNodeName());
        rootElement.appendChild(child);
        NamedNodeMap nodeMap = other.getAttributes();
        child.setAttribute("x", nodeMap.getNamedItem("x").getNodeValue());
        child.setAttribute("y", nodeMap.getNamedItem("y").getNodeValue());
        child.setAttribute("key", nodeMap.getNamedItem("key").getNodeValue());
        child.setAttribute("millis", nodeMap.getNamedItem("millis").getNodeValue());
        child.setAttribute("nativeEventOutput", nodeMap.getNamedItem("nativeEventOutput").getNodeValue());
    }

    public Document getDoc() {
        return doc;
    }

}
