package de.uib.utilities;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import de.uib.utilities.logging.logging;

/**
 * This class contains global constants and functions for the library Copyright:
 * Copyright (c) uib 2001-2018
 */

public class Globals {
	public static String APPNAME = "";
	public static Image mainIcon = null;
	public static String iconresourcename = "";

	public static final Font defaultFont = new java.awt.Font("SansSerif", 0, 11);
	public static final Font defaultFontStandardBold = new java.awt.Font("SansSerif", Font.BOLD, 11);
	public static final Font defaultFontSmall = new java.awt.Font("SansSerif", 0, 9);
	public static final Font defaultFontSmallBold = new java.awt.Font("SansSerif", Font.BOLD, 9);
	// public static final Font defaultFont12 = new java.awt.Font("SansSerif", 0,
	// 12);
	public static final Font defaultFontBig = new java.awt.Font("SansSerif", 0, 12);
	public static final Font defaultFontBold = new java.awt.Font("SansSerif", Font.BOLD, 12);
	public static final Font defaultFontStandard = defaultFont;

	/*
	 * // Get all font family names
	 * GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	 * String fontNames[] = ge.getAvailableFontFamilyNames();
	 * 
	 * // Iterate the font family names
	 * for (int i=0; i<fontNames.length; i++) {
	 * logging.debug("FONT ==>>> " + fontNames[i]);
	 * }
	 */

	public static final Color backgroundWhite = new Color(245, 245, 245);
	public static final Color backgroundGrey = new Color(220, 220, 220);
	public static final Color backgroundLightGrey = new Color(230, 230, 230);
	public static final Color backLightBlue = new Color(220, 228, 242); // new Color (210,216,232);
	public static final Color backVeryLightBlue = new Color(240, 240, 245);
	// public static final Color backLight = new Color (220,230,255);
	public static final Color backBlue = new Color(200, 200, 250);
	public static final Color backNimbus = new Color(214, 219, 222);
	public static final Color backNimbusLight = new Color(224, 229, 235);

	public static final Color backYellow = new java.awt.Color(255, 255, 182);
	public static final Color backLightYellow = new java.awt.Color(250, 248, 221);
	public static final Color backLightGreen = new java.awt.Color(230, 255, 210);
	public static final Color backTabsColor = new java.awt.Color(206, 223, 247);
	public static final Color darkOrange = new java.awt.Color(218, 180, 4);
	public static final Color lightBlack = new Color(30, 30, 30);
	public static final Color textGrey = new Color(80, 80, 80);
	public static final Color blue = new Color(30, 30, 100);
	public static final Color blueGrey = new Color(180, 190, 190);
	public static final Color violett = new Color(160, 170, 200);
	public static final Color greyed = new Color(150, 150, 150);

	public static final Color opsiLogoBlue = new Color(106, 128, 174);
	public static final Color opsiLogoLightBlue = new Color(195, 200, 222);

	public static final Color defaultTableCellBgColor1 = new Color(255, 255, 255);
	public static final Color defaultTableCellBgColor2 = backLightYellow; // new Color (224,231,255);
	public static final Color backgroundColorEditFields = backLightYellow;
	public static final Color defaultTableHeaderBgColor = new Color(222, 231, 247); // new Color (206,223,247);
	public static final Color defaultTableCellSelectedBgColor = new Color(184, 207, 229);
	public static final Color defaultTableCellSelectedBgColorNotEditable = new Color(189, 207, 231);

	public static final int toolTipInitialDelayMs = 1000;
	public static final int toolTipDismissDelayMs = 20000;
	public static final int toolTipReshowDelayMs = 0;

	public static final int dateFormatStylePattern = DateFormat.LONG;

	public static final int vGapSize = 10;
	public static final int hGapSize = 10;

	public static final int buttonHeight = 24;
	public static final int smallHeight = 18;
	public static final int lineHeight = 28;
	public static final int progressBarHeight = 10;
	public static final int tableRowHeight = 16; // 16 seems to be the default, underscore are not visible; for lyinng
													// inside table cells, 21 is necessary
	public static final int buttonWidth = 140;
	public static final int iconWidth = 60;
	public static final int labelWidth = 80;
	public static final int squareButtonWidth = 24;

