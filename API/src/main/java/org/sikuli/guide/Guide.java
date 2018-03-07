/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.sikuli.guide.Transition.TransitionListener;
import org.sikuli.basics.Debug;
import org.sikuli.util.EventObserver;
import org.sikuli.util.EventSubject;
import org.sikuli.script.Location;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.util.OverlayTransparentWindow;
import org.sikuli.basics.Settings;
import org.sikuli.natives.SysUtil;

public class Guide extends OverlayTransparentWindow implements EventObserver {

  static float DEFAULT_TIMEOUT = 10.0f;
  static public final int FIRST = 0;
  static public final int MIDDLE = 1;
  static public final int LAST = 2;
  static public final int SIMPLE = 4;
  final float DIMMING_OPACITY = 0.5f;

  Robot robot;
  Region _region;
  JPanel content = null;
  Transition transition;
  ArrayList<Transition> transitions = new ArrayList<Transition>();
  ArrayList<Tracker> trackers = new ArrayList<Tracker>();
  Transition triggeredTransition;
  ClickableWindow clickableWindow = null;
  SxBeam beam = null;

  /**
   * create a new guide overlay on whole primary screen
   */
  public Guide() {
    super(new Color(0.1f, 0f, 0f, 0.1f), null);
    super.addObserver(this);
    init(new Screen());
  }

  /**
   * create a new guide overlay on given region
   */
  public Guide(Region region) {
    super(new Color(0.1f, 0f, 0f, 0.1f), null);
    super.addObserver(this);
    init(region);
  }

  private void init(Region region) {
    try {
      robot = new Robot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    content = getJPanel();
    _region = region;
    Rectangle rect = _region.getRect();
    content.setPreferredSize(rect.getSize());
    add(content);
    setBounds(rect);
    getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
    ((JPanel) getContentPane()).setDoubleBuffered(true);
    setVisible(false);
    setFocusableWindowState(false);
  }

  public void focusBelow() {
    if (Settings.isMac()) {
      // TODO: replace this hack with a more robust method

      // Mac's hack to bring focus to the window directly underneath
      // this hack works on the assumption that the caller has
      // the input focus but no interaction area at the current
      // mouse cursor position
      // This hack does not work well with applications that
      // can receive mouse clicks without having the input focus
      // (e.g., finder, system preferences)
      //         robot.mousePress(InputEvent.BUTTON1_MASK);
      //         robot.mouseRelease(InputEvent.BUTTON1_MASK);

      // Another temporary hack to switch to the previous window on Mac
      robot.keyPress(KeyEvent.VK_META);
      robot.keyPress(KeyEvent.VK_TAB);
      robot.keyRelease(KeyEvent.VK_META);
      robot.keyRelease(KeyEvent.VK_TAB);

      // wait a little bit for the switch to complete
      robot.delay(1000);
    }

  }

  @Override
  public void toFront() {
    if (Settings.isMac() || Settings.isWindows()) {
      // this call is necessary to allow clicks to go through the window (ignoreMouse == true)
      if (Settings.JavaVersion < 7) {
        SysUtil.getOSUtil().bringWindowToFront(this, true);
      } else {
      }
    }
    super.toFront();
  }

  @Override
  public void update(EventSubject es) {
    //TODO transparent paint
  }

  public String showNow(float secs) {
    transitions.add(new TimeoutTransition((int) secs * 1000));
    return showNow();
  }

  public String showNow() {
    String cmd = "Next";
    if (content.getComponentCount() == 0
            && transitions.isEmpty()) {
      //&& search == null) {
      return cmd;
    }
    startAnimation();
    startTracking();
    setVisible(true);
    toFront();

    //<editor-fold defaultstate="collapsed" desc="deal with interactive search elements">
    /*    if (search != null) {
     * search.setVisible(true);
     * search.requestFocus();
     * synchronized (this) {
     * try {
     * wait();
     * } catch (InterruptedException e) {
     * e.printStackTrace();
     * }
     * }
     * search.dispose();
     * search.setVisible(false);
     * String key = search.getSelectedKey();
     * search = null;
     * reset();
     * focusBelow();
     * return key;
     * }*/    //</editor-fold>

    if (transitions.isEmpty()) {
      // if no transition is added, use the default timeout transition
      transitions.add(new TimeoutTransition((int) DEFAULT_TIMEOUT * 1000));
    }
    final Object token = new Object();
    synchronized (token) {
      for (Transition transition : transitions) {
        transition.waitForTransition(new TransitionListener() {
          @Override
          public void transitionOccurred(Object source) {
            triggeredTransition = (Transition) source;
            synchronized (token) {
              token.notify();
            }
          }
        });
      }
      try {
        token.wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if (triggeredTransition instanceof ClickableWindow) {
      ClickableWindow cw = (ClickableWindow) triggeredTransition;
//TODO click through if not button
      cmd = cw.getLastClicked().getName();
    } else if (triggeredTransition instanceof TimeoutTransition) {
      cmd = "timeout";
    }
    reset();
    return cmd;
  }

  public void addToFront(JComponent comp) {
    addComponent(comp);
  }

  public void addComponent(JComponent comp) {
    content.add(comp, 0);
  }

  public void addToFront(Visual comp) {
    addComponent(comp, 0);
  }

  public void addComponent(Visual comp, int index) {
    if (comp instanceof SxClickable) {
      // add to the guide window
      content.add(comp, 0);
      if (clickableWindow == null) {
        clickableWindow = new ClickableWindow(this);
        addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            //Debug.info("[Guide] window closed");
            GlobalMouseMotionTracker.getInstance().stop();
          }
        });
      }
      clickableWindow.addClickable((SxClickable) comp);
      addTransition(clickableWindow);
      return;
    }
    content.add(comp, index);
    if (comp instanceof SxSpotlight) {
      setDarken(true);
    }
  }

