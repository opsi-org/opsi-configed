/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ItemEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandCurl;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandTemplate;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PMInstallCurlPanel extends PMInstallPanel {
	private JLabel jLabelURL = new JLabel();
	private JTextField jTextFieldURL;

	private JLabel jLabelDir = new JLabel();
	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonAutoCompletion;

	private JCheckBox jCheckBoxIncludeZSync;

	private JCheckBox jCheckBoxCompareMD5;

	private CurlAuthenticationPanel curlAuthPanel;

	private String mainProduct;
	private String mainDir;

	public PMInstallCurlPanel() {
		super();

		initComponents();
		initLayout();
	}

	private void initComponents() {
		jLabelURL.setText(Configed.getResourceValue("PMInstallCurlPanel.jLabelCurlUrl"));
		jLabelURL.setFont(jLabelURL.getFont().deriveFont(Font.BOLD));

		jLabelDir.setText(Configed.getResourceValue("PMInstallCurlPanel.jLabelCurlDir"));
		jLabelDir.setFont(jLabelDir.getFont().deriveFont(Font.BOLD));

		CompletionComboButton autocompletion = new CompletionComboButton(additionalDefaultPaths);
		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.addItem(workbench);
		jComboBoxAutoCompletion.setSelectedItem(workbench);
		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion = autocompletion.getButton();
		jButtonAutoCompletion.setEnabled(true);

		jTextFieldURL = new JTextField();

		curlAuthPanel = new CurlAuthenticationPanel();
		curlAuthPanel.isOpen(true);
		curlAuthPanel.close();

		jCheckBoxIncludeZSync = new JCheckBox(Configed.getResourceValue("PMInstallCurlPanel.jLabelCurlIncludeZsync"),
				true);
		jCheckBoxIncludeZSync
				.setToolTipText(Configed.getResourceValue("PMInstallCurlPanel.jCheckBoxIncludeZsync.tooltip"));
		jCheckBoxIncludeZSync.addItemListener((ItemEvent itemEvent) -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				jCheckBoxCompareMD5.setSelected(true);
				jCheckBoxCompareMD5.setEnabled(true);
			} else {
				jCheckBoxCompareMD5.setSelected(false);
				jCheckBoxCompareMD5.setEnabled(false);
			}
		});

		jCheckBoxCompareMD5 = new JCheckBox(Configed.getResourceValue("PMInstallCurlPanel.jLabelCurlCompareMD5Sum"),
				true);
		jCheckBoxCompareMD5.setToolTipText(Configed.getResourceValue("PMInstallCurlPanel.jCheckBoxCompareMD5.tooltip"));
	}

	public MultiCommandTemplate getCommand(MultiCommandTemplate commands) {
		if (jTextFieldURL.getText().isBlank()) {
			return null;
		}

		SingleCommandCurl curl = getCurlCommand();
		if (curl != null) {
			if (curlAuthPanel.getCheckBox().isSelected()) {
				curl.setAuthentication("--insecure -u " + curlAuthPanel.getUser() + ":" + curlAuthPanel.getPassword());
			} else {
				curl.setAuthentication("");
			}

			commands.addCommand(curl);
			Logging.info(this, "doAction1 wget ", curl);
		}
		if (jCheckBoxCompareMD5.isSelected()) {
			String product = mainDir + "/" + getFilenameFromUrl(mainProduct);

			commands.addCommand(new SingleCommandTemplate("md5_vergleich",
					" if [ -z $((cat " + product + ".md5" + ") | " + "grep $(md5sum " + product
							+ "  | head -n1 | cut -d \" \" -f1)) ] ; " + " then echo \""
							+ Configed.getResourceValue("PMInstallCurlPanel.md5sumsAreNotEqual") + "\"; else echo \""
							+ Configed.getResourceValue("PMInstallCurlPanel.md5sumsAreEqual") + "\"; fi",
					""));
		}
		return commands;
	}

	public void addDialogToReactOn(Window associatedDialog) {
		// Add the listener and invoke it later so that it is executed after the panel 
		// is set visible
		curlAuthPanel.getCheckBox().addItemListener(itemEvent -> SwingUtilities.invokeLater(associatedDialog::pack));
	}

	private SingleCommandCurl getCurlCommand() {
		String d;
		String wgetDir = (String) jComboBoxAutoCompletion.getSelectedItem();

		String tempTextFieldDir = "<" + Configed.getResourceValue("CurlParameterDialog.jLabelDirectory") + ">";
		if (!wgetDir.isEmpty() || !wgetDir.equals(tempTextFieldDir)) {
			d = wgetDir;
		} else {
			return null;
		}

		String tempTextFieldURL = "<" + Configed.getResourceValue("PMInstallCurlPanel.jLabelCurlUrl").replace(":", "")
				+ ">";

		String u = "";

		if (!jTextFieldURL.getText().isEmpty() || !jTextFieldURL.getText().equals(tempTextFieldURL)) {
			u = jTextFieldURL.getText();
		} else {
			return null;
		}

		mainProduct = u;
		mainDir = d;

		String additionalProds = "";

		if (jCheckBoxIncludeZSync.isSelected() && additionalProds.contains(".opsi")) {
			additionalProds = " " + u.replace(".opsi", ".opsi.zsync");
			additionalProds = additionalProds + " " + u.replace(".opsi", ".opsi.md5");
		}

		return new SingleCommandCurl(d, u, additionalProds);
	}

	public String getProduct() {
		return (String) jComboBoxAutoCompletion.getSelectedItem() + getFilenameFromUrl(jTextFieldURL.getText());
	}

	private static String getFilenameFromUrl(String url) {
		return url.substring(url.lastIndexOf("/") + 1);
	}

	private void initLayout() {
		setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "[grow, fill][]",
				"[]0[]0[]0[]0[]"));
		add(jLabelURL);
		add(jTextFieldURL, "growx, gapbottom " + Globals.GAP_SIZE);
		add(jLabelDir);
		add(jComboBoxAutoCompletion, "split 2, growx");
		add(jButtonAutoCompletion, "gapbottom " + Globals.GAP_SIZE + ", wrap");
		add(jCheckBoxIncludeZSync);
		add(jCheckBoxCompareMD5, "gapbottom " + Globals.GAP_SIZE);
		add(curlAuthPanel.getCheckBox());
		add(curlAuthPanel, "growx, hidemode 3");
	}
}
