package lan.sahara.jxs.server;

import lan.sahara.jxs.impl.LogServer;
import lan.sahara.jxs.impl.SwingServer;

@SuppressWarnings("unused")
public class TestServer {
	private static XServer         _xServer;
	private static int             port = 6000;

	/**
	 * @param args
	 * @throws IllegalAccessException 
	 */
	public static void main(String[] args) throws IllegalAccessException {
		_xServer = new XServer (new SwingServer(),port, null);
	}
	 @Override
	 protected void finalize() throws Throwable {
		 _xServer = null;
		 System.err.println("main end?");
	 }
}
