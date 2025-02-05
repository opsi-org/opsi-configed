/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.messageoftheday;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.MainFrame;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserFeaturesConfig;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

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
		List<Object> forbiddenItemsMOTD = UserConfig.getCurrentUserConfig()
				.getValues(UserFeaturesConfig.KEY_MOTD_ACCESS_FORBIDDEN);
		forbiddenDevice = forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_DEVICE);
		forbiddenUser = forbiddenItemsMOTD.contains(UserFeaturesConfig.KEY_OPT_MOTD_USER);
		if (forbiddenDevice && forbiddenUser) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		motdData = persistenceController.getConfigDataService().getMessageOfTheDayConfigs();
		define();
		init();

		// JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
		JOptionPane optionPane = new JOptionPane(scrollpane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, new Object[] { Configed.getResourceValue("save"), Configed.getResourceValue("buttonCancel") });
		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConfigedMain.MessageOfTheDay.title"));
		dialog.setVisible(true);

		if (optionPane.getValue() != null && optionPane.getValue().equals(Configed.getResourceValue("buttonExecute"))) {
			saveData();
		}
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
		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);

		resetButton.addActionListener(e -> resetData());
		JLabel frameTitleLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.title"));

		SequentialGroup seqGroup = gpl.createSequentialGroup();
		seqGroup.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(frameTitleLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(resetButton,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		ParallelGroup vertGroup = gpl.createParallelGroup();
		vertGroup.addGroup(gpl.createSequentialGroup()
				.addComponent(frameTitleLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addGroup(gpl.createSequentialGroup().addComponent(resetButton,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		if (!forbiddenDevice) {
			seqGroup.addGap(Globals.GAP_SIZE).addComponent(pMsgInfoGeneral, GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
			vertGroup.addComponent(pMsgInfoGeneral, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
					Short.MAX_VALUE);
		}
		if (!forbiddenUser) {
			vertGroup.addComponent(pMsgInfoUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
					Short.MAX_VALUE);
			seqGroup.addGap(Globals.GAP_SIZE).addComponent(pMsgInfoUser, GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
		}
		gpl.setVerticalGroup(seqGroup);
		gpl.setHorizontalGroup(vertGroup);
		scrollpane.getViewport().add(panel);
		scrollpane.setBorder(null);
	}

	private void resetData() {
		Logging.debug("FMessageOfTheDay resetData(both)");
		motdData = persistenceController.getConfigDataService().getMessageOfTheDayConfigs();
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
		Logging.debug("FMessageOfTheDay doAction2 store");
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
			Logging.error("FMessageOfTheDay doAction2 store no data", data);
			return;
		}
		persistenceController.getConfigDataService().setMessageOfTheDayConfigs(data);
		Logging.info("FMessageOfTheDay doAction2 store done: ", data);
		resetData();
	}
}
