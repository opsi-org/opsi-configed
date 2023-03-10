package de.uib.configed.guidata;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Globals;

public class ListMerger extends ArrayList {
	boolean onlyPartiallyExisting;
	boolean havingCommonValue;

	public static final ListMerger NO_COMMON_VALUE = new ListMerger(new ArrayList<>());
	static {
		NO_COMMON_VALUE.setHavingNoCommonValue();
	}

	// prevents correct recognition

	// building the merger:

	private List listValue;

	public ListMerger(List list) {
		super(list);
		listValue = list;
		this.havingCommonValue = true;
	}

	public void setHavingNoCommonValue() {
		havingCommonValue = false;
	}

	private boolean equals(List list1, List list2) {

		if (list1 == null && list2 == null)
			return true;

		if (list1 == null || list2 == null)
			return false;

		return list1.containsAll(list2) && list2.containsAll(list1);
	}

	public ListMerger merge(List listToMergeIn) {
		if (havingCommonValue && !equals(listValue, listToMergeIn)) {
			havingCommonValue = false;

			listValue = NO_COMMON_VALUE;
		}

		if (!havingCommonValue)
			return NO_COMMON_VALUE;

		return this;
	}

	public boolean hasCommonValue() {
		return havingCommonValue;
	}

	public List getValue() {
		return listValue;
	}

	public static List getMergedList(List li) {
		if (li instanceof ListMerger)
			return ((ListMerger) li).getValue();
		else
			// li is ArrayList
			return li;
	}

	public Color getTextColor() {
		if (!havingCommonValue) {
			return Globals.LIST_MERGER_NO_COMMON_VALUE_TEXT_COLOR;
		} else
			return Globals.LIST_MERGER_NORMAL_VALUE_TEXT_COLOR;
	}

	public Color getBackgroundColor() {
		if (!havingCommonValue) {
			return Globals.LIST_MERGER_NO_COMMON_VALUE_BACKGROUND_COLOR;
		} else
			return Globals.LIST_MERGER_NORMAL_VALUE_BACKGROUND_COLOR;
	}

}
