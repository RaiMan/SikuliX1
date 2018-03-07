/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.text.html.HTMLDocument;

import org.jdesktop.swingx.painter.Painter;

/**
 * A collection of utility methods for Swing(X) classes.
 *
 * <ul>
 * PENDING JW: think about location of this class and/or its methods, Options:
 *
 *  <li> move this class to the swingx utils package which already has a bunch of xxUtils
 *  <li> move methods between xxUtils classes as appropriate (one window/comp related util)
 *  <li> keep here in swingx (consistent with swingutilities in core)
 * </ul>
 * @author Karl George Schaefer
 */
public final class SwingXUtilities {
    private SwingXUtilities() {
        //does nothing
    }

    /**
     * A helper for creating and updating key bindings for components with
     * mnemonics. The {@code pressed} action will be invoked when the mnemonic
     * is activated.
     * <p>
     * TODO establish an interface for the mnemonic properties, such as {@code
     * MnemonicEnabled} and change signature to {@code public static <T extends
     * JComponent & MnemonicEnabled> void updateMnemonicBinding(T c, String
     * pressed)}
     *
     * @param c
     *            the component bindings to update
     * @param pressed
     *            the name of the action in the action map to invoke when the
     *            mnemonic is pressed
     * @throws NullPointerException
     *             if the component is {@code null}
     */
    public static void updateMnemonicBinding(JComponent c, String pressed) {
        updateMnemonicBinding(c, pressed, null);
    }

