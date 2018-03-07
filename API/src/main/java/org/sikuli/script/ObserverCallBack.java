/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.EventListener;

import org.sikuli.basics.Debug;
import org.sikuli.util.JLangHelperInterface;
import org.sikuli.util.JRubyHelper;
import org.sikuli.util.JythonHelper;

/**
 * Use this class to implement call back methods for the Region observers
 * onAppear, onVanish and onChange. <br>
 * by overriding the respective method appeared, vanished or changed
 * <pre>
 * example:
 * aRegion.onAppear(anImageOrPattern,
 *   new ObserverCallBack() {
 *     appeared(ObserveEvent e) {
 *       // do something
 *     }
 *   }
 * );
 * </pre>
 * when the image appears, your above call back appeared() will be called<br>
 * see {@link ObserveEvent} about the features available in the callback function
 */
public class ObserverCallBack implements EventListener {

  private Object callback = null;
  private ObserveEvent.Type obsType = ObserveEvent.Type.GENERIC;
  private JLangHelperInterface scriptHelper = null;
  private String scriptRunnerType = null;

  public ObserverCallBack() {
  }

  public ObserverCallBack(Object callback, ObserveEvent.Type obsType) {
    this.callback = callback;
    this.obsType = obsType;
    if (callback.getClass().getName().contains("org.python")) {
      scriptRunnerType = "jython";
      scriptHelper = JythonHelper.get();
    } else if (callback.getClass().getName().contains("org.jruby")) {
      scriptRunnerType = "jruby";
      scriptHelper = JRubyHelper.get();
    } else {
      Debug.error("ObserverCallBack: %s init: ScriptRunner not available for class %s", obsType,
              callback.getClass().getName());
    }
  }

  public ObserveEvent.Type getType() {
    return obsType;
  }

  public void appeared(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.APPEAR.equals(obsType)) {
      run(e);
    }
  }

  public void vanished(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.VANISH.equals(obsType)) {
      run(e);
    }
  }

  public void changed(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.CHANGE.equals(obsType)) {
      run(e);
    }
  }

  public void happened(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.GENERIC.equals(obsType)) {
      run(e);
    }
  }

  public void findfailed(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.FINDFAILED.equals(obsType)) {
      run(e);
    }
  }

  public void missing(ObserveEvent e) {
    if (scriptHelper != null && ObserveEvent.Type.MISSING.equals(obsType)) {
      run(e);
    }
  }

  private void run(ObserveEvent e) {
    boolean success = true;
    Object[] args = new Object[]{callback, e};
    if (scriptHelper != null) {
      success = scriptHelper.runObserveCallback(args);
      if (!success) {
        Debug.error("ObserverCallBack: problem with scripting handler: %s\n%s",
                scriptHelper.getClass().getName(),
                callback.getClass().getName());
      }
    }
  }

  public void setType(ObserveEvent.Type givenType) {
    obsType = givenType;
  }
}
