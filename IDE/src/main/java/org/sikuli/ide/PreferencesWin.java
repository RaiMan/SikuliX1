/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Element;
import org.jdesktop.layout.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.Key;

/*
 * Created by JFormDesigner on Mon Nov 16 10:13:52 EST 2009
 */
public class PreferencesWin extends JFrame {

  PreferencesUser pref = PreferencesUser.getInstance();
  private boolean isInitialized = false;
  int cap_hkey, cap_mod;
  int old_cap_hkey, old_cap_mod;
  Font _oldFont;
  String _oldFontName;
  int _oldFontSize;
  private double _delay;
  private int _old_cap_hkey, _old_cap_mod;
  private int _autoNamingMethod;
  private boolean _chkAutoUpdate;
  private boolean _chkExpandTab;
  private int _spnTabWidth;
  Locale _locale;
  EditorPane codePane;
	JFrame winPrefMore;

  boolean isDirty = false;

  //<editor-fold defaultstate="collapsed" desc="JFormDesigner - Variables declaration">
  // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
  private JTabbedPane _tabPane;
  private JTextField _txtHotkey;
  private JLabel _lblHotkey;
  private JLabel _lblDelay;
  private JSpinner spnDelay;
  private JLabel _lblDelaySecs;
  private JLabel _lblNaming;
  private JRadioButton _radTimestamp;
  private JRadioButton _radOCR;
  private JRadioButton _radOff;
  private JPanel _paneTextEditing;
  private JCheckBox chkExpandTab;
  private JLabel _lblTabWidth;
  private JComboBox _cmbFontName;
  private JLabel _lblFont;
  private JLabel _titleAppearance;
  private JLabel _titleIndentation;
  private JSpinner spnTabWidth;
  private JLabel _lblFontSize;
  private JSpinner _spnFontSize;
  private JCheckBox chkAutoUpdate;
  private JComboBox _cmbLang;
  private JLabel _lblUpdates;
  private JLabel _lblLanguage;
  private JButton _btnOk;
  private JButton _btnApply;
  private JButton _btnCancel;
	private JButton _btnMore;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
  //</editor-fold>

  public PreferencesWin() {
    setTitle(SikuliIDE._I("winPreferences"));
    initComponents();
    loadPrefs();
    isInitialized = true;
  }

