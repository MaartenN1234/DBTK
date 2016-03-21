package mn.dbtk.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.Set;


/**
 * Keeps track of a list of values for a certain key. Whenever values are added
 * in the multiset they are added whenever the values does not exists for the key.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class MultiSet<K, V> extends HashMap<K, Set<V>> {
	private static final long serialVersionUID = -4675110942146977631L;



	/**
	 * Returns all key values with a value from the values set as a map
	 * 
	 * @return a map which represents all key values with a value from the 
	 * values list.
	 */
	public Map<K, V> toSingleValueMap() {
		Map<K, V> result = new HashMap<K, V>();
		for (K k : keySet()){
			result.put(k, get(k).iterator().next());
		}
		return result;
	}


	/**
	 * Adds a value to the set which belongs to a certain key. In case such 
	 * set does not exists, it is created.
	 * 
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
	 */
	public void add(K key, V value){
		if (containsKey(key)){
			get(key).add(value);
		} else {
			Set<V> entreeSet = new HashSet<V>();
			entreeSet.add(value);
			put(key, entreeSet);
		}
	}
	
	/**
	 * Removes a key, value pair from the multiset.
	 * 
     * @param key key of the pair that needs to be removed
     * @param value value of the pair that needs to be removed
	 */
	public void remove(K key, V value) {
		Set<V> entreeSet = get(key);
		if (entreeSet != null){
			entreeSet.remove(value);
			if (entreeSet.isEmpty())
				remove(key);
		}
	}	

	/**
	 * Adds all key, value pairs to the multiset
	 * 
	 * @param input the map which contents has to be added
	 */
	public void addAll(Map<K, ? extends Collection<V>> input){
		if (input == null) return;

		for (K k : input.keySet())
			for (V v : input.get(k))
				add(k,v);
	}



	public String toString(){
		StringBuffer result = new StringBuffer();
		for (K k : keySet()){
			result.append(k +" ("+ get(k).size()+"): "+ get(k)+"\r\n");
		}
		return result.toString();
	}
}