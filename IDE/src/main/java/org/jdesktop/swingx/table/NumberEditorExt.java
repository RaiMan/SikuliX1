/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.table;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.text.StrictNumberFormatter;
import org.jdesktop.swingx.text.NumberFormatExt;

/**
 *
 * Issue #393-swingx: localized NumberEditor. Added feature to use StrictNumberFormatter.
 *
 * @author Noel Grandin
 * @author Jeanette Winzenburg
 */
public class NumberEditorExt extends DefaultCellEditor {

    private static Class<?>[] argTypes = new Class[]{String.class};
    java.lang.reflect.Constructor<?> constructor;
    private boolean useStrictFormatter;

    /**
     * Instantiates an editor with default NumberFormat and default NumberFormatter.
     */
    public NumberEditorExt() {
        this(null);
    }

    /**
     * Instantiates an editor with the given NumberFormat and default NumberFormatter.
     *
     * @param format the NumberFormat to use for conversion, may be null to indicate
     *    usage of default NumberFormat.
     */
    public NumberEditorExt(NumberFormat format) {
        this(format, false);
    }

    /**
     * Instantiates an editor with default NumberFormat and NumberFormatter depending
     * on useStrictFormatter.
     *
     * @param useStrictFormatter if true, uses a StrictNumberFormatter, else uses
     *    default NumberFormatter
     */
    public NumberEditorExt(boolean useStrictFormatter) {
        this(null, useStrictFormatter);
    }

    /**
     * Instantiates an editor with the given NumberFormat and NumberFormatter depending on
     * useStrictFormatter.
     *
     * @param format the NumberFormat to use for conversion, may be null to indicate
     *    usage of default NumberFormat
     * @param useStrictFormatter if true, uses a StrictNumberFormatter, else uses
     *    default NumberFormatter
     */
    public NumberEditorExt(NumberFormat format, boolean useStrictFormatter) {

        super(useStrictFormatter ? createFormattedTextFieldX(format) : createFormattedTextField(format));
        this.useStrictFormatter = useStrictFormatter;
        final JFormattedTextField textField = getComponent();

        textField.setName("Table.editor");
        textField.setHorizontalAlignment(JTextField.RIGHT);

        // remove action listener added in DefaultCellEditor
        textField.removeActionListener(delegate);
        // replace the delegate created in DefaultCellEditor
        delegate = new EditorDelegate() {
                @Override
                public void setValue(Object value) {
                    getComponent().setValue(value);
                }

                @Override
                public Object getCellEditorValue() {
                    try {
                        getComponent().commitEdit();
                        return getComponent().getValue();
                    } catch (ParseException ex) {
                        return null;
                    }
                }
        };
        textField.addActionListener(delegate);
    }

    @Override
    public boolean stopCellEditing() {
        if (!isValid()) return false;
        return super.stopCellEditing();
    }

    /**
     * Returns a boolean indicating whether the current text is valid for
     * instantiating the expected Number type.
     *
     * @return true if text is valid, false otherwise.
     */
    protected boolean isValid() {
        if (!getComponent().isEditValid()) return false;
        try {
            if (!hasStrictFormatter())
                getNumber();
            return true;
        } catch (Exception ex) {

        }
        return false;
    }

    /**
     * Returns the editor value as number. May fail for a variety of reasons,
     * as it forces parsing of the current text as well as reflective construction
     * of the target type.
     *
     * @return the editor value or null
     * @throws Exception if creation of the expected type fails in some way.
     */
    protected Number getNumber() throws Exception {
        Number number = (Number) super.getCellEditorValue();
        if (number==null) return null;
        return hasStrictFormatter() ? number :
            (Number) constructor.newInstance(new Object[]{number.toString()});
    }

    /**
     * @return
     */
    protected boolean hasStrictFormatter() {
        return useStrictFormatter;
    }

