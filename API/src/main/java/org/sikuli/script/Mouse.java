/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.*;
import org.sikuli.script.support.devices.MouseDevice;
import org.sikuli.util.Highlight;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * Main pupose is to coordinate the mouse usage among threads <br>
 * At any one time, the mouse has one owner (usually a Region object) <br>
 * who exclusively uses the mouse, all others wait for the mouse to be free again <br>
 * if more than one possible owner is waiting, the next owner is uncertain <br>
 * It is detected, when the mouse is moved external from the workflow, which can be used for
 * appropriate actions (e.g. pause a script) <br>
 * the mouse can be blocked for a longer time, so only this owner can use the mouse (like some
 * transactional processing) <br>
 * Currently deadlocks and infinite waits are not detected, but should not happen ;-) <br>
 * Contained are methods to use the mouse (click, move, button down/up) as is
 */
public class Mouse {

  private static String me = "Mouse: ";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static Mouse mouse = null;
  private Device device = null;

  protected Location mousePos;
  protected boolean clickDouble;
  protected int buttons;
  protected int beforeWait;
  protected int innerWait;
  protected int afterWait;

  public static final int LEFT = InputEvent.BUTTON1_DOWN_MASK;
  public static final int MIDDLE = InputEvent.BUTTON2_DOWN_MASK;
  public static final int RIGHT = InputEvent.BUTTON3_DOWN_MASK;
  public static final int WHEEL_UP = -1;
  public static int WHEEL_DOWN = 1;
  public static final int WHEEL_STEP_DELAY = 50;

  private Mouse() {
  }

  public static void init() {
    if (mouse == null) {
      mouse = new Mouse();
      mouse.device = new Device(mouse);
      mouse.device.isMouse = true;
      mouse.device.lastPos = null;
    }
  }

  private static Mouse get() {
    if (!MouseDevice.isUseable()) {
      Commons.terminate(999, "Mouse not useable"); //TODO
    }
    if (mouse == null) {
      init();
    }
    return mouse;
  }

  protected static boolean use() {
    return get().device.use(null);
  }

  protected static boolean use(Object owner) {
    return get().device.use(owner);
  }

  protected static boolean keep(Object owner) {
    return get().device.keep(owner);
  }

  protected static boolean let() {
    return get().device.let(null);
  }

  protected static boolean let(Object owner) {
    return get().get().device.let(owner);
  }

  public static Location at() {
    return get().device.getLocation();
  }

  public static void reset() {
    if (mouse == null) {
      return;
    }
    get().device.unblock(get().device.owner);
    mouse.get().device.let(get().device.owner);
    get().device.let(get().device.owner);
    get().device.mouseMovedResponse = get().device.MouseMovedIgnore;
    get().device.mouseMovedCallback = null;
    get().device.callback = null;
    get().device.lastPos = null;
    resetRandom();
    Screen.getPrimaryScreen().getRobot().mouseReset();
  }

  /**
   * current setting what to do if mouse is moved outside Sikuli's mouse protection
   *
   * @return current setting see {@link #setMouseMovedAction(int)}
   */
  public static int getMouseMovedResponse() {
    return get().device.mouseMovedResponse;
  }

  /**
   * what to do if mouse is moved outside Sikuli's mouse protection <br>
   * - Mouse.MouseMovedIgnore (0) ignore it (default) <br>
   * - Mouse.MouseMovedShow (1) show and ignore it <br>
   * - Mouse.MouseMovedPause (2) show it and pause until user says continue <br>
   * (2 not implemented yet - 1 is used)
   *
   * @param movedAction value
   */
  public static void setMouseMovedAction(int movedAction) {
    if (movedAction > -1 && movedAction < 3) {
      get().device.mouseMovedResponse = movedAction;
      get().device.mouseMovedCallback = null;
      log(lvl, "setMouseMovedAction: %d", get().device.mouseMovedResponse);
    }
  }

  /**
   * what to do if mouse is moved outside Sikuli's mouse protection <br>
   * only 3 is honored:<br>
   * in case of event the user provided callBack.happened is called
   *
   * @param callBack ObserverCallBack
   */
  public static void setMouseMovedCallback(Object callBack) {
    if (callBack != null) {
      get().device.mouseMovedResponse = 3;
      get().device.mouseMovedCallback = new ObserverCallBack(callBack, ObserveEvent.Type.GENERIC);
    }
  }

  public static void setMouseMovedHighlight(boolean state) {
    get().device.MouseMovedHighlight = state;
  }

  /**
   * check if mouse was moved since last mouse action
   *
   * @return true/false
   */
  public static boolean hasMoved() {
    Location pos = get().device.getLocation();
    if (get().device.lastPos.x != pos.x || get().device.lastPos.y != pos.y) {
      return true;
    }
    return false;
  }

