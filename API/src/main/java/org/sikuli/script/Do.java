/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements handy features from version 2
 */
public class Do {

  private static Log log = new Log();

  //<editor-fold desc="popup, popAsk, popError, input">
  private enum PopType {
    POPUP, POPASK, POPERROR, POPINPUT
  }

  public static String input(Object... args) {
    if (SX.isHeadless()) {
      log.error("running headless: input");
    } else {
      return (String) doPop(PopType.POPINPUT, args);
    }
    return null;
  }

  public static Boolean popup(Object... args) {
    if (SX.isHeadless()) {
      log.error("running headless: popup");
    } else {
      return (Boolean) doPop(PopType.POPUP, args);
    }
    return false;
  }

  public static Boolean popAsk(Object... args) {
    if (SX.isHeadless()) {
      log.error("running headless: popAsk");
    } else {
      return (Boolean) doPop(PopType.POPASK, args);
    }
    return false;
  }

  public static Boolean popError(Object... args) {
    if (SX.isHeadless()) {
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
      boolean running = true;
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
      }

      @Override
      public void run() {
        returnValue = null;
        if (PopType.POPUP.equals(popType)) {
          JOptionPane.showMessageDialog(frame, message, title, JOptionPane.PLAIN_MESSAGE);
          returnValue = new Boolean(true);
        } else if (PopType.POPASK.equals(popType)) {
          int ret = JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION);
          returnValue = new Boolean(true);
          if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.NO_OPTION) {
            returnValue = new Boolean(false);
          }
        } else if (PopType.POPERROR.equals(popType)) {
          JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
          returnValue = new Boolean(true);
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
            toGetFocus = passwordField;
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
        }
        stop();
      }

      JComponent toGetFocus = null;

      public void setFocus() {
        if (SX.isNotNull(toGetFocus)) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              toGetFocus.requestFocusInWindow();
            }
          });
        }
      }

      public boolean isRunning() {
        return running;
      }

      public void stop() {
        frame.dispose();
        running = false;
      }

      public int getTimeout() {
        if (Integer.MAX_VALUE == timeout) {
          return timeout;
        }
        return timeout * 1000;
      }

      public Object getReturnValue() {
        return returnValue;
      }
    }
    RunInput popRun = new RunInput(popType, args);
    new Thread(popRun).start();
    SX.pause(0.3);
    popRun.setFocus();
    long end = new Date().getTime() + popRun.getTimeout();
    while (popRun.isRunning()) {
      SX.pause(0.3);
      if (end < new Date().getTime()) {
        popRun.stop();
      }
    }
    return popRun.getReturnValue();
  }

  private static Map<String, Object> getPopParameters(Object... args) {
    String parameterNames = "message,title,preset,hidden,timeout,location";
    String parameterClass = "s,s,s,b,i,e";
    Object[] parameterDefault = new Object[]{"not set", "SikuliX", "", false, Integer.MAX_VALUE, Do.on()};
    return Parameters.get(parameterNames, parameterClass, parameterDefault, args);
  }

  private static JFrame getFrame(Object point) {
    int x;
    int y;
    if (point instanceof Point) {
      x = ((Point) point).x;
      y = ((Point) point).y;
    } else {
      if (SX.isVersion1()) {
        x = ((Region) point).getCenter().x;
        y = ((Region) point).getCenter().y;
      } else {
        x = ((Element) point).getCenter().x;
        y = ((Element) point).getCenter().y;
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

  private static Region on() {
    return Screen.getPrimaryScreen();
  }

  static class SX {
    public static boolean isNotNull(Object obj) {
      return null != obj;
    }

    public static void pause(double time) {
      try {
        Thread.sleep((int) (time * 1000));
      } catch (InterruptedException ex) {
      }
    }

    public static boolean isHeadless() {
      return GraphicsEnvironment.isHeadless();
    }

    public static  boolean isVersion1() { return true; };
  }

  static class Log {
    public static void error(String msg, Object... args) {
      Debug.error("Do: " + msg, args);
    }
  }

  static class Element extends Region {}

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
              if (SX.isVersion1()) {
                clazz = "Region";
              }
              clazz = "Element";
            }
          }
          if ("String".equals(clazz) || "Integer".equals(clazz) ||
                  "Double".equals(clazz) || "Boolean".equals(clazz) ||
                  "Element".equals(clazz) || "Region".equals(clazz)) {
            parameterTypes.put(names[n], clazz);
          }
        }
        parameterNames = names;
        parameterDefaults = theDefaults;
      } else {
        log.error("Parameters: different length: names: %s classes: %s", theNames, theClasses);
      }
    }

    public static Map<String, Object> get(Object... args) {
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
      } else if ("Element".equals(clazz)) {
        if (SX.isVersion1()) {
          if (possibleValue instanceof Region) {
            value = possibleValue;
          }
        } else if (possibleValue instanceof Element) {
          value = possibleValue;
        }
      }
      return value;
    }

    public Map<String, Object> getParameters(Object[] args) {
      Map<String, Object> params = new HashMap<>();
      if (SX.isNotNull(parameterNames)) {
        int n = 0;
        int argsn = 0;
        for (String parameterName : parameterNames) {
          params.put(parameterName, parameterDefaults[n]);
          if (args.length > 0 && argsn < args.length) {
            Object arg = getParameter(args[argsn], parameterName);
            if (SX.isNotNull(arg)) {
              params.put(parameterName, arg);
              argsn++;
            }
          }
          n++;
        }
      }
      return params;
    }
  }

//</editor-fold>
}
