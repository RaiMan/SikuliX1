/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.json.simple.JSONValue;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Settings;
import org.sikuli.ide.SikuliIDE;

public class ExtensionManagerFrame extends JFrame {

  final static String EXTENSION_LIST_URL =
          SikuliIDE.runTime.SikuliRepo + "extensions.json";
  private static ExtensionManagerFrame _instance = null;
  private int selected_idx = 0;
  ArrayList<ExtensionItem> _extensions;

  static public ExtensionManagerFrame getInstance() {
    if (_instance == null) {
//TODO reactivate extension manager
      _instance = null;
//      _instance = new ExtensionManagerFrame();
    }
    return _instance;
  }

  private ExtensionManagerFrame() {
    super();

    setTitle("Sikuli Extensions");
    setResizable(false);
    createComponents();

    addKeyListener(new SelectExtensionKeyListener());
    pack();
    setLocationRelativeTo(null);
  }

  private void createComponents() {
    Container pane;

    pane = getContentPane();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

    try {

      // Populate the list of extensions
      _extensions = retrieveExtensions();

      for (ExtensionItem ext : _extensions) {
        pane.add(ext);
      }

      // Select the first one
      select(0);

    } catch (IOException io) {

      String msg = "Unable to load extensions list from: " + EXTENSION_LIST_URL;
      JTextField txt = new JTextField(msg);
      txt.setBackground(null);
      txt.setBorder(null);
      txt.setEditable(false);
      pane.add(txt);
    }

    JPanel bottomBar = new JPanel();
    bottomBar.setLayout(new BorderLayout());
    bottomBar.setMinimumSize(new Dimension(400, 20));
    JButton closeBtn = new JButton("Close");

    closeBtn.addActionListener(new ActionListener() {
//TODO: clean extensions: old versions?
      @Override
      public void actionPerformed(ActionEvent arg0) {
        dispose();
      }
    });

    closeBtn.setFocusable(false);
    bottomBar.add(closeBtn, BorderLayout.LINE_END);
    pane.add(bottomBar);

  }

  private ArrayList<ExtensionItem> retrieveExtensions() throws IOException {

    ArrayList<ExtensionItem> extensions = new ArrayList<ExtensionItem>();

    Debug.log(2, "Retrieving from " + EXTENSION_LIST_URL);

    String json = ExtensionManagerFrame.html2txt(EXTENSION_LIST_URL);

    Object obj = JSONValue.parse(json);
    Map map = (Map) obj;

    Map extension_list = (Map) map.get("extension-list");
    List exts = (List) extension_list.get("extensions");

    for (Object o : exts) {
      Map ext = (Map) o;

      String name = (String) ext.get("name");
      String version = (String) ext.get("version");
      String description = (String) ext.get("description");
      String imgurl = (String) ext.get("imgurl");
      String infourl = (String) ext.get("infourl");
      String jarurl = (String) ext.get("jarurl");

      extensions.add(new ExtensionItem(name, version, description, imgurl, infourl, jarurl));
    }

    return extensions;
  }

  private static String html2txt(String urlstring) throws IOException {
    URL url = new URL(urlstring);
    URLConnection yc = url.openConnection();
    yc.setConnectTimeout(5000);
    BufferedReader in = new BufferedReader(
            new InputStreamReader(
            yc.getInputStream()));
    String txt = "";
    String inputLine;

    while ((inputLine = in.readLine()) != null) {
      txt = txt + inputLine;
    }
    in.close();

    return txt;

  }

  protected void select(ExtensionItem ext) {
    int idx = _extensions.indexOf(ext);
    select(idx);
  }

  private void select(int idx) {
    _extensions.get(selected_idx).setSelected(false);
    selected_idx = idx;
    _extensions.get(selected_idx).setSelected(true);
  }

  private void selectPrevious() {
    if (selected_idx == 0) {
      return;
    }

    select(selected_idx - 1);
  }

  private void selectNext() {
    if (selected_idx == _extensions.size() - 1) {
      return;
    }

    select(selected_idx + 1);
  }

  private class SelectExtensionKeyListener implements KeyListener {

    @Override
    public void keyPressed(KeyEvent e) {
      int key = e.getKeyCode();
      if (key == KeyEvent.VK_UP) {
        selectPrevious();
      } else if (key == KeyEvent.VK_DOWN) {
        selectNext();
      }

    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }
  }

//  public static void main(String[] args) throws IOException {
//
//    ExtensionManagerFrame f = ExtensionManagerFrame.getInstance();
//    f.setVisible(true);
//  }
}
