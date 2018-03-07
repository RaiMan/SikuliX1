/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.JComponent;

/**
 * Encapsulates the default visual configuration of renderering components,
 * respecting the state of the passed-in <code>CellContext</code>. It's
 * basically re-usable across all types of renderees (JTable, JList, JTree).
 * <p>
 *
 * Guarantees to completely configure the default visual properties (listed
 * below) of a given component. As a consequence, client code (f.i. in
 * <code>Highlighter</code>s) can safely change them without long-lasting
 * visual artefacts.
 *
 * <ul>
 * <li> foreground and background, depending on selected and focused state
 * <li> border
 * <li> font
 * <li> Painter (if applicable)
 * <li> enabled
 * <li> componentOrientation
 * <li> toolTipText
 * <li> minimum-, maximum-, preferredSize
 * <li> name
 * </ul>
 *
 * Client code will rarely need to be aware of this class. It's the single
 * place to change on introduction of new properties considered as belonging
 * to the "default visuals" of rendering components. <p>
 *
 * PENDING: allow mutators for overruling the <code>CellContext</code>s
 * defaults? Would prefer not to, as in the context of SwingX visual config on
 * the renderer level is discouraged (the way to go are <code>Highlighter</code>s.<p>
 *
 * PENDING: not yet quite decided whether the toolTipText property belongs
 * into the visual default config. Doing so gives client code the choice to
 * set it either in a Highlighter or a custom ComponentProvider.
 *
 * @author Jeanette Winzenburg
 *
 * @see CellContext
 */
public class DefaultVisuals<T extends JComponent> implements Serializable {

    private Color unselectedForeground;

    private Color unselectedBackground;

    /**
     * Sets the renderer's unselected-foreground color to the specified color.
     * If <code>not null</code> this color will overrule the default color of
     * the CellContext.
     *
     * @param c set the foreground color to this value
     */
    public void setForeground(Color c) {
        unselectedForeground = c;
    }

    /**
     * Sets the renderer's unselected-background color to the specified color.
     * If <code>not null</code> this color will overrule the default color of
     * the CellContext.
     *
     * @param c set the background color to this value
     */
    public void setBackground(Color c) {
        unselectedBackground = c;
    }

    //---------------- subclass configuration
    /**
     * Configures all default visual state of the rendering component from the
     * given cell context.
     *
     * @param renderingComponent the component to configure, must not be null
     * @param context the cell context to configure from, must not be null
     * @throws NullPointerException if either renderingComponent or cellContext
     *   is null
     */
    public void configureVisuals(T renderingComponent, CellContext context) {
        configureState(renderingComponent, context);
        configureColors(renderingComponent, context);
        configureBorder(renderingComponent, context);
        configurePainter(renderingComponent, context);
    }

    /**
     * Configures the default Painter if applicable. Here: set's to null.
     *
     * @param renderingComponent the component to configure, must not be null
     * @param context the cell context to configure from, must not be null
     */
    protected void configurePainter(T renderingComponent, CellContext context) {
        if (renderingComponent instanceof PainterAware) {
            ((PainterAware) renderingComponent).setPainter(null);
        }

    }

    /**
     * Configure "divers" visual state of the rendering component from the given
     * cell context.
     * <p>
     *
     * Here: synch <code>Font</code>, <code>ComponentOrientation</code> and
     * <code>enabled</code> to context's component. Resets toolTipText to null.
     * Calls configureSizes to reset xxSize if appropriate. Resets the component's
     * name property.
     * <p>
     *
     * PENDING: not fully defined - "divers" means everything that's not
     * <code>Color</code>s
     * nor <code>Border</code> nor <code>Painter</code>.
     *
     * @param renderingComponent the component to configure, must not be null
     * @param context the cell context to configure from, must not be null
     */
    protected void configureState(T renderingComponent, CellContext context) {
        renderingComponent.setName(context.getCellRendererName());
        renderingComponent.setToolTipText(null);
        configureSizes(renderingComponent, context);
        // PENDING JW: as of Issue #1269 this was changed to query the
        // CellContext for the font - should move out off the else?
        // context takes care of null component
        renderingComponent.setFont(context.getFont());
        if (context.getComponent() == null) {
            // what to do?
            // we guarantee to cleanup completely - what are the defaults?
            // leave the decistion to the context?
        } else {
            renderingComponent.setEnabled(context.getComponent().isEnabled());
            renderingComponent.applyComponentOrientation(context.getComponent()
                    .getComponentOrientation());
        }
    }

    /**
     * Configures min-, max, preferredSize properties of the renderingComponent.
     *
     * Here: set all to null.
     *
     * @param renderingComponent the component to configure, must not be null
     * @param context the cell context to configure from, must not be null
     */
    protected void configureSizes(T renderingComponent, CellContext context) {
        renderingComponent.setPreferredSize(null);
        renderingComponent.setMinimumSize(null);
        renderingComponent.setMaximumSize(null);
    }

    /**
     * Configures colors of rendering component from the given cell context.
     *
     * @param renderingComponent the component to configure, must not be null
     * @param context the cell context to configure from, must not be null
     */
    protected void configureColors(T renderingComponent, CellContext context) {
        if (context.isSelected()) {
            renderingComponent.setForeground(context.getSelectionForeground());
            renderingComponent.setBackground(context.getSelectionBackground());
        } else {
            renderingComponent.setForeground(getForeground(context));
            renderingComponent.setBackground(getBackground(context));
        }
        if (context.isFocused()) {
            configureFocusColors(renderingComponent, context);
        }
    }
    /**
     * Configures focus-related colors form given cell context.<p>
     *
     * PENDING: move to context as well? - it's the only comp
     * with focus specifics? Problem is the parameter type...
     *
     * @param renderingComponent the component to configure, must not be null
     * @param context the cell context to configure from, must not be null
     */
    protected void configureFocusColors(T renderingComponent, CellContext context) {
        if (!context.isSelected() && context.isEditable()) {
            Color col = context.getFocusForeground();
            if (col != null) {
                renderingComponent.setForeground(col);
            }
            col = context.getFocusBackground();
            if (col != null) {
                renderingComponent.setBackground(col);
            }
        }
    }

    /**
     * Configures the rendering component's border from the given cell context.<p>
     *
     * @param renderingComponent the component to configure, must not be null
     * @param context the cell context to configure from, must not be null
     */
    protected void configureBorder(T renderingComponent, CellContext context) {
        renderingComponent.setBorder(context.getBorder());
    }

    /**
     * Returns the unselected foreground to use for the rendering
     * component. <p>
     *
     * Here: returns this renderer's unselected foreground is not null,
     * returns the foreground from the given context. In other words:
     * the renderer's foreground takes precedence if set.
     *
     * @param context the cell context.
     * @return the unselected foreground.
     */
    protected Color getForeground(CellContext context) {
        if (unselectedForeground != null)
            return unselectedForeground;
        return context.getForeground();
    }

    /**
     * Returns the unselected background to use for the rendering
     * component. <p>
     *
     * Here: returns this renderer's unselected background is not null,
     * returns the background from the given context. In other words:
     * the renderer's background takes precedence if set.
     *
     * @param context the cell context.
     * @return the unselected background.
     */
    protected Color getBackground(CellContext context) {
        if (unselectedBackground != null)
            return unselectedBackground;
        return context.getBackground();
    }


}
