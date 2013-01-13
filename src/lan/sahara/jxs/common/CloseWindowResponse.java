package lan.sahara.jxs.common;

public class CloseWindowResponse {
	private final int _e;
	private final Resource _r;
	public CloseWindowResponse(int e,Resource r) {
		_e = e;
		_r = r;
	}
	public int getEventCode() {
		return _e;
	}
	public Resource getResource() {
		return _r;
	}
	
}
