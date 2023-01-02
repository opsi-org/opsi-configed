package de.uib.configed.productgroup;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

public class TreeSetBuddy extends TreeSet<String> {
	public TreeSetBuddy(Collection<String> c) {
		super(c);
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	public boolean equals(TreeSetBuddy other) {
		if (other == null)
			return false;

		if (other.size() != size())
			return false;

		boolean equal = true;

		Iterator<String> iter = this.iterator();
		Iterator<String> otherIter = other.iterator();

		while (equal && iter.hasNext()) {
			String str0 = iter.next();
			String str1 = otherIter.next();

			if (!str0.equals(str1))
				equal = false;

		}

		if (equal && otherIter.hasNext())
			equal = false;

		return equal;
	}
}
