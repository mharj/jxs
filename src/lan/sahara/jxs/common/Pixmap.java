package lan.sahara.jxs.common;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;

public class Pixmap extends Resource {
	protected Pixmap(Integer resource_id,AbsApiServer ourServer, AbsApiClient ourClient) {
		super(Resource.PIXMAP, resource_id, ourServer, ourClient);
	}
}
