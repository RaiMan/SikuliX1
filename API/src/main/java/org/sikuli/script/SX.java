/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.script.support.Commons;
import org.sikuli.util.SikulixFileChooser;

import javax.swing.*;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SX {

  static public class Log {
    public static void error(String msg, Object... args) {
      Debug.error("SX: " + msg, args);
    }
  }

  private static Log log = new Log();

  //private static final ScheduledExecutorService TIMEOUT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

  //<editor-fold desc="01 input, popup, popAsk, popError">
  private enum PopType {
    POPUP, POPASK, POPERROR, POPINPUT, POPSELECT, POPFILE, POPGENERIC
  }

  private static boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }

  private static void pause(double time) {
    try {
      Thread.sleep((int) (time * 1000));
    } catch (InterruptedException ex) {
    }
  }

  /**
   * optionally timed popup (self-vanishing)
   *
   * @param args (message, title, preset, hidden = false, timeout = forever)
   * @return
   */
  public static String input(Object... args) {
    if (isHeadless()) {
      log.error("running headless: input");
    } else {
      return (String) doPop(PopType.POPINPUT, args);
    }
    return null;
  }

  /**
   * optionally timed popup (self-vanishing)
   *
   * @param args (message, title, preset, hidden = false, timeout = forever, options-list)
   * @return
   */
  public static String popSelect(Object... args) {
    if (isHeadless()) {
      log.error("running headless: select");
    } else {
      return (String) doPop(PopType.POPSELECT, args);
    }
    return null;
  }

  public static String popFile(Object... args) {
    if (isHeadless()) {
      log.error("running headless: file");
    } else {
      return (String) doPop(PopType.POPFILE, args);
    }
    return null;
  }

  /**
   * optionally timed popup (self-vanishing)
   *
   * @param args (message, title, preset, hidden = false, timeout = forever)
   * @return
   */
  public static Boolean popup(Object... args) {
    if (isHeadless()) {
      log.error("running headless: popup");
    } else {
      return (Boolean) doPop(PopType.POPUP, args);
    }
    return false;
  }

  /**
   * optionally timed popup (self-vanishing)
   *
   * @param args (message, title, preset, hidden = false, timeout = forever)
   * @return
   */
  public static Boolean popAsk(Object... args) {
    if (isHeadless()) {
      log.error("running headless: popAsk");
    } else {
      return (Boolean) doPop(PopType.POPASK, args);
    }
    return false;
  }

  /**
   * optionally timed popup (self-vanishing)
   *
   * @param args (message, title, preset, hidden = false, timeout = forever)
   * @return
   */
  public static Integer popGeneric(Object... args) {
    if (isHeadless()) {
      log.error("running headless: popGeneric");
    } else {
      return (Integer) doPop(PopType.POPGENERIC, args);
    }
    return -1;
  }

  /**
   * optionally timed popup (self-vanishing)
   *
   * @param args (message, title, preset, hidden = false, timeout = forever)
   * @return
   */
  public static Boolean popError(Object... args) {
    if (isHeadless()) {
      log.error("running headless: popError");
    } else {
      return (Boolean) doPop(PopType.POPERROR, args);
    }
    return false;
  }

  private static Object doPop(PopType popType, Object... args) {
    class RunInput implements Runnable {
      PopType popType = PopType.POPUP;
      JFrame frame = null;
      String title = "";
      String message = "";
      String preset = "";
      Boolean hidden = false;
      Integer timeout = 0;
      Object options = null;
      Pattern pattern = null;
      Map<String, Object> parameters = new HashMap<>();
      Object returnValue;

      public RunInput(PopType popType, Object... args) {
        this.popType = popType;
        parameters = getPopParameters(args);
        title = (String) parameters.get("title");
        message = (String) parameters.get("message");
        preset = (String) parameters.get("preset");
        hidden = (Boolean) parameters.get("hidden");
        timeout = (Integer) parameters.get("timeout");
        frame = getFrame(parameters.get("location"));
        options = parameters.get("options");
        pattern = (Pattern) parameters.get("pattern");
      }

      @Override
      public void run() {
        returnValue = null;
        if (PopType.POPUP.equals(popType)) {
          JOptionPane.showMessageDialog(frame, message, title, JOptionPane.PLAIN_MESSAGE);
          returnValue = Boolean.TRUE;
        } else if (PopType.POPASK.equals(popType)) {
          int ret = JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION);
          returnValue = Boolean.TRUE;
          if (ret == JOptionPane.CLOSED_OPTION) {
            returnValue = null;
          } else if (ret == JOptionPane.NO_OPTION) {
            returnValue = Boolean.FALSE;
          }
        } else if (PopType.POPERROR.equals(popType)) {
          JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
          returnValue = Boolean.TRUE;
        } else if (PopType.POPINPUT.equals(popType)) {
          if (!hidden) {
            if ("".equals(title)) {
              title = "Sikuli input request";
            }
            returnValue = JOptionPane.showInputDialog(frame, message, title,
                JOptionPane.PLAIN_MESSAGE, null, null, preset);
          } else {
            JTextArea messageText = new JTextArea(message);
            messageText.setColumns(20);
            messageText.setLineWrap(true);
            messageText.setWrapStyleWord(true);
            messageText.setEditable(false);
            messageText.setBackground(new JLabel().getBackground());
            final JPasswordField passwordField = new JPasswordField(preset);

            frame.addWindowListener(new WindowAdapter() {
              @Override
              public void windowOpened(WindowEvent e) {
                frame.removeWindowListener(this);
                new Thread(() -> {
                  pause(0.3);
                  EventQueue.invokeLater(() -> {
                    passwordField.requestFocusInWindow();
                  });
                }).start();
              }
            });

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(passwordField);
            panel.add(Box.createVerticalStrut(10));
            panel.add(messageText);
            int retval = JOptionPane.showConfirmDialog(frame, panel, title, JOptionPane.OK_CANCEL_OPTION);
            returnValue = "";
            if (0 == retval) {
              char[] pwchar = passwordField.getPassword();
              for (int i = 0; i < pwchar.length; i++) {
                returnValue = (String) returnValue + pwchar[i];
                pwchar[i] = 0;
              }
            }
          }
        } else if (PopType.POPSELECT.equals(popType)) {
          Object[] realOptions = new Object[0];
          List<String> optionList = new ArrayList<>();
          if (options != null) {
            if (options instanceof Object[]) {
              realOptions = (Object[]) options;
            } else if (options instanceof String[]) {
              realOptions = (Object[]) options;
            } else if (options instanceof Collection<?>) {
              realOptions = ((Collection<?>) options).toArray();
            } else if (options instanceof String) {
              String optionString = (String) options;
              int slen;
              while (!optionString.isEmpty()) {
                try {
                  slen = Integer.parseInt(optionString.substring(0, 4));
                } catch (NumberFormatException e) {
                  slen = 0;
                }
                if (slen == 0 || slen < 1000) {
                  realOptions = new Object[0];
                  break;
                }
                slen -= 1000;
                optionList.add(optionString.substring(4, 4 + slen));
                optionString = optionString.substring(slen + 4);
              }
              if (optionList.size() > 0) {
                realOptions = optionList.toArray();
              }
            }
          }
          if (realOptions.length == 0) {
            returnValue = "";
          } else {
            returnValue = JOptionPane.showInputDialog(frame, message, title,
                JOptionPane.PLAIN_MESSAGE, null, realOptions, preset);
          }
        } else if (PopType.POPFILE.equals(popType)) {
          File fileChoosen = new SikulixFileChooser(frame).open(title);
          returnValue = fileChoosen == null ? "" : fileChoosen.getAbsolutePath();
        } else if (PopType.POPGENERIC.equals(popType)) { //TODO allow the other button options
          returnValue = 0;
          if (options instanceof String[]) {
            String[] realOptions = (String[]) options;
            int response = JOptionPane.showOptionDialog(frame, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                realOptions, preset);
            returnValue = response;
          }
        }

        synchronized (this) {
          dispose(); // needs to be here, frame is not always closed properly otherwise
          this.notify();
        }
      }

      public int getTimeout() {
        if (Integer.MAX_VALUE == timeout) {
          return timeout;
        }
        return timeout * 1000;
      }

      public void dispose() {
        if (frame.getSize().width < 3) {
          frame.dispose();
        }
      }

      public Object getReturnValue() {
        return returnValue;
      }
    }

    RunInput popRun = new RunInput(popType, args);

    ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> timeoutJob = timeoutScheduler.schedule((() -> {
      popRun.dispose();
    }), popRun.getTimeout(), TimeUnit.MILLISECONDS);

    if (EventQueue.isDispatchThread()) {
      popRun.run();
    } else {
      synchronized (popRun) {
        EventQueue.invokeLater(popRun);
        try {
          popRun.wait();
        } catch (InterruptedException e) {
          Debug.error("Interrupted while waiting for popup close: %s", e.getMessage());
        }
      }
    }
    Object returnValue = popRun.getReturnValue();
    if (timeoutJob.isDone()) {
      returnValue = null;
    } else {
      timeoutJob.cancel(false);
    }
    timeoutScheduler.shutdown();
    return returnValue;
  }

  private static Map<String, Object> getPopParameters(Object... args) {
    String parameterNames = "message,title,preset,hidden,timeout,location,options,pattern";
    String parameterClass = "s,s,s,b,i,e,o,p";
    Object[] parameterDefault = new Object[]{"not set", "SikuliX", "",
        false, Integer.MAX_VALUE, null, new Object[0], null};
    return Parameters.get(parameterNames, parameterClass, parameterDefault, args);
  }

  private static JFrame getFrame(Object point) {
    Location currentPopLocation = Sikulix.getCurrentPopLocation();
    int x = currentPopLocation.x;
    int y = currentPopLocation.y;
    if (null != point) {
      if (point instanceof Point) {
        x = ((Point) point).x;
        y = ((Point) point).y;
      } else {
        if (point instanceof Region) {
          Region reg = (Region) point;
          if (reg.getName().equals("***SXIDE***")) {
            return Commons.getSXIDE();
          }
          x = reg.getCenter().x;
          y = reg.getCenter().y;
        } else if (point instanceof Location) {
          x = ((Location) point).x;
          y = ((Location) point).y;
        }
      }
    }
    JFrame anchor = new JFrame();
    anchor.setAlwaysOnTop(true);
    anchor.setUndecorated(true);
    anchor.setSize(1, 1);
    anchor.setLocation(x, y);
    anchor.setVisible(true);
    return anchor;
  }

  public static Region on() {
    return Screen.getPrimaryScreen();
  }

  static private class Parameters {

    private Map<String, String> parameterTypes = new HashMap<>();
    private String[] parameterNames = null;
    private Object[] parameterDefaults = new Object[0];

    public Parameters(String theNames, String theClasses, Object[] theDefaults) {
      String[] names = theNames.split(",");
      String[] classes = theClasses.split(",");
      if (names.length == classes.length) {
        for (int n = 0; n < names.length; n++) {
          String clazz = classes[n];
          if (clazz.length() == 1) {
            clazz = clazz.toLowerCase();
            if ("s".equals(clazz)) {
              clazz = "String";
            } else if ("i".equals(clazz)) {
              clazz = "Integer";
            } else if ("d".equals(clazz)) {
              clazz = "Double";
            } else if ("b".equals(clazz)) {
              clazz = "Boolean";
            } else if ("e".equals(clazz)) {
              clazz = "Region";
            } else if ("o".equals(clazz)) {
              clazz = "Object";
            } else if ("p".equals(clazz)) {
              clazz = "Pattern";
            }
          }
          if ("String".equals(clazz) || "Integer".equals(clazz) ||
              "Double".equals(clazz) || "Boolean".equals(clazz) ||
              "Region".equals(clazz) || "Object".equals(clazz) || "Pattern".equals(clazz)) {
            parameterTypes.put(names[n], clazz);
          }
        }
        parameterNames = names;
        parameterDefaults = theDefaults;
      } else {
        log.error("Parameters: different length: names: %s classes: %s", theNames, theClasses);
      }
    }

    public static Map<String, Object>   get(Object... args) {
      String theNames = (String) args[0];
      String theClasses = (String) args[1];
      Object[] theDefaults = (Object[]) args[2];
      Object[] theArgs = (Object[]) args[3];
      Parameters theParameters = new Parameters(theNames, theClasses, theDefaults);
      return theParameters.getParameters(theArgs);
    }

    private Object getParameter(Object possibleValue, String parameterName) {
      String clazz = parameterTypes.get(parameterName);
      Object value = null;
      if ("String".equals(clazz)) {
        if (possibleValue instanceof String) {
          value = possibleValue;
        }
      } else if ("Integer".equals(clazz)) {
        if (possibleValue instanceof Integer) {
          value = possibleValue;
        }
      } else if ("Double".equals(clazz)) {
        if (possibleValue instanceof Double) {
          value = possibleValue;
        }
      } else if ("Boolean".equals(clazz)) {
        if (possibleValue instanceof Boolean) {
          value = possibleValue;
        }
      } else if ("Region".equals(clazz)) {
        if (possibleValue instanceof Region) {
          value = possibleValue;
        }
      } else if ("Object".equals(clazz)) {
        value = possibleValue;
      }
      return value;
    }

    private int findNextParameter(Object possibleValue, int parmIndex) {
      parmIndex++;
      Object value = null;
      while (parmIndex < parameterNames.length) {
        if (null == possibleValue) {
          return parmIndex;
        }
        value = getParameter(possibleValue, parameterNames[parmIndex]);
        if (value == null) {
          parmIndex++;
        } else {
          return parmIndex;
        }
      }
      return -1;
    }

    public Map<String, Object> getParameters(Object[] args) {
      Map<String, Object> params = new HashMap<>();
      if (isNotNull(parameterNames)) {
        int n = 0;
        for (String parameterName : parameterNames) {
          params.put(parameterName, parameterDefaults[n]);
          n++;
        }
        int argParm = -1;
        for (Object arg : args) {
          argParm = findNextParameter(arg, argParm);
          if (argParm < 0) {
            break;
          } else {
            params.put(parameterNames[argParm], arg);
          }
        }
      }
      return params;
    }
  }
  //</editor-fold>

  public static boolean isNotNull(Object obj) {
    return null != obj;
  }

  public static boolean isNull(Object obj) {
    return null == obj;
  }

  //<editor-fold desc="10 Python support">
  public void reset() {
    Debug.log(3, "SX.reset()");
    Screen.resetMonitorsQuiet();
    Mouse.reset();
  }
  //</editor-fold>
}
