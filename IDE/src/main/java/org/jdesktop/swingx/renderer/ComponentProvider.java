/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.plaf.UIDependent;

/**
 * Abstract base class of a provider for a cell rendering component. Configures
 * the component's content and default visuals depending on the renderee's state
 * as captured in a <code>CellContext</code>. It's basically re-usable across
 * all types of renderees (JTable, JList, JTree).
 * <p>
 *
 * <h2> Content </h2>
 *
 * A provider guarantees to configure the "content" properties completely
 * for any given object. The most frequent mappings are to text and/or icon
 * properties of the rendering components. The former is controlled by a
 * StringValue (see below), the latter by an IconValue. Subclasses which
 * hand out component of type IconAware guarantee to reset its icon property
 * always. <p>
 *
 * To ease content configuration, it supports a pluggable
 * <code>StringValue</code> which purpose is to create and return a string
 * representation of a given object. Implemenations of a ComponentProvider can
 * use it to configure their rendering component as appropriate.<p>
 *
 * F.i. to show a Contributor cell object as "Busywoman, Herta" implement a
 * custom StringValue and use it in a text rendering provider. (Note that SwingX
 * default implementations of Table/List/TreeCellRenderer have convenience
 * constructors to take the converter and create a default LabelProvider which
 * uses it).
 *
 * <pre><code>
 * StringValue stringValue = new StringValue() {
 *
 *     public String getString(Object value) {
 *         if (!(value instanceof Contributor))
 *             return TO_STRING.getString(value);
 *         Contributor contributor = (Contributor) value;
 *         return contributor.lastName + &quot;, &quot; + contributor.firstName;
 *     }
 *
 * };
 * table.setDefaultRenderer(Contributor.class, new DefaultTableRenderer(
 *         stringValue));
 * list.setCellRenderer(new DefaultListRenderer(stringValue));
 * tree.setCellRenderer(new DefaultTreeRenderer(stringValue));
 *
 * </code></pre>
 *
 * To ease handling of formatted localizable content, there's a
 * <code>FormatStringValue</code> which is pluggable with a
 * <code>Format</code>. <p>
 *
 * F.i. to show a Date's time in the default Locale's SHORT
 * version and right align the cell
 *
 * <pre><code>
 *   StringValue stringValue = new FormatStringValue(
 *       DateFormat.getTimeInstance(DateFormat.SHORT));
 *   table.getColumnExt(&quot;timeID&quot;).setCellRenderer(
 *       new DefaultTableRenderer(stringValue, JLabel.RIGHT);
 * </code></pre>
 *
 *
 * <p>
 *
 *
 * <h2> Default Visuals </h2>
 *
 * Guarantees to completely configure the visual properties listed below. As a
 * consequence, client code (f.i. in <code>Highlighter</code>s) can safely
 * change them without long-lasting visual artefacts.
 *
 * <ul>
 * <li> foreground and background, depending on selected and focused state
 * <li> border
 * <li> font
 * <li> Painter (if applicable)
 * <li> enabled
 * <li> componentOrientation
 * <li> tooltipText
 * <li> minimum-, maximum-, preferredSize
 * <li> horizontal alignment (if applicable)
 * </ul>
 *
 * As this internally delegates default visual configuration to a
 * <code>DefaultVisuals</code> (which handles the first eight items)
 * subclasses have to guarantee the alignment only.
 * <p>
 *
 *
 * @see StringValue
 * @see FormatStringValue
 * @see IconValue
 * @see BooleanValue
 * @see CellContext
 * @see DefaultTableRenderer
 * @see DefaultListRenderer
 * @see DefaultTreeRenderer
 * @see DefaultVisuals
 */
