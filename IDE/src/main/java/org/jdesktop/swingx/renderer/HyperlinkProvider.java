/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Point;
import java.awt.event.ActionEvent;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.jdesktop.swingx.rollover.RolloverProducer;
import org.jdesktop.swingx.rollover.RolloverRenderer;

/**
 * Renderer for hyperlinks". <p>
 *
 * The renderer is configured with a LinkAction<T>.
 * It's mostly up to the developer to guarantee that the all
 * values which are passed into the getXXRendererComponent(...) are
 * compatible with T: she can provide a runtime class to check against.
 * If it isn't the renderer will configure the
 * action with a null target. <p>
 *
 * It's recommended to not use the given Action anywhere else in code,
 * as it is updated on each getXXRendererComponent() call which might
 * lead to undesirable side-effects. <p>
 *
 * Internally uses JXHyperlink as rendering component. <p>
 *
 * PENDING: can go from ButtonProvider? <p>
 *
 * PENDING: make renderer respect selected cell state. <p>
 *
 * PENDING: TreeCellRenderer has several issues <p>
 * <ol>
 *   <li> no icons
 *   <li> usual background highlighter issues
 * </ol>
 *
 * @author Jeanette Winzenburg
 */
public class HyperlinkProvider
   extends ComponentProvider<JXHyperlink> implements
         RolloverRenderer {

    private AbstractHyperlinkAction<Object> linkAction;
    protected Class<?> targetClass;

    /**
     * Instantiate a LinkRenderer with null LinkAction and null
     * targetClass.
     *
     */
    public HyperlinkProvider() {
        this(null, null);
    }

    /**
     * Instantiate a LinkRenderer with the LinkAction to use with
     * target values.
     *
     * @param linkAction the action that acts on values.
     */
    public HyperlinkProvider(AbstractHyperlinkAction linkAction) {
        this(linkAction, null);
    }

    /**
     * Instantiate a LinkRenderer with a LinkAction to use with
     * target values and the type of values the action can cope with. <p>
     *
     * It's up to developers to take care of matching types.
     *
     * @param linkAction the action that acts on values.
     * @param targetClass the type of values the action can handle.
     */
    public HyperlinkProvider(AbstractHyperlinkAction linkAction, Class<?> targetClass) {
        super();
//        rendererComponent.addActionListener(createEditorActionListener());
        setLinkAction(linkAction, targetClass);
    }

    /**
     * Sets the class the action is supposed to handle. <p>
     *
     * PENDING: make sense to set independently of LinkAction?
     *
     * @param targetClass the type of values the action can handle.
     */
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Sets the LinkAction for handling the values. <p>
     *
     * The action is assumed to be able to cope with any type, that is
     * this method is equivalent to setLinkAction(linkAction, null).
     *
     * @param linkAction
     */
    public void setLinkAction(AbstractHyperlinkAction linkAction) {
        setLinkAction(linkAction, null);
    }

    /**
     * Sets the LinkAction for handling the values and the
     * class the action can handle. <p>
     *
     * PENDING: in the general case this is not independent of the
     * targetClass. Need api to set them combined?
     *
     * @param linkAction
     */
    public void setLinkAction(AbstractHyperlinkAction linkAction, Class<?> targetClass) {
        if (linkAction == null) {
            linkAction = createDefaultLinkAction();
        }
        setTargetClass(targetClass);
        this.linkAction = linkAction;
        rendererComponent.setAction(linkAction);

    }
    /**
     * decides if the given target is acceptable for setTarget.
     * <p>
     *
     *  target == null is acceptable for all types.
     *  targetClass == null is the same as Object.class
     *
     * @param target the target to set.
     * @return true if setTarget can cope with the object,
     *  false otherwise.
     *
     */
    public  boolean isTargetable(Object target) {
        // we accept everything
        if (targetClass == null) return true;
        if (target == null) return true;
        return targetClass.isAssignableFrom(target.getClass());
    }


    /**
     * default action - does nothing... except showing the target.
     *
     * @return a default LinkAction for showing the target.
     */
    protected AbstractHyperlinkAction createDefaultLinkAction() {
        return new AbstractHyperlinkAction<Object>(null) {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub

            }

        };
    }

//----------------------- Implement RolloverRenderer

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void doClick() {
        rendererComponent.doClick();
    }

//------------------------ ComponentProvider

    /**
     * {@inheritDoc} <p>
     *
     * PENDING JW: Needs to be overridden - doesn't comply to contract!. Not sure
     * how to do it without disturbing the hyperlinks current setting?
     * All hyperlink properties are defined by the LinkAction configured
     * with the target ...
     */
    @Override
    public String getString(Object value) {
        if (isTargetable(value)) {
            Object oldTarget = linkAction.getTarget();
            linkAction.setTarget(value);
            String text = linkAction.getName();
            linkAction.setTarget(oldTarget);
            return text;
        }
        return super.getString(value);
    }


    /**
     * {@inheritDoc} <p>
     *
     * Overridden to set the hyperlink's rollover state.
     */
    @Override
    protected void configureState(CellContext context) {
//        rendererComponent.setHorizontalAlignment(getHorizontalAlignment());
        if (context.getComponent() !=  null) {
            Point p = (Point) context.getComponent()
                    .getClientProperty(RolloverProducer.ROLLOVER_KEY);
            if (/*hasFocus || */(p != null && (p.x >= 0) &&
                    (p.x == context.getColumn()) && (p.y == context.getRow()))) {
                if (!rendererComponent.getModel().isRollover())
                 rendererComponent.getModel().setRollover(true);
            } else {
                if (rendererComponent.getModel().isRollover())
                 rendererComponent.getModel().setRollover(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Overridden to set the LinkAction's target to the context's value, if
     * targetable.<p>
     *
     * Forces foreground color to the one defined by hyperlink for unselected
     * cells, doesn't change the foreground for selected (as darkish text on dark selection
     * background might be unreadable, Issue #840-swingx). Not entirely safe because
     * the unselected background might be dark as well. Need to find a better way in
     * the long run. Until then, client code can use Highlighters to repair
     * (which is nasty!). <p>
     *
     * PENDING JW: by-passes XXValues - state currently is completely defined by
     * the action. Hmm ...
     *
     */
    @Override
    protected void format(CellContext context) {
        Object value = context.getValue();
        if (isTargetable(value)) {
            linkAction.setTarget(value);
        } else {
            linkAction.setTarget(null);
        }
        // hmm... the hyperlink should do this automatically..
        // Issue #840-swingx: hyperlink unreadable if selected (for dark selection colors)
        // so we only force clicked/unclicked if unselected
        if (!context.isSelected()) {
            rendererComponent.setForeground(linkAction.isVisited() ?
                rendererComponent.getClickedColor() : rendererComponent.getUnclickedColor());
        } else {
            // JW: workaround #845-swingx which was introduced by fixing #840
            // if we interfere with the colors, need to do always. Not quite understood
            rendererComponent.setForeground(context.getSelectionForeground());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JXHyperlink createRendererComponent() {
        return new JXRendererHyperlink();
    }

}
