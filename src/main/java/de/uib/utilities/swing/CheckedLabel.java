/*
*	CheckedLabel.java
*	(c) uib 2017
*	GPL licensed
*   Author Rupert Röder
*/

package de.uib.utilities.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

//a class similar to JCheckBox
public class CheckedLabel extends JPanel implements FocusListener
// is an ObservedSubject for key and mouse actions

{
	static final int setCheckedOn = 1;
	static final int setCheckedOff = 0;
	static final String cmdSetCheckedOn = "cmdSetOn";
	static final String cmdSetCheckedOff = "cmdSetOff";
	protected boolean changeStateAutonomously = true;

	protected JLabel selectedLabel;
	protected JLabel unselectedLabel;
	protected JLabel nullLabel;
	protected JLabel textLabel;

	protected java.awt.Font textFont;

	// protected Map<TextAttribute, ? > defaultTextAttributes;
	// I didn't get work this, couldn't fix the Generics issues

	protected Map defaultTextAttributes;
	protected Map focusedTextAttributes;

	protected Boolean selected;

	protected ArrayList<ActionListener> myListeners;

	protected class GeneralMouseListener extends MouseAdapter {
		protected String source;

		public GeneralMouseListener(String source) {
			this.source = source;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			logging.info(this, "CheckLabel mouseClicked on " + source);
		}
	}

	public CheckedLabel(boolean selected) {
		this("", selected);
	}

	public CheckedLabel(Icon icon, boolean selected) {
		this("", icon, icon, selected);
	}

	public CheckedLabel(String text, boolean selected) {
		this(text, (Icon) null, (Icon) null, selected);
	}

	public CheckedLabel(String text, Icon selectedIcon, Icon unselectedIcon, boolean selected) {
		this(text, selectedIcon, unselectedIcon, (Icon) null, selected);
	}

	public CheckedLabel(Icon selectedIcon, Icon unselectedIcon, boolean selected) {
		this("", selectedIcon, unselectedIcon, (Icon) null, selected);
	}

	public CheckedLabel(String text, Icon selectedIcon, Icon unselectedIcon, Icon nullIcon, boolean selected) {
		super();

		setOpaque(false);

		setFocusable(true);
		setRequestFocusEnabled(true);
		

		addFocusListener(this);

		myListeners = new ArrayList<>();

		textLabel = null;
		try {
			textLabel = new JLabel(text);
		} catch (java.lang.ClassCastException ignore) {
			textLabel = new JLabel(text);
		}

		textFont = textLabel.getFont();
		defaultTextAttributes = textFont.getAttributes();
		focusedTextAttributes = new HashMap<>(defaultTextAttributes);
		focusedTextAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		selectedLabel = new JLabel(selectedIcon);
		unselectedLabel = new JLabel(unselectedIcon);
		nullLabel = new JLabel(nullIcon);

		setLayout();

		setSelected(selected);

		addInternalListeners();

	}

	public void setChangeStateAutonomously(boolean b) {
		changeStateAutonomously = b;
	}

	protected void addInternalListeners() {
		final CheckedLabel ME = this;
		textLabel.addMouseListener(new GeneralMouseListener("textLabel"));
		selectedLabel.addMouseListener(new GeneralMouseListener("selectedLabel") {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled())
					return;
				super.mouseClicked(e);
				if (changeStateAutonomously)
					ME.setSelected(false);
				notifyActionListeners(
						new ActionEvent(ME, setCheckedOff, cmdSetCheckedOff, new java.util.Date().getTime(), 0));
			}
		});

		/*
		 * selectedLabel.addKeyListener(new KeyAdapter(){
		 * 
		 * public void keyPressed(KeyEvent e)
		 * {
		 * //logging.info(this, "event " + e);
		 * super.keyPressed(e);
		 * if (e.getKeyCode() == KeyEvent.VK_SPACE)
		 * {
		 * ME.setSelected(false);
		 * notifyActionListeners(
		 * new ActionEvent(ME, setCheckedOff, cmdSetCheckedOff, new
		 * java.util.Date().getTime(), 0)
		 * );
		 * }
		 * }
		 * }
		 * );
		 */

		unselectedLabel.addMouseListener(new GeneralMouseListener("unselectedLabel") {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled())
					return;
				super.mouseClicked(e);
				if (changeStateAutonomously)
					ME.setSelected(true);
				notifyActionListeners(
						new ActionEvent(ME, setCheckedOn, cmdSetCheckedOn, new java.util.Date().getTime(), 0));
			}
		});

		/*
		 * unselectedLabel.addKeyListener(new KeyAdapter(){
		 * 
		 * public void keyPressed(KeyEvent e)
		 * {
		 * //logging.info(this, "event " + e);
		 * super.keyPressed(e);
		 * if (e.getKeyCode() == KeyEvent.VK_SPACE)
		 * {
		 * ME.setSelected(true);
		 * notifyActionListeners(
		 * new ActionEvent(ME, setCheckedOn, cmdSetCheckedOn, new
		 * java.util.Date().getTime(), 0)
		 * );
		 * }
		 * }
		 * }
		 * );
		 */

		nullLabel.addMouseListener(new GeneralMouseListener("nullLabel") {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled())
					return;
				super.mouseClicked(e);
				if (changeStateAutonomously)
					ME.setSelected(false);
				notifyActionListeners(
						new ActionEvent(ME, setCheckedOff, cmdSetCheckedOff, new java.util.Date().getTime(), 0));
			}
		});

		/*
		 * nullLabel.addKeyListener(new KeyAdapter(){
		 * 
		 * public void keyPressed(KeyEvent e)
		 * {
		 * //logging.info(this, "event " + e);
		 * super.keyPressed(e);
		 * if (e.getKeyCode() == KeyEvent.VK_SPACE)
		 * {
		 * ME.setSelected(true);
		 * notifyActionListeners(
		 * new ActionEvent(ME, setCheckedOn, cmdSetCheckedOn, new
		 * java.util.Date().getTime(), 0)
		 * );
		 * }
		 * }
		 * }
		 * );
		 */

		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				logging.info(this, "event " + e);
				if (!isEnabled())
					return;
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					if (selected == null || selected == true) {

						ME.setSelected(false);
						notifyActionListeners(new ActionEvent(ME, setCheckedOff, cmdSetCheckedOff,
								new java.util.Date().getTime(), 0));
					} else {
						ME.setSelected(true);
						notifyActionListeners(new ActionEvent(ME, setCheckedOff, cmdSetCheckedOff,
								new java.util.Date().getTime(), 0));
					}
				}
			}
		});
	}

	public void setText(String s) {
		textLabel.setText(s);
	}

	@Override
	public void setToolTipText(String s) {
		super.setToolTipText(s);
		nullLabel.setToolTipText(s);
		selectedLabel.setToolTipText(s);
		unselectedLabel.setToolTipText(s);
		textLabel.setToolTipText(s);
	}

	// FocusListener
	@Override
	public void focusGained(FocusEvent e) {
		textLabel.setFont(textFont.deriveFont(focusedTextAttributes));
	}

	@Override
	public void focusLost(FocusEvent e) {
		textLabel.setFont(textFont);
	}

	protected void setLayout() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setVerticalGroup(layout.createParallelGroup()
				.addComponent(textLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(selectedLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(unselectedLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(nullLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT));

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(5, 5, 5)
				.addComponent(selectedLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(unselectedLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(nullLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	public void setSelected(Boolean b) {

		nullLabel.setVisible(b == null);
		selectedLabel.setVisible(b != null && b);
		unselectedLabel.setVisible(b != null && !b);
		selected = b;
	}

	public Boolean isSelected() {
		return selected;
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		textLabel.setEnabled(b);
	}

	public void addActionListener(ActionListener al) {
		myListeners.add(al);
	}

	public ArrayList<ActionListener> getActionListeners() {
		return myListeners;
	}

	public void removeActionListener(ActionListener al) {
		myListeners.remove(al);
	}

	public void removeAllActionListeners() {
		myListeners.clear();
	}

	public void notifyActionListeners(ActionEvent ae) {
		for (ActionListener al : myListeners) {
			al.actionPerformed(ae);
		}
		// logging.info(this, "notifed action listeners about " + ae);
	}

}
