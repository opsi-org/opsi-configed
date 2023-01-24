package de.uib.configed.type;

public class HWAuditClientEntry {
	// private constructor to hide the implicit public one
	private HWAuditClientEntry() {
	}

	// TODO What is the use for this class???????

	public static final String HOST_KEY = "hostId";
	public static final String NAME_KEY = "name";
	public static final String STATE_KEY = "state"; // value is Integer
	public static final String HARDWARE_CLASS_KEY = "hardwareClass";

	public static final String LAST_SEEN_KEY = "lastseen";
	public static final String FIRST_SEENKEY = "firstseen";

	public static final String DESCRIPTION_KEY = "description";

	public static final String TYPE_KEY = "type";
	public static final String OPSI_NOM_TYPE = "AuditHardwareOnHost";
}
