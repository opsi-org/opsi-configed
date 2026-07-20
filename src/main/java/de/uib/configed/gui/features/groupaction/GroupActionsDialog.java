/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.groupaction;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class GroupActionsDialog {
	private JLabel groupNameLabel;
	private JLabel clientsCountLabel;

	private JComboBox<String> comboSelectImage;

	private Collection<String> associatedClients;

	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;

	public GroupActionsDialog(ConfigedMain configedMain) {
		super();

		this.configedMain = configedMain;

		JButton buttonSetup = new JButton(Configed.getResourceValue("save"));
		buttonSetup.setToolTipText(Configed.getResourceValue("FGroupAction.buttonSetup.tooltip"));
		buttonSetup.addActionListener(actionEvent -> replay());

		JButton buttonReload = new JButton(Configed.getResourceValue("reload"));
		buttonReload.addActionListener(actionEvent -> reload());

		JPanel panel = defineImageActionPanel();
		SwingUtils.addKeyBindingToJComponent(panel, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), this::reload);

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] { buttonSetup, buttonReload, Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(), Configed.getResourceValue("FGroupAction.title"));
		dialog.setModal(false);
	}

	private void setGroupLabelling(String label, String clientCount) {
		groupNameLabel.setText(Configed.getResourceValue("FGroupAction.groupname") + ": " + label);
		clientsCountLabel.setText(Configed.getResourceValue("FGroupAction.clientcounter") + ": " + clientCount);
	}

	public void show() {
		reload();

		dialog.pack();
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	private void setImages() {
		Set<String> imagesCollection = new TreeSet<>(
				persistenceController.getDataServices().product.getCommonProductPropertyValues(associatedClients,
						OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY,
						OpsiServiceNOMPersistenceController.LOCAL_IMAGE_LIST_PROPERTY_KEY));

		comboSelectImage.setModel(new DefaultComboBoxModel<>(imagesCollection.toArray(new String[0])));
	}

	private void reload() {
		String groupName = configedMain.getSelectedGroupName();
		if (groupName == null || groupName.isEmpty()) {
			Logging.info(this, "No group selected for group action");
			groupName = Configed.getResourceValue("FGroupAction.noGroupSelected");
		}

		// setGroupLabelling(groupName, "" + configedMain.getClientTablePanel().getClientTable().getTable().model.getRowCount());

		// associatedClients = configedMain.getClientTablePanel().getClientTable().getClients();
		setImages();
	}

	private void replay() {
		Logging.debug(this, "replay ", comboSelectImage.getSelectedItem());

		if (comboSelectImage.getSelectedItem() == null) {
			return;
		}

		String image = (String) comboSelectImage.getSelectedItem();

		List<String> values = new ArrayList<>();

		// selected from common product property values
		values.add(image);

		dialog.setCursor(Globals.WAIT_CURSOR);

		persistenceController.getDataServices().product.setCommonProductPropertyValue(associatedClients,
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY,
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_TO_RESTORE_PROPERTY_KEY, values);

		Map<String, String> changedValues = new HashMap<>();
		changedValues.put(ProductState.KEY_ACTION_REQUEST, "setup");

		persistenceController.getDataServices().product.updateProductOnClients(associatedClients,
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY, OpsiPackage.TYPE_NETBOOT,
				changedValues);

		dialog.setCursor(null);
	}

	private JPanel defineImageActionPanel() {
		JPanel panel = new JPanel();

		// Values will be set in reload method
		groupNameLabel = new JLabel();
		clientsCountLabel = new JLabel();

		JLabel labelCombo = SwingUtils.createBoldLabel("FGroupAction.existingImages");

		comboSelectImage = new JComboBox<>();

		panel.setLayout(new MigLayout("insets 0, fillx", "", "[]0"));

		panel.add(groupNameLabel, "split 2");
		panel.add(clientsCountLabel, "wrap, gapleft " + Globals.GAP_SIZE);

		panel.add(labelCombo, "gapy " + Globals.GAP_SIZE + ", wrap");
		panel.add(comboSelectImage, "growx");

		return panel;
	}
}
