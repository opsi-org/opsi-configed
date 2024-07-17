/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import de.uib.utils.logging.Logging;

/**
 * {@code SecureSSLSocketFactory} extends {@code SSLSocketFactory} to retrieve
 * information about SSL version used in HTTPS connection.
 * <p>
 * The retrieved information is logged in {@link MyHandshakeCompletedListener}
 * class.
 * <p>
 * The solution is based on the following stackoverflow answer:
 * https://stackoverflow.com/questions/27075678/get-ssl-version-used-in-httpsurlconnection-java
 */
public class SecureSSLSocketFactory extends SSLSocketFactory {
	private final SSLSocketFactory delegate;
	private HandshakeCompletedListener handshakeListener;

	public SecureSSLSocketFactory(SSLSocketFactory delegate, HandshakeCompletedListener handshakeListener) {
		this.delegate = delegate;
		this.handshakeListener = handshakeListener;
	}

	@Override
	@SuppressWarnings("java:S1192")
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		SSLSocket socket = (SSLSocket) delegate.createSocket(s, host, port, autoClose);
		Logging.debug(this, "createSocket host, port: ", host, ",", port, " autoClose ", autoClose,
				" enabled cipher suites ", Arrays.toString(socket.getEnabledCipherSuites()));

		if (null != handshakeListener) {
			socket.addHandshakeCompletedListener(handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket() throws IOException {
		SSLSocket socket = (SSLSocket) delegate.createSocket();
		Logging.debug(this, "createSocket ", " enabled cipher suites ",
				Arrays.toString(socket.getEnabledCipherSuites()));
		// on some connections there is, after some time, a javax.net.ssl.SSLException:
		// SSL peer shut down incorrect
		// the standard enabled cipher suite seems to be TLS_RSA_WITH_AES_256_CBC_SHA256

		if (null != handshakeListener) {
			socket.addHandshakeCompletedListener(handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
		Logging.debug(this, "createSocket host, port: ", host, ",", port, " enabled cipher suites ",
				Arrays.toString(socket.getEnabledCipherSuites()));

		if (null != handshakeListener) {
			socket.addHandshakeCompletedListener(handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		SSLSocket socket = (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
		Logging.debug(this, "createSocket adress, port, localAddress, localPort: ", address, ",", port, ",",
				localAddress, ",", localPort, " enabled cipher suites ",
				Arrays.toString(socket.getEnabledCipherSuites()));

		if (null != handshakeListener) {
			socket.addHandshakeCompletedListener(handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
		Logging.debug(this, "createSocket host, port: ", host, ",", port, " enabled cipher suites ",
				Arrays.toString(socket.getEnabledCipherSuites()));

		if (null != handshakeListener) {
			socket.addHandshakeCompletedListener(handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		SSLSocket socket = (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
		Logging.debug(this, "createSocket host, port, localHost, localPort: ", host, ",", port, ",", localHost, ",",
				localPort, " enabled cipher suites ", Arrays.toString(socket.getEnabledCipherSuites()));

		if (null != handshakeListener) {
			socket.addHandshakeCompletedListener(handshakeListener);
		}

		return socket;
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return delegate.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return delegate.getSupportedCipherSuites();
	}
}
