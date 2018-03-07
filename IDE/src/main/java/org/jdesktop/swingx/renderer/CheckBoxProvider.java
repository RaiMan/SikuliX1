/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

/**
 * A component provider which uses a JCheckBox.
 * <p>
 *
 * This implementation respects a BooleanValue and a StringValue to configure
 * the button's selected and text property. By default, the selected is mapped
 * to a Boolean-type value and the text is empty.
 * <p>
 *
 * To allow mapping to different types, client code can supply a custom
 * StringValue which also implements BooleanValue. F.i. to render a cell value
 * of type TableColumnExt with the column's visibility mapped to the selected
 * and the column's title to the text:
 *
 * <pre><code>
 *
 *     BooleanValue bv = new BooleanValue(){
 *        public boolean getBoolean(Object value) {
 *           if (value instanceof TableColumnExt)
 *               return ((TableColumnExt) value).isVisible();
 *           return false;
 *        }
 *     };
 *     StringValue sv = new StringValue() {
 *         public String getString(Object value) {
 *           if (value instanceof TableColumnExt)
 *               return ((TableColumnExt) value).getTitle();
 *           return &quot;&quot;;
 *         }
 *     };
 *     list.setCellRenderer(new DefaultListRenderer(
 *           new CheckBoxProvider(new MappedValue(sv, null, bv), JLabel.LEADING)));
 * </code></pre>
 *
 *
 * @see BooleanValue
 * @see StringValue
 * @see MappedValue
 *
 * @author Jeanette Winzenburg
 */
public class CheckBoxProvider extends ComponentProvider<AbstractButton> {

    private boolean borderPainted;

    /**
     * Instantiates a CheckBoxProvider with default properties. <p>
     *
     */
    public CheckBoxProvider() {
        this(null);
    }

    /**
     * Instantiates a CheckBoxProvider with the given StringValue and default
     * alignment.
     *
     * @param stringValue the StringValue to use for formatting.
     */
    public CheckBoxProvider(StringValue stringValue) {
        this(stringValue, JLabel.CENTER);
    }

    /**
     * Instantiates a CheckBoxProvider with the given StringValue and
     * alignment.
     *
     * @param stringValue the StringValue to use for formatting.
     * @param alignment the horizontalAlignment.
     */
    public CheckBoxProvider(StringValue stringValue, int alignment) {
        super(stringValue == null ? StringValues.EMPTY : stringValue, alignment);
        setBorderPainted(true);
    }

    /**
     * Returns the border painted flag.
     * @return the borderpainted flag to use on the checkbox.
     * @see #setBorderPainted(boolean)
     */
    public boolean isBorderPainted() {
        return borderPainted;
    }

    /**
     * Sets the border painted flag. The underlying checkbox
     * is configured with this value on every request.<p>
     *
     * The default value is true.
     *
     * @param borderPainted the borderPainted property to configure
     *   the underlying checkbox with.
     *
     *  @see #isBorderPainted()
     */
    public void setBorderPainted(boolean borderPainted) {
        this.borderPainted = borderPainted;
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to set the button's selected state and text.<p>
     *
     *  PENDING: set icon?
     *
     *  @see #getValueAsBoolean(CellContext)
     *  @see #getValueAsString(CellContext)
     */
    @Override
    protected void format(CellContext context) {
        rendererComponent.setSelected(getValueAsBoolean(context));
        rendererComponent.setText(getValueAsString(context));
    }

    /**
     * Returns a boolean representation of the content.<p>
     *
     * This method messages the
     * <code>BooleanValue</code> to get the boolean rep. If none available,
     * checks for Boolean type directly and returns its value. Returns
     * false otherwise. <p>
     *
     * PENDING: fallback to check for boolean is convenient .. could cleanup
     *   to use a default BooleanValue instead.
     *
     * @param context the cell context, must not be null.
     * @return the boolean representation of the cell's content,
     *   or false if none if available.
     */
    protected boolean getValueAsBoolean(CellContext context) {
        if (formatter instanceof BooleanValue) {
            return ((BooleanValue) formatter).getBoolean(context.getValue());
        }
        return Boolean.TRUE.equals(context.getValue());
    }

    /**
     * {@inheritDoc}<p>
     *
     * Here: set's the buttons horizontal alignment and borderpainted properties
     * to this provider's properties.
     */
    @Override
    protected void configureState(CellContext context) {
        rendererComponent.setBorderPainted(isBorderPainted());
        rendererComponent.setHorizontalAlignment(getHorizontalAlignment());
    }

    /**
     * {@inheritDoc}<p>
     * Here: returns a JCheckBox as rendering component.<p>
     *
     */
    @Override
    protected AbstractButton createRendererComponent() {
        return new JRendererCheckBox();
    }

}
