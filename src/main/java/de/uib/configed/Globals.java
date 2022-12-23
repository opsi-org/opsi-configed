package de.uib.configed;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.text.Collator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.ImageIcon;

import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;

/**
 * This class contains app constants (including some global functions) Copyright
 * (c) uib 2001-2022
 */

public class Globals {
	public static final String VERSION = "4.2.20.1";
	public static final String VERDATE = "2022-12-15";

	public static final String VERHASHTAG = "";

	public static final String ZERODATE = "";

	public static final String REQUIRED_SERVICE_VERSION = "4.1.0";
	// public static final String RECOMMENDED_OPSI_VERSION = "4.2.0";
	public static final String MIN_SUPPORTED_OPSI_VERSION = "4.0";

	public static final String COPYRIGHT1 = "Copyright (c) uib 2001 - 2022 (www.uib.de)";
	public static final String COPYRIGHT2 = "Open Source license: AGPL v3";

	public static final String APPNAME = "opsi config editor";
	public static final String iconresourcename = "opsi.gif";

	public static final String opsiDocpage = "http://www.opsi.org";
	public static final String opsiSupportpage = "http://opsi.org/support";
	public static final String opsiForumpage = "http://forum.opsi.org";

	public static final String BUNDLE_NAME = "de/uib/messages/configed";

	public static final boolean showIconsInProductTable = false;
	public static final Color INVISIBLE = new Color(11, 13, 17); // some value which shall be interpreted as identical
																	// with background;

	public static final String CERTIFICATE_FILE_NAME = "opsi-ca-cert";
	public static final String CERTIFICATE_FILE_EXTENSION = "pem";
	public static final String CERTIFICATE_FILE = CERTIFICATE_FILE_NAME + "." + CERTIFICATE_FILE_EXTENSION;

	public static class ProductPackageVersionSeparator {

		private ProductPackageVersionSeparator() {
		}

		public static String forDisplay() {
			return "-";
		}

		public static String forKey() {
			return ";";
		}

		public static String formatKeyForDisplay(String key) {
			if (key == null)
				return null;

			int i = key.indexOf(forKey());
			if (i == -1)
				return key;

			String result = key.substring(0, i);
			if (i < key.length())
				result = result + forDisplay() + key.substring(i + 1);

			return result;
		}
	}

	public static final Font defaultFont = new java.awt.Font("SansSerif", 0, 11);
	public static final Font defaultFontStandardBold = new java.awt.Font("SansSerif", Font.BOLD, 11);
	public static final Font defaultFontSmall = new java.awt.Font("SansSerif", 0, 9);
	public static final Font defaultFontSmallBold = new java.awt.Font("SansSerif", Font.BOLD, 9);
	// public static final Font defaultFont12 = new java.awt.Font("SansSerif", 0,
	// 12);
	public static final Font defaultFontBig = new java.awt.Font("SansSerif", 0, 12);
	public static final Font defaultFontBold = new java.awt.Font("SansSerif", Font.BOLD, 12);
	public static final Font defaultFontTitle = new java.awt.Font("SansSerif", 0, 16);

	public static final int DEFAULT_FTEXTAREA_HEIGHT = 200;
	public static final int DEFAULT_FTEXTAREA_WIDTH = 350;

