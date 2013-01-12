package lan.sahara.jxs.impl;

import lan.sahara.jxs.common.Extension;

public class LoggingClient implements ClientApiInterface {

	@Override
	public Extension reqQueryExtension(String extension) {
		// we don't have any extensions, return null always
		return null;
	}
	
	public String[] getAllClientFonts() {
		return new String[] {"fixed","cursor"};
	}
}
