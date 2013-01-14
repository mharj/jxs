package lan.sahara.jxs.protocol;

import java.io.IOException;

import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class QueryExtension {
	/**
	 * The XQueryExtension() function determines if the named extension is present
	 * op_code: 98
	 * 
	 * @param unused Optional argument
	 * @param bytesRemaining size of data
	 * @param inputOutput TCP wrapper
	 * @param sequenceNumber current sequence id
	 * @return Extension name or null if error
	 * @throws IOException
	 */
	public static String query(byte unused, int bytesRemaining, InputOutput inputOutput,int sequenceNumber) throws IOException {
		System.err.print("Request: QueryExtension ");
    	if (bytesRemaining < 4) {
    		inputOutput.readSkip (bytesRemaining);
    		ErrorCode.write (inputOutput,sequenceNumber,ErrorCode.Length,RequestCode.QueryExtension, 0);
            return null;
    	}
    	int name_length = inputOutput.readShort ();		// Length of name.
    	int pad = -name_length & 3;
    	inputOutput.readSkip (2);        				// Unused.
    	bytesRemaining -= 4;
    	if (bytesRemaining != name_length + pad) {
    		inputOutput.readSkip (bytesRemaining);
    		ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length,RequestCode.QueryExtension, 0);
    		return null;
    	}
    	// read string
    	byte[] bytes = new byte[name_length];
    	inputOutput.readBytes(bytes, 0, name_length);
    	bytesRemaining -= name_length;
    	inputOutput.readSkip (pad);      // Unused.
    	bytesRemaining -= pad;
//    	System.out.println("QueryExtension - Bytes left:"+bytesRemaining);
    	String name = new String (bytes);
    	System.err.println(name);
    	return name;
	}
	
	public static void reply(Extension e,InputOutput inputOutput,int sequenceNumber) throws IOException {
		System.out.println("Reply: QueryExtension "+((e==null)?"False":"True"));
    	synchronized (inputOutput) {
            Util.writeReplyHeader (inputOutput,sequenceNumber, (byte) 0);
            inputOutput.writeInt (0);							// Reply length.
            if (e == null) {
            	inputOutput.writeByte ((byte) 0);				// Present. 0 = false.
            	inputOutput.writeByte ((byte) 0);				// Major opcode.
            	inputOutput.writeByte ((byte) 0);				// First event.
            	inputOutput.writeByte ((byte) 0);				// First error.
            } else {
            	inputOutput.writeByte ((byte) 1);				// Present. 1 = true.
            	inputOutput.writeByte (e.getMajorOpcode());		// Major opcode.
            	inputOutput.writeByte (e.getFirstEvent());		// First event.
            	inputOutput.writeByte (e.getFirstError());		// First error.
            }
            inputOutput.writePadBytes (20);  // Unused.
    	}
    	inputOutput.flush (); 
	}
}
