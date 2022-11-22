package de.uib.configed;

/**
 * configed - configuration editor for client work stations in opsi (open pc
 * server integration) www.opsi.org CopyrightInfos 2017
 */

public class CopyrightInfos {
	static String discard, major, minor, update, build;
	static final String COMPLETE_VERSION_INFO = System.getProperty("java.runtime.version");

	private static final java.util.ArrayList<String> infos;
	static {
		infos = new java.util.ArrayList<>();

		infos.add(de.uib.configed.Globals.APPNAME + "  Version " + de.uib.configed.Globals.VERSION + " "
				+ de.uib.configed.Globals.VERDATE + " " + de.uib.configed.Globals.VERHASHTAG);
		infos.add("_________________________________________________________________________________");
		infos.add("");
		infos.add(de.uib.configed.Globals.COPYRIGHT1);
		infos.add(de.uib.configed.Globals.COPYRIGHT2);
		infos.add("");
		infos.add("running on java version " + COMPLETE_VERSION_INFO);
		infos.add("");
		infos.add("");
		infos.add(configed.getResourceValue("CopyrightInfos.thanksTo"));
		infos.add("");
		infos.add("commons-io Apache library");
		infos.add("http://www.apache.org/licenses/");
		infos.add("");
		infos.add("KDE oxygen icon library");
		infos.add("licensed by a Creative Commons Public License");
		infos.add("");
		infos.add("iText® 5.4.5 ©2000-2013 pdf library");
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
		infos.add("Tango Desktop Project");
		infos.add("http://tango.freedesktop.org/Tango_Desktop_Project ");
		infos.add("Public Domain");
		infos.add("");

	}

	private CopyrightInfos() {
	}

	public static java.util.List<String> get() {
		return infos;
	}
}