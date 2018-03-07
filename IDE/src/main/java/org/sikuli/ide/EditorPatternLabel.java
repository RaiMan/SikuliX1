/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.text.Element;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.Image;
import org.sikuli.script.Location;

public class EditorPatternLabel extends EditorRegionLabel {

  public static String CAPTURE = "__CLICK-TO-CAPTURE__";
  public static String NOTFOUND = "!? ";
  private String lblText;
  private EditorPane pane;
  private float sim;
  private Location off;
  private String imgName = null;
  private String imgNameShort = null;
  private String imgFile;
  private Image image;
  private JFrame imgpop = null;
  private boolean onImagePath = true;
  private boolean isFromCapture;
  private Border pbrd = BorderFactory.createEmptyBorder(5, 5, 5, 5);
  private Border ibrd = BorderFactory.createLineBorder(Color.BLACK);
  private Border brd = BorderFactory.createCompoundBorder(pbrd, ibrd);
//TODO clickToShow: make a user pref
  private boolean clickToShow = false;

  private SikuliIDEPopUpMenu popMenu = null;
  private boolean wasPopup = false;

  public EditorPatternLabel(EditorPane parentPane, String str) {
    super(parentPane, str);
    initLabel(parentPane);
  }

  public EditorPatternLabel(EditorPane parentPane, String str, boolean fromCapture) {
    super(parentPane, str);
    isFromCapture = fromCapture;
    initLabel(parentPane);
  }

  public EditorPatternLabel(EditorPane parentPane, String str, String oldString) {
    super(parentPane, str, oldString);
    isFromCapture = true;
    initLabel(parentPane);
  }

  public EditorPatternLabel(EditorPane parentPane, EditorPatternButton btn) {
    super(parentPane, btn.toString());
    initLabel(parentPane);
  }

  private void initLabel(EditorPane parentPane) {
    pane = parentPane;
    sim = 0.7F;
    off = new Location(0, 0);
    if ("".equals(pyText)) {
      lblText = CAPTURE;
      pyText = "\"" + lblText + "\"";
    } else if (pyText.startsWith("Pattern")) {
      String[] tokens = pyText.split("\\)\\s*\\.?");
      for (String tok : tokens) {
        //System.out.println("token: " + tok);
        if (tok.startsWith("exact")) {
          sim = 0.99F;
        } else if (tok.startsWith("Pattern")) {
          setFileNames(tok.substring(tok.indexOf("\"") + 1, tok.lastIndexOf("\"")));
          if (lblText == null) {
            break;
          }
        } else if (tok.startsWith("similar")) {
          String strArg = tok.substring(tok.lastIndexOf("(") + 1);
          try {
            sim = Float.valueOf(strArg);
          } catch (NumberFormatException e) {
            sim = 0.7F;
          }
        } else if (tok.startsWith("targetOffset")) {
          String strArg = tok.substring(tok.lastIndexOf("(") + 1);
          String[] args = strArg.split(",");
          try {
            off = new Location(0, 0);
            off.x = Integer.valueOf(args[0]);
            off.y = Integer.valueOf(args[1]);
          } catch (NumberFormatException e) {
          }
        }
      }
      if (lblText != null) {
        setLabelText();
      }
    } else {
      setFileNames(pyText.replaceAll("\"", ""));
    }
    if (lblText == null) {
      onImagePath = false;
    } else {
      setText(lblText);
      setLabelPyText();
    }
  }

  @Override
  public boolean isRegionLabel() {
    return false;
  }

	private void setFileNames(String givenName) {
		lblText = null;
		if (!new File(givenName).isAbsolute()
						&& FileManager.isFilenameDotted(givenName)) {
			return;
		}
		Image img = pane.getImageInBundle(givenName);
		if (img.isValid()) {
			if (isFromCapture || !img.isAbsolute() || img.isBundled()) {
				image = img;
				imgFile = img.getFilename();
				imgName = img.getName();
				imgNameShort = imgName.replaceFirst(".png", "").replaceFirst(".jpg", "");
				lblText = imgNameShort;
			}
		}
	}

  public boolean isOnImagePath() {
    return onImagePath;
  }

  public void showPopup(boolean show) {
    if (show) {
      if (imgpop == null) {
        BufferedImage img = image.get();
        if (img == null) {
          Debug.log(4, "EditorPatternLabel: mouseEntered: not found " + this.imgName);
          return;
        }
        imgpop = new JFrame();
        imgpop.setAlwaysOnTop(true);
        imgpop.setUndecorated(true);
        imgpop.setResizable(false);
        imgpop.setFocusableWindowState(false);
        imgpop.setBackground(Color.WHITE);
        Container p = imgpop.getContentPane();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JLabel lbl = new JLabel();
        lbl.setIcon(new ImageIcon(img));
        lbl.setBorder(brd);
        p.add(Box.createHorizontalGlue());
        p.add(lbl);
        p.add(Box.createHorizontalGlue());
        imgpop.pack();
      }
      Point p = getLocationOnScreen();
      Rectangle r = (new Location(p)).getScreen().getRect();
      Point p1 = new Point();
      if (p.y < (r.y + r.height) / 2) {
        p1.y = p.y + getHeight() + 3;
      } else {
        p1.y = p.y - 3 - imgpop.getHeight();
      }
      if (p.x < (r.x + r.width) / 2) {
        p1.x = p.x;
      } else {
        p1.x = p.x - imgpop.getWidth() + getWidth();
      }
      imgpop.setLocation(p1);
      imgpop.setVisible(true);
    } else {
      if (imgpop != null) {
        imgpop.setVisible(false);
      }
    }
  }

