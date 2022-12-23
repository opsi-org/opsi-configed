package de.uib.opsicommand.sshcommand;

/**
 * Represent a sshcommand object
 **/
public abstract interface SSHSFTPCommand {
	public abstract String getId();

	public abstract String getDescription();

	public abstract String getTitle();

	public abstract String getSourcePath();

	public abstract String getFullSourcePath();

	public abstract String getSourceFilename();

	public abstract String getFullTargetPath();

	public abstract String getTargetPath();

	public abstract String getTargetFilename();

	public abstract boolean getOverwriteMode();

	public abstract boolean getShowOutputDialog();

	public void setTitle(String t);

	public void setDescription(String d);

	public void setSourcePath(String p);

	public void setFullSourcePath(String p);

	public void setSourceFilename(String f);

	public void setTargetPath(String p);

	public void setTargetFilename(String f);

	public void setOverwriteMode(boolean o);
	
	// /** @return command String to execute **/
	
	
	// 
	

	// /** @return raw command String **/
	
	// /** @returnlist of parameter-Ersatz **/
	
	// /** @return True if command need sudo **/
	
	// /** @return command id String **/
	
	// 
	// /** @return command menu text String **/
	
	// /** @return command parent menu text String **/
	
	// /** @return command tooltip text String **/
	
	// /** @return command priority int **/
	
	

	// /** @return True if command needs an parameter gui **/
	
	// /** @return True if command is a multicommand **/
	
	// 
	// /** @return the command dialog(parameter)**/
	

	
}