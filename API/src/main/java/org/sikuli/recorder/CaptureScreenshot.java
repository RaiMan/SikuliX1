package org.sikuli.recorder;

import org.sikuli.script.Image;
import org.sikuli.script.Mouse;
import org.sikuli.script.Screen;

/**
 * Captures the screenshot from the screen where the mouse is located and saves it to the given directory.
 *
 * @author jspinak
 */
public class CaptureScreenshot {

    private SaveToFile saveToFile;

    public CaptureScreenshot(SaveToFile saveToFile) {
        this.saveToFile = saveToFile;
    }

    public void saveScreenshot(String baseFileName) {
        Screen activeScreen = Mouse.at().getMonitor();
        Image screenshot = activeScreen.getImage();
        saveToFile.saveImageWithDate(screenshot, baseFileName);
    }

}
