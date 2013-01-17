package lan.sahara.jxs.protocol;

import java.io.IOException;

import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Window;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;

public class MapWindow {
	public static void query(byte unused, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		System.err.println("Request: MapWindow");
		if (bytesRemaining < 4) {
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.MapWindow, 0);
		} else {
			int			id = inputOutput.readInt ();
			Resource	r = ourServer.getResource (id);
			bytesRemaining -= 4;
			if ( r == null || r.getType() != Resource.WINDOW) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Window, RequestCode.MapWindow, id);
				return;
			} 
			((Window)r).setMap(true);
		}		
	}
}
