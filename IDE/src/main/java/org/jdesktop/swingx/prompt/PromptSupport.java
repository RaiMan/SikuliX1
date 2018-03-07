/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.prompt;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXFormattedTextField;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.Painters;
import org.jdesktop.swingx.plaf.PromptTextUI;
import org.jdesktop.swingx.plaf.TextUIWrapper;

/**
 * <p>
 * Sets prompt text, foreground, background and {@link FocusBehavior} properties
 * on a JTextComponent by calling
 * {@link JTextComponent#putClientProperty(Object, Object)}. These properties
 * are used by {@link PromptTextUI} instances to render the prompt of a text
 * component.
 * </p>
 *
 * <p>
 * This class is used by {@link JXTextField}, {@link JXFormattedTextField} and
 * {@link JXTextArea} to get and set prompt properties. {@link PromptTextUI}
 * retrieves these properties using PromptSupport.
 * </p>
 *
 * @see JXTextField
 * @see JXFormattedTextField
 * @see JXTextArea
 * @see PromptTextUI
 *
 * @author Peter Weishapl <petw@gmx.net>
 * @author Karl Schaefer
 */
public final class PromptSupport {
	/**
	 * The prompt text property.
	 */
	public static final String PROMPT = "promptText";

	/**
	 * The color of the prompt text property.
	 */
	public static final String FOREGROUND = "promptForeground";

	/**
	 * The prompt background property.
	 */
	public static final String BACKGROUND = "promptBackground";

	/**
	 * The prompt background property.
	 */
	public static final String BACKGROUND_PAINTER = "promptBackgroundPainter";

	/**
	 * The focus behavior property.
	 */
	public static final String FOCUS_BEHAVIOR = "focusBehavior";

	/**
	 * The font style property, if different from the components font.
	 */
	public static final String FONT_STYLE = "promptFontStyle";

	/**
	 * <p>
	 * Determines how the {@link JTextComponent} is rendered when focused and no
	 * text is present.
	 * </p>
	 */
	public static enum FocusBehavior {
		/**
		 * Keep the prompt text visible.
		 */
		SHOW_PROMPT,
		/**
		 * Highlight the prompt text as it would be selected.
		 */
		HIGHLIGHT_PROMPT,
		/**
		 * Hide the prompt text.
		 */
		HIDE_PROMPT
	};

	private PromptSupport() {
	    //prevent instantiation
	}

	/**
	 * <p>
	 * Convenience method to set the <code>promptText</code> and
	 * <code>promptTextColor</code> on a {@link JTextComponent}.
	 * </p>
	 * <p>
	 * If <code>stayOnUIChange</code> is true, The prompt support will stay
	 * installed, even when the text components UI changes. See
	 * {@link #install(JTextComponent, boolean)}.
	 * </p>
	 *
	 * @param promptText
	 * @param promptForeground
	 * @param promptBackground
	 * @param textComponent
	 */
	public static void init(String promptText, Color promptForeground, Color promptBackground,
			final JTextComponent textComponent) {
		if (promptText != null && promptText.length() > 0) {
			setPrompt(promptText, textComponent);
		}
		if (promptForeground != null) {
			setForeground(promptForeground, textComponent);
		}
		if (promptBackground != null) {
			setBackground(promptBackground, textComponent);
		}
	}

	/**
	 * Get the {@link FocusBehavior} of <code>textComponent</code>.
	 *
	 * @param textComponent
	 * @return the {@link FocusBehavior} or {@link FocusBehavior#HIDE_PROMPT} if
	 *         none is set
	 */
	public static FocusBehavior getFocusBehavior(JTextComponent textComponent) {
		FocusBehavior fb = (FocusBehavior) textComponent.getClientProperty(FOCUS_BEHAVIOR);
		if (fb == null) {
			fb = FocusBehavior.HIDE_PROMPT;
		}
		return fb;
	}

	/**
	 * Sets the {@link FocusBehavior} on <code>textComponent</code> and
	 * repaints the component to reflect the changes, if it is the focus owner.
	 *
	 * @param focusBehavior
	 * @param textComponent
	 */
	public static void setFocusBehavior(FocusBehavior focusBehavior, JTextComponent textComponent) {
		textComponent.putClientProperty(FOCUS_BEHAVIOR, focusBehavior);
		if (textComponent.isFocusOwner()) {
			textComponent.repaint();
		}
	}

	/**
	 * Get the prompt text of <code>textComponent</code>.
	 *
	 * @param textComponent
	 * @return the prompt text
	 */
	public static String getPrompt(JTextComponent textComponent) {
		return (String) textComponent.getClientProperty(PROMPT);
	}

	/**
	 * <p>
	 * Sets the prompt text on <code>textComponent</code>. Also sets the
	 * tooltip text to the prompt text if <code>textComponent</code> has no
	 * tooltip text or the current tooltip text is the same as the current
	 * prompt text.
	 * </p>
	 * <p>
	 * Calls {@link #install(JTextComponent)} to ensure that the
	 * <code>textComponent</code>s UI is wrapped by the appropriate
	 * {@link PromptTextUI}.
	 * </p>
	 *
	 * @param promptText
	 * @param textComponent
	 */
	public static void setPrompt(String promptText, JTextComponent textComponent) {
		TextUIWrapper.getDefaultWrapper().install(textComponent, true);

		// display prompt as tooltip by default
		if (textComponent.getToolTipText() == null || textComponent.getToolTipText().equals(getPrompt(textComponent))) {
			textComponent.setToolTipText(promptText);
		}

		textComponent.putClientProperty(PROMPT, promptText);
		textComponent.repaint();
	}

