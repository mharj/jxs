package lan.sahara.jxs.protocol;

import java.io.IOException;

import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;


/**
 * 
 * TODO: root id resource always=1 ?
 * @author mharj
 *
 */
public class GetInputFocus {
	public static Integer query(byte unused, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiClient ourClient,AbsApiServer ourServer) throws IOException {
		Integer ret = null;
		System.err.println("Request: GetInputFocus ");
		if (bytesRemaining != 0) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.GetInputFocus, 0);
		} else {
			int			wid;
			Integer focusWindow = ourClient.getInputFocus();
			if (focusWindow == null)
				wid = 0;
			else if ( focusWindow == ourServer.getRootId() )
				wid = 1;
			else
				wid = focusWindow;
			ret = wid;
		}
		return ret;
	}
	
	public static void reply(Integer focus_id,byte rev_focus_id,InputOutput inputOutput,int sequenceNumber) throws IOException {
		System.out.println("Reply: GetInputFocus [focus_id:"+focus_id+",rev_focus_id:"+rev_focus_id+"]");
		synchronized (inputOutput) {
			Util.writeReplyHeader (inputOutput,sequenceNumber, rev_focus_id);
			inputOutput.writeInt (0);	// Reply length.
			inputOutput.writeInt (focus_id);	// Focus window.
			inputOutput.writePadBytes (20);	// Unused.
		}
		inputOutput.flush ();
	}
}
