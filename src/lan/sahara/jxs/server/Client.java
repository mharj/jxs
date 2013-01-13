package lan.sahara.jxs.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;


import lan.sahara.jxs.common.Atom;
import lan.sahara.jxs.common.CloseWindowResponse;
import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.EventCode;
import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.GContext;
import lan.sahara.jxs.common.Geom;
import lan.sahara.jxs.common.Property;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Window;
import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;

public class Client extends Thread {
    public static final int         Destroy = 0;
    public static final int         RetainPermanent = 1;
    public static final int         RetainTemporary = 2;

    private final AbsApiClient				_ourClient;
    private final AbsApiServer				_ourServer;    
    private final XServer                   _xServer;
    private final Socket                    _socket;
    private final InputOutput               _inputOutput;
    private final int                               _resourceIdBase;
    private final int                               _resourceIdMask;
//    private final Vector<Resource>  _resources;
//    private int                                             _sequenceNumber = 0;
    private boolean _closeConnection = false;
    private boolean _isConnected = true;
    private int _closeDownMode = Destroy;	
	
	public Client(AbsApiServer ourServer,AbsApiClient ourClient,XServer xserver,Socket socket,int resourceIdBase,int resourceIdMask)  throws IOException {
		System.err.println("Client thread created");
		_ourServer = ourServer;
		_ourClient = ourClient;
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
    private void close () {
    	if (!_isConnected)
    		return;

    	_isConnected = false;

    	try {
			_inputOutput.close ();
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
            	byte	opcode = (byte) _inputOutput.readByte ();
            	byte	arg = (byte) _inputOutput.readByte ();
            	int		requestLength = _inputOutput.readShort ();

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
    private void processRequest (byte opcode,byte arg,int bytesRemaining) throws IOException {
    		_ourClient._sequenceNumber++;
            switch (opcode) {
                    case RequestCode.CreateWindow:reqCreateWindow(this,bytesRemaining);break;
                    case RequestCode.ChangeWindowAttributes:reqChangeWindowAttributes(this,bytesRemaining);break;
                    case RequestCode.GetWindowAttributes:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.DestroyWindow:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.DestroySubwindows:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ChangeSaveSet:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ReparentWindow:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.MapWindow:reqMapWindow(this,bytesRemaining);break;
                    case RequestCode.MapSubwindows:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UnmapWindow:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UnmapSubwindows:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ConfigureWindow:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CirculateWindow:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryTree:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ChangeProperty:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.DeleteProperty:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetProperty:reqGetProperty(this,false,bytesRemaining);break;
                    case RequestCode.ListProperties:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryPointer:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetMotionEvents:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.TranslateCoordinates:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ClearArea:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ListInstalledColormaps:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.RotateProperties:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetGeometry:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CopyArea:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CopyPlane:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyPoint:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyLine:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolySegment:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyRectangle:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyArc:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.FillPoly:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyFillRectangle:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyFillArc:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PutImage:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetImage:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyText8:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.PolyText16:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ImageText8:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ImageText16:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryBestSize:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.InternAtom:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetAtomName:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetSelectionOwner:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetSelectionOwner:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ConvertSelection:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SendEvent:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GrabPointer:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UngrabPointer:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GrabButton:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UngrabButton:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ChangeActivePointerGrab:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GrabKeyboard:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UngrabKeyboard:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GrabKey:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UngrabKey:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.AllowEvents:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetInputFocus:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetInputFocus:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GrabServer:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UngrabServer:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.WarpPointer:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ChangePointerControl:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetPointerControl:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetPointerMapping:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetPointerMapping:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.OpenFont:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CloseFont:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryFont:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryTextExtents:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ListFonts:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ListFontsWithInfo:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetFontPath:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetFontPath:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CreatePixmap:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.FreePixmap:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CreateGC:reqCreateGC(this,bytesRemaining);break;
                    case RequestCode.ChangeGC:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CopyGC:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetDashes:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetClipRectangles:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.FreeGC:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CreateColormap:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CopyColormapAndFree:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.FreeColormap:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.InstallColormap:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.UninstallColormap:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.AllocColor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.AllocNamedColor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.AllocColorCells:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.AllocColorPlanes:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.FreeColors:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.StoreColors:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.StoreNamedColor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryColors:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.LookupColor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CreateCursor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.CreateGlyphCursor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.FreeCursor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.RecolorCursor:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryExtension:reqQueryExtension(this,bytesRemaining);break;
                    case RequestCode.ListExtensions:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.QueryKeymap:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ChangeKeyboardMapping:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetKeyboardMapping:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ChangeKeyboardControl:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetModifierMapping:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetModifierMapping:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetKeyboardControl:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.Bell:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetScreenSaver:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.GetScreenSaver:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ChangeHosts:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ListHosts:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetAccessControl:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.SetCloseDownMode:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.KillClient:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.ForceScreenSaver:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    case RequestCode.NoOperation:System.err.println("op_code:"+opcode+" size:"+bytesRemaining);break;
                    default:        // Opcode not implemented.
                    		System.err.println("op_code:"+opcode+" size:"+bytesRemaining);
                            break;
            }
    }
    

    /*
     * Protocol in bottom of 
     * http://www.x.org/releases/X11R7.5/doc/x11proto/proto.pdf
     */
    
    // parsing methods
    private void reqMapWindow(Client client,int bytesRemaining) throws IOException {
		if (bytesRemaining < 4) {
			ErrorCode.write (this, ErrorCode.Length, RequestCode.MapWindow, 0);
		} else {
			int			id = _inputOutput.readInt ();
			Resource	r = _ourServer.getResource (id);
			bytesRemaining -= 4;
			System.err.println("req: MapWindow(ID:"+id+")  BYTES REMAINING:"+bytesRemaining);
			if (r == null || r.getType () != Resource.WINDOW) {
				_inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (this, ErrorCode.Window, RequestCode.MapWindow, id);
			} else {
				
//				r.processRequest (this, opcode, arg, bytesRemaining);
			}
		}	
    }
    
    

    
    private void reqChangeWindowAttributes(Client client,int bytesRemaining) throws IOException {
    	System.err.println("req: ChangeWindowAttributes");
    	if (bytesRemaining < 4) {
			ErrorCode.write (this, ErrorCode.Length, RequestCode.ChangeWindowAttributes, 0);
		} else {
			int			id = _inputOutput.readInt ();
			Resource	r = _ourServer.getResource (id);
			System.err.println("req: ChangeWindowAttributes [WindowId:"+id+"]");
			bytesRemaining -= 4;
			if (r == null || r.getType () != Resource.WINDOW) {
				_inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (this, ErrorCode.Window, RequestCode.ChangeWindowAttributes, id);
			} else {
				int	valueMask = _inputOutput.readInt ();	// Value mask.

			}
		}
    }
    
    private void reqCreateWindow(Client client,int bytesRemaining) throws IOException {
    	if (bytesRemaining < 28) {
			_inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (this, ErrorCode.Length, RequestCode.CreateWindow, 0);
		} else {
			int			window_id = _inputOutput.readInt ();	// Window ID.
			int			parent_id = _inputOutput.readInt ();	// Parent.
			if ( parent_id > 1 ) // TODO: change ResourceManager!!
				parent_id -=1;
			Resource	parent_resource = _ourServer.getResource (parent_id);
			System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+")");
			bytesRemaining -= 8;
			
			// TODO: check mask thing like in AbsApiServer.validResourceId
			if ( _ourServer.resourceExists(window_id) ) {
				System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") NOT VALID SOURCE");
				_inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (this, ErrorCode.IDChoice, RequestCode.CreateWindow,window_id);
			} else if (parent_resource == null || parent_resource.getType () != Resource.WINDOW) {
				System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") NOT VALID SOURCE NULL or NOT WINDOW");
				_inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (this, ErrorCode.Window, RequestCode.CreateWindow,parent_id);
			} else {
				try {
					System.err.println("BYTES REMAINING:"+bytesRemaining);
					Window parent_window = (Window) parent_resource;
					Geom geom = new Geom();
					geom.setX( _inputOutput.readShort () );	// X position.
					geom.setY( _inputOutput.readShort () );	// Y position.
					geom.setW( _inputOutput.readShort () );	// W size.
					geom.setH( _inputOutput.readShort () );	// H size.
					bytesRemaining -= 8;
					System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") GEOM POS AND SIZE ok "+geom.toString()+", BYTES REMAINING:"+bytesRemaining);
					int				borderWidth = _inputOutput.readShort ();	// Border width.
					bytesRemaining -= 2;
					System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") GEOM borderWidth, BYTES REMAINING:"+bytesRemaining);
					int				wclass = _inputOutput.readShort ();	// Window class.
					bytesRemaining -= 2;
					System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") GEOM Window class, BYTES REMAINING:"+bytesRemaining);
					int visual = _inputOutput.readInt ();	// Visual.
					bytesRemaining -= 4;
					System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") GEOM visual, BYTES REMAINING:"+bytesRemaining);
				
//				bytesRemaining -= 16;
				boolean			inputOnly;
				if (wclass == 0)	// Copy from parent.
					inputOnly = parent_window.isInputOnly();
				else if (wclass == 1)	// Input/output.
					inputOnly = false;
				else
					inputOnly = true;
				// create window
				System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") CREATE Window()");
				Window			w = null;
				try {
					w = new Window (window_id,_ourServer,_ourClient,parent_window,geom,borderWidth,inputOnly,false);
				} catch (OutOfMemoryError e) {
					_inputOutput.readSkip (bytesRemaining);
					ErrorCode.write (client, ErrorCode.Alloc,RequestCode.CreateWindow, 0);
					return;
				}

				if (bytesRemaining < 4) {
					_inputOutput.readSkip (bytesRemaining);
					ErrorCode.write (client, ErrorCode.Length, RequestCode.CreateWindow, 0);
					return;
				}

				int			valueMask = _inputOutput.readInt ();	// Value mask.
				bytesRemaining -= 4;
				System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") valueMask, BYTES REMAINING:"+bytesRemaining);
				int			n = Util.bitcount (valueMask);

				
				if (bytesRemaining != n * 4) {
					_inputOutput.readSkip (bytesRemaining);
					ErrorCode.write (client, ErrorCode.Length, RequestCode.CreateWindow, 0);
					return;
				}
				System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") value-list size:"+(n*4)+"");
				for (int i = 0; i < 15; i++) {
					if ((valueMask & (1 << i)) != 0) {
						System.err.println("req: CreateWindow(ID:"+window_id+" Parent:"+parent_id+") Value Type:"+i);
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
								w.setAttribute(i, _inputOutput.readInt() );
								break;
							case Window.AttrBitGravity:
							case Window.AttrWinGravity:
							case Window.AttrBackingStore:
							case Window.AttrOverrideRedirect:
							case Window.AttrSaveUnder:
								w.setAttribute(i, _inputOutput.readByte() );
								_inputOutput.readSkip (3);
								break;
						}
					}
				}
				valueMask = 0xffffffff; // for new window
				
				w.applyValues (client, RequestCode.CreateWindow, valueMask);
				
				_ourServer.addResource(w);
				parent_window.addChildren(window_id);
				
				// push to client api
				_ourClient.clientCreateWindow(w,geom);
