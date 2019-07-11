package org.sikuli.script.support.generators;

import org.sikuli.script.Image;
import org.sikuli.script.Location;
import org.sikuli.script.Pattern;

public interface ICodeGenerator {
  public String pattern(Pattern pattern, String mask);

  public String click(Pattern pattern);

  public String doubleClick(Pattern pattern);

  public String typeText(String text, String[] modifiers);

  public String typeKey(String key, String[] modifiers);

  public String waitClick(Pattern pattern);

}
