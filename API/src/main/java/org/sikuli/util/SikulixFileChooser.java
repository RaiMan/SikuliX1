/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
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

import org.sikuli.basics.Debug;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.RunTime;

public class SikulixFileChooser {
  static final int FILES = JFileChooser.FILES_ONLY;
  static final int DIRS = JFileChooser.DIRECTORIES_ONLY;
  static final int DIRSANDFILES = JFileChooser.FILES_AND_DIRECTORIES;
  static final int SAVE = FileDialog.SAVE;
  static final int LOAD = FileDialog.LOAD;
  Frame _parent;
  boolean accessingAsFile = false;
  boolean loadingImage = false;
  String theLastDir = PreferencesUser.get().get("LAST_OPEN_DIR", "");

  public SikulixFileChooser(Frame parent) {
    _parent = parent;
  }

  public SikulixFileChooser(Frame parent, boolean accessingAsFile) {
    _parent = parent;
    this.accessingAsFile = accessingAsFile;
  }

//  private boolean isPython = false;
//
//  public void setPython() {
//    isPython = true;
//  }
//
//  private boolean isText = false;
//
//  public void setText() {
//    isText = true;
//  }

  private boolean isUntitled = false;

  public void setUntitled() {
    isUntitled = true;
  }

  private boolean isGeneric() {
    if (_parent == null) {
      return false;
    }
    return (_parent.getWidth() < 3 && _parent.getHeight() < 3);
  }

  public File show(String title) {
    File ret = show(title, LOAD, DIRSANDFILES);
    return ret;
  }

  public File load() {
    String title = "Open a file or folder";
    File ret;
    ret = show(title, LOAD, DIRSANDFILES);
    return ret;
  }

  public File save() {
    File ret;
    File selectedFile;
//    if (isUntitled) {
//      if (isText) {
//        selectedFile = show("Save a Text File", SAVE, DIRSANDFILES, anyFilterS);
//      } else {
//        String lastUsedFilter = PreferencesUser.get().get("LAST_USED_FILTER", "");
//        boolean pythonOnly = RunTime.get().options().isOption("ide.pythononly", false);
//        String title = "Save as Sikuli or Python Script";
//        if (pythonOnly) {
//          title = "Save as Python Script";
//          selectedFile = show(title, SAVE, DIRSANDFILES, pythonFilterS);
//        } else {
//          if ("op".equals(lastUsedFilter) || "sp".equals(lastUsedFilter)) {
//            selectedFile = show(title, SAVE, DIRSANDFILES, pythonFilterS, sikuliFilterS);
//          } else {
//            selectedFile = show(title, SAVE, DIRSANDFILES, sikuliFilterS, pythonFilterS);
//          }
//        }
//      }
//      ret = selectedFile;
//    } else if (isPython) {
//      selectedFile = show("Save a Python script", SAVE, FILES, pythonFilterS);
//      ret = selectedFile;
//    } else if (isText) {
//      selectedFile = show("Save a Text File", SAVE, FILES, anyFilterS);
//      ret = selectedFile;
//    } else {
//      selectedFile = show("Save a Sikuli Script", SAVE, DIRSANDFILES, sikuliFilterS);
//      ret = selectedFile;
//    }
    selectedFile = show("Save a File or Folder", SAVE, DIRSANDFILES);
    return selectedFile;
  }

  public File export() {
    String type = "Save packed as .skl or .zip";
    String title = "Export packed as .skl or .zip";
    File ret = show(title, SAVE, FILES);
    return ret;
  }

  public File loadImage() {
    loadingImage = true;
    File ret = show("Load Image File", LOAD, FILES,
        new FileNameExtensionFilter("Image files (jpg, png)", "jpg", "jpeg", "png"));
    return ret;
  }

  private File show(final String title, final int mode, final int theSelectionMode, Object... filters) {
    Debug.log(3, "SikulixFileChooser: %s at %s", title.split(" ")[0], theLastDir);
    File fileChosen;
    final Object[] genericFilters = filters;
    final Object[] result = new Object[]{null, null};
    while (true) {
      boolean tryAgain = false;
      if (isGeneric()) {
        try {
          EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
              processDialog(theSelectionMode, theLastDir, title, mode, genericFilters, result);
            }
          });
        } catch (Exception e) {
        }
      } else {
        processDialog(theSelectionMode, theLastDir, title, mode, filters, result);
      }
      if (null != result[0]) {
        fileChosen = (File) result[0];
        String fileChosenPath = fileChosen.getAbsolutePath();
        if (fileChosenPath.contains("###Error")) {
          tryAgain = true;
          fileChosen = new File(fileChosenPath.split("###")[0]);
        }
        theLastDir = fileChosen.getParent();
        if (tryAgain) {
          continue;
        }
        PreferencesUser.get().put("LAST_OPEN_DIR", theLastDir);
        return fileChosen;
      } else {
        Debug.log(-1, "SikulixFileChooser: action cancelled or did not work");
        return null;
      }
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
    boolean shouldTraverse = false;
    String btnApprove = "Select";
    if (isGeneric()) {
      fchooser.setFileSelectionMode(DIRSANDFILES);
      fchooser.setAcceptAllFileFilterUsed(true);
      shouldTraverse = true;
    } else {
      if (Settings.isMac() && Settings.isJava7() && selectionMode == DIRS) {
        selectionMode = DIRSANDFILES;
      }
      fchooser.setFileSelectionMode(selectionMode);
      if (mode == FileDialog.SAVE) {
        fchooser.setDialogType(JFileChooser.SAVE_DIALOG);
        btnApprove = "Save";
      }
      if (filters.length == 0) {
        fchooser.setAcceptAllFileFilterUsed(true);
        shouldTraverse = true;
      } else {
        fchooser.setAcceptAllFileFilterUsed(false);
        for (Object filter : filters) {
          fchooser.setFileFilter((FileNameExtensionFilter) filter);
          shouldTraverse = true;
        }
      }
    }
    if (shouldTraverse && Settings.isMac()) {
      fchooser.putClientProperty("JFileChooser.packageIsTraversable", "always");
    }
    int dialogResponse = fchooser.showDialog(_parent, btnApprove);
    if (dialogResponse != JFileChooser.APPROVE_OPTION) {
      fileChoosen = null;
    } else {
      fileChoosen = fchooser.getSelectedFile();
    }
    if (null != fileChoosen) {
//      filterChoosen = fchooser.getFileFilter();
//      if (filterChoosen instanceof SikulixFileFilter) {
//        fileChoosen = new File(((SikulixFileFilter) filterChoosen).validateFile(fileChoosen));
//      }
    }
    result[0] = fileChoosen;
    result[1] = filterChoosen;
  }

