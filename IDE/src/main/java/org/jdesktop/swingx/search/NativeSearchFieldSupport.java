/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.search;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.jdesktop.swingx.plaf.AbstractUIChangeHandler;
import org.jdesktop.swingx.util.OS;

/**
 * TODO: comment
 *
 * @author Peter Weishapl <petw@gmx.net>
 */
public class NativeSearchFieldSupport {
	public static final String FIND_POPUP_PROPERTY = "JTextField.Search.FindPopup";
	public static final String FIND_ACTION_PROPERTY = "JTextField.Search.FindAction";
	public static final String MAC_SEARCH_VARIANT = "search";
	public static final String MAC_TEXT_FIELD_VARIANT_PROPERTY = "JTextField.variant";
	public static final String CANCEL_ACTION_PROPERTY = "JTextField.Search.CancelAction";

	/**
	 * @return <code>true</code> if we run Leopard and the Mac Look And Feel.
	 */
	public static boolean isNativeSearchFieldSupported() {
		try {
			String versionString = System.getProperty("os.version");
			// Mac versions have the format 10.x or 10.x.x
			if (versionString.length() < 4) {
				return false;
			}
			// only the part 10.x is important
			versionString = versionString.substring(0, 4);

			return OS.isMacOSX() && Float.parseFloat(versionString) >= 10.5
					&& UIManager.getLookAndFeel().getName().equals("Mac OS X");
		} catch (Exception e) {
			// in case the os.version cannot be parsed, we are surely not
			// running mac os x.
			return false;
		}
	}

	public static void setSearchField(JTextField txt, boolean isSearchField) {
		// Leopard Hack: ensure property change event is triggered, if nothing
		// changes.
		if (isSearchField == isSearchField(txt)) {
			txt.putClientProperty(MAC_TEXT_FIELD_VARIANT_PROPERTY, "_triggerevent_");
		} else if (isSearchField) {
			// if we have a search field here, register listener for ui changes
			// (leopard hack)
			uiChangeHandler.install(txt);
		} else {
			// if we don't have a search field, we don't need to listen anymore.
			uiChangeHandler.uninstall(txt);
		}

		if (isSearchField) {
			txt.putClientProperty(MAC_TEXT_FIELD_VARIANT_PROPERTY, MAC_SEARCH_VARIANT);
			txt.putClientProperty("Quaqua.TextField.style", MAC_SEARCH_VARIANT);

		} else {
			txt.putClientProperty(MAC_TEXT_FIELD_VARIANT_PROPERTY, "default");
			txt.putClientProperty("Quaqua.TextField.style", "default");
		}
	}

	public static boolean isSearchField(JTextField txt) {
		return MAC_SEARCH_VARIANT.equals(txt.getClientProperty(MAC_TEXT_FIELD_VARIANT_PROPERTY));
	}

	public static boolean isNativeSearchField(JTextField txt) {
		return isSearchField(txt) && isNativeSearchFieldSupported();
	}

	public static void setFindPopupMenu(JTextField txt, JPopupMenu popupMenu) {
		txt.putClientProperty(FIND_POPUP_PROPERTY, popupMenu);
	}

	public static JPopupMenu getFindPopupMenu(JTextField txt) {
		return (JPopupMenu) txt.getClientProperty(FIND_POPUP_PROPERTY);
	}

	public static void setFindAction(JTextField txt, ActionListener findAction) {
		txt.putClientProperty(FIND_ACTION_PROPERTY, findAction);
	}

	public static ActionListener getFindAction(JTextField txt) {
		return (ActionListener) txt.getClientProperty(FIND_ACTION_PROPERTY);
	}

	public static void setCancelAction(JTextField txt, ActionListener cancelAction) {
		txt.putClientProperty(CANCEL_ACTION_PROPERTY, cancelAction);
	}

	public static ActionListener getCancelAction(JTextField txt) {
		return (ActionListener) txt.getClientProperty(CANCEL_ACTION_PROPERTY);
	}

	private static final SearchFieldUIChangeHandler uiChangeHandler = new SearchFieldUIChangeHandler();

	private static final class SearchFieldUIChangeHandler extends AbstractUIChangeHandler {
		@Override
        public void propertyChange(PropertyChangeEvent evt) {
			JTextField txt = (JTextField) evt.getSource();
			// Leopard hack to make appear correctly in search variant when
			// changing LnF.
			setSearchField(txt, isSearchField(txt));
		}
	}
}
