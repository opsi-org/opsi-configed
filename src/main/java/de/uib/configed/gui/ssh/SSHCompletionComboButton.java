package de.uib.configed.gui.ssh;

import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.logging.*;

// Verwendung in Beisspielsweise SSHOpsiSetRightsDialog.java
public class SSHCompletionComboButton
{
	private JComboBox combobox;
	private JButton button;
	private JTextField textfield;
	private Vector<String> defaultvalues;
	private String search_specific_files;
	private String combobox_default_path;
	private final String root = "/";
	private final String home = "~";
	private String opsiRepo = "/"; // will be overwritten with config
	// private String opsiVarRepository = "/var/lib/opsi/repository/";
	private String basic_search_path;

	private SSHCommandFactory factory = SSHCommandFactory.getInstance();
	private PersistenceController persist = PersistenceControllerFactory.getPersistenceController();
	public SSHCompletionComboButton()
	{
		this(null, null, null);
	}
	public SSHCompletionComboButton(Vector<String> values)
	{
		this(values, null, null);
	}

	public SSHCompletionComboButton(Vector<String> values, String search_specific_files, String combobox_default_path)
	{
		logging.info(this, "instance created");
		this.search_specific_files = search_specific_files;
		this.combobox_default_path = combobox_default_path;
		init(values);
		createInstances();
		initTextfield();
		if (combobox_default_path != null)
			textfield.setText(combobox_default_path);
		initCombobox();
		initButton();
	}
	public SSHCompletionComboButton(String search_specific_files, String combobox_default_path) ///* ".opsi" */
	{
		this(null, search_specific_files, combobox_default_path);
	}
	public SSHCompletionComboButton(String search_specific_files) ///* ".opsi" */
	{
		this(null, search_specific_files, null);
	}

	final private void enableComponents(boolean value)
	{
		combobox.setEnabled(value);
		button.setEnabled(value);
	}

	public String getBasicPath()
	{
		basic_search_path = (String) combobox.getSelectedItem();
		if (basic_search_path != null)
			return basic_search_path.trim();
		return "";
	}

	private void init()
	{
		init(null);
	}

	private void init(Vector<String> defvalues)
	{
		if (persist == null) logging.info(this, "init PersistenceController null");
		else  opsiRepo = persist.configedWORKBENCH_defaultvalue;
		if (opsiRepo.charAt(opsiRepo.length()-1) != '/')
			opsiRepo = opsiRepo + "/";
		if (combobox_default_path != null)
		{
			defaultvalues =  new Vector();
			defaultvalues.addElement(combobox_default_path);
			defaultvalues.addElement(root);
			defaultvalues.addElement(opsiRepo);
			// defaultvalues.addElement(opsiVarRepository);
		}
		else
		{
			defaultvalues =  new Vector();
			defaultvalues.addElement(opsiRepo);
			defaultvalues.addElement(root);
			// defaultvalues.addElement(opsiVarRepository);
		}
		// Is element in defaultValues?
		if (defvalues != null)
			for (String elem : defvalues)
				if ((elem != null) && (!elem.trim().equals("")))
					defaultvalues.addElement(elem);
		logging.info(this, "init =======================================");
		for (String elem : defaultvalues)
			logging.debug(this, "init defaultvalues contains " + elem);
	}
	public Vector getDefaultValues() {
		return defaultvalues;
	}
	private void initTextfield()
	{
		textfield = new JTextField();
		textfield.setBackground(Globals.backLightYellow);
	}
	private void createInstances() {
		button = new JButton();
		combobox = new SSHCompletionComboBox(new DefaultComboBoxModel(defaultvalues.toArray()));
	}

