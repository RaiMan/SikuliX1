/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.color.GradientPreviewPanel;
import org.jdesktop.swingx.color.GradientThumbRenderer;
import org.jdesktop.swingx.color.GradientTrackRenderer;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbListener;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * <p>A specialized JXPanel that allows the user to construct and choose a Gradient.
 * The returned values will be one of: LinearGradientPaint or RadialGradientPaint.</p>
 *
 * <p><b>Dependency</b>: Because this class relies on LinearGradientPaint and
 * RadialGradientPaint, it requires the optional MultipleGradientPaint.jar</p>
 *
 * @author joshy
 */
@JavaBean
public class JXGradientChooser extends JXPanel {
    private enum GradientStyle { Linear, Radial }

    /**
     * The multi-thumb slider to use for the gradient stops
     */
    private JXMultiThumbSlider<Color> slider;
    private JButton deleteThumbButton;
    private JButton addThumbButton;

    private JTextField colorField;
    private JXColorSelectionButton changeColorButton;
    private JSpinner colorLocationSpinner;
    private JSpinner alphaSpinner;
    private JSlider alphaSlider;

    private JComboBox styleCombo;
    private GradientPreviewPanel gradientPreview;

    private JRadioButton noCycleRadio;
    private JRadioButton reflectedRadio;
    private JRadioButton repeatedRadio;
    private JCheckBox reversedCheck;
    private MultipleGradientPaint gradient;

    /**
     * Creates new JXGradientChooser
     */
    public JXGradientChooser() {
        initComponents2();
    }

    /**
     * Returns the MultipleGradientPaint currently choosen by the user.
     * @return the currently selected gradient
     */
    public MultipleGradientPaint getGradient() {
        return gradient;
    }

    private boolean thumbsMoving = false;
    private Logger log = Logger.getLogger(JXGradientChooser.class.getName());

    /**
     * Sets the gradient within this panel to the new gradient. This will delete
     * the old gradient all of it's settings, resetting the slider, gradient
     * type selection, and other gradient configuration options to match the
     * new gradient.
     *
     * @param mgrad The desired gradient.
     */
    public void setGradient(MultipleGradientPaint mgrad) {
        if(gradient == mgrad) {
            return;
        }
        float[] fracts = mgrad.getFractions();
        Color[] colors = mgrad.getColors();

        if(!thumbsMoving) {
            // update the slider properly
            if(slider.getModel().getThumbCount() !=
                    mgrad.getColors().length) {
                // removing all thumbs;
                while(slider.getModel().getThumbCount() > 0) {
                    slider.getModel().removeThumb(0);
                }
                // add them back
                for(int i=0; i<fracts.length; i++) {
                    slider.getModel().addThumb(fracts[i],colors[i]);
                }
            } else {
                for(int i=0; i<fracts.length; i++) {
                    slider.getModel().getThumbAt(i).setObject(colors[i]);
                    slider.getModel().getThumbAt(i).setPosition(fracts[i]);
                }
            }
        } else {
            log.fine("not updating because it's moving");
        }
        if(mgrad instanceof RadialGradientPaint) {
            if(styleCombo.getSelectedItem() != GradientStyle.Radial) {
                styleCombo.setSelectedItem(GradientStyle.Radial);
            }
        } else {
            if(styleCombo.getSelectedItem() != GradientStyle.Linear) {
                styleCombo.setSelectedItem(GradientStyle.Linear);
            }
        }

        if(mgrad.getCycleMethod() == MultipleGradientPaint.CycleMethod.REFLECT) {
            this.reflectedRadio.setSelected(true);
            gradientPreview.setReflected(true);
        }
        if(mgrad.getCycleMethod() == MultipleGradientPaint.CycleMethod.REPEAT) {
            this.repeatedRadio.setSelected(true);
            gradientPreview.setRepeated(true);
        }
        gradientPreview.setGradient(mgrad);
        //reflectedRadio.setSelected()
        MultipleGradientPaint old = this.getGradient();
        gradient = mgrad;
        firePropertyChange("gradient",old,getGradient());
        repaint();
    }

    private void recalcGradientFromStops() {
        setGradient(gradientPreview.getGradient());
    }

    private void updateFromStop(Thumb<Color> thumb) {
        if(thumb == null) {
            updateFromStop(-1,-1,Color.black);
        } else {
            updateFromStop(1,thumb.getPosition(),thumb.getObject());
        }
    }

