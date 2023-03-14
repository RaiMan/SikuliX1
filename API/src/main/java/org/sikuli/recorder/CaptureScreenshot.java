package org.sikuli.recorder;

import org.sikuli.script.Image;
import org.sikuli.script.Mouse;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import java.io.File;

/**
 * Captures the screenshot from the screen where the mouse is located and saves it to the given directory.
 *
 * @author jspinak
 */
public class CaptureScreenshot {

    private SaveFile saveFile;

    public CaptureScreenshot(SaveFile saveFile) {
        this.saveFile = saveFile;
    }

    public void saveScreenshot(File directory, String baseFileName) {
        Screen activeScreen = Mouse.at().getMonitor();
        Image screenshot = activeScreen.getImage();
        saveFile.saveWithDate(screenshot.get(), directory, baseFileName);
    }

}
