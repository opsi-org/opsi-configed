/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.swinfopage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ErrorCode;
import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;

/**
 * Abstract class to manage batch export of SWAudit data, subclasses implement
 * the type of export
 */
public abstract class AbstractSWExporter {
	private String filepathStart;

	private String filenamePrefix = "report_swaudit_";

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	protected GenTableModel modelSWInfo;
	protected String scanInfo = "";
	protected String theHost;
	protected String exportFilename;

	private String server;
	private String user;
	private String password;
	private String clientsFile;
	private String outDir;

	/* constructor for standalone use */
	protected AbstractSWExporter() {
	}

	public void setArgs(String server, String user, String password, String clientsFile, String outDir) {
		this.server = server;
		this.user = user;
		this.password = password;
		this.clientsFile = clientsFile;
		this.outDir = outDir;
	}

	public void addMissingArgs() {
		if (server == null) {
			server = Globals.getCLIparam("Host (default: localhost): ", false);
		}

		if (server.isEmpty()) {
			server = "localhost";
		}

		if (user == null) {
			user = Globals.getCLIparam("User (default: " + System.getProperty("user.name") + ") : ", false);
		}

		if (user.isEmpty()) {
			user = System.getProperty("user.name");
		}

		if (password == null) {
			password = Globals.getCLIparam("Password: ", true);
		}

		if (clientsFile == null) {
			clientsFile = Globals.getCLIparam("File with client names: ", false);
		}

		if (clientsFile.isEmpty()) {
			finish(ErrorCode.CLIENTNAMES_FILENAME_MISSING);
		}

		File userHome = new File(System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY));
		String userHomeS = userHome.toString();

		if (outDir == null) {
			outDir = Globals.getCLIparam("Export directory (default: " + userHomeS + "): ", false);
		}

		if (outDir.isEmpty()) {
			outDir = userHomeS;
		}

		if (outDir.endsWith("/")) {
			filepathStart = outDir + filenamePrefix;
		} else {
			filepathStart = outDir + File.separator + filenamePrefix;
		}
	}

	public void run() {
		Messages.setLocale("en");
		persistenceController = PersistenceControllerFactory.getNewPersistenceController(server, user, password);
		if (persistenceController == null) {
			finish(ErrorCode.INITIALIZATION_ERROR);
		}

		if (persistenceController.getConnectionState().getState() != ConnectionState.CONNECTED) {
			finish(ErrorCode.CONNECTION_ERROR);
		}

		Logging.info(this, "starting");

		try (BufferedReader in = new BufferedReader(new FileReader(clientsFile, StandardCharsets.UTF_8))) {
			Logging.info(this, " in " + in);
			String line = in.readLine();
			while (line != null) {
				// we assume that each line is a hostId
				setHost(line);
				updateModel();

				setWriteToFile(filepathStart + line + getExtension());

				Logging.debug(" outDir: " + outDir);
				Logging.debug(" filePath: " + filepathStart + line + getExtension());
				export();

				line = in.readLine();
			}

		} catch (IOException iox) {
			Logging.warning(this, "IOException " + iox);
		}

	}

	private static void finish(int exitcode) {
		Logging.error(de.uib.configed.ErrorCode.tell(exitcode));
		Main.endApp(exitcode);
	}

	private void setWriteToFile(String path) {
		exportFilename = path;
	}

	public void setHost(String hostId) {
		if (modelSWInfo == null) {
			initModel(hostId);
		} else {
			theHost = hostId;
			updateModel();

		}

		setWriteToFile(filepathStart + hostId + ".pdf");
	}

	private void initModel(String hostId) {
		theHost = hostId;

		List<String> columnNames;
		List<String> classNames;

		columnNames = new ArrayList<>(SWAuditClientEntry.KEYS);
		columnNames.remove(0);
		classNames = new ArrayList<>();
		int[] finalColumns = new int[columnNames.size()];
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
			finalColumns[i] = i;
		}

		// no updates
		modelSWInfo = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						Logging.info(this, "retrieving data for " + theHost);
						Map<String, Map<String, Object>> tableData = persistenceController
								.retrieveSoftwareAuditData(theHost);

						if (tableData == null || tableData.keySet().isEmpty()) {
							Logging.debug(this, "tableData is empty or null");

							scanInfo = Configed.getResourceValue("PanelSWInfo.noScanResult");
						} else {
							Logging.debug(this, "retrieved size  " + tableData.keySet().size());
							scanInfo = "Scan " + persistenceController.getLastSoftwareAuditModification(theHost);
						}

						return tableData;
					}
				})), -1, finalColumns, null, null);
	}

	public abstract void export();

	protected abstract String getExtension();

	private void updateModel() {
		Logging.debug(this, "update modelSWInfo.getRowCount() " + modelSWInfo.getRowCount());
		modelSWInfo.requestReload();
		modelSWInfo.reset();
		Logging.debug(this, "update modelSWInfo.getRowCount() " + modelSWInfo.getRowCount());
	}
}