    /** Override and set the border back to normal in case there was an error previously */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                             boolean isSelected,
                                             int row, int column) {
        ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
        try {
            final Class<?> type = table.getColumnClass(column);
            if (hasStrictFormatter()) {
                // delegate to formatter which decides at parsing time
                // then either handles or throws
                ((NumberFormatter) getComponent().getFormatter()).setValueClass(type);
            } else {
                // Assume that the Number object we are dealing with has a constructor which takes
                // a single string parameter.
                if (!Number.class.isAssignableFrom(type)) {
                    throw new IllegalStateException("NumberEditor can only handle subclasses of java.lang.Number");
                }
                constructor = type.getConstructor(argTypes);
            }
            // JW: in strict mode this may fail in setting the value in the formatter
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        } catch (Exception ex) {
            // PENDING JW: super generic editor swallows all failures and returns null
            // should we do so as well?
            throw new IllegalStateException("value/type not compatible with Number", ex);
        }
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to instantiate a Number of the expected type. Note that this
     * may throw a IllegalStateException if invoked without querying
     * for a valid value with stopCellEditing. This should not happen during
     * normal usage.
     *
     * @throws IllegalStateException if current value invalid
     *
     */
    @Override
    public Number getCellEditorValue() throws IllegalStateException {
        try {
            return getNumber();
        } catch (Exception ex) {
            throw new IllegalStateException("Number conversion not possible from " +
            		"current string " + getComponent().getText());
        }
    }

    /**
     * {@inheritDoc} <p>
     *
     * Convenience override with type cast.
     */
    @Override
    public JFormattedTextField getComponent() {
        return (JFormattedTextField) super.getComponent();
    }

    /**
     * Creates and returns a JFormattedTextField configured with SwingX extended
     * NumberFormat and StrictNumberFormatter. This method is called if
     * the constructor parameter useStrictFormatter is true.
     *
     * Use a static method so that we can do some stuff before calling the
     * superclass.
     */
    private static JFormattedTextField createFormattedTextFieldX(
            NumberFormat format) {
       StrictNumberFormatter formatter = new StrictNumberFormatter(
                new NumberFormatExt(format));
        final JFormattedTextField textField = new JFormattedTextField(
                formatter);
        /*
         * FIXME: I am sure there is a better way to do this, but I don't know
         * what it is. JTable sets up a binding for the ESCAPE key, but
         * JFormattedTextField overrides that binding with it's own. Remove the
         * JFormattedTextField binding.
         */
        InputMap map = textField.getInputMap();
        map.put(KeyStroke.getKeyStroke("ESCAPE"), "none");
//        while (map != null) {
//            map.remove(KeyStroke.getKeyStroke("pressed ESCAPE"));
//            map = map.getParent();
//        }
        /*
         * Set an input verifier to prevent the cell losing focus when the value
         * is invalid
         */
        textField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JFormattedTextField ftf = (JFormattedTextField) input;
                return ftf.isEditValid();
            }
        });
        /*
         * The formatted text field will not call stopCellEditing() until the
         * value is valid. So do the red border thing here.
         */
        textField.addPropertyChangeListener("editValid",
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() == Boolean.TRUE) {
                    ((JFormattedTextField) evt.getSource())
                    .setBorder(new LineBorder(Color.black));
                } else {
                    ((JFormattedTextField) evt.getSource())
                    .setBorder(new LineBorder(Color.red));
                }
            }
        });
        return textField;
    }

    /**
     * Creates and returns a JFormattedTextField configured with defaults. This
     * method is called if the contructor useStrictFormatter is false.<p>
     *
     * Use a static method so that we can do some stuff before calling the
     * superclass.
     */
    private static JFormattedTextField createFormattedTextField(
            NumberFormat formatter) {
        final JFormattedTextField textField = new JFormattedTextField(
                new NumberFormatExt(formatter));
        /*
         * FIXME: I am sure there is a better way to do this, but I don't know
         * what it is. JTable sets up a binding for the ESCAPE key, but
         * JFormattedTextField overrides that binding with it's own. Remove the
         * JFormattedTextField binding.
         */
        InputMap map = textField.getInputMap();
        map.put(KeyStroke.getKeyStroke("ESCAPE"), "none");
//        while (map != null) {
//            map.remove(KeyStroke.getKeyStroke("pressed ESCAPE"));
//            map = map.getParent();
//        }
        /*
         * Set an input verifier to prevent the cell losing focus when the value
         * is invalid
         */
        textField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JFormattedTextField ftf = (JFormattedTextField) input;
                return ftf.isEditValid();
            }
        });
        /*
         * The formatted text field will not call stopCellEditing() until the
         * value is valid. So do the red border thing here.
         */
        textField.addPropertyChangeListener("editValid",
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getNewValue() == Boolean.TRUE) {
                            ((JFormattedTextField) evt.getSource())
                                    .setBorder(new LineBorder(Color.black));
                        } else {
                            ((JFormattedTextField) evt.getSource())
                                    .setBorder(new LineBorder(Color.red));
                        }
                    }
                });
        return textField;
    }
}
