/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.*;
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

  final static String EXTENSION_LIST_URL = "";
//          SikulixIDE.runTime.SikuliRepo + "extensions.json";
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

  static class ExtensionItem extends JPanel implements ActionListener {

    JButton _installCtrl;
    JButton _infoCtrl;
    String _name;
    String _infourl;
    String _jarurl;
    String _version;
    String _description;
    boolean _installed;
    final int NOT_INSTALLED = 0;
    final int INSTALLED = 1;
    final int OUT_OF_DATE = 2;
    int _status = NOT_INSTALLED;
    JPanel _controls;
    JPanel _content;
    JLabel _htmlLabel;

    public ExtensionItem(String name, String version, String description,
            String imgurl, String infourl, String jarurl) {
      this._name = name;
      this._version = version;
      this._infourl = infourl;
      this._infourl = infourl;
      this._jarurl = jarurl;
      this._description = description;
      this._status = getStatus();

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
      setBorder(loweredetched);

      _content = new JPanel();

      Image image = null;
      URL url;
      try {
        // Read from a URL
        url = new URL(imgurl);
        image = ImageIO.read(url);
      } catch (Exception e) {
      }
      if (image == null) {
        try {
  //        url = new URL(RunTime.get().getSikuliRepo() + "extensionImage.jpg");
  //        image = ImageIO.read(url);
        } catch (Exception e) {
        }
      }

      JLabel iconLabel = new JLabel();
      iconLabel.setIcon(new ImageIcon(image));
      iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      _content.setLayout(new BorderLayout(5, 5));
      _content.add(iconLabel, BorderLayout.LINE_START);

      _htmlLabel = new JLabel(renderHTML());
      _content.add(_htmlLabel);
      add(_content);

  //    JButton btn = new JButton(SikuliIDEI18N._I("extBtnInstall"));
      JButton btn = new JButton("extBtnInstall");
      btn.addActionListener(this);
      btn.setActionCommand("Install");
      _installCtrl = btn;
      _installCtrl.setFocusable(false);

  //    btn = new JButton(SikuliIDEI18N._I("extBtnInfo"));
      btn = new JButton("extBtnInfo");
      btn.addActionListener(this);
      btn.setActionCommand("Info");
      _infoCtrl = btn;
      _infoCtrl.setFocusable(false);

      _controls = new JPanel();
      _controls.setLayout(new BorderLayout(5, 5));
      _controls.add(_infoCtrl, BorderLayout.LINE_START);
      _controls.add(_installCtrl, BorderLayout.LINE_END);
      add(_controls);

      updateControls();
      addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {

          getInstance().select((ExtensionItem) e.getSource());
        }
      });
    }

    protected void setSelected(boolean selected) {
      _controls.setVisible(selected);
      /*

       Color darkRed = new Color(0.5f,0.0f,0.0f);

       Color bg,fg;
       if (selected){
       bg = darkRed;//Color.red;
       fg = Color.white;
       }else{
       bg = null;
       fg = Color.black;
       }
       _controls.setBackground(bg);
       _content.setBackground(bg);

       for (Component comp : _content.getComponents()){
       comp.setForeground(fg);
       }
       */

    }

    private String renderHTML() {
      String installed_version = ExtensionManager.getInstance().getVersion(_name);
      if (installed_version == null) {
        installed_version = "Not installed";
      }
      return "<html><div style='width:300px'><b>" + _name + "</b> " + "(" + installed_version + ")" + "<br>"
              + _description + "</div></html>";
    }

    private void updateControls() {

      int status = getStatus();

      if (status == INSTALLED) {
        _installCtrl.setEnabled(false);
  //      _installCtrl.setText(SikuliIDEI18N._I("extMsgInstalled"));
        _installCtrl.setText("extMsgInstalled");
      } else if (status == NOT_INSTALLED) {
        _installCtrl.setEnabled(true);
  //      _installCtrl.setText(SikuliIDEI18N._I("extBtnInstallVer", _version));
        _installCtrl.setText("extBtnInstallVer");
      } else if (status == OUT_OF_DATE) {
        _installCtrl.setEnabled(true);
  //      _installCtrl.setText(SikuliIDEI18N._I("extBtnUpdateVer", _version));
        _installCtrl.setText("extBtnUpdateVer");
      }

      _htmlLabel.setText(renderHTML());
    }

    private int getStatus() {

      ExtensionManager extMgr = ExtensionManager.getInstance();

      if (!extMgr.isInstalled(_name)) {
        return NOT_INSTALLED;
      } else if (extMgr.isOutOfDate(_name, _version)) {
        return OUT_OF_DATE;
      } else {
        return INSTALLED;
      }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equals("Install")) {
        Debug.log("Installing " + _name + " from " + _jarurl);

        ExtensionManager extMgr = ExtensionManager.getInstance();

        // try to install the extension
        if (extMgr.install(_name, _jarurl, _version)) {

          // if successful, change the item's status to installed
          //_installed = true;
          updateControls();
        }

      } else if (cmd.equals("Info")) {

        Debug.log("Openning URL: " + _infourl);
        openURL(_infourl, _name);
      }

    }

    static void openURL(String url, String name) {
      try {
        URL u = new URL(url);
        java.awt.Desktop.getDesktop().browse(u.toURI());
      } catch (Exception ex) {
        Debug.error("SikuliExtension: " + name + " -- no information available!");
      }
    }

  }
}