  /**
   * to click (left, right, middle - single or double) at the given location using the given button
   * only useable for local screens
   * <p>
   * timing parameters: <br>
   * - one value <br>
   * &lt; 0 wait before mouse down <br>
   * &gt; 0 wait after mouse up <br>
   * - 2 or 3 values 1st wait before mouse down <br>
   * 2nd wait after mouse up <br>
   * 3rd inner wait (milli secs, cut to 1000): pause between mouse down and up (Settings.ClickDelay)
   * <p>
   * wait before and after: &gt; 9 taken as milli secs - 1 ... 9 are seconds
   *
   * @param loc    where to click (not null)
   * @param action L,R,M left, right, middle - D means double click
   * @param args   timing parameters
   * @return the location
   */
  public static Location click(Location loc, String action, Integer... args) {
    if (null == loc) {
      throw new IllegalArgumentException("Mouse: click: Location should not be null");
    }
    if (get().device.isSuspended() || loc.isOtherScreen()) {
      return null;
    }
    getArgsClick(loc, action, args);
    get().device.use();
    Device.delay(mouse.beforeWait);
    Settings.ClickDelay = mouse.innerWait / 1000;
    click(loc, mouse.buttons, 0, ((Mouse) get()).clickDouble, null);
    Device.delay(mouse.afterWait);
    get().device.let();
    return loc;
  }

  private static void getArgsClick(Location loc, String action, Integer... args) {
    mouse.mousePos = loc;
    mouse.clickDouble = false;
    action = action.toUpperCase();
    if (action.contains("D")) {
      mouse.clickDouble = true;
    }
    mouse.buttons = 0;
    if (action.contains("L")) {
      mouse.buttons += LEFT;
    }
    if (action.contains("M")) {
      mouse.buttons += MIDDLE;
    }
    if (action.contains("R")) {
      mouse.buttons += RIGHT;
    }
    if (mouse.buttons == 0) {
      mouse.buttons = LEFT;
    }
    mouse.beforeWait = 0;
    mouse.innerWait = (int) (Settings.ClickDelay * 1000);
    mouse.afterWait = 0;
    if (args.length > 0) {
      if (args.length == 1) {
        if (args[0] < 0) {
          mouse.beforeWait = -args[0];
        } else {
          mouse.afterWait = args[0];
        }
      }
      mouse.beforeWait = args[0];
      if (args.length > 1) {
        mouse.afterWait = args[1];
        if (args.length > 2) {
          mouse.innerWait = args[2];
        }
      }
    }
  }

  protected static int click(Location loc, int buttons, Integer modifiers, boolean dblClick, Region region) {
    if (modifiers == null) {
      modifiers = 0;
    }
    Debug profiler = Debug.startTimer("Mouse.click");
    boolean shouldMove = true;
    if (loc == null) {
      shouldMove = false;
      loc = at();
    }
    IRobot robot = null;
    IScreen screen = loc.getScreen();
    if (screen == null) {
      profiler.end();
      return 0;
    }
    robot = screen.getRobot();
    if (robot == null) {
      profiler.end();
      return 0;
    }
    get().device.use(region);
    profiler.lap("before move");
    doMove(shouldMove, screen, loc, robot);
    robot.clickStarts();
    if (modifiers > 0) {
      robot.pressModifiers(modifiers);
    }
    int pause = Settings.ClickDelay > 1 ? 1 : (int) (Settings.ClickDelay * 1000);
    Settings.ClickDelay = 0.0;
    profiler.lap("before Down");
    if (dblClick) {
      robot.mouseDown(buttons);
      profiler.lap("before Up");
      robot.mouseUp(buttons);
      profiler.lap("before Down");
      robot.delay(pause);
      robot.mouseDown(buttons);
      profiler.lap("before Up");
      robot.mouseUp(buttons);
    } else {
      robot.mouseDown(buttons);
      robot.delay(pause);
      profiler.lap("before Up");
      robot.mouseUp(buttons);
    }
    profiler.lap("after click");
    if (modifiers > 0) {
      robot.releaseModifiers(modifiers);
    }
    robot.clickEnds();
    robot.waitForIdle();
    profiler.lap("before let");
    get().device.let(region);
    long duration = profiler.end();
    Debug.action(getClickMsg(loc, buttons, modifiers, dblClick, duration));
    return 1;
  }

  private static void doMove(boolean shouldMove, IScreen screen, Location loc, IRobot robot) {
    if (shouldMove) {
      if (Settings.isShowActions() && !screen.isOtherScreen()) {
        Highlight highlight = new Highlight(loc).doShow();
        RunTime.pause(0.3f);
        ((RobotDesktop) robot).smoothMoveSlow(loc);
        highlight.close();
        RunTime.pause(0.2f);
      } else {
        robot.smoothMove(loc);
      }
      log(4, "moved to: %d, %d", loc.x, loc.y);
    }
  }

