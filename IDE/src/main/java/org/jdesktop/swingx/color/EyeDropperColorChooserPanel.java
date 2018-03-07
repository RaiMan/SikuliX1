/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.color;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXColorSelectionButton;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * <p>EyeDropperColorChooserPanel is a pluggable panel for the
 * {@link JColorChooser} which allows the user to grab any
 * color from the screen using a magnifying glass.</p>
 *
 * <p>Example usage:</p>
 * <pre><code>
 *    public static void main(String ... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JColorChooser chooser = new JColorChooser();
                chooser.addChooserPanel(new EyeDropperColorChooserPanel());
                JFrame frame = new JFrame();
                frame.add(chooser);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
 * </code></pre>
 *
 * @author joshua@marinacci.org
 */
public class EyeDropperColorChooserPanel extends AbstractColorChooserPanel {

    /**
     * Example usage
     */
    public static void main(String ... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JColorChooser chooser = new JColorChooser();
                chooser.addChooserPanel(new EyeDropperColorChooserPanel());
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(chooser);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    /**
     * Creates new EyeDropperColorChooserPanel
     */
    public EyeDropperColorChooserPanel() {
        initComponents();
        MouseInputAdapter mia = new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
            }
            @Override
            public void mouseDragged(MouseEvent evt) {
                Point pt = evt.getPoint();
                SwingUtilities.convertPointToScreen(pt,evt.getComponent());
                ((MagnifyingPanel)magPanel).setMagPoint(pt);
            }
            @Override
            public void mouseReleased(MouseEvent evt) {
                Color newColor = new Color(((MagnifyingPanel)magPanel).activeColor);
                getColorSelectionModel().setSelectedColor(newColor);
            }
        };
        eyeDropper.addMouseListener(mia);
        eyeDropper.addMouseMotionListener(mia);
        try {
            eyeDropper.setIcon(new ImageIcon(
                    EyeDropperColorChooserPanel.class.getResource("mag.png")));
            eyeDropper.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        magPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Color color = new Color(((MagnifyingPanel)magPanel).activeColor);
                activeColor.setBackground(color);
                hexColor.setText(PaintUtils.toHexString(color).substring(1));
                rgbColor.setText(color.getRed() +"," + color.getGreen() + "," + color.getBlue());
            }
        });
    }

    private class MagnifyingPanel extends JPanel {
        private Point2D point;
        private int activeColor;
        public void setMagPoint(Point2D point) {
            this.point = point;
            repaint();
        }
        @Override
        public void paintComponent(Graphics g) {
            if(point != null) {
                Rectangle rect = new Rectangle((int)point.getX()-10,(int)point.getY()-10,20,20);
                try {
                    BufferedImage img =new Robot().createScreenCapture(rect);
                    g.drawImage(img,0,0,getWidth(),getHeight(),null);
                    int oldColor = activeColor;
                    activeColor = img.getRGB(img.getWidth()/2,img.getHeight()/2);
                    firePropertyChange("activeColor", oldColor, activeColor);
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            }
            g.setColor(Color.black);
            g.drawRect(getWidth()/2 - 5, getHeight()/2 -5, 10,10);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        eyeDropper = new javax.swing.JButton();
        magPanel = new MagnifyingPanel();
        activeColor = new JXColorSelectionButton();
        hexColor = new javax.swing.JTextField();
        JTextArea jTextArea1 = new JTextArea();
        jLabel1 = new javax.swing.JLabel();
        rgbColor = new javax.swing.JTextField();
        JLabel jLabel2 = new JLabel();

        setLayout(new java.awt.GridBagLayout());

        eyeDropper.setText("eye");
        add(eyeDropper, new java.awt.GridBagConstraints());

        magPanel.setLayout(new java.awt.BorderLayout());

        magPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        magPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        magPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(magPanel, gridBagConstraints);

        activeColor.setEnabled(false);
        activeColor.setPreferredSize(new java.awt.Dimension(40, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        add(activeColor, gridBagConstraints);

        hexColor.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        add(hexColor, gridBagConstraints);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("Drag the magnifying glass to select a color from the screen.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(jTextArea1, gridBagConstraints);

        jLabel1.setText("#");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(jLabel1, gridBagConstraints);

        rgbColor.setEditable(false);
        rgbColor.setText("255,255,255");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        add(rgbColor, gridBagConstraints);

        jLabel2.setText("RGB");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(jLabel2, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton activeColor;
    private javax.swing.JButton eyeDropper;
    private javax.swing.JTextField hexColor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel magPanel;
    private javax.swing.JTextField rgbColor;
    // End of variables declaration//GEN-END:variables

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChooser() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildChooser() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Grab from Screen";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Icon getSmallDisplayIcon() {
        return new ImageIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Icon getLargeDisplayIcon() {
        return new ImageIcon();
    }
}
