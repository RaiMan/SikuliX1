/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import py4Java.Protocol;
import py4Java.Py4JException;
import py4Java.ReturnObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

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
        try (ReadableByteChannel in = (ReadableByteChannel) obj) {
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
		}
	}
}