	public static final Dimension buttonDimension = new Dimension(buttonWidth, buttonHeight);
	public static final Dimension lowerButtonDimension = new Dimension(buttonWidth, buttonHeight - 4);
	public static final Dimension smallButtonDimension = new Dimension(buttonWidth / 2, buttonHeight + 4);
	public static final Dimension textfieldDimension = new Dimension(buttonWidth, lineHeight);
	public static final Dimension labelDimension = new Dimension(labelWidth, lineHeight);
	public static final Dimension shortlabelDimension = new Dimension(60, lineHeight);
	public static final Dimension counterfieldDimension = new Dimension(60, lineHeight);
	public static final Dimension newSmallButton = new Dimension(30, 30);
	public static final Dimension filechooserSize = new Dimension(600, 400);

	public static int smallFramesDistanceFromLeft = 80;
	public static int smallFramesDistanceFromTop = 80;
	public static int bigFramesDistanceFromLeft = 60;
	public static int bigFramesDistanceFromTop = 40;

	// public static int smallFramesDistanceFromLeftOnDefaultDisplay;

	public static Integer startX;
	public static Integer startY;
	public static Integer startWidth;
	public static Integer startHeight;

	private static java.text.Collator alphaCollator = null;

	public static java.text.Collator getCollator() {
		if (alphaCollator == null) {
			alphaCollator = java.text.Collator.getInstance();
			// alphaCollator.setStrength(java.text.Collator.PRIMARY);
			alphaCollator.setStrength(java.text.Collator.IDENTICAL);

		}
		return alphaCollator;
	}

	public static Container masterFrame;

	// mainIcon =
	// Toolkit.getDefaultToolkit().createImage(ConfigedGlobals.class.getResource("opsi.gif"));

	public static final String imageBase = "images";

	public static String imageBaseAbsolute;

	public static String getImagesBaseAbsolute() {
		if (imageBaseAbsolute == null)
			imageBaseAbsolute = Globals.class.getResource(imageBase).toString();
		return imageBaseAbsolute;
	}

	public final static String fileseparator = "/";

	public static boolean isWindows() {
		Runtime rt = Runtime.getRuntime();
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().startsWith("windows");
	}

	public static java.net.URL getImageResourceURL(String relPath) {
		String resourceS = imageBase + fileseparator + relPath;
		java.net.URL imgURL = Globals.class.getResource(resourceS);
		// logging.debug ( " ---- imgURL " + imgURL );
		logging.info("getImageResourceURL  found for " + resourceS + " url: " + imgURL);
		if (imgURL != null) {
			return imgURL;
		} else {
			logging.warning("Couldn't find file  " + relPath);
			return null;
		}
	}

	public static javax.swing.ImageIcon createImageIcon(String path, String description) {
		// logging.debug ( " ---- image path: " + imageBase + fileseparator + path
		// );
		java.net.URL imgURL = Globals.class.getResource(imageBase + fileseparator + path);
		// logging.debug ( " ---- imgURL " + imgURL );
		if (imgURL != null) {
			return new javax.swing.ImageIcon(imgURL, description);
		} else {
			logging.warning("Couldn't find file: " + path);
			return null;
		}
	}

	private static Map objects;

	public static Map getMap() {
		if (objects == null) {
			objects = new HashMap();

			objects.put("mainIcon", mainIcon);
			objects.put("defaultFont", defaultFont);
			objects.put("APPNAME", APPNAME);
		}

		return objects;
	}

	public static Boolean interpretAsBoolean(Object value) {
		// logging.debug("Globals: interpretAsBoolean " + value);

		if (value == null)
			return null;

		if (value instanceof Boolean) {
			// logging.debug("Globals: interpretAsBoolean based on Boolean ");
			return (Boolean) value;
		}

		if (value instanceof Integer) {
			// logging.debug("Globals: interpretAsBoolean based on Integer ");
			int val = (Integer) value;
			if (val == 1)
				return true;
			else if (val == 0)
				return false;

			else
				throw new IllegalArgumentException("" + value + " cannot be interpreted as boolean");
		}

		if (value instanceof String) {
			// logging.debug("Globals: interpretAsBoolean based on String ");

			String val = ((String) value).toLowerCase();

			if (val.equals(""))
				return null;

			if (val.equals("true"))
				return true;

			if (val.equals("false"))
				return false;

			if (val.equals("1"))
				return true;

			if (val.equals("0"))
				return false;

			throw new IllegalArgumentException(" " + value + " cannot be interpreted as boolean");
		}

		throw new IllegalArgumentException(" " + value + " cannot be interpreted as boolean");
	}

