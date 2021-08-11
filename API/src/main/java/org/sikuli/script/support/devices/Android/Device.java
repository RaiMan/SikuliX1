package org.sikuli.script.support.devices.Android;

import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.support.IRobot;
import org.sikuli.script.support.IScreen;

import java.awt.*;
import java.io.IOException;

public class Device implements IScreen, IRobot {
  @Override
  public void keyDown(String keys) {

  }

  @Override
  public void keyUp(String keys) {

  }

  @Override
  public void keyDown(int code) {

  }

  @Override
  public void keyUp(int code) {

  }

  @Override
  public void keyUp() {

  }

  @Override
  public void pressModifiers(int modifiers) {

  }

  @Override
  public void releaseModifiers(int modifiers) {

  }

  @Override
  public void typeChar(char character, KeyMode mode) {

  }

  @Override
  public void typeKey(int key) {

  }

  @Override
  public void typeStarts() {

  }

  @Override
  public void typeEnds() {

  }

  @Override
  public void mouseMove(int x, int y) {

  }

  @Override
  public void mouseDown(int buttons) {

  }

  @Override
  public int mouseUp(int buttons) {
    return 0;
  }

  @Override
  public void mouseReset() {

  }

  @Override
  public void clickStarts() {

  }

  @Override
  public void clickEnds() {

  }

  @Override
  public void smoothMove(Location dest) {

  }

  @Override
  public void smoothMove(Location src, Location dest, long ms) {

  }

  @Override
  public void mouseWheel(int wheelAmt) {

  }

  @Override
  public ScreenImage captureScreen(Rectangle screenRect) {
    return null;
  }

  @Override
  public void waitForIdle() {

  }

  @Override
  public void delay(int ms) {

  }

  @Override
  public void setAutoDelay(int ms) {

  }

  @Override
  public Color getColorAt(int x, int y) {
    return null;
  }

  @Override
  public void cleanup() {

  }

  @Override
  public boolean isRemote() {
    return false;
  }

  @Override
  public IScreen getScreen() {
    return null;
  }

  @Override
  public int getID() {
    return 0;
  }

  @Override
  public int getIdFromPoint(int srcx, int srcy) {
    return 0;
  }

  @Override
  public String getIDString() {
    return null;
  }

  @Override
  public IRobot getRobot() {
    return null;
  }

  @Override
  public ScreenImage capture() {
    return null;
  }

  @Override
  public ScreenImage capture(int x, int y, int w, int h) {
    return null;
  }

  @Override
  public ScreenImage capture(Rectangle rect) {
    return null;
  }

  @Override
  public ScreenImage capture(Region reg) {
    return null;
  }

  @Override
  public ScreenImage userCapture(String string) {
    return null;
  }

  @Override
  public ScreenImage getLastScreenImageFromScreen() {
    return null;
  }

  @Override
  public String getLastScreenImageFile(String path, String name) throws IOException {
    return null;
  }

  @Override
  public int getX() {
    return 0;
  }

  @Override
  public int getW() {
    return 0;
  }

  @Override
  public int getY() {
    return 0;
  }

  @Override
  public int getH() {
    return 0;
  }

  @Override
  public Rectangle getBounds() {
    return null;
  }

  @Override
  public Rectangle getRect() {
    return null;
  }

  @Override
  public boolean isOtherScreen() {
    return false;
  }

  @Override
  public Region setOther(Region element) {
    return null;
  }

  @Override
  public Location setOther(Location element) {
    return null;
  }

  @Override
  public Location newLocation(int x, int y) {
    return null;
  }

  @Override
  public Location newLocation(Location loc) {
    return null;
  }

  @Override
  public Region newRegion(int x, int y, int w, int h) {
    return null;
  }

  @Override
  public Region newRegion(Location loc, int w, int h) {
    return null;
  }

  @Override
  public Region newRegion(Region reg) {
    return null;
  }

  @Override
  public void waitAfterAction() {

  }

  @Override
  public Object action(String action, Object... args) {
    return null;
  }
}
