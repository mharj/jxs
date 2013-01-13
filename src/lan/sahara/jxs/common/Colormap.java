package lan.sahara.jxs.common;


import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;

public class Colormap extends Resource {

	protected Colormap(Integer resource_id, AbsApiServer ourServer, AbsApiClient ourClient) {
		super(Resource.COLORMAP, resource_id,ourServer, ourClient);
	}
}