	public static String driverType = "";

	public static String formT(String timeExpression) {
		if (driverType.equals("MSSQL"))
			return " convert(datetime, " + timeExpression + ", 121) "; // MSSQL
		else
			return timeExpression; // standard
		// return " convert(char(19, convert(datetime, " + timeExpression + ", 121),
		// 121) "; //MSSQL, convert back to string

	}

	public static String getMinutes() {
		String sqlNow = new java.sql.Timestamp(new java.util.GregorianCalendar().getTimeInMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(':'));
		sqlNow = sqlNow.replace(' ', '-');
		// sqlNow = sqlNow.replace(':', '-');

		return sqlNow;
	}

	public static String getSeconds() {
		String sqlNow = new java.sql.Timestamp(new java.util.GregorianCalendar().getTimeInMillis()).toString();

		// logging.debug(" sqlNow " + sqlNow);
		int i = sqlNow.lastIndexOf(' ');
		String date = sqlNow.substring(0, i);
		date = date.replace(' ', '-');
		String time = sqlNow.substring(i + 1);
		time = time.substring(0, time.indexOf('.'));

		return date + "_" + time;

		/*
		 * sqlNow = sqlNow.substring(0, sqlNow.indexOf('.'));
		 * sqlNow = sqlNow.replace(' ', '-');
		 * sqlNow.replace, sqlNow.lastIndexOf('_'));
		 * sqlNow = sqlNow.replace(':', '-');
		 * 
		 * return sqlNow;
		 */
	}

	public static String getDate(boolean justNumbers) {
		String sqlNow = new java.sql.Timestamp(new java.util.GregorianCalendar().getTimeInMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(' '));

		if (justNumbers)
			sqlNow = sqlNow.replaceAll("-", "");

		return sqlNow;
	}

	public static Date getToday() {
		return new java.sql.Timestamp(new java.util.GregorianCalendar().getTimeInMillis());
	}

	private static String formatlNumberUpTo99(long n) {
		if (n < 10)
			return "0" + n;
		else
			return "" + n;
	}

	public static String giveTimeSpan(final long millis) {
		long seconds;
		long remseconds;
		String remSecondsS;
		long minutes;
		long remminutes;
		String remMinutesS;
		long hours;
		String hoursS;

		seconds = millis / 1000;
		minutes = seconds / 60;
		remseconds = seconds % 60;

		hours = minutes / 60;
		remminutes = minutes % 60;

		remSecondsS = formatlNumberUpTo99(remseconds);
		remMinutesS = formatlNumberUpTo99(remminutes);
		hoursS = formatlNumberUpTo99(hours);

		String result = "" + hoursS + ":" + remMinutesS + ":" + remSecondsS;
		// logging.info(this, "giveTimeSpan for millis " + millis + " " + result);

		return result;
	}

	public static String getStringValue(Object s) {
		if (s == null)
			return "";
		/*
		 * if (s instanceof String)
		 * return s;
		 */

		return s.toString();
	}

	public static java.util.ArrayList<String> takeAsStringList(java.util.List<Object> list) {
		java.util.ArrayList<String> result = new java.util.ArrayList<String>();

		if (list == null)
			return result;

		for (Object val : list) {
			result.add((String) val);
		}

		return result;
	}

	public final static String pseudokeySeparator = ";";

	public static String pseudokey(Vector<Object> partialvalues) {
		StringBuffer resultBuffer = new StringBuffer("");

		if (partialvalues.size() > 0) {
			resultBuffer.append(partialvalues.get(0));

			for (int i = 1; i < partialvalues.size(); i++) {
				resultBuffer.append(pseudokeySeparator);
				resultBuffer.append("" + partialvalues.get(i));
			}
		}

		return resultBuffer.toString();
	}

	public static String pseudokey(String[] partialvalues) {
		StringBuffer resultBuffer = new StringBuffer("");

		if (partialvalues.length > 0) {
			resultBuffer.append(partialvalues[0]);

			for (int i = 1; i < partialvalues.length; i++) {
				resultBuffer.append(pseudokeySeparator);
				resultBuffer.append(partialvalues[i]);
			}
		}

		return resultBuffer.toString();
	}

	final static int tooltipLineLength = 50;
	final static int uncertainty = 20;

