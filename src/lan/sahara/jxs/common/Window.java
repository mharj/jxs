package lan.sahara.jxs.common;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;


import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.Client;

public class Window extends Resource {

	private Window _parent;	
	private int _eventMask = 0;
	private final Hashtable<Client, Integer> _clientMasks;
	private final Vector<Integer>	_children;
	private final Hashtable<Integer, Property>	_properties;	
	
	private boolean _overrideRedirect;
	public final boolean _inputOnly;
	private final int _borderWidth;
	private final boolean _isRoot;
	private boolean _isMapped = false;
	
	private final Rectangle _geom;
	

	private int[] _attributes;

	private static final int Unobscured = 0;
	private static final int PartiallyObscured = 1;
	private static final int FullyObscured = 2;
	private static final int NotViewable = 3;

	public static final int AttrBackgroundPixmap = 0;
	public static final int AttrBackgroundPixel = 1;
	public static final int AttrBorderPixmap = 2;
	public static final int AttrBorderPixel = 3;
	public static final int AttrBitGravity = 4;
	public static final int AttrWinGravity = 5;
	public static final int AttrBackingStore = 6;
	public static final int AttrBackingPlanes = 7;
	public static final int AttrBackingPixel = 8;
	public static final int AttrOverrideRedirect = 9;
	public static final int AttrSaveUnder = 10;
	public static final int AttrEventMask = 11;
	public static final int AttrDoNotPropagateMask = 12;
	public static final int AttrColormap = 13;
	public static final int AttrCursor = 14;

	private static final int WinGravityUnmap = 0;
	private static final int WinGravityNorthWest = 1;
	private static final int WinGravityNorth = 2;
	private static final int WinGravityNorthEast = 3;
	private static final int WinGravityWest = 4;
	private static final int WinGravityCenter = 5;
	private static final int WinGravityEast = 6;
	private static final int WinGravitySouthWest = 7;
	private static final int WinGravitySouth = 8;
	private static final int WinGravitySouthEast = 9;
	private static final int WinGravityStatic = 10;

	/**
	 * @param resource_id Resource ID
	 * @param ourServer Current Server
	 * @param ourClient Current Client socket
	 * @param parent Parent Resource ID
	 * @param geom Rectangle geometry
	 * @param borderWidth Border Width
	 * @param inputOnly Input only
	 * @param isRoot Is Root Window
	 */
	public Window(Integer resource_id,Window parent, Rectangle geom, int borderWidth, boolean inputOnly, boolean isRoot) {
		super(Resource.WINDOW, resource_id);

		_parent = parent;
		_inputOnly = inputOnly;
		_borderWidth = borderWidth;
		_isRoot = isRoot;
		_geom = geom;
		_attributes = new int[] { 0, // background-pixmap = None
				0, // background-pixel = zero
				0, // border-pixmap = CopyFromParent
				0, // border-pixel = zero
				0, // bit-gravity = Forget
				WinGravityNorthWest, // win-gravity = NorthWest
				0, // backing-store = NotUseful
				0xffffffff, // backing-planes = all ones
				0, // backing-pixel = zero
				0, // override-redirect = False
				0, // save-under = False
				0, // event-mask = empty set
				0, // do-not-propogate-mask = empty set
				0, // colormap = CopyFromParent
				0 // cursor = None
		};
		if (isRoot) {
			_attributes[AttrBackgroundPixel] = 0xffc0c0c0;
			_isMapped = true;
//			_cursor = (Cursor) _xServer.getResource (2);	// X cursor.
		}
		
		_children = new Vector<Integer>();
		_properties = new Hashtable<Integer, Property> ();
//		_passiveButtonGrabs = new HashSet<PassiveButtonGrab>();
//		_passiveKeyGrabs = new HashSet<PassiveKeyGrab>();
		_clientMasks = new Hashtable<Client, Integer>();
//		_shapeSelectInput = new Vector<Client> ();		
	}

	/**
	 * Return the window's cumulative event mask.
	 * 
	 * @return The window's event mask.
	 */
	public int getEventMask() {
		return _eventMask;
	}

	/**
	 * Return the list of clients selecting on the events.
	 * 
	 * @param mask The event mask.
	 * @return List of clients, or null if none selecting.
	 */
	public Vector<Client> getSelectingClients(int mask) {
		if ((mask & _eventMask) == 0)
			return null;
		Vector<Client> rc = new Vector<Client>();
		Set<Client> sc = _clientMasks.keySet();
		for (Client c : sc)
			if ((_clientMasks.get(c) & mask) != 0)
				rc.add(c);

		return rc;
	}

