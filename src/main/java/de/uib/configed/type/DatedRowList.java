package de.uib.configed.type;

import java.util.ArrayList;
import java.util.List;

public class DatedRowList {

	protected List<String[]> rowList;
	protected String dateS;

	public DatedRowList() {
		rowList = new ArrayList<>();
		dateS = "";
	}

	public DatedRowList(List<String[]> rowList, String dateS) {
		this.rowList = rowList;
		this.dateS = dateS;
	}

	public List<String[]> getRows() {
		return rowList;
	}

	public String getDate() {
		return dateS;
	}

}