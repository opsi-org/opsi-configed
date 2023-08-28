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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CellAlternatingColorizer;

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

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
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
		initTextfield();
		if (comboboxDefaultPath != null) {
			textfield.setText(comboboxDefaultPath);
		}

		initCombobox();
		initButton();
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
			opsiRepo = OpsiserviceNOMPersistenceController.getConfigedWorkbenchDefaultValue();
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
				if (elem != null && !elem.trim().isEmpty()) {
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

	private void initTextfield() {
		textfield = new JTextField();
		if (!Main.THEMES) {
			textfield.setBackground(Globals.BACKGROUND_COLOR_9);
		}
	}

	private void createInstances() {
		button = new JButton();
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

	private void initButton() {
		button.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button"));
		button.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button.tooltip"));
		button.addActionListener(actionEvent -> doButtonAction());
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

				EmptyCommand getDirectories = new EmptyCommand(SSHCommandFactory.STRING_COMMAND_GET_DIRECTORIES
						.replace(SSHCommandFactory.STRING_REPLACEMENT_DIRECTORY, curdir)) {
					/** Sets the command specific error text **/
					@Override
					public String getErrorText() {
						return ROOT_DIRECTORY;
					}
				};

				SSHConnectExec ssh = new SSHConnectExec();
				String result = ssh.exec(getDirectories, false);
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

				EmptyCommand getFiles = new EmptyCommand(SSHCommandFactory.STRING_COMMAND_GET_DIRECTORIES
						.replace(SSHCommandFactory.STRING_REPLACEMENT_DIRECTORY, curdir));
				SSHConnectExec ssh = new SSHConnectExec();
				String result = ssh.exec(getFiles, false);
				if (result == null || result.isEmpty()) {
					result = ROOT_DIRECTORY;
				}

				getFiles = new EmptyCommand(SSHCommandFactory.STRING_COMMAND_GET_OPSI_FILES
						.replace(SSHCommandFactory.STRING_REPLACEMENT_DIRECTORY, curdir)) {
					/** Sets the command specific error text **/
					@Override
					public String getErrorText() {
						// no file found
						return ROOT_DIRECTORY;
					}
				};

				////// FUNKTIONIERT NUR WENN BERECHTIGUNGEN RICHTIG SIND.....
				// Bricht nach nächster Bedingung ab und schreibt keinen result ---> try-catch
				String tempResult = ssh.exec(getFiles, false);
				if (tempResult != null && !"null".equals(tempResult.trim())) {
					result += tempResult;
				}

				setItems(result, curdir);
				enableComponents(true);
			}
		}.start();
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
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Logging.debug(this, "getListCellRendererComponent called");

			// is null or not JComponent
			if (!(c instanceof JComponent)) {
				return c;
			}

			JComponent jc = (JComponent) c;

			if (jc instanceof JLabel) {
				String getText = ((JLabel) jc).getText();
				if (autocompletion == null || getText == null || getText.isEmpty()) {
					return c;
				}

				getText = getText.trim();
				String basicPath = autocompletion.getBasicPath();
				Logging.debug(this, "(1)  basicPath " + basicPath + " getText " + getText);

				// könnte eigtl raus. funktiniert sonst aber nicht...
				if (!basicPath.isEmpty() && !getText.isEmpty()) {
					if (basicPath.contains("//")) {
						basicPath = basicPath.replace("//", "/");
					}

					if (getText.contains("//")) {
						getText = getText.replace("//", "/");
					}

					if (getText.equals(basicPath) || autocompletion.containsInDefaults(getText)) {
						Logging.debug(this, "getListCellRendererComponent colorize(" + getText + ") = true");
						CellAlternatingColorizer.colorize(jc, isSelected, true, true);
					}

					if (getText.startsWith(basicPath) && !getText.equals(basicPath)
							&& !basicPath.equals(ROOT_DIRECTORY)) {
						((JLabel) jc).setText(getText.replace(basicPath, ""));
					}
					Logging.debug(this, "(2) basicPath " + basicPath + " getText " + getText);
				}
			}
			return jc;
		}
	}
}
