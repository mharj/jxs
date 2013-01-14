package lan.sahara.jxs.impl;

import java.util.Hashtable;
import java.util.Observable;

import lan.sahara.jxs.common.CloseWindowResponse;
import lan.sahara.jxs.common.EventCode;
import lan.sahara.jxs.common.GContext;
import lan.sahara.jxs.common.Property;
import lan.sahara.jxs.common.Resource;
import lan.sahara.jxs.common.Window;


public abstract class AbsApiClient extends Observable implements InterfaceApiClient {

	public final Hashtable<Integer, Property> properties = new Hashtable<Integer, Property>();
	/**
	 * Notify Api Server that Resource is closing (Window)
	 * @param window
	 * @return 
	 */
	public void clientDestroyWindow(Window window) {
//		System.err.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		setChanged();
		notifyObservers(new CloseWindowResponse(EventCode.DestroyNotify,window));
	}
	public void addResource(Resource resource) {
		System.err.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName()+" ID:"+resource.getId());
		if (resource instanceof Window ) {
			clientCreateWindow((Window)resource);
		}
		if (resource instanceof GContext ) {
			clientCreateGC((GContext)resource);
		}
		
	}
}