/*
				
					return false;

				w._drawable.clear ();

				_xServer.addResource (w);
				client.addResource (w);
				_children.add (w);
*/
				// TODO fix getSelectingClients and w.applyValues !!!
				EventCode.sendCreateNotify (this, parent_window, w, geom.getX(), geom.getY(), geom.getW(), geom.getH(),borderWidth, w.getOverrideRedirect());
				
				Vector<Client>		sc = w.getSelectingClients (EventCode.MaskSubstructureNotify);
				if (sc != null) {
					for (Client c: sc) {
						EventCode.sendCreateNotify (c, parent_window, w, geom.getX(), geom.getY(), geom.getW(), geom.getH(),borderWidth, w.getOverrideRedirect());
					}
				}
				
				} catch (IOException e ) {
					System.err.println("Caught IOException: " + e.getMessage());
				}
			}
		}    	
    }
    
    /**
     * The XCreateGC() function creates a graphics context and returns a GC
     * op_code: 55 
     * TODO: check GContext.java for parser and build similar with logging and pass to interface
     * @param client
     * @param bytesRemaining
     * @throws IOException
     */
    private void reqCreateGC(Client client,int bytesRemaining) throws IOException {
    	
        if (bytesRemaining < 12) {
        	_inputOutput.readSkip (bytesRemaining);
            ErrorCode.write (this, ErrorCode.Length, RequestCode.CreateGC, 0);
        } else {
            int	cid = 	 _inputOutput.readInt ();  		 // GContext ID.
            int	drawable = _inputOutput.readInt ();		 // Drawable ID.
            
            bytesRemaining -= 8;
            System.err.println("req: CreateGC(ID:"+cid+" Parent:"+drawable+")");
            
            
            Resource r = _ourServer.getResource (drawable);
            if (! _ourServer.validResourceId (cid,_ourClient) ) {
            	_inputOutput.readSkip (bytesRemaining);
                ErrorCode.write (this, ErrorCode.IDChoice, RequestCode.CreateGC, cid);
            	
            } else if (r == null || ! r.isDrawable ()) {
                _inputOutput.readSkip (bytesRemaining);
                ErrorCode.write (this, ErrorCode.Drawable, RequestCode.CreateGC, drawable);            	
            } else {
            	GContext gc = new GContext(cid, _ourServer,_ourClient);
            	if ( processGCValues(gc,bytesRemaining) == true ) {
            		_ourServer.addResource(gc);
            		
//					TODO: client should not care?            		
//            		_ourClient.addResource(gc); 	
            	} else {
            		System.err.println("Error in parsing!");
            	}
            }
        }    	
    }
    
    private void reqGetProperty(Client client,boolean delete,int bytesRemaining) throws IOException {
    	bytesRemaining -= 4;
    	if (bytesRemaining  != 16 ) {
    		_inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (this, ErrorCode.Length, RequestCode.GetProperty, 0);
			return;
		} else {
			int			window_id = _inputOutput.readInt ();	// Property.
			int			pid = _inputOutput.readInt ();	// Property.
			int			tid = _inputOutput.readInt ();	// Type.
			int			longOffset = _inputOutput.readInt ();	// Long offset.
			int			longLength = _inputOutput.readInt ();	// Long length.
			Atom		property = _ourServer.getAtom (pid);

			if (property == null) {
				ErrorCode.write (client, ErrorCode.Atom,RequestCode.GetProperty, pid);
				return;
			} else if (tid != 0 && ! _ourServer.atomExists (tid)) {
				ErrorCode.write (client, ErrorCode.Atom,RequestCode.GetProperty, tid);
				return;
			}
			System.err.println("Window:"+window_id+" Property:"+pid+" Type:"+tid);
			Window w = (Window) _ourServer.getResource(window_id);
			System.err.println("longOffset:"+longOffset+" longLength:"+longLength+"");
			byte		format = 0;
			int			bytesAfter = 0;
			byte[]		value = null;
			boolean		generateNotify = false;

			if ( _ourClient.properties.containsKey (pid)) {
				Property	p = _ourClient.properties.get(pid);

				tid = p._type;
				format = p._format;

				if (tid != 0 && tid != p._type) {
					bytesAfter = (p._data == null) ? 0 : p._data.length;
				} else {
					int		n, i, t, l;

					n = (p._data == null) ? 0 : p._data.length;
					i = 4 * longOffset;
					t = n - i;

					if (longLength < 0 || longLength > 536870911)
						longLength = 536870911;	// Prevent overflow.

					if (t < longLength * 4)
						l = t;
					else
						l = longLength * 4;

					bytesAfter = n - (i + l);

					if (l < 0) {
						ErrorCode.write (client, ErrorCode.Value,
													RequestCode.GetProperty, 0);
						return;
					}

					if (l > 0) {
						value = new byte[l];
						System.arraycopy (p._data, i, value, 0, l);
					}

					if (delete && bytesAfter == 0) {
						_ourClient.properties.remove (pid);
						generateNotify = true;
					}
				}
			} else {
				tid = 0;
			}

			int			length = (value == null) ? 0 : value.length;
			int			pad = -length & 3;
			int			valueLength;

			if (format == 8)
				valueLength = length;
			else if (format == 16)
				valueLength = length / 2;
			else if (format == 32)
				valueLength = length / 4;
			else
				valueLength = 0;

			synchronized (_inputOutput) {
				Util.writeReplyHeader (client, format);
				_inputOutput.writeInt ((length + pad) / 4);	// Reply length.
				_inputOutput.writeInt (tid);	// Type.
				_inputOutput.writeInt (bytesAfter);	// Bytes after.
				_inputOutput.writeInt (valueLength);	// Value length.
				_inputOutput.writePadBytes (12);	// Unused.

				if (value != null) {
					_inputOutput.writeBytes (value, 0, value.length);	// Value.
					_inputOutput.writePadBytes (pad);	// Unused.
				}
			}
			_inputOutput.flush ();

			if (generateNotify) {
				Vector<Client>		sc;
				if ((sc = w.getSelectingClients (EventCode.MaskPropertyChange)) != null) {
					for (Client c: sc)
						EventCode.sendPropertyNotify (c, w, property,_ourServer.getTimestamp (), 1);
				}
			}			
		}
    }
    
    /**
     * The XQueryExtension() function determines if the named extension is present
     * op_code: 98
     * 
     * TODO: cleanup!!!
     * TODO: names like in X documentation
     *  
     * @param client
     * @param bytesRemaining
     * @throws IOException
     */
    private void reqQueryExtension(Client client,int bytesRemaining) throws IOException {
    	if (bytesRemaining < 4) {
    		System.err.println("error in query ext data");
    		_inputOutput.readSkip (bytesRemaining);
    		ErrorCode.write (client, ErrorCode.Length,RequestCode.QueryExtension, 0);
            return;
    	}
    	int name_length = _inputOutput.readShort ();       // Length of name.
    	int pad = -name_length & 3;
    	_inputOutput.readSkip (2);        // Unused.
    	bytesRemaining -= 4;
    	
    	if (bytesRemaining != name_length + pad) {
    		_inputOutput.readSkip (bytesRemaining);
    		ErrorCode.write (client, ErrorCode.Length,RequestCode.QueryExtension, 0);
    		return;
    	}
    	// read string
    	byte[]          bytes = new byte[name_length];
    	_inputOutput.readBytes(bytes, 0, name_length);
    	_inputOutput.readSkip (pad);      // Unused.

    	String          s = new String (bytes);
    	System.err.println("req: QueryExtension("+s+")");
    	Extension e =_ourServer.queryExtension(s); // ask from ApiServer (not client!)
    	synchronized (_inputOutput) {
            Util.writeReplyHeader (client, (byte) 0);
            _inputOutput.writeInt (0);        // Reply length.
            if (e == null) {
            	_inputOutput.writeByte ((byte) 0);        // Present. 0 = false.
            	_inputOutput.writeByte ((byte) 0);        // Major opcode.
            	_inputOutput.writeByte ((byte) 0);        // First event.
            	_inputOutput.writeByte ((byte) 0);        // First error.
            } else {
            	_inputOutput.writeByte ((byte) 1);        // Present. 1 = true.
            	_inputOutput.writeByte (e.getMajorOpcode());   // Major opcode.
            	_inputOutput.writeByte (e.getFirstEvent());    // First event.
            	_inputOutput.writeByte (e.getFirstError());    // First error.
            }
            _inputOutput.writePadBytes (20);  // Unused.
    	}
    	_inputOutput.flush ();  
    	System.err.println("rep: QueryExtension("+s+") ret:"+e);
    }

    /**
     * used in Util.java
     * @return
     */
    public InputOutput getInputOutput () {
    	return _inputOutput;
    }
    
    /**
     * used in Util.java
     * @return
     */
    public int getSequenceNumber () {
    	return _ourClient._sequenceNumber;
    }

   
	private boolean validResourceId (int id) {
		return ((id & ~_resourceIdMask) == _resourceIdBase && ! _ourServer.resourceExists(id) );
	}

    
    /**
	 * Process a list of GContext attribute values.
	 *
	 * @param client	The remote client.
	 * @param opcode	The opcode being processed.
	 * @param bytesRemaining	Bytes yet to be read in the request.
	 * @return	True if the values are all valid.
	 * @throws IOException
	 */
