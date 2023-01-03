/*
 * Mapping.java
 * completely describes  a map
 *
 */

package de.uib.utilities;

import java.util.ArrayList;
/**
 *
 * @author roeder
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public class Mapping<K, V> {
	protected Map<K, V> map;
	protected Map<String, V> mapOfStrings;
	protected boolean invertible;
	protected Map<V, K> inverseMap;
	protected List<K> domain;
	protected List<V> range;
	protected List<String> rangeAsStrings;

	public Mapping() {
		this(null);
	}

	public Mapping(Map<K, V> definingMap) {
		map = new HashMap<>();
		inverseMap = new HashMap<>();
		domain = new ArrayList<>();
		range = new ArrayList<>();
		mapOfStrings = new HashMap<>();
		rangeAsStrings = new ArrayList<>();

		defineBy(definingMap);
	}

	public void clear() {
		map.clear();
		inverseMap.clear();
		domain.clear();
		range.clear();
		mapOfStrings.clear();
		invertible = true;
	}

	public Map<K, V> getMap() {
		return map;
	}

	public Map<V, K> getInverseMap() {
		return inverseMap;
	}

	public Map<String, V> getMapOfStrings() {
		return mapOfStrings;
	}

	public boolean isInvertible() {
		return invertible;
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public List<K> getDomain() {
		return domain;
	}

	public NavigableSet<K> getDomainNaturallyOrdered() {
		return new TreeSet<>(domain);
	}

	public NavigableSet<String> getDomainAsStringsCollated() {
		TreeSet<String> ts = new TreeSet<>(Globals.getCollator());
		ts.addAll(mapOfStrings.keySet());
		return ts;
	}

	public List<V> getRange() {
		return range;
	}

	public NavigableSet<V> getRangeNaturallyOrdered() {
		return new TreeSet<>(range);
	}

	public NavigableSet<String> getRangeAsStringsCollated() {
		TreeSet<String> ts = new TreeSet<>(Globals.getCollator());
		ts.addAll(rangeAsStrings);
		return ts;
	}

	public void addPair(K k, V v) {
		domain.add(k);
		range.add(v);
		rangeAsStrings.add("" + v);
		mapOfStrings.put("" + k, v);

		if (invertible) {
			if (inverseMap.get(v) != null)
				// there is already an inverse assignment,
				// the next one corrupts the function
				invertible = false;
			else
				inverseMap.put(v, k);
		}
	}

	public void defineBy(Map<K, V> m) {
		clear();
		if (m == null)
			return;

		map = m;
		Iterator<K> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			K k = iter.next();
			V v = map.get(k);
			if (v == null)
				logging.info(this, " " + k + " mapped to null in map " + m);
			else

				addPair(k, v);
		}
	}

	public Mapping<K, V> restrictedTo(java.util.Set<K> partialDomain) {
		HashMap<K, V> restrictedMap = new HashMap<>();
		for (K key : partialDomain) {
			if (domain.contains(key)) {
				restrictedMap.put(key, map.get(key));
			}
		}

		return new Mapping<>(restrictedMap);
	}

	@Override
	public String toString() {
		return "de.uib.utilities.Mapping defined by Map " + map;
	}
}
