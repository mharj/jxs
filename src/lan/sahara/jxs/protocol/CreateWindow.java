package lan.sahara.jxs.protocol;

import java.awt.Rectangle;
import java.io.IOException;


import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Window;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class CreateWindow {
	public static Window query(byte unused, int bytesRemaining, InputOutput inputOutput, int sequenceNumber, AbsApiServer ourServer) throws IOException {
		Window w = null;
		int id = inputOutput.readInt(); // Window ID.
		int parent = inputOutput.readInt(); // Parent.
		bytesRemaining -= 8;
		Resource r = ourServer.getResource(parent);
		bytesRemaining -= 8;
		if (!ourServer.validResourceId(id)) {
			inputOutput.readSkip(bytesRemaining);
			ErrorCode.write(inputOutput, sequenceNumber, ErrorCode.IDChoice, RequestCode.CreateWindow, id);
		} else if (r == null || r.getType() != Resource.WINDOW) {
			inputOutput.readSkip(bytesRemaining);
			ErrorCode.write(inputOutput, sequenceNumber, ErrorCode.Window, RequestCode.CreateWindow, parent);
		} else {
			Window parent_window = (Window) r;
			int x = (short) inputOutput.readShort(); // X position.
			int y = (short) inputOutput.readShort(); // Y position.
			int width = inputOutput.readShort(); // Window width.
			int height = inputOutput.readShort(); // Window height.
			int borderWidth = inputOutput.readShort(); // Border width.
			int wclass = inputOutput.readShort(); // Window class.
			boolean inputOnly;
			inputOutput.readInt(); // Visual.
			bytesRemaining -= 16;

			if (wclass == 0) // Copy from parent.
				inputOnly = parent_window._inputOnly;
			else if (wclass == 1) // Input/output.
				inputOnly = false;
			else
				inputOnly = true;

			try {
				// Window(Integer resource_id,Window parent, Rectangle geom, int borderWidth, boolean inputOnly, boolean isRoot)
				w = new Window(id, parent_window, new Rectangle(x,y,width,height) , borderWidth, inputOnly, false);
			} catch (OutOfMemoryError e) {
				inputOutput.readSkip(bytesRemaining);
				ErrorCode.write(inputOutput, sequenceNumber, ErrorCode.Alloc, RequestCode.CreateWindow, 0);
				return null;
			}

			if (bytesRemaining < 4) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput, sequenceNumber, ErrorCode.Length, RequestCode.CreateWindow, 0);
				return null;
			}
			int			valueMask = inputOutput.readInt ();	// Value mask.
			bytesRemaining -= 4;
			int			n = Util.bitcount (valueMask);
			if (bytesRemaining != n * 4) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput, sequenceNumber, ErrorCode.Length, RequestCode.CreateWindow, 0);
				return null;
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
			valueMask = 0xffffffff; // for new window
		
			// TODO: fill rest!!!
			ERROR
			ERROR
			// TODO: fill rest!!!

		}
		return w;
	}

	public static void reply(Window w, InputOutput inputOutput, int sequenceNumber) throws IOException {

	}
}
