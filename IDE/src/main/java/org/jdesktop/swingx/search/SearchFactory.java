/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.search;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFindBar;
import org.jdesktop.swingx.JXFindPanel;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXRootPane;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.util.Utilities;

/**
 * Factory to create, configure and show application consistent
 * search and find widgets.
 *
 * Typically a shared JXFindBar is used for incremental search, while
 * a shared JXFindPanel is used for batch search. This implementation
 *
 * <ul>
 *  <li> JXFindBar - adds and shows it in the target's toplevel container's
 *    toolbar (assuming a JXRootPane)
 *  <li> JXFindPanel - creates a JXDialog, adds and shows the findPanel in the
 *    Dialog
 * </ul>
 *
 *
 * PENDING: JW - update (?) views/wiring on focus change. Started brute force -
 * stop searching. This looks extreme confusing for findBars added to ToolBars
 * which are empty except for the findbar. Weird problem if triggered from
 * menu - find widget disappears after having been shown for an instance.
 * Where's the focus?
 *
 *
 * PENDING: add methods to return JXSearchPanels (for use by PatternMatchers).
 *
 * @author Jeanette Winzenburg
 */
public class SearchFactory implements UIDependent {
    private static class LaFListener implements PropertyChangeListener {
        private final WeakReference<SearchFactory> ref;

        public LaFListener(SearchFactory sf) {
            this.ref = new WeakReference<SearchFactory>(sf);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            SearchFactory sf = ref.get();

            if (sf == null) {
                UIManager.removePropertyChangeListener(this);
            } else if ("lookAndFeel".equals(evt.getPropertyName())) {
                sf.updateUI();
            }
        }
    }

    // PENDING: rename methods to batch/incremental instead of dialog/toolbar

    static {
        // Hack to enforce loading of SwingX framework ResourceBundle
        LookAndFeelAddons.getAddon();
    }

    private static SearchFactory searchFactory;

    /** the shared find widget for batch-find. */
    protected JXFindPanel findPanel;

    /** the shared find widget for incremental-find. */
    protected JXFindBar findBar;
    /** this is a temporary hack: need to remove the useSearchHighlighter property. */
    protected JComponent lastFindBarTarget;

    private boolean useFindBar;

    private Point lastFindDialogLocation;

    private FindRemover findRemover;

    /**
     * Returns the shared SearchFactory.
     *
     * @return the shared <code>SearchFactory</code>
     */
    public static SearchFactory getInstance() {
          if (searchFactory == null) {
              searchFactory = new SearchFactory();
          }
          return searchFactory;
      }

    /**
     * Sets the shared SearchFactory.
     *
     * @param factory
     */
    public static void setInstance(SearchFactory factory) {
        searchFactory = factory;
    }

    public SearchFactory() {
        UIManager.addPropertyChangeListener(new LaFListener(this));
    }

    /**
     * Returns a common Keystroke for triggering
     * a search. Tries to be OS-specific. <p>
     *
     * PENDING: this should be done in the LF and the
     * keyStroke looked up in the UIManager.
     *
     * @return the keyStroke to register with a findAction.
     */
    public KeyStroke getSearchAccelerator() {
        // JW: this should be handled by the LF!
        // get the accelerator mnemonic from the UIManager
        String findMnemonic = "F";
        KeyStroke findStroke = Utilities.stringToKey("D-" + findMnemonic);
        // fallback for sandbox (this should be handled in Utilities instead!)
        if (findStroke == null) {
            findStroke = KeyStroke.getKeyStroke("control F");
        }
        return findStroke;

    }
    /**
     * Returns decision about using a batch- vs. incremental-find for the
     * searchable. This implementation returns the useFindBar property directly.
     *
     * @param target - the component associated with the searchable
     * @param searchable - the object to search.
     * @return true if a incremental-find should be used, false otherwise.
     */
    public boolean isUseFindBar(JComponent target, Searchable searchable) {
        return useFindBar;
    }

    /**
     * Sets the default search type to incremental or batch, for a
     * true/false boolean. The default value is false (== batch).
     *
     * @param incremental a boolean to indicate the default search
     * type, true for incremental and false for batch.
     */
    public void setUseFindBar(boolean incremental) {
        if (incremental == useFindBar) return;
        this.useFindBar = incremental;
        getFindRemover().endSearching();
    }

    /**
     * Shows an appropriate find widget targeted at the searchable.
     * Opens a batch-find or incremental-find
     * widget based on the return value of <code>isUseFindBar</code>.
     *
     * @param target - the component associated with the searchable
     * @param searchable - the object to search.
     *
     * @see #isUseFindBar(JComponent, Searchable)
     * @see #setUseFindBar(boolean)
     */
    public void showFindInput(JComponent target, Searchable searchable) {
        if (isUseFindBar(target, searchable)) {
            showFindBar(target, searchable);
        } else {
            showFindDialog(target, searchable);
        }
    }

//------------------------- incremental search

