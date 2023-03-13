package org.sikuli.recorder;

public interface RecordInputs {

    public void initDocument();
    public void addElement(String name, String key, String value);
    public void saveDocument(String path);
}
