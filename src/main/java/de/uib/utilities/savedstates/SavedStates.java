package de.uib.utilities.savedstates;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.PropertiesStore;
import de.uib.utilities.logging.Logging;

public class SavedStates extends PropertiesStore {
	public SaveInteger savedMaxShownLogLevel;
	public SaveInteger saveUsageCount;
	public SaveBoolean saveRegisterUser;

	public SaveDepotSelection saveDepotSelection;
	public SaveString saveGroupSelection;
	public SaveString saveSWauditKindOfExport;
	public SaveString saveSWauditExportDir;
	public SaveString saveSWauditExportFilePrefix;

	public SessionSaveSet<String> saveLocalbootproductFilter;
	public SessionSaveSet<String> saveNetbootproductFilter;

	// up to now not used

	public Map<String, SaveString> saveServerConfigs;

	public SavedStates(File store) {
		super(store);
		savedMaxShownLogLevel = new SaveInteger("savedMaxShownLogLevel", 0, this);
		saveUsageCount = new SaveInteger("saveUsageCount", 0, this);
		saveDepotSelection = new SaveDepotSelection(this);
		saveGroupSelection = new SaveString("groupname", this);
		saveSWauditKindOfExport = new SaveString("swaudit_kind_of_export", this);
		saveSWauditExportDir = new SaveString("swaudit_export_dir", this);
		saveSWauditExportFilePrefix = new SaveString("swaudit_export_file_prefix", this);

		saveRegisterUser = new SaveBoolean(AbstractPersistenceController.KEY_USER_REGISTER, false, this);
		// we memorize it locally in order to signal if the config has changed
		saveLocalbootproductFilter = new SessionSaveSet<>();
		saveNetbootproductFilter = new SessionSaveSet<>();

		saveServerConfigs = new HashMap<>();

	}

	public void store() {
		try {
			super.store(null);
		} catch (IOException iox) {
			Logging.warning(this, "could not store saved states, " + iox);
		}
	}

}
