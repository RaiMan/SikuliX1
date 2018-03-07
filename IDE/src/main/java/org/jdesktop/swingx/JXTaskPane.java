/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.TaskPaneAddon;
import org.jdesktop.swingx.plaf.TaskPaneUI;

/**
 * <code>JXTaskPane</code> is a container for tasks and other
 * arbitrary components.
 *
 * <p>
 * Several <code>JXTaskPane</code>s are usually grouped together within a
 * {@link org.jdesktop.swingx.JXTaskPaneContainer}. However it is not mandatory
 * to use a JXTaskPaneContainer as the parent for JXTaskPane. The JXTaskPane can
 * be added to any other container. See
 * {@link org.jdesktop.swingx.JXTaskPaneContainer} to understand the benefits of
 * using it as the parent container.
 *
 * <p>
 * <code>JXTaskPane</code> provides control to expand and
 * collapse the content area in order to show or hide the task list. It can have an
 * <code>icon</code>, a <code>title</code> and can be marked as
 * <code>special</code>. Marking a <code>JXTaskPane</code> as
 * <code>special</code> ({@link #setSpecial(boolean)} is only a hint for
 * the pluggable UI which will usually paint it differently (by example by
 * using another color for the border of the pane).
 *
 * <p>
 * When the JXTaskPane is expanded or collapsed, it will be
 * animated with a fade effect. The animated can be disabled on a per
 * component basis through {@link #setAnimated(boolean)}.
 *
 * To disable the animation for all newly created <code>JXTaskPane</code>,
 * use the UIManager property:
 * <code>UIManager.put("TaskPane.animate", Boolean.FALSE);</code>.
 *
 * <p>
 * Example:
 * <pre>
 * <code>
 * JXFrame frame = new JXFrame();
 *
 * // a container to put all JXTaskPane together
 * JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
 *
 * // create a first taskPane with common actions
 * JXTaskPane actionPane = new JXTaskPane();
 * actionPane.setTitle("Files and Folders");
 * actionPane.setSpecial(true);
 *
 * // actions can be added, a hyperlink will be created
 * Action renameSelectedFile = createRenameFileAction();
 * actionPane.add(renameSelectedFile);
 * actionPane.add(createDeleteFileAction());
 *
 * // add this taskPane to the taskPaneContainer
 * taskPaneContainer.add(actionPane);
 *
 * // create another taskPane, it will show details of the selected file
 * JXTaskPane details = new JXTaskPane();
 * details.setTitle("Details");
 *
 * // add standard components to the details taskPane
 * JLabel searchLabel = new JLabel("Search:");
 * JTextField searchField = new JTextField("");
 * details.add(searchLabel);
 * details.add(searchField);
 *
 * taskPaneContainer.add(details);
 *
 * // put the action list on the left
 * frame.add(taskPaneContainer, BorderLayout.EAST);
 *
 * // and a file browser in the middle
 * frame.add(fileBrowser, BorderLayout.CENTER);
 *
 * frame.pack();
 * frame.setVisible(true);
 * </code>
 * </pre>
 *
 * @see org.jdesktop.swingx.JXTaskPaneContainer
 * @see org.jdesktop.swingx.JXCollapsiblePane
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @author Karl George Schaefer
 *
 * @javabean.attribute
 *          name="isContainer"
 *          value="Boolean.TRUE"
 *          rtexpr="true"
 *
 * @javabean.attribute
 *          name="containerDelegate"
 *          value="getContentPane"
 *
 * @javabean.class
 *          name="JXTaskPane"
 *          shortDescription="JXTaskPane is a container for tasks and other arbitrary components."
 *          stopClass="java.awt.Component"
 *
 * @javabean.icons
 *          mono16="JXTaskPane16-mono.gif"
 *          color16="JXTaskPane16.gif"
 *          mono32="JXTaskPane32-mono.gif"
 *          color32="JXTaskPane32.gif"
 */
