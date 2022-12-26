package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.Globals;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.logging;

public class LicenceUsageEntry extends StringValuedRelationElement {
	/*
	 * describe LICENSE_ON_CLIENT:
	 * | Field | Type | Null | Key | Default | Extra
	 * | license_on_client_id | int(11) | NO | PRI | NULL | auto_increment |
	 * | softwareLicenseId | varchar(100) | NO | MUL | NULL |
	 * | licensePoolId | varchar(100) | NO | | NULL |
	 * | clientId | varchar(255) | YES | MUL | NULL |
	 * | licenseKey | varchar(100) | YES | | NULL |
	 * | notes | varchar(1024) | YES | | NULL |
	 */

	public static final String identKEY = "ident";
	public static final String idKEY = "license_on_client_id";
	public static final String licencepoolIdKEY = "licensePoolId";
	public static final String licenceIdKEY = "softwareLicenseId";
	public static final String clientIdKEY = "clientId";
	public static final String licencekeyKEY = "licenseKey";
	public static final String notesKEY = "notes";

	public static final String opsiNOMtype = "LicenseOnClient";

	private String lic4pool;

	private static List<String> KEYS;
	static {
		KEYS = new ArrayList<>();
		KEYS.add(idKEY);

		KEYS.add(licencepoolIdKEY);
		KEYS.add(licenceIdKEY);
		KEYS.add(clientIdKEY);
		KEYS.add(licencekeyKEY);
		KEYS.add(notesKEY);
	}

	public LicenceUsageEntry(String hostId, String softwareLicenceId, String licencePoolId, String licenceKey,
			String notes) {
		super();
		setAllowedAttributes(KEYS);

		if (hostId == null)
			put(clientIdKEY, "");
		else
			put(clientIdKEY, hostId);

		if (softwareLicenceId == null)
			put(licenceIdKEY, "");
		else
			put(licenceIdKEY, softwareLicenceId);

		if (licencePoolId == null)
			put(licencepoolIdKEY, "");
		else
			put(licencepoolIdKEY, licencePoolId);

		if (licenceKey == null)
			put(licencekeyKEY, "");
		else
			put(licencekeyKEY, licenceKey);

		if (notes == null)
			put(notesKEY, "");
		else
			put(notesKEY, notes);

		lic4pool = Globals.pseudokey(new String[] { get(licenceIdKEY), get(licencepoolIdKEY) });
	}

	public LicenceUsageEntry(Map<String, Object> entry) {
		super();
		setAllowedAttributes(KEYS);

		Set<String> reducedEntrySet = entry.keySet();
		reducedEntrySet.remove(identKEY);
		for (String key : reducedEntrySet) {
			put(key, "" + entry.get(key));
		}

		if (get(licenceIdKEY) == null || get(licencepoolIdKEY) == null)
			logging.warning(this, "missing values " + entry);

		lic4pool = Globals.pseudokey(new String[] { get(licenceIdKEY), get(licencepoolIdKEY) });
	}

	public String getId() {
		return get(idKEY);
	}

	public String getClientId() {
		return get(clientIdKEY);
	}

	public String getLicencekey() {
		return get(licencekeyKEY);
	}

	public String getLicencepool() {
		return get(licencepoolIdKEY);
	}

	public String getLicenceId() {
		return get(licenceIdKEY);
	}

	public String getLic4pool() {
		return lic4pool;
	}

	public Map<String, Object> getNOMobject() {
		Map<String, Object> m = new HashMap<>();
		m.put(clientIdKEY, getClientId());
		m.put(licenceIdKEY, getLicenceId());
		m.put(licencepoolIdKEY, getLicencepool());
		m.put("type", opsiNOMtype);
		return m;
	}

	public static String produceKey(String hostId, String licencePoolId, String licenceId) {
		return Globals.pseudokey(new String[] { hostId, licencePoolId, licenceId });
	}

	public String getPseudoKey() {
		return produceKey(getClientId(), getLicencepool(), getLicenceId());
	}

}
