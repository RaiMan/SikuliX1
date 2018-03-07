/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.synth;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;

/**
 * Utility class as stand-in for package private synth utility methods.
 *
 * @author Jeanette Winzenburg
 */
public class SynthUtils {

//----------------------- context-related

    /**
     * Used to avoid null painter checks everywhere.
     */
    private static SynthPainter NULL_PAINTER = new SynthPainter() {};

    /**
     * Returns a SynthContext with the specified values.
     *
     * @param component JComponent
     * @param region Identifies the portion of the JComponent
     * @param style Style associated with the component
     * @param state State of the component as defined in SynthConstants.
     * @return a SynthContext with the specified values.
     *
     * @throws NullPointerException if component, region of style is null.
     *
     */
    public static SynthContext getContext(JComponent c, Region region, SynthStyle style, int state) {
        return new SynthContext(c, region, style, state);
    }

    /**
     * @param context
     * @param style
     * @return
     */
    public static SynthContext getContext(SynthContext context, SynthStyle style) {
        if (context.getStyle().equals(style)) return context;
        return getContext(context.getComponent(), context.getRegion(), style, context.getComponentState());
    }
    /**
     * Returns a context with the given component state and all other fields same as input context.
     *
     * @param context the context, must not be null
     * @param state the component state.
     * @return a context with the given component state and other fields as inpu context.
     */
    public static SynthContext getContext(SynthContext context, int state) {
        if (context.getComponentState() == state) return context;
        return getContext(context.getComponent(), context.getRegion(), context.getStyle(), state);
    }

    /**
     * Returns a SynthPainter from the context's style. Fall-back to default if
     * none available.
     *
     * @param context SynthContext containing the style, must not be null.
     * @return a SynthPainter from the context's style, or a default if null.
     */
    public static SynthPainter getPainter(SynthContext context) {
        SynthPainter painter = context.getStyle().getPainter(context);
        return painter != null ? painter : NULL_PAINTER;
    }

//------------------- style-related

    /**
     * Returns true if the Style should be updated in response to the
     * specified PropertyChangeEvent. This forwards to
     * <code>shouldUpdateStyleOnAncestorChanged</code> as necessary.
     */
    public static boolean shouldUpdateStyle(PropertyChangeEvent event) {
        String eName = event.getPropertyName();
        if ("name" == eName) {
            // Always update on a name change
            return true;
        }
        else if ("componentOrientation" == eName) {
            // Always update on a component orientation change
            return true;
        }
        else if ("ancestor" == eName && event.getNewValue() != null) {
            // Only update on an ancestor change when getting a valid
            // parent and the LookAndFeel wants this.
            LookAndFeel laf = UIManager.getLookAndFeel();
            return (laf instanceof SynthLookAndFeel &&
                    ((SynthLookAndFeel)laf).
                     shouldUpdateStyleOnAncestorChanged());
        }
        // Note: The following two nimbus based overrides should be refactored
        // to be in the Nimbus LAF. Due to constraints in an update release,
        // we couldn't actually provide the public API necessary to allow
        // NimbusLookAndFeel (a subclass of SynthLookAndFeel) to provide its
        // own rules for shouldUpdateStyle.
        else if ("Nimbus.Overrides" == eName) {
            // Always update when the Nimbus.Overrides client property has
            // been changed
            return true;
        }
        else if ("Nimbus.Overrides.InheritDefaults" == eName) {
            // Always update when the Nimbus.Overrides.InheritDefaults
            // client property has changed
            return true;
        }
        else if ("JComponent.sizeVariant" == eName) {
            // Always update when the JComponent.sizeVariant
            // client property has changed
            return true;
        }
        return false;
    }

//--------------- component related

    public static int getComponentState(JComponent c) {
        if (c.isEnabled()) {
            if (c.isFocusOwner()) {
                return SynthConstants.ENABLED | SynthConstants.FOCUSED;
            }
            return SynthConstants.ENABLED;
        }
        return SynthConstants.DISABLED;
    }

    // ---------------- divers ...

    /**
     * A convenience method that handles painting of the background. All SynthUI
     * implementations should override update and invoke this method.
     *
     * @param context must not be null
     * @param g must not be null
     */
    public static void update(SynthContext context, Graphics g) {
        update(context, g, null);
    }

    /**
     * A convenience method that handles painting of the background. All SynthUI
     * implementations should override update and invoke this method.
     *
     * @param context must not be null
     * @param g must not be null
     * @param the bounds to fill, may be null to indicate the complete size
     */
    public static void update(SynthContext context, Graphics g, Rectangle bounds) {
        JComponent c = context.getComponent();
        SynthStyle style = context.getStyle();
        int x, y, width, height;

        if (bounds == null) {
            x = 0;
            y = 0;
            width = c.getWidth();
            height = c.getHeight();
        } else {
            x = bounds.x;
            y = bounds.y;
            width = bounds.width;
            height = bounds.height;
        }

        // Fill in the background, if necessary.
        boolean subregion = context.getRegion().isSubregion();
        if ((subregion && style.isOpaque(context))
                || (!subregion && c.isOpaque())) {
            g.setColor(style.getColor(context, ColorType.BACKGROUND));
            g.fillRect(x, y, width, height);
        }
    }

}