//  class SikulixFileFilter extends FileFilter {
//
//    private String _type, _desc;
//
//    public SikulixFileFilter(String desc, String type) {
//      _type = type;
//      _desc = desc;
//    }
//
//    public String validateFile(File selectedFile) {
//      String validatedFile = selectedFile.getAbsolutePath();
//      String errorTag = "###";
//      String error = errorTag + "Error: notPossible";
//      String isTextFile = errorTag + "textFile";
//      if (_type == "sp") {
//        if (!selectedFile.getName().endsWith(".py")) {
//          if (selectedFile.isDirectory()) {
//            validatedFile = selectedFile.getAbsolutePath() + error;
//          } else {
//            validatedFile = selectedFile.getAbsolutePath() + ".py";
//          }
//        }
//      } else if (_type == "s") {
//        if (!selectedFile.getName().endsWith(".sikuli")) {
//          if (selectedFile.isDirectory()) {
//            validatedFile = selectedFile.getAbsolutePath() + error;
//          } else {
//            validatedFile = selectedFile.getAbsolutePath() + ".sikuli";
//          }
//        }
//      } else if (_type == "o") {
//        if (!selectedFile.getName().endsWith(".sikuli") || !selectedFile.exists()) {
//          validatedFile = selectedFile.getAbsolutePath() + error;
//        }
//      } else if (_type == "oa") {
//        if (selectedFile.isDirectory() || !selectedFile.exists()) {
//          validatedFile = selectedFile.getAbsolutePath() + error;
//        } else {
//          validatedFile = selectedFile.getAbsolutePath() + error;
//        }
//      } else if (_type == "op") {
//        if (!selectedFile.getName().endsWith(".py") || selectedFile.isDirectory() || !selectedFile.exists()) {
//          validatedFile = selectedFile.getAbsolutePath() + error;
//        }
//      } else if (_type == "e") {
////        if (!selectedFile.getName().endsWith(".skl")) {
////          validatedFile = selectedFile.getAbsolutePath() + ".skl";
////        }
//      }
//      if (validatedFile.contains(errorTag)) {
//        Debug.log(3, "SikulixFileChooser: error: (%s) %s", _type, validatedFile);
//      }
//      return validatedFile;
//    }
//
////    private boolean isTextFile(File file) {
////      String name = file.getName();
////      String[] nameStrings = name.split("\\.");
////      if (nameStrings.length > 1) {
////        String textfiles = RunTime.get().options().getOption("ide.textfiles", "txt,");
////        if (!textfiles.endsWith(",")) {
////          textfiles += ",";
////        }
////        textfiles = textfiles.replace(" ", "");
////        String nameEnding = nameStrings[nameStrings.length - 1] + ",";
////        if (textfiles.contains(nameEnding)) {
////          return true;
////        }
////      } else {
////        if (!file.isDirectory()) {
////          return true;
////        }
////      }
////      return false;
////    }
//
//    @Override
//    public boolean accept(File f) {
//      if ("o".equals(_type) && (isExt(f.getName(), "sikuli") || isExt(f.getName(), "skl"))) {
//        return true;
//      }
//      if ("op".equals(_type) && (isExt(f.getName(), "py"))) {
//        return true;
//      }
//      if ("oa".equals(_type) && f.isFile()) {
//        return true;
//      }
//      if ("s".equals(_type) && isExt(f.getName(), "sikuli")) {
//        return true;
//      }
//      if ("sp".equals(_type) && isExt(f.getName(), "py")) {
//        return true;
//      }
//      if ("sa".equals(_type) && f.isFile()) {
//        return true;
//      }
//      if ("e".equals(_type)) {
//        if (isExt(f.getName(), "skl")) {
//          return true;
//        }
//        if (Settings.isMac() && isExt(f.getName(), "sikuli")) {
//          return false;
//        }
//      }
//      if (!Settings.isMac() && f.isDirectory()) {
//        return true;
//      }
//      return false;
//    }
//
//    private boolean isExt(String fName, String givenExt) {
//      int i = fName.lastIndexOf('.');
//      if (i > 0) {
//        if (fName.substring(i + 1).toLowerCase().equals(givenExt)) {
//          return true;
//        }
//      }
//      return false;
//    }
//
//    @Override
//    public String getDescription() {
//      return _desc;
//    }
//  }
}
