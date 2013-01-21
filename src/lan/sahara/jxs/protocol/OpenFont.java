package lan.sahara.jxs.protocol;

import java.io.IOException;






import lan.sahara.jxs.common.Atom;
import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.Font;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;

/**
 * OpenFont 
 * opcode: 45
 * @author mharj
 *
 */
public class OpenFont {
	public static Font query(byte unused, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		Font ret = null;
		System.err.print("Request: OpenFont ");
		int id = inputOutput.readInt ();	// Font ID.
		bytesRemaining -= 4;
		System.err.print("[resource:"+id+"]");
		if ( ! ourServer.validResourceId(id) ) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.IDChoice, RequestCode.OpenFont, id);
			System.err.println("");
		} else {
			int length = inputOutput.readShort ();	// Length of name.
			int pad = -length & 3;
			inputOutput.readSkip (2);	// Unused.
			bytesRemaining -= 4;
			if (bytesRemaining != length + pad) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.OpenFont,0);
				return null;
			}
			byte[]		nameBytes = new byte[length];
			inputOutput.readBytes (nameBytes, 0, length);
			inputOutput.readSkip (pad);

			String		name = new String (nameBytes);
			System.err.println(" [font:"+name+"]");
			ret = new Font (id,name);
			
			// Create an atom containing the font name.
			Atom		a = ourServer.findAtom (name);
			if (a == null) {
				a = new Atom (ourServer.nextFreeAtomId (), name);
				ourServer.addAtom (a);
			}
			ret._nameAtom = a;			
		}
		return ret;
	}
}
