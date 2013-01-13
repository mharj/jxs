package lan.sahara.jxs.common;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;
import lan.sahara.jxs.server.Client;
import lan.sahara.jxs.server.XServer;

public class Font extends Resource {
	/**
	 * @param resource_id Resource Id
	 * @param name Font Name
	 * @param ourServer current server
	 * @param ourClient current client
	 */
	public Font (Integer resource_id,String name,AbsApiServer ourServer,AbsApiClient ourClient) {
		super(Resource.FONT, resource_id, ourServer, ourClient);
	}
}
