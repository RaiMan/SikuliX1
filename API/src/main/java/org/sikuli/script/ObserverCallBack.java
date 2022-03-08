/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.util.EventListener;

import org.sikuli.basics.Debug;
import org.sikuli.script.support.Commons;

/**
 * Use this class to implement callbacks for<br>
 * the Region observers onAppear, onVanish and onChange. <br>
 * by overriding the respective methods {@link #appeared(ObserveEvent)}, {@link #vanished(ObserveEvent)}, {@link #changed(ObserveEvent)} <br>
 * see: {@link Region#onAppear(Object, Object)}, {@link Region#onVanish(Object, Object)}, {@link Region#onChange(Object)}}<br>
 * and the handling of FindFailed and ImageMissing by overriding {@link #findfailed(ObserveEvent)} or {@link #missing(ObserveEvent)}<br>
 * see: {@link Region#setFindFailedHandler(Object)}, {@link Region#setImageMissingHandler(Object)}
 * <pre>
 * // example (called when image appears):
 * aRegion.onAppear(anImageOrPattern,
 *   new ObserverCallBack() {
 *     appeared(ObserveEvent e) {
 *       // do something
 *     }
 *   }
 * );
 * </pre>
 * see {@link ObserveEvent} about the features available in the callback function
 */
public class ObserverCallBack implements EventListener {

  private Object callback = null;
  private String scriptRunnerType = null;

  /**
   * setup a callback to be used on the Java API level
   <pre>
   * // example (called when image appears):
   * aRegion.onAppear(anImageOrPattern,
   *   new ObserverCallBack() {
   *     appeared(ObserveEvent e) {
   *       // do something
   *     }
   *   }
   * );
   * </pre>
   *    */
  public ObserverCallBack() {
  }


  /**
   * INTERNAL: callbacks from Jython or JRuby
   * @param callback funtion to call
   * @param obsType observer type
   */
  public ObserverCallBack(Object callback, ObserveEvent.Type obsType) {
    this.callback = callback;
    this.obsType = obsType;
    scriptRunnerType = callback.getClass().getName();
  }

  /**
   * INTERNAL USE
   * @param givenType observer type
   */
  public void setType(ObserveEvent.Type givenType) {
    obsType = givenType;
  }

  /**
   * INTERNAL USE
   * @return observer type
   */
  public ObserveEvent.Type getType() {
    return obsType;
  }

  private ObserveEvent.Type obsType = ObserveEvent.Type.GENERIC;

  /**
   * to be overwritten to handle appear events
   * @param event that happened
   */
  public void appeared(ObserveEvent event) {
    if (scriptRunnerType != null && ObserveEvent.Type.APPEAR.equals(obsType)) {
      run(event);
    }
  }

  /**
   * to be overwritten to handle vanish events
   * @param event that happened
   */
  public void vanished(ObserveEvent event) {
    if (scriptRunnerType != null && ObserveEvent.Type.VANISH.equals(obsType)) {
      run(event);
    }
  }

  /**
   * to be overwritten to handle changed events
   * @param event that happened
   */
  public void changed(ObserveEvent event) {
    if (scriptRunnerType != null && ObserveEvent.Type.CHANGE.equals(obsType)) {
      run(event);
    }
  }

  /**
   * to be overwritten to handle FindFailed events
   * @param event that happened
   */
  public void findfailed(ObserveEvent event) {
    if (scriptRunnerType != null && ObserveEvent.Type.FINDFAILED.equals(obsType)) {
      run(event);
    }
  }

  /**
   * to be overwritten to handle image missing events
   * @param event that happened
   */
  public void missing(ObserveEvent event) {
    if (scriptRunnerType != null && ObserveEvent.Type.MISSING.equals(obsType)) {
      run(event);
    }
  }

  /**
   * to be overwritten to handle generic events
   * @param event that happened
   */
  public void happened(ObserveEvent event) {
    if (scriptRunnerType != null && ObserveEvent.Type.GENERIC.equals(obsType)) {
      run(event);
    }
  }

  private void run(ObserveEvent e) {
    Object[] args = new Object[]{callback, e};
    if (scriptRunnerType != null) {
      if (!(Boolean) Commons.runFunctionScriptingSupport(scriptRunnerType, "runObserveCallback", args)) {
        Debug.error("ObserverCallBack: problem with scripting handler: %s\n%s",
                scriptRunnerType, callback.getClass().getName());
      }
    }
  }
}