	public static String wrapToHTML(String s) {
		StringBuffer result = new StringBuffer("<html>");
		String remainder = s;
		while (remainder.length() > 0) {
			de.uib.utilities.logging.logging.debug("Globals, remainder " + remainder);
			if (remainder.length() <= tooltipLineLength) {
				result.append(remainder.replace("\\n", "<br />"));
				remainder = "";
				break;
			}
			result.append(remainder.substring(0, tooltipLineLength).replace("\\n", "<br />"));

			int testspan = min(remainder.length() - tooltipLineLength, uncertainty);

			String separationString = remainder.substring(tooltipLineLength, tooltipLineLength + testspan);

			boolean found = false;
			int i = 0;
			de.uib.utilities.logging.logging.debug("Globals, separationString " + separationString);

			while (!found && i < testspan) {
				if (separationString.charAt(i) == ' ' || separationString.charAt(i) == '\n'
						|| separationString.charAt(i) == '\t') {
					found = true;
					if (separationString.charAt(i) == '\n')
						result.append("<br />");
				} else
					i++;
			}

			result.append(separationString.substring(0, i));
			result.append("<br />");
			int end = max(remainder.length(), tooltipLineLength);
			remainder = remainder.substring(tooltipLineLength + i, end);
		}

		result.append("</html>");
		return result.toString();
	}

	public static int max(int a, int b) {
		int m = a;
		if (b > a)
			m = b;
		return m;
	}

	public static int min(int a, int b) {
		int m = a;
		if (b < a)
			m = b;
		return m;
	}

	private static Integer stringCompareAsInt(String s1, String s2) throws NumberFormatException {
		// logging.debug ( " compare int " + s1 + " " + s2 );
		if (s1 == null && s2 == null)
			return 0;
		if (s1 == null)
			return -1;
		if (s2 == null)
			return +1;

		String s1A = s1.trim();
		String s2A = s2.trim();

		if (s1A.equals(s2A))
			return 0;

		if (s1A.length() == 0)
			return -1;

		if (s2A.length() == 0)
			return +1;

		int val1 = Integer.valueOf(s1A);
		int val2 = Integer.valueOf(s2A);
		// logging.debug ( " compare int " + val1 + " " + val2 );
		return val1 - val2;
	}

	public static Integer compareDottedNumberStrings(final String ver1, final String ver2)
			throws NumberFormatException {
		// logging.debug ( " ver1 " + ver1 );
		// logging.debug ( " ver2 " + ver2 );

		if (ver1 == null && ver2 == null)
			return 0;
		if (ver1 == null)
			return -1;
		if (ver2 == null)
			return +1;
		if (ver1.equals(ver2))
			return 0;

		String ver1A = ver1.replace('_', '.');
		String ver2A = ver2.replace('_', '.');

		String[] ver1parts = ver1A.split("\\.");
		String[] ver2parts = ver2A.split("\\.");

		// logging.debug ( " ver1parts " + Arrays.toString( ver1parts ) );
		// logging.debug ( " ver2parts " + Arrays.toString( ver2parts ) );

		int i = 0;
		int result = 0;

		while (result == 0 && i < ver1parts.length && i < ver2parts.length) {
			result = stringCompareAsInt(ver1parts[i], ver2parts[i]);
			if (result == 0)
				i++;
		}

		return result;
	}

	public static int compareOpsiVersions(final String number1, final String number2) {
		if (number1 == null)
			throw new IllegalArgumentException("Number1 can not be null");
		if (!number1.matches("[0-9]+(\\.[0-9]+)*"))
			throw new IllegalArgumentException("Invalid number1 format");

		if (number2 == null)
			throw new IllegalArgumentException("Number2 can not be null");
		// if (number2 == null) return 1;
		if (!number2.matches("[0-9]+(\\.[0-9]+)*"))
			throw new IllegalArgumentException("Invalid number2 format");
		String[] n1Parts = number1.split("\\.");
		String[] n2Parts = number2.split("\\.");
		int length = Math.max(n1Parts.length, n2Parts.length);
		for (int i = 0; i < length; i++) {
			int n1Part = i < n1Parts.length ? Integer.parseInt(n1Parts[i]) : 0;
			int n2Part = i < n2Parts.length ? Integer.parseInt(n2Parts[i]) : 0;
			if (n1Part < n2Part)
				return -1;
			if (n1Part > n2Part)
				return 1;
		}
		return 0;
	}

