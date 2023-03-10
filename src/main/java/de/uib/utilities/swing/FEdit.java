/*
 * FEdit.java
 *
 * (c) uib  2009-2013,2021
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.utilities.logging.Logging;

public class FEdit extends JDialog implements ActionListener, KeyListener {
	protected Dimension areaDimension = new Dimension(100, 40);

	protected String initialText = "";
	protected String hint;

	protected JPanel framingPanel;
	protected JComponent editingArea;
	protected JLabel labelHint;

	protected IconButton buttonCommit;
	protected IconButton buttonCancel;

	protected boolean dataChanged;
	protected boolean cancelled;
	protected boolean starting = true;

	protected AbstractCellEditor servedCellEditor;

	protected JTextComponent caller;
	protected Font callerFont;

	public FEdit(String initialText) {
		this(initialText, null);
	}

	public FEdit(String initialText, String hint) {
		super((Dialog) null);

		Logging.debug(this, " FEdit constructed for >>" + initialText + "<< title " + hint);
		super.setIconImage(Globals.mainIcon);

		if (initialText != null) {
			this.initialText = initialText;
		}

		this.hint = hint;

		createComponents();
		// components initialized lazily in init()
	}

	public void setHint(String hint) {
		labelHint.setVisible(hint != null);
		labelHint.setText(hint);
	}

	private void createComponents() {
		framingPanel = new JPanel();
		editingArea = new JPanel(new BorderLayout());

		labelHint = new JLabel();
		labelHint.setFont(Globals.defaultFontStandardBold);

		buttonCommit = new IconButton(Configed.getResourceValue("PanelGenEditTable.SaveButtonTooltip"),
				"images/apply.png", "images/apply_over.png", "images/apply_disabled.png", true) {
			@Override
			public void setEnabled(boolean b) {
				super.setEnabled(b);
				Logging.debug(this, "setEnabled " + b);
			}
		};

		buttonCommit.setPreferredSize(new Dimension(40, Globals.BUTTON_HEIGHT));

		buttonCancel = new IconButton(Configed.getResourceValue("PanelGenEditTable.CancelButtonTooltip"),
				"images/cancel.png", "images/cancel_over.png", "images/cancel_disabled.png", true);
		buttonCancel.setPreferredSize(new Dimension(40, Globals.BUTTON_HEIGHT));

	}

	protected void initComponents() {
		if (!ConfigedMain.OPSI_4_3) {
			framingPanel.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		setHint(hint);

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);

		buttonCommit.addKeyListener(this);
		buttonCancel.addKeyListener(this);

		GroupLayout layout1 = new GroupLayout(framingPanel);
		framingPanel.setLayout(layout1);
		layout1.setHorizontalGroup(layout1.createParallelGroup(Alignment.LEADING)
				.addGroup(layout1.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layout1.createParallelGroup(Alignment.LEADING)
								.addComponent(labelHint, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(editingArea, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGroup(layout1.createSequentialGroup()
										.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

										.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));
		layout1.setVerticalGroup(layout1.createParallelGroup(Alignment.LEADING).addGroup(layout1.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(labelHint, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addComponent(editingArea, 20, 80, Short.MAX_VALUE)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layout1.createParallelGroup(Alignment.BASELINE)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout
						.createSequentialGroup().addContainerGap().addComponent(framingPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout
						.createSequentialGroup().addContainerGap().addComponent(framingPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addContainerGap(20, 20)));

		pack();

	}

	public void setDataChanged(boolean b) {

		dataChanged = b;
		buttonCommit.setEnabled(b);
		buttonCancel.setEnabled(true);

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
		Logging.debug(this, "FEdit.setStartText(): " + s);

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
		if (starting && b) {
			starting = false;
			setDataChanged(false);
		}

		super.setVisible(b);
	}

	public boolean init(Dimension usableAreaSize) {

		if (editingArea.getComponentCount() != 1) {
			Logging.info(this, " editing area not filled with component");

		}

		editingArea.getComponent(0).setPreferredSize(usableAreaSize);
		initComponents();
		return true;
	}

	public boolean init() {

		return init(areaDimension);
	}

	protected void enter() {
		if (caller != null) {
			callerFont = caller.getFont();
			caller.setFont(callerFont.deriveFont(Font.ITALIC));

		}
	}

	public void deactivate() {
		if (caller != null) {

			caller.setFont(callerFont);
			caller.validate();
		}
	}

	protected void leave() {
		Logging.debug(this, "leave");
		updateCaller(initialText);
		buttonCommit.setEnabled(false);
		setVisible(false);

		// no effect probably because of reentering the field
		deactivate();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();

		} else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {

			enter();
		} else if (e.getID() == WindowEvent.WINDOW_DEACTIVATED) {
			// TODO what to do here?
		}

		super.processWindowEvent(e);
	}

	protected void commit() {
		Logging.info(this, "commit");
		setStartText(getText());

		if (servedCellEditor != null) {
			servedCellEditor.stopCellEditing();
		}

		leave();
	}

	protected void cancel() {
		Logging.info(this, "cancel, go back to " + initialText);
		setStartText(initialText);
		cancelled = true;

		if (servedCellEditor != null) {
			servedCellEditor.stopCellEditing();
		}

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

			cancel();
		}

	}

	// interface
	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		Logging.debug(this, " key event " + e);
		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

}
