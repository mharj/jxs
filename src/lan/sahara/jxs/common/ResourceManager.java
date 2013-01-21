package lan.sahara.jxs.common;

import java.util.concurrent.ConcurrentHashMap;

public class ResourceManager {
	private int firstFree = 1;
	private static ConcurrentHashMap<Integer,Resource> resourceMap = new ConcurrentHashMap<Integer,Resource>();
	private static ConcurrentHashMap<Resource,Integer> resourceReverseMap = new ConcurrentHashMap<Resource,Integer>();
	
	public Integer addResource(final Resource r) {
		Integer resource_id = r.getId();
		synchronized (this) {
			if ( r.getId() == null )
				resource_id = firstFree;
				
			if ( resourceMap.containsKey(firstFree) )
				throw new RuntimeException("ResourceManager: Error Resource already defined");
			else {
				resourceMap.put(resource_id, r);
				resourceReverseMap.put(r, resource_id);
			}
			for ( int i=1; i< Integer.MAX_VALUE; i++ ) {
				if ( ! resourceMap.keySet().contains(i) ) {
//					System.out.println("Added Resource = "+resource_id+" (Class:"+r.getClass().getSimpleName()+") Next free resource id = "+i);
					firstFree = i;
					break;
				}
			}
		}
		return resource_id;
	}
	public Resource get(int resource_id) {
		return resourceMap.get(resource_id);
	}
	public boolean containsKey(int resource_id) {
		return resourceMap.containsKey(resource_id);
	}
	public void remove(int resource_id) {
		synchronized (this) {
			if ( resourceMap.containsKey(resource_id) ) {
				Resource r = resourceMap.get(resource_id);
				resourceMap.remove(resource_id);
				resourceReverseMap.remove(r);
			}
			for ( int i=1; i < Integer.MAX_VALUE; i++ ) {
				if ( ! resourceMap.keySet().contains(i) ) {
//					System.out.println("Deleted Resource = "+resource_id+" Next free resource id = "+i);
					firstFree = i;
					break;
				}
			}
		}
	}
	public void remove(Resource resource) {
		synchronized (this) {
			Integer id = null;
			if ( resourceReverseMap.containsKey(resource) ) {
				id = resourceReverseMap.get(resource);
				resourceReverseMap.remove(resource);
				resourceMap.remove(id);
			}
			for ( int i=1; i < Integer.MAX_VALUE; i++ ) {
				if ( ! resourceMap.keySet().contains(i) ) {
//					System.out.println("Deleted Resource = "+id+" Next free resource id = "+i);
					firstFree = i;
					break;
				}
			}
		}
	}
	public Integer getId(Resource resource) {
		return resourceReverseMap.get(resource);
	}
}
