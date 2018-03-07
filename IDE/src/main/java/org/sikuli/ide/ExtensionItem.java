/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import org.sikuli.basics.Debug;
import org.sikuli.basics.ExtensionManager;
import org.sikuli.basics.Settings;
import org.sikuli.ide.SikuliIDEI18N;
import org.sikuli.ide.SikuliIDEI18N;

class ExtensionItem extends JPanel implements ActionListener {

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
        url = new URL(SikuliIDE.runTime.SikuliRepo + "extensionImage.jpg");
        image = ImageIO.read(url);
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

    JButton btn = new JButton(SikuliIDEI18N._I("extBtnInstall"));
    btn.addActionListener(this);
    btn.setActionCommand("Install");
    _installCtrl = btn;
    _installCtrl.setFocusable(false);

    btn = new JButton(SikuliIDEI18N._I("extBtnInfo"));
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

        ExtensionManagerFrame.getInstance().select((ExtensionItem) e.getSource());
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
      _installCtrl.setText(SikuliIDEI18N._I("extMsgInstalled"));
    } else if (status == NOT_INSTALLED) {
      _installCtrl.setEnabled(true);
      _installCtrl.setText(SikuliIDEI18N._I("extBtnInstallVer", _version));
    } else if (status == OUT_OF_DATE) {
      _installCtrl.setEnabled(true);
      _installCtrl.setText(SikuliIDEI18N._I("extBtnUpdateVer", _version));
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
