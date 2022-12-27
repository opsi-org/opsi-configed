package de.uib.utilities.thread;

import java.util.ArrayList;
import java.util.List;

public class WaitInfoString {
	private List<String> waitInfoList;
	private int current = 0;
	private String baseString;

	public WaitInfoString() {
		this("");
		init(baseString);
	}

	public WaitInfoString(String baseString) {
		this.baseString = baseString;
		if (baseString == null)
			this.baseString = "";
		init(this.baseString);
	}

	protected void init(String baseString) {
		waitInfoList = new ArrayList<>();

		waitInfoList.add(baseString + "       ");
		waitInfoList.add(baseString + " .     ");
		waitInfoList.add(baseString + " ..    ");
		waitInfoList.add(baseString + " ....  ");
		waitInfoList.add(baseString + " ..... ");

	}

	public String start() {
		current = 0;
		return next();
	}

	public String next() {
		String result = "";

		if (waitInfoList != null && current < waitInfoList.size())
			result = waitInfoList.get(current);

		if (waitInfoList != null) {
			current++;
			if (current >= waitInfoList.size())
				current = 0;
		}

		return result;
	}
}
