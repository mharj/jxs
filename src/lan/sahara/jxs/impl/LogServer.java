package lan.sahara.jxs.impl;

import java.awt.Rectangle;

import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.Font;

public class LogServer extends AbsApiServer {

	@Override
	public AbsApiClient createClient(int clientIdBase, int clientIdStep) {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new LogClient(clientIdBase,clientIdStep);
	}

	@Override
	public Extension queryExtension(String extension) {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		// return NULL as we don't have extensions
		return null;
	}

	@Override
	public Font getDefaultFont() {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new Font(null,"", this, null);
	}

	@Override
	public Rectangle getRootWindowSize() {
		return new Rectangle(0,0,200,200);
	}
}
