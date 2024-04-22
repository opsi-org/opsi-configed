/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommandCurl;
import de.uib.configed.serverconsole.command.SingleCommandTemplate;
import de.uib.utils.logging.Logging;

public class PMInstallCurlPanel extends PMInstallPanel {
	private JLabel jLabelURL = new JLabel();
	private JTextField jTextFieldURL;

	private JLabel jLabelDir = new JLabel();
	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonAutoCompletion;

	private JLabel jLabelIncludeZsync = new JLabel();
	private JCheckBox jCheckBoxIncludeZSync;
	private JLabel jLabelIncludeZSync2 = new JLabel();

	private JLabel jLabelCompareMD5Sum = new JLabel();
	private JCheckBox jCheckBoxCompareMD5;

	private CurlAuthenticationPanel curlAuthPanel;

	private String mainProduct;
	private String mainDir;
	private String urlDefText;

	public PMInstallCurlPanel() {
		super();
		urlDefText = Configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url");
		initComponents();
		initLayout();
	}

	private void initComponents() {
		jLabelDir.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetDir"));
		jLabelURL.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl"));
		jLabelIncludeZsync.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync"));
		jLabelIncludeZSync2.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync2"));
		jLabelCompareMD5Sum.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetCompareMD5Sum"));

		CompletionComboButton autocompletion = new CompletionComboButton(additionalDefaultPaths);
		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.addItem(workbench);
		jComboBoxAutoCompletion.setSelectedItem(workbench);
		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion = autocompletion.getButton();
		jButtonAutoCompletion.setEnabled(true);

		jTextFieldURL = new JTextField(urlDefText);

		jTextFieldURL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (jTextFieldURL.getText().equals(urlDefText)) {
					jTextFieldURL.setSelectionStart(0);
					jTextFieldURL.setSelectionEnd(jTextFieldURL.getText().length());
				}
			}
		});

		curlAuthPanel = new CurlAuthenticationPanel();
		curlAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH * 2, Globals.BUTTON_HEIGHT);
		curlAuthPanel.isOpen(true);
		curlAuthPanel.close();

		jCheckBoxIncludeZSync = new JCheckBox();
		jCheckBoxIncludeZSync.setSelected(true);
		jCheckBoxIncludeZSync.setToolTipText(Configed.getResourceValue(
				"SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxIncludeZsync.tooltip"));
		jCheckBoxIncludeZSync.addItemListener((ItemEvent itemEvent) -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				jCheckBoxCompareMD5.setSelected(true);
				jCheckBoxCompareMD5.setEnabled(true);
			} else {
				jCheckBoxCompareMD5.setSelected(false);
				jCheckBoxCompareMD5.setEnabled(false);
			}
		});

		jCheckBoxCompareMD5 = new JCheckBox();
		jCheckBoxCompareMD5.setSelected(true);
		jCheckBoxCompareMD5.setToolTipText(Configed.getResourceValue(
				"SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxCompareMD5.tooltip"));
	}

	public MultiCommandTemplate getCommand(MultiCommandTemplate commands) {
		if (jTextFieldURL.getText() == null || jTextFieldURL.getText().isBlank()
				|| jTextFieldURL.getText().trim().equals(urlDefText)) {
			return null;
		}

		SingleCommandCurl curl = getCurlCommand();
		if (curl != null) {
			if (((JCheckBox) curlAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
				curl.setAuthentication("--insecure -u " + curlAuthPanel.getUser() + ":" + curlAuthPanel.getPw());
			} else {
				curl.setAuthentication("");
			}

			commands.addCommand(curl);
			Logging.info(this, "doAction1 wget " + curl);
		}
		if (jCheckBoxCompareMD5.isSelected()) {
			String product = mainDir + "/" + getFilenameFromUrl(mainProduct);

			commands.addCommand(new SingleCommandTemplate("md5_vergleich", " if [ -z $((cat " + product + ".md5"
					+ ") | " + "grep $(md5sum " + product + "  | head -n1 | cut -d \" \" -f1)) ] ; " + " then echo \""
					+ Configed.getResourceValue(
							"SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreNotEqual")
					+ "\"; else echo \""
					+ Configed.getResourceValue(
							"SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreEqual")
					+ "\"; fi", ""));
		}
		return commands;
	}

	private SingleCommandCurl getCurlCommand() {
		String d;
		String wgetDir = (String) jComboBoxAutoCompletion.getSelectedItem();

		String tempTextFieldDir = "<" + Configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelDirectory")
				+ ">";
		if (!wgetDir.isEmpty() || !wgetDir.equals(tempTextFieldDir)) {
			d = wgetDir;
		} else {
			return null;
		}

		String tempTextFieldURL = "<"
				+ Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl")
						.replace(":", "")
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
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jTextFieldURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jButtonAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelIncludeZsync, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxIncludeZSync, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jLabelIncludeZSync2, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelCompareMD5Sum, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxCompareMD5, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(curlAuthPanel.get(CurlAuthenticationPanel.LBLNEEDAUTH),
										Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
								.addComponent(curlAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH),
										Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(curlAuthPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(2 * Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
				.addGap(2 * Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelIncludeZsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelCompareMD5Sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(curlAuthPanel.get(CurlAuthenticationPanel.LBLNEEDAUTH),
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addComponent(jTextFieldURL, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)
								.addComponent(jButtonAutoCompletion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(jCheckBoxIncludeZSync, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE).addComponent(jLabelIncludeZSync2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup().addComponent(jCheckBoxCompareMD5,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup().addComponent(
								curlAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH), GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(Globals.GAP_SIZE))
				.addGroup(layout.createSequentialGroup().addComponent(curlAuthPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));
	}
}
