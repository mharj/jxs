package lan.sahara.jxs.impl;

import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;


import lan.sahara.jxs.common.Atom;
import lan.sahara.jxs.common.CloseWindowResponse;
import lan.sahara.jxs.common.EventCode;
import lan.sahara.jxs.common.GContext;
import lan.sahara.jxs.common.Geom;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.ResourceManager;
import lan.sahara.jxs.common.Visual;
import lan.sahara.jxs.common.Window;

@SuppressWarnings("unused")
public abstract class AbsApiServer extends Observable implements InterfaceApiServer,Observer  {

	
	private long				_timestamp;
	private final ResourceManager 				resourceMgmr		= new ResourceManager();
	private final Hashtable<Integer, Atom>		_atoms= new Hashtable<Integer, Atom>();
	private final Hashtable<String, Atom>		_atomNames = new Hashtable<String, Atom>();
	private int									_maxAtomId = 0;
	
//	private final Hashtable<Integer, Resource> _resources = new Hashtable<Integer, Resource>();
	private final Vector<AbsApiClient> _clients = new Vector<AbsApiClient>(); // Cons need to define which Client class to use
	private String			_windowManagerClass = null;
	
	private Window					_rootWindow = null;	
	private final Visual			_rootVisual = new Visual(1);
	
	public AbsApiServer() {
		init();
	}
	
	public AbsApiServer(String windowManagerClass) {
		setWindowManagerClass(windowManagerClass);
		
	}
	private void init() {
		Atom.registerPredefinedAtoms(this); //load atoms to server
		Geom rootSize = this.getRootWindowSize(); // get root size from implementation
		if ( rootSize == null )
			throw new RuntimeException("No Root Window size found! (Geometry in NULL)"); // terminate whole application

		_rootWindow = new Window(1,this, null, null,rootSize,0,false,true);
		addResource(_rootWindow);
		addResource(this.getDefaultFont());
		
		_timestamp = System.currentTimeMillis ();
	}
	
	public void setWindowManagerClass(String windowManagerClass) {
		_windowManagerClass = windowManagerClass;
	}
	
	public String getWindowManagerClass(String windowManagerClass) {
		return _windowManagerClass;
	}

	public AbsApiClient _createClient(int clientIdBase,int clientIdStep) throws RuntimeException {
		AbsApiClient client = createClient(clientIdBase,clientIdStep); // use interface to get final implementation
		if ( client == null )
			throw new RuntimeException("No output Client implementation found!"); // terminate whole application
		client.addObserver(this);
		_clients.add(client);
		return client;
	}

	public Resource getResource(int resource_id) {
		if (!resourceExists (resource_id))
			return null;
		return resourceMgmr.get (resource_id);
	}
	
	public boolean resourceExists (int resource_id) {
		return resourceMgmr.containsKey (resource_id);
	}
	
	public boolean validResourceId (int cid,int resourceIdMask,int resourceIdBase) {
		return ((cid & ~resourceIdMask) == resourceIdBase && ! resourceExists(cid) );
	}
	
	public boolean validResourceId (int cid,AbsApiClient client) {
		return ((cid & ~client.getResourceIdMask()) == client.getResourceIdBase() && ! resourceExists(cid) );
	}



	public void addAtom(Atom atom) {
		_atoms.put (atom.getId (), atom);
		_atomNames.put (atom.getName (), atom);

		if (atom.getId () > _maxAtomId)
			_maxAtomId = atom.getId ();
	}

	public Atom getAtom(final int id) {
		if (!_atoms.containsKey (id))	// No such atom.
			return null;
		return _atoms.get (id);
	}
	public Atom findAtom (final String name) {
		if (!_atomNames.containsKey (name))
			return null;
		return _atomNames.get (name);
	}

	public int nextFreeAtomId() {
		return ++_maxAtomId;
	}

	public boolean atomExists(int tid) {
		return _atoms.containsKey (tid);
	}
	// resource methods
	public void freeResource(int id) {
		resourceMgmr.remove (id);
	}

	public void freeResource(Resource resource) {
		resourceMgmr.remove (resource);
	}
	
	public void addResource(Resource resource) {
		System.err.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		resourceMgmr.addResource(resource);
	}
	public Integer getResourceId(Resource resource) {
		return resourceMgmr.getId(resource);
	}

	public int getTimestamp() {
		long diff = (System.currentTimeMillis () - _timestamp);
		if (diff <= 0)
			return 1;
		return (int) diff;
	}
	// observer
	public void update(Observable obj, Object arg) {
        if (arg instanceof CloseWindowResponse) {
        	CloseWindowResponse resp = (CloseWindowResponse) arg;
            System.out.println("ABS EVENT CODE: " + resp.getEventCode() );
    		setChanged();
    		notifyObservers(arg);            
        }
    }
}
