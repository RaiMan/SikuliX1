/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.synth;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.plaf.synth.Region;

/**
 * Extended Region to register custom component delegates.
 *
 * @author Jeanette Winzenburg
 */
public class XRegion extends Region {

    static Map<String, XRegion> uiToXRegionMap = new HashMap<String, XRegion>();
    public static final Region XLIST = new XRegion("XList", null, false, "XListUI", LIST);

    /** the Region which identifies the base styles */
    private Region parent;

    /**
     * Creates a XRegion with the specified name.
     *
     * @param name Name of the region
     * @param subregion Whether or not this is a subregion.
     * @param realUI String that will be returned from
     *           <code>component.getUIClassID</code>.
     * @param parent the parent region which this is extending.
     */
    public XRegion(String name, String dummyUI, boolean subregion, String realUI, Region parent) {
        super(name, dummyUI, subregion);
        this.parent = parent;
        if (realUI != null) {
            uiToXRegionMap.put(realUI, this);
        }
    }

    /**
     * Returns a region appropriate for the component.
     *
     * @param component the component to get the region for
     * @param useParent a boolean indicating whether or not to return a fallback
     *    of the XRegion, if available
     * @return a region for the component or null if not available.
     */
    public static Region getXRegion(JComponent component, boolean useParent) {
        XRegion region = uiToXRegionMap.get(component.getUIClassID());
        if (region != null)
            return useParent && region.parent != null ? region.parent : region;
        return region;
    }
}
