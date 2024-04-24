/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FSelectionList;
import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;

public final class TerminalFrame implements MessagebusListener {
	private JFrame frame;

	private Messagebus messagebus;

	private TerminalTabbedPane tabbedPane;
	private TerminalFileUploadProgressIndicator fileUploadProgressIndicator;

	private boolean restrictView;

	private Runnable callback;

	public TerminalFrame() {
		this(false);
	}

	public TerminalFrame(boolean restrictView) {
		this.restrictView = restrictView;
	}

	public void setMessagebus(Messagebus messagebus) {
		this.messagebus = messagebus;
	}

	public JFrame getFrame() {
		return frame;
	}

	public TerminalTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public TerminalFileUploadProgressIndicator getTerminalFileUploadProgressIndicator() {
		return fileUploadProgressIndicator;
	}

	public void setOnClose(Runnable callback) {
		this.callback = callback;
	}

	private void createAndShowGUI() {
		frame = new JFrame(Configed.getResourceValue("Terminal.title"));
		frame.setIconImage(Utils.getMainIcon());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		TerminalMenuBar menuBar = new TerminalMenuBar(this);
		menuBar.init();
		frame.setJMenuBar(menuBar);

		JPanel allPane = new JPanel();

		GroupLayout allLayout = new GroupLayout(allPane);
		allPane.setLayout(allLayout);

		JPanel northPanel = createNorthPanel();
		fileUploadProgressIndicator = new TerminalFileUploadProgressIndicator();
		fileUploadProgressIndicator.init();
		fileUploadProgressIndicator.setVisible(false);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(fileUploadProgressIndicator));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup().addComponent(northPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(allLayout.createSequentialGroup().addComponent(fileUploadProgressIndicator)));

		frame.add(allPane);

		frame.setSize(600, 400);
		frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				tabbedPane.removeAllTerminalTabs();
				close();
			}

			@Override
			public void windowActivated(WindowEvent e) {
				focusOnSelectedWidget();
			}
		});
	}

	public void openNewWindow() {
		if (restrictView) {
			return;
		}

		TerminalFrame terminalFrame = new TerminalFrame();
		terminalFrame.setMessagebus(messagebus);
		terminalFrame.display();
	}

	public void openNewSession() {
		if (restrictView) {
			return;
		}

		tabbedPane.addTerminalTab();
		displaySessionsDialog();
	}

	public void displaySessionsDialog() {
		if (restrictView) {
			return;
		}

		FSelectionList sessionsDialog = new FSelectionList(frame, Configed.getResourceValue("Terminal.session.title"),
				true, new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") },
				500, 300);
		List<String> clientsConnectedByMessagebus = new ArrayList<>(PersistenceControllerFactory
				.getPersistenceController().getHostDataService().getMessagebusConnectedClients());
		clientsConnectedByMessagebus.add("Configserver");
		Collections.sort(clientsConnectedByMessagebus);
		sessionsDialog.setListData(clientsConnectedByMessagebus);
		sessionsDialog.setVisible(true);

		if (sessionsDialog.getResult() == 2) {
			TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
			if (widget != null) {
				tabbedPane.getSelectedTerminalWidget().close();
				tabbedPane.resetTerminalWidgetOnSelectedTab();
				tabbedPane.openSessionOnSelectedTab(sessionsDialog.getSelectedValue());
			}
		}
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		tabbedPane = new TerminalTabbedPane(this);
		tabbedPane.setMessagebus(messagebus);
		tabbedPane.init();
		tabbedPane.addTerminalTab();
		if (!restrictView) {
			tabbedPane.openSessionOnSelectedTab("Configserver");
		} else {
			tabbedPane.getSelectedTerminalWidget().connectPipedTty();
		}
		tabbedPane.getSelectedTerminalWidget().requestFocus();

		northLayout
				.setVerticalGroup(northLayout.createSequentialGroup().addComponent(tabbedPane, 0, 0, Short.MAX_VALUE));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup()
				.addGroup(northLayout.createSequentialGroup().addComponent(tabbedPane, 0, 0, Short.MAX_VALUE)));

		return northPanel;
	}

	private void focusOnSelectedWidget() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget != null) {
			widget.requestFocusInWindow();
		}
	}

	public void writeToWidget(String message) {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget != null) {
			widget.write(message.getBytes());
		}
	}

	public void disableUserInputForSelectedWidget() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		widget.getTerminalPanel().addCustomKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				widget.setDisableUserInput(true);
				event.consume();
			}

			@Override
			public void keyReleased(KeyEvent event) {
				widget.setDisableUserInput(false);
				event.consume();
			}
		});
		widget.setViewRestricted(true);
	}

	@SuppressWarnings({ "java:S2325" })
	public void uploadFile(AbstractBackgroundFileUploader fileUploader) {
		fileUploader.setTotalFilesToUpload(fileUploader.getTotalFilesToUpload() + 1);
		fileUploader.execute();
	}

	public void display() {
		if (frame == null) {
			createAndShowGUI();
		} else {
			frame.setVisible(true);
		}

		if (!messagebus.getWebSocket().isListenerRegistered(this)) {
			messagebus.getWebSocket().registerListener(this);
		}
	}

	public void changeTitle() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (frame != null && widget != null) {
			frame.setTitle(widget.getTitle());
		}
	}

	public void close() {
		callback.run();
		frame.dispose();
		messagebus.getWebSocket().unregisterListener(this);
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		close();
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		if (tabbedPane.getTabCount() == 0) {
			close();
		}

		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget == null || !widget.isMessageForThisChannel(message)) {
			return;
		}

		String type = (String) message.get("type");
		if (WebSocketEvent.TERMINAL_CLOSE_EVENT.toString().equals(type) && tabbedPane.getTabCount() == 1) {
			close();
		}
	}
}
