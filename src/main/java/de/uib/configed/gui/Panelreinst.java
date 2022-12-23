package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.uib.configed.Globals;

import de.uib.utilities.swing.SurroundPanel;

public class Panelreinst extends JPanel implements KeyListener, MouseListener, ActionListener {
	final String callReinstmgr = "reinstmgr";
	// private String unsetCommand = "";
	// private String installCommand = "";

	String pcName = "";

	final int fieldHeight = 21;
	JPanel panelContent = new JPanel();
	JPanel panelVerticalGrid = new JPanel();

	JLabel labelInstallation = new JLabel();
	JPanel panelSurroundSelectImage = new SurroundPanel();

	JLabel labelSelectImage = new JLabel();
	JComboBox comboImages = new JComboBox<>();
	JLabel labelEthernetAdress = new JLabel();
	JTextField fieldEthernetAddress = new JTextField();
	JPanel panelSurroundEthernetAddress = new SurroundPanel();

	JTextField fieldInstallCommand = new JTextField();

	JButton buttonStartReinstmgr = new JButton();

	JLabel labelUnset = new JLabel();
	JTextField fieldUnsetCommand = new JTextField();
	JButton buttonUnset = new JButton();

	JTextArea textOut = new JTextArea();

	

	public Panelreinst() {

		guiInit();

	}

	protected void guiInit() {
		panelContent.setBackground(Globals.backgroundWhite);

		panelContent.setLayout(new BorderLayout());
		panelContent.setOpaque(true);
		panelVerticalGrid.setLayout(new GridLayout(12, 1));
		panelVerticalGrid.setOpaque(false);

		panelVerticalGrid.add(new JLabel(""));

		labelInstallation.setText("Installation des PCs anstoßen");
		labelInstallation.setFont(Globals.defaultFontBig);
		panelVerticalGrid.add(new SurroundPanel(labelInstallation));

		labelSelectImage.setText("Image-Auswahl");
		labelSelectImage.setBackground(Globals.backgroundWhite);
		panelSurroundSelectImage.add(labelSelectImage);
		comboImages.setBackground(Color.WHITE);
		comboImages.addActionListener(this);
		panelSurroundSelectImage.add(comboImages);
		panelSurroundSelectImage.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelVerticalGrid.add(panelSurroundSelectImage);

		labelEthernetAdress.setText("falls erforderlich, Ethernet-(MAC-) Adresse des PCs:");
		fieldEthernetAddress.setText("  ");
		fieldEthernetAddress.setColumns(18);
		fieldEthernetAddress.setPreferredSize(new Dimension(100, fieldHeight));
		fieldEthernetAddress.addKeyListener(this);
		fieldEthernetAddress.addMouseListener(this);
		panelSurroundEthernetAddress.add(labelEthernetAdress);
		panelSurroundEthernetAddress.add(fieldEthernetAddress);
		panelVerticalGrid.add(panelSurroundEthernetAddress);

		fieldInstallCommand.setText(callReinstmgr);
		fieldInstallCommand.setPreferredSize(new Dimension(350, fieldHeight));
		fieldInstallCommand.setBackground(Globals.backgroundGrey);
		fieldInstallCommand.setEditable(true);
		panelVerticalGrid.add(new SurroundPanel(fieldInstallCommand));

		buttonStartReinstmgr.setText("Ausführen");
		buttonStartReinstmgr.addActionListener(this);
		buttonStartReinstmgr.setPreferredSize(new Dimension(100, fieldHeight));
		buttonStartReinstmgr.setBackground(Globals.backBlue);
		panelVerticalGrid.add(new SurroundPanel(buttonStartReinstmgr));

		panelVerticalGrid.add(new JLabel(""));

		labelUnset.setText("PC soll \"normal\" starten");
		labelUnset.setFont(Globals.defaultFontBig);
		panelVerticalGrid.add(new SurroundPanel(labelUnset));

		fieldUnsetCommand.setText(callReinstmgr);
		fieldUnsetCommand.setPreferredSize(new Dimension(350, fieldHeight));
		fieldUnsetCommand.setBackground(Globals.backgroundGrey);
		fieldUnsetCommand.setEditable(true);
		panelVerticalGrid.add(new SurroundPanel(fieldUnsetCommand));

		buttonUnset.setText("Ausführen");
		buttonUnset.addActionListener(this);
		buttonUnset.setBackground(Globals.backBlue);
		buttonUnset.setPreferredSize(new Dimension(100, fieldHeight));
		panelVerticalGrid.add(new SurroundPanel(buttonUnset));

		panelVerticalGrid.add(new JLabel(""));

		panelContent.add(new SurroundPanel(panelVerticalGrid), BorderLayout.CENTER);

		textOut.setColumns(70);
		textOut.setRows(4);
		textOut.setBackground(Globals.backgroundGrey);

		textOut.setLineWrap(false);
		textOut.setEditable(false);
		panelContent.add(new SurroundPanel(new JScrollPane(textOut, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)), BorderLayout.SOUTH);

		add(panelContent);
	}

	protected void buildInstallCommand() {
		String installCommand = callReinstmgr + " " + (String) comboImages.getSelectedItem() + " " + pcName
				+ fieldEthernetAddress.getText();
		fieldInstallCommand.setText(installCommand);
		fieldInstallCommand.setCaretPosition(0);
	}

	protected void executeReinstmgr(String command) {

		/*
		 * textOut.append ( PersistenceController.getPersistenceController().execRemote
		 * (command) );
		 * 
		 * if
		 * (!PersistenceController.getPersistenceController().getLastError().equals(""))
		 * {
		 * JOptionPane.showMessageDialog(this,
		 * PersistenceController.getPersistenceController().getLastError(), "Warnung",
		 * JOptionPane.WARNING_MESSAGE);
		 * }
		 */

	}

	public void startFor(String pcName, Vector installImages) {
		this.pcName = pcName;
		comboImages.setModel(new DefaultComboBoxModel<>(installImages));
		String installCommand = callReinstmgr;
		fieldInstallCommand.setText(installCommand);
		String unsetCommand = callReinstmgr + " unset " + pcName;
		fieldUnsetCommand.setText(unsetCommand);
	}

	// implementation of ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == comboImages) {
			buildInstallCommand();
		} else if (e.getSource() == buttonStartReinstmgr) {
			executeReinstmgr(fieldInstallCommand.getText());
		} else if (e.getSource() == buttonUnset) {
			executeReinstmgr(fieldUnsetCommand.getText());
		}

	}

	// implementation of KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		buildInstallCommand();
	}

	// implementation of MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		buildInstallCommand();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}