	public static boolean checkCollection(Object source, String location, String cName, Collection c) {
		boolean result = (c != null);
		if (result) {
			if (c instanceof Collection) {
				logging.info(source.getClass().getName() + " " + cName + " has size  " + ((Collection) c).size());
			} else if (c instanceof Map) {
				logging.info(source.getClass().getName() + " " + cName + " has size  " + ((Map) c).size());
			} else {
				logging.info(source.getClass().getName() + " " + cName + " is neither a Collection nor a Map  ");
				result = false;
			}
		} else
			logging.info(source.getClass().getName() + " " + cName + " is null");

		return result;
	}

	public static String makeHTMLlines(String s) {
		if (s == null || s.trim().startsWith("<"))
			return s;

		final int maxLineLength = 80;

		StringBuffer b = new StringBuffer("<html>");
		int charsInLine = 0;
		boolean indentDone = false;
		int lineIndent = 0;
		for (int c = 0; c < s.length(); c++) {
			charsInLine++;
			switch (s.charAt(c)) {
			case ' ':
				b.append("&nbsp;");
				if (!indentDone)
					lineIndent = lineIndent + 1;
				break;
			case '\t':
				b.append("&nbsp;&nbsp;&nbsp;");
				if (!indentDone)
					lineIndent = lineIndent + 3;
				break;
			case '\n':
				b.append("<br/>");
				indentDone = false;
				charsInLine = 0;
				lineIndent = 0;
				break;
			default:
				indentDone = true;
				b.append(s.charAt(c));
			}
			if (charsInLine >= maxLineLength) {
				if (c + 1 < s.length()) {
					if ((s.charAt(c + 1) == ' ') || (s.charAt(c + 1) == '\t') || (s.charAt(c + 1) == '\n')) {
						c++;
						b.append("<br/>");
						if (s.charAt(c) != '\n') {
							while (lineIndent > 0) {
								lineIndent--;
								charsInLine++;
								b.append("&nbsp;");
							}
						}
						charsInLine = 0;
						indentDone = false;
						lineIndent = 0;
					}
				}
			}
		}

		b.append("</html>");

		return b.toString();
	}

