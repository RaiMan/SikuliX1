/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.tips;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Loads tips from Properties.<br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class TipLoader {

  private TipLoader() { }

  /**
   * Initializes a TipOfTheDayModel from properties. Each tip is defined by two
   * properties, its name and its description:
   *
   * <pre>
   * <code>
   * tip.1.name=First Tip
   * tip.1.description=This is the description
   *
   * tip.2.name=Second Tip
   * tip.2.description=&lt;html&gt;This is an html description
   *
   * ...
   *
   * tip.10.description=No name for this tip, name is optional
   * </code>
   * </pre>
   *
   * @param props
   * @return a TipOfTheDayModel
   * @throws IllegalArgumentException
   *           if a name is found without description
   */
  public static TipOfTheDayModel load(Properties props) {
    List<TipOfTheDayModel.Tip> tips = new ArrayList<TipOfTheDayModel.Tip>();

    int count = 1;
    while (true) {
      String nameKey = "tip." + count + ".name";
      String nameValue = props.getProperty(nameKey);

      String descriptionKey = "tip." + count + ".description";
      String descriptionValue = props.getProperty(descriptionKey);

      if (nameValue != null && descriptionValue == null) { throw new IllegalArgumentException(
        "No description for name " + nameValue); }

      if (descriptionValue == null) {
        break;
      }

      DefaultTip tip = new DefaultTip(nameValue, descriptionValue);
      tips.add(tip);

      count++;
    }

    return new DefaultTipOfTheDayModel(tips);
  }

}
