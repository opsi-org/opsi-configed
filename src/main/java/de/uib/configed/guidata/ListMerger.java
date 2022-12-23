package de.uib.configed.guidata;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Globals;

public class ListMerger extends ArrayList {
	boolean onlyPartiallyExisting;
	boolean havingCommonValue;

	public static Color noCommonValueTextcolor = Globals.backgroundGrey;
	public static Color noCommonValueBackcolor = Globals.backgroundGrey;
	public static Color noCommonKeyTextcolor = Globals.backBlue;
	public static Color noCommonKeyBackcolor = Globals.backBlue;

	public static final ListMerger NO_COMMON_VALUE = new ListMerger(new ArrayList<>());
	static {
		NO_COMMON_VALUE.setHavingNoCommonValue();
	}
	// static{ NO_COMMON_VALUE.add("NO COMMON VALUE"); } // setting a String
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
		// logging.debug(this, "equals list1, list2: " + list1 + ", " + list2);
		if (list1 == null && list2 == null)
			return true;

		if (list1 == null && list2 != null)
			return false;

		if (list1 != null && list2 == null)
			return false;

		if (list1.containsAll(list2) && list2.containsAll(list1))
			return true;

		return false;
	}

	public ListMerger merge(List listToMergeIn) {
		if (havingCommonValue) // we were yet in the state of a unique value
		{
			if (!equals(listValue, listToMergeIn)) {
				havingCommonValue = false;

				// logging.debug(this, "merge first list " + listToMergeIn + " to " + listValue
				// + " havingCommonValue " + havingCommonValue);

				listValue = NO_COMMON_VALUE;
			}
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
			return noCommonValueTextcolor;
		} else
			return Color.BLACK;
	}

	public Color getBackgroundColor() {
		if (!havingCommonValue) {
			return noCommonValueBackcolor;
		} else
			return Color.BLACK;
	}

}
