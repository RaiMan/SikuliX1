package org.sikuli.recorder;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.SikuliXception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

public class StandardSaveToFile implements SaveFile {

    /**
     * Save the BufferedImage into the given path
     * @param bimg the BufferedImage
     * @param path the path to save the image
     * @return the absolute path of the saved image
     */
    public String saveWithDate(BufferedImage bimg, File path, String baseFileName) {
        File fImage = new File(path, String.format("%s-%d.png", baseFileName, new Date().getTime()));
        try {
            ImageIO.write(bimg, FilenameUtils.getExtension(fImage.getName()), fImage);
            Debug.log(3, "ScreenImage::saveImage: %s", fImage);
        } catch (Exception ex) {
            Debug.error("ScreenImage::saveInto: did not work: %s (%s)", fImage, ex.getMessage());
            return null;
        }
        return fImage.getAbsolutePath();
    }

    public File createDirectory(String prefix) {
        File screenshotDir;
        try {
            screenshotDir = Files.createTempDirectory(prefix).toFile();
            return screenshotDir;
        } catch (IOException e) {
            throw new SikuliXception("Recorder: createTempDirectory: not possible");
        }
    }
}
