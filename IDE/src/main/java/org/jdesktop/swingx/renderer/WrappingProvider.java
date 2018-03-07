/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.jdesktop.swingx.treetable.TreeTableNode;

/**
 * Wrapping ComponentProvider for usage in tree rendering. Handles the icon
 * itself, delegates the node content to the wrappee. Value-based icon and
 * content mapping can be configured by custom <code>IconValue</code>s and
 * <b>StringValue</b>, respectively.
 * <p>
 *
 * An example of how to configure a file tree by using the system icons and
 * display names
 *
 * <pre><code>
 * TreeCellRenderer r = new DefaultTreeRenderer(
 *         IconValues.FILE_ICON, StringValues.FILE_NAME);
 * tree.setCellRenderer(r);
 * treeTable.setTreeCellRenderer(r);
 * </code></pre>
 *
 * PENDING: ui specific focus rect variation (draw rect around icon) missing
 * <p>
 */
public class WrappingProvider extends
    ComponentProvider<WrappingIconPanel>  implements RolloverRenderer {

    protected ComponentProvider<?> wrappee;
    private boolean unwrapUserObject;

    /**
     * Instantiates a WrappingProvider with default delegate provider.
     *
     */
    public WrappingProvider() {
        this((ComponentProvider<?>) null);
    }

    /**
     * Instantiates a WrappingProvider with default wrappee, configured
     * to use the wrappeeStringValue. Uses the
     * given IconValue to configure the icon.
     *
     * @param iconValue the IconValue to use for configuring the icon.
     * @param wrappeeStringValue the StringValue to use in the wrappee.
     */
    public WrappingProvider(IconValue iconValue, StringValue wrappeeStringValue) {
        this(iconValue, wrappeeStringValue, true);
    }

    /**
     * Instantiates a WrappingProvider with default wrappee. Uses the
     * given IconValue to configure the icon.
     *
     * @param iconValue the IconValue to use for configuring the icon.
     */
    public WrappingProvider(IconValue iconValue) {
        this(iconValue, null);
    }

    /**
     * Instantiates a WrappingProvider with default wrappee configured
     * with the given StringValue.
     *
     * PENDING: we have a slight semantic glitch compared to super because
     * the given StringValue is <b>not</b> for use in this provider but for use
     * in the wrappee!
     *
     * @param wrappeeStringValue the StringValue to use in the wrappee.
     */
    public WrappingProvider(StringValue wrappeeStringValue) {
        this(null, wrappeeStringValue);
    }

    /**
     * Instantiates a WrappingProvider with the given delegate
     * provider for the node content. If null, a default
     * LabelProvider will be used.
     *
     * @param delegate the provider to use as delegate
     */
    public WrappingProvider(ComponentProvider<?> delegate) {
        this(delegate, true);
    }

    /**
     * Instantiates a WrappingProvider with the given delegate
     * provider for the node content and unwrapUserObject property.
     * If the delegate is null, a default LabelProvider will be used.
     *
     * @param delegate the provider to use as delegate
     * @param unwrapUserObject a flag indicating whether this provider
     * should auto-unwrap the userObject from the context value.
     */
    public WrappingProvider(ComponentProvider<?> delegate, boolean unwrapUserObject) {
        this(null, delegate, unwrapUserObject);
    }

    /**
     * Instantiates a WrappingProvider with the given delegate
     * provider for the node content and unwrapUserObject property.
     * If the delegate is null, a default LabelProvider will be used.
     *
     * @param iv the icon converter to use for this provider
     * @param delegate the provider to use as delegate
     * @param unwrapUserObject a flag indicating whether this provider
     *          should auto-unwrap the userObject from the context value.
     */
    public WrappingProvider(IconValue iv, ComponentProvider<?> delegate, boolean unwrapUserObject) {
        super(iv != null ? (new MappedValue(null, iv)) : StringValues.EMPTY);
        setWrappee(delegate);
        setUnwrapUserObject(unwrapUserObject);
    }

    /**
     * Instantiates a WrappingProvider with the given delegate
     * provider for the node content and unwrapUserObject property.
     * If the delegate is null, a default LabelProvider will be used.
     *
     * @param iv the icon converter to use for this provider
     * @param delegateStringValue the StringValue to use in the wrappee.
     * @param unwrapUserObject a flag indicating whether this provider
     *          should auto-unwrap the userObject from the context value.
     */
    public WrappingProvider(IconValue iv, StringValue delegateStringValue, boolean unwrapUserObject) {
        this(iv, (ComponentProvider<?>) null, unwrapUserObject);
        getWrappee().setStringValue(delegateStringValue);
    }

    /**
     * Sets the given provider as delegate for the node content.
     * If the delegate is null, a default LabelProvider is set.<p>
     *
     *  PENDING: rename to setDelegate?
     *
     * @param delegate the provider to use as delegate.
     */
    public void setWrappee(ComponentProvider<?> delegate) {
        if (delegate == null) {
            delegate = new LabelProvider();
        }
        this.wrappee = delegate;
    }

    /**
     * Returns the delegate provider used to render the node content.
     *
     * @return the provider used for rendering the node content.
     */
    public ComponentProvider<?> getWrappee() {
        return wrappee;
    }

    /**
     * Sets the unwrapUserObject property. If true, this provider
     * replaces a context value of type XXNode with its user object before
     * delegating to the wrappee. Otherwise the value is passed as-is always.<p>
     *
     * The default value is true.
     *
     * @param unwrap
     * @see #getUnwrapUserObject()
     */
    public void setUnwrapUserObject(boolean unwrap) {
        this.unwrapUserObject = unwrap;
    }

    /**
     * Returns a boolean indicating whether this provider tries to unwrap
     * a userObject from a tree/table/node type value before delegating the
     * context.
     *
     * @return a flag indicating the auto-unwrap property.
     *
     * @see #setUnwrapUserObject(boolean)
     */
    public boolean getUnwrapUserObject() {
        return unwrapUserObject;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to comply to contract: returns the string representation as
     * provided by the wrappee (as this level has no string rep). Must do the
     * same unwrapping magic as in configuring the rendering component if the
     * unwrapUserObject property is true. <p>
     *
     *
     * @param value the Object to get a String representation for.
     *
     * @see #setUnwrapUserObject(boolean)
     * @see #getUnwrappedValue(Object)
     */
    @Override
    public String getString(Object value) {
        value = getUnwrappedValue(value);
        return wrappee.getString(value);
    }

    /**
     * Sets a boolean indicating whether or not the main component's opacity
     * should be applied to the Icon region.<p>
     *
     * The default value is false. This covers the main use case in a JTree.
     *
     * @param extendsComponentOpacity
     */
    public void setExtendsComponentOpacity(boolean extendsComponentOpacity) {
        rendererComponent.setExtendsComponentOpacity(extendsComponentOpacity);

    }
    /**
     * @return the extendsComponentOpacity
     */
    public boolean getExtendsComponentOpacity() {
        return rendererComponent.getExtendsComponentOpacity();
    }

    /**
     * Returns the value as it should be passed to the delegate. If the unwrapUserObject
     * property is true, tries return a userObject as appropriate for the value type.
     * Returns the given value itself, ff the property is false or the type does
     * not support the notion of userObject<p>
     *
     * Here: unwraps userObject of DefaultMutableTreeNode and TreeTableNode.<p>
     *
     * @param value the value to possibly unwrap
     * @return the userObject if the value has an appropriate type and the
     *   unwrapUserObject property is true, otherwise returns the value unchanged.
     *
     * @see #setUnwrapUserObject(boolean)
     * @see #getString(Object)
     * @see #getRendererComponent(CellContext)
     */
    protected Object getUnwrappedValue(Object value) {
        if (!getUnwrapUserObject()) return value;
        if (value instanceof DefaultMutableTreeNode) {
            value = ((DefaultMutableTreeNode) value).getUserObject();
        } else if (value instanceof TreeTableNode) {
            TreeTableNode node = (TreeTableNode) value;
            value = node.getUserObject();
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WrappingIconPanel getRendererComponent(CellContext context) {
        if (context != null) {
            rendererComponent.setComponent(wrappee.rendererComponent);
            Object oldValue = adjustContextValue(context);
            // PENDING JW: sequence of config?
            // A - first wrappee, then this allows to override configure/format methods
            // of this class and overrule the wrappee
            // B - first this, then wrappee allows overrule by overriding getRendererComp
            // would take control from wrappee (f.i. Hyperlink foreground)
            super.getRendererComponent(context);
            wrappee.getRendererComponent(context);
            restoreContextValue(context, oldValue);
            return rendererComponent;
        }
        // PENDING JW: Findbugs barking [NP] Load of known null value
        // probably can move the return rendererComponent from the if
        // to here (the contract is to return the comp as-is if the
        // context is null) - so we can do it here instead of delegating
        // to super?
        return super.getRendererComponent(context);
    }

    /**
     * Restores the context value to the old value.
     *
     * @param context the CellContext to restore.
     * @param oldValue the value to restore the context to.
     */
    protected void restoreContextValue(CellContext context, Object oldValue) {
        context.replaceValue(oldValue);
    }

    /**
     * Replace the context's value with the userobject if the value is a type
     * supporting the notion of userObject and this provider's unwrapUserObject
     * property is true. Otherwise does nothing.<p>
     *
     * Subclasses may override but must guarantee to return the original
     * value for restoring.
     *
     * @param context the context to adjust
     * @return the old context value
     *
     * @see #setUnwrapUserObject(boolean)
     * @see #getString(Object)
     */
    protected Object adjustContextValue(CellContext context) {
        Object oldValue = context.getValue();
        if (getUnwrapUserObject()) {
            context.replaceValue(getUnwrappedValue(oldValue));
        }
        return oldValue;
    }

    @Override
    protected void configureState(CellContext context) {
        rendererComponent.setBorder(BorderFactory.createEmptyBorder());
    }

//    /**
//     * @return
//     */
//    private boolean isBorderAroundIcon() {
//        return Boolean.TRUE.equals(UIManager.get("Tree.drawsFocusBorderAroundIcon"));
//    }

    @Override
    protected WrappingIconPanel createRendererComponent() {
        return new WrappingIconPanel();
    }

    /**
     * {@inheritDoc} <p>
     *
     * Here: implemented to set the icon.
     */
    @Override
    protected void format(CellContext context) {
        rendererComponent.setIcon(getValueAsIcon(context));
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to fallback to the default icons supplied by the
     * context if super returns null.
     *
     */
    @Override
    protected Icon getValueAsIcon(CellContext context) {
        Icon icon = super.getValueAsIcon(context);
        if (icon == null) {
            return context.getIcon();
        }
        return IconValue.NULL_ICON == icon ? null : icon;
    }

    //----------------- implement RolloverController

    /**
     * {@inheritDoc}
     */
    @Override
    public void doClick() {
        if (isEnabled()) {
            ((RolloverRenderer) wrappee).doClick();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return (wrappee instanceof RolloverRenderer) &&
           ((RolloverRenderer) wrappee).isEnabled();
    }


}
