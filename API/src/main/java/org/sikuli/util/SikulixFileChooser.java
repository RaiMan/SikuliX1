/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
//import java.io.FilenameFilter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.PreferencesUser;

public class SikulixFileChooser {
  static final int FILES = JFileChooser.FILES_ONLY;
  static final int DIRS = JFileChooser.DIRECTORIES_ONLY;
  static final int DIRSANDFILES = JFileChooser.FILES_AND_DIRECTORIES;
  static final int SAVE = FileDialog.SAVE;
  static final int LOAD = FileDialog.LOAD;

  private Frame parentFrame;

  public SikulixFileChooser(Frame parentFrame) {
    this.parentFrame = parentFrame;
  }

  private String getLastDir() {
    return PreferencesUser.get().get("LAST_OPEN_DIR", "");
  }

  //TODO implement according to SX.doPop
  private boolean fromPopFile = false;

  public File open() {
    File selectedFile = show("Open a file or folder", LOAD, DIRSANDFILES);
    return selectedFile;
  }

  public File open(String title) {
    //fromPopFile = true;
    File selectedFile = show(title, LOAD, DIRSANDFILES);
    return selectedFile;
  }

  public File save() {
    File selectedFile = show("Save a File or Folder", SAVE, DIRSANDFILES);
    return selectedFile;
  }

  public File saveAs(String extension, boolean isBundle) {
    File selectedFile;
    if (isBundle)
      selectedFile = show("Save as .sikuli, folder or file", SAVE, DIRSANDFILES
            , new SXFilter("as file", extension)
            , new SXFilter("as plain folder", SXFilter.FOLDER)
            , new SXFilter("as folder.sikuli", SXFilter.SIKULI)
             );
    else {
      selectedFile = show("Save as .sikuli, folder or file", SAVE, DIRSANDFILES
          , new SXFilter("as file", extension)
      );
    }
    return selectedFile;
  }

  public File export() {
    String title = "Export packed as .skl or .zip";
    File ret = show(title, SAVE, FILES);
    return ret;
  }

  public File loadImage() {
    File ret = show("Load Image File", LOAD, FILES,
            new FileNameExtensionFilter("Image files (jpg, png)", "jpg", "jpeg", "png"));
    return ret;
  }

  private File show(final String title, final int mode, final int theSelectionMode, Object... filters) {
    Debug.log(3, "SikulixFileChooser: %s at %s", title.split(" ")[0], getLastDir());
    File fileChosen;
    final Object[] genericFilters = filters;
    final Object[] result = new Object[]{null, null};
    if (fromPopFile) {
      try {
        EventQueue.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            processDialog(theSelectionMode, getLastDir(), title, mode, genericFilters, result);
          }
        });
      } catch (Exception e) {
      }
    } else {
      processDialog(theSelectionMode, getLastDir(), title, mode, filters, result);
      if (filters.length == 0) {
        result[1] = new SXFilter("", SXFilter.GENERIC);
      }
    }
    if (null != result[0]) {
      fileChosen = (File) result[0];
      PreferencesUser.get().put("LAST_OPEN_DIR", fileChosen.getParent());
      if (result[1] != null) {
        if (result[1].getClass().equals(SXFilter.class)) {
          SXFilter filter = (SXFilter) result[1];
          if (filter.isType(SXFilter.GENERIC)) {
            if (fileChosen.getName().equals(fileChosen.getParentFile().getName())) {
              fileChosen = fileChosen.getParentFile();
            }
          } else if (filter.isType(SXFilter.SIKULI)) {
            if (FilenameUtils.getExtension(fileChosen.getName()).equals("")) {
              fileChosen = new File(fileChosen.getAbsolutePath() + ".sikuli");
            } else if (!FilenameUtils.getExtension(fileChosen.getName()).equals("sikuli")) {
              fileChosen = new File(FilenameUtils.removeExtension(
                      fileChosen.getAbsolutePath()) + ".sikuli");
            }
          } else if (filter.isType(SXFilter.FOLDER)) {
            fileChosen = new File(FilenameUtils.removeExtension(fileChosen.getAbsolutePath()));
          } else if (filter.isType(SXFilter.FILE)) {
            fileChosen = new File(FilenameUtils.removeExtension(
                    fileChosen.getAbsolutePath()) + filter.getExtension());
          } else {
            return null;
          }
        }
      }
      return fileChosen;
    } else {
      Debug.log(-1, "SikulixFileChooser: action cancelled or did not work");
      return null;
    }
  }

  private void processDialog(int selectionMode, String last_dir, String title, int mode, Object[] filters,
                             Object[] result) {
    JFileChooser fchooser = new JFileChooser();
    File fileChoosen = null;
    FileFilter filterChoosen = null;
    if (!last_dir.isEmpty()) {
      fchooser.setCurrentDirectory(new File(last_dir));
    }
    fchooser.setSelectedFile(null);
    fchooser.setDialogTitle(title);
    String btnApprove = "Select";
    if (fromPopFile) {
      fchooser.setFileSelectionMode(DIRSANDFILES);
      fchooser.setAcceptAllFileFilterUsed(true);
    } else {
      fchooser.setFileSelectionMode(selectionMode);
      if (mode == FileDialog.SAVE) {
        fchooser.setDialogType(JFileChooser.SAVE_DIALOG);
        btnApprove = "Save";
      }
      if (filters.length == 0) {
        fchooser.setAcceptAllFileFilterUsed(true);
      } else {
        fchooser.setAcceptAllFileFilterUsed(false);
        for (Object filter : filters) {
          fchooser.setFileFilter((FileFilter) filter);
        }
      }
    }
    if (Settings.isMac()) {
      fchooser.putClientProperty("JFileChooser.packageIsTraversable", "always");
    }
    int dialogResponse = fchooser.showDialog(parentFrame, btnApprove);
    if (dialogResponse != JFileChooser.APPROVE_OPTION) {
      fileChoosen = null;
    } else {
      fileChoosen = fchooser.getSelectedFile();
    }
    result[0] = fileChoosen;
    if (filters.length > 0) {
      result[1] = fchooser.getFileFilter();
    }
  }

  class SXFilter extends FileFilter {

    public static final String SIKULI = "s";
    public static final String FOLDER = "d";
    public static final String FILE = "f";
    public static final String GENERIC = "g";

    private String type, description, extension;

    public SXFilter(String description, String type) {
      this.type = type;
      if (type != SIKULI && type != FOLDER && type != GENERIC) {
        extension = type;
        this.type = FILE;
      }
      this.description = description;
    }

    @Override
    public boolean accept(File f) {
      return true;
    }

    @Override
    public String getDescription() {
      return description;
    }

    public boolean isType(String type) {
      return this.type.equals(type);
    }

    public String getExtension() {
      return "." + extension;
    }
  }
}
