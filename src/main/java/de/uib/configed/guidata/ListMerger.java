/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.ArrayList;
import java.util.List;

public class ListMerger extends ArrayList<Object> {
	private static final ListMerger NO_COMMON_VALUE = new ListMerger(new ArrayList<>());
	static {
		NO_COMMON_VALUE.setHavingNoCommonValue();
	}

	private boolean havingCommonValue;

	// prevents correct recognition

	// building the merger:

	private List<?> listValue;

	public ListMerger(List<?> list) {
		super(list);
		listValue = list;
		this.havingCommonValue = true;
	}

	public void setHavingNoCommonValue() {
		havingCommonValue = false;
	}

	private static boolean equals(List<?> list1, List<?> list2) {
		if (list1 == null && list2 == null) {
			return true;
		}

		if (list1 == null || list2 == null) {
			return false;
		}

		return list1.containsAll(list2) && list2.containsAll(list1);
	}

	public ListMerger merge(List<?> listToMergeIn) {
		if (havingCommonValue && !equals(listValue, listToMergeIn)) {
			havingCommonValue = false;
			listValue = NO_COMMON_VALUE;
		}

		if (!havingCommonValue) {
			return NO_COMMON_VALUE;
		}

		return this;
	}

	@SuppressWarnings("java:S1452")
	public List<?> getValue() {
		return listValue;
	}

	@SuppressWarnings("java:S1452")
	public static List<?> getMergedList(List<?> li) {
		if (li instanceof ListMerger) {
			return ((ListMerger) li).getValue();
		} else {
			// li is ArrayList
			return li;
		}
	}
}
