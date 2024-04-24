/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandFactory;
import de.uib.utils.WebDAVClient;
import de.uib.utils.logging.Logging;

public class CompletionComboButton {
	private JComboBox<String> combobox;
	private JButton button;
	private JTextField textfield;
	private Set<String> defaultvalues;
	private String fileExtension;
	private String comboboxDefaultPath;

	public CompletionComboButton() {
		this(null, null, null);
	}

	public CompletionComboButton(List<String> values) {
		this(values, null, null);
	}

	public CompletionComboButton(List<String> values, String searchSpecificFiles, String comboboxDefaultPath) {
		Logging.info(this.getClass(), "instance created");
		this.fileExtension = searchSpecificFiles;
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
		String opsiRepo = CommandFactory.WEBDAV_OPSI_PATH_VAR_WORKBENCH;
		defaultvalues = new HashSet<>();

		if (comboboxDefaultPath != null) {
			defaultvalues.add(comboboxDefaultPath);
			defaultvalues.add(opsiRepo);
		} else {
			defaultvalues.add(opsiRepo);
		}
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

	public Set<String> getDefaultValues() {
		return defaultvalues;
	}

	private void createInstances() {
		button = new JButton(Configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button"));
		button.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button.tooltip"));
		button.addActionListener(actionEvent -> doButtonAction());
		combobox = new CompletionComboBox<>(new DefaultComboBoxModel<>(defaultvalues.toArray(new String[0])));
	}

	public final void initCombobox() {
		combobox.setEnabled(false);
		combobox.setRenderer(new ItemElementListener(this));
		combobox.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.cb_serverDir.tooltip"));
		combobox.setEditable(true);

		if (comboboxDefaultPath != null) {
			combobox.setSelectedItem(comboboxDefaultPath);
		}

		if (fileExtension != null && !fileExtension.isEmpty()) {
			combobox.addActionListener((ActionEvent actionEvent) -> {
				if (combobox.getSelectedItem() != null
						&& ((String) combobox.getSelectedItem()).endsWith(fileExtension)) {
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
		if (strcbtext != null && !strcbtext.isEmpty() && !"/".equals(strcbtext.substring(strcbtext.length() - 1))) {
			combobox.removeItem(strcbtext);
			Logging.info(this, "doButtonAction combo.removeItem(" + strcbtext + ")");
			strcbtext = strcbtext + "/";
			combobox.addItem(strcbtext);
			Logging.info(this, "doButtonAction combo.additem(" + strcbtext + ")");
			combobox.setSelectedItem(strcbtext);
		}

		if (fileExtension != null && !fileExtension.isEmpty()) {
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
		return combobox.getEditor().getItem().toString();
	}

	private void getDirectoriesIn(final String curdir) {
		new Thread() {
			@Override
			public void run() {
				WebDAVClient webDAVClient = new WebDAVClient();
				Set<String> result = webDAVClient.getDirectoriesIn(curdir);
				setItems(result, curdir);
				enableComponents(true);
			}
		}.start();
	}

	private void getDirectoriesAndFilesIn(final String curdir) {
		new Thread() {
			@Override
			public void run() {
				WebDAVClient webDAVClient = new WebDAVClient();
				Set<String> result = webDAVClient.getDirectoriesAndFilesIn(curdir, fileExtension);
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

	private final void setItems(Set<String> items, final String curdir) {
		if (items == null) {
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
			for (String item : items) {
				Logging.debug(this, "setItems add " + item);
				int itemIndex = ((DefaultComboBoxModel<String>) combobox.getModel()).getIndexOf(item);
				if (itemIndex != -1) {
					continue;
				}
				combobox.addItem(item.replace("//", "/"));
			}
			combobox.setSelectedItem(curdir);
		}
		if (comboboxDefaultPath != null && !comboboxDefaultPath.isEmpty()) {
			combobox.setSelectedItem(comboboxDefaultPath);
			setComboDefault(null);
		}
	}

	private class ItemElementListener extends DefaultListCellRenderer {
		private CompletionComboButton autocompletion;

		public ItemElementListener(CompletionComboButton autocompletion) {
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

			if (!basicPath.isEmpty() && !text.isEmpty()) {
				basicPath = basicPath.replace("//", "/");
				text = text.replace("//", "/");

				if (text.startsWith(basicPath) && !text.equals(basicPath) && !"/".equals(basicPath)) {
					setText(text.replace(basicPath, ""));
				}
			}
			return this;
		}
	}
}
