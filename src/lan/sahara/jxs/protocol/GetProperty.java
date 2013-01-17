package lan.sahara.jxs.protocol;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;


import lan.sahara.jxs.common.Atom;
import lan.sahara.jxs.common.ErrorCode;
import lan.sahara.jxs.common.EventCode;
import lan.sahara.jxs.common.Property;
import lan.sahara.jxs.common.RequestCode;
import lan.sahara.jxs.common.Window;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.Client;
import lan.sahara.jxs.server.InputOutput;
import lan.sahara.jxs.server.Util;

public class GetProperty {
	public static void query(byte delete, int bytesRemaining, InputOutput inputOutput,int sequenceNumber,AbsApiServer ourServer) throws IOException {
		System.err.print("Request: GetProperty ");
		if (bytesRemaining  != 20 ) {
			inputOutput.readSkip (bytesRemaining);
			ErrorCode.write(inputOutput,sequenceNumber,ErrorCode.Length, RequestCode.GetProperty, 0);
			return;
		}
		int window_id 	= inputOutput.readInt ();	// Window ID.
		int	property_id	= inputOutput.readInt ();	// Property.
		int	type_id		= inputOutput.readInt ();	// Type.
		int	longOffset 		= inputOutput.readInt ();	// Long offset.
		int	longLength 		= inputOutput.readInt ();	// Long length.		
		Atom property	= ourServer.getAtom (property_id);
		
		System.err.println("(Window:"+window_id+" Property:"+property_id+" Type:"+type_id+" AtomProperty:"+((property==null)?"NULL":property.getName())+")");
		
		// check valid property and atom exists
		if (property == null) {
			ErrorCode.write (inputOutput,sequenceNumber,ErrorCode.Atom,RequestCode.GetProperty, property_id);
			return;
		} else if (type_id != 0 && ! ourServer.atomExists (type_id)) {
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Atom,RequestCode.GetProperty, type_id);
			return;
		}
		// Check Window exists
		Window window = (Window) ourServer.getResource(window_id);
		if ( window == null) {
			ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Window,RequestCode.GetProperty, window_id);
			return;
		}
		Hashtable<Integer, Property> properties = window.getProperties();
		// TODO: learn what this is doing
		byte		format = 0;
		int			bytesAfter = 0;
		byte[]		value = null;
		boolean		generateNotify = false;
		
		if ( properties.containsKey (property_id)) {
			Property	p = properties.get(property_id);
			type_id = p._type;
			format = p._format;
			if (type_id != 0 && type_id != p._type) {
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
					ErrorCode.write (inputOutput,sequenceNumber, ErrorCode.Value,RequestCode.GetProperty, 0);
					return;
				}
				if (l > 0) {
					value = new byte[l];
					System.arraycopy (p._data, i, value, 0, l);
				}
				if ( delete == 1  && bytesAfter == 0) {
					properties.remove (property_id);
					generateNotify = true;
				}
			}
		} else {
			type_id = 0;
		}
		// reply
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

		synchronized (inputOutput) {
			Util.writeReplyHeader (inputOutput,sequenceNumber, format);
			inputOutput.writeInt ((length + pad) / 4);	// Reply length.
			inputOutput.writeInt (type_id);	// Type.
			inputOutput.writeInt (bytesAfter);	// Bytes after.
			inputOutput.writeInt (valueLength);	// Value length.
			inputOutput.writePadBytes (12);	// Unused.

			if (value != null) {
				inputOutput.writeBytes (value, 0, value.length);	// Value.
				inputOutput.writePadBytes (pad);	// Unused.
			}
			System.out.println("Reply: GetProperty()");
		}
		inputOutput.flush ();	
		// notify 
		if (generateNotify) {
			Vector<Client>		sc;
			if ((sc = window.getSelectingClients (EventCode.MaskPropertyChange)) != null) {
				for (Client c: sc)
					EventCode.sendPropertyNotify (c, window, property,ourServer.getTimestamp (), 1);
			}
		}
	}
	
}
