package org.sikuli.recorder;

import java.awt.image.BufferedImage;
import java.io.File;

public interface SaveFile {

    String saveWithDate(BufferedImage bimg, File path, String baseFileName);
    File createDirectory(String prefix);
}
