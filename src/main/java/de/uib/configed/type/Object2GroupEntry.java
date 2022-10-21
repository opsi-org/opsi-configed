package de.uib.configed.type;

import java.util.*;

public class Object2GroupEntry
{
	public final static String TYPE_NAME = "ObjectToGroup";
	public final static String GROUP_TYPE_KEY = "groupType";
	public final static String GROUP_TYPE_HOSTGROUP = "HostGroup";
	public final static String GROUP_TYPE_PRODUCTGROUP = "ProductGroup";
	public final static String GROUP_ID_KEY = "groupId";
	public final static String MEMBER_KEY = "objectId";

	protected String groupType;
	protected String groupId;
	protected String member;


	public Object2GroupEntry(String groupType, String member, String groupId)
	{
		this.groupType = groupType;
		this.groupId = groupId;
		this.member = member;
	}

	public String getGroupType()
	{
		return groupType;
	}
	public String getGroupId()
	{
		return groupId;
	}
	public String getMember()
	{
		return member;
	}




}
