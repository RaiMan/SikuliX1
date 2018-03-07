/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.tips;

import org.jdesktop.swingx.tips.TipOfTheDayModel.Tip;

/**
 * Default {@link org.jdesktop.swingx.tips.TipOfTheDayModel.Tip} implementation.<br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class DefaultTip implements Tip {

    private String name;

    private Object tip;

    public DefaultTip() {
    }

    public DefaultTip(String name, Object tip) {
        this.name = name;
        this.tip = tip;
    }

    @Override
    public Object getTip() {
        return tip;
    }

    public void setTip(Object tip) {
        this.tip = tip;
    }

    @Override
    public String getTipName() {
        return name;
    }

    public void setTipName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getTipName();
    }

}
