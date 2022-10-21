package de.uib.configed.type.licences;
import java.util.*;
import de.uib.configed.type.*;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.logging.*;

public class LicenceStatisticsRow extends HashMap<String, String>
{
	//callkeys
	public final static String idKEY = "licensePoolId";
	public final static String licenseOptionsKEY = "licence_options";
	public final static String usedByOpsiKEY = "used_by_opsi";
	public final static String remainingOpsiKEY = "remaining_opsi";
	public final static String swInventoryUsedKEY = "SWinventory_used";
	public final static String swinventoryRemainingKEY = "SWinventory_remaining";
	
	private final static String ZERO = "0";
	
	protected ExtendedInteger allowedUsages;
	protected Integer opsiUsages;
	protected Integer swInventoryUsages;
	
	public LicenceStatisticsRow(String licencePool)
	{
		super();
		put(usedByOpsiKEY, ZERO);
		put(idKEY, licencePool);
		put(licenseOptionsKEY, ZERO);
		put(remainingOpsiKEY, ZERO);
		put(swInventoryUsedKEY,ZERO);
		put(swinventoryRemainingKEY,ZERO);
		allowedUsages = ExtendedInteger.ZERO;
		opsiUsages = 0;
		swInventoryUsages = 0;
		
	}
	
	public void setAllowedUsagesCount(ExtendedInteger count)
	{
		if (count != null)
		{
			//if (count == ExtendedInteger.INFINITE)
			//	logging.info(this, " count " + count);
			String value =  count.getDisplay();
			allowedUsages = count;
			put(licenseOptionsKEY, value);
			put(remainingOpsiKEY, value);
			put(swinventoryRemainingKEY, value);
			//logging.info(this, " we have " + this);
		}
	}
	
	public void setOpsiUsagesCount(Integer count)
	{
		if (count != null)
		{
			put(usedByOpsiKEY, count.toString());
			opsiUsages = count;
			put(remainingOpsiKEY, 
					(allowedUsages.add(-count)).getDisplay());
		}
	}
	
	
	public void setSWauditUsagesCount(Integer count)
	{
		if (count != null)
		{
			put(swInventoryUsedKEY, count.toString());
			swInventoryUsages = count;
			put(swinventoryRemainingKEY, 
					(allowedUsages.add(-count)).getDisplay());
		}
	}

}
