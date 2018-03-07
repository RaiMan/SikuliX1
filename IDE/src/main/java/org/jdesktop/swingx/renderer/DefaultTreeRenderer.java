/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 * Adapter to glue SwingX renderer support to core api.
 * <p>
 *
 *
 * @author Jeanette Winzenburg
 *
 *
 */
public class DefaultTreeRenderer extends AbstractRenderer
        implements TreeCellRenderer {

    private TreeCellContext cellContext;

    /**
     * Instantiates a default tree renderer with the default component
     * provider.
     *
     */
    public DefaultTreeRenderer() {
        this((ComponentProvider<?>)null);
    }

    /**
     * Instantiates a default tree renderer with the given component provider.
     * If the controller is null, creates and uses a default. The default
     * controller is of type <code>WrappingProvider</code>.
     *
     * @param componentProvider the provider of the configured component to
     *        use for cell rendering
     */
    public DefaultTreeRenderer(ComponentProvider<?> componentProvider) {
        super(componentProvider);
        this.cellContext = new TreeCellContext();
    }

    /**
     * Instantiates a default tree renderer with the default
     * wrapping provider, using the given IconValue for
     * customizing the icons.
     *
     * @param iv the IconValue to use for mapping a custom icon
     *    for a given value
     *
     */
    public DefaultTreeRenderer(IconValue iv) {
        this(new WrappingProvider(iv));
    }

    /**
     * Instantiates a default tree renderer with a default component
     * provider using the given converter.
     *
     * @param sv the converter to use for mapping the
     *   content value to a String representation.
     *
     */
    public DefaultTreeRenderer(StringValue sv) {
        this(new WrappingProvider(sv));
    }

    /**
     * Instantiates a default tree renderer with the default
     * wrapping provider, using the given IconValue for
     * customizing the icons and the given StringValue for
     * node content.
     *
     * @param iv the IconValue to use for mapping a custom icon
     *    for a given value
     * @param sv the converter to use for mapping the
     *   content value to a String representation.
     *
     */
    public DefaultTreeRenderer(IconValue iv, StringValue sv) {
        this(new WrappingProvider(iv, sv));
    }

    /**
     * Instantiates a default tree renderer with the default
     * wrapping provider, using the given IconValue for
     * customizing the icons and the given StringValue for
     * node content.
     *
     * @param iv the IconValue to use for mapping a custom icon
     *    for a given value
     * @param sv the converter to use for mapping the
     *   content value to a String representation.
     * @param unwrapUserObject a flag indicating whether this provider
     *     should auto-unwrap the userObject from the context value.
     *
     */
    public DefaultTreeRenderer(IconValue iv, StringValue sv, boolean unwrapUserObject) {
        this(new WrappingProvider(iv, sv, unwrapUserObject));
    }

    // -------------- implements javax.swing.table.TableCellRenderer
    /**
     *
     * Returns a configured component, appropriate to render the given tree
     * cell. <p>
     *
     * @param tree the <code>JTree</code>
     * @param value the value to assign to the cell
     * @param selected true if cell is selected
     * @param expanded true if the cell is expanded
     * @param leaf true if the cell is a leaf
     * @param hasFocus true if cell has focus
     * @param row the row of the cell to render
     * @return a component to render the given list cell.
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        cellContext.installContext(tree, value, row, 0, selected, hasFocus,
                expanded, leaf);
        Component comp = componentController.getRendererComponent(cellContext);
        // fix issue #1040-swingx: memory leak if value not released
        cellContext.replaceValue(null);
        return comp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ComponentProvider<?> createDefaultComponentProvider() {
        return new WrappingProvider();
    }

}
