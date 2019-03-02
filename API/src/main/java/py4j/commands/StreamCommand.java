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
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import py4j.Protocol;
import py4j.Py4JException;
import py4j.ReturnObject;

/**
 * <p>
 * A {@link StreamCommand} is like a {@link CallCommand}, but returns
 * the value directly.
 * </p>
 *
 * @author Nick White
 *
 */
public class StreamCommand extends AbstractCommand {
	public final static String STREAM_COMMAND_NAME = "S";

	/**
	 * Usually a page.
	 */
	private final ByteBuffer streamBuffer = ByteBuffer.allocateDirect(4096);

	public StreamCommand() {
		this.commandName = STREAM_COMMAND_NAME;
	}

	private void feedException(BufferedWriter writer, ReturnObject e) throws IOException {
		writer.write(Protocol.RETURN_MESSAGE);
		writer.write(e.getCommandPart());
		writer.write(Protocol.END_OUTPUT);
		writer.flush();
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String targetObjectId = reader.readLine();
		String methodName = reader.readLine();
		List<Object> arguments = getArguments(reader);

		ReturnObject returnObject = invokeMethod(methodName, targetObjectId, arguments);
		if (returnObject.isError()) {
			feedException(writer, returnObject);
			return;
		}

		if (!returnObject.isReference()) {
			// No point putting a Py4J protocol message down the socket if the caller
			// is expecting a binary blob.
			feedException(writer, ReturnObject
					.getErrorReturnObject(new ClassCastException("expected the method to return an Object")));
			return;
		}
		Object obj = gateway.getObject(returnObject.getName());
		if (!(obj instanceof ReadableByteChannel)) {
			feedException(writer, ReturnObject.getErrorReturnObject(
					new ClassCastException("expected the method to return a ReadableByteChannel")));
			return;
		}

		writer.write(Protocol.VOID_COMMAND);
		writer.flush();

		// just dump the contents into the Socket
		ReadableByteChannel in = (ReadableByteChannel) obj;
		try {
			WritableByteChannel out = Channels.newChannel(connection.getSocket().getOutputStream());
			streamBuffer.rewind();

			while (in.read(streamBuffer) != -1) {
				streamBuffer.flip();
				out.write(streamBuffer);
				streamBuffer.compact();
			}

			// drain the rest
			streamBuffer.flip();
			while (streamBuffer.hasRemaining()) {
				out.write(streamBuffer);
			}
		} finally {
			in.close();
		}
	}
}
