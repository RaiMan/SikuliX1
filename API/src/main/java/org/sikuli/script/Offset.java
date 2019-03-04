package org.sikuli.script;

public class Offset {
  public int x = 0;
  public int y = 0;

  public Offset() {
  }

  /**
   * a new offset with given x and y distances
   *
   * @param x column
   * @param y row
   */
  public Offset(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public <RMILO> Offset(RMILO whatEver) {
    if (whatEver instanceof Region || whatEver instanceof Match) {
      Region what = (Region) whatEver;
      x = what.w;
      y = what.h;
    } else if (whatEver instanceof Image) {
      Image what = (Image) whatEver;
      if (null != what.get()) {
        x = what.get().getWidth();
        y = what.get().getHeight();
      }
    } else if (whatEver instanceof Location) {
      Location what = (Location) whatEver;
      x = what.x;
      y = what.y;
    } else if (whatEver instanceof Offset) {
      Offset what = (Offset) whatEver;
      x = what.x;
      y = what.y;
    }
  }

  public <RMILO> Offset modify(RMILO whatever) {
    Offset offset = new Offset(whatever);
    offset.x = this.x + offset.x;
    offset.y = this.y + offset.y;
    return offset;
  }
}
