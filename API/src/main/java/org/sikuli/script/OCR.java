package org.sikuli.script;

public class OCR extends TextRecognizer {

  public static void withSmallFont() {
    TextRecognizer tr = TextRecognizer.start();
    if (tr.isValid()) {
      tr.setHeight(11);
    }
  }
}