	static private Rectangle getMaxDevice() {
		Rectangle result;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		result = new Rectangle(1, 1);
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i = 0; i < gc.length; i++) {
				int w = gc[i].getBounds().width;
				int h = gc[i].getBounds().height;
				// logging.info("maxBounds: compare " + result + " to " + gc[i].getBounds());
				if (w > result.width || h > result.height)
					result = new Rectangle(w, h);
			}
		}

		logging.info("maxBounds: giving " + result);

		return result;
	}

	static private Rectangle getMinDevice() {
		Rectangle result = getMaxDevice();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		result = new Rectangle(1600, 1200);
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i = 0; i < gc.length; i++) {
				int w = gc[i].getBounds().width;
				int h = gc[i].getBounds().height;
				// logging.info("minimalBounds: compare " + result + " to " +
				// gc[i].getBounds());
				if (w < result.width || h < result.height)
					result = new Rectangle(w, h);
			}
		}

		logging.info("minimalBounds: giving " + result);

		return result;
	}

	static public Rectangle buildLocationOnDefaultDisplay(int intendedWidth, int intendedHeight, int placementX,
			int placementY) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		int x = gc.getBounds().x + placementX;
		int y = gc.getBounds().y + placementY;
		logging.info(
				"Globals: x " + gc.getBounds().x + " + " + placementX + ", y " + gc.getBounds().y + " + " + placementY);
		return new Rectangle(x, y, intendedWidth, intendedHeight);

	}

	static public Rectangle buildLocationOnDefaultDisplay(int intendedWidth, int intendedHeight, int placementX) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		int x = gc.getBounds().x + placementX;
		int y = gc.getBounds().y + (gc.getBounds().height) / 2;
		logging.info("Globals: x " + gc.getBounds().x + " + " + placementX + ", y " + gc.getBounds().y
				+ " + half of height: " + ((gc.getBounds().height) / 2));
		return new Rectangle(x, y, intendedWidth, intendedHeight);

	}

	static public Rectangle buildLocation(javax.swing.JFrame f, int placementX, int placementY) {
		return buildLocation(f, f.getWidth(), f.getHeight(), placementX, placementY);
	}

	static public Rectangle buildLocation(int intendedWidth, int intendedHeight, int placementX, int placementY) {
		return buildLocation(null, intendedWidth, intendedHeight, placementX, placementY);
	}

	static public Rectangle buildLocation(javax.swing.JFrame f, int intendedWidth, int intendedHeight, int placementX,
			int placementY) {

		int width = intendedWidth;
		int height = intendedHeight;

		logging.info("buildLocation startX, startY, startWidth, startHeight " + startX + ", " + startY + ", "
				+ startWidth + ", " + startHeight);

		if (startX != null && startY != null && startX != 0 && startY != 0)
		// take given values which could e.g. be retrieved from a previous session
		{
			placementX = startX;
			placementY = startY;

			if (startWidth != null && startHeight != null && startWidth != 0 && startHeight != 0) {
				width = startWidth;
				height = startHeight;
			}

		} else {

			Rectangle minBounds = getMinDevice();

			if (intendedWidth + 2 * bigFramesDistanceFromLeft > minBounds.width) {
				intendedWidth = minBounds.width - 2 * bigFramesDistanceFromLeft;
			}

			if (intendedHeight + 2 * bigFramesDistanceFromTop > minBounds.height) {
				intendedHeight = minBounds.height - 2 * bigFramesDistanceFromTop;
			}

			if (f != null) {
				f.setSize(intendedWidth, intendedHeight);
				logging.info("buildLocation " + f);
			}

			width = intendedWidth;
			height = intendedHeight;

			if (width + placementX > minBounds.width) {
				logging.info("buildLocation  width + placementX > minBounds.width " + width + ", " + placementX + ", "
						+ minBounds.width);
				placementX = bigFramesDistanceFromLeft;
				width = minBounds.width - 2 * placementX;
				logging.info("buildLocation  width , placementX " + width + ", " + placementX);
			} else if (placementX == 0) {
				// center in minBounds
				logging.info("buildLocation  width, minBounds.width " + width + ", " + minBounds.width);

				placementX = (minBounds.width - width) / 2;

			}

			logging.info("buildLocation placementX " + placementX);

			if (height + placementY > minBounds.height) {
				logging.info("buildLocation  height + placementY > minBounds.height " + height + ", " + placementY
						+ ", " + minBounds.height);
				placementY = bigFramesDistanceFromTop;
				height = minBounds.height - 2 * placementY;
				logging.info("buildLocation  height + placementY " + height + ", " + placementY);
			} else if (placementY == 0) {
				// center in minBounds
				logging.info(" locate given minBounds.height, height  " + minBounds.width + ", " + height);
				placementY = (minBounds.height - height) / 2;

			}
			logging.info("buildLocation placementY " + placementY);
		}

		return new Rectangle(placementX, placementY, width, height);
	}

	/*
	 * find current display
	 * cf.
	 * https://stackoverflow.com/questions/2234476/how-to-detect-the-current-display
	 * -with-java
	 * 
	 * GraphicsDevice myDevice = myFrame.getGraphicsConfiguration().getDevice();
	 * for(GraphicsDevice
	 * gd:GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()){
	 * if(
	 * frame.getLocation().getX() >=
	 * gd.getDefaultConfiguration().getBounds().getMinX() &&
	 * frame.getLocation().getX() <
	 * gd.getDefaultConfiguration().getBounds().getMaxX() &&
	 * frame.getLocation().getY() >=
	 * gd.getDefaultConfiguration().getBounds().getMinY() &&
	 * frame.getLocation().getY() <
	 * gd.getDefaultConfiguration().getBounds().getMaxY()
	 * )
	 * myDevice=gd;
	 * }
	 */

	public static String usedMemory() {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();

		return " " + (((total - free) / 1024) / 1024) + " MB ";
	}

	public static String getCLIparam(String question, boolean password) {
		java.io.Console con = System.console();
		if (con == null)
			return "";
		System.out.print(question);
		if (password)
			return String.valueOf(con.readPassword()).trim();
		Scanner sc = new Scanner(con.reader());
		// logging.debug( "stop " + sc);
		return sc.nextLine();
		// return String.valueOf( System.console().readLine() ).trim();
	}

	public static void main(String[] args) {
		// tests
		// logging.debug(wrapToHTML(args[0]));
		logging.debug("arguments given " + Arrays.toString(args));
		// logging.debug(" result of lastpositionDifferenceForDotSeparatedNumbers "
		// + lastpositionDifferenceForDotSeparatedNumbers(args[0], args[1]));
		logging.debug(" result of compareDottedNumberStrings " + compareDottedNumberStrings(args[0], args[1]));
	}

}