  private void initComponents() {
    // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
    DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
    _tabPane = new JTabbedPane();
    JPanel paneCapture = new JPanel();
    _txtHotkey = new JTextField();
    _lblHotkey = new JLabel();
    _lblDelay = new JLabel();
    spnDelay = new JSpinner();
    _lblDelaySecs = new JLabel();
    _lblNaming = new JLabel();
    _radTimestamp = new JRadioButton();
    _radOCR = new JRadioButton();
    _radOff = new JRadioButton();
    _paneTextEditing = new JPanel();
    chkExpandTab = new JCheckBox();
    _lblTabWidth = new JLabel();
    _cmbFontName = new JComboBox();
    _lblFont = new JLabel();
    _titleAppearance = compFactory.createTitle("");
    _titleIndentation = compFactory.createTitle("");
    spnTabWidth = new JSpinner();
    _lblFontSize = new JLabel();
    _spnFontSize = new JSpinner();
    JPanel paneGeneral = new JPanel();
    chkAutoUpdate = new JCheckBox();
    _cmbLang = new JComboBox();
    _lblUpdates = new JLabel();
    _lblLanguage = new JLabel();
    JPanel paneOkCancel = new JPanel();
    JPanel hSpacer1 = new JPanel(null);
    _btnOk = new JButton();
    _btnApply = new JButton();
    _btnCancel = new JButton();
		_btnMore = new JButton();

    //======== this ========
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    //======== _tabPane ========
    {
      _tabPane.setBorder(new EmptyBorder(10, 10, 0, 10));

      //======== paneCapture ========
      {

        //---- _txtHotkey ----
        _txtHotkey.setHorizontalAlignment(SwingConstants.RIGHT);
        _txtHotkey.addFocusListener(new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent e) {
            txtHotkeyFocusGained(e);
          }
        });
        _txtHotkey.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            txtHotkeyKeyPressed(e);
          }
        });

        //---- _lblHotkey ----
        _lblHotkey.setLabelFor(_txtHotkey);

        //---- _lblDelay ----
        _lblDelay.setLabelFor(spnDelay);

        //---- _spnDelay ----
        spnDelay.setModel(new SpinnerNumberModel(1.0, 0.0, null, 0.1));

        //---- _radTimestamp ----
        _radTimestamp.setSelected(true);

        GroupLayout paneCaptureLayout = new GroupLayout(paneCapture);
        paneCapture.setLayout(paneCaptureLayout);
        paneCaptureLayout.setHorizontalGroup(
                paneCaptureLayout.createParallelGroup()
                .add(paneCaptureLayout.createSequentialGroup()
                .add(26, 26, 26)
                .add(paneCaptureLayout.createParallelGroup()
                .add(GroupLayout.TRAILING, _lblDelay)
                .add(GroupLayout.TRAILING, _lblHotkey)
                .add(GroupLayout.TRAILING, _lblNaming))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(paneCaptureLayout.createParallelGroup()
                .add(_radTimestamp)
                .add(_radOCR)
                .add(_radOff)
                .add(paneCaptureLayout.createSequentialGroup()
                .add(spnDelay, GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(_lblDelaySecs, GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                .add(_txtHotkey, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .add(69, 69, 69)));
        paneCaptureLayout.setVerticalGroup(
                paneCaptureLayout.createParallelGroup()
                .add(paneCaptureLayout.createSequentialGroup()
                .add(34, 34, 34)
                .add(paneCaptureLayout.createParallelGroup(GroupLayout.BASELINE)
                .add(_lblHotkey, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                .add(_txtHotkey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(paneCaptureLayout.createParallelGroup()
                .add(_lblDelay, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                .add(spnDelay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(_lblDelaySecs, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(paneCaptureLayout.createParallelGroup(GroupLayout.LEADING, false)
                .add(paneCaptureLayout.createSequentialGroup()
                .add(paneCaptureLayout.createParallelGroup(GroupLayout.BASELINE)
                .add(_lblNaming, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                .add(_radTimestamp))
                .add(18, 18, 18)
                .add(_radOff)
                .addPreferredGap(LayoutStyle.RELATED))
                .add(GroupLayout.TRAILING, paneCaptureLayout.createSequentialGroup()
                .add(_radOCR)
                .add(21, 21, 21)))
                .add(80, 80, 80)));
      }
      _tabPane.addTab(SikuliIDEI18N._I("prefTabScreenCapturing"), paneCapture);

      //======== _paneTextEditing ========
      {

        //---- _lblTabWidth ----
        _lblTabWidth.setLabelFor(spnTabWidth);

        //---- _cmbFontName ----
        _cmbFontName.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            fontNameItemStateChanged(e);
          }
        });

        //---- _lblFont ----
        _lblFont.setLabelFor(_cmbFontName);

        //---- _lblFontSize ----
        _lblFontSize.setLabelFor(_spnFontSize);

        //---- _spnFontSize ----
        _spnFontSize.addChangeListener(new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            fontSizeStateChanged(e);
          }
        });

        GroupLayout _paneTextEditingLayout = new GroupLayout(_paneTextEditing);
        _paneTextEditing.setLayout(_paneTextEditingLayout);
        _paneTextEditingLayout.setHorizontalGroup(
                _paneTextEditingLayout.createParallelGroup()
                .add(GroupLayout.TRAILING, _paneTextEditingLayout.createSequentialGroup()
                .add(95, 95, 95)
                .add(_paneTextEditingLayout.createParallelGroup()
                .add(_titleIndentation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(_paneTextEditingLayout.createSequentialGroup()
                .add(_titleAppearance, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(_paneTextEditingLayout.createParallelGroup()
                .add(_paneTextEditingLayout.createSequentialGroup()
                .add(29, 29, 29)
                .add(_paneTextEditingLayout.createParallelGroup()
                .add(GroupLayout.TRAILING, _lblTabWidth)
                .add(GroupLayout.TRAILING, _lblFont)
                .add(GroupLayout.TRAILING, _lblFontSize))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(_paneTextEditingLayout.createParallelGroup()
                .add(_cmbFontName, 0, 218, Short.MAX_VALUE)
                .add(_spnFontSize, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                .add(spnTabWidth, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED, 97, Short.MAX_VALUE))
                .add(chkExpandTab, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))))
                .addContainerGap()));
        _paneTextEditingLayout.setVerticalGroup(
                _paneTextEditingLayout.createParallelGroup()
                .add(_paneTextEditingLayout.createSequentialGroup()
                .add(21, 21, 21)
                .add(_titleIndentation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(_paneTextEditingLayout.createParallelGroup()
                .add(_paneTextEditingLayout.createSequentialGroup()
                .add(81, 81, 81)
                .add(_titleAppearance, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(_paneTextEditingLayout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.RELATED)
                .add(chkExpandTab)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(_paneTextEditingLayout.createParallelGroup()
                .add(_lblTabWidth, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                .add(spnTabWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(40, 40, 40)
                .add(_paneTextEditingLayout.createParallelGroup(GroupLayout.BASELINE)
                .add(_lblFont)
                .add(_cmbFontName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(_paneTextEditingLayout.createParallelGroup(GroupLayout.TRAILING)
                .add(_lblFontSize, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                .add(_spnFontSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(154, Short.MAX_VALUE)));
        _paneTextEditingLayout.linkSize(new Component[]{_lblTabWidth, spnTabWidth}, GroupLayout.VERTICAL);
        _paneTextEditingLayout.linkSize(new Component[]{_cmbFontName, _lblFont}, GroupLayout.VERTICAL);
      }
      _tabPane.addTab(SikuliIDEI18N._I("PreferencesWin.paneTextEditing.tab.title"), _paneTextEditing);

      //======== paneGeneral ========
      {

        //---- _lblUpdates ----
        _lblUpdates.setFont(_lblUpdates.getFont().deriveFont(_lblUpdates.getFont().getStyle() | Font.BOLD));

        //---- _lblLanguage ----
        _lblLanguage.setFont(_lblLanguage.getFont().deriveFont(_lblLanguage.getFont().getStyle() | Font.BOLD));

        GroupLayout paneGeneralLayout = new GroupLayout(paneGeneral);
        paneGeneral.setLayout(paneGeneralLayout);
        paneGeneralLayout.setHorizontalGroup(
                paneGeneralLayout.createParallelGroup()
                .add(paneGeneralLayout.createSequentialGroup()
                .add(137, 137, 137)
                .add(paneGeneralLayout.createParallelGroup()
                .add(paneGeneralLayout.createSequentialGroup()
                .add(_lblLanguage)
                .add(185, 185, 185))
                .add(paneGeneralLayout.createSequentialGroup()
                .add(38, 38, 38)
                .add(_cmbLang, GroupLayout.PREFERRED_SIZE, 215, GroupLayout.PREFERRED_SIZE))
                .add(paneGeneralLayout.createSequentialGroup()
                .add(_lblUpdates)
                .add(318, 318, 318))
                .add(GroupLayout.TRAILING, paneGeneralLayout.createSequentialGroup()
                .add(38, 38, 38)
                .add(chkAutoUpdate, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)))
                .addContainerGap()));
        paneGeneralLayout.setVerticalGroup(
                paneGeneralLayout.createParallelGroup()
                .add(paneGeneralLayout.createSequentialGroup()
                .add(26, 26, 26)
                .add(_lblUpdates)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(chkAutoUpdate)
                .add(40, 40, 40)
                .add(_lblLanguage)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(_cmbLang, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(196, Short.MAX_VALUE)));
      }
      _tabPane.addTab(SikuliIDEI18N._I("prefTabGeneralSettings"), paneGeneral);

    }
    contentPane.add(_tabPane, BorderLayout.CENTER);

    //======== paneOkCancel ========
    {
      paneOkCancel.setBorder(new EmptyBorder(5, 5, 5, 5));
      paneOkCancel.setLayout(new BoxLayout(paneOkCancel, BoxLayout.X_AXIS));
      paneOkCancel.add(hSpacer1);

      //---- _btnMore ----
      _btnMore.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnMoreActionPerformed(e);
        }
      });
      paneOkCancel.add(_btnMore);

      //---- _btnOk ----
      _btnOk.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnOkActionPerformed(e);
        }
      });
      paneOkCancel.add(_btnOk);

      //---- _btnApply ----
      _btnApply.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnApplyActionPerformed(e);
        }
      });
      paneOkCancel.add(_btnApply);

      //---- _btnCancel ----
      _btnCancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnCancelActionPerformed(e);
        }
      });

      this.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          _btnCancel.doClick();
        }
      });

      paneOkCancel.add(_btnCancel);
    }
    contentPane.add(paneOkCancel, BorderLayout.SOUTH);
    setSize(600, 475);
    setLocationRelativeTo(getOwner());

    //---- btngrpNaming ----
    ButtonGroup btngrpNaming = new ButtonGroup();
    btngrpNaming.add(_radTimestamp);
    btngrpNaming.add(_radOCR);
    btngrpNaming.add(_radOff);

    initComponentsI18n();
    // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }

  private void initComponentsI18n() {
    // JFormDesigner - Component i18n initialization - DO NOT MODIFY  //GEN-BEGIN:initI18n
    DefaultComponentFactory.setTextAndMnemonic(_titleAppearance, SikuliIDEI18N._I("PreferencesWin.titleAppearance.textWithMnemonic"));
    DefaultComponentFactory.setTextAndMnemonic(_titleIndentation, SikuliIDEI18N._I("PreferencesWin.titleIndentation.textWithMnemonic"));
    _lblHotkey.setText(SikuliIDEI18N._I("prefCaptureHotkey"));
    _lblDelay.setText(SikuliIDEI18N._I("prefCaptureDelay"));
    _lblDelaySecs.setText(SikuliIDEI18N._I("prefSeconds"));
    _lblNaming.setText(SikuliIDEI18N._I("prefAutoNaming"));
    _radTimestamp.setText(SikuliIDEI18N._I("prefTimestamp"));
    _radOCR.setText(SikuliIDEI18N._I("prefRecognizedText"));
    _radOff.setText(SikuliIDEI18N._I("prefManualInput"));
    _tabPane.setTitleAt(0, SikuliIDEI18N._I("prefTabScreenCapturing"));
    chkExpandTab.setText(SikuliIDEI18N._I("PreferencesWin.chkExpandTab.text"));
    _lblTabWidth.setText(SikuliIDEI18N._I("PreferencesWin.lblTabWidth.text"));
    _lblFont.setText(SikuliIDEI18N._I("PreferencesWin.lblFont.text"));
    _lblFontSize.setText(SikuliIDEI18N._I("PreferencesWin.lblFontSize.text"));
    _tabPane.setTitleAt(1, SikuliIDEI18N._I("PreferencesWin.paneTextEditing.tab.title"));
    chkAutoUpdate.setText(SikuliIDEI18N._I("prefGeneralAutoCheck"));
    _lblUpdates.setText(SikuliIDEI18N._I("PreferencesWin.lblUpdates.text"));
    _lblLanguage.setText(SikuliIDEI18N._I("PreferencesWin.lblLanguage.text"));
    _tabPane.setTitleAt(2, SikuliIDEI18N._I("prefTabGeneralSettings"));
    _btnMore.setText(SikuliIDEI18N._I("more"));
    _btnOk.setText(SikuliIDEI18N._I("ok"));
    _btnApply.setText(SikuliIDEI18N._I("apply"));
    _btnCancel.setText(SikuliIDEI18N._I("cancel"));
    // JFormDesigner - End of component i18n initialization  //GEN-END:initI18n
  }

  private void loadPrefs() {
    SikuliIDE ide = SikuliIDE.getInstance();
    _delay = pref.getCaptureDelay();
    spnDelay.setValue(_delay);
    _old_cap_hkey = old_cap_hkey = cap_hkey = pref.getCaptureHotkey();
    _old_cap_mod = old_cap_mod = cap_mod = pref.getCaptureHotkeyModifiers();
    setTxtHotkey(cap_hkey, cap_mod);
    _autoNamingMethod = pref.getAutoNamingMethod();
    switch (_autoNamingMethod) {
      case PreferencesUser.AUTO_NAMING_TIMESTAMP:
        _radTimestamp.setSelected(true);
        break;
      case PreferencesUser.AUTO_NAMING_OCR:
        _radOCR.setSelected(true);
        break;
      case PreferencesUser.AUTO_NAMING_OFF:
        _radOff.setSelected(true);
        break;
      default:
        Debug.error("Error in reading auto naming method preferences");
    }
    _chkAutoUpdate = pref.getCheckUpdate();
    chkAutoUpdate.setSelected(_chkAutoUpdate);

    _chkExpandTab = pref.getExpandTab();
    chkExpandTab.setSelected(_chkExpandTab);

    _spnTabWidth = pref.getTabWidth();
    spnTabWidth.setValue(_spnTabWidth);
    initFontPrefs();
    initLangPrefs();
    codePane = ide.getCurrentCodePane();
    if (codePane != null) {
      _oldFont = codePane.getFont();
    } else {
      _oldFontName = pref.getFontName();
      _oldFontSize = pref.getFontSize();
    }
    _locale = pref.getLocale();
  }

  private void savePrefs() {
    SikuliIDE ide = SikuliIDE.getInstance();
    pref.setCaptureDelay((Double) spnDelay.getValue());
    pref.setCaptureHotkey(cap_hkey);
    pref.setCaptureHotkeyModifiers(cap_mod);
    pref.setAutoNamingMethod(
            _radTimestamp.isSelected() ? PreferencesUser.AUTO_NAMING_TIMESTAMP
            : _radOCR.isSelected() ? PreferencesUser.AUTO_NAMING_OCR
            : PreferencesUser.AUTO_NAMING_OFF);
    if (pref.getAutoNamingMethod() != PreferencesUser.AUTO_NAMING_TIMESTAMP) {
      pref.setPrefMoreTextOCR(true);
    }
    if (old_cap_hkey != cap_hkey || old_cap_mod != cap_mod) {
      ide.removeCaptureHotkey();
      ide.installCaptureHotkey();
    }
    pref.setCheckUpdate(chkAutoUpdate.isSelected());

    pref.setExpandTab(chkExpandTab.isSelected());
    pref.setTabWidth((Integer) spnTabWidth.getValue());

    pref.setFontName((String) _cmbFontName.getSelectedItem());
    pref.setFontSize((Integer) _spnFontSize.getValue());

    Locale locale = (Locale) _cmbLang.getSelectedItem();
    pref.setLocale(locale);
    SikuliIDEI18N.setLocale(locale);
    isDirty = true;
  }

  private void resetPrefs() {
    SikuliIDE ide = SikuliIDE.getInstance();
    pref.setCaptureDelay(_delay);
    pref.setCaptureHotkey(_old_cap_hkey);
    pref.setCaptureHotkeyModifiers(_old_cap_mod);
    if (old_cap_hkey != _old_cap_hkey || old_cap_mod != _old_cap_mod) {
      ide.removeCaptureHotkey();
      ide.installCaptureHotkey();
    }
    pref.setAutoNamingMethod(_autoNamingMethod);
    pref.setCheckUpdate(_chkAutoUpdate);

    pref.setExpandTab(_chkExpandTab);
    pref.setTabWidth(_spnTabWidth);

    if (codePane == null) {
      pref.setFontName(_oldFontName);
      pref.setFontSize(_oldFontSize);
    } else {
      pref.setFontName(_oldFont.getFontName());
      pref.setFontSize(_oldFont.getSize());
      codePane.setFont(_oldFont);
    }

    pref.setLocale(_locale);
    SikuliIDEI18N.setLocale(_locale);
  }

  private void initFontPrefs() {
    String[] fontList = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
    for (String font : fontList) {
      _cmbFontName.addItem(font);
    }
    _cmbFontName.setSelectedItem(pref.getFontName());
    _spnFontSize.setValue(pref.getFontSize());
  }

  private void initLangPrefs() {
    String[] SUPPORT_LOCALES = {
      "it", "es", "pt_BR", "ar", "fr", "ru", "bg", "he", "sv", "ca", "ja", "tr",
      "da", "ko", "uk", "de", "nl", "zh_CN", "en_US", "pl", "zh_TW", "ta_IN"
    };
    Locale[] sortedLocales = new Locale[SUPPORT_LOCALES.length];
    int count = 0;
    for (String locale_code : SUPPORT_LOCALES) {
      Locale l;
      if (locale_code.indexOf("_") >= 0) {
        String[] lang_country = locale_code.split("_");
        l = new Locale(lang_country[0], lang_country[1]);
      } else {
        l = new Locale(locale_code);
      }
      sortedLocales[count++] = l;
    }
    Arrays.sort(sortedLocales, new Comparator<Locale>() {
			@Override
      public int compare(Locale l1, Locale l2) {
        return l1.getDisplayLanguage().compareTo(l2.getDisplayLanguage());
      }
    });

    for (Locale l : sortedLocales) {
      _cmbLang.addItem(l);
    }
    _cmbLang.setRenderer(new LocaleListCellRenderer());
    Locale curLocale = pref.getLocale();
    _cmbLang.setSelectedItem(curLocale);
    if (!_cmbLang.getSelectedItem().equals(curLocale)) {
      if (curLocale.getVariant().length() > 0) {
        curLocale = new Locale(curLocale.getLanguage(), curLocale.getCountry());
        _cmbLang.setSelectedItem(curLocale);
      }
      if (!_cmbLang.getSelectedItem().equals(curLocale)) {
        _cmbLang.setSelectedItem(new Locale(curLocale.getLanguage()));
      }
    }
  }

  private void btnMoreActionPerformed(ActionEvent e) {
		winPrefMore = new JFrame("Preferences: more Options ...");
    Container mpwinCP = winPrefMore.getContentPane();
    mpwinCP.setLayout(new BorderLayout());
		mpwinCP.add(new PreferencesWindowMore(), BorderLayout.CENTER);
		winPrefMore.pack();
		winPrefMore.setAlwaysOnTop(true);
		winPrefMore.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		if (Settings.isJava7()) {
			winPrefMore.setLocation(getLocation().x-70, getLocation().y);
		}
		else {
			winPrefMore.setLocation(getLocation().x+getWidth()+10, getLocation().y);
		}
		winPrefMore.setVisible(true);
  }

  private void btnOkActionPerformed(ActionEvent e) {
    savePrefs();
    String warn = "Until some bugs have been fixed,\n" +
            "you should restart the IDE now!\n" +
						"(except for most options in [more options ...])\n" +
            "Otherwise you might notice strange behavior ;-)\n" +
            "--- but only if you have made any changes!\n\n" +
            "Use CANCEL next time, if nothing was changed!";
    JOptionPane.showMessageDialog(this, warn,
            "--- Preferences have been saved ---", JOptionPane.WARNING_MESSAGE);
		if (winPrefMore != null) winPrefMore.dispose();
    this.dispose();
  }

  private void btnApplyActionPerformed(ActionEvent e) {
    savePrefs();
  }

  private void btnCancelActionPerformed(ActionEvent e) {
    if (isDirty) {
      resetPrefs();
    }
		if (winPrefMore != null) winPrefMore.dispose();
	  this.dispose();
  }

  private void setTxtHotkey(int code, int mod) {
    cap_hkey = code;
    cap_mod = mod;
    _txtHotkey.setText(Key.convertKeyToText(code, mod));
  }

  private void txtHotkeyFocusGained(FocusEvent e) {
    _txtHotkey.setEditable(true);
  }

  private void txtHotkeyKeyPressed(KeyEvent e) {
    int code = e.getKeyCode();
    int mod = e.getModifiers();
    Debug.log(2, "HotKey: " + code + " " + mod);
    setTxtHotkey(code, mod);
    _txtHotkey.setEditable(false);
  }

  private void updateFontPreview() {
    if (! isInitialized || codePane == null) {
      return;
    }
    SikuliIDE ide = SikuliIDE.getInstance();
    Font font = new Font((String) _cmbFontName.getSelectedItem(), Font.PLAIN,
            (Integer) _spnFontSize.getValue());
    try {
      Element root = codePane.getDocument().getDefaultRootElement();
      codePane.jumpTo(root.getElementIndex(codePane.getCaretPosition()));
    } catch (Exception ex) {
    }
    codePane.setFont(font);
    isDirty = true;
  }

  private void fontNameItemStateChanged(ItemEvent e) {
    updateFontPreview();
  }

  private void fontSizeStateChanged(ChangeEvent e) {
    updateFontPreview();
  }
}

//<editor-fold defaultstate="collapsed" desc="class LocaleListCellRenderer">
class LocaleListCellRenderer extends DefaultListCellRenderer {

  @Override
  public Component getListCellRendererComponent(JList list,
  Object value, int index, boolean isSelected, boolean hasFocus) {
    Locale locale = (Locale) (value);
    return super.getListCellRendererComponent(list,
            locale.getDisplayName(locale), index, isSelected, hasFocus);
  }
}
//</editor-fold>