@JavaBean
@SuppressWarnings("nls")
public class JXTaskPane extends JPanel implements
  JXCollapsiblePane.CollapsiblePaneContainer, Mnemonicable {

  /**
   * JXTaskPane pluggable UI key <i>swingx/TaskPaneUI</i>
   */
  public final static String uiClassID = "swingx/TaskPaneUI";

  // ensure at least the default ui is registered
  static {
    LookAndFeelAddons.contribute(new TaskPaneAddon());
  }

  /**
   * Used when generating PropertyChangeEvents for the "scrollOnExpand" property
   */
  public static final String SCROLL_ON_EXPAND_CHANGED_KEY = "scrollOnExpand";

  /**
   * Used when generating PropertyChangeEvents for the "title" property
   */
  public static final String TITLE_CHANGED_KEY = "title";

  /**
   * Used when generating PropertyChangeEvents for the "icon" property
   */
  public static final String ICON_CHANGED_KEY = "icon";

  /**
   * Used when generating PropertyChangeEvents for the "special" property
   */
  public static final String SPECIAL_CHANGED_KEY = "special";

  /**
   * Used when generating PropertyChangeEvents for the "animated" property
   */
  public static final String ANIMATED_CHANGED_KEY = "animated";

  private String title;
  private Icon icon;
  private boolean special;
  private boolean scrollOnExpand;

  private int        mnemonic;
  private int        mnemonicIndex           = -1;

  private JXCollapsiblePane collapsePane;

  /**
   * Creates a new empty <code>JXTaskPane</code>.
   */
  public JXTaskPane() {
      this((String) null);
  }

    /**
     * Creates a new task pane with the specified title.
     *
     * @param title
     *            the title to use
     */
    public JXTaskPane(String title) {
        this(title, null);
    }

    /**
     * Creates a new task pane with the specified icon.
     *
     * @param icon
     *            the icon to use
     */
    public JXTaskPane(Icon icon) {
        this(null, icon);
    }

    /**
     * Creates a new task pane with the specified title and icon.
     *
     * @param title
     *            the title to use
     * @param icon
     *            the icon to use
     */
    public JXTaskPane(String title, Icon icon) {
      collapsePane = new JXCollapsiblePane();
      collapsePane.setOpaque(false);
      super.setLayout(new BorderLayout(0, 0));
      super.addImpl(collapsePane, BorderLayout.CENTER, -1);

      setTitle(title);
      setIcon(icon);

      updateUI();
      setFocusable(true);

      // disable animation if specified in UIManager
      setAnimated(!Boolean.FALSE.equals(UIManager.get("TaskPane.animate")));

      // listen for animation events and forward them to registered listeners
        collapsePane.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JXTaskPane.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
                        evt.getNewValue());
            }
        });
  }

  /**
   * Returns the contentPane object for this JXTaskPane.
   * @return the contentPane property
   */
  public Container getContentPane() {
    return collapsePane.getContentPane();
  }

  /**
   * Notification from the <code>UIManager</code> that the L&F has changed.
   * Replaces the current UI object with the latest version from the <code>UIManager</code>.
   *
   * @see javax.swing.JComponent#updateUI
   */
  @Override
  public void updateUI() {
    // collapsePane is null when updateUI() is called by the "super()"
    // constructor
    if (collapsePane == null) {
      return;
    }
    setUI((TaskPaneUI)LookAndFeelAddons.getUI(this, TaskPaneUI.class));
  }

  /**
   * Sets the L&F object that renders this component.
   *
   * @param ui the <code>TaskPaneUI</code> L&F object
   * @see javax.swing.UIDefaults#getUI
   *
   * @beaninfo bound: true hidden: true description: The UI object that
   * implements the taskpane group's LookAndFeel.
   */
  public void setUI(TaskPaneUI ui) {
    super.setUI(ui);
  }

  /**
   * Returns the name of the L&F class that renders this component.
   *
   * @return the string {@link #uiClassID}
   * @see javax.swing.JComponent#getUIClassID
   * @see javax.swing.UIDefaults#getUI
   */
  @Override
  public String getUIClassID() {
    return uiClassID;
  }

  /**
   * Returns the title currently displayed in the border of this pane.
   *
   * @return the title currently displayed in the border of this pane
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title to be displayed in the border of this pane.
   *
   * @param title the title to be displayed in the border of this pane
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setTitle(String title) {
    String old = this.title;
    this.title = title;
    firePropertyChange(TITLE_CHANGED_KEY, old, title);
  }

  /**
   * Returns the icon currently displayed in the border of this pane.
   *
   * @return the icon currently displayed in the border of this pane
   */
  public Icon getIcon() {
    return icon;
  }

  /**
   * Sets the icon to be displayed in the border of this pane. Some pluggable
   * UIs may impose size constraints for the icon. A size of 16x16 pixels is
   * the recommended icon size.
   *
   * @param icon the icon to be displayed in the border of this pane
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setIcon(Icon icon) {
    Icon old = this.icon;
    this.icon = icon;
    firePropertyChange(ICON_CHANGED_KEY, old, icon);
  }

  /**
   * Returns true if this pane is "special".
   *
   * @return true if this pane is "special"
   * @see #setSpecial(boolean)
   */
  public boolean isSpecial() {
    return special;
  }

  /**
   * Sets this pane to be "special" or not. Marking a <code>JXTaskPane</code>
   * as <code>special</code> is only a hint for the pluggable UI which will
   * usually paint it differently (by example by using another color for the
   * border of the pane).
   *
   * <p>
   * Usually the first JXTaskPane in a JXTaskPaneContainer is marked as special
   * because it contains the default set of actions which can be executed given
   * the current context.
   *
   * @param special
   *          true if this pane is "special", false otherwise
   * @javabean.property bound="true" preferred="true"
   */
  public void setSpecial(boolean special) {
      boolean oldValue = isSpecial();
      this.special = special;
      firePropertyChange(SPECIAL_CHANGED_KEY, oldValue, isSpecial());
  }

  /**
   * Should this group be scrolled to be visible on expand.
   *
   * @param scrollOnExpand true to scroll this group to be
   * visible if this group is expanded.
   *
   * @see #setCollapsed(boolean)
   *
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setScrollOnExpand(boolean scrollOnExpand) {
      boolean oldValue = isScrollOnExpand();
      this.scrollOnExpand = scrollOnExpand;
      firePropertyChange(SCROLL_ON_EXPAND_CHANGED_KEY,
              oldValue, isScrollOnExpand());
  }

  /**
   * Should this group scroll to be visible after
   * this group was expanded.
   *
   * @return true if we should scroll false if nothing
   * should be done.
   */
  public boolean isScrollOnExpand() {
    return scrollOnExpand;
  }

    /**
     * Expands or collapses this group.
     * <p>
     * As of SwingX 1.6.3, the property change event only fires when the
     * state is accurate.  As such, animated task panes fire once the
     * animation is complete.
     *
     * @param collapsed
     *                true to collapse the group, false to expand it
     * @javabean.property
     *          bound="true"
     *          preferred="false"
     */
    public void setCollapsed(boolean collapsed) {
        collapsePane.setCollapsed(collapsed);
    }

    /**
     * Returns the collapsed state of this task pane.
     *
     * @return {@code true} if the task pane is collapsed; {@code false}
     *         otherwise
     */
    public boolean isCollapsed() {
        return collapsePane.isCollapsed();
    }

  /**
   * Enables or disables animation during expand/collapse transition.
   *
   * @param animated
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setAnimated(boolean animated) {
      boolean oldValue = isAnimated();
      collapsePane.setAnimated(animated);
      firePropertyChange(ANIMATED_CHANGED_KEY, oldValue, isAnimated());
  }

  /**
   * Returns true if this task pane is animated during expand/collapse
   * transition.
   *
   * @return true if this task pane is animated during expand/collapse
   *         transition.
   */
  public boolean isAnimated() {
    return collapsePane.isAnimated();
  }

    /**
     * {@inheritDoc}
     * <p>
     * If the character defined by the mnemonic is found within the task pane's
     * text string, the first occurrence of it will be underlined to indicate
     * the mnemonic to the user.
     */
    @Override
    public int getMnemonic() {
        return mnemonic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMnemonic(int mnemonic) {
        int oldValue = getMnemonic();
        this.mnemonic = mnemonic;

        firePropertyChange("mnemonic", oldValue, getMnemonic());

        updateDisplayedMnemonicIndex(getTitle(), mnemonic);
        revalidate();
        repaint();
    }

    /**
     * Update the displayedMnemonicIndex property. This method
     * is called when either text or mnemonic changes. The new
     * value of the displayedMnemonicIndex property is the index
     * of the first occurrence of mnemonic in text.
     */
    private void updateDisplayedMnemonicIndex(String text, int mnemonic) {
        if (text == null || mnemonic == '\0') {
            mnemonicIndex = -1;

            return;
        }

        char uc = Character.toUpperCase((char)mnemonic);
        char lc = Character.toLowerCase((char)mnemonic);

        int uci = text.indexOf(uc);
        int lci = text.indexOf(lc);

        if (uci == -1) {
            mnemonicIndex = lci;
        } else if(lci == -1) {
            mnemonicIndex = uci;
        } else {
            mnemonicIndex = (lci < uci) ? lci : uci;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDisplayedMnemonicIndex() {
        return mnemonicIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayedMnemonicIndex(int index)
                                          throws IllegalArgumentException {
        int oldValue = mnemonicIndex;
        if (index == -1) {
            mnemonicIndex = -1;
        } else {
            String text = getTitle();
            int textLength = (text == null) ? 0 : text.length();
            if (index < -1 || index >= textLength) {  // index out of range
                throw new IllegalArgumentException("index == " + index);
            }
        }
        mnemonicIndex = index;
        firePropertyChange("displayedMnemonicIndex", oldValue, index);
        if (index != oldValue) {
            revalidate();
            repaint();
        }
    }

  /**
   * Adds an action to this <code>JXTaskPane</code>. Returns a
   * component built from the action. The returned component has been
   * added to the <code>JXTaskPane</code>.
   *
   * @param action
   * @return a component built from the action
   */
  public Component add(Action action) {
    Component c = ((TaskPaneUI)ui).createAction(action);
    add(c);
    return c;
  }

  /**
   * @see JXCollapsiblePane.CollapsiblePaneContainer
   */
  @Override
public Container getValidatingContainer() {
    return getParent();
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  @Override
  protected void addImpl(Component comp, Object constraints, int index) {
    getContentPane().add(comp, constraints, index);
    //Fixes SwingX #364; adding to internal component we need to revalidate ourself
    revalidate();
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  @Override
  public void setLayout(LayoutManager mgr) {
    if (collapsePane != null) {
      getContentPane().setLayout(mgr);
    }
  }

  /**
   * Overridden to redirect call to the content pane
   */
  @Override
  public void remove(Component comp) {
    getContentPane().remove(comp);
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  @Override
  public void remove(int index) {
    getContentPane().remove(index);
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  @Override
  public void removeAll() {
    getContentPane().removeAll();
  }

  /**
   * @see JComponent#paramString()
   */
  @Override
  protected String paramString() {
    return super.paramString()
      + ",title="
      + getTitle()
      + ",icon="
      + getIcon()
      + ",collapsed="
      + String.valueOf(isCollapsed())
      + ",special="
      + String.valueOf(isSpecial())
      + ",scrollOnExpand="
      + String.valueOf(isScrollOnExpand())
      + ",ui=" + getUI();
  }

}