    private void updateFromStop(int thumb, float position, Color color) {
        log.fine("updating: " + thumb + " " + position + " " + color);
        if(thumb == -1) {
            colorLocationSpinner.setEnabled(false);
            alphaSpinner.setEnabled(false);
            alphaSlider.setEnabled(false);
            colorField.setEnabled(false);
            changeColorButton.setEnabled(false);
            changeColorButton.setBackground(Color.black);
            deleteThumbButton.setEnabled(false);
        } else {
            colorLocationSpinner.setEnabled(true);
            alphaSpinner.setEnabled(true);
            alphaSlider.setEnabled(true);
            colorField.setEnabled(true);
            changeColorButton.setEnabled(true);
            colorLocationSpinner.setValue((int)(100*position));
            colorField.setText(Integer.toHexString(color.getRGB()).substring(2));
            alphaSpinner.setValue(color.getAlpha()*100/255);
            alphaSlider.setValue(color.getAlpha()*100/255);
            changeColorButton.setBackground(color);
            deleteThumbButton.setEnabled(true);
        }
        updateDeleteButtons();
        recalcGradientFromStops();
    }

    private void updateDeleteButtons() {
        if(slider.getModel().getThumbCount() <= 2) {
            deleteThumbButton.setEnabled(false);
        }
    }

