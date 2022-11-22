package de.uib.configed;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;

/**
 * This class contains app constants (including some global functions) Copyright
 * (c) uib 2001-2022
 */

public class Globals {
	public static final String VERSION = "4.2.18.2";
	public static final String VERDATE = "(2022/14/10)";

	public static final String VERHASHTAG = "";

	public static final String ZERODATE = "";

	public static final String REQUIRED_SERVICE_VERSION = "4.1.0";
	//public static final String RECOMMENDED_OPSI_VERSION = "4.2.0";
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
	public static final Color INVISIBLE = new Color(11, 13, 17); //some value which shall be interpreted as identical with background;

	public static class ProductPackageVersionSeparator {
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
	//public static final Font defaultFont12 = new java.awt.Font("SansSerif", 0, 12);
	public static final Font defaultFontBig = new java.awt.Font("SansSerif", 0, 12);
	public static final Font defaultFontBold = new java.awt.Font("SansSerif", Font.BOLD, 12);
	public static final Font defaultFontTitle = new java.awt.Font("SansSerif", 0, 16);
	/*
	// Get all font family names
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	String fontNames[] = ge.getAvailableFontFamilyNames();
	
	// Iterate the font family names
	for (int i=0; i<fontNames.length; i++) {
	System.out.println("FONT ==>>> " + fontNames[i]);
	}
	*/

	public static final Color backgroundWhite = new Color(245, 245, 245);
	public static final Color backgroundGrey = new Color(220, 220, 220);
	public static final Color backgroundLightGrey = new Color(230, 230, 230);
	public static final Color backLightBlue = new Color(220, 228, 242); // new Color (210,216,232); 
	public static final Color backLighterBlue = new Color(230, 230, 245);
	public static final Color backVeryLightBlue = new Color(240, 240, 245);
	//public static final Color backLight = new Color (220,230,230); 
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

	public static void formatButtonSmallText(javax.swing.JButton button) {
		button.setFont(defaultFontSmall);
		button.setPreferredSize(new Dimension(45, 20));
		button.setForeground(lightBlack);
		button.setBackground(backBlue);
		button.setOpaque(false);
		//button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		//button.setBorder(BorderFactory.createEmptyBorder());
	}

	public static final Color nimbusSelectionBackground = new Color(57, 105, 138);
	public static final Color nimbusBackground = new Color(214, 217, 223);
	public static final Color backNimbus = new Color(214, 219, 222);
	public static final Color backNimbusLight = new Color(224, 229, 235);

	//in table, change colors by row
	public static final Color defaultTableCellBgColor1 = Color.white; //new Color (255,255,255);
	public static final Color defaultTableCellBgColor2 = backLightYellow; // new java.awt.Color (250, 48, 221);

	//tableSelection
	//public static final Color defaultTableSelectedRowDark =  new Color (189,207,231);
	public static final Color defaultTableSelectedRowDark = new Color(204, 220, 238);
	public static final Color defaultTableSelectedRowBright = new Color(221, 233, 249);

	//in table, change colors by row and column
	public static final Color defaultTableCellBgColor00 = backLightYellow;// new java.awt.Color (250, 48, 221);
	public static final Color defaultTableCellBgColor01 = new Color(236, 235, 214);
	public static final Color defaultTableCellBgColor10 = Color.white; //new Color (255,255,255);
	public static final Color defaultTableCellBgColor11 = backgroundWhite;

	public static final Color defaultTableHeaderBgColor = new Color(222, 231, 247); //new Color (206,223,247);
	public static final Color defaultTableCellSelectedBgColor = new Color(206, 224, 235); // new Color (184,207,229);
	public static final Color defaultTableCellSelectedBgColorNotEditable = new Color(189, 207, 231);

