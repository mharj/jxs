package lan.sahara.jxs.common;

import java.io.IOException;

import lan.sahara.jxs.server.Client;
import lan.sahara.jxs.server.InputOutput;

/**
 * @author Matthew Kwan
 * 
 *         All the X error codes.
 */
public class ErrorCode {
	public static final byte None = 0;
	public static final byte Request = 1;
	public static final byte Value = 2;
	public static final byte Window = 3;
	public static final byte Pixmap = 4;
	public static final byte Atom = 5;
	public static final byte Cursor = 6;
	public static final byte Font = 7;
	public static final byte Match = 8;
	public static final byte Drawable = 9;
	public static final byte Access = 10;
	public static final byte Alloc = 11;
	public static final byte Colormap = 12;
	public static final byte GContext = 13;
	public static final byte IDChoice = 14;
	public static final byte Name = 15;
	public static final byte Length = 16;
	public static final byte Implementation = 17;

	/**
	 * Write an X error.
	 * 
	 * @param client The remote client.
	 * @param error The error code.
	 * @param opcode The opcode of the error request.
	 * @param resourceId The (optional) resource ID that caused the error.
	 * @throws IOException
	 */
	public static void write(Client client, byte error, byte opcode, int resourceId) throws IOException {
		System.err.println("ErrorCode: " + getErrorName(error) + " resourceId:" + resourceId);
		writeWithMinorOpcode(client, error, (short) 0, opcode, resourceId);
	}
	
	public static void write(InputOutput inputOutput,int sequenceNumber,byte error, byte opcode, int resourceId) throws IOException {
		System.err.println("ErrorCode: " + getErrorName(error) + " resourceId:" + resourceId);
		writeWithMinorOpcode(inputOutput,sequenceNumber,error, (short) 0, opcode, resourceId);
	}	

	/**
	 * Write an X error with a minor opcode specified.
	 * 
	 * @param client The remote client.
	 * @param error The error code.
	 * @param minorOpcode The minor opcode of the error request.
	 * @param opcode The major opcode of the error request.
	 * @param resourceId The (optional) resource ID that caused the error.
	 * @throws IOException
	 */
	public static void writeWithMinorOpcode(Client client, byte error, short minorOpcode, byte opcode, int resourceId) throws IOException {
		InputOutput io = client.getInputOutput();
		short sn = (short) (client.getSequenceNumber() & 0xffff);

		synchronized (io) {
			io.writeByte((byte) 0); // Indicates an error.
			io.writeByte(error); // Error code.
			io.writeShort(sn); // Sequence number.
			io.writeInt(resourceId); // Bad resource ID.
			io.writeShort(minorOpcode); // Minor opcode.
			io.writeByte(opcode); // Major opcode.
			io.writePadBytes(21); // Unused.
		}
		io.flush();
	}
	
	public static void writeWithMinorOpcode(InputOutput inputOutput,int sequenceNumber, byte error, short minorOpcode, byte opcode, int resourceId) throws IOException {
		short sn = (short) (sequenceNumber & 0xffff);
		synchronized (inputOutput) {
			inputOutput.writeByte((byte) 0); // Indicates an error.
			inputOutput.writeByte(error); // Error code.
			inputOutput.writeShort(sn); // Sequence number.
			inputOutput.writeInt(resourceId); // Bad resource ID.
			inputOutput.writeShort(minorOpcode); // Minor opcode.
			inputOutput.writeByte(opcode); // Major opcode.
			inputOutput.writePadBytes(21); // Unused.
		}
		inputOutput.flush();
	}	

	private static String getErrorName(int error) {
		switch (error) {
		case ErrorCode.None:
			return "None";
		case ErrorCode.Request:
			return "Request";
		case ErrorCode.Value:
			return "Value";
		case ErrorCode.Window:
			return "Window";
		case ErrorCode.Pixmap:
			return "Pixmap";
		case ErrorCode.Atom:
			return "Atom";
		case ErrorCode.Cursor:
			return "Cursor";
		case ErrorCode.Font:
			return "Font";
		case ErrorCode.Match:
			return "Match";
		case ErrorCode.Drawable:
			return "Drawable";
		case ErrorCode.Access:
			return "Access";
		case ErrorCode.Alloc:
			return "Alloc";
		case ErrorCode.Colormap:
			return "Colormap";
		case ErrorCode.GContext:
			return "GContext";
		case ErrorCode.IDChoice:
			return "IDChoice";
		case ErrorCode.Name:
			return "Name";
		case ErrorCode.Length:
			return "Length";
		case ErrorCode.Implementation:
			return "Implementation";
		}
		return null;
	}
}