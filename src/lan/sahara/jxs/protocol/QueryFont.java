package lan.sahara.jxs.protocol;

import java.io.IOException;



import lan.sahara.jxs.common.Atom;
import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.Font;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class QueryFont {
	public static void query(byte arg, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		int			id = inputOutput.readInt ();
		Resource	r = ourServer.getResource (id);
		
		bytesRemaining -= 4;
		if (r == null || !r.isFontable ()) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Font, RequestCode.QueryFont, id);
		} else {
			if (bytesRemaining != 0) {
				inputOutput.readSkip (bytesRemaining);
				ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Length, RequestCode.QueryFont, 0);
			} else {
				Font f = (Font)r;
				int				numFontProperties = ( f._nameAtom == null) ? 0 : 1;
				int				numCharInfos = f._maxChar - 31;
				char[]			chars = new char[numCharInfos];

				for (char c = 32; c <= f._maxChar; c++)
					chars[c - 32] = c;

				String			s = new String (chars);
//				Rect			bounds = new Rect ();
				float[]			widths = new float[numCharInfos];

//				_paint.getTextWidths (s, widths);
/*
				synchronized (inputOutput) {
					Util.writeReplyHeader (inputOutput,sequenceNumber, (byte) 0);
					// Reply length.
					inputOutput.writeInt (7 + numFontProperties * 2 + numCharInfos * 3);

					// Min bounds.
					inputOutput.writeShort ((short) 0);	// Left side bearing.
					inputOutput.writeShort ((short) 0);	// Right side bearing.
					inputOutput.writeShort ((short) _minWidth);	// Character width.
					inputOutput.writeShort ((short) 0);	// Ascent.
					inputOutput.writeShort ((short) 0);	// Descent.
					inputOutput.writeShort ((short) 0);	// Attributes.
					inputOutput.writePadBytes (4);	// Unused.

					// Max bounds.
					inputOutput.writeShort ((short) 0);	// Left side bearing.
					inputOutput.writeShort ((short) _maxWidth);	// Right side bearing.
					inputOutput.writeShort ((short) _maxWidth);	// Character width.
					inputOutput.writeShort (_maxAscent);	// Ascent.
					inputOutput.writeShort (_maxDescent);	// Descent.
					inputOutput.writeShort ((short) 0);	// Attributes.
					inputOutput.writePadBytes (4);	// Unused.

					inputOutput.writeShort ((short) 32);	// Min char or byte2.
					inputOutput.writeShort ((short) _maxChar);	// Max char or byte2.
					inputOutput.writeShort ((short) 32);	// Default char.
					inputOutput.writeShort ((short) numFontProperties);
					inputOutput.writeByte ((byte) 0);	// Draw direction = left-to-right.
					inputOutput.writeByte ((byte) 0);	// Min byte 1.
					inputOutput.writeByte ((byte) 0);	// Max byte 1.
					inputOutput.writeByte ((byte) 0);	// All chars exist = false.
					inputOutput.writeShort (_ascent);	// Font ascent.
					inputOutput.writeShort (_descent);	// Font descent.
					inputOutput.writeInt (numCharInfos);

					// If name atom is specified, write the FONT property.
					if (f._nameAtom != null) {
						Atom		a = ourServer.findAtom ("FONT");

						inputOutput.writeInt (a.getId ());	// Name.
						inputOutput.writeInt (f._nameAtom.getId ());	// Value.
					}

					for (int i = 0; i < numCharInfos; i++) {
//						_paint.getTextBounds (s, i, i + 1, bounds);
						inputOutput.writeShort ((short) bounds.left);	// Left side bearing.
						inputOutput.writeShort ((short) bounds.right);	// Right side bearing.
						inputOutput.writeShort ((short) (widths[i]));	// Character width.
						inputOutput.writeShort ((short) -bounds.top);	// Ascent.
						inputOutput.writeShort ((short) bounds.bottom);	// Descent.
						inputOutput.writeShort ((short) 0);	// Attributes.
					}
				}
				inputOutput.flush ();
	*/			
			}
		}
		
	}
}
