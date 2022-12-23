package de.uib.configed.type.licences;

import java.util.HashMap;

import de.uib.utilities.ExtendedInteger;

public class LicenceStatisticsRow extends HashMap<String, String> {
	// callkeys
	public static final String idKEY = "licensePoolId";
	public static final String licenseOptionsKEY = "licence_options";
	public static final String usedByOpsiKEY = "used_by_opsi";
	public static final String remainingOpsiKEY = "remaining_opsi";
	public static final String swInventoryUsedKEY = "SWinventory_used";
	public static final String swinventoryRemainingKEY = "SWinventory_remaining";

	private static final String ZERO = "0";

	protected ExtendedInteger allowedUsages;
	protected Integer opsiUsages;
	protected Integer swInventoryUsages;

	public LicenceStatisticsRow(String licencePool) {
		super();
		put(usedByOpsiKEY, ZERO);
		put(idKEY, licencePool);
		put(licenseOptionsKEY, ZERO);
		put(remainingOpsiKEY, ZERO);
		put(swInventoryUsedKEY, ZERO);
		put(swinventoryRemainingKEY, ZERO);
		allowedUsages = ExtendedInteger.ZERO;
		opsiUsages = 0;
		swInventoryUsages = 0;

	}

	public void setAllowedUsagesCount(ExtendedInteger count) {
		if (count != null) {

			String value = count.getDisplay();
			allowedUsages = count;
			put(licenseOptionsKEY, value);
			put(remainingOpsiKEY, value);
			put(swinventoryRemainingKEY, value);

		}
	}

	public void setOpsiUsagesCount(Integer count) {
		if (count != null) {
			put(usedByOpsiKEY, count.toString());
			opsiUsages = count;
			put(remainingOpsiKEY, (allowedUsages.add(-count)).getDisplay());
		}
	}

	public void setSWauditUsagesCount(Integer count) {
		if (count != null) {
			put(swInventoryUsedKEY, count.toString());
			swInventoryUsages = count;
			put(swinventoryRemainingKEY, (allowedUsages.add(-count)).getDisplay());
		}
	}

}