	public static final Color logColorEssential = new Color(0, 0, 0); //1 - black
	public static final Color logColorCritical = new Color(255, 0, 0); //new Color(200,0,200); // 2 - red
	public static final Color logColorError = new Color(200, 100, 0); //new Color(200,0,0); // 3 - orange
	public static final Color logColorWarning = new Color(20, 20, 200); // new Color(0,200,0); //new Color(255,128,0); //4 - blue
	public static final Color logColorNotice = new Color(10, 150, 10); //new Color(0,200,0); //5 -  green
	public static final Color logColorInfo = new Color(50, 50, 50); //6 - grey
	public static final Color logColorDebug = new Color(150, 150, 150);//7 - brighter grey
	public static final Color logColorDebug2 = new Color(150, 150, 150);
	public static final Color logColorConfidential = new Color(150, 150, 0);
	/*	
	public static final Color logColorEssential    = new Color(0,0,0);
	public static final Color logColorCritical     = new Color(200,0,200);
	public static final Color logColorError        = new Color(200,0,0);
	public static final Color logColorWarning      = new Color(255,128,0);
	public static final Color logColorNotice       = new Color(0,200,0);
	public static final Color logColorInfo         = new Color(20,20,20);
	public static final Color logColorDebug        = new Color(150,150,150);
	public static final Color logColorDebug2       = new Color(150,150,150);
	public static final Color logColorConfidential = new Color(150,150,0);
	*/

	public static final Color opsiLogoBlue = new Color(106, 128, 174);
	public static final Color opsiLogoLightBlue = new Color(195, 200, 222);

	public static javax.swing.border.Border createPanelBorder() {
		return new javax.swing.border.LineBorder(Globals.backBlue, 2, true);
	}

	public static final int toolTipInitialDelayMs = 1000;
	public static final int toolTipDismissDelayMs = 20000;
	public static final int toolTipReshowDelayMs = 0;

	public static final int gapSize = 10;
	public static final int minGapSize = 5;
	public static final int vGapSize = 10;
	public static final int hGapSize = 10;
	public static final int minVGapSize = 5;
	public static final int minHGapSize = 5;

	public static final int buttonHeight = 24;
	public static final int lineHeight = 28;
	public static final int smallHeight = 18;
	public static final int progressBarHeight = 10;
	public static final int buttonWidth = 140;
	public static final int iconWidth = 60;
	public static final int labelWidth = 80;
	public static final int timeSpinnerWidth = 50;
	public static final int squareButtonWidth = 24;

	public static final Dimension buttonDimension = new Dimension(buttonWidth, buttonHeight);
	public static final Dimension smallButtonDimension = new Dimension(buttonWidth / 2, buttonHeight);
	public static final Dimension shortButtonDimension = new Dimension(buttonWidth / 4, buttonHeight);
	public static final Dimension textfieldDimension = new Dimension(buttonWidth, lineHeight);
	public static final Dimension labelDimension = new Dimension(labelWidth, lineHeight);
	public static final Dimension shortlabelDimension = new Dimension(60, lineHeight);
	public static final int counterfieldWidth = 160;
	public static final Dimension counterfieldDimension = new Dimension(counterfieldWidth, lineHeight);

	public static final Dimension modeSwitchDimension = new Dimension(50, 50);

	public static int graphicButtonHeight = 40;
	public static int graphicButtonWidth = 40;
	public static int graphicButtonWidthX = 55;

	public static int checkBoxWidth = 20;
	public static int comboBoxRowCount = 20;

	public static final Dimension graphicButtonDimension = new Dimension(graphicButtonWidth, graphicButtonHeight);

	//action form constants
	public static int hFirstGap = hGapSize * 3;
	public static int firstLabelWidth = 250;

	public static final String[] logtypes = new String[] { "clientconnect", "instlog", "userlogin", "bootimage",
			"opsiconfd" };
	public static final int[] maxLogSizes = new int[] { 4 * 1024 * 1024, 8 * 1024 * 1024, 8 * 1024 * 1024, 0,
			1 * 1024 * 1024 };
	//public static final int[] maxLogSizes = new int[]{3*1024*1024, 3*1024*1024, 3*1024*1024, 0, 1* 1024*1024};
	//if each factor is 1 a heap size of 256 m is sufficient; for more, 512 should be given 

	public static Image mainIcon = null;
	//mainIcon = Toolkit.getDefaultToolkit().createImage(Globals.class.getResource("opsi.gif")); called in configed

	public static final String CONFLICTSTATEstring = "mixed";
	public static final Color CONFLICTSTATEcellcolor = new Color(255, 180, 180);
	public static final String NOVALIDSTATEstring = "";//"invalid";

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

