/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.android;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.script.Image;

/**
 * Created by RaiMan on 12.07.16.
 * <p>
 * Test for the basic ADB based features
 */
public class ADBTest {

  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "ADBDevice: " + message, args);
  }

  private static void logp(String message, Object... args) {
    System.out.println(String.format(message, args));
  }

  private static RunTime rt = null;
  private static boolean runTests = true;


  public static void main(String[] args) throws FindFailed {

    ADBScreen aScr = startTest();

    if (aScr.isValid()) {
      if (runTests) {

        basicTest(aScr);

        ADBScreen.stop();

        System.exit(0);
      }
    } else {
      System.exit(1);
    }

    // ********* playground
  }

  private static ADBScreen startTest() {
    Debug.on(3);
    rt = RunTime.get();
    ADBScreen adbs = new ADBScreen();
    if (adbs.isValid()) {
      adbs.wakeUp(2);
      adbs.wait(1f);
      if (runTests) {
        adbs.aKey(ADBDevice.KEY_HOME);
        adbs.wait(1f);
      }
    }
    return adbs;
  }


  private static void basicTest(ADBScreen adbs) throws FindFailed {
    log(lvl, "**************** running basic test");
    adbs.aSwipeLeft();
    adbs.aSwipeRight();
    adbs.wait(1f);
    ScreenImage sIMg = adbs.userCapture("Android");
    sIMg.save(RunTime.get().fSikulixStore.getAbsolutePath(), "android");
    adbs.aTap(new Image(sIMg));
  }


  /**
   * used in SikuliIDE menu tool to run a test against an attached device
   *
   * @param aScr
   */
  public static void ideTest(ADBScreen aScr) {
    String title = "Android Support - Testing device";
    Sikulix.popup("Take care\n\nthat device is on and unlocked\n\nbefore clicking ok", title);
    aScr.wakeUp(2);
    aScr.aKey(ADBDevice.KEY_HOME);
    if (Sikulix.popAsk("Now the device should show the HOME screen.\n" +
            "\nclick YES to proceed watching the test on the device" +
            "\nclick NO to end the test now", title)) {
      aScr.aSwipeLeft();
      aScr.aSwipeRight();
      aScr.wait(1f);
      if (Sikulix.popAsk("You should have seen a swipe left and a swipe right.\n" +
              "\nclick YES to capture an icon from homescreen and then aTap it" +
              "\nclick NO to end the test now", title)) {
        ScreenImage sIMg = aScr.userCapture("AndroidTest");
        sIMg.save(RunTime.get().fSikulixStore.getAbsolutePath(), "android");
        try {
          aScr.aTap(new Image(sIMg));
          Sikulix.popup("The image was found on the device's current screen" +
                  "\nand should have been tapped.\n" +
                  "\nIf you think it worked, you can now try\n" +
                  "to capture needed images from the device.\n" +
                  "\nYou have to come back here and click Default!", title);
        } catch (FindFailed findFailed) {
          Sikulix.popError("Sorry, the image you captured was\nnot found on the device's current screen", title);
        }
      }
    }
  }

}
