/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.productpage.TextMarkdownPane;
import de.uib.configed.share.logging.Logging;

/**
 * Utility methods for showing Swing dialogs and message popups.
 * <p>
 * This class centralizes all application-wide dialog logic such as warning
 * messages, informational popups, and "About" or "Credits" dialogs. It exists
 * to keep UI classes free from repetitive dialog construction code.
 * </p>
 * <p>
 * Only dialog-related helpers should live here. General Swing component helpers
 * belong in {@link SwingUtils}.
 * </p>
 */
public final class DialogUtils {
	private static final String COMPLETE_VERSION_INFO = System.getProperty("java.runtime.version");

	private DialogUtils() {
	}

	/** Shows a warning about missing licensed modules */
	public static void showMissingLicenseModules(String message) {
		TextMarkdownPane textPane = new TextMarkdownPane();
		textPane.setText(message);

		JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), textPane,
				Configed.getResourceValue("Permission.modules.title"), JOptionPane.WARNING_MESSAGE);
	}

	/** Shows the About dialog with app version, copyright, and Java info */
	public static void showAbout(JFrame parent) {
		String message = String.join("\n",
				Globals.APPNAME + "  " + Configed.getResourceValue("LoginDialog.version") + "  " + Globals.VERSION
						+ " (" + Globals.VERDATE + ")",
				"", "The opsi-logviewer is part of the " + Globals.APPNAME + " since version 4.2.22.1",
				"______________________________________________________________________", "", Globals.COPYRIGHT1,
				Globals.COPYRIGHT2, "", "running on java version " + COMPLETE_VERSION_INFO,
				"on architecture " + System.getProperty("os.arch"));

		JTextArea textArea = new JTextArea(message);
		textArea.setEditable(false);
		textArea.setCaretPosition(0);

		JOptionPane.showMessageDialog(parent, textArea,
				Configed.getResourceValue("Utils.aboutOpsiConfiged") + " " + Globals.APPNAME,
				JOptionPane.PLAIN_MESSAGE);
	}

	/** Shows credits dialog, including optional content from credits.md */
	public static void showCredits(JFrame parent) {
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("FCreditsDialog.message1")).append("<br>")
				.append(Configed.getResourceValue("FCreditsDialog.message2")).append("<br><br>")
				.append(Configed.getResourceValue("FCreditsDialog.message3")).append("<br><br>");

		appendCreditsFromFile(message);

		TextMarkdownPane textPane = new TextMarkdownPane();
		textPane.setText(message.toString());
		textPane.setPreferredSize(new Dimension(textPane.getPreferredSize().width, 200));

		JScrollPane scrollPane = new JScrollPane(textPane);
		JOptionPane.showMessageDialog(parent, scrollPane, Configed.getResourceValue("MainFrame.jMenuHelpCredits"),
				JOptionPane.PLAIN_MESSAGE);
	}

	private static void appendCreditsFromFile(StringBuilder message) {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("credits.md"),
						StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				message.append(line).append("<br>");
			}
		} catch (IOException e) {
			Logging.warning(e, "Unable to read credits file");
		}
	}

	public static boolean includeOpsiHostKey() {
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("Utils.opsiHostKey.message1"));
		message.append("\n\n");
		message.append(Configed.getResourceValue("Utils.opsiHostKey.message2"));

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("securityWarning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		return answer == JOptionPane.YES_OPTION;
	}

	public static void enableDialogResizing(JOptionPane optionPane) {
		HierarchyListener listener = new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				Window window = SwingUtilities.getWindowAncestor(optionPane);
				if (window instanceof Dialog dialog && !dialog.isResizable()) {
					dialog.setResizable(true);
					optionPane.removeHierarchyListener(this);
				}
			}
		};
		optionPane.addHierarchyListener(listener);
	}
}
