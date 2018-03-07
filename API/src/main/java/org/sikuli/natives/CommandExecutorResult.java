/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives;

public class CommandExecutorResult {
    private int exitValue;
    private String errorOutput;
    private String standardOutput;

    public CommandExecutorResult(int exitValue, String standardOutput, String errorOutput) {
        this.exitValue = exitValue;
        this.errorOutput = errorOutput;
        this.standardOutput = standardOutput;
    }

    public int getExitValue() {
        return exitValue;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public String getStandardOutput() {
        return standardOutput;
    }
}
