/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandCurl;
import de.uib.configed.serverconsole.command.SingleCommandHelp;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class CurlParameterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel jLabelURL = new JLabel();
	private JLabel jLabelDir = new JLabel();
	private JLabel jLabelVerbosity = new JLabel();
	private JLabel jLabelFreeInput = new JLabel();

	private JButton jButtonHelp;
	private JButton jButtonExecute;
	private JButton jButtonSearchDir;

	private JTextField jTextFieldURL;
	private JTextField jTextFieldDir;
	private JComboBox<String> jComboBoxDir;
	private JComboBox<Integer> jComboBoxVerbosity;
	private JTextField jTextFieldFreeInput;

	private SingleCommandCurl commandCurl = new SingleCommandCurl();
	private CompletionComboButton completion;
	private CurlAuthenticationPanel curlAuthPanel;

	private ConfigedMain configedMain;

	public CurlParameterDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("CurlParameterDialog.title"), false);
		this.configedMain = configedMain;
		this.completion = new CompletionComboButton();

		init();
		initLayout();
		super.setSize(Globals.DIALOG_FRAME_DEFAULT_WIDTH, 320);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setVisible(true);
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			setComponentsEnabledRO(false);
		}

		jComboBoxDir.setEnabled(true);
	}

	private void setComponentsEnabledRO(boolean value) {
		jTextFieldURL.setEnabled(value);
		jTextFieldURL.setEditable(value);
		jTextFieldDir.setEnabled(value);
		jTextFieldDir.setEditable(value);
		jComboBoxDir.setEnabled(value);
		jComboBoxDir.setEditable(value);
		jComboBoxVerbosity.setEnabled(value);

		jTextFieldFreeInput.setEnabled(value);
		jTextFieldFreeInput.setEditable(value);

		jButtonExecute.setEnabled(value);
		jButtonHelp.setEnabled(value);
	}

	private void init() {
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setPreferredSize(new Dimension(376, 220));

		jLabelURL.setText(Configed.getResourceValue("CurlParameterDialog.jLabelUrl"));
		jTextFieldURL = new JTextField();
		jTextFieldURL.setText(Configed.getResourceValue("CurlParameterDialog.downloadLink"));
		jTextFieldURL.getDocument().addDocumentListener(new DocumentListenerAdapter(this::changeUrl));
		jTextFieldURL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (jTextFieldURL.getText().equals(Configed.getResourceValue("CurlParameterDialog.downloadLink"))) {
					jTextFieldURL.setSelectionStart(0);
					jTextFieldURL.setSelectionEnd(jTextFieldURL.getText().length());
				}
			}
		});

		jLabelDir.setText(Configed.getResourceValue("CurlParameterDialog.jLabelDirectory"));
		jTextFieldDir = new JTextField();

		jComboBoxDir = completion.getCombobox();
		jButtonSearchDir = completion.getButton();

		jLabelVerbosity.setText(Configed.getResourceValue("verbosity"));
		jComboBoxVerbosity = new JComboBox<>();
		jComboBoxVerbosity.setToolTipText(Configed.getResourceValue("verbosity.tooltip"));
		for (int i = 0; i < 5; i++) {
			jComboBoxVerbosity.addItem(i);
		}

		jComboBoxVerbosity.setSelectedItem(1);
		jComboBoxVerbosity.addItemListener(
				(ItemEvent itemEvent) -> commandCurl.setVerbosity((int) jComboBoxVerbosity.getSelectedItem()));

		jLabelFreeInput.setText(Configed.getResourceValue("CurlParameterDialog.jLabelFreeInput"));
		jTextFieldFreeInput = new JTextField();
		jTextFieldFreeInput.setToolTipText(Configed.getResourceValue("CurlParameterDialog.jLabelFreeInput.tooltip"));
		jTextFieldFreeInput.getDocument().addDocumentListener(new DocumentListenerAdapter(this::changeFreeInput));

		curlAuthPanel = new CurlAuthenticationPanel();
		((JCheckBox) curlAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH)).setSelected(false);
		curlAuthPanel.isOpen(true);
		curlAuthPanel.close();
		curlAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 67, Globals.BUTTON_HEIGHT);

		jButtonHelp = new JButton(Configed.getResourceValue("CurlParameterDialog.buttonParameterInfo"));
		jButtonHelp.setToolTipText(Configed.getResourceValue("CurlParameterDialog.buttonParameterInfo.tooltip"));
		jButtonHelp.addActionListener(actionEvent -> doActionHelp());

		jButtonExecute = new JButton(Configed.getResourceValue("buttonExecute"));

		jButtonExecute.addActionListener((ActionEvent actionEvent) -> {
			if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
					.isGlobalReadOnly()) {
				doAction3();
			}
		});

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));

		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonHelp);
		buttonPanel.add(jButtonExecute);

		changeUrl();
		changeFreeInput();
	}

	private void changeFreeInput() {
		if (!jTextFieldFreeInput.getText().isBlank()) {
			commandCurl.setFreeInput(jTextFieldFreeInput.getText().trim());
		} else {
			commandCurl.setFreeInput("");
		}
	}

	private void changeUrl() {
		if (!(jTextFieldURL.getText().isEmpty())) {
			commandCurl.setUrl(jTextFieldURL.getText().trim());
		} else {
			commandCurl.setUrl("");
		}
	}

	@Override
	public void doAction3() {
		Logging.warning(this, "execute");
		if (jTextFieldURL.getText().equals(Configed.getResourceValue("CurlParameterDialog.downloadLink"))
				|| jTextFieldURL.getText().isEmpty()) {
			Logging.warning(this, "Please enter url.");
			return;
		}

		commandCurl.setDir((String) jComboBoxDir.getSelectedItem());
		if (((JCheckBox) curlAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
			commandCurl
					.setAuthentication("--insecure -u " + curlAuthPanel.getUser() + ":" + curlAuthPanel.getPassword());
		} else {
			commandCurl.setAuthentication("");
		}

		if (commandCurl.checkCommand()) {
			new Thread() {
				@Override
				public void run() {
					Logging.info(this, "doAction3 wget ");
					CommandExecutor executor = new CommandExecutor(configedMain, commandCurl);
					executor.execute();
				}
			}.start();
		}
	}

	private void doActionHelp() {
		CommandExecutor executor = new CommandExecutor(configedMain, new SingleCommandHelp(commandCurl));
		executor.execute();
	}

	private void cancel() {
		super.doAction1();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addGroup(inputPanelLayout.createSequentialGroup()
								.addGroup(inputPanelLayout.createParallelGroup()
										.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelVerbosity, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(
												curlAuthPanel.get(CurlAuthenticationPanel.LBLNEEDAUTH),
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelFreeInput, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(inputPanelLayout.createParallelGroup()
										.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jTextFieldURL,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE))
										.addGroup(inputPanelLayout.createSequentialGroup()
												.addComponent(jComboBoxDir, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
														Short.MAX_VALUE)
												.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
										.addComponent(jComboBoxVerbosity, GroupLayout.Alignment.LEADING,
												Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
										.addComponent(curlAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH),
												GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jTextFieldFreeInput, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)))
						.addComponent(curlAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jTextFieldURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jLabelURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonSearchDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jLabelDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxVerbosity, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jLabelVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(curlAuthPanel.get(CurlAuthenticationPanel.LBLNEEDAUTH), Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(curlAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH),
								GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addComponent(curlAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jTextFieldFreeInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jLabelFreeInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE).addContainerGap(70, 70));
	}

	private static class DocumentListenerAdapter implements DocumentListener {
		private Runnable method;

		public DocumentListenerAdapter(Runnable method) {
			this.method = method;
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			method.run();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			method.run();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			method.run();
		}
	}
}
