package org.sikuli.recorder;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.SikuliXception;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
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

    /**
     * Save as XML to file.
     * @param doc the XML Document
     * @param path to save to
     */
    public void saveXML(Document doc, String path) {
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
