package lan.sahara.jxs.common;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.Client;
import lan.sahara.jxs.server.XServer;

public class Font extends Resource {
	private final String _name;
	public Atom _nameAtom;
	public final char _maxChar;
	
	private String fontFoundry		= null; // font foundry, the company or individual which made the font.
	private String fontFamily		= null; // font family, the popular nickname of the font
	private String fontWeight		= null; // font weight (bold, medium, etc.)
	private String fontSlant		= null; // font slant (italics, oblique, roman (normal), etc.)
	private String fontWidth		= null; // font width (normal, condensed, extended, etc.)
	private String additionalStyle 	= null; // additional style (sans serif, serif, etc.)
	private String pixelSize	 	= null; // pixel size, the number of pixels vertically in a character
	private String pointSize		= null; // approximate point size of the text (similar to pixelSize)
	private String xResolution		= null; // horizontal resolution, in dpi
	private String yResolution		= null; // vertical resolution, in dpi
	private String spacing			= null; // spacing
	private String avgWidth			= null; // average character width of the font
	private String registry			= null; // the recognized registry that lists the font
	private String encoding			= null; // nationality encoding
/*	
	-adobe-helvetica-medium-r-normal-*-*-120-*-*-p-*-iso10646-1

fndry - font foundry, the company or individual which made the font.
fmly - font family, the popular nickname of the font
wght - font weight (bold, medium, etc.)
slant - font slant (italics, oblique, roman (normal), etc.)
sWdth - font width (normal, condensed, extended, etc.)
adstyl - additional style (sans serif, serif, etc.)
pxlsz - pixel size, the number of pixels vertically in a character
ptSz - approximate point size of the text (similar to pxlsz)
resx - horizontal resolution, in dpi
resy - vertical resolution, in dpi
spc - spacing, only useful, apparently, in the Schumacher fonts
avgWidth - average character width of the font
rgstry - the recognized registry that lists the font
encdng - nationality encoding
*/
	
	
	/**
	 * @param resource_id Resource Id
	 * @param name Font Name
	 */
	public Font (Integer resource_id,String name) {
		super(Resource.FONT, resource_id);
		char maxChar = 255;
		_name = name;
		String[]	fields = name.split ("-");
		if ( fields.length == 15 ) {
			System.err.println("Font 15 parts");
			fontFoundry 		= fields[1];
			fontFamily			= fields[2];
			fontWeight			= fields[3];
			fontSlant			= fields[4];
			fontWidth			= fields[5];
			additionalStyle 	= fields[6];
			pixelSize	 		= fields[7];
			pointSize			= fields[8];
			xResolution			= fields[9];
			yResolution			= fields[10];
			spacing				= fields[11];
			avgWidth			= fields[12];
			registry			= fields[13];
			encoding			= fields[14];
			
			if (registry.equalsIgnoreCase ("iso10646"))
				maxChar = 65534;
		}
		_maxChar = maxChar;
	}
	public String getName() {
		return _name;
	}
}