	public static java.awt.Container mainContainer; //transparent for appletHandling
	public static javax.swing.JFrame mainFrame; //fixed
	public static javax.swing.JFrame frame1; //can be changed
	public static java.awt.Container container1; //can be changed

	public static java.awt.Dimension helperFormDimension = new java.awt.Dimension(1100, 600);

	public static int locationDistanceX = 150;
	public static int locationDistanceY = 150;

	public static int dialogFrameDefaultHeight = 400;
	public static int dialogFrameDefaultWidth = 800;
	public static java.awt.Dimension dialogFrameDefaultSize = new java.awt.Dimension(dialogFrameDefaultWidth,
			dialogFrameDefaultHeight);

	public static String getResourceValue(String key) {
		return configed.getResourceValue(key);
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

		ClassLoader cl = de.uib.configed.Globals.class.getClassLoader();
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
		ClassLoader cl = de.uib.configed.Globals.class.getClassLoader();
		//based on MainFrame

		java.net.URL imgURL = cl.getResource(xPath);

		//imgURL = de.uib.configed.Globals.class.getResource(xPath);
		//should have the same result (but seems not to have) 

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
		ClassLoader cl = de.uib.configed.Globals.class.getClassLoader();
		//based on MainFrame

		java.net.URL imgURL = cl.getResource(xPath);

		//imgURL = de.uib.configed.Globals.class.getResource(xPath);
		//should have the same result (but seems not to have) 

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
		//sqlNow = sqlNow.replace(':', '-');

		return sqlNow;
	}

	public static String getSeconds() {
		String sqlNow = new java.sql.Timestamp(new java.util.GregorianCalendar().getTimeInMillis()).toString();

		//System.out.println(" sqlNow " + sqlNow);
		int i = sqlNow.lastIndexOf(' ');
		String date = sqlNow.substring(0, i);
		date = date.replace(' ', '-');
		String time = sqlNow.substring(i + 1);
		time = time.substring(0, time.indexOf('.'));

		return date + "_" + time;

		/*
		sqlNow = sqlNow.substring(0, sqlNow.indexOf('.'));
		sqlNow = sqlNow.replace(' ', '-');
		sqlNow.replace, sqlNow.lastIndexOf('_'));
		sqlNow = sqlNow.replace(':', '-');
		
		return sqlNow;
		*/
	}

	public static String getDate(boolean justNumbers) {
		String sqlNow = new java.sql.Timestamp(new java.util.GregorianCalendar().getTimeInMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(' '));

		if (justNumbers)
			sqlNow = sqlNow.replaceAll("-", "");

		return sqlNow;
	}

	public final static ArrayList<Object> getNowTimeListValue() {
		return getNowTimeListValue(null);
	}

	public final static ArrayList<Object> getNowTimeListValue(final String comment) {
		ArrayList<Object> result = new ArrayList<Object>();
		//result. add( new Date().toString() );
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
				//logging.info(source.getClass().getName() + " " + cName + " has size  " + ((Collection)c).size() );
			} else if (c instanceof Map) {
				//logging.info(source.getClass().getName() + " " + cName + " has size  " + ((Map)c).size() );
			} else {
				logging.info(source.getClass().getName() + " " + cName + " is neither a Collection nor a Map  ");
				result = false;
			}
		} else
			logging.debug(source.getClass().getName() + " " + cName + " is null");

		return result;
	}

	public static Color brightenColor(java.awt.Color c)
	//experimental
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

		boolean result = PersistenceControllerFactory.getPersistenceController().isServerFullPermission();

		return result;
	}

	public static boolean forbidEditingTargetSpecific() {
		//boolean forbidEditing = PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly();

		boolean forbidEditing = false;

		de.uib.utilities.logging.logging.debug("forbidEditing for target " + ConfigedMain.getEditingTarget() + "?");

		if (ConfigedMain.getEditingTarget() == ConfigedMain.EditingTarget.SERVER) {
			forbidEditing

					= !PersistenceControllerFactory.getPersistenceController().isServerFullPermission();

			//PersistenceControllerFactory.getPersistenceController().checkDepotRestrictedPermission()
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

	public final static String STARRED_STRING = "*****";

	public static void main(String[] args) {
		createImageIcon("images/cancel.png", "");
	}

}
