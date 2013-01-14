package lan.sahara.jxs.server;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import org.apache.log4j.Logger;


import lan.sahara.jxs.common.Atom;
import lan.sahara.jxs.common.CloseWindowResponse;
import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.EventCode;
import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.GContext;
import lan.sahara.jxs.common.Property;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Window;
import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.protocol.CreateGC;
import lan.sahara.jxs.protocol.GetProperty;
import lan.sahara.jxs.protocol.QueryExtension;

public class Client extends Thread {
	static Logger logger = Logger.getLogger(Client.class.getName());
    public static final int         Destroy = 0;
    public static final int         RetainPermanent = 1;
    public static final int         RetainTemporary = 2;

    private final AbsApiClient				_ourClient;
    private final AbsApiServer				_ourServer;    
    private final XServer                   _xServer;
    private final Socket                    _socket;
    private final InputOutput               _inputOutput;
//    private final Vector<Resource>  _resources;

    private boolean _closeConnection = false;
    private boolean _isConnected = true;
    private int _closeDownMode = Destroy;	
	
	public Client(AbsApiServer ourServer,AbsApiClient ourClient,XServer xserver,Socket socket,int resourceIdBase,int resourceIdMask)  throws IOException {
		logger.info("constructor("+socket.getRemoteSocketAddress().toString()+")");
		_ourServer = ourServer;
		_ourServer.setResourceBaseMask(resourceIdBase,resourceIdMask);
		_ourClient = ourClient;
        _xServer = xserver;
        _socket = socket;
        _inputOutput = new InputOutput (socket);
//        _resources = new Vector<Resource>();
	}

	@Override
	public void run() {
		logger.info("run()");
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
    	logger.info("close()");
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
                    _inputOutput.writeInt (_ourServer._resourceIdBase);
                    _inputOutput.writeInt (_ourServer._resourceIdMask);
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
//                   _inputOutput.writeByte ((byte) kb.getMinimumKeycode ());
//                   _inputOutput.writeByte ((byte) kb.getMaximumKeycode ());
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
    		_ourServer._sequenceNumber++;
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
                    case RequestCode.GetProperty:
                    	GetProperty.query(arg, bytesRemaining, _inputOutput, _ourServer._sequenceNumber, _ourServer, _ourClient.properties);
                    	break;
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
                    case RequestCode.CreateGC:
                    	GContext gcontext = CreateGC.query(arg, bytesRemaining, _inputOutput, _ourServer._sequenceNumber, _ourServer, _ourClient);
                    	if ( gcontext != null) {
                    		_ourServer.addResource(gcontext);
                    		_ourClient.addResource(gcontext);
                    	}
//                    	reqCreateGC(this,bytesRemaining);
                    	break;
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
                    case RequestCode.QueryExtension:
                    	String extension = QueryExtension.query(arg,bytesRemaining,_inputOutput,_ourServer._sequenceNumber);
                    	if ( extension != null ) {
                    		Extension e =_ourServer.queryExtension(extension);
                    		QueryExtension.reply(e, _inputOutput, _ourServer._sequenceNumber);
                    	}
                    break;
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
				Window w = (Window) r;
				int	valueMask = _inputOutput.readInt ();	// Value mask.
				bytesRemaining -= 4;
				int			n = Util.bitcount (valueMask);
				if (bytesRemaining != n * 4) {
					_inputOutput.readSkip (bytesRemaining);
					ErrorCode.write (client, ErrorCode.Length, RequestCode.ChangeWindowAttributes, 0);
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
			
			bytesRemaining -= 8;
			
			// TODO: check mask thing like in AbsApiServer.validResourceId
			if ( _ourServer.resourceExists(window_id) ) {
				_inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (this, ErrorCode.IDChoice, RequestCode.CreateWindow,window_id);
			} else if (parent_resource == null || parent_resource.getType () != Resource.WINDOW) {
				_inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (this, ErrorCode.Window, RequestCode.CreateWindow,parent_id);
			} else {
				try {
					Window parent_window = (Window) parent_resource;
					Rectangle geom = new Rectangle();
					geom.x = _inputOutput.readShort ();	// X position.
					geom.y = _inputOutput.readShort ();	// Y position.
					geom.width = _inputOutput.readShort ();	// W size.
					geom.height =  _inputOutput.readShort ();	// H size.
					bytesRemaining -= 8;
					int				borderWidth = _inputOutput.readShort ();	// Border width.
					bytesRemaining -= 2;
					int				wclass = _inputOutput.readShort ();	// Window class.
					bytesRemaining -= 2;
					int visual = _inputOutput.readInt ();	// Visual.
					bytesRemaining -= 4;

					boolean			inputOnly;
					if (wclass == 0)	// Copy from parent.
						inputOnly = parent_window.isInputOnly();
					else if (wclass == 1)	// Input/output.
						inputOnly = false;
					else
						inputOnly = true;
					
					System.err.println("Request: CreateWindow(ID:"+window_id+" Parent:"+parent_id+" "+geom.toString()+")");
					// TODO: check and add all values to Window !!!
					// TODO: create "Window" first and directly add values to Class instead of monster constructor !!
					// create window
					Window			w = null;
					try {
						w = new Window (window_id,parent_window,geom,borderWidth,inputOnly,false);
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
					int			n = Util.bitcount (valueMask);
					if (bytesRemaining != n * 4) {
						_inputOutput.readSkip (bytesRemaining);
						ErrorCode.write (client, ErrorCode.Length, RequestCode.CreateWindow, 0);
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
					_ourClient.addResource(w);
					parent_window.addChildren(window_id);
				
					// push to client api
//					_ourClient.clientCreateWindow(w,geom);
/*
				
					return false;

				w._drawable.clear ();

				_xServer.addResource (w);
				client.addResource (w);
				_children.add (w);
*/
					// TODO fix getSelectingClients and w.applyValues !!!
					EventCode.sendCreateNotify (this, parent_window, w, geom.x, geom.y, geom.width, geom.height,borderWidth, w.getOverrideRedirect());
				
					Vector<Client>		sc = w.getSelectingClients (EventCode.MaskSubstructureNotify);
					if (sc != null) {
						for (Client c: sc) {
							EventCode.sendCreateNotify (c, parent_window, w,geom.x, geom.y, geom.width, geom.height,borderWidth, w.getOverrideRedirect());
						}
					}
				
				} catch (IOException e ) {
					System.err.println("Caught IOException: " + e.getMessage());
				}
			}
		}    	
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
     * TODO: remove
     * @return
     */
    
    public int getSequenceNumber () {
    	return _ourServer._sequenceNumber;
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
