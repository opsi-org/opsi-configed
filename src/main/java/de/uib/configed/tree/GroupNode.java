package de.uib.configed.tree;

public class GroupNode extends IconNode {
	private String description;
	private boolean childsArePersistent = true;
	private boolean allowingOnlyGroupChilds;
	private boolean allowingSubGroups = true;
	private boolean immutable;
	private boolean fixed;

	public GroupNode(Object userObject, String description) {
		super(userObject, true);
		this.description = description;
	}

	public GroupNode(Object userObject) {
		this(userObject, "" + userObject);
	}

	public void setChildsArePersistent(boolean b) {
		childsArePersistent = b;
	}

	public boolean isChildsArePersistent() {
		return childsArePersistent;
	}

	public void setAllowsOnlyGroupChilds(boolean b) {
		allowingOnlyGroupChilds = b;
	}

	public boolean allowsOnlyGroupChilds() {
		return allowingOnlyGroupChilds;
	}

	public void setAllowsSubGroups(boolean b) {
		allowingSubGroups = b;
	}

	public boolean allowsSubGroups() {
		return allowingSubGroups;
	}

	public void setImmutable(boolean b) {
		immutable = b;
	}

	public boolean isImmutable() {
		return immutable;
	}

	public void setFixed(boolean b) {
		fixed = b;
	}

	public boolean isFixed() {
		return fixed;
	}

}
