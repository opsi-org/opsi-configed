/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.messageoftheday;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uib.configed.core.domain.permission.UserFeaturesConfig;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

/**
 * Represents the overall dialog for the "message of the day" (motd)
 * configuration. This dialog contains two panels, one for the general and one
 * for the user motd config. This dialog is used in the {@link MainFrame} class
 * to show the motd configuration in the top menu "window".
 */
public class MessageOfTheDayDialog {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private PanelMessageInfos pMsgInfoGeneral;
	private PanelMessageInfos pMsgInfoUser;
	private JButton resetButton = new JButton(Configed.getResourceValue("MessageOfTheDay.resetButton"));
	private Map<String, String> motdData = new HashMap<>();
	private boolean forbiddenDevice;
	private boolean forbiddenUser;
	private JDialog dialog;
	private JScrollPane scrollpane = new JScrollPane();

	public MessageOfTheDayDialog() {
		List<Object> forbiddenItemsMOTD = persistenceController.getDataServices().userRoles.getForbiddenMOTD();
		forbiddenDevice = forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_DEVICE);
		forbiddenUser = forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_USER);
		if (forbiddenDevice && forbiddenUser) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		motdData = persistenceController.getDataServices().config.getMessageOfTheDayConfigs();
		define();
		init();

		JButton buttonSave = new JButton(Configed.getResourceValue("save"));

		JOptionPane optionPane = new JOptionPane(scrollpane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, new Object[] { buttonSave, Configed.getResourceValue("buttonCancel") });
		DialogUtils.enableDialogResizing(optionPane);
		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MessageOfTheDay.title"));
		dialog.setModal(false);

		buttonSave.addActionListener((ActionEvent event) -> {
			saveData();
			dialog.setVisible(false);
		});

		dialog.setVisible(true);
	}

	private void init() {
		Logging.debug("FMessageOfTheDay resetData from init(both)");
		if (!forbiddenDevice) {
			Logging.debug("FMessageOfTheDay start data reset for device",
					motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL));
			pMsgInfoGeneral.resetData();
		}
		if (!forbiddenUser) {
			Logging.debug("FMessageOfTheDay start data reset for user",
					motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL));
			pMsgInfoUser.resetData();
		}
	}

	private void define() {
		if (!forbiddenDevice) {
			pMsgInfoGeneral = new PanelMessageInfos(PanelMessageInfos.InfoType.DEVICE, motdData, forbiddenDevice);
		}
		if (!forbiddenUser) {
			pMsgInfoUser = new PanelMessageInfos(PanelMessageInfos.InfoType.USER, motdData, forbiddenUser);
		}

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, fill, wrap 1", "", "[]0"));

		resetButton.addActionListener(e -> resetData());
		JLabel frameTitleLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.title"));

		panel.add(frameTitleLabel, "split 2, gapright " + Globals.GAP_SIZE);
		panel.add(resetButton, "wrap");

		if (!forbiddenDevice) {
			panel.add(pMsgInfoGeneral, "grow, pushy, gapy " + Globals.GAP_SIZE);
		}

		if (!forbiddenUser) {
			panel.add(pMsgInfoUser, "grow, pushy, gapy " + Globals.GAP_SIZE);
		}

		scrollpane.getViewport().add(panel);
		scrollpane.setBorder(null);
	}

	private void resetData() {
		Logging.debug("FMessageOfTheDay resetData(both)");
		motdData = persistenceController.getDataServices().config.getMessageOfTheDayConfigs();
		if (!forbiddenDevice) {
			pMsgInfoGeneral.setDataMap(motdData);
			pMsgInfoGeneral.resetData();
		}
		if (!forbiddenUser) {
			pMsgInfoUser.setDataMap(motdData);
			pMsgInfoUser.resetData();
		}
	}

	private void saveData() {
		Logging.debug("FMessageOfTheDay saveData");
		Map<String, String> data = new HashMap<>();
		if (!forbiddenDevice) {
			data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE, pMsgInfoGeneral.getText());
			data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL,
					pMsgInfoGeneral.getValidUntil());
		}
		if (!forbiddenUser) {
			data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER, pMsgInfoUser.getText());
			data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL,
					pMsgInfoUser.getValidUntil());
		}
		if (data.isEmpty()) {
			Logging.error("FMessageOfTheDay saveData no data", data);
			return;
		}
		persistenceController.getDataServices().config.setMessageOfTheDayConfigs(data);
		Logging.info("FMessageOfTheDay saveData done: ", data);
		resetData();
	}
}
