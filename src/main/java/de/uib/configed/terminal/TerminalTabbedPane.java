/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.AbstractClosableTabComponent;

public class TerminalTabbedPane extends JPanel implements MessagebusListener {
	private JTabbedPane jTabbedPane;
	private TerminalFrame terminalFrame;
	private Messagebus messagebus;

	private boolean isClosingEvent;

	public TerminalTabbedPane(TerminalFrame terminalFrame) {
		this.terminalFrame = terminalFrame;
	}

	public void setMessagebus(Messagebus messagebus) {
		this.messagebus = messagebus;
	}

	public void init() {
		if (!messagebus.getWebSocket().isListenerRegistered(this)) {
			messagebus.getWebSocket().registerListener(this);
		}
		GroupLayout groupLayout = new GroupLayout(this);
		this.setLayout(groupLayout);

		jTabbedPane = new JTabbedPane();
		jTabbedPane.addChangeListener((ChangeEvent event) -> onTabChange());
		jTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		groupLayout
				.setVerticalGroup(groupLayout.createSequentialGroup().addComponent(jTabbedPane, 0, 0, Short.MAX_VALUE));

		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
				.addGroup(groupLayout.createSequentialGroup().addComponent(jTabbedPane, 0, 0, Short.MAX_VALUE)));
	}

	private void onTabChange() {
		if (isClosingEvent) {
			return;
		}
		TerminalWidget widget = getSelectedTerminalWidget();
		terminalFrame.changeTitle();
		if (widget != null) {
			jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(),
					getTitleFromSessionChannel(widget.getSessionChannel()));
			widget.requestFocus();
		}
	}

	public void addTerminalTab() {
		TerminalWidget widget = createTerminalWidget();
		jTabbedPane.addTab("", widget);
		jTabbedPane.setSelectedIndex(getTabIndex());
		AbstractClosableTabComponent closableTabComponent = new AbstractClosableTabComponent(jTabbedPane) {
			@Override
			public void close() {
				removeTerminalTab(jTabbedPane.indexOfTabComponent(this));
				if (getTabCount() == 0) {
					terminalFrame.close();
				}
			}
		};
		jTabbedPane.setTabComponentAt(getTabIndex(), closableTabComponent);
	}

	public void openSessionOnSelectedTab(String session) {
		TerminalWidget widget = getSelectedTerminalWidget();
		widget.openSession(session);
		jTabbedPane.setTitleAt(getTabIndex(), getTitleFromSessionChannel(widget.getSessionChannel()));
	}

	private int getTabIndex() {
		return jTabbedPane.getTabCount() != 0 ? (jTabbedPane.getTabCount() - 1) : 0;
	}

	public void removeSelectedTerminalTab() {
		removeTerminalTab(jTabbedPane.getSelectedIndex());
	}

	public void removeAllTerminalTabs() {
		int tabCount = getTabCount();
		for (int i = 0; i < tabCount; i++) {
			removeTerminalTab(0);
		}
	}

	private void removeTerminalTab(int index) {
		TerminalWidget widget = getTerminalWidget(index);
		if (widget != null && !isClosingEvent) {
			isClosingEvent = false;
			widget.close();
		}
		jTabbedPane.removeTabAt(index);
	}

	private TerminalWidget createTerminalWidget() {
		TerminalWidget widget = new TerminalWidget(terminalFrame, TerminalWidget.DEFAULT_TERMINAL_COLUMNS,
				TerminalWidget.DEFAULT_TERMINAL_ROWS, new TerminalSettingsProvider());
		widget.setMessagebus(messagebus);
		widget.init();
		return widget;
	}

	private static String getTitleFromSessionChannel(String sessionChannel) {
		return sessionChannel == null || TerminalWidget.CONFIG_SERVER_SESSION_CHANNEL.equals(sessionChannel)
				? PersistenceControllerFactory.getPersistenceController().getHostInfoCollections().getConfigServer()
				: sessionChannel;
	}

	public void changeSelectedTerminalTabTitle(String title) {
		jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), title);
	}

	public TerminalWidget getSelectedTerminalWidget() {
		return getTerminalWidget(jTabbedPane.getSelectedIndex());
	}

	private TerminalWidget getTerminalWidget(int index) {
		TerminalWidget widget = null;
		if (index == -1) {
			return widget;
		}

		try {
			widget = (TerminalWidget) jTabbedPane.getComponentAt(index);
		} catch (IndexOutOfBoundsException e) {
			Logging.warning(this, "Requested component does not exist " + index, e);
		}
		return widget;
	}

	public int getTabCount() {
		return jTabbedPane.getTabCount();
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		removeAllTerminalTabs();
		messagebus.getWebSocket().unregisterListener(this);
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		TerminalWidget widget = getSelectedTerminalWidget();
		if (widget == null || !widget.isMessageForThisChannel(message)) {
			return;
		}

		String type = (String) message.get("type");
		if (WebSocketEvent.TERMINAL_CLOSE_EVENT.toString().equals(type)) {
			isClosingEvent = true;
			if (jTabbedPane == null || getTabCount() == 0) {
				messagebus.getWebSocket().unregisterListener(this);
			} else {
				removeSelectedTerminalTab();
			}
		}
	}
}
