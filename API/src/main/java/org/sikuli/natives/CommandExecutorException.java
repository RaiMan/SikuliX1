/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives;

/**
 * Wrapper for all Exception which occurs during native command execution.
 *
 * @author tschneck
 *         Date: 9/15/15
 */
public class CommandExecutorException extends RuntimeException {
    private final CommandExecutorResult commandExecutorResult;

    public CommandExecutorException(String message) {
        super(message);
        this.commandExecutorResult = null;
    }

    public CommandExecutorException(String message, CommandExecutorResult commandExecutorResult) {
        super(message);
        this.commandExecutorResult = commandExecutorResult;
    }

    public CommandExecutorResult getCommandExecutorResult() {
        return commandExecutorResult;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("[error] ");
        if (super.getMessage() != null) {
            sb.append(super.getMessage());
        }
        if (commandExecutorResult != null) {
            String stout = commandExecutorResult.getStandardOutput();
            if (stout != null && !stout.isEmpty()) {
                sb.append("\n[stout] ").append(stout);
            }
            String errorOutput = commandExecutorResult.getErrorOutput();
            if (errorOutput != null && !errorOutput.isEmpty()) {
                sb.append("\n[errout] ").append(errorOutput);
            }
        }
        return sb.toString();
    }
}
