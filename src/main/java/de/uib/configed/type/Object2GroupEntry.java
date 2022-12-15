package de.uib.configed.type;

public class Object2GroupEntry {
	public static final String TYPE_NAME = "ObjectToGroup";
	public static final String GROUP_TYPE_KEY = "groupType";
	public static final String GROUP_TYPE_HOSTGROUP = "HostGroup";
	public static final String GROUP_TYPE_PRODUCTGROUP = "ProductGroup";
	public static final String GROUP_ID_KEY = "groupId";
	public static final String MEMBER_KEY = "objectId";

	protected String groupType;
	protected String groupId;
	protected String member;

	public Object2GroupEntry(String groupType, String member, String groupId) {
		this.groupType = groupType;
		this.groupId = groupId;
		this.member = member;
	}

	public String getGroupType() {
		return groupType;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getMember() {
		return member;
	}

}