  public void removeComponent(Component comp) {
    content.remove(comp);
  }

  private void reset() {
    clear();
    transitions.clear();
    // now we dipose window so the .py script can terminate
    if (clickableWindow != null) {
      clickableWindow.dispose();
      clickableWindow = null;
    }
    dispose();
  }

  public void clear() {
    if (clickableWindow != null) {
      clickableWindow.clear();
    }
    stopAnimation();
    stopTracking();
    content.removeAll();
    transition = null;
    beam = null;
    setDarken(false);
    setVisible(false);
    GlobalMouseMotionTracker.getInstance().stop();
  }

  public void setDefaultTimeout(float timeout_in_seconds) {
    DEFAULT_TIMEOUT = timeout_in_seconds;
  }

  public Region getRegion() {
    return _region;
  }

  Point convertToRegionLocation(Point point_in_global_coordinate) {
    Point ret = new Point(point_in_global_coordinate);
    ret.translate(-_region.x, -_region.y);
    return ret;
  }

//<editor-fold defaultstate="collapsed" desc="TODO not used: searchDialog">
  //  SearchDialog search = null;

  /*  public void addSearchDialog() {
   * search = new SearchDialog(this, "Enter the search string:");
   * //search = new GUISearchDialog(this);
   * search.setLocationRelativeTo(null);
   * search.setAlwaysOnTop(true);
   * }
   *
   * public void setSearchDialog(SearchDialog search) {
   * this.search = search;
   * }
   *
   * public void addSearchEntry(String key, Region region) {
   * if (search == null) {
   * addSearchDialog();
   * }
   * search.addEntry(key, region);
   * }*/
  //</editor-fold>

  boolean hasSpotlight() {
    for (Component comp : content.getComponents()) {
      if (comp instanceof SxSpotlight) {
        return true;
      }
    }
    return false;
  }

  public void updateSpotlights(ArrayList<Region> regions) {
    removeSpotlights();

    if (regions.isEmpty()) {

      setBackground(null);
      content.setBackground(null);

    } else {

      // if there are spotlights added, darken the background
      setBackground(new Color(0f, 0f, 0f, DIMMING_OPACITY));
      content.setBackground(new Color(0f, 0f, 0f, DIMMING_OPACITY));
      for (Region r : regions) {
        SxSpotlight spotlight = new SxSpotlight(r);
        spotlight.setShape(SxSpotlight.CIRCLE);
        //addSpotlight(r,SxSpotlight.CIRCLE);
      }
    }

    repaint();
  }

  public void removeSpotlights() {
    for (Component co : content.getComponents()) {
      if (co instanceof SxSpotlight) {
        content.remove(co);
      }
    }
  }

  public void setDarken(boolean darken) {
//TODO check against transparency
    if (darken) {
      //setBackground(new Color(0f,0f,0f,DIMMING_OPACITY));
      content.setBackground(new Color(0f, 0f, 0f, DIMMING_OPACITY));
    } else {
      setBackground(null);
      content.setBackground(null);
    }
  }

