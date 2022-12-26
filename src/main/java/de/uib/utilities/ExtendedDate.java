package de.uib.utilities;

import java.util.Date;

public class ExtendedDate {
	static final String infiniteImport = "never";
	static final String sINFINITE = "INFINITE";

	public static final ExtendedDate INFINITE = new ExtendedDate(sINFINITE);
	public static final ExtendedDate ZERO = new ExtendedDate("1900-01-01 00:00:0");

	private Date date;
	private String sDate;

	public ExtendedDate(Object value) {
		interpretAsTimestamp(value);

	}

	private void setFromDate(Date d) {
		date = d;
		sDate = date.toString();
		// remove time
		sDate = sDate.substring(0, sDate.indexOf(' '));
	}

	private void interpretAsTimestamp(Object ob) {
		date = null;
		sDate = null;

		if (ob == null)
			return;

		if (ob instanceof String) {
			String value = ((String) ob).trim();
			if (value.equalsIgnoreCase(infiniteImport) || value.equalsIgnoreCase(sINFINITE)) {
				sDate = sINFINITE;
			} else {

				// extend

				if (value.indexOf(' ') == -1) {
					// append time for reading timestamp
					value = value + " 00:00:0";
				}
				setFromDate(java.sql.Timestamp.valueOf(value));
			}

			return;

		}

		if (ob instanceof Date) {
			setFromDate((Date) ob);
			return;
		}

	}

	@Override
	public String toString() {
		return sDate;
	}

	@Override
	public boolean equals(Object ob) {
		return (this == ob) || (ob instanceof ExtendedDate && toString().equals(ob.toString()));
	}

	public Date getDate() {
		return date;
	}

	public int compareTo(Date compareDate) {
		if (equals(INFINITE))
			return 1;

		else if (getDate().equals(compareDate))
			return 0;

		else if (getDate().after(compareDate))
			return 1;

		else
			return -1;
	}

	public int compareTo(ExtendedDate compareValue) {
		if (equals(compareValue))
			return 0;

		else if (equals(INFINITE))
			return 1;

		else if (compareValue.equals(INFINITE))
			return -1;

		else if (getDate().after(compareValue.getDate()))
			return 1;

		else
			return -1;
	}

	public static void main(String[] args) {
		System.out.println(" given " + args[0]);
		ExtendedDate myDate = new ExtendedDate(args[0]);
		System.out.println("as extended date " + myDate);
		System.out.println("as date " + myDate.getDate());
	}

}
