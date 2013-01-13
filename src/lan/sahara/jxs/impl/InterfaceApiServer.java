package lan.sahara.jxs.impl;

import java.awt.Rectangle;

import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.Font;

public interface InterfaceApiServer {
	/**
	 * output client createClient implementation interface
	 * @param clientIdBase
	 * @param clientIdStep
	 * @return implementation client
	 */
	public AbsApiClient createClient(int clientIdBase,int clientIdStep);
	
	/**
	 * Check extensions
	 * @param extension
	 * @return Extension or null
	 */
	public Extension queryExtension(String extension);
	
	public Rectangle getRootWindowSize();
	
	public Font getDefaultFont();
}
