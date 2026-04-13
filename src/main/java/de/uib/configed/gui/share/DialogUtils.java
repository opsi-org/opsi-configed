/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

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

	public static int showJListConfirmationDialog(String outerDialogTitle, String innerDialogTitle, String message,
			int maxDisplayLimit, Collection<String> items, Object[] options) {
		JOptionPane optionPane = new JOptionPane(
				createJListDialogContent(innerDialogTitle, message, maxDisplayLimit, items),
				JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);

		enableDialogResizing(optionPane);

		JDialog dialog = optionPane.createDialog(ConfigedMain.getMainFrame(), outerDialogTitle);
		dialog.setMinimumSize(new Dimension());
		dialog.pack();
		dialog.setVisible(true);
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());

		return getUserResponse(optionPane.getValue(), options);
	}

	private static int getUserResponse(Object selectedValue, Object[] options) {
		if (selectedValue == JOptionPane.UNINITIALIZED_VALUE) {
			return -1;
		}

		for (int i = 0; i < options.length; i++) {
			if (options[i].equals(selectedValue)) {
				return i;
			}
		}

		return -1;
	}

	private static JPanel createJListDialogContent(String title, String message, int maxItems,
			Collection<String> items) {
		JPanel content = new JPanel(new BorderLayout(10, 10));
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel topPanel = new JPanel();
		topPanel.add(new JLabel(message));

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(createItemListPanel(items, maxItems, title), BorderLayout.CENTER);

		content.add(topPanel, BorderLayout.NORTH);
		content.add(centerPanel, BorderLayout.CENTER);

		return content;
	}

	private static JScrollPane createItemListPanel(Collection<String> items, int maxDisplayLimit,
			String innerDialogTitle) {
		DefaultListModel<String> model = new DefaultListModel<>();
		int count = 0;
		int displayLimit = Math.min(items.size(), maxDisplayLimit);

		for (String item : items) {
			model.addElement(item);
			count++;
			if (count >= displayLimit) {
				break;
			}
		}

		if (items.size() > displayLimit) {
			String moreMsg = Configed.getResourceValue("DialogUtils.jListDialog.more");
			model.addElement(String.format(moreMsg, items.size() - displayLimit));
		}

		JList<String> itemList = new JList<>(model);
		itemList.setEnabled(false);
		itemList.setVisibleRowCount(10);

		JScrollPane scrollPane = new JScrollPane(itemList);
		scrollPane.setPreferredSize(new Dimension(400, 200));

		Border titledBorder = BorderFactory.createTitledBorder(innerDialogTitle);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(titledBorder, emptyBorder));

		return scrollPane;
	}
}