	public boolean applyValues(Client client, byte opcode, int mask) throws IOException {
		boolean ok = true;
/*		
		if ((mask & (1 << AttrBackgroundPixmap)) != 0) {
			int pmid = _attributes[AttrBackgroundPixmap];
			if (pmid == 0) { // None.
				_backgroundBitmap = null;
				_drawable.setBackgroundBitmap(null);
			} else if (pmid == 1) { // ParentRelative.
				_backgroundBitmap = _parent._backgroundBitmap;
				_attributes[AttrBackgroundPixel] = _parent._attributes[AttrBackgroundPixel];
				_drawable.setBackgroundBitmap(_backgroundBitmap);
				_drawable.setBackgroundColor(_attributes[AttrBackgroundPixel] | 0xff000000);
			} else {
				Resource r = _xServer.getResource(pmid);

				if (r != null && r.getType() == Resource.PIXMAP) {
					Pixmap p = (Pixmap) r;
					Drawable d = p.getDrawable();

					_backgroundBitmap = d.getBitmap();
					_drawable.setBackgroundBitmap(_backgroundBitmap);
				} else {
					ErrorCode.write(client, ErrorCode.Colormap, opcode, pmid);
					ok = false;
				}
			}
		}

		if ((mask & (1 << AttrBackgroundPixel)) != 0)
			_drawable.setBackgroundColor(_attributes[AttrBackgroundPixel] | 0xff000000);

		if ((mask & (1 << AttrColormap)) != 0) {
			int cid = _attributes[AttrColormap];

			if (cid != 0) {
				Resource r = _xServer.getResource(cid);

				if (r != null && r.getType() == Resource.COLORMAP) {
					_colormap = (Colormap) r;
				} else {
					ErrorCode.write(client, ErrorCode.Colormap, opcode, cid);
					ok = false;
				}
			} else if (_parent != null) {
				_colormap = _parent._colormap;
			}
		}
*/
		if ((mask & (1 << AttrEventMask)) != 0) {
			_clientMasks.put(client, _attributes[AttrEventMask]);
//			System.err.println("for Client store AttrEventMask = " + _attributes[AttrEventMask]);
			Set<Client> sc = _clientMasks.keySet();

			_eventMask = 0;
			for (Client c : sc)
				_eventMask |= _clientMasks.get(c);
		}
/*
		if ((mask & (1 << AttrOverrideRedirect)) != 0)
			_overrideRedirect = (_attributes[AttrOverrideRedirect] == 1);

		if ((mask & (1 << AttrCursor)) != 0) {
			int cid = _attributes[AttrCursor];

			if (cid != 0) {
				Resource r = _xServer.getResource(cid);

				if (r != null && r.getType() == Resource.CURSOR) {
					_cursor = (Cursor) r;
				} else {
					ErrorCode.write(client, ErrorCode.Cursor, opcode, cid);
					ok = false;
				}
			} else {
				_cursor = null;
			}
		}
*/
		return ok;
	}

	public boolean isInputOnly() {
		return _inputOnly;
	}

	public void setAttribute(int type, int value) {
//		System.out.println("Window.setAttribute "+getAttributeName(type)+"="+value);
		_attributes[type] = value;
	}
	public int getAttribute(int type) {
		return _attributes[type];
	}

	public boolean getOverrideRedirect() {
		return _overrideRedirect;
	}
	public String getAttributeName(int typeId) {
		switch (typeId) {
			case AttrBackgroundPixmap:		return "background-pixmap";
			case AttrBackgroundPixel:		return "background-pixel";
			case AttrBorderPixmap:			return "border-pixmap";
			case AttrBorderPixel:			return "border-pixel";
			case AttrBitGravity:			return "bit-gravity";
			case AttrWinGravity:			return "win-gravity";
			case AttrBackingStore:			return "backing-store";
			case AttrBackingPlanes:			return "backing-planes";
			case AttrBackingPixel:			return "backing-pixel";
			case AttrOverrideRedirect:		return "override-redirect";
			case AttrSaveUnder:				return "save-under";
			case AttrEventMask:				return "event-mask";
			case AttrDoNotPropagateMask:	return "do-not-propagate-mask";
			case AttrColormap:				return "colormap";
			case AttrCursor:				return "cursor";
		}
		return null;
	}
	public String getMaskNameValues() {
		List<String> names = new LinkedList<String>();
		for ( int i = 0;i<14;i++) {
			names.add(getAttributeName(i)+"="+_attributes[i]);
		}
		return names.toString();
	}

	public void addChildren(int window_id) {
		_children.add(window_id);
	}

	public Rectangle getGeometry() {
		return _geom;
	}
	
}