package de.uib.configed.type;

//import lombok.*;
import java.util.*;
import de.uib.utilities.datastructure.*;


public class GroupRelation extends Relation
{
	public GroupRelation() 
	{
		super( new ArrayList<String> () );
		attributes.add("groupId");
		attributes.add("description");
		attributes.add("notes");
	}
	
	
	public String getKey(RelationElement values)
	{
		return "" + values.get("groupId");
	}
	
}
