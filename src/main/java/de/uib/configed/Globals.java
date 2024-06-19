/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.text.DateFormat;

import javax.swing.UIManager;

/**
 * This class contains app constants
 */

public final class Globals {
	// get version from pom.xml
	public static final String VERSION = Globals.class.getPackage().getImplementationVersion();
	public static final String VERDATE = "2024-06-18";

	public static final String COPYRIGHT1 = "Copyright (c) uib 2001 - 2024 (www.uib.de)";
	public static final String COPYRIGHT2 = "Open Source license: AGPL v3";

	public static final String APPNAME = "opsi-configed";
	public static final String APPNAME_SERVER_CONNECTION = "opsi config editor";

	public static final String ICON_CONFIGED = "opsilogos/opsi-configed.png";
	public static final String ICON_LOGVIEWER = "opsilogos/opsi-logviewer.png";

	public static final String OPSI_DOC_PAGE = "http://www.opsi.org";
	public static final String OPSI_SUPPORT_PAGE = "http://opsi.org/support";
	public static final String OPSI_FORUM_PAGE = "http://forum.opsi.org";

	public static final String CERTIFICATE_FILE_NAME = "opsi-ca-cert";
	public static final String CERTIFICATE_FILE_EXTENSION = "pem";
	public static final String CERTIFICATE_FILE = CERTIFICATE_FILE_NAME + "." + CERTIFICATE_FILE_EXTENSION;

	public static final String HEALTH_CHECK_LOG_FILE_NAME = "healthCheck.log";
	public static final String DIAGNOSTIC_DATA_JSON_FILE_NAME = "diagnosticData.json";

	// The official opsi colors
	public static final Color OPSI_DARK_GREY = new Color(63, 63, 62);
	public static final Color OPSI_GREY = new Color(178, 178, 178);
	public static final Color OPSI_LIGHT_GREY = new Color(228, 228, 227);
	public static final Color OPSI_MAGENTA = new Color(203, 30, 88);
	public static final Color OPSI_BLUE = new Color(63, 90, 166);

	public static final Color OPSI_DARK_BLUE = new Color(45, 65, 120);

	// Logging colors
	public static final Color LOG_COLOR_ESSENTIAL = new Color(41, 121, 255);
	public static final Color LOG_COLOR_CRITICAL = new Color(226, 0, 102);
	public static final Color LOG_COLOR_ERROR = new Color(229, 29, 59);
	public static final Color LOG_COLOR_WARNING = new Color(255, 145, 0);

	public static final Color LOG_COLOR_NOTICE = new Color(0, 150, 5);
	public static final Color LOG_COLOR_INFO_LIGHT = new Color(33, 33, 33);
	public static final Color LOG_COLOR_INFO_DARK = new Color(245, 245, 245);
	public static final Color LOG_COLOR_DEBUG_LIGHT = new Color(86, 86, 86);
	public static final Color LOG_COLOR_DEBUG_DARK = new Color(192, 192, 192);
	public static final Color LOG_COLOR_TRACE = new Color(139, 139, 139);
	public static final Color LOG_COLOR_SECRET = new Color(213, 0, 249);

	public static final Color OPSI_OK = LOG_COLOR_NOTICE;
	public static final Color OPSI_OK_DARK = new Color(36, 107, 50);

	public static final Color OPSI_WARNING = LOG_COLOR_WARNING;
	public static final Color OPSI_WARNING_DARK = new Color(187, 136, 25);

	public static final Color OPSI_ERROR = LOG_COLOR_ERROR;
	public static final Color OPSI_ERROR_DARK = new Color(170, 33, 38);

	public static final Color OPSI_BACKGROUND_LIGHT = new Color(255, 255, 255);

	public static final Color OPSI_FOREGROUND_LIGHT = new Color(30, 30, 30);
	public static final Color OPSI_FOREGROUND_DARK = new Color(225, 225, 225);

	// End new colors

	public static final int DEFAULT_FTEXTAREA_HEIGHT = 200;
	public static final int DEFAULT_FTEXTAREA_WIDTH = 350;

	// some value which shall be interpreted as identical
	public static final Color INVISIBLE = new Color(11, 13, 17);

	public static final Color FAILED_COLOR = OPSI_ERROR;
	public static final Color ACTION_COLOR = OPSI_OK;
	public static final Color OK_COLOR = OPSI_OK;
	public static final Color UNKNOWN_COLOR = LOG_COLOR_SECRET;

	// also used for GlassPane...

	public static final Color LOG_PANE_CARET_COLOR = FAILED_COLOR;

	public static final Color PANEL_PRODUCT_SETTINGS_FAILED_COLOR = FAILED_COLOR;

	public static final Color ACTION_REQUEST_NONE_COLOR = INVISIBLE;
	public static final Color ACTION_REQUEST_SETUP_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_UPDATE_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_UNINSTALL_COLOR = LOG_COLOR_ERROR;
	public static final Color ACTION_REQUEST_ALWAYS_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_ONCE_COLOR = ACTION_COLOR;
	public static final Color ACTION_REQUEST_CUSTOM_COLOR = ACTION_COLOR;

