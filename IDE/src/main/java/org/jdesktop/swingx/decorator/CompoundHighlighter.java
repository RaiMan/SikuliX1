/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.decorator;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.util.Contract;

/**
 * A class which manages the lists of <code>Highlighter</code>s.
 *
 * @see Highlighter
 *
 * @author Ramesh Gupta
 * @author Jeanette Winzenburg
 *
 */
public class CompoundHighlighter extends AbstractHighlighter
    implements UIDependent {
    public static final Highlighter[] EMPTY_HIGHLIGHTERS = new Highlighter[0];

    protected List<Highlighter> highlighters;

    /** the listener for changes in contained Highlighters. */
    private ChangeListener highlighterChangeListener;

    /**
     * Instantiates a CompoundHighlighter containing the given
     * <code>Highlighter</code>s.
     *
     * @param inList zero or more not-null Highlighters to manage by this
     *   CompoundHighlighter.
     * @throws NullPointerException if array is null or array contains null values.
     */
    public CompoundHighlighter(Highlighter... inList) {
        this(null, inList);
    }

    /**
     * Instantiates a CompoundHighlighter with the given predicate containing the given
     * <code>Highlighter</code>s.
     *
     * @param predicate the highlightPredicate to use
     * @param inList zero or more not-null Highlighters to manage by this
     *   CompoundHighlighter.
     * @throws NullPointerException if array is null or array contains null values.
     */
    public CompoundHighlighter(HighlightPredicate predicate, Highlighter... inList) {
        super(predicate);
        highlighters = new ArrayList<Highlighter>();
        setHighlighters(inList);
    }

    /**
     * Sets the given
     * <code>Highlighter</code>s.
     *
     * @param inList zero or more not-null Highlighters to manage by this
     *   CompoundHighlighter.
     * @throws NullPointerException if array is null or array contains null values.
     */
    public void setHighlighters(Highlighter... inList) {
        Contract.asNotNull(inList, "Highlighter must not be null");
        if (highlighters.isEmpty() && (inList.length == 0)) return;
        removeAllHighlightersSilently();
        for (Highlighter highlighter : inList) {
            addHighlighterSilently(highlighter, false);
        }
        fireStateChanged();
    }

    /**
     * Removes all contained highlighters without firing an event.
     * Deregisters the listener from all.
     */
    private void removeAllHighlightersSilently() {
        for (Highlighter highlighter : highlighters) {
            highlighter.removeChangeListener(getHighlighterChangeListener());
        }
        highlighters.clear();
    }

    /**
     * Appends a highlighter to the pipeline.
     *
     * @param highlighter highlighter to add
      * @throws NullPointerException if highlighter is null.
    */
    public void addHighlighter(Highlighter highlighter) {
        addHighlighter(highlighter, false);
    }

    /**
     * Adds a highlighter to the pipeline.
     *
     * PENDING: Duplicate inserts?
     *
     * @param highlighter highlighter to add
     * @param prepend prepend the highlighter if true; false will append
     * @throws NullPointerException if highlighter is null.
     */
    public void addHighlighter(Highlighter highlighter, boolean prepend) {
        addHighlighterSilently(highlighter, prepend);
        fireStateChanged();
    }

    private void addHighlighterSilently(Highlighter highlighter, boolean prepend) {
        Contract.asNotNull(highlighter, "Highlighter must not be null");
        if (prepend) {
            highlighters.add(0, highlighter);
        } else {
            highlighters.add(highlighters.size(), highlighter);
        }
        updateUI(highlighter);
        highlighter.addChangeListener(getHighlighterChangeListener());
    }

    /**
     * Removes a highlighter from the pipeline.
     *
     *
     * @param hl highlighter to remove
     */
    public void removeHighlighter(Highlighter hl) {
        boolean success = highlighters.remove(hl);
        if (success) {
            // PENDING: duplicates?
            hl.removeChangeListener(getHighlighterChangeListener());
            fireStateChanged();
        }
        // should log if this didn't succeed. Maybe
    }

    /**
     * Returns an array of contained Highlighters.
     *
     * @return the contained Highlighters, might be empty but never null.
     */
    public Highlighter[] getHighlighters() {
        if (highlighters.isEmpty()) return EMPTY_HIGHLIGHTERS;
        return highlighters.toArray(new Highlighter[highlighters.size()]);
    }

//--------------------- implement UIDependent

    /**
     * {@inheritDoc} <p>
     *
     * Implemented to call updateUI on contained Highlighters.
     */
    @Override
    public void updateUI() {
        for (Highlighter highlighter : highlighters) {
            updateUI(highlighter);
        }
    }

    /**
     * Returns the <code>ChangeListner</code> to contained
     * <code>Highlighter</code>s. The listener is lazily created.
     *
     * @return the listener for contained highlighters, guaranteed
     *   to be not null.
     */
    protected ChangeListener getHighlighterChangeListener() {
        if (highlighterChangeListener == null) {
            highlighterChangeListener = createHighlighterChangeListener();
        }
        return highlighterChangeListener;
    }

    /**
     * Creates and returns the ChangeListener registered to
     * contained <code>Highlighter</code>s. Here: fires a
     * stateChanged on each notification.
     *
     * @return the listener for contained Highlighters.
     *
     */
    protected ChangeListener createHighlighterChangeListener() {
        return highlighterChangeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                fireStateChanged();
            }

        };
    }

   /**
    *  Updates the ui-dependent state of the given Highlighter.
    *
     * @param hl the highlighter to update.
     */
    private void updateUI(Highlighter hl) {
        if (hl instanceof UIDependent) {
            ((UIDependent) hl).updateUI();
        }
    }

//------------------- implement Highlighter

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component doHighlight(Component stamp, ComponentAdapter adapter) {
        for (Highlighter highlighter : highlighters) {
            stamp = highlighter.highlight(stamp, adapter);
        }
        return stamp;
    }


}
