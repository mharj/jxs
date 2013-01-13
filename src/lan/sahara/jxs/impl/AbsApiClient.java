package lan.sahara.jxs.impl;

import java.util.Hashtable;
import java.util.Observable;

import lan.sahara.jxs.common.CloseWindowResponse;
import lan.sahara.jxs.common.EventCode;
import lan.sahara.jxs.common.Property;
import lan.sahara.jxs.common.Window;


public abstract class AbsApiClient extends Observable implements InterfaceApiClient {
	public int _sequenceNumber = 0;
	public final Hashtable<Integer, Property> properties = new Hashtable<Integer, Property>();
    private final int _resourceIdBase;
    private final int  _resourceIdMask;
	public AbsApiClient(int resourceIdBase,int resourceIdMask) {
		_resourceIdBase = resourceIdBase;
		_resourceIdMask = resourceIdMask;
	}
	public int getResourceIdMask() {
		return _resourceIdMask;
	}
	public int getResourceIdBase() {
		return _resourceIdBase;
	}
	/**
	 * Notify Api Server that Resource is closing (Window)
	 * @param window
	 * @return 
	 */
	public void clientDestroyWindow(Window window) {
		System.err.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		setChanged();
		notifyObservers(new CloseWindowResponse(EventCode.DestroyNotify,window));
	}
}
