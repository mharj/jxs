package lan.sahara.jxs.protocol;

import java.awt.Color;
import java.io.IOException;

import lan.sahara.jxs.common.Cursor;
import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Font;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;

public class CreateGlyphCursor {
	public static Cursor query(byte arg, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		System.err.print("Request: CreateGlyphCursor");
		Cursor ret = null;
		int			id = inputOutput.readInt ();	// Cursor ID.
		System.err.print("[id:"+id+"]");
		bytesRemaining -= 4;
		if (! ourServer.validResourceId (id)) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.IDChoice, RequestCode.CreateGlyphCursor, id);
		} else {
			int	sid = inputOutput.readInt ();	// Source font ID.
			int	mid = inputOutput.readInt ();	// Mask font ID.
			int	sourceChar = inputOutput.readShort ();	// Source char.
			int	maskChar = inputOutput.readShort ();	// Mask char.
			float fgRed = (float)inputOutput.readShort()/65535;
			float fgGreen = (float)inputOutput.readShort()/65535;
			float fgBlue = (float)inputOutput.readShort()/65535;
			float bgRed = (float)inputOutput.readShort ()/65535;
			float bgGreen = (float)inputOutput.readShort ()/65535;
			float bgBlue = (float)inputOutput.readShort ()/65535;
			Resource	r = ourServer.getResource (sid);
			Resource	mr = null;
			System.err.println("[source_font_id:"+sid+",mask_font_id:"+mid+",fg["+fgRed+","+fgGreen+","+fgBlue+"],bg["+bgRed+","+bgGreen+","+bgBlue+"] ]");
			if (r == null || r.getType () != Resource.FONT) {
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Font, RequestCode.CreateGlyphCursor, sid);
				return null;
			} else if (mid != 0) {
				mr = ourServer.getResource (mid);
				if (mr == null || mr.getType () != Resource.FONT) {
					ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Font, RequestCode.CreateGlyphCursor, mid);
					return null;
				}
			}
			ret = new Cursor(id, (Font) r,(Font) mr, sourceChar, maskChar, new Color(fgRed, fgGreen, fgBlue) , new Color(bgRed, bgGreen, bgBlue) );
		}
		return ret;
	}
}
