/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;

public class CommandExecutorHelper {

    public static CommandExecutorResult execute(String commandString, int expectedExitValue) throws Exception {
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        ByteArrayOutputStream stout = new ByteArrayOutputStream();
        CommandLine cmd = CommandLine.parse(commandString);
        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(expectedExitValue);
            executor.setStreamHandler(new PumpStreamHandler(stout, error));
            //if exit value != expectedExitValue => Exception
            int exitValue = executor.execute(cmd);
            return new CommandExecutorResult(exitValue, stout.toString(), error.toString());

        } catch (Exception e) {
            int exitValue = -1;
            if (e instanceof ExecuteException) {
                exitValue = ((ExecuteException) e).getExitValue();
            }
            throw new CommandExecutorException(
                    "error in command " + cmd.toString(),
                    new CommandExecutorResult(exitValue, stout.toString(), error.toString()));
        }
    }

}
