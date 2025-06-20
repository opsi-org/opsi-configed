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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineImpl;

import de.uib.configed.Globals;
import de.uib.opsicommand.certificate.CertificateValidator;
import de.uib.opsicommand.certificate.CertificateValidatorFactory;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class WebDAVClient {
	private static final int DEFAULT_UPLOAD_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	private static final Pattern ENCODING_PATTERN = Pattern.compile(".*%[0-9a-fA-F]{2}.*");

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

		Logging.info(this, "Uploading file to WebDAV: " + parsedRemoteURL);

		if (isInputStreamEmpty(dataSource)) {
			Logging.info(this, "Input stream is empty, uploading zero-byte file: " + parsedRemoteURL);
			sardine.put(parsedRemoteURL, new byte[0]);
		} else {
			Logging.info(this, "Input stream is non-empty, uploading file: " + location);
			InputStream uploadStream = (dataSource instanceof BufferedInputStream) ? dataSource
					: new BufferedInputStream(dataSource);
			sardine.put(parsedRemoteURL, uploadStream);
			Logging.info(this, "Successfully uploaded file to: " + parsedRemoteURL);
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
		createDirectoryIfNotExists(remoteDirUrl);

		ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_UPLOAD_THREADS);
		try {
			uploadRecursiveParallel(localDir, remoteDirUrl, executor);
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException ie) {
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
				createDirectoryIfNotExists(subDirUrl);
				Logging.info(this, "Created/checked directory: ", subDirUrl);
				uploadRecursiveParallel(file, subDirUrl, executor);
			} else {
				executor.submit(() -> {
					try (InputStream fis = new BufferedInputStream(new FileInputStream(file))) {
						String remoteFileUrl = parseURL(remoteDirUrl + "/" + file.getName());
						Logging.info(this, "Uploading file: ", file.getAbsolutePath(), " to ", remoteFileUrl);
						uploadFile(remoteFileUrl, fis);
						Logging.info(this, "Successfully uploaded file: ", file.getAbsolutePath());
					} catch (IOException e) {
						Logging.warning(this, "Failed to upload file: ", file.getAbsolutePath(), " - ", e);
					}
				});
			}
		}
	}

	private void createDirectoryIfNotExists(String remoteDirUrl) {
		try {
			sardine.createDirectory(remoteDirUrl);
			Logging.info(this, "Created directory: ", remoteDirUrl);
		} catch (SardineException se) {
			if (se.getStatusCode() != 405 && se.getStatusCode() != 409) {
				Logging.warning(this, "Failed to create directory (SardineException): ", remoteDirUrl, " - ", se);
			}
		} catch (IOException ioe) {
			Logging.warning(this, "Failed to create directory (IOException): ", remoteDirUrl, " - ", ioe);
		}
	}

	public void createDirectories(String remoteDirPath) throws IOException {
		if (remoteDirPath == null || remoteDirPath.isEmpty()) {
			return;
		}

		for (String dirUrl : getDirectoryUrls(remoteDirPath)) {
			if (!sardine.exists(dirUrl)) {
				createDirectory(dirUrl);
			}
		}
	}

	private List<String> getDirectoryUrls(String remoteDirPath) {
		String[] parts = remoteDirPath.split("/");
		StringBuilder currentPath = new StringBuilder();
		List<String> urls = new ArrayList<>();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			currentPath.append(encodePathSegmentIfNeeded(part)).append("/");
			urls.add(getBaseURL() + currentPath.toString());
		}
		return urls;
	}

	private void createDirectory(String dirUrl) {
		try {
			sardine.createDirectory(dirUrl);
			Logging.info(this, "Created directory: ", dirUrl);
		} catch (SardineException se) {
			if (se.getStatusCode() != 405 && se.getStatusCode() != 409) {
				Logging.warning(this, "Failed to create directory (SardineException): ", dirUrl, " - ", se);
			}
		} catch (IOException ioe) {
			Logging.warning(this, "Failed to create directory (IOException): ", dirUrl, " - ", ioe);
		}
	}

	private String encodePathSegmentIfNeeded(String segment) {
		if (isEncoded(segment)) {
			return segment;
		}
		try {
			return URLEncoder.encode(segment, StandardCharsets.UTF_8.toString()).replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			Logging.warning(this, "Unsupported encoding", e);
		}
		return "";
	}

	private static boolean isEncoded(String segment) {
		return ENCODING_PATTERN.matcher(segment).matches();
	}

	private String parseURL(String rawUrl) {
		try {
			int pathIndex = rawUrl.indexOf('/', rawUrl.indexOf("://") + 3);
			String base = (pathIndex > 0) ? rawUrl.substring(0, pathIndex) : rawUrl;
			String path = (pathIndex > 0) ? rawUrl.substring(pathIndex) : "";

			URI baseUri = new URI(base);
			URI fullUri = new URI(baseUri.getScheme(), null, baseUri.getHost(), baseUri.getPort(), path, null, null);
			return fullUri.toURL().toString();
		} catch (URISyntaxException use) {
			Logging.warning(this, "Failed to parse URL ", use);
		} catch (MalformedURLException mue) {
			Logging.warning(this, "Malformed URL encountered: ", rawUrl, mue);
		}
		return "";
	}

	public boolean existsAndIsDirectory(String url) {
		try {
			return isDirectory(url);
		} catch (IOException ioe) {
			Logging.warning(this, "Failed to check whether directory exists ", url, ioe);
			return false;
		}
	}

	public boolean isDirectory(String url) throws IOException {
		String fullUrl = parseURL(getBaseURL() + url);

		String requestedPath;
		try {
			URI requestedUri = new URI(fullUrl);
			requestedPath = requestedUri.getPath();
			if (!requestedPath.endsWith("/")) {
				requestedPath += "/";
			}
		} catch (URISyntaxException e) {
			Logging.warning(this, "Invalid URI: ", fullUrl, e);
			return false;
		}

		List<DavResource> resources = sardine.list(fullUrl);
		for (DavResource res : resources) {
			String resourcePath = res.getHref().getPath();
			if (!resourcePath.endsWith("/")) {
				resourcePath += "/";
			}
			String decodedRequestedPath = URLDecoder.decode(requestedPath, StandardCharsets.UTF_8);
			String decodedResourcePath = URLDecoder.decode(resourcePath, StandardCharsets.UTF_8);

			if (decodedRequestedPath.equals(decodedResourcePath)) {
				return res.isDirectory();
			}
		}
		return !resources.isEmpty() && resources.get(0).isDirectory();
	}

	public Set<String> getDirectoriesIn(String currentDirectory) {
		return getDirectoriesIn(currentDirectory, true);
	}

	public Set<String> getDirectoriesIn(String currentDirectory, boolean includeParentDir) {
		return getEntriesIn(currentDirectory, null, includeParentDir, true);
	}

	public Set<String> getDirectoriesAndFilesIn(String currentDirectory, String fileExtension) {
		return getDirectoriesAndFilesIn(currentDirectory, fileExtension, true);
	}

	public Set<String> getDirectoriesAndFilesIn(String currentDirectory, String fileExtension,
			boolean includeParentDir) {
		return getEntriesIn(currentDirectory, fileExtension, includeParentDir, false);
	}

	@SuppressWarnings("java:S134")
	private Set<String> getEntriesIn(String currentDirectory, String fileExtension, boolean includeParentDir,
			boolean dirsOnly) {
		Set<String> entries = new HashSet<>();
		String url = getBaseURL() + currentDirectory;
		Logging.info("Retrieving " + (dirsOnly ? "directory" : "directory and file") + " list via WebDAV ", url);

		String basePath = "/dav/";
		String currentDirPath = currentDirectory.endsWith("/") ? currentDirectory : (currentDirectory + "/");
		String parentDisplayName = getParentDisplayName(currentDirectory);

		try {
			List<DavResource> resources = sardine.list(url);
			entries.addAll(resources.parallelStream()
					.filter(resource -> shouldInclude(resource, fileExtension, dirsOnly, includeParentDir,
							parentDisplayName))
					.map(resource -> normalizePath(resource.getPath(), basePath, currentDirPath, includeParentDir))
					.filter(path -> !path.isEmpty()).collect(Collectors.toSet()));
		} catch (IOException e) {
			Logging.error(this, e,
					"Failed to retrieve " + (dirsOnly ? "directories" : "directories and files") + " from ", url);
		}
		return entries;
	}

	private static String getParentDisplayName(String currentDirectory) {
		if (currentDirectory.endsWith("/") && currentDirectory.length() > 1) {
			return currentDirectory.substring(0, currentDirectory.length() - 1);
		}
		return currentDirectory;
	}

	private static boolean shouldInclude(DavResource resource, String fileExtension, boolean dirsOnly,
			boolean includeParentDir, String parentDisplayName) {
		boolean isDir = resource.isDirectory();
		boolean isFile = !dirsOnly && resource.getDisplayName() != null
				&& resource.getDisplayName().endsWith(fileExtension);
		boolean isParentDir = isDir && parentDisplayName.equals(resource.getDisplayName());
		return (isDir && (!isParentDir || includeParentDir)) || isFile;
	}

	private static String normalizePath(String path, String basePath, String currentDirPath, boolean includeParentDir) {
		if (path.startsWith(basePath)) {
			path = path.substring(basePath.length());
		}
		if (!includeParentDir && path.startsWith(currentDirPath)) {
			path = path.substring(currentDirPath.length());
		}
		return path;
	}

	public String getBaseURL() {
		String webdavBaseURI = persistenceController.getHostInfoCollections().getConfigServerWebDavBaseURI();
		return webdavBaseURI + "/dav/";
	}

	public boolean exists(String location) {
		try {
			return sardine.exists(parseURL(getBaseURL() + location));
		} catch (IOException e) {
			Logging.warning(this, "Failed to check if file/dir exists on WebDAV server", e);
		}
		return false;
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
				Logging.warning(this, "Remote address may not be null");
				throw new IllegalArgumentException("Remote address may not be null");
			}
		}
	}
}
