package lan.sahara.jxs.protocol;

import java.io.IOException;




import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.Pixmap;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;

public class CreatePixmap {
	public static Pixmap query(byte depth, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		System.err.print("Request: CreatePixmap ");
		Pixmap ret = null;
		if (bytesRemaining != 12) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (inputOutput,sequenceNumber,  ErrorCode.Length, RequestCode.CreatePixmap, 0);
		} else {
			int	id = inputOutput.readInt ();		// Pixmap ID.
			int	did = inputOutput.readInt ();		// Drawable ID.
			int	width = inputOutput.readShort ();	// Width.
			int	height = inputOutput.readShort ();	// Height.
			System.err.println("[id:"+id+",drawable:"+did+",width:"+width+",height:"+height+"]");
			Resource r = ourServer.getResource (did);
			if (! ourServer.validResourceId (id)) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.IDChoice, RequestCode.CreatePixmap, id);
			} else if (r == null || !r.isDrawable ()) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Drawable, RequestCode.CreatePixmap,did);
			} else {
				ret = new Pixmap (id, width, height, depth);
			}
		}
		return ret;
	}
}