  public Visual addBeam(Region r) {
    beam = new SxBeam(this, r);
    SxAnchor anchor = new SxAnchor(r);
    addTransition(beam);
    return anchor;
  }

//<editor-fold defaultstate="collapsed" desc="TODO not used: addMagnet">
  /*  public void addMagnifier(Region region) {
   * Magnifier mag = new Magnifier(this, region);
   * content.add(mag);
   * }*/
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="TODO not used: setDialog">
  public void setDialog(String message) {
    TransitionDialog dialog = new TransitionDialog();
    dialog.setText(message);
    transition = dialog;
  }

  public void setDialog(TransitionDialog dialog_) {
    //dialog = dialog_;
    transition = dialog_;
  }

  //</editor-fold>

  public void stopAnimation() {
    for (Component co : content.getComponents()) {

      /*      if (co instanceof Magnifier) {
       * ((Magnifier) co).start();
       * }*/
      if (co instanceof Visual) {
        ((Visual) co).stopAnimation();
      }
    }
  }

  public void startAnimation() {
    for (Component co : content.getComponents()) {

//      if (co instanceof Magnifier) {
//        ((Magnifier) co).start();
//      }
      if (co instanceof Visual) {
        ((Visual) co).startAnimation();
      }
    }
  }

  public void addTransition(Transition t) {
    if (!transitions.contains(t)) {
      transitions.add(t);
    }
  }

  public Transition getTransition() {
    return transition;
  }

  public void startTracking() {
    for (Component co : content.getComponents()) {
      ((Visual) co).updateComponent();
      if (co instanceof SxAnchor) {
        ((SxAnchor) co).startTracking();
      }
    }
  }

  public void stopTracking() {
    for (Component co : content.getComponents()) {
      if (co instanceof SxAnchor) {
        ((SxAnchor) co).stopTracking();
      }
    }
  }

