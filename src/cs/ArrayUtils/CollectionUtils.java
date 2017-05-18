package cs.ArrayUtils;

import java.util.HashMap;

public final class CollectionUtils {
	public static final <K, V> HashMap<K, V> 
	constructConstMap(Object... objects) {
		HashMap<K, V> result = new HashMap<>(objects.length / 2);
		for(int i = 0; i < objects.length / 2; ++i) 
			result.put((K)objects[i*2], (V)objects[(i*2) + 1]);
		return result;
	}
}
