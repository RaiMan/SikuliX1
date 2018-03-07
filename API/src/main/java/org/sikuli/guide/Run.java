/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sikuli.guide;

import java.awt.Color;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;

/**
 *
 * @author rhocke
 */
public class Run {

	Guide guide = null;
	static Screen scr;
  static Visual sgc;

public static void main(String[] args) throws FindFailed {
  Run sgr = new Run();
  sgr.scr = new Screen();
  ImagePath.add("org.sikuli.script.RunTime/ImagesAPI.sikuli");
  sgr.setUp();
  sgr.testButton();
  sgr.tearDown();
}

	private void setUp() {
		guide = new Guide();
//		App.focus("safari");
	}

	private void tearDown() {
		System.out.println(guide.showNow(2f));
		guide = null;
	}

	public void testButton() throws FindFailed {
  Debug.on(3);
		Visual vis = guide.text("text");
//		vis.setTarget(scr.getCenter().grow(100));
    String img = "idea";
//    Match match = scr.find(img);
//    match.highlight(2);

		vis.setTarget(img);
		vis.setLayout(Visual.Layout.RIGHT);
		vis.setTextColor(Color.red);
//    g.setLocationRelativeToRegion(scr.getCenter().grow(100), Visual.Layout.BOTTOM);
//    g.setFontSize(12);
//    g.setColor(Color.white);
//    g.setTextColor(Color.black);
	}
}
