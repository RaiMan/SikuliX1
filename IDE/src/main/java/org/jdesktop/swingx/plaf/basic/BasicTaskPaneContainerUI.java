/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.TaskPaneContainerUI;

/**
 * Base implementation of the <code>JXTaskPaneContainer</code> UI.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @author Karl Schaefer
 */
public class BasicTaskPaneContainerUI extends TaskPaneContainerUI {
    /**
     * A {@code UIResource} implementation of {@code VerticalLayout}.
     *
     * @author Karl George Schaefer
     */
    protected class VerticalLayoutUIResource extends VerticalLayout implements UIResource {
        /**
         * The default layout.
         */
        public VerticalLayoutUIResource() {
            super();
        }

        /**
         * Defines a layout with the specified gap.
         *
         * @param gap
         *            the gap between components
         */
        public VerticalLayoutUIResource(int gap) {
            super(gap);
        }
    }

  /**
   * Returns a new instance of BasicTaskPaneContainerUI.
   * BasicTaskPaneContainerUI delegates are allocated one per
   * JXTaskPaneContainer.
   *
   * @return A new TaskPaneContainerUI implementation for the Basic look and
   *         feel.
   */
  public static ComponentUI createUI(JComponent c) {
    return new BasicTaskPaneContainerUI();
  }

  /**
   * The task pane container managed by this UI delegate.
   */
  protected JXTaskPaneContainer taskPane;

  /**
   * {@inheritDoc}
   */
  @Override
public void installUI(JComponent c) {
    super.installUI(c);
    taskPane = (JXTaskPaneContainer)c;
    installDefaults();

    LayoutManager manager = taskPane.getLayout();

    if (manager == null || manager instanceof UIResource) {
        taskPane.setLayout(createDefaultLayout());
    }
  }

    /**
     * Installs the default colors, border, and painter of the task pane
     * container.
     */
    protected void installDefaults() {
        LookAndFeel.installColors(taskPane, "TaskPaneContainer.background",
                "TaskPaneContainer.foreground");
        LookAndFeel.installBorder(taskPane, "TaskPaneContainer.border");
        LookAndFeelAddons.installBackgroundPainter(taskPane,
                "TaskPaneContainer.backgroundPainter");
        LookAndFeel.installProperty(taskPane, "opaque", Boolean.TRUE);
    }

    /**
     * Constructs a layout manager to be used by the Look and Feel.
     * @return the layout manager for the current Look and Feel
     */
    protected LayoutManager createDefaultLayout() {
        return new VerticalLayoutUIResource(14);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstallUI(JComponent c) {
        uninstallDefaults();

        super.uninstallUI(c);
    }

    /**
     * Uninstalls the default colors, border, and painter of the task pane
     * container.
     */
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(taskPane);
        LookAndFeelAddons.uninstallBackgroundPainter(taskPane);
    }
}
