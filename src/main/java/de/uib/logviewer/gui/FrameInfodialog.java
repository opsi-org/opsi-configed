package de.uib.logviewer.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.swing.CenterPositioner;
import de.uib.utilities.swing.VerticalPositioner;

public class FrameInfodialog extends JDialog implements ActionListener, KeyListener {
	private JPanel infoPanel;
	private VerticalPositioner textPanel;
	private JButton button1 = new JButton();
	private JTextField label1 = new JTextField();
	private JLabel label3 = new JLabel();
	private JLabel label4 = new JLabel();

	public FrameInfodialog(Frame parent) {
		super(parent);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		jbInit();
		super.pack();
	}

	/** Initialisierung der Komponenten */
	private void jbInit() {
		infoPanel = new JPanel(new BorderLayout());
		this.setTitle(Globals.APPNAME);
		setResizable(false);
		label1.setText("Version " + Globals.VERSION + " " + Globals.VERDATE);
		label1.setEditable(false);

		if (!ConfigedMain.THEMES) {
			label1.setBackground(Globals.BACKGROUND_COLOR_3);
		}
		if (!ConfigedMain.FONT) {
			label1.setFont(Globals.defaultFont);
		}
		label3.setText(Globals.COPYRIGHT1);
		if (!ConfigedMain.FONT) {
			label3.setFont(Globals.defaultFont);
		}
		label4.setText(Globals.COPYRIGHT2);
		if (!ConfigedMain.FONT) {
			label4.setFont(Globals.defaultFont);
		}
		textPanel = new VerticalPositioner(new CenterPositioner(label1), new CenterPositioner(label3),
				new CenterPositioner(label4));
		button1.setText("o.k.");
		button1.addActionListener(this);
		button1.addKeyListener(this);

		infoPanel.add(textPanel, BorderLayout.CENTER);
		infoPanel.add(new CenterPositioner(button1), BorderLayout.SOUTH);
		this.getContentPane().add(infoPanel);
	}

	/**
	 * Überschrieben, so dass eine Beendigung beim Schließen des Fensters
	 * möglich ist.
	 */
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	/** Dialog schließen */
	void cancel() {
		dispose();
	}

	/** Dialog bei Schalter-Ereignis schließen */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button1) {
			cancel();
		}
	}

	// KeyListener

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == button1 && e.getKeyCode() == KeyEvent.VK_ENTER) {
			cancel();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}