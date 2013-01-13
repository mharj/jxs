package lan.sahara.jxs.common;

import java.io.IOException;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.Client;
import lan.sahara.jxs.server.XServer;



public class GContext extends Resource {
	private int[]			_attributes;
	
	public static final int	AttrFunction = 0;
	public static final int	AttrPlaneMask = 1;
	public static final int	AttrForeground = 2;
	public static final int	AttrBackground = 3;
	public static final int	AttrLineWidth = 4;
	public static final int	AttrLineStyle = 5;
	public static final int	AttrCapStyle = 6;
	public static final int	AttrJoinStyle = 7;
	public static final int	AttrFillStyle = 8;
	public static final int	AttrFillRule = 9;
	public static final int	AttrTile = 10;
	public static final int	AttrStipple = 11;
	public static final int	AttrTileStippleXOrigin = 12;
	public static final int	AttrTileStippleYOrigin = 13;
	public static final int	AttrFont = 14;
	public static final int	AttrSubwindowMode = 15;
	public static final int	AttrGraphicsExposures = 16;
	public static final int	AttrClipXOrigin = 17;
	public static final int	AttrClipYOrigin = 18;
	public static final int	AttrClipMask = 19;
	public static final int	AttrDashOffset = 20;
	public static final int	AttrDashes = 21;
	public static final int	AttrArcMode = 22;

	/**
	 * Process a CreateGC request.
	 *
	 * @param xServer	The X server.
	 * @param client	The client issuing the request.
	 * @param id	The ID of the GContext to create.
	 * @param bytesRemaining	Bytes yet to be read in the request.
	 * @throws IOException
	 */	
	public GContext(Integer resource_id,AbsApiServer ourServer,AbsApiClient ourClient) {
		super(Resource.GCONTEXT,resource_id,ourServer, ourClient);
//		super(type, id, xServer);
		_attributes = new int[] {
				3,	// function = Copy
				0xffffffff,	// plane-mask = all ones
				0,	// foreground = 0
				1,	// background = 1
				0,	// line-width = 0
				0,	// line-style = Solid
				1,	// cap-style = Butt
				0,	// join-style = Miter
				0,	// fill-style = Solid
				0,	// fill-rule = EvenOdd
				0,	// tile = foreground-filled pixmap
				0,	// stipple = pixmap filled with ones
				0,	// tile-stipple-x-origin = 0
				0,	// tile-stipple-y-origin = 0
				0,	// font = server-dependent
				0,	// subwindow-mode = ClipByChildren
				1,	// graphics-exposures = True
				0,	// clip-x-origin = 0
				0,	// clip-y-origin = 0
				0,	// clip-mask = None
				0,	// dash-offset = 0
				4,	// dashes = 4 (i.e. the list [4,4])
				1	// arc-mode = PieSlice
		};		
	}
	public void setAttribute(int maskBit,int value) {
		_attributes[maskBit] = value;
	}
}
