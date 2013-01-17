package lan.sahara.jxs.common;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Drawable {
	private final DrawableCanvas _canvas;
	private final int _depth;
	private Image _backgroundBitmap;
	private int _backgroundColor;

	// TODO: handle depth?
	public Drawable(int width, int height, int depth, Image bgbitmap, int bgcolor) {
		_canvas = new DrawableCanvas(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
		_depth = depth;
		_backgroundBitmap = bgbitmap;
		_backgroundColor = bgcolor;
	}

	/**
	 * Return the drawable's width.
	 * 
	 * @return The drawable's width.
	 */
	public int getWidth() {
		return _canvas._bitmap.getWidth();
	}

	/**
	 * Return the drawable's height.
	 * 
	 * @return The drawable's height.
	 */
	public int getHeight() {
		return _canvas._bitmap.getHeight();
	}

	/**
	 * Return the drawable's depth.
	 * TODO: this is not real one
	 * @return The drawable's depth.
	 */
	public int getDepth() {
		return _depth;
	}

	class DrawableCanvas extends Canvas {
		private static final long serialVersionUID = -1584517123600488596L;
		public final BufferedImage _bitmap;

		public DrawableCanvas(BufferedImage bitmap) {
			_bitmap = bitmap;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(_bitmap, 0, 0, this);
		}
	}
}
