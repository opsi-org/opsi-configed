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

/**
 * This class contains app constants (including some global functions)
 */

public final class Globals {
	// get version from pom.xml
	public static final String VERSION = Globals.class.getPackage().getImplementationVersion();
	public static final String VERDATE = "2023-10-17";

	public static final String VERHASHTAG = "";

	public static final String ZERODATE = "";

	public static final String COPYRIGHT1 = "Copyright (c) uib 2001 - 2023 (www.uib.de)";
	public static final String COPYRIGHT2 = "Open Source license: AGPL v3";

	public static final String APPNAME = "opsi-configed";
	public static final String APPNAME_SERVER_CONNECTION = "opsi config editor";

	public static final String ICON_RESOURCE_NAME = "opsi.gif";

	public static final String ICON_OPSI = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_nur_Biene_quer.png";

	public static final String OPSI_DOC_PAGE = "http://www.opsi.org";
	public static final String OPSI_SUPPORT_PAGE = "http://opsi.org/support";
	public static final String OPSI_FORUM_PAGE = "http://forum.opsi.org";

	public static final String CERTIFICATE_FILE_NAME = "opsi-ca-cert";
	public static final String CERTIFICATE_FILE_EXTENSION = "pem";
	public static final String CERTIFICATE_FILE = CERTIFICATE_FILE_NAME + "." + CERTIFICATE_FILE_EXTENSION;

	public static final String HEALTH_CHECK_LOG_FILE_NAME = "healthCheck.log";
	public static final String DIAGNOSTIC_DATA_JSON_FILE_NAME = "diagnosticData.json";

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

	public static final Color OPSI_LOGO_BLUE = new Color(106, 128, 174);
	public static final Color OPSI_LOGO_LIGHT_BLUE = new Color(195, 200, 222);

	// New colors

	public static final Color OPSI_BLUE_2 = new Color(42, 60, 111);
	public static final Color OPSI_DARK_BLUE_2 = new Color(30, 43, 80);

	public static final Color OPSI_LIGHT_BLUE = new Color(159, 172, 210);
	public static final Color OPSI_LIGHT_BLUE_2 = new Color(141, 154, 192);

	public static final Color OPSI_MAGENTA_2 = new Color(135, 20, 58);
	public static final Color OPSI_DARK_MAGENTA_2 = new Color(101, 15, 44);
	public static final Color OPSI_LIGHT_MAGENTA = new Color(244, 205, 218);
	public static final Color OPSI_LIGHT_MAGENTA_2 = new Color(238, 180, 199);

	public static final Color OPSI_OK = LOG_COLOR_NOTICE;
	public static final Color OPSI_OK_DARK = new Color(36, 107, 50);

	public static final Color OPSI_WARNING = LOG_COLOR_WARNING;
	public static final Color OPSI_WARNING_DARK = new Color(187, 136, 25);

	public static final Color OPSI_ERROR = LOG_COLOR_ERROR;
	public static final Color OPSI_ERROR_DARK = new Color(170, 33, 38);

	// End new colors

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

	public static final Color OPSI_BACKGROUND_LIGHT = new Color(255, 255, 255);
	public static final Color OPSI_BACKGROUND_DARK = new Color(31, 31, 31);

	public static final Color OPSI_FOREGROUND_LIGHT = new Color(0, 0, 0);
	public static final Color OPSI_FOREGROUND_DARK = new Color(225, 225, 225);

	public static final Color FAILED_COLOR = OPSI_ERROR;
	public static final Color ACTION_COLOR = OPSI_OK;
	public static final Color OK_COLOR = OPSI_OK;
	public static final Color UNKNOWN_COLOR = LOG_COLOR_SECRET;

	public static final Color DARK_ORANGE = new Color(218, 180, 4);
	public static final Color LIGHT_BLACK = new Color(30, 30, 30);
	public static final Color BLUE = new Color(30, 30, 100);
	public static final Color BLUE_GREY = new Color(180, 190, 190);
	public static final Color GREYED = new Color(150, 150, 150);

	public static final Color CLIENT_SELECTION_DIALOG_ELEMENT_FOREGROUND = Color.BLUE;

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
	public static final Color PANEL_PRODUCT_INFO_PANE_INACTIVE = GREYED;

	public static final Color SSH_CONNECTION_OUTPUT_INIT_BACKGROUND_COLOR = Color.GREEN;
	public static final Color SSH_CONNECTION_OUTPUT_INIT_FOREGROUND_COLOR = FAILED_COLOR;

	public static final Color F_GENERAL_DIALOG_LICENSING_INFO_BACKGROUND_COLOR = PRIMARY_BACKGROUND_COLOR;

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

