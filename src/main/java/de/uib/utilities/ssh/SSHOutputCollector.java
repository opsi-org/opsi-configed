package de.uib.utilities.ssh;

import java.util.ArrayList;
import java.util.List;

public class SSHOutputCollector {
	private static SSHOutputCollector instance;
	private static final List<String> values = new ArrayList<>();

	private SSHOutputCollector() {
	}

	public static SSHOutputCollector getInstance() {
		if (instance == null) {
			instance = new SSHOutputCollector();
		}
		return instance;
	}

	public void appendValue(final String value) {
		values.add(value);
	}

	public void removeValue(final String value) {
		values.remove(value);
	}

	public void removeAllValues() {
		values.clear();
	}

	public List<String> getValues() {
		return values;
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
