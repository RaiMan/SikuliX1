/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;

import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorLevel;
import org.jdesktop.swingx.error.ErrorReporter;
import org.jdesktop.swingx.plaf.ErrorPaneUI;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.util.WindowUtils;

/**
 * Base implementation of the <code>JXErrorPane</code> UI.
 *
 * @author rbair
 * @author rah003
 */
public class BasicErrorPaneUI extends ErrorPaneUI {
    /**
     * Used as a prefix when pulling data out of UIManager for i18n
     */
    protected static final String CLASS_NAME = "JXErrorPane";

    /**
     * The error pane this UI is for
     */
    protected JXErrorPane pane;
    /**
     * Error message text area
     */
    protected JEditorPane errorMessage;

    /**
     * Error message text scroll pane wrapper.
     */
    protected JScrollPane errorScrollPane;
    /**
     * details text area
     */
    protected JXEditorPane details;
    /**
     * detail button
     */
    protected AbstractButton detailButton;
    /**
     * ok/close button
     */
    protected JButton closeButton;
    /**
     * label used to display the warning/error icon
     */
    protected JLabel iconLabel;
    /**
     * report an error button
     */
    protected AbstractButton reportButton;
    /**
     * details panel
     */
    protected JPanel detailsPanel;
    protected JScrollPane detailsScrollPane;
    protected JButton copyToClipboardButton;

    /**
     * Property change listener for the error pane ensures that the pane's UI
     * is reinitialized.
     */
    protected PropertyChangeListener errorPaneListener;

    /**
     * Action listener for the detail button.
     */
    protected ActionListener detailListener;

    /**
     * Action listener for the copy to clipboard button.
     */
    protected ActionListener copyToClipboardListener;

    //------------------------------------------------------ private helpers
    /**
     * The height of the window when collapsed. This value is stashed when the
     * dialog is expanded
     */
    private int collapsedHeight = 0;
    /**
     * The height of the window when last expanded. This value is stashed when
     * the dialog is collapsed
     */
    private int expandedHeight = 0;

    //---------------------------------------------------------- constructor

