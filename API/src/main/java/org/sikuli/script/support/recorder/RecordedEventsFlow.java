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
import org.sikuli.script.support.recorder.actions.DoubleClickAction;
import org.sikuli.script.support.recorder.actions.DragDropAction;
import org.sikuli.script.support.recorder.actions.IRecordedAction;
import org.sikuli.script.support.recorder.actions.RightClickAction;
import org.sikuli.script.support.recorder.actions.TypeKeyAction;
import org.sikuli.script.support.recorder.actions.TypeTextAction;
import org.sikuli.script.support.recorder.actions.WaitAction;

public class RecordedEventsFlow {

  private static final int START_SIZE = 20;
  private static final int X_INCREASE_SIZE = 15;
  private static final int Y_INCREASE_SIZE = 5;
  private static final int MAX_FEATURES = 200;
  private static final double FEATURE_INCREASE_RATIO = 0.8;
  private static final int FEATURE_IMAGE_MARGIN = 5;
  private static final int DOUBLE_CLICK_TIME = 300;

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

        if (event instanceof NativeKeyEvent) {
          actions.addAll(handleKeyEvent(time, (NativeKeyEvent) event));
        } else if (event instanceof NativeMouseEvent) {
          actions.addAll(handleMouseEvent(time, (NativeMouseEvent) event));
        }
      }

      if (!actions.isEmpty()) {
        actions.remove(actions.size() - 1);
      }

      return actions;
    }
  }

  private List<IRecordedAction> handleKeyEvent(Long time, NativeKeyEvent event) {
    List<IRecordedAction> actions = new ArrayList<>();

    Character character = KeyboardLayout.toChar(event, new Character[0]);
    Character characterWithModifiers = KeyboardLayout.toChar(event, modifiers.toArray(new Character[modifiers.size()]));
    if (character != null) {
      boolean isModifier = Key.isModifier(character);

      String keyText = getKeyText(character);

      if (NativeKeyEvent.NATIVE_KEY_PRESSED == event.getID()) {
        Map.Entry<Long, NativeInputEvent> nextEventEntry = events.ceilingEntry(time + 1);

        if (isModifier && nextEventEntry.getValue().getID() != NativeKeyEvent.NATIVE_KEY_RELEASED) {
          modifiers.add(character);
        } else {
          keyText = getKeyText(characterWithModifiers);
          String[] modifiersTexts = getModifierTexts();

          if (keyText.length() > 1) {
            actions.add(new TypeKeyAction(keyText, modifiersTexts));
          }else if (!modifiers.isEmpty() && characterWithModifiers == character) {
            actions.add(new TypeTextAction(keyText, modifiersTexts));
          } else {
            typedText.append(keyText);

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

            if (nextEventText == null || (nextEventText.length() > 1 && !nextEventIsModifier)) {
              String text = typedText.toString();
              typedText = new StringBuilder();
              actions.add(new TypeTextAction(text, new String[0]));
            }
          }
        }
      } else if (NativeKeyEvent.NATIVE_KEY_RELEASED == event.getID()) {
        if (isModifier) {
          modifiers.remove(character);
        }
      }
    }

    return actions;
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

  private Map.Entry<Long, NativeInputEvent> getPreviousEvent(Long time) {
    return events.floorEntry(time - 1);
  }

  private Map.Entry<Long, NativeInputEvent> getNextEvent(Long time) {
    return events.ceilingEntry(time + 1);
  }

  private Long dragStartTime = null;
  private NativeMouseEvent dragStartEvent = null;
  private int clickCount = 0;

  private List<IRecordedAction> handleMouseEvent(Long time, NativeMouseEvent event) {
    List<IRecordedAction> actions = new ArrayList<>();

    Long previousTime = null;
    NativeInputEvent previousEvent = null;

    Map.Entry<Long, NativeInputEvent> previousEventEntry = getPreviousEvent(time);
    if(previousEventEntry != null) {
      previousTime = previousEventEntry.getKey();
      previousEvent = previousEventEntry.getValue();
    }

    Map.Entry<Long, NativeInputEvent> nextEventEntry = getNextEvent(time);
    Long nextTime = nextEventEntry.getKey();
    NativeInputEvent nextEvent = nextEventEntry.getValue();

    if (NativeMouseEvent.NATIVE_MOUSE_PRESSED == event.getID()) {
      if ( NativeMouseEvent.NATIVE_MOUSE_DRAGGED == nextEvent.getID()) {
        dragStartTime = time;
        dragStartEvent = event;
      } else {
        dragStartTime = null;
        dragStartEvent = null;
      }
    } else if (NativeMouseEvent.NATIVE_MOUSE_RELEASED == event.getID()) {
      if (dragStartTime != null && (Math.abs(event.getX() - dragStartEvent.getX()) > 10 ||  Math.abs(event.getY() - dragStartEvent.getY()) > 10)) {
        try {
          Image dragImage = this.findRelevantImage(readFloorScreenshot(dragStartTime), dragStartEvent);
          Image dropImage = this.findRelevantImage(readFloorScreenshot(dragStartTime), event);

          if (dragImage != null && dropImage != null) {
            File dragFile = new File(ImagePath.getBundlePath() + File.separator + dragStartTime + ".png");
            File dropFile = new File(ImagePath.getBundlePath() + File.separator + time + ".png");

            try {
              ImageIO.write(dragImage.get(), "PNG", dragFile);
              ImageIO.write(dropImage.get(), "PNG", dropFile);

              Pattern dragPattern = new Pattern(dragFile.getAbsolutePath());
              dragPattern.targetOffset(dragImage.getOffset());
              Pattern dropPattern = new Pattern(dropFile.getAbsolutePath());
              dropPattern.targetOffset(dropImage.getOffset());

              actions.add(new DragDropAction(dragPattern, dropPattern));

            } catch (IOException e) {
              e.printStackTrace();
            }
          }

        } finally {
          clickCount = 0;
          dragStartTime = null;
          dragStartEvent = null;
        }
      } else if (previousEvent.getID() == NativeMouseEvent.NATIVE_MOUSE_PRESSED) {
        clickCount++;

        if ((nextEvent.getID() != NativeMouseEvent.NATIVE_MOUSE_PRESSED || time - DOUBLE_CLICK_TIME > nextTime
            || clickCount >= 2))
          try {
            Long firstMouseMoveTime = findFirstMouseMoveTime(previousTime);
            Mat screenshot = readCeilingScreenshot(firstMouseMoveTime);

            Image image = this.findRelevantImage(screenshot, event);

            if (image != null) {
              File file = new File(ImagePath.getBundlePath() + File.separator + time + ".png");

              try {
                ImageIO.write(image.get(), "PNG", file);
              } catch (IOException e) {
                e.printStackTrace();
              }

              Pattern pattern = new Pattern(file.getAbsolutePath());
              pattern.targetOffset(image.getOffset());

              Long lastNonMouseMoveEventTime = events.floorKey(firstMouseMoveTime - 1);

              if (lastNonMouseMoveEventTime == null) {
                lastNonMouseMoveEventTime = events.firstKey();
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

              Pattern clickPattern = pattern;

              if (!wasHereBeforeMouseMove) {
                clickPattern = null;
              }

              IRecordedAction clickAction = null;
              if (event.getButton() == NativeMouseEvent.BUTTON3) {
                clickAction = new RightClickAction(clickPattern, getModifierTexts());
              }
              if (clickCount >= 2) {
                clickAction = new DoubleClickAction(clickPattern, getModifierTexts());
              } else {
                clickAction = new ClickAction(clickPattern, getModifierTexts());
              }

              if (!wasHereBeforeMouseMove) {
                int waitTime = (int) Math.min(3, Math.ceil((firstMouseMoveTime - lastNonMouseMoveEventTime) / 1000d * 2));
                actions.add(new WaitAction(pattern, waitTime, clickAction));
              } else {
                actions.add(clickAction);
              }

            }
          } finally {
            clickCount = 0;
            dragStartTime = null;
            dragStartEvent = null;
          }
      }
    } else if (NativeMouseEvent.NATIVE_MOUSE_MOVED == event.getID()) {

    }
    return actions;
  }

  private Long findFirstMouseMoveTime(Long time) {
    Long firstMouseMoveEventTime = events.firstKey();
    List<Map.Entry<Long, NativeInputEvent>> previousEvents = new ArrayList<>(
        events.headMap(time).entrySet());
    Collections.reverse(previousEvents);

    for (Map.Entry<Long, NativeInputEvent> entry : previousEvents) {
      if (entry.getValue().getID() == NativeMouseEvent.NATIVE_MOUSE_MOVED
          || entry.getValue().getID() == NativeMouseEvent.NATIVE_MOUSE_DRAGGED) {
        firstMouseMoveEventTime = entry.getKey();
      } else {
        break;
      }
    }

    return firstMouseMoveEventTime;
  }

  private Mat readFloorScreenshot(Long time) {
    return Imgcodecs.imread(screenshots.floorEntry(time).getValue());
  }

  private Mat readCeilingScreenshot(Long time) {
    return Imgcodecs.imread(screenshots.ceilingEntry(time).getValue());
  }

  private Image findRelevantImage(Mat screenshot, NativeMouseEvent event) {
    int eventX = event.getX();
    int eventY = event.getY();

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

      int top = currentTop - Y_INCREASE_SIZE;
      int increasedTopHeight = currentBottom - top;
      int increasedTopCount = findGoodFeatures(edges, top, currentRight, currentBottom, currentLeft);
      double increasedTopFactor = (double) (currentWidth * increasedTopHeight) / currentArea;

      int right = currentRight + X_INCREASE_SIZE;
      int increasedRightWidth = right - currentLeft;
      int increasedRightCount = findGoodFeatures(edges, currentTop, right, currentBottom, currentLeft);
      double increasedRightFactor = (double) (increasedRightWidth * currentHeight) / currentArea;

      int bottom = currentBottom + Y_INCREASE_SIZE;
      int increasedBottomHeight = bottom - currentTop;
      int increasedBottomCount = findGoodFeatures(edges, currentTop, currentRight, bottom, currentLeft);
      double increasedBottomFactor = (double) (currentWidth * increasedBottomHeight) / currentArea;

      int left = currentLeft - X_INCREASE_SIZE;
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
        currentTop -= Y_INCREASE_SIZE;
        currentRight += X_INCREASE_SIZE;
        currentBottom += Y_INCREASE_SIZE;
        currentLeft -= X_INCREASE_SIZE;
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
    image.setOffset(new Location(eventX - (currentLeft + (currentRight - currentLeft) / 2),
        eventY - (currentTop + (currentBottom - currentTop) / 2)));

    return image;
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
