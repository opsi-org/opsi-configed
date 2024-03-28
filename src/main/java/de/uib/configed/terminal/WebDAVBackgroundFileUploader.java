/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.CertificateValidatorFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class WebDAVBackgroundFileUploader extends AbstractBackgroundFileUploader {
	private String destinationDir;
	private boolean visualizeProgress;

	public WebDAVBackgroundFileUploader(TerminalFrame terminal, File file, String destinationDir,
			boolean visualizeProgress, Runnable callback) {
		super(terminal, visualizeProgress, callback);
		this.currentFile = file;
		this.destinationDir = destinationDir;
		this.visualizeProgress = visualizeProgress;
		this.callback = callback;
	}

	@Override
	protected void upload() {
		Sardine sardine = new SardineImpl() {
			@Override
			protected ConnectionSocketFactory createDefaultSecureSocketFactory() {
				return new SSLSocketFactoryWrapper(CertificateValidatorFactory.createSecure().createSSLSocketFactory());
			}
		};
		int port = getPortFromHost(ConfigedMain.getHost());
		sardine.enablePreemptiveAuthentication(ConfigedMain.getHost(), port, port);
		sardine.setCredentials(ConfigedMain.getUser(), ConfigedMain.getPassword());
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(currentFile))) {
			if (visualizeProgress) {
				updateTotalFilesToUpload();
			}
			String url = "https://" + ConfigedMain.getHost() + ":" + port + "/dav/" + destinationDir
					+ currentFile.getName();
			ProgressTrackerInputStream progressInputStream = new ProgressTrackerInputStream(inputStream);
			sardine.put(url, progressInputStream);
		} catch (IOException e) {
			Logging.error(this, "Unable to upload file to a server through WebDAV", e);
		}
	}

	private int getPortFromHost(String host) {
		int port = Globals.DEFAULT_PORT;

		if (Utils.hasPort(host)) {
			Logging.info(this, "Host does have specified port - retrieving port");
			int idx = -1;
			if (host.contains("[") && host.contains("]")) {
				idx = host.indexOf(":", host.indexOf("]"));
			} else {
				idx = host.indexOf(":");
			}

			if (idx > -1) {
				port = Integer.parseInt(host.substring(idx + 1, host.length()));
			}
		} else {
			Logging.info(this, "Host doesn't have specified port - using default port " + Globals.DEFAULT_PORT);
		}

		return port;
	}

	@SuppressWarnings({ "java:S2972" })
	private class ProgressTrackerInputStream extends InputStream {
		private final InputStream inputStream;
		private int totalBytesRead;

		ProgressTrackerInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public int read() throws IOException {
			int bytesRead = inputStream.read();
			if (bytesRead != -1) {
				totalBytesRead++;
				publish(totalBytesRead);
			}
			return bytesRead;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int bytesRead = inputStream.read(b, off, len);
			if (bytesRead != -1) {
				totalBytesRead += bytesRead;
				publish(totalBytesRead);
			}
			return bytesRead;
		}

		@Override
		public void close() throws IOException {
			inputStream.close();
		}
	}

	private static class SSLSocketFactoryWrapper implements ConnectionSocketFactory {
		private final SSLSocketFactory sslSocketFactory;

		public SSLSocketFactoryWrapper(SSLSocketFactory sslSocketFactory) {
			this.sslSocketFactory = sslSocketFactory;
		}

		@Override
		public Socket createSocket(HttpContext context) throws IOException {
			return sslSocketFactory.createSocket();
		}

		@Override
		public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress,
				InetSocketAddress localAddress, HttpContext context) throws IOException {
			if (sock == null) {
				sock = createSocket(context);
			}
			if (remoteAddress != null) {
				SSLSocket sslSocket = (SSLSocket) sock;
				sslSocket.connect(remoteAddress, connectTimeout);
				return sslSocket;
			} else {
				throw new IllegalArgumentException("Remote address may not be null");
			}
		}
	}
}
