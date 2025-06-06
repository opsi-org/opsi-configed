package de.uib.configed.productaction;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.utils.WebDAVClient;
import de.uib.utils.WinProductUtils;
import de.uib.utils.logging.Logging;

/**
 * Generic background worker for uploading files/directories and optionally
 * updating product properties. Can be used by both CompleteWinProductsDialog
 * and PanelDriverUpload.
 */
public class GenericUploadWorker extends SwingWorker<Void, Void> {
	public static class Context {
		public JDialog dialog;
		public JButton executeButton;
		public JTextField targetPathField;
		public JTextField winPEField;
		public JTextField installFilesField;
		public JTextField productKeyField;
		public JLabel depotLabel;
		public String winProduct;
		public OpsiServiceNOMPersistenceController persistenceController;
		public WebDAVClient webDAVClient;
		public boolean handleProductKey = false;
		public String productKeyPropertyName = "productkey";
	}

	private final Context ctx;

	public GenericUploadWorker(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Void doInBackground() {
		try {
			setWaitCursor(true);
			if (ctx.executeButton != null) {
				ctx.executeButton.setEnabled(false);
			}

			String targetDirectory = ctx.targetPathField.getText();

			// Upload WinPE files if field is present and not empty
			if (ctx.winPEField != null) {
				String pathWinPE = ctx.winPEField.getText().trim();
				if (!pathWinPE.isEmpty()) {
					Logging.debug(this, "copy ", pathWinPE, " to ", targetDirectory);
					WinProductUtils.uploadFileOrDirectory(ctx.webDAVClient, new File(pathWinPE), targetDirectory);
				}
			}

			// Upload InstallFiles if field is present and not empty
			if (ctx.installFilesField != null) {
				String pathInstallFiles = ctx.installFilesField.getText().trim();
				if (!pathInstallFiles.isEmpty()) {
					Logging.debug(this, "copy ", pathInstallFiles, " to ", targetDirectory);
					WinProductUtils.uploadFileOrDirectory(ctx.webDAVClient, new File(pathInstallFiles),
							targetDirectory);
				}
			}

			// Handle product key if requested
			if (ctx.handleProductKey && ctx.productKeyField != null && ctx.winProduct != null
					&& ctx.depotLabel != null) {
				String productKey = ctx.productKeyField.getText().trim();
				List<String> values = Collections.singletonList(productKey);

				Map<String, Object> propsMap = ctx.persistenceController.getProductDataService().getProductPropertiesPD(
						ctx.persistenceController.getHostInfoCollections().getConfigServer(), ctx.winProduct);
				Logging.debug(this, "getProductproperties ", propsMap);

				String oldProductKey = "";
				if (mapContainsProductKey(propsMap, ctx.productKeyPropertyName)) {
					oldProductKey = (String) ((List<?>) propsMap.get(ctx.productKeyPropertyName)).get(0);
				}

				if (!oldProductKey.equals(productKey)) {
					int returnedOption = JOptionPane.showConfirmDialog(ctx.dialog,
							Configed.getResourceValue("CompleteWinProducts.setChangedProductKey"),
							Configed.getResourceValue("CompleteWinProducts.questionSetProductKey"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

					if (returnedOption == JOptionPane.YES_OPTION) {
						setWaitCursor(true);
						Logging.info(this, "setCommonProductPropertyValue ", ctx.depotLabel.getText(), ", ",
								ctx.winProduct, ", ", values);
						ctx.persistenceController.getProductDataService().setCommonProductPropertyValue(
								Collections.singleton(ctx.depotLabel.getText()), ctx.winProduct,
								ctx.productKeyPropertyName, values);
					}
				}
			}

		} catch (IOException ex) {
			Logging.error(ex, "copy error:\n", ex);
			showErrorDialog("Copy error: " + ex.getMessage());
		} catch (HeadlessException ex) {
			Logging.error(ex, "Headless exception when invoking showOptionDialog");
			showErrorDialog("Headless exception: " + ex.getMessage());
		} finally {
			setWaitCursor(false);
			if (ctx.executeButton != null)
				ctx.executeButton.setEnabled(true);
		}
		return null;
	}

	@Override
	protected void done() {
		setWaitCursor(false);
		if (ctx.executeButton != null) {
			ctx.executeButton.setEnabled(true);
		}
		JOptionPane.showMessageDialog(ctx.dialog, "Ready", Configed.getResourceValue("CompleteWinProduct.reportTitle"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void setWaitCursor(boolean wait) {
		if (ctx.dialog != null) {
			ctx.dialog.setCursor(wait ? Globals.WAIT_CURSOR : null);
		}
	}

	private void showErrorDialog(String message) {
		SwingUtilities.invokeLater(
				() -> JOptionPane.showMessageDialog(ctx.dialog, message, "Error", JOptionPane.ERROR_MESSAGE));
	}

	private static boolean mapContainsProductKey(Map<String, Object> propsMap, String propertyName) {
		if (propsMap == null || !(propsMap.get(propertyName) instanceof List)) {
			return false;
		} else {
			return !((List<?>) propsMap.get(propertyName)).isEmpty()
					&& !"".equals(((List<?>) propsMap.get(propertyName)).get(0));
		}
	}
}
