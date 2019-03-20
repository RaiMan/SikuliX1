/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.RunTime;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExtensionManagerFrame extends JFrame {

  final static String EXTENSION_LIST_URL =
          SikuliIDE.runTime.SikuliRepo + "extensions.json";
  private static ExtensionManagerFrame _instance = null;
  private int selected_idx = 0;
  ArrayList<ExtensionItem> _extensions;

  private ExtensionManagerFrame() {
    super();
    fExtensions = RunTime.get().fSikulixExtensions.listFiles();
//TODO show and select extensions
//    init();
  }

  static public ExtensionManagerFrame getInstance() {
    if (_instance == null) {
      _instance = new ExtensionManagerFrame();
    }
    return _instance;
  }

  private File[] fExtensions;

  public List<String> getClasspath() {
    return classpath;
  }

  private List<String> classpath;

  public List<File> getExtensionFiles() {
    List<File> extensions = new ArrayList<>();
    for (File extension : fExtensions) {
      String name = extension.getName();
      if (name.startsWith(".") || !name.endsWith(".jar")) {
        continue;
      }
      extensions.add(extension);
    }
    return extensions;
  }

  public List<String> getExtensionNames() {
    List<String> extensions = new ArrayList<>();
    for (File extension : fExtensions) {
      String name = extension.getName();
      if (name.startsWith(".") || !name.endsWith(".jar")) {
        if (name.startsWith("extension_classpath.txt")) {
          extensions.add(0, name);
          classpath = new ArrayList<>();
          List<String> content = Arrays.asList(FileManager.readFileToString(extension).split("\\n"));
          for (String line : content) {
            line = line.trim();
            if (line.startsWith("#") || line.startsWith("//")) {
              continue;
            }
            classpath.add(line);
          }
        }
        continue;
      }
      extensions.add(name);
    }
    return extensions;
  }

  private void init() {
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

    //TODO use Jackson
    Object obj = null; //JSONValue.parse(json);
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
