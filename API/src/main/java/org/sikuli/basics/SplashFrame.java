/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuli.basics;

import java.awt.Color;
import java.awt.Container;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import org.sikuli.script.RunTime;

/**
 *
 * @author rhocke
 */
public class SplashFrame extends JFrame {

  private static JFrame splash = null;
  private static long start = 0;

  public static void displaySplash(String[] args) {
    if (args == null) {
      if (splash != null) {
        splash.dispose();
      }
      if (start > 0) {
        Debug.log(3, "Sikuli-Script startup: " + ((new Date()).getTime() - start));
        start = 0;
      }
      return;
    }
    if (args.length > 0 && (args[0].contains("-testSetup") || args[0].startsWith("-i"))) {
      start = (new Date()).getTime();
      String[] splashArgs = new String[]{
        "splash", "#", "#" + RunTime.get().SikuliVersionScript, "", "#", "#... starting - please wait ..."};
      for (String e : args) {
        splashArgs[3] += e + " ";
      }
      splashArgs[3] = splashArgs[3].trim();
      splash = new SplashFrame(splashArgs);
    }
  }

  public static void displaySplashFirstTime(String[] args) {
    if (args == null) {
      if (splash != null) {
        splash.dispose();
      }
      if (start > 0) {
        Debug.log(3, "Sikuli-IDE environment setup: " + ((new Date()).getTime() - start));
        start = 0;
      }
      return;
    }
    start = (new Date()).getTime();
    String[] splashArgs = new String[]{
      "splash", "#", "#" + RunTime.get().SikuliVersionIDE, "", "#", "#... setting up environement - please wait ..."};
    splash = new SplashFrame(splashArgs);
  }

  private JLabel lbl, txt;
  private Container pane;
  private int proSize;
  private int fw, fh;

  public SplashFrame(String type) {
    init(new String[]{type});
  }

  public SplashFrame(String[] args) {
    init(args);
  }
  private void init(String[] args) {
    setResizable(false);
    setUndecorated(true);
    pane = getContentPane();

    if ("download".equals(args[0])) {
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(new JLabel(" "));
      lbl = new JLabel("");
      lbl.setAlignmentX(CENTER_ALIGNMENT);
      pane.add(lbl);
      pane.add(new JLabel(" "));
      txt = new JLabel("... waiting");
      txt.setAlignmentX(CENTER_ALIGNMENT);
      pane.add(txt);
      fw = 350;
      fh = 80;
    }

    if ("splash".equals(args[0])) {
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.setBackground(Color.yellow);
      int n = args.length;
      String e;
      int l = 0;
      int nlbl = 0;
      for (int i = 1; i < n; i++) {
        e = args[i];
        if (e.length() > l) {
          l = e.length();
        }
        if (e.length() > 1 && e.startsWith("#")) {
          nlbl++;
        }
      }
      JLabel[] lbls = new JLabel[nlbl];
      nlbl = 0;
      for (int i = 1; i < n; i++) {
        e = args[i];
        if (e.startsWith("#")) {
          if (e.length() > 1) {
            lbls[nlbl] = new JLabel(e.substring(1));
            lbls[nlbl].setAlignmentX(CENTER_ALIGNMENT);
            pane.add(lbls[nlbl]);
            nlbl++;
          }
          pane.add(new JSeparator());
        } else {
          pane.add(new JLabel(e));
        }
      }
      fw = 10 + 10*l;
      fh = 10 + n*15 + nlbl*15;
    }

    pack();
    setSize(fw, fh);
    setLocationRelativeTo(null);
    setAlwaysOnTop(true);
    setVisible(true);
  }

  public void setProFile(String proFile) {
    lbl.setText("Downloading: " + proFile);
  }

  public void setProSize(int proSize) {
    this.proSize = proSize;
  }

  public void setProDone(int done) {
    if (done < 0) {
      txt.setText(" ..... failed !!!");
    } else if (proSize > 0) {
      txt.setText(done + " % out of " + proSize + " KB");
    } else {
      txt.setText(done + " KB out of ??? KB");
    }
    repaint();
  }

  public void closeAfter(int secs) {
    try {
      Thread.sleep(secs*1000);
    } catch (InterruptedException ex) {
    }
    setVisible(false);
  }
}
