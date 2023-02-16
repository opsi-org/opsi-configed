package de.uib.configed.gui.ssh;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class SSHPMInstallPanel extends JPanel {
	public boolean isOpen = false;

	protected GroupLayout.Alignment center = GroupLayout.Alignment.CENTER;
	protected GroupLayout.Alignment baseline = GroupLayout.Alignment.BASELINE;

	protected List<String> additionalDefaultPaths = new ArrayList<>();

	AbstractPersistenceController persist;
	protected String workbench;

	public SSHPMInstallPanel() {
		super.setBackground(Globals.BACKGROUND_COLOR_7);

		additionalDefaultPaths.add(SSHCommandFactory.OPSI_PATH_VAR_REPOSITORY);
		persist = PersistenceControllerFactory.getPersistenceController();
		if (persist == null) {
			Logging.info(this, "init PersistenceController null");
		}
		workbench = AbstractPersistenceController.configedWorkbenchDefaultValue;
		if (workbench.charAt(workbench.length() - 1) != '/') {
			workbench = workbench + "/";
		}
	}

	public void open() {
		if (!isOpen) {
			this.setSize(this.getWidth(), this.getHeight() + this.getHeight());
			isOpen = true;
			this.setVisible(isOpen);
		}
	}

	public void close() {
		if (isOpen) {
			this.setSize(this.getWidth(), 0);
			isOpen = false;
			this.setVisible(isOpen);
		}
	}
}