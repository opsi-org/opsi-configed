package de.uib.opsidatamodel.productstate;

import java.util.LinkedHashSet;
import java.util.Set;

public final class InstallationInfo {

	// private constructor to hide the implicit public one
	private InstallationInfo() {
	}

	// TODO WHICH values are still needed? some nowhere used
	public static final String KEY = "installationInfo";

	// valid states
	public static final int NONE = 0;
	public static final int FAILED = 2;

	public static final String NONE_STRING = "";
	public static final String FAILED_STRING = "failed";
	public static final String SUCCESS_STRING = "success";

	public static final String NONE_DISPLAY_STRING = "none";
	public static final String FAILED_DISPLAY_STRING = "failed";
	public static final String SUCCESS_DISPLAY_STRING = "success";

	public static final String MANUALLY = "manually set";

	public static final Set<String> defaultDisplayValues = new LinkedHashSet<>();
	static {
		defaultDisplayValues.add(NONE_DISPLAY_STRING);
		defaultDisplayValues.add(SUCCESS_DISPLAY_STRING);
		defaultDisplayValues.add(FAILED_DISPLAY_STRING);
	}

}