    /**
     * Show a incremental-find widget targeted at the searchable.
     *
     * This implementation uses a JXFindBar and inserts it into the
     * target's toplevel container toolbar.
     *
     * PENDING: Nothing shown if there is no toolbar found.
     *
     * @param target - the component associated with the searchable
     * @param searchable - the object to search.
     */
    public void showFindBar(JComponent target, Searchable searchable) {
        if (target == null) return;
        if (findBar == null) {
            findBar = getSharedFindBar();
        } else {
            releaseFindBar();
        }
        Window topLevel = SwingUtilities.getWindowAncestor(target);
        if (topLevel instanceof JXFrame) {
            JXRootPane rootPane = ((JXFrame) topLevel).getRootPaneExt();
            JToolBar toolBar = rootPane.getToolBar();
            if (toolBar == null) {
                toolBar = new JToolBar();
                rootPane.setToolBar(toolBar);
            }
            toolBar.add(findBar, 0);
            rootPane.revalidate();
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(findBar);

        }
        lastFindBarTarget = target;
        findBar.setLocale(target.getLocale());
        target.putClientProperty(AbstractSearchable.MATCH_HIGHLIGHTER, Boolean.TRUE);
        getSharedFindBar().setSearchable(searchable);
        installFindRemover(target, findBar);
    }

    /**
     * Returns the shared JXFindBar. Creates and configures on
     * first call.
     *
     * @return the shared <code>JXFindBar</code>
     */
    public JXFindBar getSharedFindBar() {
        if (findBar == null) {
            findBar = createFindBar();
            configureSharedFindBar();
        }
        return findBar;
    }

    /**
     * Factory method to create a JXFindBar.
     *
     * @return the <code>JXFindBar</code>
     */
    public JXFindBar createFindBar() {
        return new JXFindBar();
    }

    protected void installFindRemover(Container target, Container findWidget) {
        if (target != null) {
            getFindRemover().addTarget(target);
        }
        getFindRemover().addTarget(findWidget);
    }

    private FindRemover getFindRemover() {
        if (findRemover == null) {
            findRemover = new FindRemover();
        }
        return findRemover;
    }

    /**
     * convenience method to remove a component from its parent
     * and revalidate the parent
     */
    protected void removeFromParent(JComponent component) {
        Container oldParent = component.getParent();
        if (oldParent != null) {
            oldParent.remove(component);
            if (oldParent instanceof JComponent) {
                ((JComponent) oldParent).revalidate();
            } else {
                // not sure... never have non-j comps
                oldParent.invalidate();
                oldParent.validate();
            }
        }
    }

    protected void stopSearching() {
        if (findPanel != null) {
            lastFindDialogLocation = hideSharedFindPanel(false);
            findPanel.setSearchable(null);
        }
        if (findBar != null) {
            releaseFindBar();
         }
    }

    /**
     * Pre: findbar != null.
     */
    protected void releaseFindBar() {
        findBar.setSearchable(null);
        if (lastFindBarTarget != null) {
            lastFindBarTarget.putClientProperty(AbstractSearchable.MATCH_HIGHLIGHTER, Boolean.FALSE);
            lastFindBarTarget = null;
        }
        removeFromParent(findBar);
    }

    /**
     * Configures the shared FindBar. This method is
     * called once after creation of the shared FindBar.
     * Subclasses can override to add configuration code. <p>
     *
     * Here: registers a custom action to remove the
     * findbar from its ancestor container.
     *
     * PRE: findBar != null.
     *
     */
    protected void configureSharedFindBar() {
        Action removeAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeFromParent(findBar);
//                stopSearching();
//                releaseFindBar();

            }

        };
        findBar.getActionMap().put(JXDialog.CLOSE_ACTION_COMMAND, removeAction);
    }

