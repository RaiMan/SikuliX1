/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.tips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Default {@link org.jdesktop.swingx.tips.TipOfTheDayModel} implementation.<br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class DefaultTipOfTheDayModel implements TipOfTheDayModel {

    private List<Tip> tips = new ArrayList<Tip>();

    public DefaultTipOfTheDayModel() {
    }

    public DefaultTipOfTheDayModel(Tip[] tips) {
        this(Arrays.asList(tips));
    }

    public DefaultTipOfTheDayModel(Collection<Tip> tips) {
        this.tips.addAll(tips);
    }

    @Override
    public Tip getTipAt(int index) {
        return tips.get(index);
    }

    @Override
    public int getTipCount() {
        return tips.size();
    }

    public void add(Tip tip) {
        tips.add(tip);
    }

    public void remove(Tip tip) {
        tips.remove(tip);
    }

    public Tip[] getTips() {
        return tips.toArray(new Tip[tips.size()]);
    }

    public void setTips(Tip[] tips) {
        this.tips.clear();
        this.tips.addAll(Arrays.asList(tips));
    }

}