	public static final Color INSTALLATION_STATUS_NOT_INSTALLED_COLOR = INVISIBLE;
	public static final Color INSTALLATION_STATUS_INSTALLED_COLOR = OK_COLOR;
	public static final Color INSTALLATION_STATUS_UNKNOWN_COLOR = UNKNOWN_COLOR;

	public static final Color PRODUCT_STATUS_MIXED_COLOR = LOG_COLOR_SECRET;

	// in table, change colors by row
	public static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

	public static final int TOOLTIP_INITIAL_DELAY_MS = 1000;
	public static final int TOOLTIP_DISMISS_DELAY_MS = 20000;
	public static final int TOOLTIP_RESHOW_DELAY_MS = 0;

	public static final int MIN_GAP_SIZE = 5;
	public static final int GAP_SIZE = 10;

	public static final int MIN_VSIZE = 10;
	public static final int PREF_VSIZE = 80;

	public static final int MIN_HSIZE = 50;
	public static final int PREF_HSIZE = 80;

	public static final int MIN_TABLE_V_SIZE = 40;

	public static final int BUTTON_HEIGHT = 24;
	public static final int LINE_HEIGHT = 28;
	public static final int SMALL_HEIGHT = 18;
	public static final int PROGRESS_BAR_HEIGHT = 10;
	public static final int BUTTON_WIDTH = 140;
	public static final int ICON_WIDTH = 60;
	public static final int LABEL_WIDTH = 80;

	public static final Dimension BUTTON_DIMENSION = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
	public static final Dimension SMALL_BUTTON_DIMENSION = new Dimension(BUTTON_WIDTH / 2, BUTTON_HEIGHT);
	public static final Dimension SHORT_BUTTON_DIMENSION = new Dimension(BUTTON_WIDTH / 4, BUTTON_HEIGHT);
	public static final Dimension TEXT_FIELD_DIMENSION = new Dimension(BUTTON_WIDTH, LINE_HEIGHT);
	public static final Dimension SHORT_LABEL_DIMENSION = new Dimension(60, LINE_HEIGHT);
	public static final int COUNTERFIELD_WIDTH = 160;
	public static final Dimension COUTNER_FIELD_DIMENSION = new Dimension(COUNTERFIELD_WIDTH, LINE_HEIGHT);
	public static final Dimension NEW_SMALL_BUTTON = new Dimension(30, 30);
	public static final Dimension MODE_SWITCH_DIMENSION = new Dimension(50, 50);

	// Make width long enough so that it will be not too small for the whole text
	public static final Dimension LABEL_SIZE_OF_JTREE = new Dimension(500, 20);

	public static final int GRAPHIC_BUTTON_SIZE = 40;

	public static final int COMBOBOX_ROW_COUNT = 20;

	public static final int WIDTH_FRAME_RENAME_CLIENT = 350;
	public static final int HEIGHT_FRAME_RENAME_CLIENT = 200;

	public static final int WIDTH_INFO_LOG_FILE = 400;
	public static final int HEIGHT_INFO_LOG_FILE = 200;

	public static final int REACHABLE_INFO_FRAME_WIDTH = 300;
	// Just enough so that the button is not hidden
	public static final int REACHABLE_INFO_FRAME_HEIGHT = 220;

	public static final Dimension GRAPHIC_BUTTON_DIMENSION = new Dimension(GRAPHIC_BUTTON_SIZE, GRAPHIC_BUTTON_SIZE);

	public static final int DATE_FORMAT_STYLE_PATTERN = DateFormat.LONG;

	// action form constants
	public static final int HFIRST_GAP = GAP_SIZE * 3;
	public static final int FIRST_LABEL_WIDTH = 250;

	public static final String STARRED_STRING = "*****";

	public static final String CONFLICT_STATE_STRING = "mixed";
	public static final String NO_VALID_STATE_STRING = "";

	public static final String PSEUDO_KEY_SEPARATOR = ";";

	public static final Dimension HELPER_FORM_DIMENSION = new Dimension(1100, 600);

	public static final int DIALOG_FRAME_DEFAULT_HEIGHT = 400;
	public static final int DIALOG_FRAME_DEFAULT_WIDTH = 800;

	public static final String IMAGE_BASE = "de/uib/configed/gui/";

	public static final int DEFAULT_PORT = 4447;

	// Colors for table cells
	private static Color magentaCell1;
	private static Color magentaCell2;

	private static Color greyCell1;
	private static Color greyCell2;

	private Globals() {
	}

	public static void setTableColors() {
		magentaCell1 = UIManager.getColor("magentaCell1");
		magentaCell2 = UIManager.getColor("magentaCell2");
		greyCell1 = UIManager.getColor("greyCell1");
		greyCell2 = UIManager.getColor("greyCell2");
	}

	public static Color getMagentaCell1() {
		return magentaCell1;
	}

	public static Color getMagentaCell2() {
		return magentaCell2;
	}

	public static Color getGreyCell1() {
		return greyCell1;
	}

	public static Color getGreyCell2() {
		return greyCell2;
	}
}
