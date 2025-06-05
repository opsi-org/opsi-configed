/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;

import de.uib.configed.Globals;
import de.uib.opsicommand.certificate.CertificateValidator;
import de.uib.opsicommand.certificate.CertificateValidatorFactory;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class WebDAVClient {
	private static final int DEFAULT_UPLOAD_THREADS = Runtime.getRuntime().availableProcessors() * 2;

	private Sardine sardine;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public WebDAVClient() {
		HttpClientBuilder builder = HttpClientBuilder.create();
		CertificateValidator validator = CertificateValidatorFactory.getValidator();
		builder.setSSLSocketFactory(new SSLSocketFactoryWrapper(validator.getSSLSocketFactory()));

		String sessionID = PersistenceControllerFactory.getPersistenceController().getExecutioner().getSessionId();

		if (sessionID != null) {
			builder.setDefaultHeaders(Collections.singletonList(new BasicHeader("Cookie", sessionID)));
			sardine = new SardineImpl(builder);
		} else {
			sardine = new SardineImpl(builder);
			int port = getPortFromHost(persistenceController.getExecutioner().getHost());
			sardine.enablePreemptiveAuthentication(persistenceController.getExecutioner().getHost(), port, port);
			sardine.setCredentials(persistenceController.getExecutioner().getUsername(),
					persistenceController.getExecutioner().getPassword());
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
			Logging.info(this, "Host doesn't have specified port - using default port ", Globals.DEFAULT_PORT);
		}

		return port;
	}

	public void uploadFile(String location, InputStream dataSource) throws IOException {
		String remoteURL = location.startsWith(getBaseURL()) ? location : (getBaseURL() + location);
		String parsedRemoteURL = parseURL(remoteURL);

		if (isInputStreamEmpty(dataSource)) {
			sardine.put(parsedRemoteURL, new byte[0]);
		} else {
			InputStream uploadStream = (dataSource instanceof BufferedInputStream) ? dataSource
					: new BufferedInputStream(dataSource);
			sardine.put(parsedRemoteURL, uploadStream);
		}
	}

	private static boolean isInputStreamEmpty(InputStream in) throws IOException {
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		in.mark(1);
		int b = in.read();
		in.reset();
		return b == -1;
	}

	public void uploadDirectory(File localDir, String remotePath) throws IOException {
		if (!localDir.isDirectory()) {
			Logging.warning(this, "Provided file is not a directory");
			throw new IllegalArgumentException("Provided file is not a directory");
		}

		String remoteDirUrl = parseURL(getBaseURL() + remotePath + localDir.getName());
		sardine.createDirectory(remoteDirUrl);

		ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_UPLOAD_THREADS);
		try {
			uploadRecursiveParallel(localDir, remoteDirUrl, executor);
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	private void uploadRecursiveParallel(File localDir, String remoteDirUrl, ExecutorService executor)
			throws IOException {
		for (File file : localDir.listFiles()) {
			if (file.isDirectory()) {
				String subDirUrl = parseURL(remoteDirUrl + "/" + file.getName());
				sardine.createDirectory(subDirUrl);
				uploadRecursiveParallel(file, subDirUrl, executor);
			} else {
				executor.submit(() -> {
					try (InputStream fis = new BufferedInputStream(new FileInputStream(file))) {
						String remoteFileUrl = parseURL(remoteDirUrl + "/" + file.getName());
						uploadFile(remoteFileUrl, fis);
					} catch (IOException e) {
						Logging.warning(this,
								"Failed to upload file: " + file.getAbsolutePath() + " - " + e.getMessage());
					}
				});
			}
		}
	}

	private String parseURL(String rawUrl) {
		try {
			int pathIndex = rawUrl.indexOf('/', rawUrl.indexOf("://") + 3);
			String base = (pathIndex > 0) ? rawUrl.substring(0, pathIndex) : rawUrl;
			String path = (pathIndex > 0) ? rawUrl.substring(pathIndex) : "";

			String[] parts = path.split("/");
			StringBuilder encodedPath = new StringBuilder();
			for (String part : parts) {
				if (!part.isEmpty()) {
					encodedPath.append("/").append(URLEncoder.encode(part, StandardCharsets.UTF_8));
				}
			}

			URI baseUri = new URI(base);
			URI fullUri = new URI(baseUri.getScheme(), null, baseUri.getHost(), baseUri.getPort(),
					encodedPath.toString(), null, null);
			return fullUri.toURL().toString();
		} catch (URISyntaxException e) {
			Logging.warning(this, "Failed to parse URL ", e);
		} catch (MalformedURLException e) {
			Logging.warning(this, "malformed URL encountered: ", rawUrl, e);
		}
		return "";
	}

	public boolean existsAndIsDirectory(String url) {
		try {
			return isDirectory(url);
		} catch (IOException e) {
			Logging.warning(this, "Failed to check whether directory exists ", url, e);
			return false;
		}
	}

	public boolean isDirectory(String url) throws IOException {
		List<DavResource> resources = sardine.list(parseURL(getBaseURL() + url));
		for (DavResource res : resources) {
			if ("/".equals(res.getHref().toString())) {
				return res.isDirectory();
			}
		}
		return !resources.isEmpty() && resources.get(0).isDirectory();
	}

	public Set<String> getDirectoriesIn(String currentDirectory) {
		return getDirectoriesIn(currentDirectory, true);
	}

	public Set<String> getDirectoriesIn(String currentDirectory, boolean includeParentDir) {
		Set<String> directories = new TreeSet<>();

		String url = getBaseURL() + currentDirectory;
		Logging.info("use webdav to get directories and files in ", url);

		try {
			List<DavResource> resources = sardine.list(url);
			for (DavResource resource : resources) {
				if (resource.isDirectory()) {
					String dirPath = resource.getPath().replace("/dav/", "");
					if (!includeParentDir) {
						dirPath = dirPath.replace(currentDirectory, "");
					}
					directories.add(dirPath);
				}
			}
		} catch (IOException e) {
			Logging.error(this, e, "Failed to retrieve directories from ", url);
		}
		return directories;
	}

	public Set<String> getDirectoriesAndFilesIn(String currentDirectory, String fileExtension) {
		return getDirectoriesAndFilesIn(currentDirectory, fileExtension, true);
	}

	public Set<String> getDirectoriesAndFilesIn(String currentDirectory, String fileExtension,
			boolean includeParentDir) {
		Set<String> directoriesAndFiles = new TreeSet<>();

		String url = getBaseURL() + currentDirectory;
		Logging.info("use webdav to get directories and files in ", url);

		try {
			List<DavResource> resources = sardine.list(url);
			for (DavResource resource : resources) {
				if ((!resource.getDisplayName().equals(currentDirectory.substring(0, currentDirectory.length() - 1))
						&& resource.isDirectory()) || resource.getDisplayName().endsWith(fileExtension)) {
					String dirPath = resource.getPath().replace("/dav/", "");
					if (!includeParentDir) {
						dirPath = dirPath.replace(currentDirectory, "");
					}
					directoriesAndFiles.add(dirPath);
				}
			}
		} catch (IOException e) {
			Logging.error(this, e, "Failed to retrieve directories and files from ", url);
		}
		return directoriesAndFiles;
	}

	private String getBaseURL() {
		return "https://" + persistenceController.getExecutioner().getHost() + ":"
				+ getPortFromHost(persistenceController.getExecutioner().getHost()) + "/dav/";
	}

	@SuppressWarnings({ "squid:S2972" })
	private static class SSLSocketFactoryWrapper implements LayeredConnectionSocketFactory {
		private final SSLSocketFactory sslSocketFactory;

		public SSLSocketFactoryWrapper(SSLSocketFactory sslSocketFactory) {
			this.sslSocketFactory = sslSocketFactory;
		}

		@Override
		public Socket createSocket(HttpContext context) throws IOException {
			return sslSocketFactory.createSocket();
		}

		@Override
		public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context)
				throws IOException {
			return sslSocketFactory.createSocket(socket, target, port, true);
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
