/*
 * FEdit.java
 *
 * (c) uib  2009-2013,2021
 */

package de.uib.utilities.swing;

/**
 *
 * @author roeder
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public class FEdit extends javax.swing.JDialog implements ActionListener, KeyListener {
	protected Dimension areaDimension = new Dimension(100, 40);

	protected String initialText = "";
	protected String hint = null;

	protected JPanel framingPanel;
	protected JComponent editingArea;
	protected JLabel labelHint;

	// private javax.swing.JButton cancelbutton;
	// private javax.swing.JButton buttonCommit;
	protected de.uib.configed.gui.IconButton buttonCommit;
	protected de.uib.configed.gui.IconButton buttonCancel;

	protected boolean dataChanged = false;
	protected boolean cancelled = false;
	protected boolean starting = true;

	protected AbstractCellEditor servedCellEditor;

	protected JTextComponent caller;
	protected Font callerFont;

	public FEdit(String initialText) {
		this(initialText, null);
	}

	public FEdit(String initialText, String hint) {
		super((Dialog) null);

		logging.debug(this, " FEdit constructed for >>" + initialText + "<< title " + hint);
		setIconImage(Globals.mainIcon);
		
		if (initialText != null)
			this.initialText = initialText;

		this.hint = hint;

		createComponents();
		// components initialized lazily in init()

	}

	public void setHint(String hint) {
		labelHint.setVisible(hint != null);
		labelHint.setText(hint);
	}

	protected void createComponents() {
		framingPanel = new JPanel();
		editingArea = new JPanel(new BorderLayout());
		// editingArea.addFocusListener(this);
		labelHint = new JLabel();
		labelHint.setFont(Globals.defaultFontStandardBold);

		buttonCommit = new de.uib.configed.gui.IconButton(
				de.uib.configed.configed.getResourceValue("PanelGenEditTable.SaveButtonTooltip"), "images/apply.png",
				"images/apply_over.png", "images/apply_disabled.png", true) {
			@Override
			public void setEnabled(boolean b) {
				super.setEnabled(b);
				logging.debug(this, "setEnabled " + b);
			}
		};
		// buttonCommit.setEnabled( false );
		buttonCommit.setPreferredSize(new Dimension(40, Globals.BUTTON_HEIGHT));

		buttonCancel = new de.uib.configed.gui.IconButton(
				de.uib.configed.configed.getResourceValue("PanelGenEditTable.CancelButtonTooltip"), "images/cancel.png",
				"images/cancel_over.png", "images/cancel_disabled.png", true);
		buttonCancel.setPreferredSize(new Dimension(40, Globals.BUTTON_HEIGHT));

		// buttonCancel.setEnabled( false );
	}

	protected void initComponents() {
		framingPanel.setBackground(Globals.backgroundWhite);
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		setHint(hint);

		// okbutton = new javax.swing.JButton();
		// cancelbutton = new javax.swing.JButton();
		// okbutton.setText("ok");
		// cancelbutton.setText("cancel");

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);

		buttonCommit.addKeyListener(this);
		buttonCancel.addKeyListener(this);

		// scrollpane = new javax.swing.JScrollPane();

		// textarea = new javax.swing.JTextArea();

		// textarea.setColumns(20);
		// textarea.setRows(5);
		// scrollpane.setViewportView(textarea);

		javax.swing.GroupLayout layout1 = new javax.swing.GroupLayout(framingPanel);
		framingPanel.setLayout(layout1);
		layout1.setHorizontalGroup(layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout1.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(labelHint, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(editingArea, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addGroup(layout1.createSequentialGroup()
										.addComponent(buttonCommit, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										// .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
										.addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));
		layout1.setVerticalGroup(layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout1.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(labelHint, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addComponent(editingArea, 20, 80, Short.MAX_VALUE)
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(buttonCommit, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(framingPanel,
						javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addContainerGap()
								.addComponent(framingPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addContainerGap(20, 20)));

		pack();

	}

	public void setDataChanged(boolean b) {
		// logging.info(this, " FEdit dataChanged ẃas " + dataChanged +
		// " button enabled " + buttonCommit.isEnabled() + " set to " + b);
		dataChanged = b;
		buttonCommit.setEnabled(b);
		buttonCancel.setEnabled(true);
		// buttonCancel.setEnabled(b);
	}

	protected boolean isDataChenged() {
		return dataChanged;
	}

	public void setAreaDimension(Dimension dim) {
		areaDimension = dim;
	}

	public void addToArea(JComponent c) {
		editingArea.add(c);
	}

	public void setCaller(JTextComponent c) {
		this.caller = c;
	}

	public void updateCaller(String s) {
		if (caller != null) {
			caller.setText(s);
		}
	}

	public void setStartText(String s) {
		logging.debug(this, "FEdit.setStartText(): " + s);

		initialText = s;
		setDataChanged(false);
		cancelled = false;
	}

	public String getText() {
		return initialText;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setVisible(boolean b) {
		if (starting && b) // first visibility
		{
			starting = false;
			setDataChanged(false);
		}

		super.setVisible(b);
	}

	public boolean init(Dimension usableAreaSize) {
		// logging.info(this, "init usableAreaSize " + usableAreaSize);
		if (editingArea.getComponentCount() != 1) {
			logging.info(this, " editing area not filled with component");
			// return false;
		}
		// logging.debug(" editingArea used by " +
		// editingArea.getComponent(0).getClass());
		editingArea.getComponent(0).setPreferredSize(usableAreaSize);
		initComponents();
		return true;
	}

	public boolean init() {
		// logging.info(this, "init areaDimension " + areaDimension);
		return init(areaDimension);
	}

	private int intHalf(double x) {
		return (int) (x / 2);
	}

	public void associateTo(Point p, int xplus, int yplus) {
		int startX = (int) p.getX() + xplus;
		int startY = (int) p.getY() + yplus;

		// problem: in applet in windows, we may leave the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// logging.debug(this, "centerOn screenSize " + screenSize);

		if (startX + getSize().width > screenSize.width)
			startX = screenSize.width - getSize().width;

		if (startY + getSize().height > screenSize.height)
			startY = screenSize.height - getSize().height;

		setLocation(startX, startY);
	}

	public void centerOn(Component master) {
		int startX = 0;
		int startY = 0;

		Point masterOnScreen = null;

		boolean centerOnMaster = (master != null);

		if (centerOnMaster) {
			try {
				masterOnScreen = master.getLocationOnScreen();
			} catch (Exception ex) {
				logging.debug(this, "centerOn " + master + " ex: " + ex);
				centerOnMaster = false;
			}
		}

		if (!centerOnMaster) {
			// center on Screen

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			startX = (screenSize.width - getSize().width) / 2;

			startY = (screenSize.height - getSize().height) / 2;

		} else {
			// logging.debug(this, "centerOn (int) masterOnScreen.getX() " + (int)
			// masterOnScreen.getX());
			// logging.debug(this, "centerOn (int) masterOnScreen.getY() " + (int)
			// masterOnScreen.getY());
			// logging.debug(this, "centerOn master.getWidth() " + master.getWidth() / 2);
			// logging.debug(this, "centerOn master.getHeight() " + master.getHeight() / 2)
			// ;
			// logging.debug(this, "centerOn this.getSize() " + getSize());

			// logging.debug(this, "centerOn " + master.getClass() + ", " + master);

			startX = (int) masterOnScreen.getX() + intHalf(master.getWidth()) - intHalf(getSize().getWidth());
			startY = (int) masterOnScreen.getY() + intHalf(master.getHeight()) - intHalf(getSize().getHeight());

			// problem: in applet in windows, we may leave the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			// logging.debug(this, "centerOn screenSize " + screenSize);

			if (startX + getSize().width > screenSize.width)
				startX = screenSize.width - getSize().width;

			if (startY + getSize().height > screenSize.height)
				startY = screenSize.height - getSize().height;

		}

		setLocation(startX, startY);
	}

	protected void enter() {
		if (caller != null) {
			callerFont = caller.getFont();
			caller.setFont(callerFont.deriveFont(Font.ITALIC));
			// logging.debug("set derived font");
		}
	}

	public void deactivate() {
		if (caller != null) {
			// logging.debug(" reset font ");
			caller.setFont(callerFont);
			caller.validate();
		}
	}

	protected void leave() {
		logging.debug(this, "leave");
		updateCaller(initialText);
		buttonCommit.setEnabled(false);
		setVisible(false);
		deactivate(); // no effect probably because of reentering the field
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
			// logging.debug(" window closing, text " + getText());
		} else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
			// logging.debug(" window activated");
			enter();
		} else if (e.getID() == WindowEvent.WINDOW_DEACTIVATED) {
			// logging.debug(" window deactivated");
			

		}

		super.processWindowEvent(e);
	}

	protected void commit() {
		logging.info(this, "commit");
		setStartText(getText());

		if (servedCellEditor != null)
			servedCellEditor.stopCellEditing();

		leave();
	}

	protected void cancel() {
		logging.info(this, "cancel, go back to " + initialText);
		setStartText(initialText); // sets cancelled = false
		cancelled = true;
		// if (servedCellEditor != null) servedCellEditor.cancelCellEditing();
		if (servedCellEditor != null)
			servedCellEditor.stopCellEditing();

		leave();
	}

	public void setServedCellEditor(AbstractCellEditor cellEditor) {
		servedCellEditor = cellEditor;
	}

	// interface
	// ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getSource() == buttonCancel) {
			// logging.debug (" -------- buttonCancel " + e);
			cancel();
		}

	}

	// interface
	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		logging.debug(this, " key event " + e);
		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
