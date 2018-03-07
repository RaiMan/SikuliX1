/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.swingx.painter.AbstractPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.renderer.PainterAware;

/**
 * Highlighter implementation which uses a Painter to decorate the component.
 * <p>
 *
 * As Painter implementations can be mutable and Highlighters have the
 * responsibility to notify their own listeners about any changes which might
 * effect the visuals, this class provides api to install/uninstall a listener
 * to the painter, as appropriate. It takes care of Painters of type
 * AbstractHighlighter by registering a PropertyChangeListener. Subclasses might
 * override to correctly handle different types as well.
 * <p>
 *
 * Subclasses might be implemented to change the Painter during the decoration
 * process, which must not passed-on to the Highlighter's listeners. The default
 * routing is controlled by a flag isAdjusting. This is set/reset in this
 * implementation's highlight method to ease subclass' burden (and to keep
 * backward compatibility with implementations preceding the introduction of the
 * painter listener). That is, subclasses are free to change painter properties
 * during the decoration.
 * <p>
 *
 * As an example, a ValueBasedPainterHighlighter might safely change any painter
 * property to decorate a component depending on content.
 *
 * <pre><code>
 * &#64;Override
 * protected Component doHighlight(Component renderer, ComponentAdapter adapter) {
 *      float end = getEndOfGradient((Number) adapter.getValue());
 *      RelativePainter painter = (RelativePainter) getPainter();
 *      painter.setXFraction(end);
 *      ((PainterAware) renderer).setPainter(painter);
 *      return renderer;
 * }
 *
 * &#64;Override
 * protected boolean canHighlight(Component renderer, ComponentAdapter adapter) {
 *     return super.canHighlight(renderer, adapter) &&
 *        (adapter.getValue() instanceof Number);
 * }
 * </code></pre>
 *
 * NOTE: this will change once the Painter api is stable.
 *
 * @author Jeanette Winzenburg
 */
public class PainterHighlighter extends AbstractHighlighter {

    /** The painter to use for decoration. */
    private Painter painter;
    /** The listener registered with the Painter. */
    private PropertyChangeListener painterListener;
    /**
     * A flag indicating whether or not changes in the Painter
     * should be passed-on to the Highlighter's ChangeListeners.
     */
    private boolean isAdjusting;

    /**
     * Instantiates a PainterHighlighter with null painter and
     * default predicate.
     */
    public PainterHighlighter() {
        this(null, null);
    }
    /**
     * Instantiates a PainterHighlighter with null painter which
     * uses the given predicate.
     *
     * @param predicate the HighlightPredicate which controls the highlight
     *   application.
     */
    public PainterHighlighter(HighlightPredicate predicate) {
        this(predicate, null);
    }

    /**
     * Instantiates a PainterHighlighter with the given Painter and
     * default predicate.
     *
     * @param painter the painter to use
     */
    public PainterHighlighter(Painter painter) {
        this(null, painter);
    }

    /**
     * Instantiates a PainterHighlighter with the given painter and
     * predicate.
     * @param predicate
     * @param painter
     */
    public PainterHighlighter(HighlightPredicate predicate, Painter painter) {
        super(predicate);
        setPainter(painter);
    }


    /**
     * Returns to Painter used in this Highlighter.
     *
     * @return the Painter used in this Highlighter, may be null.
     */
    public Painter getPainter() {
        return painter;
    }

    /**
     * Sets the Painter to use in this Highlighter, may be null.
     * Un/installs the listener to changes painter's properties.
     *
     * @param painter the Painter to uses for decoration.
     */
    public void setPainter(Painter painter) {
        if (areEqual(painter, getPainter())) return;
        uninstallPainterListener();
        this.painter = painter;
        installPainterListener();
        fireStateChanged();
    }

    /**
     * Installs a listener to the painter if appropriate.
     * This implementation registers its painterListener if
     * the Painter is of type AbstractPainter.
     */
    protected void installPainterListener() {
        if (getPainter() instanceof AbstractPainter) {
            ((AbstractPainter) getPainter()).addPropertyChangeListener(getPainterListener());
        }
    }

    /**
     * Uninstalls a listener from the painter if appropriate.
     * This implementation removes its painterListener if
     * the Painter is of type AbstractPainter.
     */
    protected void uninstallPainterListener() {
        if (getPainter() instanceof AbstractPainter) {
            ((AbstractPainter) getPainter()).removePropertyChangeListener(painterListener);
        }
    }

    /**
     * Lazyly creates and returns the property change listener used
     * to listen to changes of the painter.
     *
     * @return the property change listener used to listen to changes
     *   of the painter.
     */
    protected final PropertyChangeListener getPainterListener() {
        if (painterListener == null) {
            painterListener = createPainterListener();
        }
        return painterListener;
    }

    /**
     * Creates and returns the property change listener used
     * to listen to changes of the painter. <p>
     *
     * This implementation fires a stateChanged on receiving
     * any propertyChange, if the isAdjusting flag is false.
     * Otherwise does nothing.
     *
     * @return the property change listener used to listen to changes
     *   of the painter.
     */
    protected PropertyChangeListener createPainterListener() {
        PropertyChangeListener l = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isAdjusting) return;
                fireStateChanged();
            }

        };
        return l;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to set/reset the flag indicating whether or not
     * painter's property changes should be passed on to the
     * Highlighter's listener.
     */
    @Override
    public Component highlight(Component component, ComponentAdapter adapter) {
        isAdjusting = true;
        Component stamp = super.highlight(component, adapter);
        isAdjusting = false;
        return stamp;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the painter if it is not null. Does nothing
     * otherwise.
     */
    @Override
    protected Component doHighlight(Component component,
            ComponentAdapter adapter) {
       ((PainterAware) component).setPainter(painter);
        return component;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return false if the Painter is null or the component is not
     *   of type PainterAware.
     */
    @Override
    protected boolean canHighlight(Component component, ComponentAdapter adapter) {
        return getPainter() != null && (component instanceof PainterAware);
    }

}
