package org.sikuli.script.support.recorder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.script.Finder;
import org.sikuli.script.Finder.Finder2;
import org.sikuli.script.Image;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Key;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.support.KeyboardLayout;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.recorder.actions.ClickAction;
import org.sikuli.script.support.recorder.actions.IRecordedAction;
import org.sikuli.script.support.recorder.actions.TypeKeyAction;
import org.sikuli.script.support.recorder.actions.TypeTextAction;
import org.sikuli.script.support.recorder.actions.WaitClickAction;

public class RecordedEventsFlow {

  private static final int START_SIZE = 20;
  private static final int INCREASE_SIZE = 10;
  private static final int MAX_FEATURES = 200;
  private static final double FEATURE_INCREASE_RATIO = 0.8;
  private static final int FEATURE_IMAGE_MARGIN = 5;

  private TreeMap<Long, NativeInputEvent> events = new TreeMap<>();
  private TreeMap<Long, String> screenshots = new TreeMap<>();

  List<Character> modifiers = new ArrayList<>();
  StringBuilder typedText = new StringBuilder();

  static {
    RunTime.loadLibrary(RunTime.libOpenCV);
  }

  public RecordedEventsFlow() {

  }

  public void addEvent(NativeInputEvent event) {
    synchronized (this) {
      events.put(System.currentTimeMillis(), event);
    }
  }

  public void addScreenshot(String screenshotFilePath) {
    synchronized (this) {
      screenshots.put(System.currentTimeMillis(), screenshotFilePath);
    }
  }

  public void clear() {
    synchronized (this) {
      events.clear();
      screenshots.clear();
    }
  }

  public List<IRecordedAction> compile() {
    synchronized (this) {
      List<org.sikuli.script.support.recorder.actions.IRecordedAction> actions = new LinkedList<>();
      modifiers.clear();
      typedText = new StringBuilder();

      for (Map.Entry<Long, NativeInputEvent> entry : events.entrySet()) {
        Long time = entry.getKey();
        NativeInputEvent event = entry.getValue();

        IRecordedAction action = null;

        if (event instanceof NativeKeyEvent) {
          action = handleKeyEvent(time, (NativeKeyEvent) event);
        } else if (event instanceof NativeMouseEvent) {
          action = handleMouseEvent(time, (NativeMouseEvent) event);
        }

        if (action != null) {
          actions.add(action);
        }
      }

      if (!actions.isEmpty()) {
        actions.remove(actions.size() - 1);
      }

      return actions;
    }
  }

  private IRecordedAction handleKeyEvent(Long time, NativeKeyEvent event) {
    Character character = KeyboardLayout.toChar(event, new Character[0]);
    Character characterWithModifiers = KeyboardLayout.toChar(event, modifiers.toArray(new Character[modifiers.size()]));

    Long nextEventTime = findNextPressedKeyEventTime(time);
    NativeKeyEvent nextKeyEvent = null;
    Character nextEventCharacter = null;
    String nextEventText = null;
    boolean nextEventIsModifier = false;

    if (nextEventTime != null) {
      nextKeyEvent = (NativeKeyEvent) events.get(nextEventTime);
      nextEventCharacter = KeyboardLayout.toChar(nextKeyEvent, new Character[0]);
      nextEventText = getKeyText(nextEventCharacter);
      nextEventIsModifier = Key.isModifier(nextEventCharacter);
    }

    if (character != null) {
      boolean isModifier = Key.isModifier(character);

      String keyText = getKeyText(character);

      if (NativeKeyEvent.NATIVE_KEY_PRESSED == event.getID()) {
        if (isModifier && nextEventTime != null) {
          modifiers.add(character);
        } else {
          keyText = getKeyText(characterWithModifiers);
          String[] modifiersTexts = getModifierTexts();

          if (keyText.length() > 1) {
            return new TypeKeyAction(keyText, modifiersTexts);
          }

          if (!modifiers.isEmpty() && characterWithModifiers == character) {
            return new TypeTextAction(keyText, modifiersTexts);
          }

          typedText.append(keyText);

          if (nextEventText == null || (nextEventText.length() > 1 && !nextEventIsModifier)) {
            String text = typedText.toString();
            typedText = new StringBuilder();
            return new TypeTextAction(text, new String[0]);
          }
        }
      } else if (NativeKeyEvent.NATIVE_KEY_RELEASED == event.getID()) {
        if (isModifier) {
          modifiers.remove(character);
        }
      }
    }

    return null;
  }

