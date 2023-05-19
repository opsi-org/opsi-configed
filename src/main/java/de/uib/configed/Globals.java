package de.uib.configed;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.text.Collator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import de.uib.configed.gui.FTextArea;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This class contains app constants (including some global functions) Copyright
 * (c) uib 2001-2022
 */

public final class Globals {

	// get version from pom.xml
	public static final String VERSION = Globals.class.getPackage().getImplementationVersion();
	public static final String VERDATE = "2023-05-09";

	public static final String VERHASHTAG = "";

	public static final String ZERODATE = "";

	public static final String REQUIRED_SERVICE_VERSION = "4.1.0";
	public static final String MIN_SUPPORTED_OPSI_VERSION = "4.0";

	public static final String COPYRIGHT1 = "Copyright (c) uib 2001 - 2023 (www.uib.de)";
	public static final String COPYRIGHT2 = "Open Source license: AGPL v3";

	public static final String APPNAME = "opsi config editor";
	public static final String ICON_RESOURCE_NAME = "opsi.gif";

	public static final String OPSI_DOC_PAGE = "http://www.opsi.org";
	public static final String OPSI_SUPPORT_PAGE = "http://opsi.org/support";
	public static final String OPSI_FORUM_PAGE = "http://forum.opsi.org";

	public static final String BUNDLE_NAME = "de/uib/messages/configed";

	public static final boolean SHOW_ICONS_IN_PRODUCT_TABLE = false;

	public static final String CERTIFICATE_FILE_NAME = "opsi-ca-cert";
	public static final String CERTIFICATE_FILE_EXTENSION = "pem";
	public static final String CERTIFICATE_FILE = CERTIFICATE_FILE_NAME + "." + CERTIFICATE_FILE_EXTENSION;

	public static final String HEALTH_CHECK_LOG_FILE_NAME = "healthCheck.log";
	public static final String DIAGNOSTIC_DATA_JSON_FILE_NAME = "diagnosticData.json";

	public static boolean disableCertificateVerification;

	// Here to prevent initialization

	public static final Color opsiDarkGrey = new Color(63, 63, 62);
	public static final Color opsiGrey = new Color(178, 178, 178);
	public static final Color opsiLightGrey = new Color(228, 228, 227);
	public static final Color opsiMagenta = new Color(203, 30, 88);
	public static final Color opsiBlue = new Color(63, 90, 166);

	public static final Color opsiBackgroundLight = new Color(255, 255, 255);
	public static final Color opsiBackgroundDark = new Color(31, 31, 31);

	public static final Color opsiForegroundLight = new Color(0, 0, 0);
	public static final Color opsiForegroundDark = new Color(225, 225, 225);

	public static final Font defaultFont = new Font("SansSerif", 0, 11);
	public static final Font defaultFontStandardBold = new Font("SansSerif", Font.BOLD, 11);
	public static final Font defaultFontSmall = new Font("SansSerif", 0, 9);
	public static final Font defaultFontSmallBold = new Font("SansSerif", Font.BOLD, 9);

	public static final Font defaultFontBig = new Font("SansSerif", 0, 12);
	public static final Font defaultFontBold = new Font("SansSerif", Font.BOLD, 12);
	public static final Font defaultFontTitle = new Font("SansSerif", 0, 16);

	public static final int DEFAULT_FTEXTAREA_HEIGHT = 200;
	public static final int DEFAULT_FTEXTAREA_WIDTH = 350;

	// some value which shall be interpreted as identical
	public static final Color INVISIBLE = new Color(11, 13, 17);

	public static final Color PRIMARY_FOREGROUND_COLOR = Color.BLACK;

	public static final Color PRIMARY_BACKGROUND_COLOR = Color.WHITE;
	public static final Color SECONDARY_BACKGROUND_COLOR = new Color(245, 245, 245);

	public static final Color BACKGROUND_COLOR_3 = new Color(230, 230, 230);
	public static final Color BACKGROUND_COLOR_4 = new Color(220, 220, 220);
	public static final Color BACKGROUND_COLOR_5 = new Color(200, 200, 250);

	public static final Color BACKGROUND_COLOR_6 = new Color(200, 200, 250);
	public static final Color BACKGROUND_COLOR_7 = new Color(220, 228, 242);
	public static final Color BACKGROUND_COLOR_8 = new Color(240, 240, 245);

	public static final Color BACKGROUND_COLOR_9 = new Color(250, 248, 221);

	public static final Color FAILED_BACKGROUND_COLOR = new Color(100, 100, 100);

