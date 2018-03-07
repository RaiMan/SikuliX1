/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import org.sikuli.basics.Debug;
import org.sikuli.script.TextRecognizer;

//RaiMan not used import org.sikuli.script.TextRecognizer;
public class PatternPaneNaming extends JPanel {

	final static int TXT_FILE_EXT_LENGTH = 4;
	final static int TXT_FILENAME_LENGTH = 20;
	final static int MAX_OCR_TEXT_LENGTH = 12;
	final static int THUMB_MAX_HEIGHT = 200;

	EditorPatternButton _imgBtn;
	JTextField _txtPath, _txtFileExt;
	JComboBox _txtFilename;
	String _oldFilename;

	static String _I(String key, Object... args) {
		return SikuliIDEI18N._I(key, args);
	}

	public PatternPaneNaming(EditorPatternButton imgBtn, JLabel msgApplied) {
		super(new GridBagLayout());
		init(imgBtn, msgApplied);
	}

	private void init(EditorPatternButton imgBtn, JLabel msgApplied) {
		_imgBtn = imgBtn;
		JLabel lblPath = new JLabel(_I("lblPath"));
		JLabel lblFilename = new JLabel(_I("lblFilename"));

		String filename = _imgBtn.getFilename();
		File f = new File(filename);
		String fullpath = f.getParent();
		filename = getFilenameWithoutExt(f);
		_oldFilename = filename;

		BufferedImage thumb = _imgBtn.createThumbnailImage(THUMB_MAX_HEIGHT);
		Border border = LineBorder.createGrayLineBorder();
		JLabel lblThumb = new JLabel(new ImageIcon(thumb));
		lblThumb.setBorder(border);

		_txtPath = new JTextField(fullpath, TXT_FILENAME_LENGTH);
		_txtPath.setEditable(false);
		_txtPath.setEnabled(false);

		String[] candidates = new String[]{filename};
			//<editor-fold defaultstate="collapsed" desc="OCR --- not used">
			/*
		 String ocrText = getFilenameFromImage(thumb);
		 if(ocrText.length()>0 && !ocrText.equals(filename))
		 candidates = new String[] {filename, ocrText};
		 */
		//</editor-fold>
		_txtFilename = new AutoCompleteCombo(candidates);

		_txtFileExt = new JTextField(getFileExt(f), TXT_FILE_EXT_LENGTH);
		_txtFileExt.setEditable(false);
		_txtFileExt.setEnabled(false);

		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 0;
		c.insets = new Insets(100, 0, 0, 0);
		this.add(new JLabel(""), c);

		c = new GridBagConstraints();
		c.fill = 0;
		c.gridwidth = 3;
		c.gridy = 1;
		c.insets = new Insets(0, 10, 20, 10);
		this.add(lblThumb, c);

		c = new GridBagConstraints();
		c.fill = 1;
		c.gridy = 2;
		this.add(lblPath, c);
		c.gridx = 1;
		c.gridwidth = 2;
		this.add(_txtPath, c);

		c = new GridBagConstraints();
		c.gridy = 3;
		c.fill = 0;
		this.add(lblFilename, c);
		this.add(_txtFilename, c);
		this.add(_txtFileExt, c);

		c = new GridBagConstraints();
		c.gridy = 4;
		c.gridx = 1;
		c.insets = new Insets(200, 0, 0, 0);
		this.add(msgApplied, c);
	}

	protected void updateFilename() {
		_oldFilename = (String) _txtFilename.getSelectedItem();
	}

	private String getFilenameWithoutExt(File f) {
		String name = f.getName();
		int pos = name.lastIndexOf('.');
    if (pos > 0) {
      return name.substring(0, pos);
    }
    return name;
	}

	private String getFileExt(File f) {
		String name = f.getName();
		int pos = name.lastIndexOf('.');
    if (pos > 0) {
      return name.substring(pos);
    }
    return "";
	}

	public static String getFilenameFromImage(BufferedImage img) {
		TextRecognizer tr = TextRecognizer.getInstance();
		if (!PreferencesUser.getInstance().getPrefMoreTextOCR() || tr == null) {
			return "";
		}
		String text = tr.recognize(img);
		text = text.replaceAll("\\W", "");
		if (text.length() > MAX_OCR_TEXT_LENGTH) {
			return text.substring(0, MAX_OCR_TEXT_LENGTH);
		}
		return text;
	}

	public String getAbsolutePath() {
		return _txtPath.getText() + File.separatorChar
						+ _txtFilename.getSelectedItem() + _txtFileExt.getText();
	}

	public boolean isDirty() {
		String newFilename = (String) _txtFilename.getSelectedItem();
		return !_oldFilename.equals(newFilename);
	}
}

class AutoCompleteCombo extends JComboBox {

	private static final String me = "PatternPaneNaming: ";
	final static int TXT_FILENAME_LENGTH = 20;
	public int caretPos = 0;
	public JTextField editor = null;

	public AutoCompleteCombo(final Object items[]) {
		super(items);
		this.setEditable(true);
		setHook();
		//hideDropDownButton();
	}

	private void hideDropDownButton() {
		for (Component component : this.getComponents()) {
			if (component instanceof AbstractButton && component.isVisible()) {
				component.setVisible(false);
				this.revalidate();
			}
		}
	}

	@Override
	public void setSelectedIndex(int ind) {
		super.setSelectedIndex(ind);
		editor.setText(getItemAt(ind).toString());
		editor.setSelectionEnd(caretPos + editor.getText().length());
		editor.moveCaretPosition(caretPos);
	}

	public void setHook() {
		ComboBoxEditor anEditor = this.getEditor();
		if (anEditor.getEditorComponent() instanceof JTextField) {
			editor = (JTextField) anEditor.getEditorComponent();
			editor.setColumns(TXT_FILENAME_LENGTH);
			editor.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent ev) {
					char key = ev.getKeyChar();
					if (!(Character.isLetterOrDigit(key) || Character
									.isSpaceChar(key))) {
						return;
					}
					caretPos = editor.getCaretPosition();
					String text = "";
					try {
						text = editor.getText(0, caretPos);
					} catch (Exception ex) {
						Debug.error(me + "setHook: Problem getting image file name\n%s", ex.getMessage());
					}
					int n = getItemCount();
					for (int i = 0; i < n; i++) {
						int ind = ((String) getItemAt(i)).indexOf(text);
						if (ind == 0) {
							setSelectedIndex(i);
							return;
						}
					}
				}
			});
		}
	}

}
