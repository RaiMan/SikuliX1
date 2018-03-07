/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.combobox;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * A ComboBoxModel implementation that safely wraps an Enum. It allows the
 * developer to directly use an enum as their model for a combobox without any
 * extra work, though the display can can be further customized.
 * </p>
 *
 * <h4>Simple Usage</h4>
 *
 * <p>
 * The simplest usage is to wrap an <code>enum</code> inside the
 * <code>EnumComboBoxModel</code> and then set it as the model on the combo
 * box. The combo box will then appear on screen with each value in the
 * <code>enum</code> as a value in the combobox.
 * </p>
 * <p>
 * ex:
 * </p>
 *
 * <pre><code>
 *  enum MyEnum { GoodStuff, BadStuff };
 *  ...
 *  JComboBox combo = new JComboBox();
 *  combo.setModel(new EnumComboBoxModel(MyEnum.class));
 * </code></pre>
 *
 * <h4>Type safe access</h4>
 * <p>
 * By using generics and co-variant types you can make accessing elements from
 * the model be completely typesafe. ex:
 * </p>
 *
 * <pre><code>
 * EnumComboBoxModel&lt;MyEnum&gt; enumModel = new EnumComboBoxModel&lt;MyEnum1&gt;(
 *         MyEnum1.class);
 *
 * MyEnum first = enumModel.getElement(0);
 *
 * MyEnum selected = enumModel.getSelectedItem();
 * </code></pre>
 *
 * <h4>Advanced Usage</h4>
 * <p>
 * Since the exact <code>toString()</code> value of each enum constant may not
 * be exactly what you want on screen (the values won't have spaces, for
 * example) you can override to toString() method on the values when you declare
 * your enum. Thus the display value is localized to the enum and not in your
 * GUI code. ex:
 *
 * <pre><code>
 *    private enum MyEnum {GoodStuff, BadStuff;
 *        public String toString() {
 *           switch(this) {
 *               case GoodStuff: return &quot;Some Good Stuff&quot;;
 *               case BadStuff: return &quot;Some Bad Stuff&quot;;
 *           }
 *           return &quot;ERROR&quot;;
 *        }
 *    };
 * </code></pre>
 *
 * Note: if more than one enum constant returns the same {@code String} via
 * {@code toString()}, this model will throw an exception on creation.
 *
 * @author joshy
 * @author Karl Schaefer
 */
public class EnumComboBoxModel<E extends Enum<E>> extends ListComboBoxModel<E> {
    private static final long serialVersionUID = 2176566393195371004L;

    private final Map<String, E> valueMap;
    private final Class<E> enumClass;

    /**
     * Creates an {@code EnumComboBoxModel} for the enum represent by the
     * {@code Class} {@code en}.
     *
     * @param en
     *            the enum class type
     * @throws IllegalArgumentException
     *             if the {@code Enum.toString} returns the same value for more
     *             than one constant
     */
    public EnumComboBoxModel(Class<E> en) {
        super(new ArrayList<E>(EnumSet.allOf(en)));

        //we could size these, probably not worth it; enums are usually small
        valueMap = new HashMap<String, E>();
        enumClass = en;

        Iterator<E> iter = data.iterator();

        while (iter.hasNext()) {
            E element = iter.next();
            String s = element.toString();

            if (valueMap.containsKey(s)) {
                throw new IllegalArgumentException(
                        "multiple constants map to one string value");
            }

            valueMap.put(s, element);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object anItem) {
        E input = null;

        if (enumClass.isInstance(anItem)) {
            input = (E) anItem;
        } else {
            input = valueMap.get(anItem);
        }

        if (input != null || anItem == null) {
            selected = input;
        }

        this.fireContentsChanged(this, 0, getSize());
    }

    /*
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.Y_AXIS));

        JComboBox combo1 = new JComboBox();
        combo1.setModel(new EnumComboBoxModel(MyEnum1.class));
        frame.add(combo1);

        JComboBox combo2 = new JComboBox();
        combo2.setModel(new EnumComboBoxModel(MyEnum2.class));
        frame.add(combo2);

        EnumComboBoxModel<MyEnum1> enumModel = new EnumComboBoxModel<MyEnum1>(MyEnum1.class);
        JComboBox combo3 = new JComboBox();
        combo3.setModel(enumModel);
        frame.add(combo3);

        MyEnum1 selected = enumModel.getSelectedItem();

        //uncomment to see the ClassCastException
//        enumModel.setSelectedItem("Die clown");

        frame.pack();
        frame.setVisible(true);
    }

    private enum MyEnum1 {GoodStuff, BadStuff};
    private enum MyEnum2 {GoodStuff, BadStuff;
    public String toString() {
        switch(this) {
            case GoodStuff: return "Some Good Stuff";
            case BadStuff: return "Some Bad Stuff";
        }
        return "ERROR";
    }
    };
    */

}