//------------------------ batch search

    /**
     * Show a batch-find widget targeted at the given Searchable.
     *
     * This implementation uses a shared JXFindPanel contained
     * JXDialog.
     *
     * @param target -
     *            the component associated with the searchable
     * @param searchable -
     *            the object to search.
     */
    public void showFindDialog(JComponent target, Searchable searchable) {
        Window frame = null; //JOptionPane.getRootFrame();
        if (target != null) {
            target.putClientProperty(AbstractSearchable.MATCH_HIGHLIGHTER, Boolean.FALSE);
            frame = SwingUtilities.getWindowAncestor(target);
//            if (window instanceof Frame) {
//                frame = (Frame) window;
//            }
        }
        JXDialog topLevel = getDialogForSharedFindPanel();
        JXDialog findDialog;
        if ((topLevel != null) && (topLevel.getOwner().equals(frame))) {
            findDialog = topLevel;
            // JW: #635-swingx - quick hack to update title to current locale ...
//            findDialog.setTitle(getSharedFindPanel().getName());
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(findDialog);
        } else {
            Point location = hideSharedFindPanel(true);
            if (frame instanceof Frame) {
                findDialog = new JXDialog((Frame) frame, getSharedFindPanel());
            } else if (frame instanceof Dialog) {
                // fix #215-swingx: had problems with secondary modal dialogs.
                findDialog = new JXDialog((Dialog) frame, getSharedFindPanel());
            } else {
                findDialog = new JXDialog(JOptionPane.getRootFrame(), getSharedFindPanel());
            }
            // RJO: shouldn't we avoid overloaded useage like this in a JSR296 world? swap getName() for getTitle() here?
//            findDialog.setTitle(getSharedFindPanel().getName());
            // JW: don't - this will stay on top of all applications!
            // findDialog.setAlwaysOnTop(true);
            findDialog.pack();
            if (location == null) {
                findDialog.setLocationRelativeTo(frame);
            } else {
                findDialog.setLocation(location);
            }
        }
        if (target != null) {
            findDialog.setLocale(target.getLocale());
        }
        getSharedFindPanel().setSearchable(searchable);
        installFindRemover(target, findDialog);
        findDialog.setVisible(true);
    }

    /**
     * Returns the shared JXFindPanel. Lazyly creates and configures on
     * first call.
     *
     * @return the shared <code>JXFindPanel</code>
     */
    public JXFindPanel getSharedFindPanel() {
        if (findPanel == null) {
            findPanel = createFindPanel();
            configureSharedFindPanel();
        } else {
            // JW: temporary hack around #718-swingx
            // no longer needed with cleanup of hideSharedFindPanel
//            if (findPanel.getParent() == null) {
//                SwingUtilities.updateComponentTreeUI(findPanel);
//            }
        }
        return findPanel;
    }

    /**
     * Factory method to create a JXFindPanel.
     *
     * @return <code>JXFindPanel</code>
     */
    public JXFindPanel createFindPanel() {
        return new JXFindPanel();
    }

    /**
     * Configures the shared FindPanel. This method is
     * called once after creation of the shared FindPanel.
     * Subclasses can override to add configuration code. <p>
     *
     * Here: no-op
     * PRE: findPanel != null.
     *
     */
    protected void configureSharedFindPanel() {
    }


    private JXDialog getDialogForSharedFindPanel() {
        if (findPanel == null) return null;
        Window window = SwingUtilities.getWindowAncestor(findPanel);
        return (window instanceof JXDialog) ? (JXDialog) window : null;
    }

    /**
     * Hides the findPanel's toplevel window and returns its location.
     * If the dispose is true, the findPanel is removed from its parent
     * and the toplevel window is disposed.
     *
     * @param dispose boolean to indicate whether the findPanels toplevel
     *   window should be disposed.
     * @return the location of the window if visible, or the last known
     *   location.
     */
    protected Point hideSharedFindPanel(boolean dispose) {
        if (findPanel == null) return null;
        Window window = SwingUtilities.getWindowAncestor(findPanel);
        Point location = lastFindDialogLocation;
        if (window != null) {
            // PENDING JW: can't remember why it it removed always?
            if (window.isVisible()) {
                location = window.getLocationOnScreen();
                window.setVisible(false);
            }
            if (dispose) {
                findPanel.getParent().remove(findPanel);
                window.dispose();
            }
        }
        return location;
    }

    public class FindRemover implements PropertyChangeListener {
        KeyboardFocusManager focusManager;
        Set<Container> targets;

        public FindRemover() {
            updateManager();
        }

        public void addTarget(Container target) {
            getTargets().add(target);
        }

        public void removeTarget(Container target) {
            getTargets().remove(target);
        }

        private Set<Container> getTargets() {
            if (targets == null) {
                targets = new HashSet<Container>();
            }
            return targets;
        }

        private void updateManager() {
            if (focusManager != null) {
                focusManager.removePropertyChangeListener("permanentFocusOwner", this);
            }
            this.focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            focusManager.addPropertyChangeListener("permanentFocusOwner", this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {

            Component c = focusManager.getPermanentFocusOwner();
            if (c == null) return;
            for (Iterator<Container> iter = getTargets().iterator(); iter.hasNext();) {
                Container element = iter.next();
                if ((element == c) || (SwingUtilities.isDescendingFrom(c, element))) {
                    return;
                }
            }
            endSearching();
       }

        public void endSearching() {
            getTargets().clear();
            stopSearching();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI() {
        if (findBar != null) {
            SwingUtilities.updateComponentTreeUI(findBar);
        }

        if (findPanel != null) {
            SwingUtilities.updateComponentTreeUI(findPanel);
        }
    }
}
