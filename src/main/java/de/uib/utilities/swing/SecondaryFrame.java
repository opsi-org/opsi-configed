package de.uib.utilities.swing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Map;

import javax.swing.JFrame;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class SecondaryFrame extends JFrame implements WindowListener {

	protected Container masterFrame;

	public SecondaryFrame() {
		this.masterFrame = Globals.mainFrame;
		if (masterFrame == null)
			Logging.warning(this, "masterFrame yet null");
		addWindowListener(this);
	}

	public void setGlobals(Map<String, Object> globals) {
		setIconImage((Image) globals.get("mainIcon"));
		setTitle((String) globals.get("APPNAME"));
	}

	public void start() {
		setExtendedState(Frame.NORMAL);
		centerOnParent();
		setVisible(true);
		Logging.info(this, "started");
	}

	public void centerOnParent() {
		setLocationRelativeTo(masterFrame);
	}

	// for overriding
	protected void callExit() {
		setVisible(false);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			callExit();
		}

	}

	/* WindowListener implementation */
	@Override
	public void windowClosing(WindowEvent e) {
		callExit();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowClosed(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowActivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeactivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowIconified(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeiconified(WindowEvent e) {
		/* Not needed */}
}
