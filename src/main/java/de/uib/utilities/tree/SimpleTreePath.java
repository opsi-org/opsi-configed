/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.tree;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.tree.TreePath;

public class SimpleTreePath extends ArrayList<String> implements Comparable<SimpleTreePath> {
	public SimpleTreePath() {
		super();
	}

	public SimpleTreePath(Collection<String> prototype) {
		super(prototype);
	}

	@Override
	public SimpleTreePath subList(int j, int i) {
		return new SimpleTreePath(super.subList(j, i));
	}

	public String dottedString(int j, int i) {
		StringBuilder buf = new StringBuilder();

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

		if (result == 0 && size() < o.size()) {
			result = -1;
		}

		if (result == 0 && size() > o.size()) {
			result = +1;
		}

		return result;
	}
}
