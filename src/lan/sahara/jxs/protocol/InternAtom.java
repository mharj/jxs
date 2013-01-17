package lan.sahara.jxs.protocol;

import java.io.IOException;


import lan.sahara.jxs.common.Atom;
import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class InternAtom {
	public static Integer query(byte arg, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		System.err.println("Request: InternAtom");
		if (bytesRemaining < 4) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.InternAtom,0);
			return null;
		}
		int			n = inputOutput.readShort ();	// Length of name.
		int			pad = -n & 3;
		inputOutput.readSkip (2);	// Unused.
		bytesRemaining -= 4;
		if (bytesRemaining != n + pad) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.InternAtom,0);
			return null;
		}
		byte[]		name = new byte[n];
		inputOutput.readBytes (name, 0, n);	// The atom name.
		inputOutput.readSkip (pad);	// Unused.
		String atomName = new String (name);
		int			id = 0;
		boolean		onlyIfExists = (arg != 0);
		Atom		a = ourServer.findAtom(atomName);
		
		if (a != null) {
			id = a.getId ();
		} else if (!onlyIfExists) {
			a = new Atom (ourServer.nextFreeAtomId(),atomName);
			ourServer.addAtom (a);
			id = a.getId ();
		}
		return id;
	}
	
	public static void reply(Integer id,byte arg,InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		System.out.println("Reply: InternAtom");
		synchronized (inputOutput) {
			Util.writeReplyHeader (inputOutput,sequenceNumber, (byte) 0);
			inputOutput.writeInt (0);	// Reply length.
			inputOutput.writeInt (id);	// The atom ID.
			inputOutput.writePadBytes (20);	// Unused.
		}
		inputOutput.flush ();
	}
}
