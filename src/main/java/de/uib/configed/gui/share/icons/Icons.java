/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.icons;

import java.awt.Color;
import java.awt.Image;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;

import de.uib.configed.app.Main;
import de.uib.configed.core.domain.modulelicense.LicensingInfoMap;
import de.uib.configed.core.domain.modulelicense.OpsiLicensing;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.healthcheck.HealthDataProcessor;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public final class Icons {
	private static Color originalColorLight = new Color(206, 208, 214);
	private static Color originalColorDark = new Color(108, 112, 126);

	private static Image mainIcon;

	private Icons() {
	}

	public static Image getMainIcon() {
		if (mainIcon == null) {
			mainIcon = createMainIcon();
		}
		return mainIcon;
	}

	private static Image createMainIcon() {
		String iconPath = (Main.isLogviewer() ? Globals.ICON_LOGVIEWER : Globals.ICON_CONFIGED);
		ImageIcon icon = createImageIcon(iconPath, "");

		if (icon != null) {
			return icon.getImage();
		} else {
			Logging.warning(Utils.class, "cannot create main icon, icon ", iconPath, "not found");
			return null;
		}
	}

	private static FlatSVGIcon getThemeIconForThemeMenu(boolean dark, String iconName) {
		ColorFilter filter = new ColorFilter();
		if (dark) {
			iconName = iconName + "_dark";
			filter.add(originalColorLight, Globals.OPSI_FOREGROUND_LIGHT);
		} else {
			filter.add(originalColorDark, Globals.OPSI_FOREGROUND_DARK);
		}

		return new FlatSVGIcon(Globals.IMAGE_BASE + "intellij/" + iconName + ".svg").setColorFilter(filter);
	}

	public static void addThemeIconInvertedToMenuItem(AbstractButton abstractButton, String iconName) {
		abstractButton.setIcon(getThemeIconForThemeMenu(!FlatLaf.isLafDark(), iconName));
		if (!FlatLaf.isLafDark()) {
			abstractButton.setSelectedIcon(getThemeIconForThemeMenu(false, iconName));
		}
	}

	public static void addThemeIconToMenuItem(AbstractButton abstractButton, String iconName) {
		abstractButton.setIcon(getThemeIntellijIcon(iconName, 16));
		abstractButton.setSelectedIcon(getThemeIntellijIcon(iconName, 16)
				.setColorFilter(new ColorFilter(color -> Globals.OPSI_FOREGROUND_DARK)));
	}

	public static FlatSVGIcon getThemeIntellijIcon(String iconName, int size) {
		return getThemeIcon(iconName, "intellij", size);
	}

	public static FlatSVGIcon getThemeSVGRepoIcon(String iconName, int size) {
		return getThemeIcon(iconName, "svgrepo", size);
	}

	private static FlatSVGIcon getThemeIcon(String iconName, String folder, int size) {
		ColorFilter filter = new ColorFilter();
		if (FlatLaf.isLafDark()) {
			iconName = iconName + "_dark";
			filter.add(originalColorLight, Globals.OPSI_FOREGROUND_DARK);
		} else {
			filter.add(originalColorDark, Globals.OPSI_FOREGROUND_LIGHT);
		}

		return new FlatSVGIcon(Globals.IMAGE_BASE + folder + "/" + iconName + ".svg").setColorFilter(filter)
				.derive(size, size);
	}

	public static FlatSVGIcon getThemeFilledIcon(String iconName, int size) {
		FlatSVGIcon icon = getThemeIntellijIcon(iconName, size);

		ColorFilter filter = icon.getColorFilter();
		if (FlatLaf.isLafDark()) {
			filter.add(new Color(67, 69, 74), Globals.OPSI_FOREGROUND_DARK);
		} else {
			filter.add(new Color(235, 236, 240), Globals.OPSI_FOREGROUND_LIGHT);
		}

		return new FlatSVGIcon(Globals.IMAGE_BASE + "intellij/" + iconName + ".svg").setColorFilter(filter);
	}

	public static FlatSVGIcon getIntellijIcon(String iconName, Color color) {
		String path = Globals.IMAGE_BASE + "intellij/" + iconName + ".svg";

		ColorFilter filter = new ColorFilter();

		filter.add(originalColorDark, color);
		FlatSVGIcon icon = new FlatSVGIcon(path);
		icon.setColorFilter(filter);
		return icon;
	}

	public static FlatSVGIcon getIntellijIcon(String iconName, int size) {
		return getIntellijIcon(iconName).derive(size, size);
	}

	public static FlatSVGIcon getIntellijIcon(String iconName, Color color, int size) {
		return getIntellijIcon(iconName, color).derive(size, size);
	}

	public static void addIntellijIconToMenuItem(AbstractButton abstractButton, String name) {
		abstractButton.setIcon(getIntellijIcon(name));

		FlatSVGIcon selectedIcon = new FlatSVGIcon(Globals.IMAGE_BASE + "intellij/" + name + ".svg");
		selectedIcon.setColorFilter(new ColorFilter(color -> Globals.OPSI_FOREGROUND_DARK));
		abstractButton.setSelectedIcon(selectedIcon);
	}

	public static FlatSVGIcon getOpsiIcon(int size) {
		FlatSVGIcon icon = new FlatSVGIcon(Globals.IMAGE_BASE + "opsilogos/favicon.svg");
		return icon.derive(size, size);
	}

	public static FlatSVGIcon getOpsiThemeIcon(int size) {
		return getOpsiIcon(size, Globals.getForegroundColor());
	}

	public static FlatSVGIcon getOpsiIcon(int size, Color color) {
		return getOpsiIcon(size).setColorFilter(new ColorFilter(oldColor -> color));
	}

	public static Icon getSelectedOpsiModulesIcon(int size) {
		return getOpsiModulesIcon(size, Globals.OPSI_FOREGROUND_DARK);
	}

	public static Icon getActiveOpsiModulesIcon(int size) {
		return getOpsiModulesIcon(size, Globals.getActiveColor());
	}

	public static Icon getOpsiModulesIcon(int size) {
		return getOpsiModulesIcon(size, null);
	}

	/* 
	 * @param color the color of the opsi icon. If null, the theme color will be used.
	 */
	private static Icon getOpsiModulesIcon(int size, Color iconColor) {
		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();

		Color dotColor = null;
		if (persistenceController.getDataServices().module.isOpsiUserAdminPD()) {
			LicensingInfoMap licensingInfoMap = LicensingInfoMap.getInstance(
					persistenceController.getDataServices().module.getOpsiLicensingInfoOpsiAdminPD(),
					persistenceController.getDataServices().config.getConfigDefaultValuesPD(),
					!OpsiLicensing.isExtendedView());

			dotColor = switch (licensingInfoMap.getWarningLevel()) {
			case LicensingInfoMap.STATE_OVER_LIMIT -> Globals.OPSI_ERROR;
			case LicensingInfoMap.STATE_CLOSE_TO_LIMIT -> Globals.OPSI_WARNING;
			case LicensingInfoMap.STATE_OKAY -> {
				Logging.info("icon will remain null, we don't want to show a dot when modules are okay");
				yield null;
			}
			default -> {
				Logging.warning(Utils.class, "unexpected warninglevel: ", licensingInfoMap.getWarningLevel());
				yield null;
			}
			};
		}

		FlatSVGIcon opsiIcon;
		if (iconColor == null) {
			opsiIcon = getOpsiThemeIcon(size);
		} else {
			opsiIcon = getOpsiIcon(size, iconColor);
		}

		if (dotColor == null) {
			return opsiIcon;
		} else if (Globals.OPSI_FOREGROUND_DARK.equals(iconColor)) {
			return getDottedIcon(opsiIcon, iconColor, size);
		} else {
			return getDottedIcon(opsiIcon, dotColor, size);
		}
	}

	private static Icon getDottedIcon(Icon baseIcon, Color dotColor, int size) {
		FlatSVGIcon point = getIntellijIcon("point", dotColor, size / 4);

		return new CombinedSVGIcon(baseIcon, point, 1, 3.0 / 4, 3.0 / 4);
	}

	/**
	 * We will set the icons for the health check and then load the "real" icons
	 * in a separate thread because it may take a lot of time to load the health
	 * check.
	 */
	public static void addActiveHealthCheckIcon(AbstractButton button, int size) {
		button.setIcon(Icons.getIntellijIcon("springBootHealth", 32));
		button.setSelectedIcon(Icons.getIntellijIcon("springBootHealth", Globals.getActiveColor(), 32));

		new Thread(() -> {
			button.setIcon(getHealthCheckIcon(size));
			button.setSelectedIcon(getHealthCheckIcon(size, Globals.getActiveColor()));
		}).start();
	}

	/**
	 * We will set the icons for the health check and then load the "real" icons
	 * in a separate thread because it may take a lot of time to load the health
	 * check.
	 */
	public static void addSelectedHealthCheckIcon(AbstractButton button, int size) {
		button.setIcon(Icons.getIntellijIcon("springBootHealth"));
		button.setSelectedIcon(Icons.getIntellijIcon("springBootHealth", Globals.OPSI_FOREGROUND_DARK));

		new Thread(() -> {
			button.setIcon(getHealthCheckIcon(size));
			button.setSelectedIcon(getHealthCheckIcon(size, Globals.OPSI_FOREGROUND_DARK));
		}).start();
	}

	private static Icon getHealthCheckIcon(int size) {
		return getHealthCheckIcon(size, null);
	}

	private static Icon getHealthCheckIcon(int size, Color iconColor) {
		HealthDataProcessor.StatusLevel warningLevel = HealthDataProcessor.getMaxStatusLevel();

		Color dotColor = switch (warningLevel) {
		case ERROR -> Globals.OPSI_ERROR;
		case WARNING -> Globals.OPSI_WARNING;
		case OK -> {
			Logging.info("icon will remain null, we don't want to show a dot when health check are okay");
			yield null;
		}
		default -> {
			Logging.warning(Utils.class, "unexpected warninglevel: ", HealthDataProcessor.getMaxStatusLevel());
			yield null;
		}
		};

		FlatSVGIcon opsiIcon;
		if (iconColor == null) {
			opsiIcon = getIntellijIcon("springBootHealth", size);
		} else {
			opsiIcon = getIntellijIcon("springBootHealth", iconColor, size);
		}

		if (dotColor == null) {
			return opsiIcon;
		} else if (Globals.OPSI_FOREGROUND_DARK.equals(iconColor)) {
			return getDottedIcon(opsiIcon, iconColor, size);
		} else {
			return getDottedIcon(opsiIcon, dotColor, size);
		}
	}

	public static void addOpsiIconToMenuItem(AbstractButton abstractButton) {
		FlatSVGIcon icon = new FlatSVGIcon(Globals.IMAGE_BASE + "opsilogos/favicon.svg");

		// set normal icon
		abstractButton.setIcon(icon.derive(16, 16));

		// Create filter for selected icon
		ColorFilter filter = new ColorFilter();
		filter.add(Globals.OPSI_MAGENTA, Globals.OPSI_FOREGROUND_DARK);
		icon = icon.derive(16, 16);
		icon.setColorFilter(filter);
		abstractButton.setSelectedIcon(icon);
	}

	public static FlatSVGIcon getSelectedIntellijIcon(String iconName) {
		String path = Globals.IMAGE_BASE + "intellij/" + iconName + ".svg";

		return new FlatSVGIcon(path).setColorFilter(new ColorFilter(color -> Globals.getActiveColor()));
	}

	public static FlatSVGIcon getSelectedIntellijIcon(String iconName, int size) {
		return getSelectedIntellijIcon(iconName).derive(size, size);
	}

	public static FlatSVGIcon getSelectedThemeIntelljIcon(String iconName, int size) {
		ColorFilter filter = new ColorFilter();
		if (FlatLaf.isLafDark()) {
			iconName += "_dark";
			filter.add(originalColorLight, Globals.ICON_ACTIVE_DARK);
		} else {
			filter.add(originalColorDark, Globals.ICON_ACTIVE_LIGHT);
		}

		return new FlatSVGIcon(Globals.IMAGE_BASE + "intellij/" + iconName + ".svg").setColorFilter(filter).derive(size,
				size);
	}

	public static FlatSVGIcon getIntellijIcon(String iconName) {
		return getIntellijIcon(iconName, Globals.getForegroundColor());
	}

	public static ImageIcon createImageIcon(String path, String description) {
		String xPath = Globals.IMAGE_BASE + path;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL imgURL = cl.getResource(xPath);

		// should have the same result (but seems not to have)
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			Logging.info("Couldn't find file: ", path);
			return null;
		}
	}

	public static FlatSVGIcon getOpsiLogoWide() {
		String iconName = "opsi_logo_wide";
		if (FlatLaf.isLafDark()) {
			iconName += "_dark";
		}

		return new FlatSVGIcon(Globals.IMAGE_BASE + "opsilogos/" + iconName + ".svg").derive(100, 36);
	}
}
