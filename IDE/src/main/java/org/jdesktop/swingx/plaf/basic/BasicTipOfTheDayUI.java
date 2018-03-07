/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.html.HTMLDocument;

import org.jdesktop.swingx.JXTipOfTheDay;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.JXTipOfTheDay.ShowOnStartupChoice;
import org.jdesktop.swingx.plaf.TipOfTheDayUI;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.tips.TipOfTheDayModel.Tip;

/**
 * Base implementation of the <code>JXTipOfTheDay</code> UI.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class BasicTipOfTheDayUI extends TipOfTheDayUI {

  public static ComponentUI createUI(JComponent c) {
    return new BasicTipOfTheDayUI((JXTipOfTheDay)c);
  }

  protected JXTipOfTheDay tipPane;
  protected JPanel tipArea;
  protected Component currentTipComponent;

  protected Font tipFont;
  protected PropertyChangeListener changeListener;

  public BasicTipOfTheDayUI(JXTipOfTheDay tipPane) {
    this.tipPane = tipPane;
  }

  @Override
  public JDialog createDialog(Component parentComponent,
    final ShowOnStartupChoice choice) {
    return createDialog(parentComponent, choice, true);
  }

  protected JDialog createDialog(Component parentComponent,
    final ShowOnStartupChoice choice,
    boolean showPreviousButton) {
    Locale locale = parentComponent==null ? null : parentComponent.getLocale();
    String title = UIManagerExt.getString("TipOfTheDay.dialogTitle", locale);

    final JDialog dialog;

    Window window;
    if (parentComponent == null) {
      window = JOptionPane.getRootFrame();
    } else {
      window = (parentComponent instanceof Window)?(Window)parentComponent
        :SwingUtilities.getWindowAncestor(parentComponent);
    }

    if (window instanceof Frame) {
      dialog = new JDialog((Frame)window, title, true);
    } else {
      dialog = new JDialog((Dialog)window, title, true);
    }

    dialog.getContentPane().setLayout(new BorderLayout(10, 10));
    dialog.getContentPane().add(tipPane, BorderLayout.CENTER);
    ((JComponent)dialog.getContentPane()).setBorder(BorderFactory
      .createEmptyBorder(10, 10, 10, 10));

    final JCheckBox showOnStartupBox;

    // tip controls
    JPanel controls = new JPanel(new BorderLayout());
    dialog.add("South", controls);

    if (choice != null) {
      showOnStartupBox = new JCheckBox(UIManagerExt
        .getString("TipOfTheDay.showOnStartupText", locale), choice
        .isShowingOnStartup());
      controls.add(showOnStartupBox, BorderLayout.CENTER);
    } else {
      showOnStartupBox = null;
    }

    JPanel buttons =
      new JPanel(new GridLayout(1, showPreviousButton?3:2, 9, 0));
    controls.add(buttons, BorderLayout.LINE_END);

    if (showPreviousButton) {
      JButton previousTipButton = new JButton(UIManagerExt
        .getString("TipOfTheDay.previousTipText", locale));
      buttons.add(previousTipButton);
      previousTipButton.addActionListener(getActionMap().get("previousTip"));
    }

    JButton nextTipButton = new JButton(UIManagerExt
      .getString("TipOfTheDay.nextTipText", locale));
    buttons.add(nextTipButton);
    nextTipButton.addActionListener(getActionMap().get("nextTip"));

    JButton closeButton = new JButton(UIManagerExt
      .getString("TipOfTheDay.closeText", locale));
    buttons.add(closeButton);

    final ActionListener saveChoice = new ActionListener() {
      @Override
    public void actionPerformed(ActionEvent e) {
        if (choice != null) {
          choice.setShowingOnStartup(showOnStartupBox.isSelected());
        }
        dialog.setVisible(false);
      }
    };

    closeButton.addActionListener(new ActionListener() {
      @Override
    public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        saveChoice.actionPerformed(null);
      }
    });
    dialog.getRootPane().setDefaultButton(closeButton);

    dialog.addWindowListener(new WindowAdapter() {
      @Override
    public void windowClosing(WindowEvent e) {
        saveChoice.actionPerformed(null);
      }
    });

    ((JComponent)dialog.getContentPane()).registerKeyboardAction(saveChoice,
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
      JComponent.WHEN_IN_FOCUSED_WINDOW);

    dialog.pack();
    dialog.setLocationRelativeTo(parentComponent);

    return dialog;
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);
    installDefaults();
    installKeyboardActions();
    installComponents();
    installListeners();

    showCurrentTip();
  }

  protected void installKeyboardActions() {
    ActionMap map = getActionMap();
    if (map != null) {
      SwingUtilities.replaceUIActionMap(tipPane, map);
    }
  }

  ActionMap getActionMap() {
    ActionMap map = new ActionMapUIResource();
    map.put("previousTip", new PreviousTipAction());
    map.put("nextTip", new NextTipAction());
    return map;
  }

  protected void installListeners() {
    changeListener = createChangeListener();
    tipPane.addPropertyChangeListener(changeListener);
  }

  protected PropertyChangeListener createChangeListener() {
    return new ChangeListener();
  }

  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(tipPane, "TipOfTheDay.background",
      "TipOfTheDay.foreground", "TipOfTheDay.font");
    LookAndFeel.installBorder(tipPane, "TipOfTheDay.border");
    LookAndFeel.installProperty(tipPane, "opaque", Boolean.TRUE);
    tipFont = UIManager.getFont("TipOfTheDay.tipFont");
  }

  protected void installComponents() {
    tipPane.setLayout(new BorderLayout());

    // tip icon
    JLabel tipIcon = new JLabel(UIManagerExt
      .getString("TipOfTheDay.didYouKnowText", tipPane.getLocale()));
    tipIcon.setIcon(UIManager.getIcon("TipOfTheDay.icon"));
    tipIcon.setBorder(BorderFactory.createEmptyBorder(22, 15, 22, 15));
    tipPane.add("North", tipIcon);

    // tip area
    tipArea = new JPanel(new BorderLayout(2, 2));
    tipArea.setOpaque(false);
    tipArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tipPane.add("Center", tipArea);
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    return new Dimension(420, 175);
  }

  protected void showCurrentTip() {
    if (currentTipComponent != null) {
      tipArea.remove(currentTipComponent);
    }

    int currentTip = tipPane.getCurrentTip();
    if (currentTip == -1) {
      JLabel label = new JLabel();
      label.setOpaque(true);
      label.setBackground(UIManager.getColor("TextArea.background"));
      currentTipComponent = label;
      tipArea.add("Center", currentTipComponent);
      return;
    }

    // tip does not fall in current tip range
    if (tipPane.getModel() == null || tipPane.getModel().getTipCount() == 0
      || (currentTip < 0 && currentTip >= tipPane.getModel().getTipCount())) {
      currentTipComponent = new JLabel();
    } else {
      Tip tip = tipPane.getModel().getTipAt(currentTip);

      Object tipObject = tip.getTip();
      if (tipObject instanceof Component) {
        currentTipComponent = (Component)tipObject;
      } else if (tipObject instanceof Icon) {
        currentTipComponent = new JLabel((Icon)tipObject);
      } else {
        JScrollPane tipScroll = new JScrollPane();
        tipScroll.setBorder(null);
        tipScroll.setOpaque(false);
        tipScroll.getViewport().setOpaque(false);
        tipScroll.setBorder(null);

        String text = tipObject == null?"":tipObject.toString();

        if (BasicHTML.isHTMLString(text)) {
          JEditorPane editor = new JEditorPane("text/html", text);
          editor.setFont(tipPane.getFont());
//          BasicHTML.updateRenderer(editor, text);
          SwingXUtilities.setHtmlFont(
                  (HTMLDocument) editor.getDocument(), tipPane.getFont());
          editor.setEditable(false);
          editor.setBorder(null);
          editor.setMargin(null);
          editor.setOpaque(false);
          tipScroll.getViewport().setView(editor);
        } else {
          JTextArea area = new JTextArea(text);
          area.setFont(tipPane.getFont());
          area.setEditable(false);
          area.setLineWrap(true);
          area.setWrapStyleWord(true);
          area.setBorder(null);
          area.setMargin(null);
          area.setOpaque(false);
          tipScroll.getViewport().setView(area);
        }

        currentTipComponent = tipScroll;
      }
    }

    tipArea.add("Center", currentTipComponent);
    tipArea.revalidate();
    tipArea.repaint();
  }

  @Override
  public void uninstallUI(JComponent c) {
    uninstallListeners();
    uninstallComponents();
    uninstallDefaults();
    super.uninstallUI(c);
  }

  protected void uninstallListeners() {
    tipPane.removePropertyChangeListener(changeListener);
  }

  protected void uninstallComponents() {}

  protected void uninstallDefaults() {}

  class ChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (JXTipOfTheDay.CURRENT_TIP_CHANGED_KEY.equals(evt.getPropertyName())) {
        showCurrentTip();
      }
    }
  }

  class PreviousTipAction extends AbstractAction {
    public PreviousTipAction() {
      super("previousTip");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      tipPane.previousTip();
    }
    @Override
    public boolean isEnabled() {
      return tipPane.isEnabled();
    }
  }

  class NextTipAction extends AbstractAction {
    public NextTipAction() {
      super("nextTip");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      tipPane.nextTip();
    }
    @Override
    public boolean isEnabled() {
      return tipPane.isEnabled();
    }
  }

}
