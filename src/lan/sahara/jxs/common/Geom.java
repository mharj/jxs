package lan.sahara.jxs.common;

public class Geom {
	private Integer _x=null;
	private Integer _y=null;
	private Integer _w=null;
	private Integer _h=null;
	public Geom() {}
	public Geom(int x,int y,int w,int h) {
		_x=x;
		_y=y;
		_w=w;
		_h=h;
	}
	public int getX() {
		return _x;
	}
	public int getY() {
		return _y;
	}
	public int getW() {
		return _w;
	}
	public int getH() {
		return _h;
	}
	public void setX(int x) {
		_x=x;
	}
	public void setY(int y) {
		_y=y;
	}
	public void setW(int w) {
		_w=w;
	}
	public void setH(int h) {
		_h=h;
	}	
	public String toString() {
		return new String("["+_x+"*"+_y+"+"+_w+"+"+_h+"]");
	}
}
