package lan.sahara.jxs.protocol;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Set;
import java.util.Vector;



import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.EventCode;
import lan.sahara.jxs.common.Pixmap;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Window;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.Client;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class CreateWindow {
	public static Window query(byte unused, int bytesRemaining, InputOutput inputOutput, int sequenceNumber, AbsApiServer ourServer) throws IOException {
		System.err.print("Request: CreateWindow ");
		Window w = null;
		int id = inputOutput.readInt(); // Window ID.
		int parent = inputOutput.readInt(); // Parent.
		bytesRemaining -= 8;
		System.err.println("(Window:"+id+" Parent:"+parent+")");
		Resource r = ourServer.getResource(parent);
		if (! ourServer.validResourceId(id)) {
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
				System.err.println("Request: CreateWindow (Window:"+id+" Parent:"+parent+")");
				// Window(Integer resource_id,Window parent, Rectangle geom, int borderWidth, boolean inputOnly, boolean isRoot)
				w = new Window(id, parent_window, new Rectangle(x,y,width,height) , borderWidth, inputOnly, false);
			} catch (OutOfMemoryError e) {
				inputOutput.readSkip(bytesRemaining);
				ErrorCode.write(inputOutput, sequenceNumber, ErrorCode.Alloc, RequestCode.CreateWindow, 0);
				return null;
			}
			// process Window Attributes
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
		}
		return w;
	}

	public static void reply(Window w,Window parent_window, InputOutput inputOutput, int sequenceNumber) throws IOException {
		System.out.println("Reply: CreateWindow");
		Rectangle geom = w.getGeometry();
		EventCode.sendCreateNotify (inputOutput,sequenceNumber, parent_window, w, geom.x, geom.y, geom.width, geom.height,w.getAttribute(Window.AttrBorderPixel), w.getOverrideRedirect());

		// TODO fix getSelectingClients (TCP clients) and w.applyValues !!!
	/*	
		Vector<Client>		sc = w.getSelectingClients (EventCode.MaskSubstructureNotify);
		if (sc != null) {
			for (Client c: sc) {
				EventCode.sendCreateNotify (c, parent_window, w,geom.x, geom.y, geom.width, geom.height,borderWidth, w.getOverrideRedirect());
			}
		}
*/		
	}
}
