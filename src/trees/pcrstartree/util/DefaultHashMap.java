package trees.pcrstartree.util;

import java.util.HashMap;

public class DefaultHashMap<K, V> extends HashMap<K, V> { //HashMap with default return value for keys not found
	protected V defaultValue;
	public DefaultHashMap(V defaultValue) {
		this.defaultValue = defaultValue;
	}
	public V get(K key) {
		return containsKey(key) ? super.get(key) : defaultValue;
	}
}