public abstract class ComponentProvider<T extends JComponent>
    implements Serializable, UIDependent {
    /** component to render with. */
    protected T rendererComponent;
    /** configurator of default visuals. */
    protected DefaultVisuals<T> defaultVisuals;
    /** horizontal (text) alignment of component.
     * PENDING: useful only for labels, buttons? */
    protected int alignment;
    /** the converter to use for string representation.
     * PENDING: IconValue? */
    protected StringValue formatter;

    /**
     * Instantiates a component provider with LEADING
     * horizontal alignment and default to-String converter. <p>
     *
     */
    public ComponentProvider() {
        this(null, JLabel.LEADING);
    }

    /**
     * Instantiates a component provider with LEADING
     * horizontal alignment and the given converter. <p>
     *
     * @param converter the converter to use for mapping the cell value to a
     *        String representation.
     */
    public ComponentProvider(StringValue converter) {
        this(converter, JLabel.LEADING);
    }

    /**
     * Instantiates a LabelProvider with given to-String converter and given
     * horizontal alignment. If the converter is null, the default TO_STRING is
     * used.
     *
     * @param converter the converter to use for mapping the cell value to a
     *        String representation.
     * @param alignment the horizontal alignment.
     */
    public ComponentProvider(StringValue converter, int alignment) {
        setHorizontalAlignment(alignment);
        setStringValue(converter);
        rendererComponent = createRendererComponent();
        defaultVisuals = createDefaultVisuals();
    }

    /**
     * Configures and returns an appropriate component to render a cell
     * in the given context. If the context is null, returns the
     * component in its current state.
     *
     * @param context the cell context to configure from
     * @return a component to render a cell in the given context.
     */
    public T getRendererComponent(CellContext context) {
        if (context != null) {
            configureVisuals(context);
            configureContent(context);
        }
        return rendererComponent;
    }

    /**
     * Sets the horizontal alignment property to configure the component with.
     * Allowed values are those accepted by corresponding JLabel setter. The
     * default value is JLabel.LEADING. This controller guarantees to apply the
     * alignment on each request for a configured rendering component, if
     * possible. Note that not all components have a horizontal alignment
     * property.
     *
     * @param alignment the horizontal alignment to use when configuring the
     *   rendering component.
     */
    public void setHorizontalAlignment(int alignment) {
       this.alignment = alignment;
    }

    /**
     * Returns the horizontal alignment.
     *
     * @return the horizontal alignment of the rendering component.
     *
     * @see #setHorizontalAlignment(int)
     *
     */
    public int getHorizontalAlignment() {
        return alignment;
    }

    /**
     * Sets the StringValue to use. If the given StringValue is null,
     * defaults to <code>StringValue.TO_STRING</code>. <p>
     *
     * @param formatter the format to use.
     */
    public void setStringValue(StringValue formatter) {
        if (formatter == null) {
            formatter = StringValues.TO_STRING;
        }
        this.formatter = formatter;
    }

    /**
     * Returns the StringValue to use for obtaining
     * the String representation. <p>
     *
     * @return the StringValue used by this provider, guaranteed to
     *   be not null.
     */
    public StringValue getStringValue() {
        return formatter;
    }

    /**
     * Returns a string representation of the content.
     * <p>
     *
     * This method guarantees to return the same string representation as would
     * appear in the renderer, given that the corresponding cellContext has the
     * same value as the parameter passed-in here. That is (assuming that the
     * rendering component has a getText())
     *
     * <pre><code>
     * if (equals(value, context.getValue()) {
     *     assertEquals(provider.getString(value),
     *     provider.getRenderingComponent(context).getText());
     * }
     * </code></pre>
     *
     * This implementation simply delegates to its StringValue. Subclasses might
     * need to override to comply.
     * <p>
     *
     * This is a second attempt - the driving force is the need for a consistent
     * string representation across all (new and old) themes: rendering,
     * (pattern) filtering/highlighting, searching, auto-complete ...
     * <p>
     *
     * @param value the object to represent as string.
     * @return a appropriate string representation of the cell's content.
     */
    public String getString(Object value) {
        return formatter.getString(value);
    }

    /**
     * Returns a String representation of the content.<p>
     *
     * This method messages the
     * <code>StringValue</code> to get the String rep. Meant as
     * a convenience for subclasses.
     *
     * @param context the cell context, must not be null.
     * @return a appropriate string representation of the cell's content.
     */
    protected String getValueAsString(CellContext context) {
        Object value = context.getValue();
        return formatter.getString(value);
    }

    /**
     * Returns a Icon representation of the content.<p>
     *
     * This method messages the
     * <code>IconValue</code> to get the Icon rep. Meant as
     * a convenience for subclasses.
     *
     * @param context the cell context, must not be null.
     * @return a appropriate icon representation of the cell's content,
     *   or null if non if available.
     */
    protected Icon getValueAsIcon(CellContext context) {
        Object value = context.getValue();
        if (formatter instanceof IconValue) {
            return ((IconValue) formatter).getIcon(value);
        }
        return null;
    }

    /**
     * Configures the rendering component's default visuals frome
     * the given cell context. Here: delegates to the renderer
     * controller.
     *
     * @param context the cell context to configure from, must not be null.
     * @see DefaultVisuals
     */
    protected void configureVisuals(CellContext context) {
        defaultVisuals.configureVisuals(rendererComponent, context);
    }

    /**
     * Configures the renderering component's content and state from the
     * given cell context.
     *
     * @param context the cell context to configure from, must not be null.
     *
     * @see #configureState(CellContext)
     * @see #format(CellContext)
     */
    protected void configureContent(CellContext context) {
        configureState(context);
        format(context);
    }

    /**
     * Formats the renderering component's content from the
     * given cell context.
     *
     * @param context the cell context to configure from, must not be null.
     */
    protected abstract void format(CellContext context);

    /**
     * Configures the rendering component's state from the
     * given cell context.
     * @param context the cell context to configure from, must not be null.
     */
    protected abstract void configureState(CellContext context);

    /**
     * Factory method to create and return the component to use for rendering.<p>
     *
     * @return the component to use for rendering.
     */
    protected abstract T createRendererComponent();

    /**
     * Factory method to create and return the DefaultVisuals used by this
     * to configure the default visuals. Here: creates the default controller
     * parameterized to the same type as this.
     *
     * @return the controller used to configure the default visuals of
     *   the rendering component.
     */
    protected DefaultVisuals<T> createDefaultVisuals() {
        return new DefaultVisuals<T>();
    }

    /**
     * Intermediate exposure during refactoring...
     *
     * @return the default visual configurator used by this.
     */
    protected DefaultVisuals<T> getDefaultVisuals() {
        return defaultVisuals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI() {
        SwingUtilities.updateComponentTreeUI(rendererComponent);
    }
}
