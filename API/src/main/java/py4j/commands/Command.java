/******************************************************************************
 * Copyright (c) 2009-2018, Barthelemy Dagenais and individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/
package py4j.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import py4j.Gateway;
import py4j.GatewayConnection;
import py4j.Py4JException;
import py4j.Py4JServerConnection;

/**
 * <p>
 * This interface must be implemented by all commands.
 * </p>
 * <p>
 * Typically, each command will define a public constant field that contains the
 * command name, i.e., a String that uniquely identifies the command.
 * </p>
 *
 * <p>
 * The command name can be a String of any length, but it must not contain an
 * end of line character and it cannot be the End of Command character,
 * {@link py4j.Protocol#END}.
 * </p>
 *
 * <p>
 * There is a command instance per {@link GatewayConnection}: this ensures that
 * each command instance is accessed by only one thread/connection at a time.
 * </p>
 *
 * @author barthelemy
 *
 */
public interface Command {

	/**
	 *
	 * @param commandName
	 *            The command name that was extracted of the command.
	 * @param reader
	 *            The reader from which to read the command parts. Each command
	 *            part are expected to be on a separate line and readable
	 *            through {@link BufferedReader#readLine()}.
	 * @param writer
	 *            The writer to which the return value should be written.
	 * @throws Py4JException
	 *             If an error occurs while executing the command. All
	 *             exceptions except IOException caused by the reader and the
	 *             writer should be wrapper in a {@link Py4JException} instance.
	 * @throws IOException
	 *             If an error occurs while using the reader or the writer.
	 */
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException;

	public String getCommandName();

	/**
	 * <p>
	 * Called when a command instance is created and assigned to a connection.
	 * </p>
	 *
	 * @param gateway
	 * @param connection the {@link Py4JServerConnection} this socket is assigned to
	 */
	public void init(Gateway gateway, Py4JServerConnection connection);

}
