/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.multislider.DefaultMultiThumbModel;
import org.jdesktop.swingx.multislider.MultiThumbModel;
import org.jdesktop.swingx.multislider.ThumbDataEvent;
import org.jdesktop.swingx.multislider.ThumbDataListener;
import org.jdesktop.swingx.multislider.ThumbListener;
import org.jdesktop.swingx.multislider.ThumbRenderer;
import org.jdesktop.swingx.multislider.TrackRenderer;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.MultiThumbSliderAddon;
import org.jdesktop.swingx.plaf.MultiThumbSliderUI;

/**
 * <p>A slider which can have multiple control points or <i>Thumbs</i></p>
 * <p>The thumbs each represent a value between the minimum and maximum values
 * of the slider.  Thumbs can pass each other when being dragged.  Thumbs have
 * no default visual representation. To customize the look of the thumbs and the
 * track behind the thumbs you must provide a ThumbRenderer and a TrackRenderer
 * implementation. To listen for changes to the thumbs you must provide an
 * implementation of ThumbDataListener.
 *
 * TODOs:
 * add min/maxvalue convenience methods to jxmultithumbslider
 * add plafs for windows, mac, and basic (if necessary)
 * make way to properly control the height.
 * hide the inner thumb component
 *
 * @author joshy
 */
@JavaBean
public class JXMultiThumbSlider<E> extends JComponent {
    public static final String uiClassID = "MultiThumbSliderUI";

    private ThumbDataListener tdl;

    private List<ThumbComp> thumbs;

    private ThumbRenderer thumbRenderer;

    private TrackRenderer trackRenderer;

    private MultiThumbModel<E> model;

    private List<ThumbListener> listeners = new ArrayList<ThumbListener>();

    private ThumbComp selected;

    static {
        LookAndFeelAddons.contribute(new MultiThumbSliderAddon());
    }

    /** Creates a new instance of JMultiThumbSlider */
    public JXMultiThumbSlider() {
        thumbs = new ArrayList<ThumbComp>();
        setLayout(null);

        tdl = new ThumbHandler();

        setModel(new DefaultMultiThumbModel<E>());
        MultiThumbMouseListener mia = new MultiThumbMouseListener();
        addMouseListener(mia);
        addMouseMotionListener(mia);

        Dimension dim = new Dimension(60,16);
        setPreferredSize(dim);
        setSize(dim);
        setMinimumSize(new Dimension(30,16));
        updateUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    public MultiThumbSliderUI getUI() {
        return (MultiThumbSliderUI)ui;
    }

    public void setUI(MultiThumbSliderUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        setUI((MultiThumbSliderUI)LookAndFeelAddons.getUI(this, MultiThumbSliderUI.class));
        invalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(isVisible()) {
            if(trackRenderer != null) {
                JComponent comp = trackRenderer.getRendererComponent(this);
                add(comp);
                comp.paint(g);
                remove(comp);
            } else {
                paintRange((Graphics2D)g);
            }
        }
    }

    private void paintRange(Graphics2D g) {
        g.setColor(Color.blue);
        g.fillRect(0,0,getWidth(),getHeight());
    }

    private float getThumbValue(int thumbIndex) {
        return getModel().getThumbAt(thumbIndex).getPosition();
    }

    private float getThumbValue(ThumbComp thumb) {
        return getThumbValue(thumbs.indexOf(thumb));
    }

    private int getThumbIndex(ThumbComp thumb) {
        return thumbs.indexOf(thumb);
    }

    private void clipThumbPosition(ThumbComp thumb) {
        if(getThumbValue(thumb) < getModel().getMinimumValue()) {
            getModel().getThumbAt(getThumbIndex(thumb)).setPosition(
                getModel().getMinimumValue());
        }
        if(getThumbValue(thumb) > getModel().getMaximumValue()) {
            getModel().getThumbAt(getThumbIndex(thumb)).setPosition(
            getModel().getMaximumValue());
        }
    }

    public ThumbRenderer getThumbRenderer() {
        return thumbRenderer;
    }

    public void setThumbRenderer(ThumbRenderer thumbRenderer) {
        this.thumbRenderer = thumbRenderer;
    }

    public TrackRenderer getTrackRenderer() {
        return trackRenderer;
    }

    public void setTrackRenderer(TrackRenderer trackRenderer) {
        this.trackRenderer = trackRenderer;
    }

    public float getMinimumValue() {
        return getModel().getMinimumValue();
    }

    public void setMinimumValue(float minimumValue) {
        getModel().setMinimumValue(minimumValue);
    }

    public float getMaximumValue() {
        return getModel().getMaximumValue();
    }

    public void setMaximumValue(float maximumValue) {
        getModel().setMaximumValue(maximumValue);
    }

    private void setThumbPositionByX(ThumbComp selected) {
        float range = getModel().getMaximumValue()-getModel().getMinimumValue();
        int x = selected.getX();
        // adjust to the center of the thumb
        x += selected.getWidth()/2;
        // adjust for the leading space on the slider
        x -= selected.getWidth()/2;

        int w = getWidth();
        // adjust for the leading and trailing space on the slider
        w -= selected.getWidth();
        float delta = ((float)x)/((float)w);
        int thumb_index = getThumbIndex(selected);
        float value = delta*range;
        getModel().getThumbAt(thumb_index).setPosition(value);
        //getModel().setPositionAt(thumb_index,value);
        clipThumbPosition(selected);
    }

    private void setThumbXByPosition(ThumbComp thumb, float pos) {
        float lp = getWidth()-thumb.getWidth();
        float lu = getModel().getMaximumValue()-getModel().getMinimumValue();
        float tp = (pos*lp)/lu;
        thumb.setLocation((int)tp-thumb.getWidth()/2 + thumb.getWidth()/2, thumb.getY());
    }

    private void recalc() {
        for(ThumbComp th : thumbs) {
            setThumbXByPosition(th,getModel().getThumbAt(getThumbIndex(th)).getPosition());
            //getPositionAt(getThumbIndex(th)));
        }
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x,y,w,h);
        recalc();
    }

