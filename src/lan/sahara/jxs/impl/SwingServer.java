package lan.sahara.jxs.impl;

import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.Font;
import lan.sahara.jxs.common.Geom;

public class SwingServer extends AbsApiServer {

	@Override
	public AbsApiClient createClient(int clientIdBase, int clientIdStep) {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new SwingClient(clientIdBase,clientIdStep);
	}

	@Override
	public Extension queryExtension(String extension) {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return null;
	}

	@Override
	public Geom getRootWindowSize() {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new Geom(0,0,200,200);
	}

	@Override
	public Font getDefaultFont() {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new Font(null,"", this, null);
	}

}
