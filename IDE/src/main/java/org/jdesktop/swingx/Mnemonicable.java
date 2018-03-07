/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

/**
 * An interface that describes an object that is capable of being accessed/used via a mnemonic
 * keystroke.
 *
 * @author Karl George Schaefer
 */
// TODO this describes the mnemonic feature but not what is used,
// ie. what String returning method is called
interface Mnemonicable {
    /**
     * Returns the keyboard mnemonic for this component.
     *
     * @return the keyboard mnemonic
     */
    int getMnemonic();

    /**
     * Sets the keyboard mnemonic on this component. The mnemonic is the key
     * which when combined with the look and feel's mouseless modifier (usually
     * Alt) will activate this component.
     * <p>
     * A mnemonic must correspond to a single key on the keyboard and should be
     * specified using one of the <code>VK_XXX</code> keycodes defined in
     * <code>java.awt.event.KeyEvent</code>. Mnemonics are case-insensitive,
     * therefore a key event with the corresponding keycode would cause the
     * button to be activated whether or not the Shift modifier was pressed.
     *
     * @param mnemonic
     *            the key code which represents the mnemonic
     * @see java.awt.event.KeyEvent
     * @see #setDisplayedMnemonicIndex
     *
     * @beaninfo bound: true attribute: visualUpdate true description: the
     *           keyboard character mnemonic
     */
    void setMnemonic(int mnemonic);

    /**
     * Returns the character, as an index, that the look and feel should
     * provide decoration for as representing the mnemonic character.
     *
     * @since 1.4
     * @return index representing mnemonic character
     * @see #setDisplayedMnemonicIndex
     */
    int getDisplayedMnemonicIndex();

    /**
     * Provides a hint to the look and feel as to which character in the
     * text should be decorated to represent the mnemonic. Not all look and
     * feels may support this. A value of -1 indicates either there is no
     * mnemonic, the mnemonic character is not contained in the string, or
     * the developer does not wish the mnemonic to be displayed.
     * <p>
     * The value of this is updated as the properties relating to the
     * mnemonic change (such as the mnemonic itself, the text...).
     * You should only ever have to call this if
     * you do not wish the default character to be underlined. For example, if
     * the text was 'Save As', with a mnemonic of 'a', and you wanted the 'A'
     * to be decorated, as 'Save <u>A</u>s', you would have to invoke
     * <code>setDisplayedMnemonicIndex(5)</code> after invoking
     * <code>setMnemonic(KeyEvent.VK_A)</code>.
     *
     * @since 1.4
     * @param index Index into the String to underline
     * @exception IllegalArgumentException will be thrown if <code>index</code>
     *            is &gt;= length of the text, or &lt; -1
     * @see #getDisplayedMnemonicIndex
     *
     * @beaninfo
     *        bound: true
     *    attribute: visualUpdate true
     *  description: the index into the String to draw the keyboard character
     *               mnemonic at
     */
    void setDisplayedMnemonicIndex(int index) throws IllegalArgumentException;
}
