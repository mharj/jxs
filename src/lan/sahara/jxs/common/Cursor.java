package lan.sahara.jxs.common;

import java.awt.Color;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;


public class Cursor extends Resource {
	private final Font _sourceFont;
	private final Font _maskFont; 
	private final int _sourceChar;
	private final int _maskChar;
	private final Color _foregroundColor;
	private final Color _backgroundColor;

	public Cursor(Integer resource_id, Font sourceFont, Font maskFont, int sourceChar, int maskChar, Color foregroundColor, Color backgroundColor) {
		super(Resource.CURSOR, resource_id);
		_sourceFont = sourceFont;
		_maskFont = maskFont;
		_sourceChar = sourceChar;
		_maskChar = maskChar;
		_foregroundColor = foregroundColor;
		_backgroundColor = backgroundColor;
	}

}
