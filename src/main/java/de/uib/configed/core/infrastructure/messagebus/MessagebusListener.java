/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure.messagebus;

import java.util.Map;

import org.java_websocket.handshake.ServerHandshake;

public interface MessagebusListener {
	void onOpen(ServerHandshake handshakeData);

	void onClose(int code, String reason, boolean remote);

	void onError(Exception ex);

	void onMessageReceived(Map<String, Object> message);
}
