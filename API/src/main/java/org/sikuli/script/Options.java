/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.script.support.RunTime;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Options {

  int lvl = 3;

  private void log(int level, String message, Object... args) {
    Debug.logx(level, "Options: " + message, args);
  }

  private void logp(String message, Object... args) {
    log(-3, message, args);
  }

  private Options() {
  }

  public Options(String fpOptions) {
    loadOptions(fpOptions);
  }

  public static String getOptionsFileDefault() {
    String defaultContent = "# key = value";
    return defaultContent;
  }

  static Options sxOptions = null;
  static RunTime runtime = null;

  private String fnSXOptions = "SikulixOptions.txt";
  private String propOptionsFile = "OptionsFile";

  private Properties options = null;

  static boolean testing = false;

  public String getOptionsFile() {
    return getOption(propOptionsFile);
  }

  public static Options init(RunTime pRunTime) {
    runtime = pRunTime;
    return init();
  }

  private static Options init() {
    if (sxOptions == null) {
      sxOptions = new Options();
      sxOptions.loadOptions();
    }
    return sxOptions;
  }

  void loadOptions() {
/*
    // public Settings::fields as options
    Field[] fields = Settings.class.getFields();
    Object value = null;
    for (Field field : fields) {
      try {
        Field theField = Settings.class.getField(field.getName());
        value = theField.get(null);
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      p("%s (%s) %s", field.getName(), field.getType(), value);
    }
*/
    loadOptions(fnSXOptions);
    if (hasOptions()) {
      testing = isOption("testing", false);
      if (testing) {
        Debug.setDebugLevel(3);
      }
      for (Object oKey : options.keySet()) {
        String sKey = (String) oKey;
        String[] parts = sKey.split("\\.");
        if (parts.length == 1) {
          continue;
        }
        String sClass = parts[0];
        String sAttr = parts[1];
        Class cClass;
        Field cField;
        Class ccField;
        if (sClass.contains("Settings")) {
          try {
            cClass = Class.forName("org.sikuli.basics.Settings");
            cField = cClass.getField(sAttr);
            ccField = cField.getType();
            if (ccField.getName() == "boolean") {
              cField.setBoolean(null, isOption(sKey));
            } else if (ccField.getName() == "int") {
              cField.setInt(null, getOptionInteger(sKey));
            } else if (ccField.getName() == "float") {
              cField.setFloat(null, getOptionFloat(sKey));
            } else if (ccField.getName() == "double") {
              cField.setDouble(null, getOptionDouble(sKey));
            } else if (ccField.getName() == "String") {
              cField.set(null, getOption(sKey));
            }
          } catch (Exception ex) {
            log(-1, "loadOptions: not possible: %s = %s", sKey, options.getProperty(sKey));
          }
        }
      }
    }
  }

  void loadOptions(String fpOptions) {
    File fOptions = new File(fpOptions);
    if (fOptions.isAbsolute()) {
      if (!fOptions.exists()) {
        fOptions = null;
        log(-1, "loadOptions: not exists: %s", fOptions);
      }
    } else {
      for (File aFile : new File[]{runtime.fUserDir, runtime.fWorkDir, runtime.fSikulixStore}) {
        fOptions = new File(aFile, fpOptions);
        if (fOptions.exists()) {
          break;
        } else {
          fOptions = null;
        }
      }
    }
    options = new Properties();
    if (fOptions != null) {
      try {
        InputStream is;
        is = new FileInputStream(fOptions);
        options.load(is);
        is.close();
        log(lvl, "loadOptions: Options file: %s", fOptions);
        setOption(propOptionsFile, fOptions.getAbsolutePath());
      } catch (Exception ex) {
        log(-1, "loadOptions: %s: %s", fOptions, ex.getMessage());
        options = null;
      }
    } else {
      setOption(propOptionsFile, new File(runtime.fSikulixStore, fpOptions).getAbsolutePath());
    }
  }

  /**
   * save a properties store to a file (prop: this.comingfrom = abs. filepath)
   *
   * @return success
   */
  public boolean saveOptions() {
    String fpOptions = getOption(propOptionsFile, null);
    if (null == fpOptions) {
      log(-1, "saveOptions: no prop %s", propOptionsFile);
      return false;
    }
    return saveOpts(fpOptions);
  }

  /**
   * save a properties store to the given file
   *
   * @param fpOptions path to a file
   * @return success
   */
  public boolean saveOpts(String fpOptions) {
    File fOptions = new File(fpOptions);
    if (!fOptions.isAbsolute()) {
      fOptions = new File(runtime.fWorkDir, fpOptions);
    }
    try {
      setOption(propOptionsFile, fOptions.getAbsolutePath());
      OutputStream os;
      os = new FileOutputStream(fOptions);
      options.store(os, "");
      os.close();
    } catch (Exception ex) {
      log(-1, "saveOptions: %s (error %s)", fOptions, ex.getMessage());
      return false;
    }
    log(lvl, "saveOptions: saved: %s", fpOptions);
    return true;
  }

  /**
   * if no option file is found, the option is taken as not existing<br>
   * side-effect: if no options file is there, an options store will be created in memory<br>
   * in this case and when the option is absent or empty, the given default will be stored<br>
   * you might later save the options store to a file with saveOptions()<br>
   * the default value is either the empty string, number 0 or false
   *
   * @param pName    the option key (case-sensitive)
   * @param sDefault the default to be returned if option absent or empty
   * @return the associated value, the default value if absent or empty
   */
  public String getOption(String pName, String sDefault) {
    if (options == null) {
      return sDefault;
    }
    String pVal = options.getProperty(pName, sDefault);
    if (pVal.isEmpty()) {
      options.setProperty(pName, sDefault);
      return sDefault;
    }
    return pVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the associated value, empty string if absent
   */
  public String getOption(String pName) {
    return getOption(pName, "");
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param sValue the value to be set
   */
  public void setOption(String pName, String sValue) {
    init();
    options.setProperty(pName, sValue);
  }

  /**
   * {link getOption}
   *
   * @param pName    the option key (case-sensitive)
   * @param nDefault the default to be returned if option absent, empty or not convertible
   * @return the converted integer number, default if absent, empty or not possible
   */
  public int getOptionInteger(String pName, Integer nDefault) {
    if (options == null) {
      return nDefault;
    }
    String pVal = options.getProperty(pName, nDefault.toString());
    int nVal = nDefault;
    try {
      nVal = Integer.decode(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted integer number, 0 if absent or not possible
   */
  public int getOptionInteger(String pName) {
    return getOptionInteger(pName, 0);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param nValue the value to be set
   */
  public void setOptionInteger(String pName, int nValue) {
    init();
    options.setProperty(pName, "" + nValue);
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted float number, default if absent or not possible
   */
  public float getOptionFloat(String pName, float nDefault) {
    if (options == null) {
      return nDefault;
    }
    String pVal = options.getProperty(pName, "0");
    float nVal = nDefault;
    try {
      nVal = Float.parseFloat(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted float number, 0 if absent or not possible
   */
  public float getOptionFloat(String pName) {
    return getOptionFloat(pName, 0);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param nValue the value to be set
   */
  public void setOptionFloat(String pName, float nValue) {
    init();
    options.setProperty(pName, "" + nValue);
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted float number, default if absent or not possible
   */
  public double getOptionDouble(String pName, double nDefault) {
    if (options == null) {
      return nDefault;
    }
    String pVal = options.getProperty(pName, "0");
    double nVal = nDefault;
    try {
      nVal = Double.parseDouble(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return the converted double number, 0 if absent or not possible
   */
  public double getOptionDouble(String pName) {
    return getOptionDouble(pName, 0);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param nValue the value to be set
   */
  public void setOptionDouble(String pName, double nValue) {
    init();
    options.setProperty(pName, "" + nValue);
  }

  /**
   * {link getOption}
   *
   * @param pName    the option key (case-sensitive)
   * @param bDefault the default to be returned if option absent or empty
   * @return true if option has yes or no, false for no or false (not case-sensitive)
   */
  public boolean isOption(String pName, boolean bDefault) {
    if (options == null) {
      return bDefault;
    }
    String pVal = options.getProperty(pName, bDefault ? "true" : "false").toLowerCase();
    if (pVal.isEmpty()) {
      return bDefault;
    } else if (pVal.contains("yes") || pVal.contains("true") || pVal.contains("on")) {
      return true;
    }
    return false;
  }

  /**
   * {link getOption}
   *
   * @param pName the option key (case-sensitive)
   * @return true only if option exists and has yes or true (not case-sensitive), in all other cases false
   */
  public boolean isOption(String pName) {
    return isOption(pName, false);
  }

  /**
   * {link getOption}
   *
   * @param pName  the option key (case-sensitive)
   * @param bValue the value to be set
   */
  public void setOptionBool(String pName, boolean bValue) {
    init();
    options.setProperty(pName, isOption(pName, bValue) ? "true" : "false");
  }

  /**
   * check whether options are defined
   *
   * @return true if at lest one option defined else false
   */
  public boolean hasOptions() {
    return options != null && options.size() > 0;
  }

  /**
   * all options and their values
   *
   * @return a map of key-value pairs containing the found options, empty if no options file found
   */
  public Map<String, String> getOptions() {
    Map<String, String> mapOptions = new HashMap<String, String>();
    if (options != null) {
      Enumeration<?> optionNames = options.propertyNames();
      String optionName;
      while (optionNames.hasMoreElements()) {
        optionName = (String) optionNames.nextElement();
        mapOptions.put(optionName, getOption(optionName));
      }
    }
    return mapOptions;
  }

  /**
   * all options and their values written to sysout as key = value
   */
  public void dumpOptions() {
    if (hasOptions()) {
      Map<String, String> mapOptions = getOptions();
      logp("*** options dump");
      for (String sOpt : mapOptions.keySet()) {
        logp("%s = %s", sOpt, mapOptions.get(sOpt));
      }
      logp("*** options dump end");
    }
  }
}
