/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

/**
 * INTERNAL USE
 */
public class SikuliException extends Exception {

    protected String _name = "SikuliException";

    public SikuliException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        String ret = _name + ": " + getMessage() + "\n";
        for (StackTraceElement elm : getStackTrace()) {
            ret += "  Line " + elm.getLineNumber()
                    + ", in file " + elm.getFileName() + "\n";
            return ret;
        }
        ret += "Line ?, in File ?";
        return ret;
    }

    public String toStringShort() {
        return _name + ": " + getMessage();
    }
}