	public void initCombobox()
	{
		combobox.setEnabled(false);
		combobox.setRenderer(new ItemElementListener(this));
		combobox.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.cb_serverDir.tooltip"));
		combobox.setEditable(true);

		if (combobox_default_path != null)
			combobox.setSelectedItem(combobox_default_path);

		if (search_specific_files != null && (!search_specific_files.equals("")))
			combobox.addActionListener(new ActionListener(){
			                           public void actionPerformed(ActionEvent e)
			                           {
				                           if (
				                               combobox.getSelectedItem() != null
				                               &&
				                               ((String) combobox.getSelectedItem()).endsWith(search_specific_files)
				                           )
					                           textfield.setText((String) combobox.getSelectedItem());
				                           else
					                           textfield.setText("");
				                           combobox.setSelectedItem(combobox.getSelectedItem());
			                           }
		                           });
		combobox.setMaximumRowCount(Globals.comboBoxRowCount);
	}

	private void initButton()
	{
		button.setText(configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button"));
		button.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button.tooltip"));
		button.addActionListener(new ActionListener() {
			                         public void actionPerformed(ActionEvent e)
			                         { doButtonAction(); }
		                         });
	}
	public void doButtonAction()
	{
		enableComponents(false);

		String strcbtext = combobox.getEditor().getItem().toString();
		if ( (strcbtext != null) && (!strcbtext.equals("")))
			if (! strcbtext.substring(strcbtext.length() - 1).equals(root))
			{
				combobox.removeItem(strcbtext);
				logging.info(this, "doButtonAction combo.removeItem(" + strcbtext+ ")");
				strcbtext = strcbtext + root;
				combobox.addItem(strcbtext);
				logging.info(this, "doButtonAction combo.additem(" + strcbtext+ ")");
				combobox.setSelectedItem(strcbtext);
			}

		if (search_specific_files != null && (!search_specific_files.equals("")))
			getDirectoriesAndFilesIn(strcbtext);
		else getDirectoriesIn(strcbtext);
		setComboDefault(null);
	}

	public void setSearchSpecificFiles(String file_end_str)
{search_specific_files = file_end_str;}

	public void setComboDefault(String value)
	{combobox_default_path = value;}


	public JTextField getTextField()
	{return textfield;}

	public JButton getButton()
	{return button;}

	public JComboBox getCombobox()
	{return combobox;}
	public void setCombobox(JComboBox cb)
	{combobox = cb;}

	public String combobox_getStringItem()
	{
		if (combobox.getEditor().getItem().toString().startsWith("/"))
			return combobox.getEditor().getItem().toString();
		else
			return getBasicPath() +  combobox.getEditor().getItem().toString();
	}

	private void getDirectoriesIn(final String curdir)
	{
		new Thread() {
			public void run() {
				try {
					Empty_Command getDirectories = new Empty_Command(factory.str_command_getDirectories.replace(factory.str_replacement_dir, curdir)){
						                               /** Sets the command specific error text **/
						                               @Override
						                               public String get_ERROR_TEXT()
						                               {
							                               return root;
						                               }
					                               };
					SSHConnectExec ssh = new SSHConnectExec();
					String result = ssh.exec(getDirectories, false);
					if (result == null || result.equals(""))
						result = home;

					setItems(result, curdir);
					enableComponents(true);
				}
				catch (Exception e)
				{logging.logTrace(e);}
			}
		}.start();
	}
	private void getDirectoriesAndFilesIn(final String curdir)
	{
		new Thread() {
			public void run() {
				try {
					Empty_Command getFiles = new Empty_Command(
					                             factory.str_command_getDirectories.replace(factory.str_replacement_dir, curdir));
					SSHConnectExec ssh = new SSHConnectExec();
					String result = ssh.exec(getFiles, false);
					if (result == null || result.equals(""))
						result = root;

					getFiles = new Empty_Command(
					               factory.str_command_getOpsiFiles.replace(factory.str_replacement_dir, curdir)){
						           /** Sets the command specific error text **/
						           @Override
						           public String get_ERROR_TEXT()
						           {
							           return root; //no file found
						           }
					           };
					try {
						////// FUNKTIONIERT NUR WENN BERECHTIGUNGEN RICHTIG SIND.....
						// Bricht nach nächster Bedingung ab und schreibt keinen result  ---> try-catch
						String tmp_result = ssh.exec(getFiles, false);
						if ((tmp_result != null) || (tmp_result.trim() != "null"))
							result += tmp_result;
					}
					catch (Exception ei)
					{
						logging.warning(this, "Could not find .opsi files in directory " + curdir + " (It may be the rights are setted wrong.)");
					}
					setItems(result, curdir);
					enableComponents(true);
				}
				catch (Exception e)
				{
					logging.logTrace(e);
				}
			}
		}.start();
	}

	private boolean contains_in_defaults(String other)
	{
		boolean contains = defaultvalues.contains(other);
		logging.debug(this, "contains_in_defaults defaultvalues.contains_in_defaults(" + other+") = " + contains);
		return contains;
	}

	final private void setItems(String result, final String curdir)
	{
		if (result == null)
		{
			logging.warning("getDirectoriesIn could not find directories in " + curdir );
		}
		else
		{
			combobox.removeAllItems();
			for (String element : defaultvalues)
			{
				combobox.addItem(element);
				logging.debug(this, "setItems add " + element);
			}
			String curDirLocated = new String(curdir);
			if (!contains_in_defaults(curDirLocated))
				combobox.addItem(curDirLocated);
			logging.debug(this, "setItems add " + curDirLocated);
			for (String item : result.split("\n"))
			{
				logging.debug(this, "setItems add " + item);
				if (item.contains("//"))
					combobox.addItem(item.replace("//", "/"));
				else
					combobox.addItem(item); // .replace("//", "/")
			}
			combobox.setSelectedItem(curdir);
		}
		if (combobox_default_path != null && !combobox_default_path.equals(""))
		{
			combobox.setSelectedItem(combobox_default_path);
			setComboDefault(null);
		}
	}

	public class ItemElementListener extends DefaultListCellRenderer
	{
		protected int FILL_LENGTH = 20;
		protected SSHCompletionComboButton autocompletion;

		public ItemElementListener(SSHCompletionComboButton autocompletion)
		{
			super();
			this.autocompletion = autocompletion;
		}
		public ItemElementListener()
		{
			super();
		}

		public Component getListCellRendererComponent(
		    JList list,
		    Object value,            // value to display
		    int index,               // cell index
		    boolean isSelected,      // is the cell selected
		    boolean cellHasFocus)    // the list and the cell have the focus
		{
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			logging.debug(this, "getListCellRendererComponent called");

			if (c == null || !(c instanceof JComponent))
				return c;

			JComponent  jc = (JComponent) c;

			if (jc instanceof JLabel)
			{
				JLabel lbl = (JLabel) jc;
				String getText = ((JLabel)jc).getText();
				if (autocompletion==null || getText == null  || getText.equals(""))
					return c;
				getText = getText.trim();
				String basicPath = autocompletion.getBasicPath();
				logging.debug(this, "(1)  basicPath " + basicPath + " getText " + getText);

				if  ( (!basicPath.equals(""))
				        && (!getText.equals("")) ) // könnte eigtl raus. funktiniert sonst aber nicht...
				{
					if (basicPath.contains("//"))
						basicPath = basicPath.replaceAll("//", "/");
					if (getText.contains("//"))
						getText = getText.replaceAll("//", "/");


					if (getText.equals(basicPath) || autocompletion.contains_in_defaults(getText))
					{
						logging.debug(this, "getListCellRendererComponent colorize(" + getText + ") = true");
						CellAlternatingColorizer.colorize(jc, isSelected, true, true);
					}

					if (  (getText.startsWith(basicPath))
					        && (!getText.equals(basicPath))
					        && (!basicPath.equals(root))
					   )
					{
						lbl.setText(getText.replace(basicPath, ""));
					}
					logging.debug(this, "(2) basicPath " + basicPath + " getText " + getText);
				}
			}
			return jc;
		}
	}
}