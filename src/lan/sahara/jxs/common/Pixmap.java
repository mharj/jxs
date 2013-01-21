package lan.sahara.jxs.common;

import java.awt.Image;
import java.awt.image.BufferedImage;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;

public class Pixmap extends Resource {
	private final int _width;
	private final int _height;
	private final byte _depth;
	private Image _pixmap = null;
	public Pixmap(Integer resource_id, int width, int height, byte depth) {
		super(Resource.PIXMAP, resource_id);
		_width = width;
		_height = height;
		_depth = depth;
		_pixmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}
	public Image getBackgroundBitmap() {
		return _pixmap;
	}
}
