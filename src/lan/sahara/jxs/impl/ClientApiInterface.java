package lan.sahara.jxs.impl;

import lan.sahara.jxs.common.Extension;

public interface ClientApiInterface {
	Extension reqQueryExtension(String extension);
	
	
	
	
	/**
	 * "-android-serif-bold-r-normal--0-0-0-0-p-0-iso8859-1",
	 * "-android-serif-medium-i-normal--0-0-0-0-p-0-iso8859-1",
	 * ...
	 * @return string array of fonts in client
	 */
	public String[] getAllClientFonts();

}
