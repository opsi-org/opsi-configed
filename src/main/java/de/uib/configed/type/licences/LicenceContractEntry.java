package de.uib.configed.type.licences;
import java.util.*;
import de.uib.configed.type.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.datastructure.*;

public class LicenceContractEntry extends StringValuedRelationElement
{
	public LicenceContractEntry()
	{
		super();
		allowedAttributes = Table_LicenceContracts.ALLOWED_ATTRIBUTES;
	}
	
	public LicenceContractEntry(Map<String,Object> m)
	{
		super();
		setAllowedAttributes(Table_LicenceContracts.ALLOWED_ATTRIBUTES);
		produceFrom(m);
	}
	
	private String removeTime(String datetime)
	{
		if (datetime != null && datetime.length() > 0)
		{
			int idx = datetime.indexOf(" ");
			if (idx > -1)
				return datetime.substring(0,idx);
		}
		
		return datetime;
	}
			
			
	
	@Override
	protected void produceFrom(Map<String, ? extends Object> map)
	{
		super.produceFrom(map);
		//logging.info(this, "produceFrom " + map);
		put(Table_LicenceContracts.idDBKEY, get(Table_LicenceContracts.idKEY));
		remove(Table_LicenceContracts.typeKEY);
		remove(Table_LicenceContracts.identKEY);
		
		put(Table_LicenceContracts.conclusionDateKEY, removeTime( get(  Table_LicenceContracts.conclusionDateKEY ) ));
			
		put(Table_LicenceContracts.notificationDateKEY, removeTime( get( Table_LicenceContracts.notificationDateKEY ) ));
		put(Table_LicenceContracts.expirationDateKEY, removeTime( get(  Table_LicenceContracts.expirationDateKEY ) ));
			
		//logging.info(this, "produceFrom result " +this);
	}
	
	
	public String getId()
	{
		return get(Table_LicenceContracts.idDBKEY);
	}
}
	