  public boolean isCaptureButton() {
    return (CAPTURE.equals(lblText) || lblText.startsWith(NOTFOUND));
  }

  public void resetLabel(String givenFileName, float sim, Location off) {
    imgName = (new File(givenFileName)).getName();
    image = Image.createThumbNail(imgName);
    imgFile = image.getFilename();
    imgNameShort = imgName.replaceFirst(".png", "").replaceFirst(".jpg", "");
    this.sim = sim;
    this.off = off;
    setLabelText();
    setLabelPyText();
  }

  public void setLabelText() {
    String buttonSimilar = "";
    if (sim != 0.7F) {
      buttonSimilar = String.format(Locale.ENGLISH, " .%d", (int) (sim * 100F));
    }
    String buttonOffset = "";
    if (off != null && (off.x != 0 || off.y != 0)) {
      buttonOffset = String.format(" (%d,%d)", off.x, off.y);
    }
    lblText = imgNameShort + buttonSimilar + buttonOffset;
    setText(lblText);
  }

  public void setLabelPyText() {
    if (!lblText.startsWith(NOTFOUND)) {
      pyText = pane.getPatternString(imgName, sim, off, image);
    }
  }

  public void setFile(String imgFile) {
    imgName = (new File(imgFile)).getName();
    pyText = "\"" + imgName + "\"";
    imgNameShort = pyText.replaceAll("\"", "").replaceFirst(".png", "").replaceFirst(".jpg", "");
    lblText = imgNameShort;
    setText(lblText);
  }

  public String getFile() {
    return imgFile;
  }

  public void setFileName(String img) {
    this.imgName = img;
  }

  public void setTargetOffset(Location off) {
    this.off = off;
  }

  public Location getTargetOffset() {
    return off;
  }

  public void setSimilarity(float sim) {
    this.sim = sim;
  }

  public float getSimilarity() {
    return sim;
  }

  public EditorPane getPane() {
    return pane;
  }

  public static EditorPatternLabel labelFromString(EditorPane parentPane, String str) {
    EditorPatternLabel reg = new EditorPatternLabel(parentPane, str, false);
    if (reg.isOnImagePath()) {
      return reg;
    } else {
      reg = null;
      return null;
    }
  }

  @Override
  public void mousePressed(MouseEvent me) {
    checkPopup(me);
  }

  @Override
  public void mouseReleased(MouseEvent me) {
    checkPopup(me);
  }

  private void checkPopup(MouseEvent me) {
    if (me.isPopupTrigger()) {
      wasPopup = true;
      popMenu = pane.getPopMenuImage();
      if (popMenu != null) {
        if (imgpop != null && imgpop.isShowing()) {
          showPopup(false);
        }
        popMenu.show(this, me.getX(), me.getY());
      }
      return;
    }
  }

  @Override
  public void mouseClicked(MouseEvent me) {
    if (wasPopup) {
      wasPopup = false;
      return;
    }
    if (clickToShow) {
      if (imgpop != null) {
        if (!imgpop.isShowing()) {
          showPopup(true);
          return;
        } else {
          showPopup(false);
        }
      } else {
        showPopup(true);
        return;
      }
    } else {
      if (imgpop != null && imgpop.isShowing()) {
        showPopup(false);
      }
    }
    if (!CAPTURE.equals(lblText) && !lblText.startsWith(NOTFOUND)) {
      (new EditorPatternButton(this)).actionPerformed(null);
    } else {
      Element x = pane.getLineAtPoint(me);
//TODO recapture not found image
      if (x == null || lblText.startsWith(NOTFOUND)) {
        return;
      }
      (new ButtonCapture(pane, x)).actionPerformed(null);
    }
  }

  @Override
  public void mouseEntered(MouseEvent me) {
    super.mouseEntered(me);
    if (CAPTURE.equals(lblText) || lblText.startsWith(NOTFOUND)) {
      return;
    }
    if (!clickToShow) {
      showPopup(true);
    }
  }

  @Override
  public void mouseExited(MouseEvent me) {
    super.mouseExited(me);
    if (CAPTURE.equals(lblText)) {
      return;
    }
    showPopup(false);
  }

  @Override
  public String toString() {
    return super.toString();
  }

}
