package de.uib.configed.gui.ssh;

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
import de.uib.configed.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;

public class SSHRepositoryUploadDialog extends SSHFileUploadDialog
{
	static private String title = configed.getResourceValue("SSHConnection.ParameterDialog.repoupload.title");
	public SSHRepositoryUploadDialog()
	{
		this(null);
	}
	public SSHRepositoryUploadDialog(CommandRepositoryUpload com)
	{
		super(title, new CommandRepositoryUpload());
		this.setVisible (true);
		logging.info(this, "SSHRepositoryUploadDialog build");
		height = 400;
		showDialog();
	}


	@Override
	protected String doAction1_additional_setPath()
	{
		String modules_server_path = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(filechooser_local.getSelectedFile().getName());
		return modules_server_path;
	}
}