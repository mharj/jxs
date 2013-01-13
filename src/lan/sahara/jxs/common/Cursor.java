package lan.sahara.jxs.common;

import lan.sahara.jxs.impl.AbsApiClient;
import lan.sahara.jxs.impl.AbsApiServer;

public class Cursor extends Resource {
/*	
	public Cursor (
			int			id,
			XServer		xServer,
			Client		client,
			Pixmap		p,
			Pixmap		mp,
			int			x,
			int			y,
			int			foregroundColor,
			int			backgroundColor
		)
*/	

	protected Cursor(Integer resource_id,AbsApiServer ourServer, AbsApiClient ourClient) {
		super(Resource.CURSOR, resource_id,ourServer, ourClient);
		// TODO Auto-generated constructor stub
	}

}
