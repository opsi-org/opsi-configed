/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import de.uib.utils.logging.Logging;

/**
 * {@code MyHandshakeCompletedListener} logs some information about the server,
 * with which the connection was established.
 */
public class MyHandshakeCompletedListener implements HandshakeCompletedListener {
	@Override
	public void handshakeCompleted(HandshakeCompletedEvent event) {
		SSLSession session = event.getSession();
		String protocol = session.getProtocol();
		String cipherSuite = session.getCipherSuite();
		String peerName = null;

		try {
			peerName = session.getPeerPrincipal().getName();
		} catch (SSLPeerUnverifiedException e) {
			Logging.error(this, "peer's identity wasn't verified", e);
		}

		Logging.info(this, "protocol " + protocol + "  peerName " + peerName);
		Logging.info(this, "cipher suite " + cipherSuite);
	}
}
