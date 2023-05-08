package de.uib.configed.type;

public class OpsiHwAuditDevicePropertyType {
	private String opsiDbColumnName;
	private String opsiDbColumnType;

	private String reportFunction;
	private String uiName;
	private String hwClassName;

	private Boolean displayed;

	public OpsiHwAuditDevicePropertyType(String hwClass) {
		this.hwClassName = hwClass;
	}

	public String getHwClassName() {
		return hwClassName;
	}

	public void setOpsiDbColumnName(String s) {
		opsiDbColumnName = s;
	}

	public String getOpsiDbColumnName() {
		return opsiDbColumnName;
	}

	public void setOpsiDbColumnType(String s) {
		opsiDbColumnType = s;
	}

	public String getOpsiDbColumnType() {
		return opsiDbColumnType;
	}

	public void setUiName(String s) {
		uiName = s;
	}

	public String getUiName() {
		return uiName;
	}

	public void setDisplayed(boolean b) {
		displayed = b;
	}

	public Boolean getDisplayed() {
		return displayed;
	}

	@Override
	public String toString() {
		return hwClassName + ": " + opsiDbColumnName + " (" + displayed + ")";
	}

}
