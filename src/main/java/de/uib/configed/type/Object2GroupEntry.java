/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

public class Object2GroupEntry {
	public static final String TYPE_NAME = "ObjectToGroup";
	public static final String GROUP_TYPE_KEY = "groupType";
	public static final String GROUP_TYPE_HOSTGROUP = "HostGroup";
	public static final String GROUP_TYPE_PRODUCTGROUP = "ProductGroup";
	public static final String GROUP_ID_KEY = "groupId";
	public static final String MEMBER_KEY = "objectId";

	private String groupId;
	private String member;

	public Object2GroupEntry(String member, String groupId) {
		this.groupId = groupId;
		this.member = member;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getMember() {
		return member;
	}
}