	public static final Color FAILED_COLOR = Color.RED;
	public static final Color ACTION_COLOR = new Color(239, 4, 4);
	public static final Color WARNING_COLOR = new Color(204, 51, 0);
	public static final Color CHECK_COLOR = new Color(198, 225, 171); // succesful?
	public static final Color OK_COLOR = new Color(20, 140, 20);
	public static final Color UNKNOWN_COLOR = new Color(40, 17, 213);

	public static final Color darkOrange = new Color(218, 180, 4);
	public static final Color lightBlack = new Color(30, 30, 30);
	public static final Color blue = new Color(30, 30, 100);
	public static final Color blueGrey = new Color(180, 190, 190);
	public static final Color greyed = new Color(150, 150, 150);

	public static final Color ClientSelectionDialog_ELEMENT_FOREGROUND = Color.BLUE;

	public static final Color CSV_CREATE_CLIENT_PANEL_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color DEPOTS_LIST_FOREGROUND_COLOR = PRIMARY_FOREGROUND_COLOR;

	public static final Color F_DIALOG_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color F_GENERAL_DIALOG_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	// also used for GlassPane...
	public static final Color F_GENERAL_DIALOG_FADING_MIRROR_COLOR = new Color(230, 230, 250);

	public static final Color LOG_PANE_CARET_COLOR = FAILED_COLOR;

	public static final Color NEW_CLIENT_DIALOG_BORDER_COLOR = new Color(122, 138, 153);

	public static final Color PANELREINST_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color EDIT_MAP_PANEL_GROUPED_FOR_HOST_CONFIGS_BACKGROUND_COLOR = Color.YELLOW;

	public static final Color DEPENDENCIES_TREE_PANEL_BORDER_COLOR = PRIMARY_FOREGROUND_COLOR;

	public static final Color PANEL_PRODUCT_SETTINGS_TABLE_GRID_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color PANEL_PRODUCT_SETTINGS_FAILED_COLOR = FAILED_COLOR;

	public static final Color PANEL_PRODUCT_INFO_PANE_ACTIVE = PRIMARY_FOREGROUND_COLOR;
	public static final Color PANEL_PRODUCT_INFO_PANE_INACTIVE = greyed;

	public static final Color SSH_CONNECTION_OUTPUT_DIALOG_START_LINE_COLOR = Globals.lightBlack;
	public static final Color SSH_CONNECTION_OUTPUT_DIALOG_DIFFERENT_LINE_COLOR = PRIMARY_FOREGROUND_COLOR;
	public static final Color SSH_CONNECTION_OUTPUT_INIT_BACKGROUND_COLOR = Color.GREEN;
	public static final Color SSH_CONNECTION_OUTPUT_INIT_FOREGROUND_COLOR = FAILED_COLOR;
	public static final Color SSH_CONNECTION_SET_START_ANSI = PRIMARY_FOREGROUND_COLOR;

	public static final Color F_GENERAL_DIALOG_LICENSING_INFO_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color ACTION_REQUEST_NONE_COLOR = INVISIBLE;
	public static final Color ACTION_REQUEST_SETUP_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_UPDATE_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_UNINSTALL_COLOR = Color.BLUE;
	public static final Color ACTION_REQUEST_ALWAYS_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_ONCE_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_CUSTOM_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_LAST_COLOR = PRIMARY_FOREGROUND_COLOR;

	public static final Color INSTALLATION_STATUS_NOT_INSTALLED_COLOR = INVISIBLE;
	public static final Color INSTALLATION_STATUS_INSTALLED_COLOR = OK_COLOR;
	public static final Color INSTALLATION_STATUS_UNKNOWN_COLOR = UNKNOWN_COLOR;

