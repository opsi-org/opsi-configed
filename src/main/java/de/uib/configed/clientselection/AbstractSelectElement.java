package de.uib.configed.clientselection;

import java.util.ArrayList;
import java.util.List;

/**
 * An Element is a property of clients. Each Element has a number of operations
 * which can sort out some of the clients based on the data given to the
 * operation.
 */
public abstract class AbstractSelectElement {
	private String[] path;
	private String[] localizedPath;

	/**
	 * Create a new SelectElement. The path contains the group name, if this
	 * element is in any group (like a hardware group) and the name of the
	 * Element.
	 */
	protected AbstractSelectElement(String[] path, String... localizedPath) {
		this.path = path;
		this.localizedPath = localizedPath;
	}

	/** Get the non-localized path joined with '/' */
	public String getPath() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < path.length; i++)
			s.append(path[i] + '/');
		return s.substring(0, s.length() - 1);
	}

	/** Get the localized path joined with '/' */
	public String getLocalizedPath() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < localizedPath.length; i++)
			s.append(localizedPath[i] + '/');
		return s.substring(0, s.length() - 1);
	}

	public String[] getPathArray() {
		return path;
	}

	public String[] getLocalizedPathArray() {
		return localizedPath;
	}

	/** A list of Operations this element supports. */
	public abstract List<AbstractSelectOperation> supportedOperations();

	/**
	 * Returns the enumerated data, if there is any. This can be used to get a
	 * list of possible values for the user.
	 */
	public List<String> getEnumData() {
		return new ArrayList<>();
	}

	public boolean hasEnumData() {
		return false;
	}

	@Override
	public String toString() {
		return "(" + getClassName() + ", path values " + " !!! " + getPath() + " !!! " + getLocalizedPath() + " !!! )";
	}

	public String getClassName() {
		String name = getClass().getCanonicalName();
		return name.substring(name.lastIndexOf('.') + 1);
	}
}
