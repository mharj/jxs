package lan.sahara.jsx.server;

import java.io.IOException;

public class ScreenView {
//	private Window                                  _rootWindow = null;
	private final XServer                   _xServer;
	private final int                               _rootId;	
	private final float                             _pixelsPerMillimeter;
	public ScreenView(XServer xServer,int rootId,float pixelsPerMillimeter) {
		_xServer = xServer;
		_rootId = rootId;
		_pixelsPerMillimeter = pixelsPerMillimeter;
	}
    public void write (InputOutput io) throws IOException {
//    		short width = (short) getWidth ();
//	    	short height = (short) getHeight ();    	
    		short width = 800;
    		short height = 600;
    	
            Visual vis = _xServer.getRootVisual ();

/*            
			io.writeInt (_rootWindow.getId ());             // Root window ID.
            io.writeInt (_defaultColormap.getId ());        // Default colormap ID.
            io.writeInt (_defaultColormap.getWhitePixel ());        // White pixel.
            io.writeInt (_defaultColormap.getBlackPixel ());        // Black pixel.
  */          
            
            io.writeInt (1);             // Root window ID.
            io.writeInt (1);        // Default colormap ID.
            io.writeInt (1);        // White pixel.
            io.writeInt (2);        // Black pixel.
            
            io.writeInt (0);        // Current input masks.
            io.writeShort (width);    // Width in pixels.
            io.writeShort (height);   // Height in pixels.
            io.writeShort ((short) (width / _pixelsPerMillimeter));       // Width in millimeters.
            io.writeShort ((short) (height / _pixelsPerMillimeter));       // Height in millimeters.
            io.writeShort ((short) 1);      // Minimum installed maps.
            io.writeShort ((short) 1);      // Maximum installed maps.
            io.writeInt (vis.getId ());     // Root visual ID.
            io.writeByte (vis.getBackingStoreInfo ());
            io.writeByte ((byte) (vis.getSaveUnder () ? 1 : 0));
            io.writeByte ((byte) vis.getDepth ());  // Root depth.
            io.writeByte ((byte) 1);        // Number of allowed depths.

            // Write the only allowed depth.
            io.writeByte ((byte) vis.getDepth ());  // Depth.
            io.writeByte ((byte) 0);        // Unused.
            io.writeShort ((short) 1);      // Number of visuals with this depth.
            io.writePadBytes (4);   // Unused.
            vis.write (io);         // The visual at this depth.
    }
}
