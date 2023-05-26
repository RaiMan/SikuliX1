package org.sikuli.script;

import org.sikuli.script.support.IRobot;
import org.sikuli.script.support.IScreen;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageAsScreen extends Region implements IScreen {

    protected BufferedImage image;
    private Dimension dimension = new Dimension(700, 1000);

    public ImageAsScreen() {
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    ScreenImage toImage(Rectangle roi) {
        if (this.image == null) {
            BufferedImage image = new BufferedImage(this.dimension.width, this.dimension.height,
                    BufferedImage.TYPE_INT_RGB);
            return new ScreenImage(roi, image);
        } else {
            return new ScreenImage(roi, this.image);
        }
    }

    private Rectangle dimensionRectangle() {
        return new Rectangle(0, 0, this.dimension.width, this.dimension.height);
    }

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public int getIdFromPoint(int srcx, int srcy) {
        return 1;
    }

    @Override
    public String getIDString() {
        return Integer.toString(1);
    }

    @Override
    public IRobot getRobot() {
        return null;
    }

    @Override
    public ScreenImage capture() {
        return toImage(dimensionRectangle());
    }

    @Override
    public ScreenImage capture(int x, int y, int w, int h) {
        return toImage(new Rectangle(x, y, w, h));
    }

    @Override
    public ScreenImage capture(Rectangle rect) {
        return toImage(rect);
    }

    @Override
    public ScreenImage capture(Region reg) {
        Rectangle rect = new Rectangle(reg.x, reg.y, reg.w, reg.h);
        return toImage(rect);
    }

    @Override
    public ScreenImage userCapture(String string) {
        throw new IllegalStateException("Not implemented: userCapture");
    }

    @Override
    public ScreenImage getLastScreenImageFromScreen() {
        return toImage(dimensionRectangle());
    }

    @Override
    public String getLastScreenImageFile(String path, String name) throws IOException {
        throw new IllegalStateException("Not implemented: getLastScreenImageFile");
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getW() {
        return dimension.width;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getH() {
        return dimension.height;
    }

    @Override
    public Rectangle getBounds() {
        return dimensionRectangle();
    }

    @Override
    public Rectangle getRect() {
        return dimensionRectangle();
    }

    @Override
    public boolean isOtherScreen() {
        return true;
    }

    @Override
    public Region setOther(Region element) {
        return element.setOtherScreen(this);
    }

    @Override
    public Location setOther(Location element) {
        return element.setOtherScreen(this);
    }

    @Override
    public Location newLocation(int x, int y) {
        return new Location(x, y).setOtherScreen(this);
    }

    @Override
    public Location newLocation(Location loc) {
        return (new Location(loc)).copyTo(this);
    }

    @Override
    public Region newRegion(int x, int y, int w, int h) {
        Location loc = new Location(x, y);
        loc.setOtherScreen(this);
        return newRegion(loc, w, h);
    }

    @Override
    public Region newRegion(Location loc, int width, int height) {
        Location loc2 = loc.copyTo(this);
        loc2.setOtherScreen(this);
        return Region.create(loc2, width, height);
    }

    @Override
    public Region newRegion(Region reg) {
        throw new IllegalStateException("Not implemented: newRegion");
    }

    @Override
    public void waitAfterAction() {

    }

    @Override
    public Object action(String action, Object... args) {
        return null;
    }

    @Override
    public String toString() {
        return "ImageAsScreen{" +
                "image=" + image +
                ", dimension=" + dimension +
                '}';
    }
}