    /**
     * {@inheritDoc}
     */
    public static ComponentUI createUI(JComponent c) {
        return new BasicErrorPaneUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        this.pane = (JXErrorPane)c;

        installDefaults();
        installComponents();
        installListeners();

        //if the report action needs to be defined, do so
        Action a = c.getActionMap().get(JXErrorPane.REPORT_ACTION_KEY);
        if (a == null) {
            final JXErrorPane pane = (JXErrorPane)c;
            AbstractActionExt reportAction = new AbstractActionExt() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ErrorReporter reporter = pane.getErrorReporter();
                    if (reporter != null) {
                        reporter.reportError(pane.getErrorInfo());
                    }
                }
            };
            configureReportAction(reportAction);
            c.getActionMap().put(JXErrorPane.REPORT_ACTION_KEY, reportAction);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);

        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
    }

    /**
     * Installs the default colors, and default font into the Error Pane
     */
    protected void installDefaults() {
    }

    /**
     * Uninstalls the default colors, and default font into the Error Pane.
     */
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(pane);
    }

    /**
     * Create and install the listeners for the Error Pane.
     * This method is called when the UI is installed.
     */
    protected void installListeners() {
        //add a listener to the pane so I can reinit() whenever the
        //bean properties change (particularly error info)
        errorPaneListener = new ErrorPaneListener();
        pane.addPropertyChangeListener(errorPaneListener);
    }

    /**
     * Remove the installed listeners from the Error Pane.
     * The number and types of listeners removed and in this method should be
     * the same that was added in <code>installListeners</code>
     */
    protected void uninstallListeners() {
        //remove the property change listener from the pane
        pane.removePropertyChangeListener(errorPaneListener);
    }

    //    ===============================
    //     begin Sub-Component Management
    //

    /**
     * Creates and initializes the components which make up the
     * aggregate combo box. This method is called as part of the UI
     * installation process.
     */
    protected void installComponents() {
        iconLabel = new JLabel(pane.getIcon());

        errorMessage = new JEditorPane();
        errorMessage.setEditable(false);
        errorMessage.setContentType("text/html");
        errorMessage.setEditorKitForContentType("text/plain", new StyledEditorKit());
        errorMessage.setEditorKitForContentType("text/html", new HTMLEditorKit());

        errorMessage.setOpaque(false);
        errorMessage.putClientProperty(JXEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        closeButton = new JButton(UIManagerExt.getString(
                CLASS_NAME + ".ok_button_text", errorMessage.getLocale()));

        reportButton = new EqualSizeJButton(pane.getActionMap().get(JXErrorPane.REPORT_ACTION_KEY));

        detailButton = new EqualSizeJButton(UIManagerExt.getString(
                CLASS_NAME + ".details_expand_text", errorMessage.getLocale()));

        details = new JXEditorPane();
        details.setContentType("text/html");
        details.putClientProperty(JXEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        details.setTransferHandler(createDetailsTransferHandler(details));
        detailsScrollPane = new JScrollPane(details);
        detailsScrollPane.setPreferredSize(new Dimension(10, 250));
        details.setEditable(false);
        detailsPanel = new JPanel();
        detailsPanel.setVisible(false);
        copyToClipboardButton = new JButton(UIManagerExt.getString(
                CLASS_NAME + ".copy_to_clipboard_button_text", errorMessage.getLocale()));
        copyToClipboardListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                details.copy();
            }
        };
        copyToClipboardButton.addActionListener(copyToClipboardListener);

        detailsPanel.setLayout(createDetailPanelLayout());
        detailsPanel.add(detailsScrollPane);
        detailsPanel.add(copyToClipboardButton);

        // Create error scroll pane. Make sure this happens before call to createErrorPaneLayout() in case any extending
        // class wants to manipulate the component there.
        errorScrollPane = new JScrollPane(errorMessage);
        errorScrollPane.setBorder(new EmptyBorder(0,0,5,0));
        errorScrollPane.setOpaque(false);
        errorScrollPane.getViewport().setOpaque(false);

        //initialize the gui. Most of this code is similar between Mac and PC, but
        //where they differ protected methods have been written allowing the
        //mac implementation to alter the layout of the dialog.
        pane.setLayout(createErrorPaneLayout());

        //An empty border which constitutes the padding from the edge of the
        //dialog to the content. All content that butts against this border should
        //not be padded.
        Insets borderInsets = new Insets(16, 24, 16, 17);
        pane.setBorder(BorderFactory.createEmptyBorder(borderInsets.top, borderInsets.left, borderInsets.bottom, borderInsets.right));

        //add the JLabel responsible for displaying the icon.
        //TODO: in the future, replace this usage of a JLabel with a JXImagePane,
        //which may add additional "coolness" such as allowing the user to drag
        //the image off the dialog onto the desktop. This kind of coolness is common
        //in the mac world.
        pane.add(iconLabel);
        pane.add(errorScrollPane);
        pane.add(closeButton);
        pane.add(reportButton);
        reportButton.setVisible(false); // not visible by default
        pane.add(detailButton);
        pane.add(detailsPanel);

        //make the buttons the same size
        EqualSizeJButton[] buttons = new EqualSizeJButton[] {
            (EqualSizeJButton)detailButton, (EqualSizeJButton)reportButton };
        ((EqualSizeJButton)reportButton).setGroup(buttons);
        ((EqualSizeJButton)detailButton).setGroup(buttons);

        reportButton.setMinimumSize(reportButton.getPreferredSize());
        detailButton.setMinimumSize(detailButton.getPreferredSize());

        //set the event handling
        detailListener = new DetailsClickEvent();
        detailButton.addActionListener(detailListener);
    }

    /**
     * The aggregate components which compise the combo box are
     * unregistered and uninitialized. This method is called as part of the
     * UI uninstallation process.
     */
    protected void uninstallComponents() {
        iconLabel = null;
        errorMessage = null;
        closeButton = null;
        reportButton = null;

        detailButton.removeActionListener(detailListener);
        detailButton = null;

        details.setTransferHandler(null);
        details = null;

        detailsScrollPane.removeAll();
        detailsScrollPane = null;

        detailsPanel.setLayout(null);
        detailsPanel.removeAll();
        detailsPanel = null;

        copyToClipboardButton.removeActionListener(copyToClipboardListener);
        copyToClipboardButton = null;

        pane.removeAll();
        pane.setLayout(null);
        pane.setBorder(null);
    }

    //
    //     end Sub-Component Management
    //    ===============================

    /**
     * @inheritDoc
     */
    @Override
    public JFrame getErrorFrame(Component owner) {
        reinit();
        expandedHeight = 0;
        collapsedHeight = 0;
        JXErrorFrame frame = new JXErrorFrame(pane);
        centerWindow(frame, owner);
        return frame;
    }

    /**
     * @inheritDoc
     */
    @Override
    public JDialog getErrorDialog(Component owner) {
        reinit();
        expandedHeight = 0;
        collapsedHeight = 0;
        Window w = WindowUtils.findWindow(owner);
        JXErrorDialog dlg = null;
        if (w instanceof Dialog) {
            dlg = new JXErrorDialog((Dialog)w, pane);
        } else if (w instanceof Frame) {
            dlg = new JXErrorDialog((Frame)w, pane);
        } else {
            // default fallback to null
            dlg = new JXErrorDialog(JOptionPane.getRootFrame(), pane);
        }
        centerWindow(dlg, owner);
        return dlg;
    }

    /**
     * @inheritDoc
     */
    @Override
    public JInternalFrame getErrorInternalFrame(Component owner) {
        reinit();
        expandedHeight = 0;
        collapsedHeight = 0;
        JXInternalErrorFrame frame = new JXInternalErrorFrame(pane);
        centerWindow(frame, owner);
        return frame;
    }

    /**
     * Create and return the LayoutManager to use with the error pane.
     */
    protected LayoutManager createErrorPaneLayout() {
        return new ErrorPaneLayout();
    }

    protected LayoutManager createDetailPanelLayout() {
        GridBagLayout layout = new GridBagLayout();
        layout.addLayoutComponent(detailsScrollPane, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6,0,0,0),0,0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);
        layout.addLayoutComponent(copyToClipboardButton, gbc);
        return layout;
    }

    @Override
    public Dimension calculatePreferredSize() {
        //TODO returns a Dimension that is either X wide, or as wide as necessary
        //to show the title. It is Y high.
        return new Dimension(iconLabel.getPreferredSize().width + errorMessage.getPreferredSize().width, 206);
    }

    protected int getDetailsHeight() {
        return 300;
    }

    protected void configureReportAction(AbstractActionExt reportAction) {
        reportAction.setName(UIManagerExt.getString(CLASS_NAME + ".report_button_text", pane.getLocale()));
    }

    //----------------------------------------------- private helper methods

    /**
     * Creates and returns a TransferHandler which can be used to copy the details
     * from the details component. It also disallows pasting into the component, or
     * cutting from the component.
     *
     * @return a TransferHandler for the details area
     */
    private TransferHandler createDetailsTransferHandler(JTextComponent detailComponent) {
        return new DetailsTransferHandler(detailComponent);
    }

    /**
     * @return the default error icon
     */
    protected Icon getDefaultErrorIcon() {
        try {
            Icon icon = UIManager.getIcon(CLASS_NAME + ".errorIcon");
            return icon == null ? UIManager.getIcon("OptionPane.errorIcon") : icon;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the default warning icon
     */
    protected Icon getDefaultWarningIcon() {
        try {
            Icon icon = UIManager.getIcon(CLASS_NAME + ".warningIcon");
            return icon == null ? UIManager.getIcon("OptionPane.warningIcon") : icon;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Set the details section of the error dialog.  If the details are either
     * null or an empty string, then hide the details button and hide the detail
     * scroll pane.  Otherwise, just set the details section.
     *
     * @param details Details to be shown in the detail section of the dialog.
     * This can be null if you do not want to display the details section of the
     * dialog.
     */
    private void setDetails(String details) {
        if (details == null || details.equals("")) {
            detailButton.setVisible(false);
        } else {
            this.details.setText(details);
            detailButton.setVisible(true);
        }
    }

    protected void configureDetailsButton(boolean expanded) {
        if (expanded) {
            detailButton.setText(UIManagerExt.getString(
                    CLASS_NAME + ".details_contract_text", detailButton.getLocale()));
        } else {
            detailButton.setText(UIManagerExt.getString(
                    CLASS_NAME + ".details_expand_text", detailButton.getLocale()));
        }
    }

    /**
     * Set the details section to be either visible or invisible.  Set the
     * text of the Details button accordingly.
     * @param b if true details section will be visible
     */
    private void setDetailsVisible(boolean b) {
        if (b) {
            collapsedHeight = pane.getHeight();
            pane.setSize(pane.getWidth(), expandedHeight == 0 ? collapsedHeight + getDetailsHeight() : expandedHeight);
            detailsPanel.setVisible(true);
            configureDetailsButton(true);
            detailsPanel.applyComponentOrientation(detailButton.getComponentOrientation());

            // workaround for bidi bug, if the text is not set "again" and the component orientation has changed
            // then the text won't be aligned correctly. To reproduce this (in JDK 1.5) show two dialogs in one
            // use LTOR orientation and in the second use RTOL orientation and press "details" in both.
            // Text in the text box should be aligned to right/left respectively, without this line this doesn't
            // occure I assume because bidi properties are tested when the text is set and are not updated later
            // on when setComponentOrientation is invoked.
            details.setText(details.getText());
            details.setCaretPosition(0);
        } else if (collapsedHeight != 0) { //only collapse if the dialog has been expanded
            expandedHeight = pane.getHeight();
            detailsPanel.setVisible(false);
            configureDetailsButton(false);
            // Trick to force errorMessage JTextArea to resize according
            // to its columns property.
            errorMessage.setSize(0, 0);
            errorMessage.setSize(errorMessage.getPreferredSize());
            pane.setSize(pane.getWidth(), collapsedHeight);
        } else {
            detailsPanel.setVisible(false);
        }

        pane.doLayout();
    }

    /**
     * Set the error message for the dialog box
     * @param errorMessage Message for the error dialog
     */
    private void setErrorMessage(String errorMessage) {
        if(BasicHTML.isHTMLString(errorMessage)) {
            this.errorMessage.setContentType("text/html");
        } else {
            this.errorMessage.setContentType("text/plain");
        }
        this.errorMessage.setText(errorMessage);
        this.errorMessage.setCaretPosition(0);
    }

    /**
     * Reconfigures the dialog if settings have changed, such as the
     * errorInfo, errorIcon, warningIcon, etc
     */
    protected void reinit() {
        setDetailsVisible(false);
        Action reportAction = pane.getActionMap().get(JXErrorPane.REPORT_ACTION_KEY);
        reportButton.setAction(reportAction);
        reportButton.setVisible(reportAction != null && reportAction.isEnabled() && pane.getErrorReporter() != null);
        reportButton.setEnabled(reportButton.isVisible());
        ErrorInfo errorInfo = pane.getErrorInfo();
        if (errorInfo == null) {
            iconLabel.setIcon(pane.getIcon());
            setErrorMessage("");
            closeButton.setText(UIManagerExt.getString(
                    CLASS_NAME + ".ok_button_text", closeButton.getLocale()));
            setDetails("");
            //TODO Does this ever happen? It seems like if errorInfo is null and
            //this is called, it would be an IllegalStateException.
        } else {
            //change the "closeButton"'s text to either the default "ok"/"close" text
            //or to the "fatal" text depending on the error level of the incident info
            if (errorInfo.getErrorLevel() == ErrorLevel.FATAL) {
                closeButton.setText(UIManagerExt.getString(
                        CLASS_NAME + ".fatal_button_text", closeButton.getLocale()));
            } else {
                closeButton.setText(UIManagerExt.getString(
                        CLASS_NAME + ".ok_button_text", closeButton.getLocale()));
            }

            //if the icon for the pane has not been specified by the developer,
            //then set it to the default icon based on the error level
            Icon icon = pane.getIcon();
            if (icon == null || icon instanceof UIResource) {
                if (errorInfo.getErrorLevel().intValue() <= Level.WARNING.intValue()) {
                    icon = getDefaultWarningIcon();
                } else {
                    icon = getDefaultErrorIcon();
                }
            }
            iconLabel.setIcon(icon);
            setErrorMessage(errorInfo.getBasicErrorMessage());
            String details = errorInfo.getDetailedErrorMessage();
            if(details == null) {
                details = getDetailsAsHTML(errorInfo);
            }
            setDetails(details);
        }
    }

    /**
     * Creates and returns HTML representing the details of this incident info. This
     * method is only called if the details needs to be generated: ie: the detailed
     * error message property of the incident info is null.
     */
    protected String getDetailsAsHTML(ErrorInfo errorInfo) {
        if(errorInfo.getErrorException() != null) {
            //convert the stacktrace into a more pleasent bit of HTML
            StringBuffer html = new StringBuffer("<html>");
            html.append("<h2>" + escapeXml(errorInfo.getTitle()) + "</h2>");
            html.append("<HR size='1' noshade>");
            html.append("<div></div>");
            html.append("<b>Message:</b>");
            html.append("<pre>");
            html.append("    " + escapeXml(errorInfo.getErrorException().toString()));
            html.append("</pre>");
            html.append("<b>Level:</b>");
            html.append("<pre>");
            html.append("    " + errorInfo.getErrorLevel());
            html.append("</pre>");
            html.append("<b>Stack Trace:</b>");
            Throwable ex = errorInfo.getErrorException();
            while(ex != null) {
                html.append("<h4>"+ex.getMessage()+"</h4>");
                html.append("<pre>");
                for (StackTraceElement el : ex.getStackTrace()) {
                    html.append("    " + el.toString().replace("<init>", "&lt;init&gt;") + "\n");
                }
                html.append("</pre>");
                ex = ex.getCause();
            }
            html.append("</html>");
            return html.toString();
        } else {
            return null;
        }
    }

    //------------------------------------------------ actions/inner classes

    /**
     *  Default action for closing the JXErrorPane's enclosing window
     *  (JDialog, JFrame, or JInternalFrame)
     */
    private static final class CloseAction extends AbstractAction {
        private Window w;

        /**
         *  @param w cannot be null
         */
        private CloseAction(Window w) {
            if (w == null) {
                throw new NullPointerException("Window cannot be null");
            }
            this.w = w;
        }

        /**
         * @inheritDoc
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            w.setVisible(false);
            w.dispose();
        }
    }

    /**
     * Listener for Details click events.  Alternates whether the details section
     * is visible or not.
     *
     * @author rbair
     */
    private final class DetailsClickEvent implements ActionListener {

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            setDetailsVisible(!detailsPanel.isVisible());
        }
    }

    private final class ResizeWindow implements ActionListener {
        private Window w;
        private ResizeWindow(Window w) {
            if (w == null) {
                throw new NullPointerException();
            }
            this.w = w;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            Dimension contentSize = null;
            if (w instanceof JDialog) {
                contentSize = ((JDialog)w).getContentPane().getSize();
            } else {
                contentSize = ((JFrame)w).getContentPane().getSize();
            }

            Dimension dialogSize = w.getSize();
            int ydiff = dialogSize.height - contentSize.height;
            Dimension paneSize = pane.getSize();
            w.setSize(new Dimension(dialogSize.width, paneSize.height + ydiff));
            w.validate();
            w.repaint();
        }
    }

    /**
     * This is a button that maintains the size of the largest button in the button
     * group by returning the largest size from the getPreferredSize method.
     * This is better than using setPreferredSize since this will work regardless
     * of changes to the text of the button and its language.
     */
    private static final class EqualSizeJButton extends JButton {

        public EqualSizeJButton(String text) {
            super(text);
        }

        public EqualSizeJButton(Action a) {
            super(a);
        }

        /**
         * Buttons whose size should be taken into consideration
         */
        private EqualSizeJButton[] group;

        public void setGroup(EqualSizeJButton[] group) {
            this.group = group;
        }

        /**
         * Returns the actual preferred size on a different instance of this button
         */
        private Dimension getRealPreferredSize() {
            return super.getPreferredSize();
        }

        /**
         * If the <code>preferredSize</code> has been set to a
         * non-<code>null</code> value just returns it.
         * If the UI delegate's <code>getPreferredSize</code>
         * method returns a non <code>null</code> value then return that;
         * otherwise defer to the component's layout manager.
         *
         * @return the value of the <code>preferredSize</code> property
         * @see #setPreferredSize
         * @see ComponentUI
         */
        @Override
        public Dimension getPreferredSize() {
            int width = 0;
            int height = 0;
            for(int iter = 0 ; iter < group.length ; iter++) {
                Dimension size = group[iter].getRealPreferredSize();
                width = Math.max(size.width, width);
                height = Math.max(size.height, height);
            }

            return new Dimension(width, height);
        }

    }

    /**
     * Returns the text as non-HTML in a COPY operation, and disabled CUT/PASTE
     * operations for the Details pane.
     */
    private static final class DetailsTransferHandler extends TransferHandler {
        private JTextComponent details;
        private DetailsTransferHandler(JTextComponent detailComponent) {
            if (detailComponent == null) {
                throw new NullPointerException("detail component cannot be null");
            }
            this.details = detailComponent;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            String text = details.getSelectedText();
            if (text == null || text.equals("")) {
                details.selectAll();
                text = details.getSelectedText();
                details.select(-1, -1);
            }
            return new StringSelection(text);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY;
        }

    }

    private final class JXErrorDialog extends JDialog {
        public JXErrorDialog(Frame parent, JXErrorPane p) {
            super(parent, true);
            init(p);
        }

        public JXErrorDialog(Dialog parent, JXErrorPane p) {
            super(parent, true);
            init(p);
        }

        protected void init(JXErrorPane p) {
            // FYI: info can be null
            setTitle(p.getErrorInfo() == null ? null : p.getErrorInfo().getTitle());
            initWindow(this, p);
        }
    }

    private final class JXErrorFrame extends JFrame {
        public JXErrorFrame(JXErrorPane p) {
            setTitle(p.getErrorInfo().getTitle());
                initWindow(this, p);
        }
    }

    private final class JXInternalErrorFrame extends JInternalFrame {
        public JXInternalErrorFrame(JXErrorPane p) {
            setTitle(p.getErrorInfo().getTitle());

            setLayout(new BorderLayout());
            add(p, BorderLayout.CENTER);
            final Action closeAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    setVisible(false);
                    dispose();
                }
            };
            closeButton.addActionListener(closeAction);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent e) {
                    //remove the action listener
                    closeButton.removeActionListener(closeAction);
                    exitIfFatal();
                }
            });

            getRootPane().setDefaultButton(closeButton);
            setResizable(false);
            setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            getRootPane().registerKeyboardAction(closeAction, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
            //setPreferredSize(calculatePreferredDialogSize());
        }
    }

    /**
     * Utility method for initializing a Window for displaying a JXErrorPane.
     * This is particularly useful because the differences between JFrame and
     * JDialog are so minor.
     * removed.
     */
    private void initWindow(final Window w, final JXErrorPane pane) {
        w.setLayout(new BorderLayout());
        w.add(pane, BorderLayout.CENTER);
        final Action closeAction = new CloseAction(w);
        closeButton.addActionListener(closeAction);
        final ResizeWindow resizeListener = new ResizeWindow(w);
        //make sure this action listener is last (or, oddly, the first in the list)
        ActionListener[] list = detailButton.getActionListeners();
        for (ActionListener a : list) {
            detailButton.removeActionListener(a);
        }
        detailButton.addActionListener(resizeListener);
        for (ActionListener a : list) {
            detailButton.addActionListener(a);
        }

        if (w instanceof JFrame) {
            final JFrame f = (JFrame)w;
            f.getRootPane().setDefaultButton(closeButton);
            f.setResizable(true);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            f.getRootPane().registerKeyboardAction(closeAction, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
        } else if (w instanceof JDialog) {
            final JDialog d = (JDialog)w;
            d.getRootPane().setDefaultButton(closeButton);
            d.setResizable(true);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            d.getRootPane().registerKeyboardAction(closeAction, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
        }

        w.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //remove the action listener
                closeButton.removeActionListener(closeAction);
                detailButton.removeActionListener(resizeListener);
                exitIfFatal();
            }
        });
        w.pack();
    }

    private void exitIfFatal() {
        ErrorInfo info = pane.getErrorInfo();
        // FYI: info can be null
        if (info != null && info.getErrorLevel() == ErrorLevel.FATAL) {
            Action fatalAction = pane.getActionMap().get(JXErrorPane.FATAL_ACTION_KEY);
            if (fatalAction == null) {
                System.exit(1);
            } else {
                ActionEvent ae = new ActionEvent(closeButton, -1, "fatal");
                fatalAction.actionPerformed(ae);
            }
        }
    }

    private final class ErrorPaneListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            reinit();
        }
    }

    /**
     * Lays out the BasicErrorPaneUI components.
     */
    private final class ErrorPaneLayout implements LayoutManager {
        private JEditorPane dummy = new JEditorPane();

        @Override
        public void addLayoutComponent(String name, Component comp) {}
        @Override
        public void removeLayoutComponent(Component comp) {}

        /**
         * The preferred size is:
         *  The width of the parent container
         *  The height necessary to show the entire message text
         *    (as long as said height does not go off the screen)
         *    plus the buttons
         *
         * The preferred height changes depending on whether the details
         * are visible, or not.
         */
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int prefWidth = parent.getWidth();
            int prefHeight = parent.getHeight();
            final Insets insets = parent.getInsets();
            int pw = detailButton.isVisible() ? detailButton.getPreferredSize().width : 0;
            pw += detailButton.isVisible() ? detailButton.getPreferredSize().width : 0;
            pw += reportButton.isVisible() ? (5 + reportButton.getPreferredSize().width) : 0;
            pw += closeButton.isVisible() ? (5 + closeButton.getPreferredSize().width) : 0;
            prefWidth = Math.max(prefWidth, pw) + insets.left + insets.right;
            if (errorMessage != null) {
                //set a temp editor to a certain size, just to determine what its
                //pref height is
                dummy.setContentType(errorMessage.getContentType());
                dummy.setEditorKit(errorMessage.getEditorKit());
                dummy.setText(errorMessage.getText());
                dummy.setSize(prefWidth, 20);
                int errorMessagePrefHeight = dummy.getPreferredSize().height;

                prefHeight =
                        //the greater of the error message height or the icon height
                        Math.max(errorMessagePrefHeight, iconLabel.getPreferredSize().height) +
                        //the space between the error message and the button
                        10 +
                        //the button preferred height
                        closeButton.getPreferredSize().height;

                if (detailsPanel.isVisible()) {
                    prefHeight += getDetailsHeight();
                }

            }

            if (iconLabel != null && iconLabel.getIcon() != null) {
                prefWidth += iconLabel.getIcon().getIconWidth();
                prefHeight += 10; // top of icon is positioned 10px above the text
            }

            return new Dimension(
                    prefWidth + insets.left + insets.right,
                    prefHeight + insets.top + insets.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            final Insets insets = parent.getInsets();
            int x = insets.left;
            int y = insets.top;

            //place the icon
            if (iconLabel != null) {
                Dimension dim = iconLabel.getPreferredSize();
                iconLabel.setBounds(x, y, dim.width, dim.height);
                x += dim.width + 17;
                int leftEdge = x;

                //place the error message
                dummy.setContentType(errorMessage.getContentType());
                dummy.setText(errorMessage.getText());
                dummy.setSize(parent.getWidth() - leftEdge - insets.right, 20);
                dim = dummy.getPreferredSize();
                int spx = x;
                int spy = y;
                Dimension spDim = new Dimension (parent.getWidth() - leftEdge - insets.right, dim.height);
                y += dim.height + 10;
                int rightEdge = parent.getWidth() - insets.right;
                x = rightEdge;
                dim = detailButton.getPreferredSize(); //all buttons should be the same height!
                int buttonY = y + 5;
                if (detailButton.isVisible()) {
                    dim = detailButton.getPreferredSize();
                    x -= dim.width;
                    detailButton.setBounds(x, buttonY, dim.width, dim.height);
                }
                if (detailButton.isVisible()) {
                    detailButton.setBounds(x, buttonY, dim.width, dim.height);
                }
                errorScrollPane.setBounds(spx, spy, spDim.width, buttonY - spy);
                if (reportButton.isVisible()) {
                    dim = reportButton.getPreferredSize();
                    x -= dim.width;
                    x -= 5;
                    reportButton.setBounds(x, buttonY, dim.width, dim.height);
                }

                dim = closeButton.getPreferredSize();
                x -= dim.width;
                x -= 5;
                closeButton.setBounds(x, buttonY, dim.width, dim.height);

                //if the dialog is expanded...
                if (detailsPanel.isVisible()) {
                    //layout the details
                    y = buttonY + dim.height + 6;
                    x = leftEdge;
                    int width = rightEdge - x;
                    detailsPanel.setBounds(x, y, width, parent.getHeight() - (y + insets.bottom) );
                }
            }
        }
    }

    private static void centerWindow(Window w, Component owner) {
        //center based on the owner component, if it is not null
        //otherwise, center based on the center of the screen
        if (owner != null) {
            Point p = owner.getLocation();
            p.x += owner.getWidth()/2;
            p.y += owner.getHeight()/2;
            SwingUtilities.convertPointToScreen(p, owner);
            w.setLocation(p);
        } else {
            w.setLocation(WindowUtils.getPointForCentering(w));
        }
    }

    private static void centerWindow(JInternalFrame w, Component owner) {
        //center based on the owner component, if it is not null
        //otherwise, center based on the center of the screen
        if (owner != null) {
            Point p = owner.getLocation();
            p.x += owner.getWidth()/2;
            p.y += owner.getHeight()/2;
            SwingUtilities.convertPointToScreen(p, owner);
            w.setLocation(p);
        } else {
            w.setLocation(WindowUtils.getPointForCentering(w));
        }
    }

    /**
     * Converts the incoming string to an escaped output string. This method
     * is far from perfect, only escaping &lt;, &gt; and &amp; characters
     */
    private static String escapeXml(String input) {
        String s = input == null ? "" : input.replace("&", "&amp;");
        s = s.replace("<", "&lt;");
        return s = s.replace(">", "&gt;");
    }
}
