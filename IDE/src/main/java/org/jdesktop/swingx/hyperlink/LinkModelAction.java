/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.hyperlink;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

/**
 * Specialized LinkAction for a target of type {@link LinkModel}.
 * <p>
 *
 * This action delegates actionPerformed to vistingDelegate.
 *
 * PENDING: move to swingx package?
 *
 * @author Jeanette Winzenburg
 */
public class LinkModelAction<T extends LinkModel> extends AbstractHyperlinkAction<T> {

    private ActionListener delegate;
    public static final String VISIT_ACTION = "visit";
    private PropertyChangeListener linkListener;

    public LinkModelAction() {
        this((T) null);
    }

    public LinkModelAction(ActionListener visitingDelegate) {
        this(null, visitingDelegate);
    }

    public LinkModelAction(T target) {
        this(target, null);
    }

    public LinkModelAction(T target, ActionListener visitingDelegate) {
        super(target);
        setVisitingDelegate(visitingDelegate);
    }

    /**
     * The delegate to invoke on actionPerformed.
     * <p>
     * The delegates actionPerformed is invoked with an ActionEvent
     * having the target as source. Delegates are expected to
     * cope gracefully with the T.
     * <p>
     *
     * PENDING: JW - How to formalize?
     *
     * @param delegate the action invoked on the target.
     */
    public void setVisitingDelegate(ActionListener delegate) {
        this.delegate = delegate;
    }

    /**
     * This action delegates to the visitingDelegate if both
     * delegate and target are != null, does nothing otherwise.
     * The actionEvent carries the target as source.
     *
     * PENDING: pass through a null target? - most probably!
     *
     *
     *
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if ((delegate != null) && (getTarget() != null)) {
            delegate.actionPerformed(new ActionEvent(getTarget(), ActionEvent.ACTION_PERFORMED, VISIT_ACTION));
        }

    }

    /**
     * installs a propertyChangeListener on the target and
     * updates the visual properties from the target.
     */
    @Override
    protected void installTarget() {
        if (getTarget() != null) {
            getTarget().addPropertyChangeListener(getTargetListener());
        }
        updateFromTarget();
    }

    /**
     * removes the propertyChangeListener. <p>
     *
     * Implementation NOTE: this does not clean-up internal state! There is
     * no need to because updateFromTarget handles both null and not-null
     * targets. Hmm...
     *
     */
    @Override
    protected void uninstallTarget() {
        if (getTarget() == null) return;
       getTarget().removePropertyChangeListener(getTargetListener());
    }

    protected void updateFromTarget() {
        if (getTarget() != null) {
            putValue(Action.NAME, getTarget().getText());
            putValue(Action.SHORT_DESCRIPTION, getTarget().getURL().toString());
            putValue(VISITED_KEY, new Boolean(getTarget().getVisited()));
        } else {
            Object[] keys = getKeys();
            if (keys == null) return;
            for (int i = 0; i < keys.length; i++) {
               putValue(keys[i].toString(), null);
            }
        }
    }

    private PropertyChangeListener getTargetListener() {
        if (linkListener == null) {
         linkListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateFromTarget();
            }

        };
        }
        return linkListener;
    }

}
