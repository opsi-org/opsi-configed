/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.certificate.CertificateValidatorFactory;
import de.uib.utils.logging.Logging;

public class WebDAVClient {
	private Sardine sardine;

	public WebDAVClient() {
		sardine = new SardineImpl() {
			@Override
			protected ConnectionSocketFactory createDefaultSecureSocketFactory() {
				return new SSLSocketFactoryWrapper(
						CertificateValidatorFactory.createValidator().createSSLSocketFactory());
			}
		};
		int port = getPortFromHost(ConfigedMain.getHost());
		sardine.enablePreemptiveAuthentication(ConfigedMain.getHost(), port, port);
		sardine.setCredentials(ConfigedMain.getUser(), ConfigedMain.getPassword());
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
			Logging.info(this, "Host doesn't have specified port - using default port ", Globals.DEFAULT_PORT);
		}

		return port;
	}

	public void uploadFile(String location, InputStream dataSource) throws IOException {
		sardine.put(getBaseURL() + location, dataSource);
	}

	public Set<String> getDirectoriesIn(String currentDirectory) {
		Set<String> directories = new TreeSet<>();

		String url = getBaseURL() + currentDirectory;
		Logging.info("use webdav to get directories and files in ", url);

		try {
			List<DavResource> resources = sardine.list(url);
			for (DavResource resource : resources) {
				if (resource.isDirectory()) {
					directories.add(resource.getPath().replace("/dav/", ""));
				}
			}
		} catch (IOException e) {
			Logging.error(this, e, "Failed to retrieve directories from ", url);
		}
		return directories;
	}

	public Set<String> getDirectoriesAndFilesIn(String currentDirectory, String fileExtension) {
		Set<String> directoriesAndFiles = new TreeSet<>();

		String url = getBaseURL() + currentDirectory;
		Logging.info("use webdav to get directories and files in ", url);

		try {
			List<DavResource> resources = sardine.list(url);
			for (DavResource resource : resources) {
				if ((!resource.getDisplayName().equals(currentDirectory.substring(0, currentDirectory.length() - 1))
						&& resource.isDirectory()) || resource.getDisplayName().endsWith(fileExtension)) {
					directoriesAndFiles.add(resource.getPath().replace("/dav/", ""));
				}
			}
		} catch (IOException e) {
			Logging.error(this, e, "Failed to retrieve directories and files from ", url);
		}
		return directoriesAndFiles;
	}

	private String getBaseURL() {
		return "https://" + ConfigedMain.getHost() + ":" + getPortFromHost(ConfigedMain.getHost()) + "/dav/";
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