	public static final Map<String, Color> SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS = new HashMap<>();
	static {
		// user info not really ansi code !!
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;info;0m", Globals.greyed);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;error;0m", Globals.ACTION_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;30;40m", PRIMARY_FOREGROUND_COLOR);

		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;30;40m", PRIMARY_FOREGROUND_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;40;40m", PRIMARY_FOREGROUND_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;40;40m", PRIMARY_FOREGROUND_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;31;40m", Globals.ACTION_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;31;40m", Globals.ACTION_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;41;40m", Globals.ACTION_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;41;40m", Globals.ACTION_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;32;40m", Globals.OK_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;32;40m", Globals.OK_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;42;40m", Globals.OK_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;42;40m", Globals.OK_COLOR);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;33;40m", Globals.darkOrange);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;33;40m", Globals.darkOrange);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;43;40m", Globals.darkOrange);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;43;40m", Globals.darkOrange);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;34;40m", Globals.blue);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;34;40m", Globals.blue);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;44;40m", Globals.blue);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;44;40m", Globals.blue);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;35;40m", Color.MAGENTA);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;35;40m", Color.MAGENTA);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;45;40m", Color.MAGENTA);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;45;40m", Color.MAGENTA);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;36;40m", Color.CYAN);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;36;40m", Color.CYAN);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;46;40m", Color.CYAN);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;46;40m", Color.CYAN);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;37;40m", Globals.lightBlack);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;37;40m", Globals.lightBlack);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[0;47;40m", Globals.lightBlack);
		SSH_CONNECTION_OUTPUT_DIALOG_ANSI_CODE_COLORS.put("[1;47;40m", Globals.lightBlack);
	}

	public static final Color LIST_MERGER_NO_COMMON_VALUE_TEXT_COLOR = Globals.BACKGROUND_COLOR_4;
	public static final Color LIST_MERGER_NO_COMMON_VALUE_BACKGROUND_COLOR = Globals.BACKGROUND_COLOR_4;
	public static final Color LIST_MERGER_NORMAL_VALUE_TEXT_COLOR = PRIMARY_FOREGROUND_COLOR;
	public static final Color LIST_MERGER_NORMAL_VALUE_BACKGROUND_COLOR = PRIMARY_FOREGROUND_COLOR;

	public static final Color ICON_NODE_RENDERER_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color EDIT_MAP_PANEL_GROUPED_BACKGROUND_COLOR = FAILED_COLOR;

	public static final Color EDIT_MAP_PANEL_X_FOREGROUND_COLOR = FAILED_COLOR;
	public static final Color EDIT_MAP_PANEL_X_GRID_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color JTABLE_SELECTION_PANEL_GRID_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color CELL_RENDERER_BY_INDEX_SELECTED_FOREGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color F_EDIT_PANE_UNDERLINE_HIGHLIGHTER_PAINTER = Color.BLUE;

	public static final Color X_CELL_EDITOR_SELECTED_FOREGROUND = PRIMARY_BACKGROUND_COLOR;
	public static final Color X_CELL_EDITOR_NOT_SELECTED_FOREGROUND = PRIMARY_FOREGROUND_COLOR;

	public static final Color PANEL_GEN_EDIT_TABLE_GRID_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color SIMPLE_ICON_NODE_RENDERER_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

	public static final Color nimbusSelectionBackground = new Color(57, 105, 138);
	public static final Color nimbusBackground = new Color(214, 217, 223);
	public static final Color backNimbus = new Color(214, 219, 222);

	// in table, change colors by row
	public static final Color defaultTableCellBgColor1 = PRIMARY_BACKGROUND_COLOR;
	public static final Color defaultTableCellBgColor2 = BACKGROUND_COLOR_9;

	// tableSelection

	public static final Color defaultTableSelectedRowDark = new Color(204, 220, 238);
	public static final Color defaultTableSelectedRowBright = new Color(221, 233, 249);

	// in table, change colors by row and column
	public static final Color defaultTableCellBgColor00 = BACKGROUND_COLOR_9;
	public static final Color defaultTableCellBgColor01 = new Color(236, 235, 214);
	public static final Color defaultTableCellBgColor10 = PRIMARY_BACKGROUND_COLOR;
	public static final Color defaultTableCellBgColor11 = SECONDARY_BACKGROUND_COLOR;

	public static final Color defaultTableHeaderBgColor = new Color(222, 231, 247);
	public static final Color defaultTableCellSelectedBgColor = new Color(206, 224, 235);
	public static final Color defaultTableCellSelectedBgColorNotEditable = new Color(189, 207, 231);

	public static final Color logColorEssential = new Color(41, 121, 255);
	public static final Color logColorCritical = new Color(226, 0, 102);
	public static final Color logColorError = new Color(229, 29, 59);
	public static final Color logColorWarning = new Color(255, 145, 0);

	public static final Color logColorNotice = new Color(0, 150, 5);
	public static final Color logColorInfoLight = new Color(33, 33, 33);
	public static final Color logColorInfoDark = new Color(245, 245, 245);
	public static final Color logColorDebugLight = new Color(86, 86, 86);
	public static final Color logColorDebugDark = new Color(192, 192, 192);
	public static final Color logColorTrace = new Color(139, 139, 139);
	public static final Color logColorSecret = new Color(213, 0, 249);

	public static final Color opsiLogoBlue = new Color(106, 128, 174);
	public static final Color opsiLogoLightBlue = new Color(195, 200, 222);

	public static final Color CONFLICT_STATE_CELL_COLOR = new Color(255, 180, 180);

	public static final int TOOLTIP_INITIAL_DELAY_MS = 1000;
	public static final int TOOLTIP_DISMISS_DELAY_MS = 20000;
	public static final int TOOLTIP_RESHOW_DELAY_MS = 0;

	public static final int SMALL_GAP_SIZE = 2;
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

	public static final int MIN_TABLE_V_SIZE = 40;

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

	public static final int WIDTH_FRAME_RENAME_CLIENT = 350;
	public static final int HEIGHT_FRAME_RENAME_CLIENT = 200;

	public static final int WIDTH_INFO_LOG_FILE = 400;
	public static final int HEIGHT_INFO_LOG_FILE = 200;

	public static final int REACHABLE_INFO_FRAME_WIDTH = 300;
	// Just enough so that the button is not hidden
	public static final int REACHABLE_INFO_FRAME_HEIGHT = 220;

	public static final int POPUP_ON_CLIENTS_FRAME_WIDTH = 380;
	public static final int POPUP_ON_CLIENTS_FRAME_HEIGHT = 300;

	public static final Dimension graphicButtonDimension = new Dimension(GRAPHIC_BUTTON_WIDTH, GRAPHIC_BUTTON_HEIGHT);

	public static final int DATE_FORMAT_STYLE_PATTERN = DateFormat.LONG;

	// action form constants
	public static final int HFIRST_GAP = HGAP_SIZE * 3;
	public static final int FIRST_LABEL_WIDTH = 250;

	public static final String STARRED_STRING = "*****";

	private static final String[] logTypes = new String[] { "clientconnect", "instlog", "userlogin", "bootimage",
			"opsiconfd" };

	private static final int[] maxLogSizes = new int[] { 4 * 1024 * 1024, 8 * 1024 * 1024, 8 * 1024 * 1024, 0,
			1 * 1024 * 1024 };

	public static Image mainIcon;

	public static final String CONFLICT_STATE_STRING = "mixed";
	public static final String NO_VALID_STATE_STRING = "";

	private static Map<String, Object> objects;

	private static Collator alphaCollator;

	public static final String PSEUDO_KEY_SEPARATOR = ";";

	// these two things can be changed
	public static JFrame frame1;

	public static final Dimension helperFormDimension = new Dimension(1100, 600);

	public static final int LOCATION_DISTANCE_X = 150;
	public static final int LOCATION_DISTANCE_Y = 150;

	public static final int DIALOG_FRAME_DEFAULT_HEIGHT = 400;
	public static final int DIALOG_FRAME_DEFAULT_WIDTH = 800;
	public static final Dimension dialogFrameDefaultSize = new Dimension(DIALOG_FRAME_DEFAULT_WIDTH,
			DIALOG_FRAME_DEFAULT_HEIGHT);

	private static final String IMAGE_BASE = "de/uib/configed/gui/";

	public static boolean isMultiFactorAuthenticationEnabled;

	private Globals() {
	}

	public static final class ProductPackageVersionSeparator {

		public static final String FOR_DISPLAY = "-";
		public static final String FOR_KEY = ";";

		// private constructor to hide the implicit public one
		private ProductPackageVersionSeparator() {
		}

		public static String formatKeyForDisplay(String key) {
			if (key == null) {
				return null;
			}

			int i = key.indexOf(FOR_KEY);
			if (i == -1) {
				return key;
			}

			String result = key.substring(0, i);
			if (i < key.length()) {
				result = result + FOR_DISPLAY + key.substring(i + 1);
			}

			return result;
		}
	}

	public static String[] getLogTypes() {
		return logTypes;
	}

	public static String getLogType(int index) {
		if (index < 0 || index >= logTypes.length) {
			return "";
		} else {
			return logTypes[index];
		}
	}

	public static Border createPanelBorder() {
		return new LineBorder(Globals.BACKGROUND_COLOR_6, 2, true);
	}

	public static void formatButtonSmallText(AbstractButton button) {
		if (!ConfigedMain.FONT) {
			button.setFont(defaultFontSmall);
		}
		button.setPreferredSize(new Dimension(45, 20));
		if (!ConfigedMain.THEMES) {
			button.setForeground(lightBlack);
			button.setBackground(BACKGROUND_COLOR_6);
		}
		button.setOpaque(false);

		button.setBorderPainted(false);

	}

	public static void showAboutAction(JFrame parent) {
		FTextArea info = new FTextArea(parent, Globals.APPNAME + " Copyright Information", true,
				new String[] { Configed.getResourceValue("FGeneralDialog.ok") }, 500, 300);

		StringBuilder message = new StringBuilder();

		for (String line : CopyrightInfos.get()) {
			message.append("\n");
			message.append(line);
		}

		info.setMessage(message.toString());
		info.setVisible(true);
	}

	public static boolean interpretAsBoolean(Object value) {

		if (value == null) {
			return false;
		}

		if (value instanceof Boolean) {
			return (Boolean) value;
		}

		if (value instanceof Integer) {
			int val = (Integer) value;
			if (val == 1) {
				return true;
			} else if (val == 0) {
				return false;
			} else {
				throw new IllegalArgumentException("" + value + " cannot be interpreted as boolean");
			}
		}

		if (value instanceof String) {

			String val = ((String) value).toLowerCase();

			if (val.isEmpty()) {
				return false;
			}

			if ("true".equals(val)) {
				return true;
			}

			if ("false".equals(val)) {
				return false;
			}

			if ("1".equals(val)) {
				return true;
			}

			if ("0".equals(val)) {
				return false;
			}

			throw new IllegalArgumentException(" " + value + " cannot be interpreted as boolean");
		}

		throw new IllegalArgumentException(" " + value + " cannot be interpreted as boolean");
	}

	public static int getMaxLogSize(int index) {
		if (index < 0 || index >= maxLogSizes.length) {
			Logging.warning("error with index for maxLogSizes");
			return -1;
		} else {
			return maxLogSizes[index];
		}
	}

	public static Map<String, Object> getMap() {
		if (objects == null) {
			objects = new HashMap<>();

			objects.put("mainIcon", mainIcon);
			objects.put("defaultFont", defaultFont);
			objects.put("APPNAME", APPNAME);
		}

		return objects;
	}

	public static Collator getCollator() {
		if (alphaCollator == null) {
			alphaCollator = Collator.getInstance();

			alphaCollator.setStrength(Collator.IDENTICAL);
		}
		return alphaCollator;
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().startsWith("windows");
	}

	public static String fillStringToLength(String s, int len) {
		if (s.length() > len) {
			return s;
		}

		StringBuilder result = new StringBuilder(s);

		for (int i = s.length(); i < len; i++) {
			result.append(' ');
		}

		return result.toString();
	}

	public static java.net.URL getImageResourceURL(String relPath) {
		String resourceS = IMAGE_BASE + relPath;

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		java.net.URL imgURL = cl.getResource(resourceS);
		if (imgURL != null) {
			return imgURL;
		} else {
			Logging.warning("Couldn't find file  " + relPath);
			return null;
		}
	}

	public static ImageIcon createImageIcon(String path, String description) {
		String xPath = IMAGE_BASE + path;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		// based on MainFrame

		java.net.URL imgURL = cl.getResource(xPath);

		// should have the same result (but seems not to have)

		try {

			if (imgURL != null) {
				return new ImageIcon(imgURL, description);
			} else {
				Logging.info("Couldn't find file: " + path);
				return null;
			}
		} catch (Exception ex) {
			Logging.info("createImageIcon " + path + " : " + ex);

		}

		return null;
	}

	public static String getSeconds() {
		String sqlNow = new java.sql.Timestamp(System.currentTimeMillis()).toString();

		int i = sqlNow.lastIndexOf(' ');
		String date = sqlNow.substring(0, i);
		date = date.replace(' ', '-');
		String time = sqlNow.substring(i + 1);
		time = time.substring(0, time.indexOf('.'));

		return date + "_" + time;

	}

	public static String getDate() {
		String sqlNow = new java.sql.Timestamp(System.currentTimeMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(' '));

		return sqlNow;
	}

	public static Date getToday() {
		return new java.sql.Timestamp(System.currentTimeMillis());
	}

	private static String formatlNumberUpTo99(long n) {
		if (n < 10) {
			return "0" + n;
		} else {
			return "" + n;
		}
	}

	public static void threadSleep(Object caller, long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
			Logging.info(caller, "sleeping interrupted: " + ie);
			Thread.currentThread().interrupt();
		}
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
		if (s == null) {
			return "";
		}

		return s.toString();
	}

	public static List<String> takeAsStringList(List<Object> list) {
		List<String> result = new ArrayList<>();

		if (list == null) {
			return result;
		}

		for (Object val : list) {
			result.add((String) val);
		}

		return result;
	}

	public static List<Object> getNowTimeListValue() {
		return getNowTimeListValue(null);
	}

	public static List<Object> getNowTimeListValue(final String comment) {
		List<Object> result = new ArrayList<>();

		String now = new java.sql.Timestamp(System.currentTimeMillis()).toString();
		now = now.substring(0, now.indexOf("."));
		if (comment != null) {
			result.add(now + " " + comment);
		} else {
			result.add(now);
		}

		Logging.info("getNowTimeListValue" + result);

		return result;
	}

	public static void showExternalDocument(String link) {
		new Application() {

			@Override
			public void start(Stage primaryStage) throws Exception {
				// Empty, because not needed
			}
		}.getHostServices().showDocument(link);
	}

	public static String pseudokey(String[] partialvalues) {
		StringBuilder resultBuilder = new StringBuilder("");

		if (partialvalues.length > 0) {
			resultBuilder.append(partialvalues[0]);

			for (int i = 1; i < partialvalues.length; i++) {
				resultBuilder.append(";");
				resultBuilder.append(partialvalues[i]);
			}
		}

		return resultBuilder.toString();
	}

	public static String produceNonNull(Object o) {
		String result = "";
		if (o != null) {
			result = o.toString();
		}

		return result;
	}

	public static boolean checkCollection(Object source, String cName, Object c) {
		boolean result = c != null;
		if (result) {
			if (!(c instanceof Collection) && !(c instanceof Map)) {
				Logging.info(source.getClass().getName() + " " + cName + " is neither a Collection nor a Map  ");
				result = false;
			}
		} else {
			Logging.debug(source.getClass().getName() + " " + cName + " is null");
		}

		return result;
	}

	public static String makeHTMLlines(String s) {
		if (s == null || s.trim().startsWith("<")) {
			return s;
		}

		final int MAX_LINE_LENGTH = 80;

		StringBuilder b = new StringBuilder("<html>");
		int charsInLine = 0;
		boolean indentDone = false;
		int lineIndent = 0;
		for (int c = 0; c < s.length(); c++) {
			charsInLine++;
			switch (s.charAt(c)) {
			case ' ':
				b.append("&nbsp;");
				if (!indentDone) {
					lineIndent = lineIndent + 1;
				}
				break;
			case '\t':
				b.append("&nbsp;&nbsp;&nbsp;");
				if (!indentDone) {
					lineIndent = lineIndent + 3;
				}
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
				break;
			}
			if (charsInLine >= MAX_LINE_LENGTH && c + 1 < s.length()
					&& (s.charAt(c + 1) == ' ' || s.charAt(c + 1) == '\t' || s.charAt(c + 1) == '\n')) {
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

		b.append("</html>");

		return b.toString();
	}

	public static String usedMemory() {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();

		return " " + (((total - free) / 1024) / 1024) + " MB ";
	}

	@SuppressWarnings("java:S106")
	public static String getCLIparam(String question, boolean password) {
		java.io.Console con = System.console();
		if (con == null) {
			return "";
		}

		System.out.print(question);
		if (password) {
			return String.valueOf(con.readPassword()).trim();
		}

		try (Scanner sc = new Scanner(con.reader())) {
			return sc.nextLine();
		}
	}

	public static boolean isGlobalReadOnly() {
		boolean result = PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly();

		Logging.info("Globals got readonly " + result);

		return result;
	}

	public static boolean isServerFullPermission() {
		if (PersistenceControllerFactory.getPersistenceController() == null) {
			return false;
		}

		return PersistenceControllerFactory.getPersistenceController().isServerFullPermission();

	}

	public static boolean forbidEditingTargetSpecific() {
		boolean forbidEditing = false;

		Logging.debug("forbidEditing for target " + ConfigedMain.getEditingTarget() + "?");

		if (ConfigedMain.getEditingTarget() == ConfigedMain.EditingTarget.SERVER) {
			forbidEditing

					= !PersistenceControllerFactory.getPersistenceController().isServerFullPermission();
		} else {
			forbidEditing = PersistenceControllerFactory.getPersistenceController().isGlobalReadOnly();
		}

		Logging.debug("forbidEditing " + forbidEditing);

		return forbidEditing;
	}

	public static boolean isKeyForSecretValue(String s) {
		String t = s.toLowerCase();

		return t.indexOf("password") > -1 || t.startsWith("secret");
	}
}
