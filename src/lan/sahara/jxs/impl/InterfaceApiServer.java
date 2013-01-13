package lan.sahara.jxs.impl;

import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.Font;
import lan.sahara.jxs.common.Geom;

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
	
	public Geom getRootWindowSize();
	
	public Font getDefaultFont();
}
