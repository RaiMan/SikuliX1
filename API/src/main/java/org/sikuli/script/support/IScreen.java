/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;

import java.awt.Rectangle;
import java.io.IOException;

/**
 * INTERNAL USE
 * function template for (alternative) Screen implementations
 */
public interface IScreen {

	int getID();

	int getIdFromPoint(int srcx, int srcy);

	String getIDString();

	IRobot getRobot();

	ScreenImage capture();

	ScreenImage capture(int x, int y, int w, int h);

	ScreenImage capture(Rectangle rect);

	ScreenImage capture(Region reg);

	ScreenImage userCapture(String string);

	ScreenImage getLastScreenImageFromScreen();

	String getLastScreenImageFile(String path, String name) throws IOException;

	int getX();

	int getW();

	int getY();

	int getH();

	Rectangle getBounds();

	Rectangle getRect();

	boolean isOtherScreen();

	Region setOther(Region element);
  Location setOther(Location element);

  Location newLocation(int x, int y);
  Location newLocation(Location loc);

  Region newRegion(int x, int y, int w, int h);
  Region newRegion(Location loc, int w, int h);
  Region newRegion(Region reg);

  void waitAfterAction();

  Object action(String action, Object... args);
}