  private String[] getModifierTexts() {
    return modifiers.stream().map((m) -> getKeyText(m)).collect(Collectors.toList())
        .toArray(new String[modifiers.size()]);
  }

  private Long findNextPressedKeyEventTime(Long time) {
    Long nextEventTime = events.ceilingKey(time + 1);

    if (nextEventTime == null || nextEventTime - time > 3000) {
      return null;
    }

    NativeInputEvent event = events.get(nextEventTime);

    if (!(event instanceof NativeKeyEvent)) {
      return null;
    }

    if (event.getID() == NativeKeyEvent.NATIVE_KEY_PRESSED) {
      return nextEventTime;
    }

    return findNextPressedKeyEventTime(nextEventTime);
  }

  private String getKeyText(char ch) {
    if (ch == ' ') {
      return " ";
    }

    for (Field field : Key.class.getDeclaredFields()) {
      try {
        if (field.get(Key.class).equals("" + ch)) {
          return "Key." + field.getName();
        }
      } catch (IllegalArgumentException | IllegalAccessException e) {
        // ignore
      }
    }
    return "" + ch;
  }

  private IRecordedAction handleMouseEvent(Long time, NativeMouseEvent event) {
    if (NativeMouseEvent.NATIVE_MOUSE_PRESSED == event.getID()) {
      Image image = this.findRelevantImage(time, event);

      if (image != null) {
        File file = new File(ImagePath.getBundlePath() + File.separator + time + ".png");

        try {
          ImageIO.write(image.get(), "PNG", file);
        } catch (IOException e) {
          e.printStackTrace();
        }

        Pattern pattern = new Pattern(file.getAbsolutePath());
        pattern.targetOffset(image.getOffset());


        Long lastNonMouseMoveEventTime = events.firstKey();

        List<Map.Entry<Long, NativeInputEvent>> previousEvents = new ArrayList<>(events.headMap(time).entrySet());
        Collections.reverse(previousEvents);

        for (Map.Entry<Long, NativeInputEvent> entry : previousEvents) {
          if (entry.getValue().getID() != NativeMouseEvent.NATIVE_MOUSE_MOVED
              && entry.getValue().getID() != NativeMouseEvent.NATIVE_MOUSE_DRAGGED) {
            lastNonMouseMoveEventTime = entry.getKey();
            break;
          }
        }

        Mat lastNonMouseMoveScreenshot = readCeilingScreenshot(lastNonMouseMoveEventTime);
        Finder finder = new Finder(Finder2.getBufferedImage(lastNonMouseMoveScreenshot));

        finder.find(image);

        List<Match> matches = finder.getList();

        boolean wasHereBeforeMouseMove = false;
        for (Match match : matches) {
          if (match.getScore() > 0.9) {
            wasHereBeforeMouseMove = true;
            break;
          }
        }

        if (wasHereBeforeMouseMove) {
          return new ClickAction(pattern);
        } else {
          return new WaitClickAction(pattern, (int) Math.ceil((time - lastNonMouseMoveEventTime) / 1000d * 2));
        }
      }

    } else if (NativeMouseEvent.NATIVE_MOUSE_RELEASED == event.getID()) {

    } else if (NativeMouseEvent.NATIVE_MOUSE_CLICKED == event.getID()) {

    } else if (NativeMouseEvent.NATIVE_MOUSE_MOVED == event.getID()) {

    }
    return null;
  }

  private Mat readFloorScreenshot(Long time) {
    return Imgcodecs.imread(screenshots.floorEntry(time).getValue());
  }

  private Mat readCeilingScreenshot(Long time) {
    return Imgcodecs.imread(screenshots.ceilingEntry(time).getValue());
  }

