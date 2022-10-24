package de.uib.configed.gui.ssh;

import de.uib.configed.gui.*;
import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.util.regex.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import javax.swing.border.LineBorder.*;

public class SSHPMInstallPanel extends JPanel {
	public boolean isOpen = false;
	protected int PREF = GroupLayout.PREFERRED_SIZE;
	protected int MAX = Short.MAX_VALUE;
	protected GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
	protected GroupLayout.Alignment center = GroupLayout.Alignment.CENTER;
	protected GroupLayout.Alignment baseline = GroupLayout.Alignment.BASELINE;

	protected SSHCommandFactory factory = SSHCommandFactory.getInstance();

	protected Vector<String> additional_default_paths = new Vector();

	PersistenceController persist;
	protected String workbench;
	public SSHPMInstallPanel() {
		this.setBackground(Globals.backLightBlue);
		additional_default_paths.addElement(factory.opsipathVarRepository);
		persist = PersistenceControllerFactory.getPersistenceController();
		if (persist == null) logging.info(this, "init PersistenceController null");
		workbench = persist.configedWORKBENCH_defaultvalue;
		if (workbench.charAt(workbench.length()-1) != '/')
			workbench = workbench + "/";
	}


	public void open() {
		if (!isOpen) {
			this.setSize(
				this.getWidth(),
				this.getHeight() + this.getHeight()
			);
			isOpen = true;
			this.setVisible(isOpen);
		}
	}
	public void close() {
		if (isOpen) {
			this.setSize(
				this.getWidth(),
				this.getHeight() - this.getHeight()
			);
			isOpen = false;
			this.setVisible(isOpen);
		}
	}
}