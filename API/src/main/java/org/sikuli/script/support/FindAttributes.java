/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.sikuli.basics.Settings;
import org.sikuli.script.Element;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;

import java.awt.*;
import java.util.List;

public class FindAttributes {

  private boolean gray = false;

  public boolean gray() {
    return gray;
  }

  Image target;

  public Image target() {
    return target;
  }

  Image originalTarget;

  public Image originalTarget() {
    return target;
  }

  Mat what;

  public Mat what() {
    return what;
  }

  Mat mask = new Mat();

  public Mat mask() {
    return mask;
  }

  public FindAttributes(Object searchTarget) {
    if (searchTarget instanceof Pattern) {
      Pattern pattern = (Pattern) searchTarget;
      originalTarget = pattern.getImage();
      target = new Image(pattern.getImage());
      target.similarity(pattern.getSimilar());
      target.offset(pattern.getTargetOffset());
      target.resize(pattern.getResize());
      target.waitAfter(pattern.waitAfter());
      target.mask(pattern.getMask());
    } else {
      target = new Image(searchTarget);
      originalTarget = target;
    }
    what = target.getContent();
    if (target.hasURL()) {
      what = possibleImageResizeOrCallback(target, what);
    }
    if (target.isMasked()) {
      List<Mat> mats = SXOpenCV.extractMask(what, false);
      what = mats.get(0);
      mask = mats.get(1);
    } else {
      if (what.channels() == 4) {
        List<Mat> mats = SXOpenCV.extractMask(what, true);
        what = mats.get(0);
        mask = mats.get(1);
      }
      if (target.hasMask()) {
        mask = possibleImageResizeMask(target, what);
      }
    }
    if (what.channels() == 1) {
      gray = true;
    }
    Mat finalContent = new Mat();
    if (mask.empty()) {
      finalContent = what;
    } else {
      Core.multiply(what, mask, finalContent);
    }

    MatOfDouble pMean = new MatOfDouble();
    MatOfDouble pStdDev = new MatOfDouble();
    Core.meanStdDev(finalContent, pMean, pStdDev);

    double sum = 0.0;
    double[] arr = pStdDev.toArray();
    for (int i = 0; i < arr.length; i++) {
      sum += arr[i];
    }
    target.stdDev(sum);
    target.plain(sum < SXOpenCV.MIN_THRESHHOLD);

    sum = 0.0;
    arr = pMean.toArray();
    int[] cvMeanColor = new int[arr.length];
    for (int i = 0; i < arr.length; i++) {
      cvMeanColor[i] = (int) arr[i];
      sum += arr[i];
    }
    target.mean(sum);
    target.black(sum < SXOpenCV.MIN_THRESHHOLD && target.plain());

    if (cvMeanColor.length > 1) {
      target.white(SXOpenCV.isColorEqual(cvMeanColor, Color.WHITE));
      target.meanColor(new Color(cvMeanColor[2], cvMeanColor[1], cvMeanColor[0]));
    }
  }

  private Mat possibleImageResizeOrCallback(Image image, Mat what) {
    Mat originalContent = what;
    if (Settings.ImageCallback != null) {
      Mat contentResized = SXOpenCV.makeMat(Settings.ImageCallback.callback(image), false);
      if (!contentResized.empty()) {
        return contentResized;
      }
    } else {
      double factor = image.resize() == 1 ? Settings.AlwaysResize : image.resize();
      if (factor > 0.1 && factor != 1) {
        return SXOpenCV.cvResize(originalContent.clone(), factor, Image.Interpolation.CUBIC);
      }
    }
    return originalContent;
  }

  private Mat possibleImageResizeMask(Image image, Mat what) {
    Mat mask = image.getMask().getContent();
    double factor = mask.width() / what.width();
    if (factor > 0.1 && factor != 1) {
      mask = SXOpenCV.cvResize(mask.clone(), factor, Image.Interpolation.CUBIC);
    }
    List<Mat> mats = SXOpenCV.extractMask(mask, false);
    return mats.get(1);
  }

  public Match getMatchLastSeen() {
    return Element.getMatchLastSeen(target);
  }
}
