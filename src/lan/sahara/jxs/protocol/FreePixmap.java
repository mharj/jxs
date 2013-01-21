package lan.sahara.jxs.protocol;

import java.io.IOException;


import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;

public class FreePixmap {
	public static Integer query(byte arg, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		int id = inputOutput.readInt ();
		Resource r = ourServer.getResource (id);

		bytesRemaining -= 4;
		if (r == null || r.getType () != Resource.PIXMAP)
			ErrorCode.write (inputOutput,sequenceNumber,ErrorCode.Pixmap, RequestCode.FreePixmap, id);
		else {
			if (bytesRemaining != 0) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.FreePixmap, 0);
			} else {
				return id;
				
/*				ourServer.freeResource (id);
				if (_client != null)
					_client.freeResource (this);
				_drawable.getBitmap().recycle ();
*/				
			}
		}
		return null;
	}
}
