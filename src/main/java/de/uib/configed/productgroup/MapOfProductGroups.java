package de.uib.configed.productgroup;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapOfProductGroups extends HashMap<String, TreeSetBuddy> {

	public MapOfProductGroups(Map<String, Set<String>> fName2ProductGroup) {
		super();

		for (Entry<String, Set<String>> product : fName2ProductGroup.entrySet()) {
			TreeSetBuddy set = new TreeSetBuddy(product.getValue());
			super.put(product.getKey(), set);
		}
	}
}
