package org.sikuli.recorder;

import org.w3c.dom.Document;

public interface RecordInputs {

    void initDocument();
    void addElement(String name, String key, String value);
    Document getDoc();
}
