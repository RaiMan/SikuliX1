/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Component;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.event.WeakEventListenerList;

/**
 * Abstract <code>Highlighter</code> implementation which manages change
 * notification and supports conditional highlighting.
 * Subclasses are required to fire ChangeEvents on internal changes which might
 * effect the highlight. The HighlightPredicate controls whether or not
 * a highlight should be applied for the given ComponentAdapter,
 * subclasses must guarantee to respect its decision.
 * <p>
 *
 * Concrete custom implementations should focus on a single (or few) visual
 * attribute to highlight. This allows easy re-use by composition.  F.i. a custom
 * FontHighlighter:
 *
 * <pre><code>
 * public static class FontHighlighter extends AbstractHighlighter {
 *
 *     private Font font;
 *
 *     public FontHighlighter(HighlightPredicate predicate, Font font) {
 *         super(predicate);
 *         setFont(font);
 *     }
 *
 *     &#64;Override
 *     protected Component doHighlight(Component component,
 *             ComponentAdapter adapter) {
 *         component.setFont(font);
 *         return component;
 *     }
 *
 *     public final void setFont(Font font) {
 *        if (equals(font, this.font)) return;
 *        this.font = font;
 *        fireStateChanged();
 *     }
 *
 *
 * }
 *
 * </code></pre>
 *
 * Client code can combine the effect with a f.i. Color decoration, and use a
 * shared HighlightPredicate to apply both for the same condition.
 *
 * <pre><code>
 * HighlightPredicate predicate = new HighlightPredicate() {
 *     public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
 *         Object value = adapter.getFilteredValueAt(adapter.row, adapter.column);
 *         return (value instanceof Number) &amp;&amp; ((Number) value).intValue() &lt; 0;
 *     }
 * };
 * table.setHighlighters(
 *         new ColorHighlighter(predicate, Color.RED, null),
 *         new FontHighlighter(predicate, myBoldFont));
 * </code></pre>
 *
 * @author Jeanette Winzenburg
 *
 * @see HighlightPredicate
 * @see org.jdesktop.swingx.renderer.ComponentProvider
 */
public abstract class AbstractHighlighter implements Highlighter {

    /**
     * Only one <code>ChangeEvent</code> is needed per model instance since the
     * event's only (read-only) state is the source property.  The source
     * of events generated here is always "this".
     */
    private transient ChangeEvent changeEvent;

    /** The listeners waiting for model changes. */
    protected WeakEventListenerList listenerList = new WeakEventListenerList();

    /** the HighlightPredicate to use. */
    private HighlightPredicate predicate;

    /**
     * Instantiates a Highlighter with default HighlightPredicate.
     *
     * @see #setHighlightPredicate(HighlightPredicate)
     */
    public AbstractHighlighter() {
        this(null);
    }

    /**
     * Instantiates a Highlighter with the given
     * HighlightPredicate.<p>
     *
     * @param predicate the HighlightPredicate to use.
     *
     * @see #setHighlightPredicate(HighlightPredicate)
     */
    public AbstractHighlighter(HighlightPredicate predicate) {
        setHighlightPredicate(predicate);
    }

    /**
     * Set the HighlightPredicate used to decide whether a cell should
     * be highlighted. If null, sets the predicate to HighlightPredicate.ALWAYS.
     *
     * The default value is HighlightPredicate.ALWAYS.
     *
     * @param predicate the HighlightPredicate to use.
     */
    public void setHighlightPredicate(HighlightPredicate predicate) {
        if (predicate == null) {
            predicate = HighlightPredicate.ALWAYS;
        }
        if (areEqual(predicate, getHighlightPredicate())) return;
        this.predicate = predicate;
        fireStateChanged();
    }

    /**
     * Returns the HighlightPredicate used to decide whether a cell
     * should be highlighted. Guaranteed to be never null.
     *
     * @return the HighlightPredicate to use, never null.
     */
    public HighlightPredicate getHighlightPredicate() {
        return predicate;
    }

    //----------------------- implement predicate respecting highlight

    /**
     * {@inheritDoc}
     *
     * This calls doHighlight to apply the decoration if both HighlightPredicate
     * isHighlighted and canHighlight return true. Returns the undecorated component otherwise.
     *
     * @param component the cell renderer component that is to be decorated
     * @param adapter the ComponentAdapter for this decorate operation
     *
     * @see #canHighlight(Component, ComponentAdapter)
     * @see #doHighlight(Component, ComponentAdapter)
     * @see #getHighlightPredicate()
     */
    @Override
    public Component highlight(Component component, ComponentAdapter adapter) {
        if (canHighlight(component, adapter) &&
                getHighlightPredicate().isHighlighted(component, adapter)) {
            component = doHighlight(component, adapter);
        }
        return component;
    }

    /**
     * Subclasses may override to further limit the highlighting based
     * on Highlighter state, f.i. a PainterHighlighter can only be applied
     * to PainterAware components. <p>
     *
     * This implementation returns true always.
     *
     * @param component
     * @param adapter
     * @return a boolean indication if the adapter can be highlighted based
     *   general state. This implementation returns true always.
     */
    protected boolean canHighlight(Component component, ComponentAdapter adapter) {
        return true;
    }

    /**
     * Apply the highlights.
     *
     * @param component the cell renderer component that is to be decorated
     * @param adapter the ComponentAdapter for this decorate operation
     *
     * @see #highlight(Component, ComponentAdapter)
     */
    protected abstract Component doHighlight(Component component,
            ComponentAdapter adapter);

    /**
     * Returns true if the to objects are either both null or equal
     * each other.
     *
     * @param oneItem one item
     * @param anotherItem another item
     * @return true if both are null or equal other, false otherwise.
     */
    protected boolean areEqual(Object oneItem, Object anotherItem) {
        if ((oneItem == null) && (anotherItem == null)) return true;
        if (anotherItem != null) {
            return anotherItem.equals(oneItem);
        }
        return false;
    }

    //------------------------ implement Highlighter change notification

    /**
     * Adds a <code>ChangeListener</code>. ChangeListeners are
     * notified after changes of any attribute.
     *
     * @param l the ChangeListener to add
     * @see #removeChangeListener
     */
    @Override
    public final void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    /**
     * Removes a <code>ChangeListener</code>e.
     *
     * @param l the <code>ChangeListener</code> to remove
     * @see #addChangeListener
     */
    @Override
    public final void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Returns an array of all the change listeners
     * registered on this <code>Highlighter</code>.
     *
     * @return all of this model's <code>ChangeListener</code>s
     *         or an empty
     *         array if no change listeners are currently registered
     *
     * @see #addChangeListener
     * @see #removeChangeListener
     *
     * @since 1.4
     */
    @Override
    public final ChangeListener[] getChangeListeners() {
        return listenerList.getListeners(ChangeListener.class);
    }

    /**
     * Notifies registered <code>ChangeListener</code>s about
     * state changes.<p>
     *
     * Note: subclasses should be polite and implement any property
     * setters to fire only if the property is really changed.
     *
     */
    protected final void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

}
