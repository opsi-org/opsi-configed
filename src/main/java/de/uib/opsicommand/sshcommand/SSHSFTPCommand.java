/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

/**
 * Represent a sshcommand object
 **/
public interface SSHSFTPCommand {
	String getId();

	String getDescription();

	String getTitle();

	String getSourcePath();

	String getFullSourcePath();

	String getSourceFilename();

	String getFullTargetPath();

	String getTargetPath();

	String getTargetFilename();

	boolean isOverwriteMode();

	boolean isShowOutputDialog();

	void setTitle(String t);

	void setDescription(String d);

	void setSourcePath(String p);

	void setFullSourcePath(String p);

	void setSourceFilename(String f);

	void setTargetPath(String p);

	void setTargetFilename(String f);

	void setOverwriteMode(boolean o);
}