//	private boolean processGCValues (Client client,byte opcode,int bytesRemaining) throws IOException {
	private boolean processGCValues (GContext gc,int bytesRemaining) throws IOException {		

		if (bytesRemaining < 4) {
			_inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (this, ErrorCode.Length, RequestCode.CreateGC, 0);
			return false;
		}
		int			valueMask = _inputOutput.readInt ();	// Value mask.
		int			n = Util.bitcount (valueMask);
		bytesRemaining -= 4;
		if (bytesRemaining != n * 4) {
			_inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (this, ErrorCode.Length, RequestCode.CreateGC, 0);
			return false;
		}
		for (int i = 0; i < 23; i++)
			if ((valueMask & (1 << i)) != 0)
				processGCValue (gc, i);
		return true;
	}
	/**
	 * Process a single GContext attribute value.
	 *
	 * @param io	The input/output stream.
	 * @param maskBit	The mask bit of the attribute.
	 * @throws IOException
	 */
	private void processGCValue (GContext gc,int maskBit) throws IOException {
		switch (maskBit) {
			case GContext.AttrFunction:
			case GContext.AttrLineStyle:
			case GContext.AttrCapStyle:
			case GContext.AttrJoinStyle:
			case GContext.AttrFillStyle:
			case GContext.AttrFillRule:
			case GContext.AttrSubwindowMode:
			case GContext.AttrGraphicsExposures:
			case GContext.AttrDashes:
			case GContext.AttrArcMode:
				gc.setAttribute(maskBit, _inputOutput.readByte() );
				_inputOutput.readSkip(3);
				break;
			case GContext.AttrPlaneMask:
			case GContext.AttrForeground:
			case GContext.AttrBackground:
			case GContext.AttrTile:
			case GContext.AttrStipple:
			case GContext.AttrFont:
			case GContext.AttrClipMask:
				gc.setAttribute(maskBit, _inputOutput.readInt() );
				break;
			case GContext.AttrLineWidth:
			case GContext.AttrDashOffset:
				gc.setAttribute(maskBit, _inputOutput.readShort() );
				_inputOutput.readSkip (2);
				break;
			case GContext.AttrTileStippleXOrigin:
			case GContext.AttrTileStippleYOrigin:
			case GContext.AttrClipXOrigin:
			case GContext.AttrClipYOrigin:
				gc.setAttribute(maskBit, (short) _inputOutput.readShort() );
				_inputOutput.readSkip (2);
				break;
		}
	}

}
