/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sikuli.basics.Debug;

/**
 * INTERNAL USE ONLY --- NOT part of the official API
 * This class globally collects
 * all running observations and tracks the created events.<br>
 */
public class Observing {

  private static final String me = "Observing: ";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private Observing() {
  }

  private static final Map<String, Region> observers = Collections.synchronizedMap(new HashMap<String, Region>());
  private static final Map<String, ObserveEvent> events = Collections.synchronizedMap(new HashMap<String, ObserveEvent>());
  private static final List<Region> runningObservers = Collections.synchronizedList(new ArrayList<Region>());
  private static long lastName = 0;
  private static boolean shouldStopOnFirstEvent = false;

  /**
   * tell the next starting observer, to stop on the first event
   */
  public static void setStopOnFirstEvent() {
    shouldStopOnFirstEvent = true;
  }

  protected static boolean getStopOnFirstEvent() {
    boolean val = shouldStopOnFirstEvent;
    shouldStopOnFirstEvent = false;
    return val;
  }

  protected static void addRunningObserver(Region r) {
    if (shouldStopOnFirstEvent) {
      shouldStopOnFirstEvent = false;
      r.getObserver().setStopOnFirstEvent();
    }
    runningObservers.add(r);
    log(lvl,"add observer: now running %d observer(s)", runningObservers.size());
  }

  protected static void removeRunningObserver(Region r) {
    runningObservers.remove(r);
    log(lvl, "remove observer: now running %d observer(s)", runningObservers.size());
  }

  protected static synchronized String add(Region reg, ObserverCallBack obs, ObserveEvent.Type type, Object target) {
    String name;
    long now = new Date().getTime();
    while (now <= lastName) {
      now++;
    }
    lastName = now;
    name = "" + now;
    observers.put(name, reg);
    reg.getObserver().addObserver(target, (ObserverCallBack) obs, name, type);
    return name;
  }

  /**
   * set the observer with the given name inactive (not checked while observing)
   * @param name
   */
  public void setInactive(String name) {
    setActive(name, false);
  }

  /**
   * set the observer with the given name active (checked while observing)
   * @param name
   */
  public void setActive(String name) {
    setActive(name, true);
  }

  protected static void setActive(String name, boolean state) {
    if (observers.containsKey(name)) {
      observers.get(name).getObserver().setActive(name, state);
    }
  }

  /**
   * remove the observer from the list, a region observer will be stopped <br>
   * events for that observer are removed as well
   *
   * @param name name of observer
   */
  public static void remove(String name) {
    if (observers.containsKey(name)) {
      observers.get(name).stopObserver();
      observers.remove(name);
      events.remove(name);
    }
  }

  /**
   * stop and remove all observers registered for this region from the list <br>
   * events for those observers are removed as well
   * @param reg
   */
  public static void remove(Region reg) {
    for (String name : reg.getObserver().getNames()) {
      remove(name);
    }
  }

  /**
   * stop and remove all observers and their registered events
   *
   */
  public static void cleanUp() {
    String[] names;
    synchronized (observers) {
      names = new String[observers.size()];
      int i = 0;
      for (String name : observers.keySet()) {
        Region reg = observers.get(name);
        if (reg.isObserving()) {
          reg.stopObserver();
        }
        events.remove(name);
        names[i++] = name;
      }
    }
    runningObservers.clear();
    for (String name : names) {
      observers.remove(name);
    }
    log(lvl + 1, "as requested: removed all observers");
  }

  /**
   * are their any happened events
   *
   * @return true if yes
   */
  public static boolean hasEvents() {
    return events.size() > 0;
  }

  /**
   * are their any happened events for this region?
   *
   * @param reg
   * @return true if yes
   */
  public static boolean hasEvents(Region reg) {
    for (String name : reg.getObserver().getNames()) {
      if (events.containsKey(name)) {
        return true;
      }
    }
     return false;
  }

  /**
   * are their any happened events for the observer having this name?
   *
   * @param name
   * @return true if yes
   */
  public static boolean hasEvent(String name) {
    return events.containsKey(name);
  }

  protected static void addEvent(ObserveEvent evt) {
    events.put(evt.getName(), evt);
  }

  /**
   * return the events for that region <br>
   * events are removed from the list
   *
   * @param reg
   * @return the array of events or size 0 array if none
   */
  public static ObserveEvent[] getEvents(Region reg) {
    List<ObserveEvent> evts = new ArrayList<ObserveEvent>();
    ObserveEvent evt;
    for (String name : reg.getObserver().getNames()) {
      evt = events.get(name);
      if (evt != null) {
        evts.add(evt);
      }
      events.remove(name);
    }
    return evts.toArray(new ObserveEvent[0]);
  }

  /**
   * return all events (they are preserved) <br>
   *
   * @return the array of events or size 0 array if none
   */
  public static ObserveEvent[] getEvents() {
    List<ObserveEvent> evts = new ArrayList<ObserveEvent>();
    ObserveEvent evt;
    synchronized (events) {
      for (String name : events.keySet()) {
        evt = events.get(name);
        if (evt == null) {
          evts.add(evt);
        }
      }
    }
    return evts.toArray(new ObserveEvent[0]);
  }

  /**
   * retrieves and removes the requested event
   * @param name of event
   * @return the event or null
   */
  public static ObserveEvent getEvent(String name) {
    return events.remove(name);
  }

  /**
   * the event list is purged
   */
  public static void clearEvents() {
    events.clear();
  }
}
