package lan.sahara.jsx.server;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
    public static final int         Destroy = 0;
    public static final int         RetainPermanent = 1;
    public static final int         RetainTemporary = 2;

    private final XServer                   _xServer;
    private final Socket                    _socket;
    private final InputOutput               _inputOutput;
    private final int                               _resourceIdBase;
    private final int                               _resourceIdMask;
//    private final Vector<Resource>  _resources;
    private int                                             _sequenceNumber = 0;
    private boolean                                 _closeConnection = false;
    private boolean                                 _isConnected = true;
    private int                                             _closeDownMode = Destroy;	
	
	public Client(XServer xserver,Socket socket,int resourceIdBase,int resourceIdMask)  throws IOException {
		System.err.println("Client thread created");
        _xServer = xserver;
        _socket = socket;
        _inputOutput = new InputOutput (socket);
        _resourceIdBase = resourceIdBase;
        _resourceIdMask = resourceIdMask;
//        _resources = new Vector<Resource>();
	}

	@Override
	public void run() {
		System.err.println("Client thread Started");
		try {
			doComms ();
		} catch (IOException e) {
		}

		synchronized (_xServer) {
			close ();
		}
	}

    /**
     * Close the communications thread and free resources.
     */
    private void
    close () {
            if (!_isConnected)
                    return;

            _isConnected = false;

            try {
//                    _inputOutput.close ();
                    _socket.close ();
            } catch (IOException e) {
            }

                    // Clear the resources associated with this client.
/*            
            if (_closeDownMode == Destroy) {
            	for (Resource r: _resources)
            		r.delete ();
            }
             _resources.clear ();
*/ 
 
            _xServer.removeClient (this);
    }	
	
    /**
     * Handle communications with the client.
     * @throws IOException
     */
    private void doComms () throws IOException {
    	// Read the connection setup.
    	int byteOrder = _inputOutput.readByte ();
    	if (byteOrder == 0x42)
    		_inputOutput.setMSB (true);
    	else if (byteOrder == 0x6c)
    		_inputOutput.setMSB (false);
    	else
    		return;
    	
    	_inputOutput.readByte ();       // Unused.
    	_inputOutput.readShort ();      // Protocol major version.
    	_inputOutput.readShort ();      // Protocol minor version.

    	int             nameLength = _inputOutput.readShort ();
    	int             dataLength = _inputOutput.readShort ();

    	_inputOutput.readShort ();      // Unused.

    	if (nameLength > 0) {
    		_inputOutput.readSkip (nameLength);     // Authorization protocol name.
    		_inputOutput.readSkip (-nameLength & 3);        // Padding.
    	}

    	if (dataLength > 0) {
    		_inputOutput.readSkip (dataLength);     // Authorization protocol data.
    		_inputOutput.readSkip (-dataLength & 3);        // Padding.
    	}

    	// Complete the setup.
    	final byte[]    vendor = _xServer.vendor.getBytes ();
    	int                             pad = -vendor.length & 3;
    	int                             extra = 26 + 2 * _xServer.getNumFormats () + (vendor.length + pad) / 4;
//    	Keyboard                kb = _xServer.getKeyboard ();

    	synchronized (_inputOutput) {
                    _inputOutput.writeByte ((byte) 1);              // Success.
                    _inputOutput.writeByte ((byte) 0);              // Unused.
                    _inputOutput.writeShort (_xServer.ProtocolMajorVersion);
                    _inputOutput.writeShort (_xServer.ProtocolMinorVersion);
                    _inputOutput.writeShort ((short) extra);        // Length of data.
                    _inputOutput.writeInt (_xServer.ReleaseNumber); // Release number.
                    _inputOutput.writeInt (_resourceIdBase);
                    _inputOutput.writeInt (_resourceIdMask);
                    _inputOutput.writeInt (0);              // Motion buffer size.
                    _inputOutput.writeShort ((short) vendor.length);        // Vendor length.
                    _inputOutput.writeShort ((short) 0x7fff);       // Max request length.
                    _inputOutput.writeByte ((byte) 1);      // Number of screens.
                    _inputOutput.writeByte ((byte) _xServer.getNumFormats ());
                    _inputOutput.writeByte ((byte) 0);      // Image byte order (0=LSB, 1=MSB).
                    _inputOutput.writeByte ((byte) 1);      // Bitmap bit order (0=LSB, 1=MSB).
                    _inputOutput.writeByte ((byte) 8);      // Bitmap format scanline unit.
                    _inputOutput.writeByte ((byte) 8);      // Bitmap format scanline pad.
                    _inputOutput.writeByte ((byte) 8);		// min key code
                    _inputOutput.writeByte ((byte) 255); 	// max key code
//                    _inputOutput.writeByte ((byte) kb.getMinimumKeycode ());
//                    _inputOutput.writeByte ((byte) kb.getMaximumKeycode ());
                    _inputOutput.writePadBytes (4); // Unused.

                    if (vendor.length > 0) {        // Write padded vendor string.
                            _inputOutput.writeBytes (vendor, 0, vendor.length);
                            _inputOutput.writePadBytes (pad);
                    }

                    _xServer.writeFormats (_inputOutput);
                    _xServer.getScreen().write (_inputOutput);
            }
            _inputOutput.flush ();

            while (!_closeConnection) {
            	byte    opcode = (byte) _inputOutput.readByte ();
            	byte    arg = (byte) _inputOutput.readByte ();
            	int             requestLength = _inputOutput.readShort ();

            	if (requestLength == 0) // Handle big requests.
            		requestLength = _inputOutput.readInt ();

            	// Deal with server grabs.
/*            	
            	while (!_xServer.processingAllowed (this)) {
            		try {
            			sleep (100);
            		} catch (InterruptedException e) {
            		}
            	}
*/
            	synchronized (_xServer) {
            		processRequest (opcode, arg, requestLength * 4 - 4);
            	}
            }
    }
    /**
     * Process a single request from the client.
     * 
     * TODO: build those to abstract and/or interface
     *
     * @param opcode        The request's opcode.
     * @param arg   Optional first argument.
     * @param bytesRemaining        Bytes yet to be read in the request.
     * @throws IOException
     */
    private void
    processRequest (
            byte            opcode,
            byte            arg,
            int                     bytesRemaining
    ) throws IOException {
            _sequenceNumber++;
            switch (opcode) {
                    case RequestCode.CreateWindow:
                    case RequestCode.ChangeWindowAttributes:
                    case RequestCode.GetWindowAttributes:
                    case RequestCode.DestroyWindow:
                    case RequestCode.DestroySubwindows:
                    case RequestCode.ChangeSaveSet:
                    case RequestCode.ReparentWindow:
                    case RequestCode.MapWindow:
                    case RequestCode.MapSubwindows:
                    case RequestCode.UnmapWindow:
                    case RequestCode.UnmapSubwindows:
                    case RequestCode.ConfigureWindow:
                    case RequestCode.CirculateWindow:
                    case RequestCode.QueryTree:
                    case RequestCode.ChangeProperty:
                    case RequestCode.DeleteProperty:
                    case RequestCode.GetProperty:
                    case RequestCode.ListProperties:
                    case RequestCode.QueryPointer:
                    case RequestCode.GetMotionEvents:
                    case RequestCode.TranslateCoordinates:
                    case RequestCode.ClearArea:
                    case RequestCode.ListInstalledColormaps:
                    case RequestCode.RotateProperties:
                    case RequestCode.GetGeometry:
                    case RequestCode.CopyArea:
                    case RequestCode.CopyPlane:
                    case RequestCode.PolyPoint:
                    case RequestCode.PolyLine:
                    case RequestCode.PolySegment:
                    case RequestCode.PolyRectangle:
                    case RequestCode.PolyArc:
                    case RequestCode.FillPoly:
                    case RequestCode.PolyFillRectangle:
                    case RequestCode.PolyFillArc:
                    case RequestCode.PutImage:
                    case RequestCode.GetImage:
                    case RequestCode.PolyText8:
                    case RequestCode.PolyText16:
                    case RequestCode.ImageText8:
                    case RequestCode.ImageText16:
                    case RequestCode.QueryBestSize:
                    case RequestCode.InternAtom:
                    case RequestCode.GetAtomName:
                    case RequestCode.GetSelectionOwner:
                    case RequestCode.SetSelectionOwner:
                    case RequestCode.ConvertSelection:
                    case RequestCode.SendEvent:
                    case RequestCode.GrabPointer:
                    case RequestCode.UngrabPointer:
                    case RequestCode.GrabButton:
                    case RequestCode.UngrabButton:
                    case RequestCode.ChangeActivePointerGrab:
                    case RequestCode.GrabKeyboard:
                    case RequestCode.UngrabKeyboard:
                    case RequestCode.GrabKey:
                    case RequestCode.UngrabKey:
                    case RequestCode.AllowEvents:
                    case RequestCode.SetInputFocus:
                    case RequestCode.GetInputFocus:
                    case RequestCode.GrabServer:
                    case RequestCode.UngrabServer:
                    case RequestCode.WarpPointer:
                    case RequestCode.ChangePointerControl:
                    case RequestCode.GetPointerControl:
                    case RequestCode.SetPointerMapping:
                    case RequestCode.GetPointerMapping:
                    case RequestCode.OpenFont:
                    case RequestCode.CloseFont:
                    case RequestCode.QueryFont:
                    case RequestCode.QueryTextExtents:
                    case RequestCode.ListFonts:
                    case RequestCode.ListFontsWithInfo:
                    case RequestCode.SetFontPath:
                    case RequestCode.GetFontPath:
                    case RequestCode.CreatePixmap:
                    case RequestCode.FreePixmap:
                    case RequestCode.CreateGC:
                    case RequestCode.ChangeGC:
                    case RequestCode.CopyGC:
                    case RequestCode.SetDashes:
                    case RequestCode.SetClipRectangles:
                    case RequestCode.FreeGC:
                    case RequestCode.CreateColormap:
                    case RequestCode.CopyColormapAndFree:
                    case RequestCode.FreeColormap:
                    case RequestCode.InstallColormap:
                    case RequestCode.UninstallColormap:
                    case RequestCode.AllocColor:
                    case RequestCode.AllocNamedColor:
                    case RequestCode.AllocColorCells:
                    case RequestCode.AllocColorPlanes:
                    case RequestCode.FreeColors:
                    case RequestCode.StoreColors:
                    case RequestCode.StoreNamedColor:
                    case RequestCode.QueryColors:
                    case RequestCode.LookupColor:
                    case RequestCode.CreateCursor:
                    case RequestCode.CreateGlyphCursor:
                    case RequestCode.FreeCursor:
                    case RequestCode.RecolorCursor:
                    case RequestCode.QueryExtension:
                    case RequestCode.ListExtensions:
                    case RequestCode.QueryKeymap:
                    case RequestCode.ChangeKeyboardMapping:
                    case RequestCode.GetKeyboardMapping:
                    case RequestCode.ChangeKeyboardControl:
                    case RequestCode.SetModifierMapping:
                    case RequestCode.GetModifierMapping:
                    case RequestCode.GetKeyboardControl:
                    case RequestCode.Bell:
                    case RequestCode.SetScreenSaver:
                    case RequestCode.GetScreenSaver:
                    case RequestCode.ChangeHosts:
                    case RequestCode.ListHosts:
                    case RequestCode.SetAccessControl:
                    case RequestCode.SetCloseDownMode:
                    case RequestCode.KillClient:
                    case RequestCode.ForceScreenSaver:
                    case RequestCode.NoOperation:
                    default:        // Opcode not implemented.
                    		System.err.println("op_code:"+opcode);
                            break;
            }
    }
}
