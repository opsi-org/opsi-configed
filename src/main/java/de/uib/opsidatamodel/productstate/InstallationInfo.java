package de.uib.opsidatamodel.productstate;

import java.util.LinkedHashSet;

public class InstallationInfo {
	public static final String KEY = "installationInfo";

	// valid states
	public static final int NONE = 0;
	public static final int FAILED = 2;

	public static final String NONEstring = "";
	public static final String FAILEDstring = "failed";
	public static final String SUCCESSstring = "success";

	public static final String NONEdisplayString = "none";
	public static final String FAILEDdisplayString = "failed";
	public static final String SUCCESSdisplayString = "success";

	public static final String MANUALLY = "manually set";

	public static final LinkedHashSet<String> defaultDisplayValues = new LinkedHashSet<String>();
	static {
		defaultDisplayValues.add(NONEdisplayString);
		defaultDisplayValues.add(SUCCESSdisplayString);
		defaultDisplayValues.add(FAILEDdisplayString);
	}

}
