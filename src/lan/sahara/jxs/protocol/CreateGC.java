package lan.sahara.jxs.protocol;

import java.io.IOException;

import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.GContext;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class CreateGC {
	/**
	 * The XCreateGC() function creates a graphics context and returns a GC
	 * op_code: 55 
	 * 
	 * @param unused Optional argument
	 * @param bytesRemaining size of data
	 * @param inputOutput TCP wrapper
	 * @param sequenceNumber current sequence id
	 * @param ourServer our server implementation (just to get parent resource)
	 * @param ourClient our client implementation (just to get parent resource)
	 * @return GContext object
	 * @throws IOException
	 */
	public static GContext query(byte unused, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer,AbsApiClient ourClient) throws IOException {
		GContext gcontext = null;
		System.err.print("Request: CreateGC ");
		if (bytesRemaining < 12) {
			inputOutput.readSkip (bytesRemaining);
            ErrorCode.write(inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.CreateGC, 0);
            return null;
        }
        int	cid = 	 inputOutput.readInt ();  		 	// GContext ID.
        int	drawable = inputOutput.readInt ();		 	// Drawable ID.
        Resource r = ourServer.getResource (drawable);	// resource
        System.err.println("(ID:"+cid+" Parent:"+drawable+")");
        bytesRemaining -= 8;
        if (! ourServer.validResourceId (cid) ) {
        	inputOutput.readSkip (bytesRemaining);
            ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.IDChoice, RequestCode.CreateGC, cid);
        } else if (r == null || ! r.isDrawable ()) {
        	inputOutput.readSkip (bytesRemaining);
            ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Drawable, RequestCode.CreateGC, drawable);            	
        } else {
        	gcontext = new GContext(cid,drawable);
            if (bytesRemaining < 4) {
            	inputOutput.readSkip (bytesRemaining);
                ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.CreateGC, 0);
                return null;
            }
            // read mask
            int valueMask = inputOutput.readInt ();      // Value mask.
        	int n = Util.bitcount (valueMask);
        	bytesRemaining -= 4;
            if (bytesRemaining != n * 4) {
            	inputOutput.readSkip (bytesRemaining);
            	ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.CreateGC, 0);
            	return null;
            }
            // loop masked values
            for (int type = 0; type < 23; type++) {
                if ((valueMask & (1 << type)) != 0) {
                	switch (type) {
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
                    		gcontext.setAttribute(type, inputOutput.readByte () );
                    		inputOutput.readSkip(3);
                    		break;
                    	case GContext.AttrPlaneMask:
                    	case GContext.AttrForeground:
                    	case GContext.AttrBackground:
                    	case GContext.AttrTile:
                    	case GContext.AttrStipple:
                    	case GContext.AttrFont:
                    	case GContext.AttrClipMask:
                    		gcontext.setAttribute(type, inputOutput.readInt () );
                            break;
                    	case GContext.AttrLineWidth:
                    	case GContext.AttrDashOffset:
                    		gcontext.setAttribute(type, inputOutput.readShort () );
                    		inputOutput.readSkip(2);
                            break;
                    	case GContext.AttrTileStippleXOrigin:
                    	case GContext.AttrTileStippleYOrigin:
                    	case GContext.AttrClipXOrigin:
                    	case GContext.AttrClipYOrigin:
                    		gcontext.setAttribute(type, (short) inputOutput.readShort () );
                    		inputOutput.readSkip(2);
                            break;
                	}
                }
            }
        }
		return gcontext;
	}
}