    private void updateGradientProperty() {
        firePropertyChange("gradient",null,getGradient());
        gradientPreview.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */

    private JPanel topPanel, previewPanel;
    private void initComponents() {
        // declarations for anonymous components
        JPanel jPanel1, jPanel2, jPanel3, jPanel4;
        JLabel jLabel1, jLabel5, jLabel2, jLabel6, jLabel4, jLabel7, jLabel8, jLabel9;
        ButtonGroup typeGroup;
        // pre-init stuff
        slider = new JXMultiThumbSlider<Color>();
        gradientPreview = new GradientPreviewPanel();
        gradientPreview.setMultiThumbModel(slider.getModel());

        java.awt.GridBagConstraints gridBagConstraints;

        typeGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        colorField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        colorLocationSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        alphaSpinner = new javax.swing.JSpinner();
        changeColorButton = new JXColorSelectionButton();
        alphaSlider = new javax.swing.JSlider();
        //slider = new javax.swing.JSlider();
        jPanel4 = new javax.swing.JPanel();
        addThumbButton = new javax.swing.JButton();
        deleteThumbButton = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        styleCombo = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        noCycleRadio = new javax.swing.JRadioButton();
        reflectedRadio = new javax.swing.JRadioButton();
        repeatedRadio = new javax.swing.JRadioButton();
        reversedCheck = new javax.swing.JCheckBox();
        //gradientPreview = new javax.swing.JPanel();

        //setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        topPanel.setLayout(new java.awt.GridBagLayout());

        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Gradient"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Color:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel5.setText("#");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        jPanel2.add(jLabel5, gridBagConstraints);

        colorField.setColumns(6);
        colorField.setEnabled(false);
        colorField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(colorField, gridBagConstraints);

        jLabel2.setText("Location:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel2, gridBagConstraints);

        jLabel6.setText("%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        jPanel2.add(jLabel6, gridBagConstraints);

        colorLocationSpinner.setEnabled(false);
        colorLocationSpinner.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(colorLocationSpinner, gridBagConstraints);

        jLabel4.setText("Opacity:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel4, gridBagConstraints);

        jLabel7.setText("%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel2.add(jLabel7, gridBagConstraints);

        alphaSpinner.setEnabled(false);
        alphaSpinner.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(alphaSpinner, gridBagConstraints);

        changeColorButton.setText("00");
        changeColorButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel2.add(changeColorButton, gridBagConstraints);

        alphaSlider.setEnabled(false);
        alphaSlider.setPreferredSize(new java.awt.Dimension(20, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(alphaSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topPanel.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        topPanel.add(slider, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        addThumbButton.setText("Add");
        jPanel4.add(addThumbButton);

        deleteThumbButton.setText("Delete");
        jPanel4.add(deleteThumbButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        topPanel.add(jPanel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(topPanel, gridBagConstraints);

        previewPanel.setLayout(new java.awt.GridBagLayout());

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel8.setText("Style:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel8, gridBagConstraints);

        styleCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Linear", "Radial" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(styleCombo, gridBagConstraints);

        jLabel9.setText("Type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel9, gridBagConstraints);

        typeGroup.add(noCycleRadio);
        noCycleRadio.setSelected(true);
        noCycleRadio.setText("None");
        noCycleRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noCycleRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(noCycleRadio, gridBagConstraints);

        typeGroup.add(reflectedRadio);
        reflectedRadio.setText("Reflect");
        reflectedRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reflectedRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(reflectedRadio, gridBagConstraints);

        typeGroup.add(repeatedRadio);
        repeatedRadio.setText("Repeat");
        repeatedRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        repeatedRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(repeatedRadio, gridBagConstraints);

        reversedCheck.setText("Reverse");
        reversedCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reversedCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(reversedCheck, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        previewPanel.add(jPanel3, gridBagConstraints);

        gradientPreview.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gradientPreview.setPreferredSize(new java.awt.Dimension(130, 130));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 10.0;
        previewPanel.add(gradientPreview, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(previewPanel, gridBagConstraints);

    }// </editor-fold>
    private void initComponents2() {
        this.initComponents();
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(previewPanel, BorderLayout.CENTER);

        // do event handling stuff
        //create the actions and load them in the action map
        AddThumbAction addThumbAction = new AddThumbAction();
        DeleteThumbAction deleteThumbAction = new DeleteThumbAction();
        deleteThumbAction.setEnabled(false); //disabled to begin with
        //TODO Add to the action map with proper keys, etc
        ActionMap actions = getActionMap();
        actions.put("add-thumb", addThumbAction);
        actions.put("delete-thumb", deleteThumbAction);
        //actions.put("change-color", changeColorAction);
        addThumbButton.setAction(addThumbAction);
        deleteThumbButton.setAction(deleteThumbAction);
        changeColorButton.addPropertyChangeListener("background", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                selectColorForThumb();
                updateGradientProperty();
            }
        });
        colorLocationSpinner.addChangeListener(new ChangeLocationListener());
        ChangeAlphaListener changeAlphaListener = new ChangeAlphaListener();
        alphaSpinner.addChangeListener(changeAlphaListener);
        alphaSlider.addChangeListener(changeAlphaListener);
        RepaintOnEventListener repaintListener = new RepaintOnEventListener();
        styleCombo.addItemListener(repaintListener);
        styleCombo.setModel(new DefaultComboBoxModel(GradientStyle.values()));
        noCycleRadio.addActionListener(repaintListener);
        reflectedRadio.addActionListener(repaintListener);
        repeatedRadio.addActionListener(repaintListener);
        reversedCheck.addActionListener(repaintListener);
        gradientPreview.picker = this; //wow, nasty

        ///To still refactor below::
        SpinnerNumberModel alpha_model = new SpinnerNumberModel(100,0,100,1);
        alphaSpinner.setModel(alpha_model);
        SpinnerNumberModel location_model = new SpinnerNumberModel(100,0,100,1);
        colorLocationSpinner.setModel(location_model);

        slider.setOpaque(false);
        slider.setPreferredSize(new Dimension(100,35));
        slider.getModel().setMinimumValue(0f);
        slider.getModel().setMaximumValue(1.0f);

        slider.getModel().addThumb(0,Color.black);
        slider.getModel().addThumb(0.5f,Color.red);
        slider.getModel().addThumb(1.0f,Color.white);

        slider.setThumbRenderer(new GradientThumbRenderer());
        slider.setTrackRenderer(new GradientTrackRenderer());
        slider.addMultiThumbListener(new StopListener());

        // called when the gradient property of the preview pane changes
        gradientPreview.addPropertyChangeListener("gradient", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                recalcGradientFromStops();
            }
        });

        recalcGradientFromStops();

    }

    // called whenever the color location spinner is changed
    private final class ChangeLocationListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent evt) {
            if(slider.getSelectedIndex() >= 0) {
                Thumb<Color> thumb = slider.getModel().getThumbAt(slider.getSelectedIndex());
                thumb.setPosition((Integer)colorLocationSpinner.getValue()/100f);
                updateFromStop(thumb);
                updateGradientProperty();
            }
        }
    }

    // called when the alpha slider moves
    private final class ChangeAlphaListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            if(slider.getSelectedIndex() >= 0 && !thumbsMoving) {
                // get the selected thumb
                Thumb<Color> thumb = slider.getModel().getThumbAt(slider.getSelectedIndex());
                // get the new alpha value
                int alpha = changeEvent.getSource() == alphaSpinner ?
                    (Integer)alphaSpinner.getValue()
                    : alphaSlider.getValue();

                // calc new color and set it on thumb
                Color col = thumb.getObject();
                col = PaintUtils.setAlpha(col, alpha*255/100);
                thumb.setObject(col);

                // set the new alpha value on the other alpha control
                if (changeEvent.getSource() == alphaSpinner) {
                    alphaSlider.setValue(alpha);
                } else {
                    alphaSpinner.setValue(alpha);
                }

                recalcGradientFromStops();
            }
        }
    }

    private final class AddThumbAction extends AbstractActionExt {
        public AddThumbAction() {
            super("Add");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            float pos = 0.2f;
            Color color = Color.black;
            int num = slider.getModel().addThumb(pos,color);
            log.fine("new number = " + num);
            /*
            for (int i = 0; i < slider.getModel().getThumbCount(); i++) {
                float pos2 = slider.getModel().getThumbAt(i).getPosition();
                if (pos2 < pos) {
                    continue;
                }
                slider.getModel().insertThumb(pos, color, i);
                updateFromStop(i,pos,color);
                break;
            }
             */

        }
    }

    private final class DeleteThumbAction extends AbstractActionExt {
        public DeleteThumbAction() {
            super("Delete");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int index = slider.getSelectedIndex();
            if (index >= 0) {
                slider.getModel().removeThumb(index);
                updateFromStop(-1,-1,null);
            }
        }
    }

    private class StopListener implements ThumbListener {

        public StopListener() {
            super();
        }

        @Override
        public void thumbMoved(int thumb, float pos) {
            log.fine("moved: " + thumb + " " + pos);
            Color color = slider.getModel().getThumbAt(thumb).getObject();
            thumbsMoving = true;
            updateFromStop(thumb,pos,color);
            updateDeleteButtons();
            thumbsMoving = false;

        }

        @Override
        public void thumbSelected(int thumb) {

            if(thumb == -1) {
                updateFromStop(-1,-1,Color.black);
                return;
            }
            thumbsMoving = true;
            float pos = slider.getModel().getThumbAt(thumb).getPosition();
            Color color = slider.getModel().getThumbAt(thumb).getObject();
            log.fine("selected = " + thumb + " " + pos + " " + color);
            updateFromStop(thumb,pos,color);
            updateDeleteButtons();
            slider.repaint();
            thumbsMoving = false;

        }

        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getClickCount() > 1) {
                selectColorForThumb();
            }
        }
    }

    private final class RepaintOnEventListener implements ActionListener, ItemListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gradientPreview.setReflected(reflectedRadio.isSelected());
            gradientPreview.setReversed(reversedCheck.isSelected());
            gradientPreview.setRepeated(repeatedRadio.isSelected());
            //updateGradientProperty();
            recalcGradientFromStops();
            gradientPreview.repaint();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if(styleCombo.getSelectedItem() == GradientStyle.Radial) {
                gradientPreview.setRadial(true);
            } else {
                gradientPreview.setRadial(false);
            }
            recalcGradientFromStops();
        }
    }

    private void selectColorForThumb() {
        int index = slider.getSelectedIndex();
        if (index >= 0) {
            Color color = changeColorButton.getBackground();
            slider.getModel().getThumbAt(index).setObject(color);
            updateFromStop(index, slider.getModel().getThumbAt(index).getPosition(), color);
        }
    }

    /**
     * This static utility method <b>cannot</b> be called from the
     * ETD, or your application will lock up. Call it from a separate
     * thread or create a new Thread with a Runnable.
     * @param comp The component to use when finding a top level window or frame for
     * the dialog.
     * @param title The desired title of the gradient chooser dialog.
     * @param mgrad The gradient to initialize the chooser too.
     * @return The gradient the user chose.
     */
    public static MultipleGradientPaint showDialog(Component comp, String title, MultipleGradientPaint mgrad) {
        Component root = SwingUtilities.getRoot(comp);
        final JDialog dialog = new JDialog((JFrame)root,title,true);
        final JXGradientChooser picker = new JXGradientChooser();
        if(mgrad != null) {
            picker.setGradient(mgrad);
        }
        dialog.add(picker);

        JPanel panel = new JPanel();
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
            }
        });
        JButton okay = new JButton("Ok");
        okay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
            }
        });
        okay.setDefaultCapable(true);

        GridLayout gl = new GridLayout();
        gl.setHgap(2);
        panel.setLayout(gl);
        panel.add(cancel);
        panel.add(okay);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        p2.add(panel,gbc);
        dialog.add(p2,"South");

        dialog.getRootPane().setDefaultButton(okay);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setVisible(true);

        return picker.getGradient();
    }

    /**
     * Creates a string representation of a {@code MultipleGradientPaint}. This
     * string is used for debugging purposes. Its contents cannot be guaranteed
     * between releases.
     *
     * @param paint
     *                the {@code paint} to create a string for
     * @return a string representing the supplied {@code paint}
     */
    public static String toString(MultipleGradientPaint paint) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(paint.getClass().getName());
        Color[] colors = paint.getColors();
        float[] values = paint.getFractions();
        buffer.append("[");
        for(int i=0; i<colors.length; i++) {
            buffer.append("#").append(Integer.toHexString(colors[i].getRGB()));
            buffer.append(":");
            buffer.append(values[i]);
            buffer.append(", ");
        }
        buffer.append("]");
        return buffer.toString();
    }

}
