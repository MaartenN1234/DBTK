package mn.dbtk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;



/**
 * Keeps track of a list of values for a certain key. Whenever values are added
 * in the multimap they are always added as the last element for a certain key.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class MultiMap<K, V> extends HashMap<K, List<V>> {
	private static final long serialVersionUID = -3395692763409648715L;

	/**
	 * Returns all key values with the first value from the values list as a map
	 * 
	 * @return a map which represents all key values with the first value from
	 * the values list.
	 */
	public Map<K, V> toSingleValueMap (){
		Map<K, V> result = new HashMap<K, V>();
		for (K k:keySet()){
			result.put(k, get(k).get(0));
		}
		return result;
	}


	/**
	 * Adds a value to the list which belongs to a certain key. In case such 
	 * list does not exists, it is created.
	 * 
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
	 */
	public void add(K key, V value){
		if (containsKey(key)){
			get(key).add(value);
		} else {
			List<V> entreeList = new ArrayList<V>();
			entreeList.add(value);
			put(key, entreeList);
		}
	}

	/**
	 * Adds all key, value pairs to the multimap
	 * 
	 * @param input the map which contents has to be added
	 */
	public void addAll(Map<K, ? extends Collection<V>> input){
		if (input == null) return;

		for (K k : input.keySet())
			for (V v : input.get(k))
				add(k,v);
	}
	
	
	/**
	 * Creates a multiset based upon the current contents of the multimap. In 
	 * this process duplicate values for the same key are removed.
	 * 
	 * @return the multiset containing the same unique elements
	 */
	public MultiSet<K,V> toMultiSet() {
		MultiSet<K,V> result = new MultiSet<K,V>();
		for (K k:keySet()){
			result.put(k, new HashSet<V>(get(k)));
		}
		return result;
	}
	
	public String toString(){
		StringBuffer result = new StringBuffer();
		for (K k : keySet()){
			result.append(k +" ("+ get(k).size()+"): "+ get(k)+"\r\n");
		}
		return result.toString();
	}
}