    /**
     * A helper for creating and updating key bindings for components with
     * mnemonics. The {@code pressed} action will be invoked when the mnemonic
     * is activated and the {@code released} action will be invoked when the
     * mnemonic is deactivated.
     * <p>
     * TODO establish an interface for the mnemonic properties, such as {@code
     * MnemonicEnabled} and change signature to {@code public static <T extends
     * JComponent & MnemonicEnabled> void updateMnemonicBinding(T c, String
     * pressed, String released)}
     *
     * @param c
     *            the component bindings to update
     * @param pressed
     *            the name of the action in the action map to invoke when the
     *            mnemonic is pressed
     * @param released
     *            the name of the action in the action map to invoke when the
     *            mnemonic is released (if the action is a toggle style, then
     *            this parameter should be {@code null})
     * @throws NullPointerException
     *             if the component is {@code null}
     */
    public static void updateMnemonicBinding(JComponent c, String pressed, String released) {
        Class<?> clazz = c.getClass();
        int m = -1;

        try {
            Method mtd = clazz.getMethod("getMnemonic");
            m = (Integer) mtd.invoke(c);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("unable to access mnemonic", e);
        }

        InputMap map = SwingUtilities.getUIInputMap(c,
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        if (m != 0) {
            if (map == null) {
                map = new ComponentInputMapUIResource(c);
                SwingUtilities.replaceUIInputMap(c,
                        JComponent.WHEN_IN_FOCUSED_WINDOW, map);
            }

            map.clear();

            //TODO is ALT_MASK right for all platforms?
            map.put(KeyStroke.getKeyStroke(m,  InputEvent.ALT_MASK, false),
                    pressed);
            map.put(KeyStroke.getKeyStroke(m, InputEvent.ALT_MASK, true),
                    released);
            map.put(KeyStroke.getKeyStroke(m, 0, true), released);
        } else {
            if (map != null) {
                map.clear();
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <C extends JComponent & BackgroundPaintable> void paintBackground(C comp, Graphics2D g) {
        // we should be painting the background behind the painter if we have one
        // this prevents issues with buffer reuse where visual artifacts sneak in
        if (comp.isOpaque()
                || (comp instanceof AlphaPaintable && ((AlphaPaintable) comp).getAlpha() < 1f)
                || UIManager.getLookAndFeel().getID().equals("Nimbus")) {
            g.setColor(comp.getBackground());
            g.fillRect(0, 0, comp.getWidth(), comp.getHeight());
        }

        Painter<? super C> painter = comp.getBackgroundPainter();

        if (painter != null) {
            if (comp.isPaintBorderInsets()) {
                painter.paint(g, comp, comp.getWidth(), comp.getHeight());
            } else {
                Insets insets = comp.getInsets();
                g.translate(insets.left, insets.top);
                painter.paint(g, comp, comp.getWidth() - insets.left - insets.right,
                        comp.getHeight() - insets.top - insets.bottom);
                g.translate(-insets.left, -insets.top);
            }
        }
    }

    private static Component[] getChildren(Component c) {
        Component[] children = null;

        if (c instanceof MenuElement) {
            MenuElement[] elements = ((MenuElement) c).getSubElements();
            children = new Component[elements.length];

            for (int i = 0; i < elements.length; i++) {
                children[i] = elements[i].getComponent();
            }
        } else if (c instanceof Container) {
            children = ((Container) c).getComponents();
        }

        return children;
    }

    /**
     * Enables or disables of the components in the tree starting with {@code c}.
     *
     * @param c
     *                the starting component
     * @param enabled
     *                {@code true} if the component is to enabled; {@code false} otherwise
     */
    public static void setComponentTreeEnabled(Component c, boolean enabled) {
        c.setEnabled(enabled);

        Component[] children = getChildren(c);

        if (children != null) {
            for(int i = 0; i < children.length; i++) {
                setComponentTreeEnabled(children[i], enabled);
            }
        }
    }

    /**
     * Sets the locale for an entire component hierarchy to the specified
     * locale.
     *
     * @param c
     *                the starting component
     * @param locale
     *                the locale to set
     */
    public static void setComponentTreeLocale(Component c, Locale locale) {
        c.setLocale(locale);

        Component[] children = getChildren(c);

        if (children != null) {
            for(int i = 0; i < children.length; i++) {
                setComponentTreeLocale(children[i], locale);
            }
        }
    }

    /**
     * Sets the background for an entire component hierarchy to the specified
     * color.
     *
     * @param c
     *                the starting component
     * @param color
     *                the color to set
     */
    public static void setComponentTreeBackground(Component c, Color color) {
        c.setBackground(color);

        Component[] children = getChildren(c);

        if (children != null) {
            for(int i = 0; i < children.length; i++) {
                setComponentTreeBackground(children[i], color);
            }
        }
    }

    /**
     * Sets the foreground for an entire component hierarchy to the specified
     * color.
     *
     * @param c
     *                the starting component
     * @param color
     *                the color to set
     */
    public static void setComponentTreeForeground(Component c, Color color) {
        c.setForeground(color);

        Component[] children = getChildren(c);

        if (children != null) {
            for(int i = 0; i < children.length; i++) {
                setComponentTreeForeground(children[i], color);
            }
        }
    }

    /**
     * Sets the font for an entire component hierarchy to the specified font.
     *
     * @param c
     *            the starting component
     * @param font
     *            the font to set
     */
    public static void setComponentTreeFont(Component c, Font font) {
        c.setFont(font);

        Component[] children = getChildren(c);

        if (children != null) {
            for(int i = 0; i < children.length; i++) {
                setComponentTreeFont(children[i], font);
            }
        }
    }

    private static String STYLESHEET =
        "body { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0;"
        + " font-family: %s; font-size: %dpt;  }"
        + "a, p, li { margin-top: 0; margin-bottom: 0; margin-left: 0;"
        + " margin-right: 0; font-family: %s; font-size: %dpt;  }";

    /**
     * Sets the font used for HTML displays to the specified font. Components
     * that display HTML do not necessarily honor font properties, since the
     * HTML document can override these values. Calling {@code setHtmlFont}
     * after the data is set will force the HTML display to use the font
     * specified to this method.
     *
     * @param doc
     *            the HTML document to update
     * @param font
     *            the font to use
     * @throws NullPointerException
     *             if any parameter is {@code null}
     */
    public static void setHtmlFont(HTMLDocument doc, Font font) {
        String stylesheet = String.format(STYLESHEET, font.getName(),
                font.getSize(), font.getName(), font.getSize());

        try {
            doc.getStyleSheet().loadRules(new StringReader(stylesheet), null);
        } catch (IOException e) {
            //this should never happen with our sheet
            throw new IllegalStateException(e);
        }
    }

    /**
     * Updates the componentTreeUI of all top-level windows of the
     * current application.
     *
     */
    public static void updateAllComponentTreeUIs() {
//        for (Frame frame : Frame.getFrames()) {
//            updateAllComponentTreeUIs(frame);
//        }
        // JW: updated to new 1.6 api - returns all windows, owned and ownerless
        for (Window window: Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }


    /**
     * Updates the componentTreeUI of the given window and all its
     * owned windows, recursively.
     *
     *
     * @param window the window to update
     */
    public static void updateAllComponentTreeUIs(Window window) {
        SwingUtilities.updateComponentTreeUI(window);
        for (Window owned : window.getOwnedWindows()) {
            updateAllComponentTreeUIs(owned);
        }
    }

    /**
     * A version of {@link SwingUtilities#invokeLater(Runnable)} that supports return values.
     *
     * @param <T>
     *            the return type of the callable
     * @param callable
     *            the callable to execute
     * @return a future task for accessing the return value
     * @see Callable
     */
    public static <T> FutureTask<T> invokeLater(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);

        SwingUtilities.invokeLater(task);

        return task;
    }

    /**
     * A version of {@link SwingUtilities#invokeAndWait(Runnable)} that supports return values.
     *
     * @param <T>
     *            the return type of the callable
     * @param callable
     *            the callable to execute
     * @return the value returned by the callable
     * @throws InterruptedException
     *             if we're interrupted while waiting for the event dispatching thread to finish
     *             executing {@code callable.call()}
     * @throws InvocationTargetException
     *                if an exception is thrown while running {@code callable}
     * @see Callable
     */
    public static <T> T invokeAndWait(Callable<T> callable) throws InterruptedException,
            InvocationTargetException {
        try {
            //blocks until future returns
            return invokeLater(callable).get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof InvocationTargetException) {
                throw (InvocationTargetException) t;
            } else {
                throw new InvocationTargetException(t);
            }
        }
    }

    /**
     * An improved version of
     * {@link SwingUtilities#getAncestorOfClass(Class, Component)}. This method
     * traverses {@code JPopupMenu} invoker and uses generics to return an
     * appropriately typed object.
     *
     * @param <T>
     *            the type of ancestor to find
     * @param clazz
     *            the class instance of the ancestor to find
     * @param c
     *            the component to start the search from
     * @return an ancestor of the correct type or {@code null} if no such
     *         ancestor exists. This method also returns {@code null} if any
     *         parameter is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAncestor(Class<T> clazz, Component c) {
        if (clazz == null || c == null) {
            return null;
        }

        Component parent = c.getParent();

        while (parent != null && !(clazz.isInstance(parent))) {
            parent = parent instanceof JPopupMenu
                    ? ((JPopupMenu) parent).getInvoker() : parent.getParent();
        }

        return (T) parent;
    }

    /**
     * Returns whether the component is part of the parent's
     * container hierarchy. If a parent in the chain is of type
     * JPopupMenu, the parent chain of its invoker is walked.
     *
     * @param focusOwner
     * @param parent
     * @return true if the component is contained under the parent's
     *    hierarchy, coping with JPopupMenus.
     */
    public static boolean isDescendingFrom(Component focusOwner, Component parent) {
        while (focusOwner !=  null) {
            if (focusOwner instanceof JPopupMenu) {
                focusOwner = ((JPopupMenu) focusOwner).getInvoker();
                if (focusOwner == null) {
                    return false;
                }
            }
            if (focusOwner == parent) {
                return true;
            }
            focusOwner = focusOwner.getParent();
        }
        return false;
    }

    /**
     * Obtains a {@code TranslucentRepaintManager} from the specified manager.
     * If the current manager is a {@code TranslucentRepaintManager} or a
     * {@code ForwardingRepaintManager} that contains a {@code
     * TranslucentRepaintManager}, then the passed in manager is returned.
     * Otherwise a new repaint manager is created and returned.
     *
     * @param delegate
     *            the current repaint manager
     * @return a non-{@code null} {@code TranslucentRepaintManager}
     * @throws NullPointerException if {@code delegate} is {@code null}
     */
    static RepaintManager getTranslucentRepaintManager(RepaintManager delegate) {
        RepaintManager manager = delegate;

        while (manager != null && !manager.getClass().isAnnotationPresent(TranslucentRepaintManager.class)) {
            if (manager instanceof ForwardingRepaintManager) {
                manager = ((ForwardingRepaintManager) manager).getDelegateManager();
            } else {
                manager = null;
            }
        }

        return manager == null ? new RepaintManagerX(delegate) : delegate;
    }

    /**
     * Checks and returns whether the given property should be replaced
     * by the UI's default value.
     *
     * @param property the property to check.
     * @return true if the given property should be replaced by the UI's
     *   default value, false otherwise.
     */
    public static boolean isUIInstallable(Object property) {
       return (property == null) || (property instanceof UIResource);
    }

//---- methods c&p'ed from SwingUtilities2 to reduce dependencies on sun packages

    /**
     * Updates lead and anchor selection index without changing the selection.
     *
     * Note: this is c&p'ed from SwingUtilities2 to not have any direct
     * dependency.
     *
     * @param selectionModel the selection model to change lead/anchor
     * @param lead the lead selection index
     * @param anchor the anchor selection index
     */
    public static void setLeadAnchorWithoutSelection(
            ListSelectionModel selectionModel, int lead, int anchor) {
        if (anchor == -1) {
            anchor = lead;
        }
        if (lead == -1) {
            selectionModel.setAnchorSelectionIndex(-1);
            selectionModel.setLeadSelectionIndex(-1);
        } else {
            if (selectionModel.isSelectedIndex(lead))
                selectionModel.addSelectionInterval(lead, lead);
            else {
                selectionModel.removeSelectionInterval(lead, lead);
            }
            selectionModel.setAnchorSelectionIndex(anchor);
        }
    }

    public static boolean shouldIgnore(MouseEvent mouseEvent,
            JComponent component) {
        return ((component == null) || (!(component.isEnabled()))
                || (!(SwingUtilities.isLeftMouseButton(mouseEvent)))
                || (mouseEvent.isConsumed()));
    }

    public static int loc2IndexFileList(JList list, Point point) {
        int i = list.locationToIndex(point);
        if (i != -1) {
            Object localObject = list
                    .getClientProperty("List.isFileList");
            if ((localObject instanceof Boolean)
                    && (((Boolean) localObject).booleanValue())
    // PENDING JW: this isn't aware of sorting/filtering - fix!
                    && (!(pointIsInActualBounds(list, i, point)))) {
                i = -1;
            }
        }
        return i;
    }

    // PENDING JW: this isn't aware of sorting/filtering - fix!
    private static boolean pointIsInActualBounds(JList list, int index,
            Point point) {
        ListCellRenderer renderer = list.getCellRenderer();
        ListModel model = list.getModel();
        Object element = model.getElementAt(index);
        Component comp = renderer.getListCellRendererComponent(list, element,
                index, false, false);

        Dimension prefSize = comp.getPreferredSize();
        Rectangle cellBounds = list.getCellBounds(index, index);
        if (!(comp.getComponentOrientation().isLeftToRight())) {
            cellBounds.x += cellBounds.width - prefSize.width;
        }
        cellBounds.width = prefSize.width;

        return cellBounds.contains(point);
    }

    public static void adjustFocus(JComponent component) {
        if ((!(component.hasFocus())) && (component.isRequestFocusEnabled()))
            component.requestFocus();
    }

    public static int convertModifiersToDropAction(int modifiers,
            int sourcActions) {
        // PENDING JW: c'p from a decompiled SunDragSourceContextPeer
        // PENDING JW: haha ... completely readable, right ;-)
        int i = 0;

        switch (modifiers & 0xC0) {
        case 192:
            i = 1073741824;
            break;
        case 128:
            i = 1;
            break;
        case 64:
            i = 2;
            break;
        default:
            if ((sourcActions & 0x2) != 0) {
                i = 2;
                break;
            }
            if ((sourcActions & 0x1) != 0) {
                i = 1;
                break;
            }
            if ((sourcActions & 0x40000000) == 0)
                break;
            i = 1073741824;
        }

        // label88:
        return (i & sourcActions);
    }

}
