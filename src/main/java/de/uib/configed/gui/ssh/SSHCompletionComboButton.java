/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

// Verwendung in Beisspielsweise SSHOpsiSetRightsDialog.java
public class SSHCompletionComboButton {
	private static final String ROOT_DIRECTORY = "/";
	private static final String HOME_DIRECTORY = "~";

	private JComboBox<String> combobox;
	private JButton button;
	private JTextField textfield;
	private List<String> defaultvalues;
	private String searchSpecificFiles;
	private String comboboxDefaultPath;

	// will be overwritten with config
	private String opsiRepo = "/";

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SSHCompletionComboButton() {
		this(null, null, null);
	}

	public SSHCompletionComboButton(List<String> values) {
		this(values, null, null);
	}

	public SSHCompletionComboButton(List<String> values, String searchSpecificFiles, String comboboxDefaultPath) {
		Logging.info(this.getClass(), "instance created");
		this.searchSpecificFiles = searchSpecificFiles;
		this.comboboxDefaultPath = comboboxDefaultPath;
		init(values);
		createInstances();

		textfield = new JTextField();

		if (comboboxDefaultPath != null) {
			textfield.setText(comboboxDefaultPath);
		}

		initCombobox();
	}

	private final void enableComponents(boolean value) {
		combobox.setEnabled(value);
		button.setEnabled(value);
	}

	private String getBasicPath() {
		String basicSearchPath = (String) combobox.getSelectedItem();
		if (basicSearchPath != null) {
			return basicSearchPath.trim();
		}

		return "";
	}

	private void init(List<String> defvalues) {
		if (persistenceController == null) {
			Logging.info(this, "init PersistenceController null");
		} else {
			opsiRepo = persistenceController.getConfigDataService().getConfigedWorkbenchDefaultValuePD();
		}

		if (opsiRepo.charAt(opsiRepo.length() - 1) != '/') {
			opsiRepo = opsiRepo + "/";
		}

		defaultvalues = new ArrayList<>();

		if (comboboxDefaultPath != null) {
			defaultvalues.add(comboboxDefaultPath);
			defaultvalues.add(ROOT_DIRECTORY);
			defaultvalues.add(opsiRepo);
		} else {
			defaultvalues.add(opsiRepo);
			defaultvalues.add(ROOT_DIRECTORY);
		}
		// Is element in defaultValues?
		if (defvalues != null) {
			for (String elem : defvalues) {
				if (elem != null && !elem.isBlank()) {
					defaultvalues.add(elem);
				}
			}
		}

		Logging.info(this, "init");
		for (String elem : defaultvalues) {
			Logging.debug(this, "init defaultvalues contains " + elem);
		}
	}

	public List<String> getDefaultValues() {
		return defaultvalues;
	}

	private void createInstances() {
		button = new JButton(Configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button"));
		button.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button.tooltip"));
		button.addActionListener(actionEvent -> doButtonAction());

		combobox = new SSHCompletionComboBox<>(new DefaultComboBoxModel<>(defaultvalues.toArray(new String[0])));
	}

	public void initCombobox() {
		combobox.setEnabled(false);
		combobox.setRenderer(new ItemElementListener(this));
		combobox.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.cb_serverDir.tooltip"));
		combobox.setEditable(true);

		if (comboboxDefaultPath != null) {
			combobox.setSelectedItem(comboboxDefaultPath);
		}

		if (searchSpecificFiles != null && !searchSpecificFiles.isEmpty()) {
			combobox.addActionListener((ActionEvent actionEvent) -> {
				if (combobox.getSelectedItem() != null
						&& ((String) combobox.getSelectedItem()).endsWith(searchSpecificFiles)) {
					textfield.setText((String) combobox.getSelectedItem());
				} else {
					textfield.setText("");
				}

				combobox.setSelectedItem(combobox.getSelectedItem());
			});
		}

		combobox.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);
	}

	public void doButtonAction() {
		enableComponents(false);

		String strcbtext = combobox.getEditor().getItem().toString();
		if (strcbtext != null && !strcbtext.isEmpty()
				&& !strcbtext.substring(strcbtext.length() - 1).equals(ROOT_DIRECTORY)) {
			combobox.removeItem(strcbtext);
			Logging.info(this, "doButtonAction combo.removeItem(" + strcbtext + ")");
			strcbtext = strcbtext + ROOT_DIRECTORY;
			combobox.addItem(strcbtext);
			Logging.info(this, "doButtonAction combo.additem(" + strcbtext + ")");
			combobox.setSelectedItem(strcbtext);
		}

		if (searchSpecificFiles != null && !searchSpecificFiles.isEmpty()) {
			getDirectoriesAndFilesIn(strcbtext);
		} else {
			getDirectoriesIn(strcbtext);
		}

		setComboDefault(null);
	}

