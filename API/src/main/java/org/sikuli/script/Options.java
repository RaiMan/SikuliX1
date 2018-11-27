package org.sikuli.script;

import org.sikuli.basics.Debug;

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

  static Options sxOptions = null;
  static RunTime runtime = null;

  private File fOptions = null;
  private String fnOptions = "SikulixOptions.txt";

  private Properties options = null;

  static boolean testing = false;

  static Options init(RunTime.Type typ) {
    if (sxOptions == null) {
      runtime = RunTime.get();
      sxOptions = new Options();
      sxOptions.loadOptions(typ);
    }
    return sxOptions;
  }

  //<editor-fold defaultstate="collapsed" desc="internal options handling">
  void loadOptions(RunTime.Type typ) {
    for (File aFile : new File[]{runtime.fWorkDir, runtime.fUserDir, runtime.fSikulixStore}) {
      log(lvl, "loadOptions: check: %s", aFile);
      fOptions = new File(aFile, fnOptions);
      if (fOptions.exists()) {
        break;
      } else {
        fOptions = null;
      }
    }
    if (fOptions != null) {
      options = new Properties();
      try {
        InputStream is;
        is = new FileInputStream(fOptions);
        options.load(is);
        is.close();
      } catch (Exception ex) {
        log(-1, "while checking Options file:\n%s", fOptions);
        fOptions = null;
        options = null;
      }
      testing = isOption("testing", false);
      if (testing) {
        Debug.setDebugLevel(3);
      }
      log(lvl, "found Options file at: %s", fOptions);
    }
    if (hasOptions()) {
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
      options = new Properties();
      options.setProperty(pName, sDefault);
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
    getOption(pName, sValue);
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
      options = new Properties();
      options.setProperty(pName, "" + nDefault);
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
    getOptionInteger(pName, nValue);
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
      options = new Properties();
      options.setProperty(pName, "" + nDefault);
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
    getOptionFloat(pName, nValue);
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
      options = new Properties();
      options.setProperty(pName, "" + nDefault);
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
    getOptionDouble(pName, nValue);
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
      options = new Properties();
      options.setProperty(pName, bDefault ? "true" : "false");
      return bDefault;
    }
    String pVal = options.getProperty(pName, bDefault ? "true" : "false").toLowerCase();
    if (pVal.isEmpty()) {
      return bDefault;
    } else if (pVal.contains("yes") || pVal.contains("true") || pVal.contains("on")) {
      return true;
    } else if (pVal.contains("no") || pVal.contains("false") || pVal.contains("off")) {
      return false;
    }
    return true;
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
      logp("*** options dump:\n%s", (fOptions == null ? "" : fOptions));
      for (String sOpt : getOptions().keySet()) {
        logp("%s = %s", sOpt, getOption(sOpt));
      }
      logp("*** options dump end");
    }
  }
  //</editor-fold>

  //<editor-fold desc="user public options support">

  private static String optThisComingFromFile = "thisOptions.comingFromWhatFile";
  private static String optThisWhatIsANumber = "thisOptions.whatIsAnumber";
  private static String whatIsANumber = "#";

  private static boolean optIsNumber(Properties props, String pName) {
    String prefix = getOpt(props, pName, whatIsANumber);
    if (pName.contains(prefix)) {
      return true;
    }
    return false;
  }

  /**
   * load a properties file
   *
   * @param fpOptions path to a file containing options
   * @return the Properties store or null
   */
  public Properties loadOpts(String fpOptions) {
    if (fpOptions == null) {
      log(-1, "loadOptions: (error: no file)");
      return null;
    }
    File fOptions = new File(fpOptions);
    if (!fOptions.isFile()) {
      log(-1, "loadOptions: (error: not found) %s", fOptions);
      return null;
    }
    Properties pOptions = new Properties();
    try {
      fpOptions = fOptions.getCanonicalPath();
      InputStream is;
      is = new FileInputStream(fOptions);
      pOptions.load(is);
      is.close();
    } catch (Exception ex) {
      log(-1, "loadOptions: %s (error %s)", fOptions, ex.getMessage());
      return null;
    }
    log(lvl, "loadOptions: ok (%d): %s", pOptions.size(), fOptions.getName());
    pOptions.setProperty(optThisComingFromFile, fpOptions);
    return pOptions;
  }

  public static Properties makeOpts() {
    return new Properties();
  }

  /**
   * save a properties store to a file (prop: this.comingfrom = abs. filepath)
   *
   * @param pOptions the prop store
   * @return success
   */
  public boolean saveOpts(Properties pOptions) {
    String fpOptions = pOptions.getProperty(optThisComingFromFile);
    if (null == fpOptions) {
      log(-1, "saveOptions: no prop %s", optThisComingFromFile);
      return false;
    }
    return saveOpts(pOptions, fpOptions);
  }

  /**
   * save a properties store to the given file
   *
   * @param pOptions  the prop store
   * @param fpOptions path to a file
   * @return success
   */
  public boolean saveOpts(Properties pOptions, String fpOptions) {
    pOptions.remove(optThisComingFromFile);
    File fOptions = new File(fpOptions);
    try {
      fpOptions = fOptions.getCanonicalPath();
      OutputStream os;
      os = new FileOutputStream(fOptions);
      pOptions.store(os, "");
      os.close();
    } catch (Exception ex) {
      log(-1, "saveOptions: %s (error %s)", fOptions, ex.getMessage());
      return false;
    }
    log(lvl, "saveOptions: saved: %s", fpOptions);
    return true;
  }

  public static boolean hasOpt(Properties props, String pName) {
    return null != props && null != props.getProperty(pName);
  }

  public static String getOpt(Properties props, String pName) {
    return getOpt(props, pName, "");
  }

  public static String getOpt(Properties props, String pName, String deflt) {
    String retVal = deflt;
    if (hasOpt(props, pName)) {
      retVal = props.getProperty(pName);
    }
    return retVal;
  }

  public static String setOpt(Properties props, String pName, String pVal) {
    String retVal = "";
    if (hasOpt(props, pName)) {
      retVal = props.getProperty(pName);
    }
    props.setProperty(pName, pVal);
    return retVal;
  }

  public static double getOptNum(Properties props, String pName) {
    return getOptNum(props, pName, 0d);
  }

  public static double getOptNum(Properties props, String pName, double deflt) {
    double retVal = deflt;
    if (hasOpt(props, pName)) {
      try {
        retVal = Double.parseDouble(props.getProperty(pName));
      } catch (Exception ex) {
      }
    }
    return retVal;
  }

  public static double setOptNum(Properties props, String pName, double pVal) {
    double retVal = 0d;
    if (hasOpt(props, pName)) {
      try {
        retVal = Double.parseDouble(props.getProperty(pName));
      } catch (Exception ex) {
      }
    }
    props.setProperty(pName, ((Double) pVal).toString());
    return retVal;
  }

  public static String delOpt(Properties props, String pName) {
    String retVal = "";
    if (hasOpt(props, pName)) {
      retVal = props.getProperty(pName);
    }
    props.remove(pName);
    return retVal;
  }

  public static Map<String, String> getOpts(Properties props) {
    Map<String, String> mapOptions = new HashMap<String, String>();
    if (props != null) {
      Enumeration<?> optionNames = props.propertyNames();
      String optionName;
      while (optionNames.hasMoreElements()) {
        optionName = (String) optionNames.nextElement();
        mapOptions.put(optionName, props.getProperty(optionName));
      }
    }
    return mapOptions;
  }

  public static int setOpts(Properties props, Map<String, String> aMap) {
    int n = 0;
    for (String key : aMap.keySet()) {
      props.setProperty(key, aMap.get(key));
      n++;
    }
    return n;
  }

  public static boolean delOpts(Properties props) {
    if (null != props) {
      props.clear();
      return true;
    }
    return false;
  }

  public static int hasOpts(Properties props) {
    if (null != props) {
      return props.size();
    }
    return 0;
  }
  //</editor-fold>
}