  //<editor-fold defaultstate="collapsed" desc="global tracking support - not used currently">
  public void addTracker(Pattern pattern, SxAnchor anchor) {
    Tracker tracker = null;

    //      // find a tracker already assigned to this pattern
    //      for (Tracker t : trackers){
    //         if (t.isAlreadyTracking(pattern,r)){
    //            tracker = t;
    //            break;
    //         }
    //      }

    //      if (tracker == null){
    tracker = new Tracker(this, pattern, null);
    trackers.add(tracker);
    //      }
    BufferedImage img;
    try {
      img = pattern.getBImage();
      anchor.setActualSize(img.getWidth(), img.getHeight());
      tracker.setAnchor(anchor);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addTracker(Pattern pattern, Region r, Visual c) {
    Tracker tracker = null;

    // find a tracker already assigned to this pattern
    for (Tracker t : trackers) {
      if (t.isAlreadyTracking(pattern, r)) {
        tracker = t;
        break;
      }
    }

    if (tracker == null) {
      tracker = new Tracker(this, pattern, r);
      trackers.add(tracker);
    }

    tracker.setAnchor(c);
  }

  public void addTracker(Pattern pattern, Region r, ArrayList<Visual> components) {
    Tracker tracker = new Tracker(this, pattern, r);
    for (Visual c : components) {
      tracker.setAnchor(c);
    }
    trackers.add(tracker);
  }

  abstract class TrackerAdapter {
    abstract void patternAnchored();
  }

  //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="TODO not used: play steps">
  /*  public void playStepOnWebpage(Step step, Region leftmarker, Region rightmarker) {
   *
   * //Point screenshotOrigin = new Point(614,166);
   *
   * int originalWidth = step.getScreenImage().getWidth();
   * int originalHeight = step.getScreenImage().getHeight();
   *
   * int displayWidth = rightmarker.x + rightmarker.w - leftmarker.x;
   * float scale = 1.0f * displayWidth / originalWidth;
   * Debug.info("scale:" + scale);
   * int displayHeight = (int) (originalHeight * scale);
   *
   * int originX = leftmarker.x;
   * int originY = leftmarker.y - displayHeight;
   * Point screenshotOrigin = new Point(originX, originY);
   *
   * //scale = 1.0f;
   * //Point screenshotOrigin = new Point(715,192);
   * //Point screenshotOrigin = new Point(953,257);//
   *
   * Debug.info("Step size:" + step.getView().getSize());
   * //Dimension displaySize = new Dimension(480,363);
   * //float scale = 480f/640f;
   *
   * Screen s = new Screen();
   * for (Part part : step.getParts()) {
   *
   * Point targetOrigin = part.getTargetOrigin();
   *
   * Point screenOrigin = new Point();
   * screenOrigin.x = screenshotOrigin.x + (int) (targetOrigin.x * scale);
   * screenOrigin.y = screenshotOrigin.y + (int) (targetOrigin.y * scale);
   *
   * part.setAnchorScreenLocation(screenOrigin);
   * }
   *
   * playStep(step, scale);
   * }
   *
   * public void playStep(Step step) {
   * playStep(step, 1.0f);
   * }
   *
   * public void play(ArrayList<SklStepModel> steps) {
   * for (SklStepModel step : steps) {
   * String ret = playStep(step);
   * if (ret == null) {
   * continue;
   * }
   *
   * if (ret.equals("Exit")) {
   * return;
   * }
   * }
   *
   * }*/
  //   public void playStepList(SklStepListModel stepListModel){
  //      for (Enumeration<?> e = stepListModel.elements() ; e.hasMoreElements() ;) {
  //          SklStepModel eachStepModel = (SklStepModel) e.nextElement();
  //          String ret = playStep(eachStepModel);
  //          if (ret == null)
  //             continue;
  //
  //          if (ret.equals("Exit")){
  //             return;
  //          }
  //       }
  //
  //   }
  /*  public String playStep(SklStepModel step_) {
   *
   * SklStepModel step = null;
   * try {
   * step = (SklStepModel) step_.clone();
   * } catch (CloneNotSupportedException e1) {
   * e1.printStackTrace();
   * }
   *
   * for (SklModel model : step.getModels()) {
   * model.setOpacity(0f);
   * SklView view = SklViewFactory.createView(model);
   * addToFront(view);
   * }
   *
   *
   * SxButton btn = new SxButton("Exit");
   * btn.setActualLocation(10, 50);
   * addToFront(btn);
   *
   * SxButton skip = new SxButton("Skip");
   * skip.setActualLocation(90, 50);
   * addToFront(skip);
   *
   * // do these to allow static elements to be drawn
   * setVisible(true);
   * toFront();
   *
   * step.startTracking(this);
   * step.startAnimation();
   *
   * String ret = showNow();
   * Debug.info("[Guide.playStep] ret = " + ret);
   *
   * return ret;
   * }
   *
   * public void playStep(Step step, final float scale) {
   *
   *
   * SxImage screenshot = new SxImage(step.getScreenImage());
   * screenshot.setLocationRelativeToRegion(new Screen(), Layout.INSIDE);
   * //      screenshot.setOpacity(0);
   * //      screenshot.addAnimation(AnimationFactory.createOpacityAnimation(screenshot,0.1f,0.9f));
   * //      // TODO replace these with a Pause animation
   * //      screenshot.addAnimation(AnimationFactory.createOpacityAnimation(screenshot,0.9f,0.9f));
   * //      screenshot.addAnimation(AnimationFactory.createOpacityAnimation(screenshot,0.9f,0.9f));
   * //      screenshot.addAnimation(AnimationFactory.createOpacityAnimation(screenshot,1,0));
   * //      addToFront(screenshot);
   *
   * for (final Part part : step.getParts()) {
   *
   * Pattern pattern = part.getTargetPattern();
   *
   * BufferedImage patternImage = null;
   * try {
   * patternImage = pattern.getBImage();
   * } catch (Exception e) {
   * }
   *
   * final SxAnchor anchor = new SxAnchor(pattern);
   * anchor.setEditable(true);
   * anchor.setOpacity(1f);
   * anchor.setAnimateAnchoring(true);
   *
   * Point anchorLocation = new Point(part.getTargetOrigin());
   * Point screenshotLocation = screenshot.getActualLocation();
   * anchorLocation.translate(screenshotLocation.x, screenshotLocation.y);
   * anchor.setActualLocation(anchorLocation);
   *
   * SxClickable clickable = new SxClickable(null);
   * clickable.setLocationRelativeToComponent(anchor, Layout.OVER);
   * addToFront(clickable);
   *
   * // add an image to visualize the target pattern
   * final SxImage sklImage = new SxImage(patternImage);
   * sklImage.setLocationRelativeToComponent(anchor, Layout.OVER);
   *
   * anchor.addListener(new AnchorListener() {
   * @Override
   * public void anchored() {
   * sklImage.removeFromLeader();
   * sklImage.setVisible(false);
   * repaint();
   *
   *
   * for (Visual comp : anchor.getFollowers()) {
   * if (comp instanceof SxText || comp instanceof SxFlag) {
   * comp.popin();
   * }
   * }
   *
   * anchor.popin();
   * }
   *
   * @Override
   * public void found(SxAnchor source) {
   * }
   * });
   *
   * addToFront(sklImage);
   * addToFront(anchor);
   *
   * Point o = part.getTargetOrigin();
   * Point p = anchor.getActualLocation();
   *
   * for (Visual compo : part.getAnnotationComponents()) {
   *
   * Visual comp = (Visual) compo.clone();
   *
   * Point loc = comp.getActualLocation();
   * loc.x = (int) ((int) (loc.x - o.x) * scale) + p.x;
   * loc.y = (int) ((int) (loc.y - o.y) * scale) + p.y;
   *
   * comp.setActualLocation(loc);
   * comp.setLocationRelativeToComponent(anchor);
   * comp.setActualSize((int) (comp.getActualWidth() * scale), (int) (comp.getActualHeight() * scale));
   *
   * addToFront(comp);
   *
   * comp.popout();
   *
   * }
   *
   * anchor.popout();
   *
   * }
   *
   * SxButton btn = new SxButton("Exit");
   * btn.setActualLocation(50, 50);
   * addToFront(btn);
   *
   * showNow();
   *
   * }
   *
   * public void playSteps(ArrayList<Step> steps) throws FindFailed {
   *
   * Screen s = new Screen();
   *
   * for (Step step : steps) {
   * SxButton btn = new SxButton("Next");
   * btn.setLocation(s.getTopRight().left(200).below(50));
   *
   *
   * addToFront(btn);
   *
   * step.setTransition(getTransition());
   *
   * playStep(step, 1.0f);
   * }
   *
   * }*///</editor-fold>

  /**
   * create a rectangle in this guide plane and add to front
   *
   * @return the rectangle
   */

  public Visual rectangle() {
    Visual gc = new SxRectangle();
    gc.setGuide(this);
    addToFront(gc);
    return gc;
  }

  public Visual circle() {
    Visual gc = new SxCircle();
    gc.setGuide(this);
    addToFront(gc);
    return gc;
  }

  public Visual text(String text) {
    Visual gc = new SxText(text);
    gc.setGuide(this);
    addToFront(gc);
    return gc;
  }

  public Visual flag(String text) {
    Visual gc = new SxFlag(text);
    gc.setGuide(this);
    addToFront(gc);
    return gc;
  }

  public Visual callout(String text) {
    Visual gc = new SxCallout(text);
    gc.setGuide(this);
    addToFront(gc);
    return gc;
  }

  public Visual image(Object img) {
    Visual gc = null;
    if (img instanceof String) {
      gc = new SxImage((String) img);
    } else if (img instanceof BufferedImage) {
      gc = new SxImage((BufferedImage) img);
    }
    if (gc != null) {
      gc.setGuide(this);
      addToFront(gc);
    } else {
      Debug.log(2, "Guide.image: invalid argument");
    }
    return gc;
  }

  public Visual bracket() {
    Visual gc = new SxBracket();
    gc.setGuide(this);
    addToFront(gc);
    return gc;
  }

  public Visual arrow(Object from, Object to) {
    Visual gc = null;
    if (from instanceof Region) {
      gc = new SxArrow(((Region) from).getCenter().getPoint(), ((Region) to).getCenter().getPoint());
    } else if (from instanceof Point || from instanceof Location) {
      gc = new SxArrow((Point) from, (Point) to);
    } else if (from instanceof Visual) {
      gc = new SxArrow((Visual) from, (Visual) to);
    }
    if (gc != null) {
      gc.setGuide(this);
      addToFront(gc);
    } else {
      Debug.log(2, "Guide.arrow: invalid arguments");
    }
    return gc;
  }

  public Visual button(String name) {
    Visual gc = new SxButton(name);
    gc.setGuide(this);
    addToFront(gc);
    return gc;
  }
}
