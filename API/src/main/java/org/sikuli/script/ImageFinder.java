/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

/**
 * EXPERIMENTAL --- INTERNAL USE ONLY<br>
 *   is not official API --- will not be in version 2
 */
public class ImageFinder extends Finder {

  static RunTime runTime = RunTime.get();

  private static String me = "ImageFinder: ";
  private static int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }
  private boolean isImageFinder = true;
  protected boolean isImage = false;
  protected Region region = null;
  protected boolean isRegion = false;
  protected IScreen screen = null;
  protected boolean isScreen = false;
  protected int offX, offY;
  protected long MaxTimePerScan;
  private Image bImage = null;
  protected Mat base = new Mat();
  private double waitingTime = Settings.AutoWaitTimeout;
  private int minChanges;
  private ImageFind firstFind = null;
  private boolean isReusable = false;
  protected boolean isMultiFinder = false;

  public ImageFinder() {
    init(null, null, null);
  }

  public ImageFinder(Image base) {
    init(base, null, null);
  }

  public ImageFinder(IScreen scr) {
    init(null, scr, null);
  }

  public ImageFinder(Region reg) {
    init(null, null, reg);
  }

  protected ImageFinder(Mat base) {
    log(3, "init");
    reset();
    this.base = base;
    isImage = true;
    log(3, "search in: \n%s", base);
  }

  private void init(Image base, IScreen scr, Region reg) {
    log(3, "init");
    if (base != null) {
      setImage(base);
    } else if (scr != null) {
      setScreen(scr);
    } else if (reg != null) {
      setRegion(reg);
    }
  }

  private void reset() {
    firstFind = null;
    isImage = false;
    isScreen = false;
    isRegion = false;
    screen = null;
    region = null;
    bImage = null;
    base = new Mat();
  }

  @Override
  public void destroy() {
    reset();
  }

  public void setIsMultiFinder() {
    base = new Mat();
    isMultiFinder = true;
  }

  public boolean setImage(Image base) {
    reset();
    if (base.isValid()) {
      bImage = base;
      this.base = Image.createMat(base.get());
      isImage = true;
      log(3, "search in: \n%s", base.get());
    }
    return isImage;
  }

  public boolean isImage() {
    return isImage;
  }

  protected void setBase(BufferedImage bImg) {
    log(3, "search in: \n%s", bImg);
    base = Image.createMat(bImg);
  }

  public boolean setScreen(IScreen scr) {
    reset();
    if (scr != null) {
      screen = scr;
      isScreen = true;
      setScreenOrRegion(scr);
    }
    return isScreen;
  }

  public boolean setRegion(Region reg) {
    reset();
    if (reg != null) {
      region = reg;
      isRegion = true;
      setScreenOrRegion(reg);
    }
    return isRegion;
  }

  private void setScreenOrRegion(Object reg) {
    Region r = (Region) reg;
    MaxTimePerScan = (int) (1000.0 / r.getWaitScanRate());
    offX = r.x;
    offY = r.y;
    log(3, "search in: \n%s", r);
  }

  public void setFindTimeout(double t) {
    waitingTime = t;
  }

  public boolean isValid() {
    if (!isImage && !isScreen && !isRegion) {
      log(-1, "not yet initialized (not valid Image, Screen nor Region)");
      return false;
    }
    return true;
  }

  @Override
  public String find(Image img) {
    if (null == imageFind(img)) {
      return null;
    } else {
      return "--fromImageFinder--";
    }
  }

  @Override
  public String find(String filenameOrText) {
    if (null == imageFind(filenameOrText)) {
      return null;
    } else {
      return "--fromImageFinder--";
    }
  }

  @Override
  public String find(Pattern pat) {
    if (null == imageFind(pat)) {
      return null;
    } else {
      return "--fromImageFinder--";
    }
  }

  @Override
  public String findText(String text) {
    log(-1, "findText: not yet implemented");
    return null;
  }

  public <PSI> ImageFind search(PSI probe, Object... args) {
    isReusable = true;
    return imageFind(probe, args);
  }

  protected <PSI> ImageFind findInner(PSI probe, double sim) {
    ImageFind newFind = new ImageFind();
    newFind.setIsInnerFind();
    newFind.setSimilarity(sim);
    if (!newFind.checkFind(this, probe)) {
      return null;
    }
    firstFind = newFind;
    if (newFind.isValid()) {
      return newFind.doFind();
    }
    return null;
  }

  private <PSI> ImageFind imageFind(PSI probe, Object... args) {
    Debug.enter(me + ": find: %s", probe);
    ImageFind newFind = new ImageFind();
    newFind.setFindTimeout(waitingTime);
    if (!newFind.checkFind(this, probe, args)) {
      return null;
    }
    if (newFind.isValid() && !isReusable && firstFind == null) {
      firstFind = newFind;
    }
    ImageFind imgFind = newFind.doFind();
    log(lvl, "find: success: %s", imgFind.get());
    return imgFind;
  }

  public <PSI> ImageFind searchAny(PSI probe, Object... args) {
    Debug.enter(me + ": findAny: %s", probe);
    ImageFind newFind = new ImageFind();
    newFind.setFinding(ImageFind.FINDING_ANY);
    isReusable = true;
    if (!newFind.checkFind(this, probe, args)) {
      return null;
    }
    if (newFind.isValid() && !isReusable && firstFind == null) {
      firstFind = newFind;
    }
    ImageFind imgFind = newFind.doFind();
    log(lvl, "find: success: %s", imgFind.get());
    return imgFind;
  }

  public <PSI> ImageFind searchSome(PSI probe, Object... args) {
    return searchSome(probe, ImageFind.SOME_COUNT, args);
  }

  public <PSI> ImageFind searchSome(PSI probe, int count, Object... args) {
    isReusable = true;
    return imageFindAll(probe, ImageFind.BEST_FIRST, count, args);
  }

  @Override
  public String findAll(Image img) {
    if (null == imageFindAll(img, ImageFind.BEST_FIRST, 0)) {
      return null;
    } else {
      return "--fromImageFinder--";
    }
  }

  @Override
  public String findAll(String filenameOrText) {
    if (null == imageFindAll(filenameOrText, ImageFind.BEST_FIRST, 0)) {
      return null;
    } else {
      return "--fromImageFinder--";
    }
  }

  @Override
  public String findAll(Pattern pat) {
    if (null == imageFindAll(pat, ImageFind.BEST_FIRST, 0)) {
      return null;
    } else {
      return "--fromImageFinder--";
    }
  }

  public <PSI> ImageFind searchAll(PSI probe, Object... args) {
    isReusable = true;
    return imageFindAll(probe, ImageFind.BEST_FIRST, 0, args);
  }

  public <PSI> ImageFind searchAll(PSI probe, int sorted, Object... args) {
    isReusable = true;
    return imageFindAll(probe, sorted, 0, args);
  }

  private <PSI> ImageFind imageFindAll(PSI probe, int sorted, int count, Object... args) {
    Debug.enter(me + ": findAny: %s", probe);
    ImageFind newFind = new ImageFind();
    newFind.setFinding(ImageFind.FINDING_ALL);
    newFind.setSorted(sorted);
    if (count > 0) {
      newFind.setCount(count);
    }
   if (!newFind.checkFind(this, probe, args)) {
      return null;
    }
    if (newFind.isValid() && !isReusable && firstFind == null) {
      firstFind = newFind;
    }
    ImageFind imgFind = newFind.doFind();
    log(lvl, "find: success: %s", imgFind.get());
    return imgFind;
   }

  public boolean hasChanges(Mat current) {
    int PIXEL_DIFF_THRESHOLD = 5;
    int IMAGE_DIFF_THRESHOLD = 5;
    Mat bg = new Mat();
    Mat cg = new Mat();
    Mat diff = new Mat();
    Mat tdiff = new Mat();

    Imgproc.cvtColor(base, bg, Imgproc.COLOR_BGR2GRAY);
    Imgproc.cvtColor(current, cg, Imgproc.COLOR_BGR2GRAY);
    Core.absdiff(bg, cg, diff);
    Imgproc.threshold(diff, tdiff, PIXEL_DIFF_THRESHOLD, 0.0, Imgproc.THRESH_TOZERO);
    if (Core.countNonZero(tdiff) <= IMAGE_DIFF_THRESHOLD) {
      return false;
    }

    Imgproc.threshold(diff, diff, PIXEL_DIFF_THRESHOLD, 255, Imgproc.THRESH_BINARY);
    Imgproc.dilate(diff, diff, new Mat());
    Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
    Imgproc.morphologyEx(diff, diff, Imgproc.MORPH_CLOSE, se);

    List<MatOfPoint> points = new ArrayList<MatOfPoint>();
    Mat contours = new Mat();
    Imgproc.findContours(diff, points, contours, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    int n = 0;
    for (Mat pm: points) {
      log(lvl, "(%d) %s", n++, pm);
      printMatI(pm);
    }
    log(lvl, "contours: %s", contours);
    printMatI(contours);
    return true;
  }

  private static void printMatI(Mat mat) {
    int[] data = new int[mat.channels()];
    for (int r = 0; r < mat.rows(); r++) {
      for (int c = 0; c < mat.cols(); c++) {
        mat.get(r, c, data);
        log(lvl, "(%d, %d) %s", r, c, Arrays.toString(data));
      }
    }
  }

  public void setMinChanges(int min) {
    minChanges = min;
  }

  @Override
  public boolean hasNext() {
    if (null != firstFind) {
      return firstFind.hasNext();
    }
    return false;
  }

  @Override
  public Match next() {
    if (firstFind != null) {
      return firstFind.next();
    }
    return null;
  }

  @Override
  public void remove() {
  }
}
