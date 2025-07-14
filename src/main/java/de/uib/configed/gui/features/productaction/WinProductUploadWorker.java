/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productaction;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.WebDAVClient;
import de.uib.configed.share.WinProductUtils;
import de.uib.configed.share.logging.Logging;

public class WinProductUploadWorker extends SwingWorker<Void, Void> {
	@SuppressWarnings("java:S1104")
	public static class Context {
		public JDialog owner;
		public WebDAVClient webDAVClient;
		public JButton executeButton;
		public String targetDirectory;
		public String pathWinPE;
		public String pathInstallFiles;
		public String productKey;
		public String depot;
		public String winProduct;
	}

	private final Context ctx;
	private final OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public WinProductUploadWorker(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Void doInBackground() {
		try {
			setUIBusy(true);
			uploadFilesIfNeeded();
			handleProductKeyUpdate();
		} catch (IOException ex) {
			Logging.error(ex, "copy error:\n", ex);
			showErrorDialog("Copy error: " + ex.getMessage());
		} catch (HeadlessException ex) {
			Logging.error(ex, "Headless exception when invoking showOptionDialog");
			showErrorDialog("Headless exception: " + ex.getMessage());
		} finally {
			setUIBusy(false);
		}
		return null;
	}

	/**
	 * Sets the UI busy state: wait cursor and execute button enabled/disabled.
	 */
	private void setUIBusy(boolean busy) {
		setWaitCursor(busy);
		ctx.executeButton.setEnabled(!busy);
	}

	/**
	 * Uploads WinPE and InstallFiles if their paths are not empty.
	 */
	private void uploadFilesIfNeeded() throws IOException {
		uploadFileIfNotEmpty(ctx.pathWinPE);
		uploadFileIfNotEmpty(ctx.pathInstallFiles);
	}

	private void uploadFileIfNotEmpty(String path) throws IOException {
		if (path != null && !path.isEmpty()) {
			Logging.debug(this, "copy  ", path, " to ", ctx.targetDirectory);
			WinProductUtils.uploadFileOrDirectory(ctx.webDAVClient, new File(path), ctx.targetDirectory);
		}
	}

	/**
	 * Handles product key update logic: checks if the product key has changed
	 * and prompts the user.
	 */
	private void handleProductKeyUpdate() {
		List<String> values = Collections.singletonList(ctx.productKey);

		Map<String, Object> propsMap = persistenceController.getProductDataService().getProductPropertiesPD(
				persistenceController.getHostInfoCollections().getConfigServer(), ctx.winProduct);
		Logging.debug(this, " getProductproperties ", propsMap);

		String oldProductKey = getOldProductKey(propsMap);

		if (!oldProductKey.equals(ctx.productKey) && confirmProductKeyChange()) {
			setWaitCursor(true);
			Logging.info(this, "setCommonProductPropertyValue ", ctx.depot, ", ", ctx.winProduct, ", ", values);
			persistenceController.getProductDataService().setCommonProductPropertyValue(
					Collections.singleton(ctx.depot), ctx.winProduct, "productkey", values);
		}
	}

	/**
	 * Retrieves the old product key from the properties map.
	 */
	private static String getOldProductKey(Map<String, Object> propsMap) {
		if (mapContainsProductKey(propsMap)) {
			Object keyObj = propsMap.get("productkey");
			if (keyObj instanceof List<?> && !((List<?>) keyObj).isEmpty()) {
				Object first = ((List<?>) keyObj).get(0);
				if (first instanceof String strFirst) {
					return strFirst;
				}
			}
		}
		return "";
	}

	/**
	 * Prompts the user to confirm product key change.
	 */
	private boolean confirmProductKeyChange() {
		int returnedOption = JOptionPane.showConfirmDialog(ctx.owner,
				Configed.getResourceValue("CompleteWinProducts.setChangedProductKey"),
				Configed.getResourceValue("CompleteWinProducts.questionSetProductKey"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return returnedOption == JOptionPane.YES_OPTION;
	}

	@Override
	protected void done() {
		setWaitCursor(false);
		ctx.executeButton.setEnabled(true);
		JOptionPane.showMessageDialog(ctx.owner, Configed.getResourceValue("CompleteWinProduct.reportMessage"),
				Configed.getResourceValue("CompleteWinProduct.reportTitle"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void setWaitCursor(boolean wait) {
		if (ctx.owner != null) {
			ctx.owner.setCursor(wait ? Globals.WAIT_CURSOR : null);
		}
	}

	private void showErrorDialog(String message) {
		SwingUtilities.invokeLater(
				() -> JOptionPane.showMessageDialog(ctx.owner, message, "Error", JOptionPane.ERROR_MESSAGE));
	}

	private static boolean mapContainsProductKey(Map<String, Object> propsMap) {
		if (propsMap == null || !(propsMap.get("productkey") instanceof List)) {
			return false;
		} else {
			return !((List<?>) propsMap.get("productkey")).isEmpty()
					&& !"".equals(((List<?>) propsMap.get("productkey")).get(0));
		}
	}
}
