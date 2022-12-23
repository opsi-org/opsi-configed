package de.uib.configed.gui.swinfopage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.messages.Messages;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;

/**
 * Abstract class to manage batch export of SWAudit data, subclasses implement
 * the type of export
 */
public abstract class SWExporter {
	File exportDirectory;
	String exportDirectoryS;
	String filepathStart;
	/*
	 * private String server;
	 * private String user;
	 * private String password;
	 */

	protected Boolean askingForKindOfAction = false;
	protected boolean askForOverwrite = false;
	protected String filenamePrefix = "report_swaudit_";

	protected final String usage = "\n" + "configed_exporter [OPTIONS] [NAME]\n\n";

	protected PersistenceController persist;

	protected GenTableModel modelSWInfo;
	protected String scanInfo = "";
	protected String theHost;
	protected String exportFilename;

	private String server;
	private String user;
	private String password;
	private String clientsFile;
	private String outDir;

	protected String title;

	/* constructor for use in a initialized context */
	public SWExporter(PersistenceController controller) {
		this.persist = controller;
	}

	/* constructor for standalone use */
	public SWExporter() {
	}

	public void setArgs(String server, String user, String password, String clientsFile, String outDir) {
		this.server = server;
		this.user = user;
		this.password = password;
		this.clientsFile = clientsFile;
		this.outDir = outDir;
	}

	public void addMissingArgs() {
		if (server == null)
			server = Globals.getCLIparam("Host (default: localhost): ", false);
		if (server.equals(""))
			server = "localhost";

		if (user == null)
			user = Globals.getCLIparam("User (default: " + System.getProperty("user.name") + ") : ", false);
		if (user.equals(""))
			user = System.getProperty("user.name");

		if (password == null)
			password = Globals.getCLIparam("Password: ", true);

		if (clientsFile == null)
			clientsFile = Globals.getCLIparam("File with client names: ", false);
		if (clientsFile.equals("")) {
			finish(de.uib.configed.ErrorCode.CLIENTNAMES_FILENAME_MISSING);
		}

		File userHome = new File(System.getProperty(logging.envVariableForUserDirectory));
		String userHomeS = userHome.toString();

		if (outDir == null)
			outDir = Globals.getCLIparam("Export directory (default: " + userHomeS + "): ", false);
		if (outDir.equals(""))
			outDir = userHomeS;

		if (outDir.endsWith("/"))
			filepathStart = outDir + filenamePrefix;
		else
			filepathStart = outDir + File.separator + filenamePrefix;
	}

	public void run() {
		Messages.setLocale("en");
		persist = PersistenceControllerFactory.getNewPersistenceController(server, user, password);
		if (persist == null) {
			finish(de.uib.configed.ErrorCode.INITIALIZATION_ERROR);
		}

		if (persist.getConnectionState().getState() != ConnectionState.CONNECTED) {
			finish(de.uib.configed.ErrorCode.CONNECTION_ERROR);
		}

		logging.info(this, "starting");

		try {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(clientsFile));
				logging.info(this, " in " + in);
				String line = in.readLine();
				while (line != null) {
					// we assume that each line is a hostId
					setHost(line);
					updateModel();

					setWriteToFile(filepathStart + line + getExtension());

					logging.debug(" outDir: " + outDir);
					logging.debug(" filePath: " + filepathStart + line + getExtension());
					export();

					line = in.readLine();
				}
			} finally {
				if (in != null)
					in.close();
			}
		} catch (IOException iox) {
			logging.warning(this, "IOException " + iox);
		}

	}

	public void finish(int exitcode) {
		logging.error(de.uib.configed.ErrorCode.tell(exitcode));
		configed.endApp(exitcode);
	}

	public void setWriteToFile(String path) {
		exportFilename = path;

	}

	public void setHost(String hostId) {
		if (modelSWInfo == null) {
			initModel(hostId);
		}

		else {
			theHost = hostId;
			updateModel();
			// modelSWInfo.requestReload();
			// smodelSWInfo.reset();
		}

		setWriteToFile(filepathStart + hostId + ".pdf");

	}

	protected void initModel(String hostId) {
		theHost = hostId;

		exportDirectoryS = "";
		if (exportDirectory == null) {
			try {
				exportDirectory = new File(System.getProperty(logging.envVariableForUserDirectory));
			} catch (Exception ex) {
				logging.warning(this, "could not define exportDirectory)");
			}
		}

		if (exportDirectory != null)
			exportDirectoryS = exportDirectory.toString();

		// filepathStart = exportDirectoryS + File.separator + prefix;
		// setWriteToFile( filepathStart + hostId + ".pdf");

		Vector<String> columnNames;
		Vector<String> classNames;

		columnNames = new Vector<>(SWAuditClientEntry.KEYS);
		columnNames.remove(0);
		classNames = new Vector<>();
		int[] finalColumns = new int[columnNames.size()];
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
			finalColumns[i] = i;
		}

		modelSWInfo = new GenTableModel(null, // no updates
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public Map<String, Map> retrieveMap() {
						logging.info(this, "retrieving data for " + theHost);
						Map<String, Map> tableData = persist.retrieveSoftwareAuditData(theHost);

						if (tableData == null || tableData.keySet().isEmpty()) {
							scanInfo = de.uib.configed.configed.getResourceValue("PanelSWInfo.noScanResult");
						} else {
							logging.debug(this, "retrieved size  " + tableData.keySet().size());
							scanInfo = "Scan " + persist.getLastSoftwareAuditModification(theHost);
						}

						logging.info(this, "retrieved size  " + tableData.keySet().size());
						return tableData;
					}
				})), -1, finalColumns, null, null);

		;

	}

	public abstract void export();

	protected abstract String getExtension();

	public void updateModel() {
		logging.info(this, "update++");

		// logging.info(this, "update+++++ voidTableModel.getRowCount() "
		// +voidTableModel.getRowCount() );
		logging.info(this, "update++++ modelSWInfo.getRowCount() " + modelSWInfo.getRowCount());

		modelSWInfo.requestReload();
		modelSWInfo.reset();
		logging.info(this, "update++++++ modelSWInfo.getRowCount() " + modelSWInfo.getRowCount());
		// panelTable.reload();

	}

}