  private Image findRelevantImage(long time, NativeMouseEvent event) {
    if (screenshots.floorKey(time) != null) {
      int eventX = event.getX();
      int eventY = event.getY();

      Mat screenshot = readFloorScreenshot(time);

      Mat edges = new Mat();

      Imgproc.Canny(screenshot, edges, 100, 200);
      Core.bitwise_not(edges, edges);

      int offset = START_SIZE / 2;
      int currentTop = eventY - offset;
      int currentRight = eventX + offset;
      int currentBottom = eventY + offset;
      int currentLeft = eventX - offset;

      for (int i = 1; i <= 40; i++) {
        int currentCount = findGoodFeatures(edges, currentTop, currentRight, currentBottom, currentLeft);

        if (currentCount > MAX_FEATURES) {
          break;
        }

        int currentWidth = currentRight - currentLeft;
        int currentHeight = currentBottom - currentTop;
        int currentArea = currentWidth * currentHeight;

        int top = currentTop - INCREASE_SIZE;
        int increasedTopHeight = currentBottom - top;
        int increasedTopCount = findGoodFeatures(edges, top, currentRight, currentBottom, currentLeft);
        double increasedTopFactor = (double) (currentWidth * increasedTopHeight) / currentArea;

        int right = currentRight + INCREASE_SIZE;
        int increasedRightWidth = right - currentLeft;
        int increasedRightCount = findGoodFeatures(edges, currentTop, right, currentBottom, currentLeft);
        double increasedRightFactor = (double) (increasedRightWidth * currentHeight) / currentArea;

        int bottom = currentBottom + INCREASE_SIZE;
        int increasedBottomHeight = bottom - currentTop;
        int increasedBottomCount = findGoodFeatures(edges, currentTop, currentRight, bottom, currentLeft);
        double increasedBottomFactor = (double) (currentWidth * increasedBottomHeight) / currentArea;

        int left = currentLeft - INCREASE_SIZE;
        int increasedLeftWidth = currentRight - left;
        int increasedLeftCount = findGoodFeatures(edges, currentTop, currentRight, currentBottom, left);
        double increasedLeftFactor = (double) (increasedLeftWidth * currentHeight) / currentArea;

        if (increasedTopCount > currentCount * Math.max(1, increasedTopFactor * FEATURE_INCREASE_RATIO)) {
          currentTop = top;
        }

        if (increasedRightCount > currentCount * Math.max(1, increasedRightFactor * FEATURE_INCREASE_RATIO)) {
          currentRight = right;
        }

        if (increasedBottomCount > currentCount * Math.max(1, increasedBottomFactor * FEATURE_INCREASE_RATIO)) {
          currentBottom = bottom;
        }

        if (increasedLeftCount > currentCount * Math.max(1, increasedLeftFactor * FEATURE_INCREASE_RATIO)) {
          currentLeft = left;
        }

        currentCount = findGoodFeatures(edges, currentTop, currentRight, currentBottom, currentLeft);

        if (currentCount < 10) {
          currentTop -= INCREASE_SIZE;
          currentRight += INCREASE_SIZE;
          currentBottom += INCREASE_SIZE;
          currentLeft -= INCREASE_SIZE;
        }
      }

      currentTop -= FEATURE_IMAGE_MARGIN;
      currentRight += FEATURE_IMAGE_MARGIN;
      currentBottom += FEATURE_IMAGE_MARGIN;
      currentLeft -= FEATURE_IMAGE_MARGIN;

      currentTop = Math.max(0, currentTop);
      currentRight = Math.min(screenshot.cols() - 1, currentRight);
      currentBottom = Math.min(screenshot.rows() - 1, currentBottom);
      currentLeft = Math.max(0, currentLeft);

      Mat part = new Mat(screenshot,
          new Rect(currentLeft, currentTop, currentRight - currentLeft, currentBottom - currentTop));

      Image image = new Image(Finder2.getBufferedImage(part));
      image.setOffset(new Location(eventX - (currentLeft + (currentRight - currentLeft) / 2), eventY - (currentTop + (currentBottom - currentTop) / 2)));

      return image;
    }

    return null;
  }

  private int findGoodFeatures(Mat img, int top, int right, int bottom, int left) {
    top = Math.max(0, top);
    right = Math.min(img.cols() - 1, right);
    bottom = Math.min(img.rows() - 1, bottom);
    left = Math.max(0, left);
    Mat sub = new Mat(img, new Rect(left, top, right - left, bottom - top));

    MatOfPoint features = new MatOfPoint();
    Imgproc.goodFeaturesToTrack(sub, features, 0, 0.001, 1.0);

    return features.toArray().length;
  }

}
