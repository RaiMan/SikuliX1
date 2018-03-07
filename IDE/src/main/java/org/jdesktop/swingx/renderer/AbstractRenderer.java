/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Color;
import java.io.Serializable;

import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.rollover.RolloverRenderer;

/**
 * Convenience common ancestor for SwingX renderers. Concrete subclasses
 * should
 *
 *  <ul>
 *  <li> provide a bunch of convenience constructors as appropriate for the type of
 *      collection component
 *  <li> create a reasonable default ComponentProvider if none is given
 *  <li> implement the getXXCellRenderer by delegating to the ComponentProvider
 *  </ul>
 *
 * @author Jeanette Winzenburg
 */
public abstract class AbstractRenderer
    implements  RolloverRenderer, StringValue, Serializable, UIDependent {

    protected ComponentProvider<?> componentController;

    public AbstractRenderer(ComponentProvider<?> provider) {
        if (provider ==  null) {
            provider = createDefaultComponentProvider();
        }
        this.componentController = provider;
    }

    /**
     * Returns the ComponentProvider used by this renderer.
     *
     * @return the ComponentProvider used by this renderer
     */
    public ComponentProvider<?> getComponentProvider() {
        return componentController;
    }

    /**
     * The default ComponentProvider to use if no special.
     *
     * @return the default <code>ComponentProvider</code>
     */
    protected abstract ComponentProvider<?> createDefaultComponentProvider();

// --------------- implement StringValue

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(Object value) {
        return componentController.getString(value);
    }

 // ------------ implement RolloverRenderer

    /**
     * {@inheritDoc}
     */
    @Override
    public void doClick() {
        if (isEnabled()) {
            ((RolloverRenderer) componentController).doClick();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return (componentController instanceof RolloverRenderer)
                && ((RolloverRenderer) componentController).isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI() {
        componentController.updateUI();
    }

//-------------------- legacy: configure arbitrary visuals
    /**
     * @param background
     */
    public void setBackground(Color background) {
        componentController.getDefaultVisuals().setBackground(background);

    }

    /**
     * @param foreground
     */
    public void setForeground(Color foreground) {
        componentController.getDefaultVisuals().setForeground(foreground);
    }

}