    public JComponent getSelectedThumb() {
        return selected;
    }

    public int getSelectedIndex() {
        return getThumbIndex(selected);
    }

    public MultiThumbModel<E> getModel() {
        return model;
    }

    public void setModel(MultiThumbModel<E> model) {
        if(this.model != null) {
            this.model.removeThumbDataListener(tdl);
        }
        this.model = model;
        this.model.addThumbDataListener(tdl);
    }

    public void addMultiThumbListener(ThumbListener listener) {
        listeners.add(listener);
    }

    private class MultiThumbMouseListener extends MouseInputAdapter {
        @Override
        public void mousePressed(MouseEvent evt) {
            ThumbComp handle = findHandle(evt);
            if(handle != null) {
                selected = handle;
                selected.setSelected(true);
                int thumb_index = getThumbIndex(selected);
                for(ThumbListener tl : listeners) {
                    tl.thumbSelected(thumb_index);
                }
                repaint();
            } else {
                selected = null;
                for(ThumbListener tl : listeners) {
                    tl.thumbSelected(-1);
                }
                repaint();
            }
            for(ThumbListener tl : listeners) {
                tl.mousePressed(evt);
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            if(selected != null) {
                selected.setSelected(false);
            }
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if(selected != null) {
                int nx = (int)evt.getPoint().getX()- selected.getWidth()/2;
                if(nx < 0) {
                    nx = 0;
                }
                if(nx > getWidth()-selected.getWidth()) {
                    nx = getWidth()-selected.getWidth();
                }
                selected.setLocation(nx,(int)selected.getLocation().getY());
                setThumbPositionByX(selected);
                int thumb_index = getThumbIndex(selected);
                //log.fine("still dragging: " + thumb_index);
                for(ThumbListener mtl : listeners) {
                    mtl.thumbMoved(thumb_index,getModel().getThumbAt(thumb_index).getPosition());
                    //getPositionAt(thumb_index));
                }
                repaint();
            }
        }

        private ThumbComp findHandle(MouseEvent evt) {
            for(ThumbComp hand : thumbs) {
                Point p2 = new Point();
                p2.setLocation(evt.getPoint().getX() - hand.getX(),
                    evt.getPoint().getY() - hand.getY());
                if(hand.contains(p2)) {
                    return hand;
                }
            }
            return null;
        }
    }

    private static class ThumbComp extends JComponent {

        private JXMultiThumbSlider<?> slider;

        public ThumbComp(JXMultiThumbSlider<?> slider) {
            this.slider = slider;
            Dimension dim = new Dimension(10,10);//slider.getHeight());
            /*if(slider.getThumbRenderer() != null) {
                JComponent comp = getRenderer();
                dim = comp.getPreferredSize();
            }*/
            setSize(dim);
            setMinimumSize(dim);
            setPreferredSize(dim);
            setMaximumSize(dim);
            setBackground(Color.white);
        }

        @Override
        public void paintComponent(Graphics g) {
            if(slider.getThumbRenderer() != null) {
                JComponent comp = getRenderer();
                comp.setSize(this.getSize());
                comp.paint(g);
            } else {
                g.setColor(getBackground());
                g.fillRect(0,0,getWidth(),getHeight());
                if(isSelected()) {
                    g.setColor(Color.black);
                    g.drawRect(0,0,getWidth()-1,getHeight()-1);
                }
            }
        }

        private JComponent getRenderer() {
            return slider.getThumbRenderer().
                    getThumbRendererComponent(slider,slider.getThumbIndex(this),isSelected());
        }

        private boolean selected;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    private class ThumbHandler implements ThumbDataListener {
        @Override
        public void positionChanged(ThumbDataEvent e) {
            ThumbComp comp = thumbs.get(e.getIndex());
            clipThumbPosition(comp);
            setThumbXByPosition(comp, e.getThumb().getPosition());
            repaint();
        }

        @Override
        public void thumbAdded(ThumbDataEvent evt) {
            ThumbComp thumb = new ThumbComp(JXMultiThumbSlider.this);
            thumb.setLocation(0, 0);
            add(thumb);
            thumbs.add(evt.getIndex(), thumb);
            clipThumbPosition(thumb);
            setThumbXByPosition(thumb, evt.getThumb().getPosition());
            repaint();
        }

        @Override
        public void thumbRemoved(ThumbDataEvent evt) {
            ThumbComp thumb = thumbs.get(evt.getIndex());
            remove(thumb);
            thumbs.remove(thumb);
            repaint();
        }

        @Override
        public void valueChanged(ThumbDataEvent e) {
            repaint();
        }
    }
}
