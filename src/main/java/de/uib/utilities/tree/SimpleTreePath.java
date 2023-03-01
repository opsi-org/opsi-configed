package de.uib.utilities.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.TreePath;

public class SimpleTreePath extends ArrayList<String> implements Comparable<SimpleTreePath> {

	public SimpleTreePath() {
		super();
	}

	public SimpleTreePath(Collection<String> prototype) {
		super(prototype);
	}

	public SimpleTreePath(Object[] path) {
		super();
		for (int i = 0; i < path.length; i++) {
			super.add(path[i].toString());
		}
	}

	public Set<String> collectNodeNames() {
		HashSet<String> set = new HashSet<>();

		for (String nodename : this) {
			set.add(nodename);
		}

		return set;
	}

	@Override
	public SimpleTreePath subList(int j, int i) {
		return new SimpleTreePath(super.subList(j, i));
	}

	public String dottedString(int j, int i) {
		StringBuilder buf = new StringBuilder("");

		for (int k = j; k < i - 1; k++) {
			buf.append(get(k));
			buf.append(".");
		}
		buf.append(get(i - 1));

		return buf.toString();
	}

	public static String dottedString(int startNodeNo, TreePath path) {
		Object[] parts = path.getPath();

		if (parts.length <= startNodeNo) {
			return "";
		}

		StringBuilder res = new StringBuilder(parts[startNodeNo].toString());

		for (int i = startNodeNo + 1; i < parts.length; i++) {
			res.append(".");
			res.append(parts[i].toString());
		}

		return res.toString();
	}

	// interface Comparable
	@Override
	public int compareTo(SimpleTreePath o) {
		int result = 0;
		int i = 0;
		while (result == 0 && i < size() && i < o.size()) {
			result = get(i).compareTo(o.get(i));
			i++;
		}

		if (result == 0 && (size() < o.size())) {
			result = -1;
		}

		if (result == 0 && (size() > o.size())) {
			result = +1;
		}

		return result;
	}

}
