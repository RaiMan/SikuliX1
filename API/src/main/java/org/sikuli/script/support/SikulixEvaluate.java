/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.script.App;

import java.util.List;

public class SikulixEvaluate {
  public static void main(String[] args) {
    if (args.length == 0) {
      Debug.info("SikulixEvaluate: Nothing to do!");
      return;
    }
    if ("test".equals(args[0])) {
      test();
    }
  }
  //region test
  public static void test() {

//BREAKPOINT before test
//    Commons.startDebug();
//    Commons.startTrace();
//    Debug.off();
//    Debug.on(3);

    Debug.info("***** start of testing *****");

//    Screen scr = new Screen();

//TEST: waitBest/waitAny
    //    Image img1 = new Image(scr.userCapture());
//    Image img2 = new Image(scr.userCapture());
//    scr.wait(5.0);
//    Match match = scr.waitBest(5, img1, img2);
//    if (null != match) {
//      match.highlight(2);
//    } else {
//      scr.highlight(-2);
//    }
//    List<Match> matches = scr.waitAny(5, img1, img2);
//    if (matches.size() > 0) {
//      for (Match m : matches) {
//        m.highlight(1);
//      }
//    } else {
//      scr.highlight(-2);
//    }

//TEST: ImagePath revision
//    Commons.startTrace();
//    ImagePath.clear();
//    ImagePath.dump(0);
//    URL url = null;
//    File file = null;
//    String jar = "target/sikulixapi-2.0.5.jar";
//    String imageName = "";
//    Commons.makeURL(null);
//    Commons.makeURL("");
//    Commons.makeURL("target/classes/images");
//    Commons.makeURL("../API/target/classes/images");
//    Commons.makeURL(Commons.getWorkDir(), "target/classes/images");
//    Commons.makeURL("target", "classes/images");
//    Commons.makeURL(new File("target"), "classes/images");
//    url = Commons.makeURL(new File("tar get"), "classes/images");
//    url = Commons.makeURL("../API/target/sikulixapi-2.0.5.jar");
//    url = Commons.makeURL("../API/target/sikulixapi-2.0.5.jar", "images");
//    url = Commons.makeURL("../API/target/sikulixapi-2.0.5.jar!/images");
//    url = Commons.makeURL("../API/target/sikulixapi-2.0.5.jar!/images", "subImages");
//    Debug.info("%s", Commons.urlToFile(url));
//    url = Commons.makeURL("http://API/target/some.jar", "subImages");
//    Commons.stopTrace();
//    ImagePath.add(jar, "images");
//    String classes = "target/classes/images";
//    ImagePath.add(classes);
//    ImagePath.dump(0);
//    imageName = "img.png";
//    url = ImagePath.find(imageName);
//    Debug.info("%s url: %s", imageName, url);
//    imageName = "img1.png";
//    url = ImagePath.find(imageName);
//    Debug.info("%s url: %s", imageName, url);
//    imageName = "img2.png";
//    url = ImagePath.find(imageName);
//    Debug.info("%s url: %s", imageName, url);
//
//    URL first = ImagePath.insert(classes);
//    first = ImagePath.addBefore(jar, "images", first);
//    ImagePath.addBefore(jar, first);
//    URL last = ImagePath.append(jar, "folder");
//    ImagePath.addAfter(jar, "folder1", last);
//    ImagePath.addAfter(jar, "folder2", first);
//    ImagePath.addJar(".", "images");
//    ImagePath.dump(0);
//    Image img1 = Image.create("img");
//    Debug.info("%s (%s)", img1, img1.getURL());
//    ImagePath.add("sikulix.com:images", "moreImages");
//    boolean has = ImagePath.has("sikulix.com:images", "moreImages");
//    URL url = ImagePath.append("target/classes/images");
//    ImagePath.append(jar);
//    ImagePath.setBundlePath("target/classes/images");
//    ImagePath.setBundlePath(jar);
//    ImagePath.setBundlePath("sikulix.com:images");
//    String bundlePath = ImagePath.getBundlePath();
//    String net = "https://github.com/RaiMan/SikuliX1-Docs/raw/master/src/main/resources/docs/source";
//    ImagePath.append(net);
//    Pattern netImg = new Pattern("buttonText").exact();
//    String netImg = "buttonText";
//    Image netImg = Image.create(snetImg);
//    long time = new Date().getTime();
//    scr.has(netImg);
//    long lap = new Date().getTime() - time;
//    Debug.info("%d", lap);
//    scr.highlight(-2);
//    time = new Date().getTime();
//    scr.has(netImg);
//    lap = new Date().getTime() - time;
//    Debug.info("%d", lap);
//    scr.highlight(-2);
//    ImagePath.dump(0);
//    Debug.info("%s (%s)", image, image.getURL());

//TEST: SX.pop... feature should return null, if timed out
//    Object feedback = SX.popup("test timeout", 5);
//    Commons.printLog("popup returned %s", feedback);

//TEST: find(Image.getSub()) did not work always (BufferedImage problem)
//solution: make sub same type as original
//    Image image = new Image(scr.userCapture());
//    try {
//      scr.find(image).highlight(2);
//      Image subImage = image.getSub(Region.WEST); //
//      scr.find(subImage).highlight(2);
//    } catch (FindFailed findFailed) {
//      Commons.printLog("not found: %s", image);
//    }

//TEST: macOS S & P behavior
//    new Screen();

//TEST: sim value can be float
//    Settings.MinSimilarity = 0.6f;
//    Pattern pat = new Pattern("someImage");
//    Commons.printLog("%s", pat);
//    pat = new Pattern("someImage").similar(0.6f);
//    Commons.printLog("%s", pat);

//BREAKPOINT after test
    Debug.info("***** end of testing *****");
  }
//endregion
}
