package lan.sahara.jxs.protocol;

import java.io.IOException;

import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Window;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class ChangeWindowAttributes {
	public static void query(byte unused, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
    	System.err.print("Request: ChangeWindowAttributes");
    	if (bytesRemaining < 4) {
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.ChangeWindowAttributes, 0);
		} else {
			int			id = inputOutput.readInt ();
			Resource	r = ourServer.getResource (id);
			System.err.println("(WindowId:"+id+")");
			bytesRemaining -= 4;
			if (r == null || r.getType () != Resource.WINDOW) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Window, RequestCode.ChangeWindowAttributes, id);
			} else {
				Window w = (Window) r;
				int	valueMask = inputOutput.readInt ();	// Value mask.
				bytesRemaining -= 4;
				int			n = Util.bitcount (valueMask);
				if (bytesRemaining != n * 4) {
					inputOutput.readSkip (bytesRemaining);
					ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.ChangeWindowAttributes, 0);
					return;
				}
				for (int i = 0; i < 15; i++) {
					if ((valueMask & (1 << i)) != 0) {
						switch (i) {
							case Window.AttrBackgroundPixmap:
							case Window.AttrBackgroundPixel:
							case Window.AttrBorderPixmap:
							case Window.AttrBorderPixel:
							case Window.AttrBackingPlanes:
							case Window.AttrBackingPixel:
							case Window.AttrEventMask:
							case Window.AttrDoNotPropagateMask:
							case Window.AttrColormap:
							case Window.AttrCursor:
								w.setAttribute(i, inputOutput.readInt() );
								break;
							case Window.AttrBitGravity:
							case Window.AttrWinGravity:
							case Window.AttrBackingStore:
							case Window.AttrOverrideRedirect:
							case Window.AttrSaveUnder:
								w.setAttribute(i, inputOutput.readByte() );
								inputOutput.readSkip (3);
								break;
						}
					}
				}

			}
		}
	}
}
