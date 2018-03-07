/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * Created on 31.03.2011
 *
 */
package org.jdesktop.swingx.decorator;

import java.awt.Component;
import java.awt.ComponentOrientation;

/**
 * A Highlighter which applies the ComponentOrientation to the component.
 *
 * @author Jeanette Winzenburg, Berlin
 */
public class ComponentOrientationHighlighter extends AbstractHighlighter {

    private ComponentOrientation co;

    /**
     * Instantiates a ComponentOrientationHighlighter with <code>ComponentOrientation.LEFT_TO_RIGHT</code>.
     * The Highlighter is applied always.
     */
    public ComponentOrientationHighlighter() {
        this((HighlightPredicate) null);
    }

    /**
     * Instantiates a ComponentOrientationHighlighter with the given HighlightPredicate
     * and <code>ComponentOrientation.LEFT_TO_RIGHT</code>.
     *
     * @param predicate the HighlightPredicate to use, may be null to default to ALWAYS.
     */
    public ComponentOrientationHighlighter(HighlightPredicate predicate) {
        this(predicate, null);
    }

    /**
     * Instantiates a ComponentOrientationHighlighter with the given ComponentOrientation.
     * The Highlighter is applied always.
     *
     * @param co the ComponentOrientation to apply
     */
    public ComponentOrientationHighlighter(ComponentOrientation co) {
        this(null, co);
    }

    /**
     * Instantiates a ComponentOrientationHighlighter with the given ComponentOrientation and HighlightPredicate.
     *
     * @param predicate the HighlightPredicate to use, may be null to default to ALWAYS.
     * @param co the ComponentOrientation to apply, may be null
     */
    public ComponentOrientationHighlighter(HighlightPredicate predicate,
            ComponentOrientation co) {
        super(predicate);
        setComponentOrientation(co);
    }

    /**
     * Returns the ComponentOrientation to apply.
     *
     * @return the ComponentOrientation to apply, guaranteed to be not null.
     */
    public ComponentOrientation getComponentOrientation() {
        return co;
    }

    /**
     * Sets the ComponentOrientation to apply.
     *
     * @param co the co to set, may be null to denote fallback to LEFT_TO_RIGHT
     */
    public void setComponentOrientation(ComponentOrientation co) {
        if (co == null) {
            co = ComponentOrientation.LEFT_TO_RIGHT;
        }
        if (areEqual(this.co, co)) return;
        this.co = co;
        fireStateChanged();
    }

    /**
     * @inherited <p>
     * Implementated to decorate the given component with the ComponentOrientation.
     */
    @Override
    protected Component doHighlight(Component component,
            ComponentAdapter adapter) {
        component.applyComponentOrientation(getComponentOrientation());
        return component;
    }

}