	private void setComboDefault(String value) {
		comboboxDefaultPath = value;
	}

	public JTextField getTextField() {
		return textfield;
	}

	public JButton getButton() {
		return button;
	}

	public JComboBox<String> getCombobox() {
		return combobox;
	}

	public void setCombobox(JComboBox<String> cb) {
		combobox = cb;
	}

	public String comboBoxGetStringItem() {
		if (combobox.getEditor().getItem().toString().startsWith("/")) {
			return combobox.getEditor().getItem().toString();
		} else {
			return getBasicPath() + combobox.getEditor().getItem().toString();
		}
	}

	private void getDirectoriesIn(final String curdir) {
		new Thread() {
			@Override
			public void run() {
				String result = getDirectories(curdir);
				if (result == null || result.isEmpty()) {
					result = HOME_DIRECTORY;
				}

				setItems(result, curdir);
				enableComponents(true);
			}
		}.start();
	}

	private void getDirectoriesAndFilesIn(final String curdir) {
		new Thread() {
			@Override
			public void run() {
				String result = getDirectories(curdir);
				if (result == null || result.isEmpty()) {
					result = ROOT_DIRECTORY;
				}
				String tempResult = getFiles(curdir);
				if (tempResult != null && !"null".equals(tempResult.trim())) {
					result += tempResult;
				}

				setItems(result, curdir);
				enableComponents(true);
			}
		}.start();
	}

	private static String getDirectories(String curdir) {
		EmptyCommand getDirectoriesCommand = new EmptyCommand(SSHCommandFactory.STRING_COMMAND_GET_DIRECTORIES
				.replace(SSHCommandFactory.STRING_REPLACEMENT_DIRECTORY, curdir)) {
			@Override
			public String getErrorText() {
				return ROOT_DIRECTORY;
			}
		};
		SSHConnectExec ssh = new SSHConnectExec();
		return ssh.exec(getDirectoriesCommand, false);
	}

	private static String getFiles(String curdir) {
		EmptyCommand getFilesCommand = new EmptyCommand(SSHCommandFactory.STRING_COMMAND_GET_OPSI_FILES
				.replace(SSHCommandFactory.STRING_REPLACEMENT_DIRECTORY, curdir)) {
			@Override
			public String getErrorText() {
				return ROOT_DIRECTORY;
			}
		};

		////// FUNKTIONIERT NUR WENN BERECHTIGUNGEN RICHTIG SIND.....
		// Bricht nach nächster Bedingung ab und schreibt keinen result ---> try-catch
		SSHConnectExec ssh = new SSHConnectExec();
		return ssh.exec(getFilesCommand, false);
	}

	private boolean containsInDefaults(String other) {
		boolean contains = defaultvalues.contains(other);
		Logging.debug(this, "contains_in_defaults defaultvalues.contains_in_defaults(" + other + ") = " + contains);
		return contains;
	}

	private final void setItems(String result, final String curdir) {
		if (result == null) {
			Logging.warning("getDirectoriesIn could not find directories in " + curdir);
		} else {
			combobox.removeAllItems();
			for (String element : defaultvalues) {
				combobox.addItem(element);
				Logging.debug(this, "setItems add " + element);
			}
			String curDirLocated = curdir;
			if (!containsInDefaults(curDirLocated)) {
				combobox.addItem(curDirLocated);
			}

			Logging.debug(this, "setItems add " + curDirLocated);
			for (String item : result.split("\n")) {
				Logging.debug(this, "setItems add " + item);
				if (item.contains("//")) {
					combobox.addItem(item.replace("//", "/"));
				} else {
					combobox.addItem(item);
				}
			}
			combobox.setSelectedItem(curdir);
		}
		if (comboboxDefaultPath != null && !comboboxDefaultPath.isEmpty()) {
			combobox.setSelectedItem(comboboxDefaultPath);
			setComboDefault(null);
		}
	}

	private class ItemElementListener extends DefaultListCellRenderer {
		private SSHCompletionComboButton autocompletion;

		public ItemElementListener(SSHCompletionComboButton autocompletion) {
			super();
			this.autocompletion = autocompletion;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (autocompletion == null || getText() == null || getText().isEmpty()) {
				return this;
			}

			String text = getText().trim();
			String basicPath = autocompletion.getBasicPath();

			// könnte eigtl raus. funktiniert sonst aber nicht...
			if (!basicPath.isEmpty() && !text.isEmpty()) {
				basicPath = basicPath.replace("//", "/");
				text = text.replace("//", "/");

				if (text.startsWith(basicPath) && !text.equals(basicPath) && !basicPath.equals(ROOT_DIRECTORY)) {
					setText(text.replace(basicPath, ""));
				}
			}
			return this;
		}
	}
}
