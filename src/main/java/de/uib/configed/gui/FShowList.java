package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

/**
 * This class is intended to show a list in text area
 */
public class FShowList extends FTextArea {

	JTextArea jTextArea1 = new JTextArea();

	public FShowList(JFrame owner, String title, boolean modal) {
		super(owner, title, modal);
		initFShowList();
	}

	public FShowList(JFrame owner, String title, String message, boolean modal) {
		this(owner, title, modal);
		this.owner = owner;
		setMessage(message);
	}

	public FShowList(JFrame owner, String title, boolean modal, String[] buttonList) {
		super(owner, title, modal, buttonList);
		initFShowList();
	}

	public FShowList(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner, title, modal, buttonList);
		this.owner = owner;
		initFShowList(preferredWidth, preferredHeight);
	}

	@Override
	public void setMessage(String message) {
		jTextArea1.setText(message);
	}

	public void appendLine(String line) {
		if (!jTextArea1.getText().equals("")) {
			jTextArea1.setText(jTextArea1.getText() + "\n");
		}

		jTextArea1.setText(jTextArea1.getText() + line);
		jTextArea1.setCaretPosition(jTextArea1.getText().length());
	}

	public void setLines(List<String> lines) {
		for (String line : lines) {
			appendLine(line);
		}
	}

	@Override
	public void setFont(Font f) {
		if (jTextArea1 != null) {
			jTextArea1.setFont(f);
		}
	}

	public void setLineWrap(boolean b) {
		jTextArea1.setLineWrap(b);
	}

	private void initFShowList() {
		initFShowList(800, 100);
	}

	private void initFShowList(int preferredWidth, int preferredHeight) {
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		jTextArea1.setLineWrap(true);
		jTextArea1.setWrapStyleWord(true);
		jTextArea1.setOpaque(true);

		jTextArea1.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		jTextArea1.setText("          ");
		jTextArea1.setEditable(false);
		jTextArea1.setFont(new java.awt.Font("Dialog", 0, 14));

		scrollpane.getViewport().add(jTextArea1, null);

		jTextArea1.addKeyListener(this);

		pack();
	}

	@Override
	public void doAction1() {

		Logging.clearErrorList();
		if (owner != null) {
			owner.toFront();
		}

		super.doAction1();
	}

	// KeyListener
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
		}

		else if (e.getKeyCode() == KeyEvent.VK_TAB && !shiftPressed && e.getSource() == jTextArea1) {
			jButton1.requestFocus();
		}

		else if (e.getKeyCode() == KeyEvent.VK_TAB && shiftPressed && e.getSource() == jButton1) {
			jTextArea1.requestFocus();
		}
	}

}
