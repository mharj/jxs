package lan.sahara.jxs.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import lan.sahara.jxs.common.Format;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.impl.ClientApiInterface;

public class XServer {
	private final Vector<Format> _formats; // TODO: move to "implement"
											// class/interface
	private final Visual _rootVisual; // TODO: move to "implement"
										// class/interface
	public final short ProtocolMajorVersion = 11;
	public final short ProtocolMinorVersion = 0;
	public final String vendor = "Open source";
	public final int ReleaseNumber = 0;

	private final int _port;
	private AcceptThread _acceptThread = null;
	// private final HashSet<Integer> _accessControlHosts;
	private boolean _accessControlEnabled = false;

	private final Hashtable<Integer, Resource> _resources;

	private ScreenView _screen = null;

	private final ClientApiInterface _outClient;

	private final Vector<Client> _clients;
	// private final Keyboard _keyboard;

	private final int _clientIdBits = 20;
	private final int _clientIdStep = (1 << _clientIdBits);
	private int _clientIdBase = _clientIdStep;

	public XServer(ClientApiInterface outClient, int port, String windowManagerClass) {
		System.err.println("Server Started");
		_outClient = outClient;
		_port = port;
		_resources = new Hashtable<Integer, Resource>();
		_clients = new Vector<Client>();
		_formats = new Vector<Format>();
		_formats.add(new Format((byte) 32, (byte) 24, (byte) 8));
		_rootVisual = new Visual(1);
		_screen = new ScreenView(this, 3, 3.81f); // ~96 DPI
		start();
	}

	@Override
	protected void finalize() throws Throwable {
		System.err.println("Server Stopped");
		stop();
	}

	/**
	 * Get the number of pixmap formats.
	 * 
	 * @return The number of pixmap formats.
	 */
	public int getNumFormats() {
		return _formats.size();
	}

	/**
	 * Write details of all the pixmap formats.
	 * 
	 * @param io
	 *            The input/output stream.
	 * @throws IOException
	 *             TODO: move "write" to "Client" class and
	 */
	public void writeFormats(InputOutput io) throws IOException {
		for (Format f : _formats)
			f.write(io);
	}

	public ScreenView getScreen() {
		return _screen;
	}

	/**
	 * Return the root visual.
	 * 
	 * @return The root visual.
	 */
	public Visual getRootVisual() {
		return _rootVisual;
	}

	/**
	 * Get the X server's keyboard.
	 * 
	 * @return The keyboard used by the X server.
	 */
	/*
	 * public Keyboard getKeyboard () { return _keyboard; }
	 */
	public synchronized boolean start() {
		if (_acceptThread != null)
			return true; // Already running.
		try {
			_acceptThread = new AcceptThread(_port);
			_acceptThread.start();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Stop listening on the socket and terminate all clients.
	 */
	public synchronized void stop() {
		if (_acceptThread != null) {
			_acceptThread.cancel();
			_acceptThread = null;
		}
	}

	/**
	 * Remove a client from the list of active clients.
	 * 
	 * @param client
	 *            The client to remove.
	 */
	public void removeClient(Client client) {
		// for (Selection sel: _selections.values ())
		// sel.clearClient (client);

		_clients.remove(client);
		// if (_grabClient == client)
		// _grabClient = null;

		// if (client.getCloseDownMode () == Client.Destroy && _clients.size ()
		// == 0)
		// reset();
	}

	/**
	 * Free the resource with the specified ID.
	 * 
	 * @param id
	 *            The resource ID.
	 */
	public void freeResource(int id) {
		_resources.remove(id);
	}	
	
	private class AcceptThread extends Thread {
		private InetAddress IPAddress = null;
		private final ServerSocket _serverSocket;

		/**
		 * Constructor.
		 * 
		 * @param port
		 *            The port to listen on.
		 * 
		 * @throws IOException
		 */
		AcceptThread(int port) throws IOException {
			_serverSocket = new ServerSocket(port);
		}

		/**
		 * Return the internet address that is accepting connections. May be
		 * null.
		 * 
		 * @return The internet address that is accepting connections.
		 */
		public InetAddress getInetAddress() {
			return _serverSocket.getInetAddress();
		}



		/**
		 * Run the thread.
		 */
		public void run() {
			System.err.println("Thread Started");
			while (true) {
				Socket socket = null;
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception.
					socket = _serverSocket.accept();
					// TODO: check valid ip?
					System.err.println("new Connection!");
				} catch (IOException e) {
					break;
				}
				synchronized (this) {
					Client c;
					try {
						c = new Client(_outClient, XServer.this, socket, _clientIdBase, _clientIdStep - 1);
						_clients.add(c);
						c.start();
						_clientIdBase += _clientIdStep;
					} catch (IOException e) {
						try {
							socket.close();
						} catch (IOException e2) {
						}
					}
				}

			}
		}

		/**
		 * Cancel the thread.
		 */
		public void cancel() {
			try {
				_serverSocket.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

}