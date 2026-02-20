/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandCurl;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandHelp;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class CurlParameterDialog {
	private JPanel inputPanel = new JPanel();

	private JLabel jLabelURL;
	private JLabel jLabelDir;
	private JLabel jLabelLoglevel;
	private JLabel jLabelFreeInput;

	private JButton jButtonSearchDir;

	private JTextField jTextFieldURL;
	private JComboBox<String> jComboBoxDir;
	private JComboBox<Integer> jComboBoxLoglevel;
	private JTextField jTextFieldFreeInput;

	private SingleCommandCurl commandCurl = new SingleCommandCurl();
	private CompletionComboButton completion;
	private CurlAuthenticationPanel curlAuthPanel;

	private ConfigedMain configedMain;

	private JDialog dialog;

	public CurlParameterDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;

		// The dialog needs to change its size when the completion list is updated
		this.completion = new CompletionComboButton() {
			@Override
			public void setItems(Set<String> items, final String curdir) {
				super.setItems(items, curdir);
				dialog.pack();
			}
		};

		init();
		initLayout();

		jComboBoxDir.setEnabled(true);

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());

		JButton buttonParameterInfo = new JButton(Configed.getResourceValue("CurlParameterDialog.buttonParameterInfo"));
		buttonParameterInfo.addActionListener(actionEvent -> showParameterInfo());

		JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null, new Object[] { buttonExecute, buttonParameterInfo, Configed.getResourceValue("buttonCancel") });
		DialogUtils.enableDialogResizing(optionPane);
		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("CurlParameterDialog.title"));
		dialog.setModal(false);
		dialog.pack();

		dialog.setVisible(true);

		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	private void init() {
		jLabelURL = SwingUtils.createBoldLabel("CurlParameterDialog.jLabelUrl");

		jTextFieldURL = new JTextField();

		jLabelDir = SwingUtils.createBoldLabel("CurlParameterDialog.jLabelDirectory");

		jComboBoxDir = completion.getCombobox();
		jButtonSearchDir = completion.getButton();

		jLabelLoglevel = SwingUtils.createBoldLabel("loglevel");

		jComboBoxLoglevel = new JComboBox<>();
		for (int i = 3; i <= 9; i++) {
			jComboBoxLoglevel.addItem(i);
		}

		jComboBoxLoglevel.setSelectedItem(4);
		jComboBoxLoglevel
				.addItemListener(itemEvent -> commandCurl.setLoglevel((int) jComboBoxLoglevel.getSelectedItem()));

		jLabelFreeInput = SwingUtils.createBoldLabel("CurlParameterDialog.jLabelFreeInput");

		jTextFieldFreeInput = new JTextField();
		jTextFieldFreeInput.setToolTipText(Configed.getResourceValue("CurlParameterDialog.jLabelFreeInput.tooltip"));
		jTextFieldFreeInput.getDocument().addDocumentListener(SwingUtils.onDocumentChange(this::changeFreeInput));

		curlAuthPanel = new CurlAuthenticationPanel();
		curlAuthPanel.getCheckBox().setSelected(false);

		// Add the listener and invoke it later so that it is executed after the panel 
		// is set visible
		curlAuthPanel.getCheckBox().addItemListener(event -> SwingUtilities.invokeLater(() -> dialog.pack()));
		curlAuthPanel.isOpen(true);
		curlAuthPanel.close();

		changeFreeInput();
	}

	private void changeFreeInput() {
		if (!jTextFieldFreeInput.getText().isBlank()) {
			commandCurl.setFreeInput(jTextFieldFreeInput.getText().trim());
		} else {
			commandCurl.setFreeInput("");
		}
	}

	private void execute() {
		Logging.warning(this, "execute");
		if (jTextFieldURL.getText().isBlank()) {
			Logging.warning(this, "Please enter url.");
			return;
		}

		commandCurl.setDir((String) jComboBoxDir.getSelectedItem());
		if (curlAuthPanel.getCheckBox().isSelected()) {
			commandCurl
					.setAuthentication("--insecure -u " + curlAuthPanel.getUser() + ":" + curlAuthPanel.getPassword());
		} else {
			commandCurl.setAuthentication("");
		}

		if (commandCurl.checkCommand()) {
			Logging.info(this, "doAction3 wget ");
			CommandExecutor executor = new CommandExecutor(configedMain, commandCurl);
			executor.executeAsync();
		}
	}

	private void showParameterInfo() {
		CommandExecutor executor = new CommandExecutor(configedMain, new SingleCommandHelp(commandCurl));
		executor.executeAsync();
	}

	private void initLayout() {
		inputPanel.setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));
		inputPanel.add(jLabelURL);
		inputPanel.add(jTextFieldURL, "growx, gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(jLabelDir);
		inputPanel.add(jComboBoxDir, "split 2, growx, gapright " + Globals.GAP_SIZE);
		inputPanel.add(jButtonSearchDir, "wrap, gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(jLabelLoglevel);
		inputPanel.add(jComboBoxLoglevel, "gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(curlAuthPanel.getCheckBox(), "gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(curlAuthPanel, "growx, hidemode 3");
		inputPanel.add(jLabelFreeInput);
		inputPanel.add(jTextFieldFreeInput, "growx");
	}
}
