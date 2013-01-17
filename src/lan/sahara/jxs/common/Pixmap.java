package lan.sahara.jxs.common;

import java.awt.Image;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;

public class Pixmap extends Resource {
	private Image _pixmap = null;
	protected Pixmap(Integer resource_id) {
		super(Resource.PIXMAP, resource_id);
	}
	public Image getBackgroundBitmap() {
		return _pixmap;
	}
}
