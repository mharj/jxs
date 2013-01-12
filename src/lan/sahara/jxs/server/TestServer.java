package lan.sahara.jxs.server;

import lan.sahara.jxs.impl.LoggingClient;

public class TestServer {
	private static XServer         _xServer;
	private static int             port = 6000;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		_xServer = new XServer (new LoggingClient(),port, null);
	}
	 @Override
	 protected void finalize() throws Throwable {
		 _xServer = null;
		 System.err.println("main end?");
	 }
}