  private static String getClickMsg(Location loc, int buttons, int modifiers, boolean dblClick, long duration) {
    String msg = "";
    if (modifiers != 0 && modifiers < 16) {
      msg += InputEvent.getModifiersExText(modifiers).replaceAll(" ", "") + " + ";
    }
    if (buttons == LEFT && !dblClick) {
      msg += "CLICK";
    }
    if (buttons == LEFT && dblClick) {
      msg += "DOUBLE CLICK";
    }
    if (buttons == RIGHT) {
      msg += "RIGHT CLICK";
    } else if (buttons == MIDDLE) {
      msg += "MID CLICK";
    }
    msg += String.format(" on %s (%d msec)", loc, duration);
    return msg;
  }

  private Random doRandom = new Random();

  public static void randomize() {
    Mouse.get().doRandom = new Random();
  }

  public static void randomize(long seed) {
    Mouse.get().doRandom = new Random(seed);
  }

  public static int getRandom() {
    return Mouse.get().randomOffset;
  }

  public static void setRandom() {
    setRandom(Mouse.get().defaultRandom);
  }

  public static void setRandom(int randomOffset) {
    Mouse.get().randomOffset = randomOffset;
  }

  public static void resetRandom() {
    setRandom(0);
  }

  public static boolean hasRandom() {
    return Mouse.get().randomOffset > 0;
  }

  private Location makeRandom(Location loc) {
    Location offset = new Location(doRandom.nextInt(2 * randomOffset) - randomOffset,
        doRandom.nextInt(2 * randomOffset) - randomOffset);
    loc.translate(offset.x, offset.y);
    return offset;
  }

  private int randomOffset = 0;
  private int defaultRandom = 6;

  /**
   * move the mouse to the given location (local and remote)
   *
   * @param loc Location
   * @return 1 for success, 0 otherwise
   */
  public static int move(Location loc) {
    return move(loc, null);
  }

  /**
   * move the mouse from the current position to the offset position given by the parameters
   *
   * @param xoff horizontal offset (&lt; 0 left, &gt; 0 right)
   * @param yoff vertical offset (&lt; 0 up, &gt; 0 down)
   * @return 1 for success, 0 otherwise
   */
  public static int move(int xoff, int yoff) {
    return move(at().offset(xoff, yoff));
  }

  protected static int move(Location loc, Region region) {
    if (get().device.isSuspended()) {
      return 0;
    }
    if (loc != null) {
      IRobot robot = null;
      IScreen screen = loc.getScreen();
      if (screen == null) {
        return 0;
      }
      robot = screen.getRobot();
      if (robot == null) {
        return 0;
      }
      if (!robot.isRemote()) {
        get().device.use(region);
      }
      if (Mouse.hasRandom()) {
        Location offset = Mouse.get().makeRandom(loc);
        log(lvl, "Mouse: random move offset: (%d, %d)", offset.x, offset.y);
      }
      doMove(true, screen, loc, robot);
      if (!robot.isRemote()) {
        get().device.let(region);
      }
      return 1;
    }
    return 0;
  }

  /**
   * press and hold the given buttons {@link Button}
   *
   * @param buttons value
   */
  public static void down(int buttons) {
    down(buttons, null);
  }

  protected static void down(int buttons, Region region) {
    if (get().device.isSuspended()) {
      return;
    }
    get().device.use(region);
    Screen.getRobot(region).mouseDown(buttons);
  }

  /**
   * release all buttons
   */
  public static void up() {
    up(0, null);
  }

  /**
   * release the given buttons {@link Button}
   *
   * @param buttons (0 releases all buttons)
   */
  public static void up(int buttons) {
    up(buttons, null);
  }

  protected static void up(int buttons, Region region) {
    if (get().device.isSuspended()) {
      return;
    }
    Screen.getRobot(region).mouseUp(buttons);
    if (region != null) {
      get().device.let(region);
    }
  }

  /**
   * move mouse using mouse wheel in the given direction the given steps <br>
   * the result is system dependent
   *
   * @param direction {@link Button}
   * @param steps     value
   */
  public static void wheel(int direction, int steps) {
    wheel(direction, steps, null);
  }

  protected static void wheel(int direction, int steps, Region region) {
    wheel(direction, steps, region, WHEEL_STEP_DELAY);
  }

  protected static void wheel(int direction, int steps, Region region, int stepDelay) {
    if (get().device.isSuspended()) {
      return;
    }
    IRobot r = Screen.getRobot(region);
    get().device.use(region);
    String wheelComment = (direction == WHEEL_UP ? "Content upwards" : "Content downwards");
    if (!Settings.WheelNatural) {
      wheelComment = (direction == WHEEL_UP ? "Content downwards" : "Content upwards");
      direction *= -1;
    }
    Debug.log(3, "Region: wheel: %s steps: %d", wheelComment, steps);
    for (int i = 0; i < steps; i++) {
      r.mouseWheel(direction);
      r.delay(stepDelay);
    }
    get().device.let(region);
  }
}
