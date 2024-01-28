package org.sikuli.recorder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ProcessRecording {

    private ProcessNode processNode;

    public ProcessRecording(ProcessNode processNode) {
        this.processNode = processNode;
    }

    /**
     * Simplifies the raw user inputs and returns the simplified XML Document.
     * Different simplification methods can be used based on the given ProcessNode class.
     *
     * @param rawInputsDoc the raw user inputs XML Document
     * @return the simplified XML Document
     */
    public Document process(Document rawInputsDoc) {
        rawInputsDoc.getDocumentElement().normalize();
        NodeList nodeList = rawInputsDoc.getElementsByTagName("recording");

        /*
        Create a new XML Document
         */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document newDoc = builder.newDocument();
        Element root = newDoc.createElement("root");
        newDoc.appendChild(root);

        if (nodeList.item(0).hasChildNodes()) {
            RecordInputsXML processedInputsXML = new RecordInputsXML();
            processedInputsXML.initDocument();
            processNode.populateNodeList(nodeList.item(0).getChildNodes(), processedInputsXML);
            return processedInputsXML.getDoc();
        }

        return newDoc;
    }
}
