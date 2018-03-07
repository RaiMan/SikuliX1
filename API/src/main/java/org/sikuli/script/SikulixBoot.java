package org.sikuli.script;

public class SikulixBoot {

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }
  public static void main(String[] args) {
    String property = System.getProperty("java.awt.graphicsenv");
    p("java.awt.graphicsenv: %s", property);
    RunTime.get();
  }
}
