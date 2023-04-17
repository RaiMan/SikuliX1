package org.sikuli.recorder;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This node processing class assumes that the important point in a mouse movement occur when
 * the user pauses before continuing the mouse movement. Pauses are usually not on purpose but part of
 * the natural movement of the hand.
 *
 * @author jspinak
 */
public class ProcessNodePauses implements ProcessNode {

    private double timelapseFromStartOfRecording; // in milliseconds
    private double lastTimeLapse = 0.0;
    static double minTimeDifference = 400; // time difference between adjacent points necessary to include the point in the new NodeList

    public void populateNodeList(NodeList rawData, RecordInputsXML doc) {
        for (int count = 0; count < rawData.getLength(); count++) {
            Node tempNode = rawData.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) { // make sure it's element node.
                if (tempNode.hasAttributes()) {
                    if (tempNode.getNodeName().equals("MOVE") ||
                        tempNode.getNodeName().equals("DRAG")) {
                        NamedNodeMap nodeMap = tempNode.getAttributes();
                        timelapseFromStartOfRecording = Double.parseDouble(nodeMap.getNamedItem("millis").getNodeValue());
                        if (timelapseFromStartOfRecording - lastTimeLapse >= minTimeDifference
                            || lastTimeLapse == 0.0) {
                            doc.addElement(tempNode);
                            lastTimeLapse = timelapseFromStartOfRecording;
                        }
                    } else {
                        doc.addElement(tempNode);
                    }
                }
            }
        }
    }
}
