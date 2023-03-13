package org.sikuli.recorder;

import org.sikuli.script.Mouse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
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

    public void saveDocument(String path) {
        if (doc == null) return;
        String fullPath = FileSystems.getDefault().getPath(".") + path;
        FileOutputStream output;
        try {
            output = new FileOutputStream(fullPath);
            writeXml(doc, output);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // write doc to output stream
    private void writeXml(Document doc, OutputStream output) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // makes it look nice
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
