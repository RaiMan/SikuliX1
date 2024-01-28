package org.sikuli.recorder;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
import org.sikuli.script.support.Commons;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.util.Date;

public class RecorderSaveToFile implements SaveToFile {

  private File folderToUse;

  public RecorderSaveToFile(File folderToUse) {
    this.folderToUse = createFolder(folderToUse);
  }

  public File createFolder(File folder) {
    return Commons.asFolder(folder.getPath());
  }

  /**
   * Save the Image as .png into folderToUse
   *
   * @param img the image
   * @return the absolute path of the saved image
   */
  public String saveImageWithDate(Image img, String baseFileName) {
    File fImage = new File(folderToUse, String.format("%s-%d.png", baseFileName, new Date().getTime()));
    try {
      ImageIO.write(img.get(), FilenameUtils.getExtension(fImage.getName()), fImage);
    } catch (Exception ex) {
      Debug.error("ScreenImage::saveImage: did not work: %s (%s)", fImage, ex.getMessage());
      return null;
    }
    return fImage.getAbsolutePath();
  }

  /**
   * Save as XML to file.
   *
   * @param doc  the XML Document
   */
  public void saveXML(Document doc, String fileName) {
    if (doc == null) return;
    FileOutputStream output;
    try {
      output = new FileOutputStream(new File(folderToUse, fileName));
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
