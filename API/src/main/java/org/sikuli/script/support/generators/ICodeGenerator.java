/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.generators;

import org.sikuli.script.Pattern;
import org.sikuli.script.support.recorder.actions.IRecordedAction;

/**
 * Generates executable code snippets.
 *
 * @author balmma
 *
 */
public interface ICodeGenerator {
  /**
   *
   *
   * @param pattern
   * @param mask mask code TODO needs to be abstractd away
   * @return
   */
  public String pattern(Pattern pattern, String mask);

  public String click(Pattern pattern, String[] modifiers);

  public String mouseDown(Pattern pattern, String[] buttons);

  public String mouseUp(Pattern pattern, String[] buttons);

  public String mouseMove(Pattern pattern);

  public String dragDrop(Pattern sourcePattern, Pattern targetPattern);

  public String doubleClick(Pattern pattern, String[] modifiers);

  public String rightClick(Pattern pattern, String[] modifiers);

  public String typeText(String text, String[] modifiers);

  public String typeKey(String key, String[] modifiers);

  public String wait(Pattern pattern, Integer seconds, IRecordedAction matchAction);
}