	/**
	 * Get the foreground color of the prompt text. If no color has been set,
	 * the <code>textComponent</code>s disabled text color will be returned.
	 *
	 * @param textComponent
	 * @return the color of the prompt text or
	 *         {@link JTextComponent#getDisabledTextColor()} if none is set
	 */
	public static Color getForeground(JTextComponent textComponent) {
		if (textComponent.getClientProperty(FOREGROUND) == null) {
			return textComponent.getDisabledTextColor();
		}
		return (Color) textComponent.getClientProperty(FOREGROUND);
	}

	/**
	 * Sets the foreground color of the prompt on <code>textComponent</code>
	 * and repaints the component to reflect the changes. This color will be
	 * used when no text is present.
	 *
	 * @param promptTextColor
	 * @param textComponent
	 */
	public static void setForeground(Color promptTextColor, JTextComponent textComponent) {
		textComponent.putClientProperty(FOREGROUND, promptTextColor);
		textComponent.repaint();
	}

	/**
	 * Get the background color of the <code>textComponent</code>, when no
	 * text is present. If no color has been set, the <code>textComponent</code>s
	 * background color color will be returned.
	 *
	 * @param textComponent
	 * @return the the background color of the text component, when no text is
	 *         present
	 */
	public static Color getBackground(JTextComponent textComponent) {
		if (textComponent.getClientProperty(BACKGROUND) == null) {
			return textComponent.getBackground();
		}
		return (Color) textComponent.getClientProperty(BACKGROUND);
	}

	/**
	 * <p>
	 * Sets the prompts background color on <code>textComponent</code> and
	 * repaints the component to reflect the changes. This background color will
	 * only be used when no text is present.
	 * </p>
	 * <p>
	 * Calls {@link #install(JTextComponent)} to ensure that the
	 * <code>textComponent</code>s UI is wrapped by the appropriate
	 * {@link PromptTextUI}.
	 * </p>
	 *
	 * @param background
	 * @param textComponent
	 */
	public static void setBackground(Color background, JTextComponent textComponent) {
		TextUIWrapper.getDefaultWrapper().install(textComponent, true);

		textComponent.putClientProperty(BACKGROUND, background);
		textComponent.repaint();
	}

	/**
	 * Get the background painter of the <code>textComponent</code>, when no
	 * text is present. If no painter has been set, then {@code null} will be returned.
	 *
	 * @param textComponent
	 * @return the background painter of the text component
	 */
	@SuppressWarnings("unchecked")
    public static <T extends JTextComponent> Painter<? super T> getBackgroundPainter(T textComponent) {
	    Painter<? super T> painter = (Painter<? super T>) textComponent.getClientProperty(BACKGROUND_PAINTER);

	    if (painter == null) {
	        painter = Painters.EMPTY_PAINTER;
	    }

	    return painter;
	}

	/**
	 * <p>
	 * Sets the prompts background painter on <code>textComponent</code> and
	 * repaints the component to reflect the changes. This background painter will
	 * only be used when no text is present.
	 * </p>
	 * <p>
	 * Calls {@link #install(JTextComponent)} to ensure that the
	 * <code>textComponent</code>s UI is wrapped by the appropriate
	 * {@link PromptTextUI}.
	 * </p>
	 *
	 * @param background
	 * @param textComponent
	 */
	public static <T extends JTextComponent> void setBackgroundPainter(Painter<? super T> background, T textComponent) {
	    TextUIWrapper.getDefaultWrapper().install(textComponent, true);

	    textComponent.putClientProperty(BACKGROUND_PAINTER, background);
	    textComponent.repaint();
	}

	/**
	 * <p>
	 * Set the style of the prompt font, if different from the
	 * <code>textComponent</code>s font.
	 * </p>
	 * <p>
	 * Allowed values are {@link Font#PLAIN}, {@link Font#ITALIC},
	 * {@link Font#BOLD}, a combination of {@link Font#BOLD} and
	 * {@link Font#ITALIC} or <code>null</code> if the prompt font should be
	 * the same as the <code>textComponent</code>s font.
	 * </p>
	 *
	 * @param fontStyle
	 * @param textComponent
	 */
	public static void setFontStyle(Integer fontStyle, JTextComponent textComponent) {
		textComponent.putClientProperty(FONT_STYLE, fontStyle);
		textComponent.revalidate();
		textComponent.repaint();
	}

	/**
	 * Returns the font style of the prompt text, or <code>null</code> if the
	 * prompt's font style should not differ from the <code>textComponent</code>s
	 * font.
	 *
	 * @param textComponent
	 * @return font style of the prompt text
	 */
	public static Integer getFontStyle(JTextComponent textComponent) {
		return (Integer) textComponent.getClientProperty(FONT_STYLE);
	}
}
