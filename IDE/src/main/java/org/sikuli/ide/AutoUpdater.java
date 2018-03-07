/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;

public class AutoUpdater {

  private String details, bdetails;
  private String server = "";
  private String bserver = "";
  private int major, minor, sub;
  private int bmajor, bminor, beta;
  private int smajor, sminor, ssub, sbeta;
  private String name;
  public static int MAJOR = 1;
  public static int MINOR = 2;
  public static int SUB = 3;
  public static int SOMEBETA = 10;
  public static int BETA = 5;
  public static int FINAL = 6;
  private int available = 0;
  private boolean notAvailable = false;
  public String whatUpdate;

  public String getServer() {
    return server;
  }

  public String getVersion() {
    if (available > 0) {
      return String.format("%s-%d.%d.%d", name, major, minor, sub);
    }
    return "";
  }

  public String getVersionNumber() {
    if (available > 0) {
      return String.format("%d.%d.%d", major, minor, sub);
    }
    return "";
  }

  public String getBeta() {
    if (available == BETA || available >= SOMEBETA) {
      return String.format("%s-%d.%d-Beta%d", name, bmajor, bminor, beta);
    }
    return "";
  }

  public String getBetaVersion() {
    if (beta > 0) {
      return String.format("%d.%d-Beta%d", bmajor, bminor, beta);
    } else {
      return "";
    }
  }

  public String getDetails() {
    return details;
  }

  public String getBetaDetails() {
    return bdetails;
  }

  public int checkUpdate() {
    for (String s : SikuliIDE.runTime.ServerList) {
      try {
        if (checkUpdate(s)) {
          smajor = SikuliIDE.runTime.SikuliVersionMajor;
          sminor = SikuliIDE.runTime.SikuliVersionMinor;
          ssub = SikuliIDE.runTime.SikuliVersionSub;
          sbeta = SikuliIDE.runTime.SikuliVersionBetaN;
          if (sbeta > 0) {
            if (smajor == major && sminor == minor) {
              available = FINAL;
              whatUpdate = "The final version is available: " + getVersion();
              Debug.info(whatUpdate);
            } else if (smajor == bmajor && sminor == bminor && beta > sbeta) {
              available = BETA;
              whatUpdate = "A new beta version is available: " + getVersion();
              Debug.info(whatUpdate);
            }
          } else {
            if (major > smajor) {
              available = MAJOR;
              whatUpdate = "A new major version is available: " + getVersion();
              Debug.info(whatUpdate);
            } else if (major == smajor && minor > sminor) {
              available = MINOR;
              whatUpdate = "A new minor version is available: " + getVersion();
              Debug.info(whatUpdate);
            } else if (major == smajor && minor == sminor && sub > ssub) {
              available = SUB;
              whatUpdate = "A new service update is available: " + getVersion();
              Debug.info(whatUpdate);
            }
          }
          if (beta > 0 && (bmajor > smajor || (bmajor == smajor && bminor > sminor))) {
            available += SOMEBETA;
            Debug.info("A beta version is available: " + getVersion());
          }
        }
      } catch (Exception e) {
        notAvailable = true;
      }
      if (notAvailable) {
        Debug.log(2, "No version info available at " + s);
        return 0;
      }
    }
    return available;
  }

  private boolean checkUpdate(String s) throws IOException, MalformedURLException {
    // contents of latestversion
    //SikuliX 1 0 0 1 0 999
    //DOWNLOAD https://launchpad.net/sikuli/+download
    //BETA https://dl.dropboxusercontent.com/u/42895525/SikuliX/index.html
    URL url = new URL(s + "/latestversion");
    URLConnection conn;
    if (FileManager.getProxy() != null) {
      conn = url.openConnection(FileManager.getProxy());
    } else {
      conn = url.openConnection();
    }
    BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream()));
    String line;
    if ((line = in.readLine()) != null) {
      String[] vinfo = line.trim().split(" ");
      if (vinfo.length > 6) {
        name = vinfo[0];
        major = Integer.parseInt(vinfo[1]);
        minor = Integer.parseInt(vinfo[2]);
        sub = Integer.parseInt(vinfo[3]);
        bmajor = Integer.parseInt(vinfo[4]);
        bminor = Integer.parseInt(vinfo[5]);
        beta = Integer.parseInt(vinfo[6]);
      } else {
        notAvailable = true;
        return false;
      }
      details = "";
      if ((line = in.readLine()) != null) {
        if (line.startsWith("DOWNLOAD")) {
          server = line.split(" ")[1];
          details += "Please download at: " + server + "<br>";
          details += "-------------------------------------------------------------------------";
          details += "<br><br>";
        } else {
          details += line;
        }
      }
      bdetails = "";
      while ((line = in.readLine()) != null) {
        if (line.startsWith("BETA")) {
          if (beta > 0) bdetails = line;
          break;
        }
        details += line;
      }
      if (beta > 0) {
        if (! "".equals(bdetails)) {
          bserver = bdetails.split(" ")[1];
          bdetails = "Please download at: " + bserver + "<br>";
          bdetails += "-------------------------------------------------------------------------";
          bdetails += "<br><br>";
        }
        while ((line = in.readLine()) != null) {
          bdetails += line;
        }
      }
      in.close();
      return true;
    }
    return false;
  }

  public JFrame showUpdateFrame(String title, String text, int whatUpdate) {
    if (whatUpdate < 0) {
      return new UpdateFrame(title, text, null);
    } else {
      if (whatUpdate == BETA) {
        return new UpdateFrame(title, text, bserver);
      } else {
        return new UpdateFrame(title, text, server);
      }
    }
  }
}
class UpdateFrame extends JFrame {
  public UpdateFrame(String title, String text, String server) {
    setTitle(title);
    setSize(300, 200);
    setLocationRelativeTo(getRootPane());
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    JEditorPane p = new JEditorPane("text/html", text);
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.setEditable(false);
    cp.add(new JScrollPane(p), BorderLayout.CENTER);
    JButton btnOK = new JButton("ok");
    if (server != null) {
      p.addHyperlinkListener(new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
            try {
              FileManager.openURL(e.getURL().toString());
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        }
      });
      JPanel buttonPane = new JPanel();
      btnOK.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          UpdateFrame.this.dispose();
        }
      });
      JButton btnGo = new JButton("download");
      btnGo.setToolTipText(server);
      btnGo.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) {
          FileManager.openURL(((JButton) ae.getSource()).getToolTipText());
          UpdateFrame.this.dispose();
        }
      });
      buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
      buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
      buttonPane.add(Box.createHorizontalGlue());
      buttonPane.add(btnGo);
      buttonPane.add(btnOK);
      buttonPane.add(Box.createHorizontalGlue());
      getRootPane().setDefaultButton(btnOK);
      cp.add(buttonPane, BorderLayout.PAGE_END);
    }
    cp.doLayout();
    pack();
    setVisible(true);
    btnOK.requestFocus();
  }
}