	public static final Color NIMBUS_SELECTION_BACKGROUND = new Color(57, 105, 138);
	public static final Color NIMBUS_BACKGROUND = new Color(214, 217, 223);
	public static final Color BACK_NIMBUS = new Color(214, 219, 222);

	// in table, change colors by row
	public static final Color DEFAULT_TABLE_CELL_BG_COLOR_1 = PRIMARY_BACKGROUND_COLOR;
	public static final Color DEFAULT_TABLE_CELL_BG_COLOR_2 = BACKGROUND_COLOR_9;

	public static final Color DEFAULT_TABLE_SELECTION_ROW_DARK = new Color(204, 220, 238);
	public static final Color DEFAULT_TABLE_SELECTED_ROW_BRIGHT = new Color(221, 233, 249);

	// in table, change colors by row and column
	public static final Color DEFAULT_TABLE_CELL_BG_COLOR_00 = BACKGROUND_COLOR_9;
	public static final Color DEFAULT_TABLE_CELL_BG_COLOR_01 = new Color(236, 235, 214);
	public static final Color DEFAULT_TABLE_CELL_BG_COLOR_10 = PRIMARY_BACKGROUND_COLOR;
	public static final Color DEFAULT_TABLE_CELL_GB_COLOR_11 = SECONDARY_BACKGROUND_COLOR;

	public static final Color DEFAULT_TABLE_HEADER_BG_COLOR = new Color(222, 231, 247);
	public static final Color DEFAULT_TABLE_CELL_SELECTED_BG_COLOR = new Color(206, 224, 235);
	public static final Color DEFAULT_TABLE_CELL_SELECTED_BG_COLOR_NOT_EDITABLE = new Color(189, 207, 231);

	public static final Color opsiLogoBlue = new Color(106, 128, 174);
	public static final Color opsiLogoLightBlue = new Color(195, 200, 222);

	public static final Color CONFLICT_STATE_CELL_COLOR = new Color(255, 180, 180);

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
	public static final int TABLE_ROW_HEIGHT = 20;
	public static final int BUTTON_WIDTH = 140;
	public static final int ICON_WIDTH = 60;
	public static final int LABEL_WIDTH = 80;
	public static final int TIME_SPINNER_WIDTH = 50;
	public static final int SQUARE_BUTTON_WIDTH = 24;

	public static final Dimension BUTTON_DIMENSION = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
	public static final Dimension LOWER_BUTTON_DIMENSION = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT - 4);
	public static final Dimension SMALL_BUTTON_DIMENSION = new Dimension(BUTTON_WIDTH / 2, BUTTON_HEIGHT);
	public static final Dimension SHORT_BUTTON_DIMENSION = new Dimension(BUTTON_WIDTH / 4, BUTTON_HEIGHT);
	public static final Dimension TEXT_FIELD_DIMENSION = new Dimension(BUTTON_WIDTH, LINE_HEIGHT);
	public static final Dimension LABEL_DIMENSION = new Dimension(LABEL_WIDTH, LINE_HEIGHT);
	public static final Dimension SHORT_LABEL_DIMENSION = new Dimension(60, LINE_HEIGHT);
	public static final int COUNTERFIELD_WIDTH = 160;
	public static final Dimension COUTNER_FIELD_DIMENSION = new Dimension(COUNTERFIELD_WIDTH, LINE_HEIGHT);
	public static final Dimension NEW_SMALL_BUTTON = new Dimension(30, 30);
	public static final Dimension MODE_SWITCH_DIMENSION = new Dimension(50, 50);
	public static final Dimension FILE_CHOOSER_SIZE = new Dimension(600, 400);

	public static final int GRAPHIC_BUTTON_HEIGHT = 40;
	public static final int GRAPHIC_BUTTON_WIDTH = 40;

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
	public static final int HFIRST_GAP = GAP_SIZE * 3;
	public static final int FIRST_LABEL_WIDTH = 250;

	public static final String STARRED_STRING = "*****";

	public static final String CONFLICT_STATE_STRING = "mixed";
	public static final String NO_VALID_STATE_STRING = "";

	public static final String PSEUDO_KEY_SEPARATOR = ";";

	public static final Dimension HELPER_FORM_DIMENSION = new Dimension(1100, 600);

	public static final int DIALOG_FRAME_DEFAULT_HEIGHT = 400;
	public static final int DIALOG_FRAME_DEFAULT_WIDTH = 800;
	public static final Dimension dialogFrameDefaultSize = new Dimension(DIALOG_FRAME_DEFAULT_WIDTH,
			DIALOG_FRAME_DEFAULT_HEIGHT);

	public static final String IMAGE_BASE = "de/uib/configed/gui/";

	public static final boolean THEMES_ENABLED = false;

	private Globals() {
	}
}
