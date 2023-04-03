package org.sikuli.recorder;

import org.w3c.dom.Document;

import java.awt.image.BufferedImage;
import java.io.File;

public interface SaveFile {

    String saveWithDate(BufferedImage bimg, File path, String baseFileName);
    File createDirectory(String prefix);
    void saveXML(Document doc, String path);
}
