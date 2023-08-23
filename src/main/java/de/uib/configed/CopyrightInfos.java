/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.List;

/**
 * configed - configuration editor for client work stations in opsi (open pc
 * server integration) www.opsi.org CopyrightInfos 2017
 */

public final class CopyrightInfos {

	private static final String COMPLETE_VERSION_INFO = System.getProperty("java.runtime.version");

	private static final List<String> infos;
	static {
		infos = new ArrayList<>();

		infos.add(Globals.APPNAME + "  " + Configed.getResourceValue("DPassword.version") + "  " + Globals.VERSION
				+ " (" + Globals.VERDATE + ") " + Globals.VERHASHTAG);
		infos.add("");
		infos.add("The opsi-logviewer is part of the " + Globals.APPNAME + " since version 4.2.22.1");
		infos.add("______________________________________________________________________");
		infos.add("");
		infos.add(Globals.COPYRIGHT1);
		infos.add(Globals.COPYRIGHT2);
		infos.add("");
		infos.add("running on java version " + COMPLETE_VERSION_INFO);
		infos.add("");
		infos.add("");
		infos.add(Configed.getResourceValue("CopyrightInfos.thanksTo"));
		infos.add("");
		infos.add("commons-io Apache library");
		infos.add("https://www.apache.org/licenses/");
		infos.add("");
		infos.add("commons-compress Apache library");
		infos.add("https://www.apache.org/licenses/");
		infos.add("");
		infos.add("KDE oxygen icon library");
		infos.add("licensed by a Creative Commons Public License");
		infos.add("");
		infos.add("iText® 5.5.13.3 ©2000-2023 pdf library");
		infos.add("licensed by AGPL");
		infos.add("");
		infos.add("jSch 0.1.55 SSH library");
		infos.add("licensed by BSD-style library");
		infos.add("Copyright(c) 2002-2022 Atsuhiko Yamanaka, JCraft,Inc");
		infos.add("");
		infos.add("SwingX extension of Swing");
		infos.add("licenced by GNU LESSER GENERAL PUBLIC LICENSE");
		infos.add("Copyright (c) 2005-2006 Sun Microsystems, Inc.");
		infos.add("");
		infos.add("JavaFX");
		infos.add("licenced by GNU General Public LICENSE v2.0");
		infos.add("");
		infos.add("FontAwesomeFX Icon Package");
		infos.add("licenced by Apache 2.0 licenses");
		infos.add("");
		infos.add("XZ for Java");
		infos.add("https://github.com/tukaani-project/xz-java/blob/master/COPYING");
	}

	private CopyrightInfos() {
	}

	public static List<String> get() {
		return infos;
	}
}
