package lan.sahara.jxs.impl;

import java.awt.Rectangle;

import lan.sahara.jxs.common.GContext;
import lan.sahara.jxs.common.Window;

public interface InterfaceApiClient {
	public Boolean clientCreateWindow(Window window);
	public Boolean clientCreateGC(GContext gc);
	public Boolean clientFreeResource(Integer id);
	public int clientGetInputFocus();
}
