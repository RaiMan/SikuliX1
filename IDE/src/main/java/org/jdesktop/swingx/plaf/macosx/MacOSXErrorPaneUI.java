/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.macosx;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorLevel;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.plaf.basic.BasicErrorPaneUI;

/**
 *
 * Ok, the Title becomes the first line in the error dialog
 *
 * The text immediately follows. Then come the "Details". This is a
 * toggle button with an icon and text but no border and no background. The icon
 * looks like a tree toggle (arrow right or down).
 *
 * There is then more optional text. The best way to support this is to look
 * in the client properties of the JXErrorPane for any "sub text". Ideally this
 * sub text would be part of the ErrorInfo. Maybe I should just add it there?
 *
 * Finally come the buttons. If there is no report action and the error < fatal,
 * the shown ok button should say "close". Otherwise, if there is no report action
 * but the error >= fatal, it should say "Exit Application". If there is a report
 * action but error < fatal, it should say "Don't Send" for ok, "Send Report" for
 * the report button. If there is a report action and the error >= fatal, then
 * one button should say "Exit", and the report button should say
 * "Send Report and Exit".
 *
 * Whenever either button is clicked (ok button or report button), the "close dialog"
 * procedure should occur.
 *
 * @author rbair
 */
public class MacOSXErrorPaneUI extends BasicErrorPaneUI {
    private JLabel titleLabel;
    private JEditorPane disclaimerText; // this is actually part of the details!!!

    //---------------------------------------------------------- constructor
    /** Creates a new instance of BasicErrorPanelUI */
    public MacOSXErrorPaneUI() {
        super();
    }

    @Override
    protected void configureDetailsButton(boolean expanded) {
        if (expanded) {
            detailButton.setText(UIManagerExt.getString(CLASS_NAME + ".details_contract_text", detailButton.getLocale()));
            detailButton.setIcon(UIManager.getIcon("Tree.expandedIcon"));
        } else {
            detailButton.setText(UIManagerExt.getString(CLASS_NAME + ".details_expand_text", detailButton.getLocale()));
            detailButton.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
        }
    }

    @Override
    protected void configureReportAction(AbstractActionExt reportAction) {
        reportAction.setName(UIManagerExt.getString(CLASS_NAME + ".report_button_text", pane.getLocale()));
//        reportButton.setText("Send Report To Apple");
//        reportButton.setPreferredSize(new Dimension(100, 30));
//        reportButton.setMinimumSize(new Dimension(100, 30));
    }

    public static ComponentUI createUI(JComponent c) {
        return new MacOSXErrorPaneUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JFrame getErrorFrame(Component owner) {
        JFrame frame = super.getErrorFrame(owner);
        frame.setTitle(" ");
        return frame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JDialog getErrorDialog(Component owner) {
        JDialog dlg = super.getErrorDialog(owner);
        dlg.setTitle(" ");
        return dlg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JInternalFrame getErrorInternalFrame(Component owner) {
        JInternalFrame frame = super.getErrorInternalFrame(owner);
        frame.setTitle(" ");
        return frame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LayoutManager createErrorPaneLayout() {
        createExtraComponents();
        GridBagLayout layout = new GridBagLayout();
        try {
            layout.addLayoutComponent(iconLabel,      new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 17), 0, 0));
            layout.addLayoutComponent(titleLabel,     new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 12, 0), 0 ,0));
            layout.addLayoutComponent(errorScrollPane,new GridBagConstraints(1, 1, 2, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 0));
            layout.addLayoutComponent(detailButton,   new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));
            layout.addLayoutComponent(detailsPanel,   new GridBagConstraints(0, 3, 3, 1, 1.0, 1.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 6, 0), 0 ,0));
            layout.addLayoutComponent(disclaimerText, new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
            layout.addLayoutComponent(closeButton,    new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
            layout.addLayoutComponent(reportButton,   new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LayoutManager createDetailPanelLayout() {
        GridBagLayout layout = new GridBagLayout();
        layout.addLayoutComponent(detailsScrollPane, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        copyToClipboardButton.setVisible(false);
        return layout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinit() {
        super.reinit();
        ErrorInfo info = pane == null ? null : pane.getErrorInfo();
        titleLabel.setText(info == null ? "Unknown Error" : info.getTitle());

        Object finePrint = pane.getClientProperty("fine-print");
        String text = finePrint == null ? null : finePrint.toString();
        disclaimerText.setText(text);
        disclaimerText.setVisible(text != null);

        if (info != null && info.getErrorLevel() == ErrorLevel.FATAL) {
            closeButton.setText(UIManagerExt.getString(CLASS_NAME + ".fatal_button_text", closeButton.getLocale()));
        } else {
            closeButton.setText(UIManagerExt.getString(CLASS_NAME + ".ok_button_text", closeButton.getLocale()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getDetailsHeight() {
        return 150;
    }

    private void createExtraComponents() {
        titleLabel = new JLabel("Unknown Error");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        pane.add(titleLabel);

        Font f = errorMessage.getFont();
        if (f != null) {
            errorMessage.setFont(f.deriveFont(f.getSize() - 2f));
        }

        disclaimerText = new JEditorPane();
        disclaimerText.setContentType("text/html");
        disclaimerText.setVisible(false);
        disclaimerText.setEditable(false);
        disclaimerText.setOpaque(false);
        disclaimerText.putClientProperty(JXEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        if (f != null) {
            disclaimerText.setFont(f.deriveFont(f.getSize() - 2f));
        }
        pane.add(disclaimerText);

        detailButton.setBorderPainted(false);
        detailButton.setContentAreaFilled(false);
        detailButton.setBorder(BorderFactory.createEmptyBorder());
        detailButton.setMargin(new Insets(0, 0, 0 ,0));
        detailButton.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
        detailButton.setText(UIManagerExt.getString(CLASS_NAME + ".details_expand_text", detailButton.getLocale()));

        closeButton.setText(UIManagerExt.getString(CLASS_NAME + ".ok_button_text", closeButton.getLocale()));
    }
}
