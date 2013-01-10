package lan.sahara.jsx.interfaces;

import lan.sahara.jsx.server.Extension;

public class LoggingClient implements ClientApiInterface {

	@Override
	public Extension reqQueryExtension(String extension) {
		// we don't have any extensions, return null always
		return null;
	}
}
