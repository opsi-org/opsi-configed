package de.uib.opsicommand.sshcommand;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2016 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */

import java.util.*;
import java.lang.*;
// import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.utilities.logging.*;
// import de.uib.configed.gui.ssh.*;

/**
* This class represent a ssh-command 
**/
public class SSHCommand_Template implements SSHCommand, 
	Comparable<SSHCommand_Template>, SSHMultiCommand
{
	/** boolean needParameter = false **/
	private boolean needParameter = false;
	/** boolean isMultiCommand = true **/
	private boolean isMultiCommand = true;
	/** String unique command id **/
	private String id;
	/** String unique menu text **/
	private String menuText;
	/** LinkedList<SSHCommand> ssh_command **/
	private LinkedList<SSHCommand> ssh_command = new LinkedList<SSHCommand>();
	private LinkedList<SSHCommand> ssh_command_original = new LinkedList<SSHCommand>();
	/** boolean needSudo state **/
	private boolean needSudo = false;
	/** String parent menu text**/
	private String parentMenuText = null;
	/** String tooltip text **/
	private String tooltipText = "";
	/** integer position **/
	private int position;
	/** instance of SSHCommandFactory */
	private SSHCommandFactory factory = SSHCommandFactory.getInstance();
	private String confidential_information = null;
	/**
	* Creates an empty SSHCommand_Template instance
	* @return SSHCommand_Template instance
	*/
	public SSHCommand_Template()
	{
		position = factory.position_default;
	}
	/**
	* Creates an SSHCommand_Template instance with given parameter
	* @param id : String
	* @param c (commands): LinkedList<String>
	* @param mt (menu text): String
	* @param ns (needSudo): boolean
	* @param pmt (parent menu text) : String
	* @param ttt (tooltip text): String
	* @param p (position): int
	* @return SSHCommand_Template instance 
	*/
	public SSHCommand_Template(String id, LinkedList<String> c, String mt, 
		boolean ns, String pmt, String ttt, int p)
	{
		position = SSHCommandFactory.getInstance().position_default;
		setId(id);
		setMenuText(mt);
		setNeedSudo(ns);
		setParentMenuText(pmt);
		setTooltipText(ttt);
		setPriority(p);
		setCommands(c);
		logging.debug(this, "SSHCommand_Template this " + this.toString());
		logging.debug(this, "SSHCommand_Template commandlist" + this.commandlistToString());
	}
	public SSHCommand_Template(SSHCommand orig, LinkedList<String> commandlist)
	{
		this(orig.getId(), commandlist, orig.getMenuText(), orig.needSudo(), orig.getParentMenuText(), 
			orig.getToolTipText(), orig.getPriority());
		logging.debug(this, "SSHCommand_Template this " + this.toString());
		logging.debug(this, "SSHCommand_Template commandlist" + this.commandlistToString());
	}


	@Override 
	/** 
	* Sets the command specific error text
	**/
	public String get_ERROR_TEXT()
	{
		return "ERROR";
	}

	private String mainName = "";
	@Override
	public String getMainName()
	{return mainName;}
	public void setMainName(String n)
	{ mainName = n;}
	
	
	/** 
	* Sets the Id
	* @param i (id): String
	**/
	public void setId(String i)
	{ id = i;}

	private boolean firstInitCommands = true;
	/** 
	* Sets the given commandlist
	* @param c_list: LinkedList<String>
	**/
	public void setCommands(LinkedList<String> c_list)
	{
		if (c_list !=  null)
		{
			for (String c: c_list)
			{
				SSHCommand sshc = new Empty_Command( getId(),  c,  getMenuText(),  needSudo());
				ssh_command.add(sshc);
				if (firstInitCommands)
					ssh_command_original.add(sshc);
			}
			firstInitCommands=false;
		}
	}

	/** 
	* Sets the given command
	* @param c (command): String
	**/
	public void setCommand(String c)
	{  }
	/** 
	* Add given SSHCommand to commandlist
	* @param sshc: SSHCommand
	**/
	public void addCommand(SSHCommand sshc)
	{ 
		ssh_command.add(sshc);
		ssh_command_original.add(sshc);
	}
	@Override
	public String getSecureInfoInCommand()
	{
		return confidential_information; // usually null;
	}
	@Override 
	public String getSecuredCommand()
	{
		if ( (getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().equals("")))
			return 	getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.getInstance().confidential);
		else return getCommand();
	}

	/** 
	* Sets the menu text
	* @param mt (menu text): String
	**/
	public void setMenuText(String mt)
	{ menuText = mt;}

	/** 
	* Sets the parent menu text
	* @param pmt (parent menu text): String
	**/
	public void setParentMenuText(String pmt)
	{ parentMenuText = pmt;}

	/** 
	* Sets the tooltip text
	* @param ttt (tooltip text): String
	**/
	public void setTooltipText(String ttt)
	{ tooltipText = ttt;}

	/** 
	* Sets the need sudo state
	* @param ns (needSudo): boolean
	**/
	public void setNeedSudo(boolean ns)
	{ needSudo = ns;}

	/** 
	* Sets the position
	* @param p (position): int
	**/
	public void setPriority(int p)
	{ position = p;}

	/**
	* Get the command id
	* @return id 
	**/
	@Override
	public String getId()
	{ return id; }
	
	/**
	* Get the trimmed command menutext
	* @return menuText
	**/
	@Override
	public String getMenuText()
	{
		if (menuText!=null)
			if (menuText.length() > 0)
				return menuText.trim() ; 
		return menuText;
	}

	/**
	* Get the command parent menutext
	* @return parentMenuText
	**/
	@Override
	public String getParentMenuText()
	{ return parentMenuText ; }

	/**
	* Get the command tooltip text
	* @return tooltip text
	**/
	@Override
	public String getToolTipText()
	{ return tooltipText ; }

	/**
	* Get the command from sshcommand
	* @return command
	**/
	@Override
	public String getCommand()
	{ return ""; }

	/**
	* Get the all commands in sshcommand 
	* @return LinkedList of SSHCommand 
	**/
	@Override
	public LinkedList<SSHCommand> getCommands()
	{ return ssh_command; }

	public LinkedList<SSHCommand> getOriginalCommands()
	{ return ssh_command_original; }
	
	/**
	* Get the command without parameter from SSHCommand
	* @return command 
	**/
	@Override
	public String getCommandRaw()
	{ return ""; }

	/**
	* @return True if the commands needs sudo
	**/
	@Override
	public boolean needSudo()
	{ return needSudo; }
	/**
	* @return the position 
	*/
	@Override
	public int getPriority()
	{ return position; }

	/**
	* Format the commands(LinkedList<SSHCommands>) to LinkedList<String> 
	* @return LinkedList<String> with the commands
	**/
	@Override
	public LinkedList<String> getCommandsRaw()
	{
		LinkedList<String> commands_string_list = new LinkedList<String>();
		for (SSHCommand c : ssh_command)
		{
			String comstr = c.getCommandRaw();
			if (!((comstr==null) || (comstr.trim().equals(""))))
				commands_string_list.add(c.getCommandRaw());
		}
		return commands_string_list;
	}

	/**
	* @return True if the commands needs Parameter
	**/
	@Override
	public boolean needParameter()
	{ return needParameter; }
	
	/**
	* @return null (SSHCommand_Template does not have a parameter dialog)
	**/
	@Override
	public FGeneralDialog getDialog()
	{ return null; }

	/**
	* @return True
	*/
	@Override
	public boolean isMultiCommand()
	{ return isMultiCommand; }

	/**
	* @return a string representation of this command
	*/
	@Override
	public String toString()
	{
		StringBuffer com = new StringBuffer("{");
			com.append(factory.command_map_id).append(":").append(getId()).append(",");
			com.append(factory.command_map_parentMenuText).append(":").append(getParentMenuText()).append(",");
			com.append(factory.command_map_menuText).append(":").append(getMenuText()).append(",");
			com.append(factory.command_map_tooltipText).append(":").append(getToolTipText()).append(",");
			com.append(factory.command_map_needSudo).append(":").append(needSudo()).append(",");
			com.append(factory.command_map_position).append(":").append(getPriority()).append(", ");
			com.append(factory.command_map_commands).append(":").append("[");
			for (int i=0; i< getCommandsRaw().size(); i++)
			{
				String c = getCommandsRaw().get(i);
				if (i == getCommandsRaw().size()-1) com.append(c);
				else com.append(c).append(",");
			}
			com.append("]");
		com.append("}");
		return com.toString();
	}

	public String commandlistToString()
	{
		StringBuffer commandString = new StringBuffer("[");
		for (int i=0; i< getCommands().size(); i++)
		{
			String c =((Empty_Command)getCommands().get(i)).commandToString();
			if (i == getCommands().size()-1) commandString.append(c);
			else commandString.append(c).append(",");
		}
		commandString.append("]");
		return commandString.toString();
	}

	/**
	* Compares the position of SSHCommand_Templates. If it is equal compare by menuText
	* @param compareCom Compares the compareCom to this command
	* @return difference 
	*/
	@Override
	public int compareTo(SSHCommand_Template compareCom)
	{
		int dif = ((SSHCommand_Template)this).position - ((SSHCommand_Template)compareCom).getPriority();
		if (dif == 0)
			return ((SSHCommand_Template)this).menuText.compareTo( ((SSHCommand_Template)compareCom).getMenuText());
		return dif;
	}
	/**
	* Update all fields of this command to the fields of given command
	* @param SSHCommand_Template com
	* @return the updated command (this)
	*/
	public SSHCommand_Template update(SSHCommand_Template com)
	{
		if (this.id == com.getId())
		{
			logging.debug(this, "update this (" + this.toString() + ") with (" + com.toString() + ")");
			setCommands(com.getCommandsRaw());
			setMenuText(com.getMenuText());
			setNeedSudo(com.needSudo());
			setParentMenuText(com.getParentMenuText());
			setTooltipText(com.getToolTipText());
			setPriority(com.getPriority());
		}
		logging.info(this, "updated command: " + this.toString());
		return this;
	}
	
	public boolean equals(SSHCommand_Template com)
	{
		if (! this.getId().trim().equals(com.getId().trim()))
		{
			logging.debug(this, "equals different id's " + this.getId() + " != " + com.getId() + "");
			return false;
		}
		if (! this.getMenuText().trim().equals(com.getMenuText().trim()))
		{
			logging.debug(this, "equals different menuText's " + this.getMenuText() + " != " + com.getMenuText() + "");
			return false;
		}
		if (! this.getParentMenuText().trim().equals(com.getParentMenuText().trim()))
		{
			logging.debug(this, "equals different parentMenuText's " + this.getParentMenuText() + " != " + com.getParentMenuText() + "");
			return false;
		}
		if (! this.getToolTipText().trim().equals(com.getToolTipText().trim()))
		{
			logging.debug(this, "equals different toolTipText's " + this.getToolTipText() + " != " + com.getToolTipText() + "");
			return false;
		}
		if (this.getPriority() != com.getPriority())
		{
			logging.debug(this, "equals different priorities " + this.getPriority() + " != " + com.getPriority() + "");
			return false;
		}
		if (this.needSudo() != com.needSudo())
		{
			logging.debug(this, "equals different needSudo " + this.needSudo() + " != " + com.needSudo() + "");
			return false;
		}
		if (this.getCommandsRaw().size() != com.getCommandsRaw().size())
		{
			logging.debug(this, "equals different commandlist length " + this.getCommandsRaw().size() + " != " + com.getCommandsRaw().size() + "");
			return false;
		}
		
		for (int i =0; i<this.getCommandsRaw().size(); i++)
		{
			if (! this.getCommandsRaw().get(i).equals(com.getCommandsRaw().get(i)))
			{
				logging.debug(this, "equals different commands " + this.getCommandsRaw() + " != " + com.getCommandsRaw() + "");
				return false;
			}
		}
		logging.debug(this, "equals commands are equal");
		return true;

	}


	/**
	* @return null 
	*/
	@Override
	public ArrayList<String> getParameterList()
	{
		return null;
	}
}