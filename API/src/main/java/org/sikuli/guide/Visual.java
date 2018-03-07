/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComponent;

import org.sikuli.basics.Debug;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

public class Visual extends JComponent
        implements Cloneable {

  public enum Layout {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    FOLLOWERS,
    INSIDE,
    OVER,
    ORIGIN,
    CENTER,
    AROUND
  };

  public boolean hasChanged = false;
  public int PADDING_X = 4;
  public int PADDING_Y = 4;

  //<editor-fold defaultstate="collapsed" desc="Setters/Getters">
  Region targetRegion = null;
  Visual targetComponent = null;
  Pattern targetPattern = null;
  Guide currentGuide = null;

  public void setGuide(Guide g) {
    currentGuide = g;
  }

  public Region getTarget() {
    if (targetRegion != null) {
      return targetRegion;
    }
    return getRegion();
  }

  public Visual setScale(float scale) {
    return this;
  }

  public Layout layout = Layout.OVER;
  public Layout currentLayout = layout;

  public Visual setLayout(Layout lo) {
    currentLayout = layout;
    layout = lo;
    setLocationRelative(lo);
    return this;
  }

  public static Color defColor = Color.RED;
  Color color;
  public static Color defColorFront = Color.MAGENTA;
  Color colorFront;
  public static Color defColorBack = Color.WHITE;
  Color colorBack;
  public static Color defColorFrame = Color.BLACK;
  Color colorFrame;
  public static Color defColorText = Color.BLACK;
  Color colorText;

  /**
   * set a Java Color for the component<br>
   * not all components have all color options
   * @param all sets the color for all options (which have to be null in this case)
   * @param front sets the foreground color
   * @param back sets the background color
   * @param frame sets the color of the frame
   * @param text sets the color of the text
   * @return the component itself for dot-chaining
   */
  public Visual setColors(Color all, Color front, Color back, Color frame, Color text) {
    if (all != null) {
      color = all;
    }
    if (front != null) {
      colorFront = front;
      setForeground(colorFront);
    }
    if (back != null) {
      colorBack = back;
      setBackground(colorBack);
    }
    if (frame != null) {
      colorFrame = frame;
    }
    if (text != null) {
      colorText = text;
    }
    return this;
  }

  /**
   * mainly for Jython layer: colors given as (r, g, b) integer array
   *
   * @param front sets the foreground color
   * @param back sets the background color
   * @param frame sets the color of the frame
   * @param text sets the color of the text
   * @return the component itself for dot-chaining
   */
  public Visual setColors(int[] front, int[] back, int[] frame, int[] text) {
    Color cf = null;
    Color cb = null;
    Color cr = null;
    Color ct = null;
    if (front != null) {
      cf = new Color(front[0], front[1], front[2]);
    }
    if (back != null) {
      cb = new Color(back[0], back[1], back[2]);
    }
    if (frame != null) {
      cr = new Color(frame[0], frame[1], frame[2]);
    }
    if (text != null) {
      ct = new Color(text[0], text[1], text[2]);
    }
    setColors(null, cf, cb, cr, ct);
    return this;
  }

  /**
   * set the front and back color
   * @param color
	 * @return the component itself
   */
  public Visual setColor(Color color) {
    setColors(null, color, color, null, null);
    return this;
  }

  /**
   * set the front and back color as (r, g, b) integer array
	 * @return the component itself
   */
  public Visual setColor(int r, int g, int b) {
    setColor(new Color(r, g, b));
    return this;
  }

  /**
   * set the text color
   * @param color
	 * @return the component itself
   */
  public Visual setTextColor(Color color) {
    setColors(null, null, null, null, color);
    return this;
  }

  /**
   * set text color as (r, g, b) integer array
	 * @return the component itself
   */
  public Visual setTextColor(int r, int g, int b) {
    setTextColor(new Color(r, g, b));
    return this;
  }

  public static String getColorHex(Color col) {
    String rgb = Integer.toHexString(col.getRGB());
    return rgb.substring(2, rgb.length()).toUpperCase();
  }

  public static int defStroke = 3;
  int stroke;

  public Visual setStroke(int stk) {
    stroke = stk;
    return this;
  }

  public static String defFont = "";
  String fontName = "";
  public static int defFontSize = 0;
  int fontSize = 0;

  public Visual setFont(String font, int fontSize) {
    if (font != null && !this.fontName.isEmpty()) {
      this.fontName = font;
      hasChanged = true;
    }
    if (fontSize > 0 && this.fontSize > 0) {
      this.fontSize = fontSize;
      hasChanged = true;
    }
    if (hasChanged) {
      hasChanged = false;
    }
    return this;
  }

  public Visual setFontSize(int i) {
    setFont(null, i);
    return this;
  }

  String getStyleString() {
    String s = "font-size:" + fontSize + "px;color:#" + getColorHex(colorText)
            + ";background-color:#" + getColorHex(colorBack) + ";padding:3px";
    if (!fontName.isEmpty()) {
      s = "font:" + fontName + ";" + s;
    }
    return s;
  }

  static int defMaxWidth = 300;
  int maxWidth;

  public Visual setMaxWidth(int w) {
    maxWidth = w;
    return this;
  }

  String text = "";

  public String getText() {
    return text;
  }

  public Visual setText(String text) {
    if (!this.text.isEmpty()) {
      this.text = text;
    }
    return this;
  }
  //</editor-fold>

  public Visual() {
    super();
    init();
  }

  private void init() {
    cm = new ComponentMover();
    setMovable(false);
    setActualLocation(0, 0);
    setActualSize(new Dimension(0, 0));
    color = defColor;
    colorFront = defColorFront;
    colorBack = defColorBack;
    colorFrame = defColorFrame;
    colorText = defColorText;
    stroke = defStroke;
    fontName = defFont;
    fontSize = defFontSize;
    maxWidth = defMaxWidth;
  }

  //<editor-fold defaultstate="collapsed" desc="AutoFeatures">
  private boolean autoLayoutEnabled = false;
  private boolean autoResizeEnabled = false;
  private boolean autoMoveEnabled = false;
  private boolean autoVisibilityEnabled = false;

  public void setAutoLayoutEnabled(boolean autoLayoutEnabled) {
    this.autoLayoutEnabled = autoLayoutEnabled;
  }

  public boolean isAutoLayoutEnabled() {
    return autoLayoutEnabled;
  }

  public void setAutoResizeEnabled(boolean autoResizeEnabled) {
    this.autoResizeEnabled = autoResizeEnabled;
  }

  public boolean isAutoResizeEnabled() {
    return autoResizeEnabled;
  }

  public void setAutoMoveEnabled(boolean autoMoveEnabled) {
    this.autoMoveEnabled = autoMoveEnabled;
  }

  public boolean isAutoMoveEnabled() {
    return autoMoveEnabled;
  }

  public void setAutoVisibilityEnabled(boolean autoVisibilityEnabled) {
    this.autoVisibilityEnabled = autoVisibilityEnabled;
  }

  public boolean isAutoVisibilityEnabled() {
    return autoVisibilityEnabled;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="RectangleFeatures">
  private Rectangle actualBounds = new Rectangle();

  public Rectangle getActualBounds() {
    return actualBounds;
  }

  public Region getRegion() {
    return Region.create(getBounds());
  }

  public void setActualBounds(Rectangle actualBounds) {
    this.actualBounds = (Rectangle) actualBounds.clone();
    Rectangle paintBounds = (Rectangle) actualBounds.clone();
    if (hasShadow()) {
      paintBounds.x -= (shadowSize - shadowOffset);
      paintBounds.y -= (shadowSize - shadowOffset);
      paintBounds.width += (2 * shadowSize);
      paintBounds.height += (2 * shadowSize);
    }
    super.setBounds(paintBounds);
    updateAllFollowers();
  }

  public Point getCenter() {
    Point loc = new Point(getActualLocation());
    Dimension size = getActualSize();
    loc.x += size.width / 2;
    loc.y += size.height / 2;
    return loc;
  }

  public Dimension getActualSize() {
    return new Dimension(getActualWidth(), getActualHeight());
  }

  public void setActualSize(int width, int height) {
    if (height == 0) {
      setActualSize(new Dimension(width, getActualHeight()));
    } else if (width == 0) {
      setActualSize(new Dimension(getActualWidth(), height));
    } else {
      setActualSize(new Dimension(width, height));
    }
  }

  public void setActualSize(Dimension actualSize) {
    actualBounds.setSize(actualSize);
    Dimension paintSize = (Dimension) actualSize.clone();
    if (hasShadow()) {
      paintSize.width += (2 * shadowSize);
      paintSize.height += (2 * shadowSize);
    }
    super.setSize(paintSize);
    updateAllFollowers();
  }

  public int getActualWidth() {
    return getActualBounds().width;
  }

  public int getActualHeight() {
    return getActualBounds().height;
  }

  public Point getActualLocation() {
    return actualBounds.getLocation();
  }

  public void offsetLocation(int x, int y) {
    setActualLocation(getActualLocation().x + x, getActualLocation().y + y);
  }

  public void setActualLocation(Point location) {
    setActualLocation(location.x, location.y);
  }

  public void setActualLocation(int x, int y) {
    int paintX = x;
    int paintY = y;
    actualBounds.setLocation(x, y);
    if (hasShadow()) {
      paintX -= (shadowSize - shadowOffset);
      paintY -= (shadowSize - shadowOffset);
    }
    super.setLocation(paintX, paintY);
    updateAllFollowers();
  }

  class Margin {

    int top;
    int left;
    int bottom;
    int right;
  }
  Margin margin = null;

  public void setMargin(int top, int left, int bottom, int right) {
    margin = new Margin();
    margin.top = top;
    margin.left = left;
    margin.bottom = bottom;
    margin.right = right;
  }

  int offsetx = 0;
  int offsety = 0;
  public void setOffset(int offsetx, int offsety) {
    this.offsetx = offsetx;
    this.offsety = offsety;
    offsetLocation(offsetx, offsety);
  }

  // TODO: fix this
  float zoomLevel = 1.0f;
  public void setZoomLevel(float zoomLevel) {

    if (true) {
      return;
    }

    this.zoomLevel = zoomLevel;

    for (Visual sklComp : getFollowers()) {
      if (sklComp.autolayout != null) {
        sklComp.setZoomLevel(zoomLevel);
      }
    }

    Debug.info("[setZoomLevel] Component:" + this);
//      Debug.info("Actual bounds:" + actualBounds);
    Rectangle bounds = new Rectangle(getActualBounds());

    bounds.x *= zoomLevel;
    bounds.y *= zoomLevel;
    bounds.width *= zoomLevel;
    bounds.height *= zoomLevel;

    //super.setBounds(bounds);
    super.setBounds(bounds);

    for (Visual sklComp : getFollowers()) {
      if (sklComp.autolayout != null) {

        Debug.info("Updaing by offset:" + sklComp.autolayout);
        Debug.info("Updaing child:" + sklComp);

        if (sklComp.autolayout instanceof AutoLayoutByMovement) {
          ((AutoLayoutByMovement) sklComp.autolayout).x = bounds.x;
          ((AutoLayoutByMovement) sklComp.autolayout).y = bounds.y;
        } else if (sklComp.autolayout instanceof AutoLayoutByOffset) {
//               ((AutoLayoutByOffset) sklComp.autolayout).offsetx *= zoomLevel;
//               ((AutoLayoutByOffset) sklComp.autolayout).offsety *= zoomLevel;
//               sklComp.zoomLevel = zoomLevel;
          sklComp.autolayout.update();
        } else {
          sklComp.autolayout.update();
        }
      }
    }

  }
  // this allows the component to be dragged to another location on the screen
  ComponentMover cm;

  public void setMovable(boolean movable) {
    if (movable) {
      cm.registerComponent(this);
    } else {
      cm.deregisterComponent(this);
    }
  }
  float opacity = 1.0f;

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Painting">
  public void setOpacity(float opacity) {
    if (opacity > 0) {
      setVisible(true);
    } else {
      setVisible(false);
    }
    this.opacity = opacity;
    for (Visual sklComp : getFollowers()) {
      sklComp.setOpacity(opacity);
    }
    //      if (shadowRenderer != null){
    //         shadowRenderer.createShadowImage();
    //      }
    Rectangle r = getBounds();
    if (getTopLevelAncestor() != null) //getTopLevelAncestor().repaint(r.x,r.y,r.width,r.height);
      // for some reason the whole thing needs to be repainted otherwise the
      // shadow of other compoments looks weird
    {
      getTopLevelAncestor().repaint();
    }
    //   public void changeOpacityTo(float targetOpacity){
    //      OpacityAnimator anim = new OpacityAnimator(this, opacity,targetOpacity);
    //      anim.start();
    //   }
  }

  public void updateComponent() {
  }

  public void paintPlain(Graphics g) {
    super.paint(g);
  }

  @Override
  public void paint(Graphics g) {
    BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    if (shadowRenderer != null) {
      shadowRenderer.paintComponent(g2);
      g2.translate((shadowSize - shadowOffset), (shadowSize - shadowOffset));
    }
    super.paint(g2);
    Graphics2D g2d = (Graphics2D) g;
    ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    g2d.drawImage(image, 0, 0, null, null);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Shadow">
  ShadowRenderer shadowRenderer;
  private int defShadowSize = 10;
  int shadowSize = 0;
  private int defShadowOffset = 2;
  int shadowOffset = 0;

  public void setShadowDefault() {
    setShadow(defShadowSize, defShadowOffset);
  }

  public void setShadow(int shadowSize, int shadowOffset) {
    this.shadowSize = shadowSize;
    this.shadowOffset = shadowOffset;
    shadowRenderer = new ShadowRenderer(this, shadowSize);
    super.setSize(getActualWidth() + 2 * shadowSize, getActualHeight() + 2 * shadowSize);
    Point p = getActualLocation();
    p.x = p.x - shadowSize + shadowOffset;
    p.y = p.y - shadowSize + shadowOffset;
    super.setLocation(p.x, p.y);
  }

  boolean hasShadow() {
    return shadowRenderer != null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Animation">
  boolean animationRunning = false;

  class AnimationSequence {
    Queue<NewAnimator> queue = new LinkedBlockingQueue<NewAnimator>();

    private void startNextAnimation() {
      if (queue.peek() != null) {
        NewAnimator anim = queue.remove();
        anim.start();
        anim.setListener(new AnimationListener() {
          @Override
          public void animationCompleted() {
            startNextAnimation();
          }
        });

      }
    }

    public void add(NewAnimator animator) {
      queue.add(animator);
    }

    public void start() {
      startNextAnimation();
    }
  }

  AnimationSequence animationSequence = new AnimationSequence();

  AnimationFactory getAnimationFactory() {
    return new AnimationFactory();
  }

  public void addAnimation(NewAnimator anim) {
    animationSequence.add(anim);
  }

  public void addMoveAnimation(Point source, Point destination) {
    animationSequence.add(AnimationFactory.createMoveAnimation(this, source, destination));
  }

  public void addResizeAnimation(Dimension currentSize, Dimension targetSize) {
    animationSequence.add(AnimationFactory.createResizeAnimation(this, currentSize, targetSize));
  }

  public void addCircleAnimation(Point origin, float radius) {
    animationSequence.add(AnimationFactory.createCircleAnimation(this, origin, radius));
  }

  public void addFadeinAnimation() {
    if (opacity < 1f) {
      animationSequence.add(AnimationFactory.createOpacityAnimation(this, opacity, 1f));
    }
  }

  public void addFadeoutAnimation() {
    if (opacity > 0f) {
      animationSequence.add(AnimationFactory.createOpacityAnimation(this, opacity, 0f));
    }
  }

  public void addSlideAnimation(Point destination, Layout side) {
    Point p0 = new Point(destination);
    Point p1 = new Point(destination);

    if (side == Layout.RIGHT) {
      p0.x += 20;
    } else if (side == Layout.BOTTOM) {
      p0.y += 20;
    } else if (side == Layout.TOP) {
      p0.y -= 20;
    } else if (side == Layout.LEFT) {
      p0.x -= 20;
    }

    setActualLocation(p0);
    addMoveAnimation(p0, p1);
  }

  public void startAnimation() {
    animationSequence.start();
  }

  public void stopAnimation() {
    if (emphasis_anim != null) {
      emphasis_anim.stop();
    }
    if (entrance_anim != null) {
      entrance_anim.stop();
    }
  }

  public Animator createSlidingAnimator(int offset_x, int offset_y) {
    Point dest = getActualLocation();
    Point src = new Point(dest.x + offset_x, dest.y + offset_y);
    return new MoveAnimator(this, src, dest);
  }

  public Animator createMoveAnimator(int dest_x, int dest_y) {
    Point src = getActualLocation();
    Point dest = new Point(dest_x, dest_y);
    return new MoveAnimator(this, src, dest);
  }

  public void resizeTo(Dimension targetSize) {
    //ResizeAnimator anim = new ResizeAnimator(this, getActualSize(),targetSize);
    //anim.start();
  }

  public void moveTo(Point targetLocation) {
    NewAnimator anim = AnimationFactory.createCenteredMoveAnimation(this, getActualLocation(), targetLocation);
    anim.start();
  }

  public void moveTo(Point targetLocation, AnimationListener listener) {
    NewAnimator anim = AnimationFactory.createCenteredMoveAnimation(this, getActualLocation(), targetLocation);
    anim.setListener(listener);
    anim.start();
  }

  public void popin() {
    Dimension targetSize = new Dimension(getActualSize());
    targetSize.width /= 1.2;
    targetSize.height /= 1.2;
    NewAnimator anim = AnimationFactory.createCenteredResizeToAnimation(this, targetSize);
    anim.start();
  }

  public void popout() {
    setShadowDefault();

    Dimension targetSize = new Dimension(getActualSize());
    targetSize.width *= 1.2;
    targetSize.height *= 1.2;
    NewAnimator anim = AnimationFactory.createCenteredResizeToAnimation(this, targetSize);
    anim.start();
  }

  Animator entrance_anim;
  Animator emphasis_anim;

  public void setEntranceAnimation(Animator anim) {
    if (entrance_anim != null) {
      entrance_anim.stop();
    } else {
      entrance_anim = anim;
    }
  }

  public void setEmphasisAnimation(Animator anim) {
    if (emphasis_anim != null) {
      emphasis_anim.stop();
    }

    if (entrance_anim != null) {
      entrance_anim.stop();
    }

    emphasis_anim = anim;
  }

  AnimationListener animationListener;

  public void addAnimationListener(AnimationListener listener) {
    animationListener = listener;
  }

  public void animationCompleted() {
    if (animationListener != null) {
      animationListener.animationCompleted();
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="layout">
  public Visual left() {
    left(0);
    return this;
  }

  public Visual left(int x) {
    setLayout(Layout.LEFT);
    if (x != 0) {
      setOffset(-x, 0);
    }
    return this;
  }
  public Visual right() {
    return right(0);
  }

  public Visual right(int x) {
    setLayout(Layout.RIGHT);
    if (x != 0) {
      setOffset(x, 0);
    }
    return this;
  }
  public Visual above() {
    return above(0);
  }

  public Visual above(int y) {
    setLayout(Layout.TOP);
    if (y != 0) {
      setOffset(0, -y);
    }
    return this;
  }

  public Visual below() {
    return below(0);
  }

  public Visual below(int y) {
    setLayout(Layout.BOTTOM);
    if (y != 0) {
      setOffset(0, y);
    }
    return this;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="AutoLayout">
  AutoLayout autolayout = null;

  class AutoLayout implements ComponentListener {

    private Visual targetComponent;

    AutoLayout(Visual targetComponent) {
      this.setTargetComponent(targetComponent);
      //targetComponent.addComponentListener(this);
    }

    public void setTargetComponent(Visual targetComponent) {
      this.targetComponent = targetComponent;
    }

    public Visual getTargetComponent() {
      return targetComponent;
    }

    void update() {
      //Debug.info("Update caused by leader:" + this);
      // TODO calculate necesary region to udpate
      //         if (getParent()!=null){
      //
      //            if (getParent().getParent()!=null){
      //               getParent().getParent().repaint();
      //            }else{
      //               getParent().repaint();
      //            }
      //         }
    }

    void stop() {
      // targetComponent.removeComponentListener(this);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
      //         if (isAutoVisibilityEnabled()){
      //            setVisible(false);
      //            update();
      //         }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      //         if (isAutoMoveEnabled())
      //            update();
    }

    @Override
    public void componentResized(ComponentEvent e) {
      //         if (isAutoResizeEnabled())
      //            update();
    }

    @Override
    public void componentShown(ComponentEvent e) {
      //         if (isAutoVisibilityEnabled()){
      //            setVisible(true);
      //            update();
      //         }
    }
  }

  class AutoLayoutBySide extends AutoLayout {
    Layout side;

    AutoLayoutBySide(Visual targetComponent, Layout side) {
      super(targetComponent);
      this.side = side;
    }

    @Override
    void update() {
      if (side == Layout.FOLLOWERS) {
        // set to the total bounds of the other followers
        // first set its bounds to be equal to the targets, so that
        // its current bounds won't have effect on the calculation
        // of the total bounds
        setBounds(getTargetComponent().getBounds());
        // then this call will gives us the total bounds of the
        // rest of the followers
        Rectangle totalBounds = getTargetComponent().getFollowerBounds();
        totalBounds.grow(5, 5);
        setBounds(totalBounds);
      } else {
        setLocationRelativeToRegion(getTargetComponent().getRegion(), side);
      }
      super.update();
    }
  }

  class AutoLayoutByMovement extends AutoLayout {
    // previous known location of the target this component follows

    int x;
    int y;
    Point targetLocation;

    AutoLayoutByMovement(Visual targetComponent) {
      super(targetComponent);
      targetLocation = new Point(targetComponent.getActualLocation());
      this.x = targetComponent.getX();
      this.y = targetComponent.getY();
    }

    @Override
    public void update() {

      //Debug.info("auto moved by leader");

      Point newTargetLocation = getTargetComponent().getActualLocation();
      int dx = newTargetLocation.x - targetLocation.x;
      int dy = newTargetLocation.y - targetLocation.y;
      targetLocation = newTargetLocation;
      Point actualLocation = getActualLocation();
      actualLocation.x += dx;
      actualLocation.y += dy;

      setActualLocation(actualLocation.x, actualLocation.y);
    }
  }

  class AutoLayoutByOffset extends AutoLayout {

    int offsetx;
    int offsety;

    AutoLayoutByOffset(Visual targetComponent, int offsetx, int offsety) {
      super(targetComponent);
      this.offsetx = offsetx;
      this.offsety = offsety;
    }

    @Override
    void update() {
      setOffset(offsetx, offsety);
      Region region = new Region(leader.getBounds());
      setLocationRelativeToRegion(region, Layout.ORIGIN);
      super.update();
    }
  }

  class AutoLayoutByRatio extends AutoLayout {

    float x, y;

    AutoLayoutByRatio(Visual targetComponent, float x, float y) {
      super(targetComponent);
      this.x = x;
      this.y = y;
    }

    @Override
    void update() {
      Region region = new Region(getTargetComponent().getBounds());
      setHorizontalAlignmentWithRegion(region, x);
      setVerticalAlignmentWithRegion(region, y);
      super.update();
    }
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Position">
  public <RCPS> Visual setTarget(RCPS target) {
    if (target instanceof Region) {
      targetRegion = (Region) target;
    } else if (target instanceof Visual) {
      targetComponent = (Visual) target;
    } else if (target instanceof Pattern) {
      targetPattern = (Pattern) target;
    } else if (target instanceof String) {
      targetPattern = new Pattern((String) target);
    }
    if (targetPattern != null) {
      targetComponent = new SxAnchor(targetPattern);
      currentGuide.addToFront(targetComponent);
      setLayout(layout);
    }
    updateComponent();
    return this;
  }

  public Visual setLocationRelative(Layout side) {
    if (targetRegion != null) {
      setLocationRelativeToRegion(targetRegion, side);
    } else if (targetComponent != null) {
      setLocationRelativeToComponent(targetComponent, side);
    }
    return this;
  }

  public void setLocationRelativeToComponent(Visual comp, Layout side) {
    if (autolayout != null) {
      autolayout.stop();
    }
    comp.addFollower(this);
    autolayout = new AutoLayoutBySide(comp, side);
    autolayout.update();
  }

  public void setLocationRelativeToComponent(Visual comp, int offsetx, int offsety) {
    if (autolayout != null) {
      autolayout.stop();
    }
    comp.addFollower(this);
    autolayout = new AutoLayoutByOffset(comp, offsetx, offsety);
    autolayout.update();
  }

  public void setLocationRelativeToComponent(Visual comp, float relativeX, float relativeY) {
    if (autolayout != null) {
      autolayout.stop();
    }
    autolayout = new AutoLayoutByRatio(comp, relativeX, relativeY);
    autolayout.update();
  }

  public void setLocationRelativeToComponent(Visual leader) {
    if (autolayout != null) {
      autolayout.stop();
    }
    leader.addFollower(this);
    autolayout = new AutoLayoutByMovement(leader);
    autolayout.update();
  }

  public void setLocationRelativeToPoint(Point point, Layout side) {
    Rectangle bounds = getActualBounds();
    // TODO implement other positioning parameters
    if (side == Layout.CENTER) {
      setActualLocation(point.x - bounds.width / 2, point.y - bounds.height / 2);
    }
  }

  public Visual setLocationRelativeToRegion(Region region, Layout side) {
    Region orgReg = new Region(region);
    if (margin != null) {
      region.x -= margin.left;
      region.y -= margin.top;
      region.w += (margin.left + margin.right);
      region.h += (margin.top + margin.bottom);
      targetRegion = region;
      margin = null;
    }
    if (offsetx > 0 || offsety > 0) {
      region.x += offsetx;
      region.y += offsety;
      targetRegion = region;
      offsetx = 0;
      offsety = 0;
    }
    layout = side;
    int height = getActualHeight();
    int width = getActualWidth();
    if (side == Layout.TOP) {
      setActualLocation(region.x + region.w / 2 - width / 2, region.y - height);
    } else if (side == Layout.BOTTOM) {
      setActualLocation(region.x + region.w / 2 - width / 2, region.y + region.h);
    } else if (side == Layout.LEFT) {
      setActualLocation(region.x - width, region.y + region.h / 2 - height / 2);
    } else if (side == Layout.RIGHT) {
      setActualLocation(region.x + region.w, region.y + region.h / 2 - height / 2);
    } else if (side == Layout.INSIDE) {
      setActualLocation(region.x + region.w / 2 - width / 2, region.y + region.h / 2 - height / 2);
    } else if (side == Layout.OVER) {
      setActualBounds(region.getRect());
    } else if (side == Layout.ORIGIN) {
      setActualLocation(region.x, region.y);
    }
    return this;
  }

  public void setHorizontalAlignmentWithRegion(Region region, float f) {
    int x0 = region.x;
    int x1 = region.x + region.w - getActualWidth();
    int x = (int) (x0 + (x1 - x0) * f);
    setActualLocation(x, getActualLocation().y);
  }

  public void setVerticalAlignmentWithRegion(Region region, float f) {
    int y0 = region.y;
    int y1 = region.y + region.h - getActualHeight();
    int y = (int) (y0 + (y1 - y0) * f);
    setActualLocation(getActualLocation().x, y);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Follower">
  private ArrayList<Visual> followers = new ArrayList<Visual>();
  Visual leader;

  public void removeFromLeader() {
    if (leader != null) {
      leader.removeFollower(this);
    }
    leader = null;
  }

  public void addFollower(Visual sklComp) {
    // force the follower to have the same visibility
    sklComp.setVisible(isVisible());
    sklComp.setOpacity(opacity);
    if (followers.indexOf(sklComp) < 0) {
      // if this component is not already a follower
      // add it to the list of follower
      followers.add(sklComp);
      // remove its previous leader
      sklComp.removeFromLeader();
      // set its new leader to self
      sklComp.leader = this;
    }
  }

  private void updateAllFollowers() {
    for (Visual sklComp : getFollowers()) {
      if (sklComp.autolayout != null) {
        sklComp.autolayout.update();
      }
    }
  }

  @Override
  public void setVisible(boolean visible) {
    for (Visual follower : getFollowers()) {
      follower.setVisible(visible);
    }
    super.setVisible(visible);
  }

  //<editor-fold defaultstate="collapsed" desc="not used">
  //   @Override
  //   public void setLocation(Point location){
  //      setLocation(location.x, location.y);
  //   }
  //   @Override
  //   public void setLocation(int x, int y){
  //
  ////      if (shadowRenderer != null){
  ////         x -= 8;
  ////         y -= 8;
  ////      }
  //
  //      getActualBounds().x = (int) (x/zoomLevel);
  //      getActualBounds().y = (int) (y/zoomLevel);
  //
  //      super.setLocation(x,y);
  //      updateAllFollowers();
  //   }
  //
  //   @Override
  //   public void setBounds(int x, int y, int w, int h){
  //
  //      Rectangle bounds = new Rectangle(x,y,w,h);
  //
  //      actualBounds = new Rectangle(bounds);
  //      actualBounds.x /= zoomLevel;
  //      actualBounds.y /= zoomLevel;
  //      actualBounds.width /= zoomLevel;
  //      actualBounds.height /= zoomLevel;
  //
  //      for (Visual sklComp : getFollowers()){
  //         if (sklComp.autolayout != null){
  //            sklComp.autolayout.update();
  //         }
  //      }
  //      super.setBounds(x,y,w,h);
  //   }
  //   @Override
  //   public void setBounds(Rectangle bounds){
  //
  //      setActualBounds(new Rectangle(bounds));
  //      getActualBounds().x /= zoomLevel;
  //      getActualBounds().y /= zoomLevel;
  //      getActualBounds().width /= zoomLevel;
  //      getActualBounds().height /= zoomLevel;
  //
  //      super.setBounds(bounds);
  //      updateAllFollowers();
  //   }
  //   @Override
  //   public void setSize(int width, int height){
  //      getActualBounds().width = (int) (width/zoomLevel);
  //      getActualBounds().height = (int) (height/zoomLevel);
  //
  //      if (hasShadow()){
  //         width += 20;
  //         height += 20;
  //      }
  //
  //      super.setSize(width, height);
  ////      updateAllFollowers();
  //   }
  //
  //   @Override
  //   public void setSize(Dimension size){
  ////      getActualBounds().width = (int) (size.width/zoomLevel);
  ////      getActualBounds().height = (int) (size.height/zoomLevel);
  //
  ////      if (hasShadow()){
  ////         size.width += 20;
  ////         size.height += 20;
  ////      }
  //
  //      super.setSize(size);
  ////      updateAllFollowers();
  //   }
  //</editor-fold>

  public ArrayList<Visual> getFollowers() {
    return followers;
  }

  public Visual getLeader() {
    return leader;
  }

  public void removeFollower(Visual comp) {
    followers.remove(comp);
  }

  public Rectangle getFollowerBounds() {
    // find the total bounds of all the components
    Rectangle bounds = new Rectangle(getBounds());
    for (Visual sklComp : getFollowers()) {
      bounds.add(sklComp.getBounds());
    }
    return bounds;
  }
//</editor-fold>

  public void removeFrom(Container container) {
    for (Visual follower : getFollowers()) {
      follower.removeFrom(container);
    }
    container.remove(this);
  }

  @Override
  public String toString() {
    return "" + getClass() + " " + "[actualBounds=" + getActualBounds() + "]";
  }

  @Override
  public Object clone() {
    Visual clone;
    try {
      clone = (Visual) super.clone();

      // do not clone references to other components
      clone.followers = new ArrayList<Visual>();
      clone.removeFromLeader();
      clone.actualBounds = new Rectangle(actualBounds);
      clone.autolayout = null;
      //clone.connectors = new ArrayList<Connector>();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString());
    }
  }
}