	public static Boolean interpretAsBoolean(Object value) {
		

		if (value == null)
			return null;

		if (value instanceof Boolean) {
			
			return (Boolean) value;
		}

		if (value instanceof Integer) {
			
			int val = (Integer) value;
			if (val == 1)
				return true;
			else if (val == 0)
				return false;

			else
				throw new IllegalArgumentException("" + value + " cannot be interpreted as boolean");
		}

		if (value instanceof String) {
			

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

	public static final Color backgroundWhite = new Color(245, 245, 245);
	public static final Color backgroundGrey = new Color(220, 220, 220);
	public static final Color backgroundLightGrey = new Color(230, 230, 230);
	public static final Color backLightBlue = new Color(220, 228, 242);
	public static final Color backLighterBlue = new Color(230, 230, 245);
	public static final Color backVeryLightBlue = new Color(240, 240, 245);
	public static final Color backBlue = new Color(200, 200, 250);
	public static final Color backYellow = new java.awt.Color(255, 255, 182);
	public static final Color backLightYellow = new java.awt.Color(250, 248, 221);
	public static final Color backTabsColor = new java.awt.Color(206, 223, 247);
	public static final Color darkOrange = new java.awt.Color(218, 180, 4);
	public static final Color lightBlack = new Color(30, 30, 30);
	public static final Color blue = new Color(30, 30, 100);
	public static final Color blueGrey = new Color(180, 190, 190);
	public static final Color violett = new Color(160, 170, 200);
	public static final Color greyed = new Color(150, 150, 150);
	public static final Color failedBackColor = new Color(100, 100, 100);
	public static final Color okGreen = new Color(20, 140, 20);
	public static final Color actionRed = new Color(239, 4, 4);
	public static final Color warningRed = new Color(204, 51, 0);
	public static final Color unknownBlue = new Color(40, 17, 213);
	public static final Color verylightGreen = new Color(102, 255, 102);
	public static final Color green = new Color(0, 170, 170);
	public static final Color lightPink = new Color(255, 208, 233);
	public static final Color lightPurple = new Color(212, 199, 255);
	public static final Color purple = new Color(220, 185, 255);
	public static final Color licensingIconPurple = new Color(141, 145, 230);
	public static final Color licensingIconPurpleLight = new Color(164, 169, 235);
	public static final Color checkGreen = new Color(174, 201, 143);
	public static final Color checkGreenLight = new Color(198, 225, 171);

	public static final int toolTipInitialDelayMs = 1000;
	public static final int toolTipDismissDelayMs = 20000;
	public static final int toolTipReshowDelayMs = 0;

	public static void formatButtonSmallText(javax.swing.JButton button) {
		button.setFont(defaultFontSmall);
		button.setPreferredSize(new Dimension(45, 20));
		button.setForeground(lightBlack);
		button.setBackground(backBlue);
		button.setOpaque(false);
		// button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		// button.setBorder(BorderFactory.createEmptyBorder());
	}

	public static final Color nimbusSelectionBackground = new Color(57, 105, 138);
	public static final Color nimbusBackground = new Color(214, 217, 223);
	public static final Color backNimbus = new Color(214, 219, 222);

	// in table, change colors by row
	public static final Color defaultTableCellBgColor1 = Color.white; // new Color (255,255,255);
	public static final Color defaultTableCellBgColor2 = backLightYellow; // new java.awt.Color (250, 48, 221);

	// tableSelection
	// public static final Color defaultTableSelectedRowDark = new Color
	// (189,207,231);
	public static final Color defaultTableSelectedRowDark = new Color(204, 220, 238);
	public static final Color defaultTableSelectedRowBright = new Color(221, 233, 249);

	// in table, change colors by row and column
	public static final Color defaultTableCellBgColor00 = backLightYellow;// new java.awt.Color (250, 48, 221);
	public static final Color defaultTableCellBgColor01 = new Color(236, 235, 214);
	public static final Color defaultTableCellBgColor10 = Color.white; // new Color (255,255,255);
	public static final Color defaultTableCellBgColor11 = backgroundWhite;

	public static final Color defaultTableHeaderBgColor = new Color(222, 231, 247); // new Color (206,223,247);
	public static final Color defaultTableCellSelectedBgColor = new Color(206, 224, 235); // new Color (184,207,229);
	public static final Color defaultTableCellSelectedBgColorNotEditable = new Color(189, 207, 231);

	public static final Color logColorEssential = new Color(0, 0, 0); // 1 - black
	public static final Color logColorCritical = new Color(255, 0, 0); // new Color(200,0,200); // 2 - red
	public static final Color logColorError = new Color(200, 100, 0); // new Color(200,0,0); // 3 - orange
	public static final Color logColorWarning = new Color(20, 20, 200); // new Color(0,200,0); //new Color(255,128,0);
																		// //4 - blue
	public static final Color logColorNotice = new Color(10, 150, 10); // new Color(0,200,0); //5 - green
	public static final Color logColorInfo = new Color(50, 50, 50); // 6 - grey
	public static final Color logColorDebug = new Color(150, 150, 150);// 7 - brighter grey
	public static final Color logColorDebug2 = new Color(150, 150, 150);
	public static final Color logColorConfidential = new Color(150, 150, 0);
	/*
	 * public static final Color logColorEssential = new Color(0,0,0);
	 * public static final Color logColorCritical = new Color(200,0,200);
	 * public static final Color logColorError = new Color(200,0,0);
	 * public static final Color logColorWarning = new Color(255,128,0);
	 * public static final Color logColorNotice = new Color(0,200,0);
	 * public static final Color logColorInfo = new Color(20,20,20);
	 * public static final Color logColorDebug = new Color(150,150,150);
	 * public static final Color logColorDebug2 = new Color(150,150,150);
	 * public static final Color logColorConfidential = new Color(150,150,0);
	 */

	public static final Color opsiLogoBlue = new Color(106, 128, 174);
	public static final Color opsiLogoLightBlue = new Color(195, 200, 222);

	public static javax.swing.border.Border createPanelBorder() {
		return new javax.swing.border.LineBorder(Globals.backBlue, 2, true);
	}

	public static final int GAP_SIZE = 10;
	public static final int MIN_GAP_SIZE = 5;
	public static final int VGAP_SIZE = 10;
	public static final int HGAP_SIZE = 10;
	public static final int MIN_VGAP_SIZE = 5;
	public static final int MIN_HGAP_SIZE = 5;

	public static final int MIN_VSIZE = 10;
	public static final int PREF_VSIZE = 80;

	public static final int MIN_HSIZE = 50;
	public static final int PREF_HSIZE = 80;

	public static final int BUTTON_HEIGHT = 24;
	public static final int LINE_HEIGHT = 28;
	public static final int SMALL_HEIGHT = 18;
	public static final int PROGRESS_BAR_HEIGHT = 10;
	public static final int TABLE_ROW_HEIGHT = 16;
	public static final int BUTTON_WIDTH = 140;
	public static final int ICON_WIDTH = 60;
	public static final int LABEL_WIDTH = 80;
	public static final int TIME_SPINNER_WIDTH = 50;
	public static final int SQUARE_BUTTON_WIDTH = 24;

	public static final Dimension buttonDimension = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
	public static final Dimension lowerButtonDimension = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT - 4);
	public static final Dimension smallButtonDimension = new Dimension(BUTTON_WIDTH / 2, BUTTON_HEIGHT);
	public static final Dimension shortButtonDimension = new Dimension(BUTTON_WIDTH / 4, BUTTON_HEIGHT);
	public static final Dimension textfieldDimension = new Dimension(BUTTON_WIDTH, LINE_HEIGHT);
	public static final Dimension labelDimension = new Dimension(LABEL_WIDTH, LINE_HEIGHT);
	public static final Dimension shortlabelDimension = new Dimension(60, LINE_HEIGHT);
	public static final int COUNTERFIELD_WIDTH = 160;
	public static final Dimension counterfieldDimension = new Dimension(COUNTERFIELD_WIDTH, LINE_HEIGHT);
	public static final Dimension newSmallButton = new Dimension(30, 30);
	public static final Dimension modeSwitchDimension = new Dimension(50, 50);
	public static final Dimension filechooserSize = new Dimension(600, 400);

	public static final int GRAPHIC_BUTTON_HEIGHT = 40;
	public static final int GRAPHIC_BUTTON_WIDTH = 40;
	public static final int GRAPHIC_BUTTON_WIDTH_X = 55;

	public static final int CHECKBOX_WIDTH = 20;
	public static final int COMBOBOX_ROW_COUNT = 20;

	public static final Dimension graphicButtonDimension = new Dimension(GRAPHIC_BUTTON_WIDTH, GRAPHIC_BUTTON_HEIGHT);

	public static Integer startX;
	public static Integer startY;
	public static Integer startWidth;
	public static Integer startHeight;

	public static final int dateFormatStylePattern = DateFormat.LONG;

	// action form constants
	public static final int HFIRST_GAP = HGAP_SIZE * 3;
	public static final int FIRST_LABEL_WIDTH = 250;

	public static final String[] logtypes = new String[] { "clientconnect", "instlog", "userlogin", "bootimage",
			"opsiconfd" };
	public static final int[] maxLogSizes = new int[] { 4 * 1024 * 1024, 8 * 1024 * 1024, 8 * 1024 * 1024, 0,
			1 * 1024 * 1024 };
	// public static final int[] maxLogSizes = new int[]{3*1024*1024, 3*1024*1024,
	// 3*1024*1024, 0, 1* 1024*1024};
	// if each factor is 1 a heap size of 256 m is sufficient; for more, 512 should
	// be given

	public static Image mainIcon = null;
	// mainIcon =
	// Toolkit.getDefaultToolkit().createImage(Globals.class.getResource("opsi.gif"));
	// called in configed

	public static final String CONFLICT_STATE_STRING = "mixed";
	public static final Color CONFLICT_STATE_CELL_COLOR = new Color(255, 180, 180);
	public static final String NO_VALID_STATE_STRING = "";// "invalid"

	private static Map<String, Object> objects;

	public static Map<String, Object> getMap() {
		if (objects == null) {
			objects = new HashMap<>();

			objects.put("mainIcon", mainIcon);
			objects.put("defaultFont", defaultFont);
			objects.put("APPNAME", APPNAME);
		}

		return objects;
	}

	private static Collator alphaCollator = null;

	public static Collator getCollator() {
		if (alphaCollator == null) {
			alphaCollator = Collator.getInstance();
			// alphaCollator.setStrength(java.text.Collator.PRIMARY);
			alphaCollator.setStrength(java.text.Collator.IDENTICAL);

		}
		return alphaCollator;
	}

	public static java.awt.Container mainContainer; // transparent for appletHandling // masterFrame
	public static javax.swing.JFrame mainFrame; // fixed
	public static javax.swing.JFrame frame1; // can be changed
	public static java.awt.Container container1; // can be changed

	public static final java.awt.Dimension helperFormDimension = new java.awt.Dimension(1100, 600);

	public static final int LOCATION_DISTANCE_X = 150;
	public static final int LOCATION_DISTANCE_Y = 150;

	public static final int DIALOG_FRAME_DEFAULT_HEIGHT = 400;
	public static final int DIALOG_FRAME_DEFAULT_WIDTH = 800;
	public static final java.awt.Dimension dialogFrameDefaultSize = new java.awt.Dimension(DIALOG_FRAME_DEFAULT_WIDTH,
			DIALOG_FRAME_DEFAULT_HEIGHT);

	public static String getResourceValue(String key) {
		return configed.getResourceValue(key);
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().startsWith("windows");
	}

	public static String fillStringToLength(String s, int len) {
		if (s.length() > len)
			return s;

		StringBuffer buff = new StringBuffer(len);
		for (int i = 0; i < s.length(); i++)
			buff.append(s.charAt(i));

		for (int i = s.length(); i < len; i++)
			buff.append(' ');

		return buff.toString();
	}

	private static final String imageBase = "de/uib/configed/gui/";

	public static java.net.URL getImageResourceURL(String relPath) {
		String resourceS = imageBase + relPath;

		ClassLoader cl = Globals.class.getClassLoader();
		java.net.URL imgURL = cl.getResource(resourceS);
		if (imgURL != null) {
			return imgURL;
		} else {
			de.uib.utilities.logging.logging.warning("Couldn't find file  " + relPath);
			return null;
		}
	}

	public static Image createImage(String path) {
		String xPath = imageBase + path;
		ClassLoader cl = Globals.class.getClassLoader();
		// based on MainFrame

		java.net.URL imgURL = cl.getResource(xPath);

		// imgURL = Globals.class.getResource(xPath);
		// should have the same result (but seems not to have)

		try {

			if (imgURL != null) {
				return java.awt.Toolkit.getDefaultToolkit().createImage(imgURL);
			} else {
				de.uib.utilities.logging.logging.info("Couldn't find file: " + path);
				return null;
			}
		} catch (Exception ex) {
			de.uib.utilities.logging.logging.info("createImageIcon " + path + " : " + ex);

		}

		return null;
	}

	public static ImageIcon createImageIcon(String path, String description) {
		String xPath = imageBase + path;
		ClassLoader cl = Globals.class.getClassLoader();
		// based on MainFrame

		java.net.URL imgURL = cl.getResource(xPath);

		// imgURL = Globals.class.getResource(xPath);
		// should have the same result (but seems not to have)

		try {

			if (imgURL != null) {
				return new ImageIcon(imgURL, description);
			} else {
				de.uib.utilities.logging.logging.info("Couldn't find file: " + path);
				return null;
			}
		} catch (Exception ex) {
			de.uib.utilities.logging.logging.info("createImageIcon " + path + " : " + ex);

		}

		return null;
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
			sqlNow = sqlNow.replace("-", "");

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

		return "" + hoursS + ":" + remMinutesS + ":" + remSecondsS;
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

	public static List<String> takeAsStringList(List<Object> list) {
		List<String> result = new ArrayList<>();

		if (list == null)
			return result;

		for (Object val : list) {
			result.add((String) val);
		}

		return result;
	}

	public static final String pseudokeySeparator = ";";

	public static final List<Object> getNowTimeListValue() {
		return getNowTimeListValue(null);
	}

	public static final List<Object> getNowTimeListValue(final String comment) {
		ArrayList<Object> result = new ArrayList<>();
		// result. add( new Date().toString() );
		String now = new java.sql.Timestamp(new java.util.GregorianCalendar().getTimeInMillis()).toString();
		now = now.substring(0, now.indexOf("."));
		if (comment != null)
			result.add(now + " " + comment);
		else
			result.add(now);

		logging.info("getNowTimeListValue" + result);

		return result;
	}

	public static String pseudokey(String[] partialvalues) {
		StringBuffer resultBuffer = new StringBuffer("");

		if (partialvalues.length > 0) {
			resultBuffer.append(partialvalues[0]);

			for (int i = 1; i < partialvalues.length; i++) {
				resultBuffer.append(";");
				resultBuffer.append(partialvalues[i]);
			}
		}

		return resultBuffer.toString();
	}

	static final int tooltipLineLength = 50;
	static final int uncertainty = 20;

	public static String wrapToHTML(String s) {
		StringBuffer result = new StringBuffer("<html>");
		String remainder = s;
		while (remainder.length() > 0) {
			de.uib.utilities.logging.logging.debug("Globals, remainder " + remainder);
			if (remainder.length() <= tooltipLineLength) {
				result.append(remainder.replace("\\n", "<br />"));
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

	public static String produceNonNull(Object o) {
		String result = "";
		if (o != null)
			result = o.toString();
		return result;
	}

	public static boolean checkCollection(Object source, String location, String cName, Object c) {
		boolean result = (c != null);
		if (result) {
			if (c instanceof Collection) {
				
				// ((Collection)c).size() );
			} else if (c instanceof Map) {
				
				// ((Map)c).size() );
			} else {
				logging.info(source.getClass().getName() + " " + cName + " is neither a Collection nor a Map  ");
				result = false;
			}
		} else
			logging.debug(source.getClass().getName() + " " + cName + " is null");

		return result;
	}

	private static Integer stringCompareAsInt(String s1, String s2) throws NumberFormatException {
		
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
		
		return val1 - val2;
	}

	public static Integer compareDottedNumberStrings(final String ver1, final String ver2)
			throws NumberFormatException {
		
		

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

	private static Rectangle getMinDevice() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		Rectangle result = new Rectangle(1600, 1200);
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i = 0; i < gc.length; i++) {
				int w = gc[i].getBounds().width;
				int h = gc[i].getBounds().height;
				
				// gc[i].getBounds());
				if (w < result.width || h < result.height)
					result = new Rectangle(w, h);
			}
		}

		logging.info("minimalBounds: giving " + result);

		return result;
	}

	public static Rectangle buildLocation(int intendedWidth, int intendedHeight, int placementX, int placementY) {
		return buildLocation(null, intendedWidth, intendedHeight, placementX, placementY);
	}

	private static int bigFramesDistanceFromLeft = 60;
	private static int bigFramesDistanceFromTop = 40;

	public static Rectangle buildLocation(javax.swing.JFrame f, int intendedWidth, int intendedHeight, int placementX,
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
		try (Scanner sc = new Scanner(con.reader())) {
			return sc.nextLine();
		}
	}

	public static Color brightenColor(java.awt.Color c)
	// experimental
	{
		int r = min(255, c.getRed() + 10);
		int g = min(255, c.getGreen() + 10);
		int b = min(255, c.getBlue() + 10);
		return new Color(r, g, b);
	}

	public static boolean isGlobalReadOnly() {
		boolean result = PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly();

		de.uib.utilities.logging.logging.info("Globals got readonly " + result);

		return result;
	}

	public static boolean isServerFullPermission() {
		if (PersistenceControllerFactory.getPersistenceController() == null)
			return false;

		return PersistenceControllerFactory.getPersistenceController().isServerFullPermission();

	}

	public static boolean forbidEditingTargetSpecific() {
		// boolean forbidEditing =
		// PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly();

		boolean forbidEditing = false;

		de.uib.utilities.logging.logging.debug("forbidEditing for target " + ConfigedMain.getEditingTarget() + "?");

		if (ConfigedMain.getEditingTarget() == ConfigedMain.EditingTarget.SERVER) {
			forbidEditing

					= !PersistenceControllerFactory.getPersistenceController().isServerFullPermission();

			// PersistenceControllerFactory.getPersistenceController().checkDepotRestrictedPermission()
			;
		}

		else {
			forbidEditing = PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly();
		}

		de.uib.utilities.logging.logging.debug("forbidEditing " + forbidEditing);

		return forbidEditing;
	}

	public static boolean isKeyForSecretValue(String s) {
		String t = s.toLowerCase();
		boolean result = t.indexOf("password") > -1 || t.startsWith("secret");
		return result;
	}

	public static final String STARRED_STRING = "*****";

	public static void main(String[] args) {
		createImageIcon("images/cancel.png", "");
	}

}
