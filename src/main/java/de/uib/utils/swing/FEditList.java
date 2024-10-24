/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.list.StandardListCellRenderer;

public class FEditList<O> extends JDialog
		implements ListSelectionListener, MouseListener, ActionListener, KeyListener, DocumentListener {
	protected static final int BUTTON_WIDTH = 30;

	private JScrollPane scrollpane;

	protected boolean leaveOnCommit = true;

	protected JPanel editingArea;
	protected JPanel loggingPanel;

	protected JButton buttonCommit;
	protected JButton buttonCancel;
	protected JButton buttonAdd;

	protected FlatTextField extraField;
	protected JTextArea loggingArea;

	protected JList<O> visibleList;

	private Object initialValue = "";

	private List<O> initiallySelected;

	protected Object selValue = "";

	private JPanel framingPanel;

	private JSplitPane splitPane;

	public FEditList() {
		super.setIconImage(Icons.getMainIcon());

		createComponents();
		// components initialized lazily in init()

		addListeners();

		visibleList = new JList<>();
		visibleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		visibleList.addListSelectionListener(this);
		visibleList.setCellRenderer(new StandardListCellRenderer());

		visibleList.addMouseListener(this);

		scrollpane = new JScrollPane();
		scrollpane.setViewportView(visibleList);
		editingArea.add(scrollpane, BorderLayout.CENTER);
	}

	private void addListeners() {
		editingArea.addKeyListener(this);

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);
		buttonAdd.addActionListener(this);

		buttonCommit.addKeyListener(this);
		buttonCancel.addKeyListener(this);
		buttonAdd.addKeyListener(this);
	}

	protected void createComponents() {
		framingPanel = new JPanel();
		editingArea = new JPanel(new BorderLayout(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));
		loggingPanel = new JPanel(new BorderLayout());
		loggingPanel.setBorder(new EmptyBorder(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
				Globals.MIN_GAP_SIZE));
		loggingPanel.setVisible(false);

		loggingArea = new JTextArea();
		loggingArea.setEditable(false);

		JScrollPane scrollpaneL = new JScrollPane(loggingArea);
		scrollpaneL.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		loggingPanel.add(scrollpaneL, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		buttonCommit = new JButton(Icons.getIntellijIcon("checkmark"));
		buttonCommit.setToolTipText(Configed.getResourceValue("save"));
		buttonCommit.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));

		buttonCancel = new JButton(Icons.getIntellijIcon("close"));
		buttonCancel.setToolTipText(Configed.getResourceValue("FEditObject.CancelButtonTooltip"));
		buttonCancel.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonCancel.setEnabled(true);

		buttonAdd = new JButton(Icons.getIntellijIcon("add"));
		buttonAdd.setToolTipText(Configed.getResourceValue("FEditObject.AddButtonTooltip"));
		buttonAdd.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonAdd.setEnabled(false);

		extraField = new FlatTextField();
		extraField.setTrailingComponent(buttonAdd);
		extraField.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
		extraField.setVisible(false);
	}

	protected void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		extraField.getDocument().addDocumentListener(this);

		GroupLayout layout1 = new GroupLayout(framingPanel);
		framingPanel.setLayout(layout1);
		layout1.setHorizontalGroup(layout1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout1.createParallelGroup(Alignment.LEADING)
						.addComponent(editingArea, 60, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGroup(layout1.createSequentialGroup()
								.addComponent(buttonCancel, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonCommit, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
								.addComponent(extraField, 20, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)))
				.addGap(Globals.MIN_GAP_SIZE));

		layout1.setVerticalGroup(layout1.createParallelGroup(Alignment.LEADING).addGroup(layout1.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(editingArea, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout1.createParallelGroup(Alignment.CENTER)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(extraField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)));

		if (loggingPanel.isVisible()) {
			splitPane.setTopComponent(framingPanel);
			splitPane.setBottomComponent(loggingPanel);

			GroupLayout layout = new GroupLayout(getContentPane());
			this.setLayout(layout);
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(splitPane, 0,
					GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(splitPane, 0,
					GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
		} else {
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
							.addComponent(framingPanel, 100, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(Globals.MIN_GAP_SIZE))
					.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
							.addComponent(loggingPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							.addGap(Globals.MIN_GAP_SIZE)));
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addContainerGap(20, 20)
							.addComponent(framingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									Short.MAX_VALUE)
							.addComponent(loggingPanel, GroupLayout.PREFERRED_SIZE, 30, Short.MAX_VALUE)
							.addContainerGap(20, 20)));
		}

		pack();

		visibleList.addKeyListener(this);
	}

	public void setDividerLocation(double loc) {
		splitPane.setDividerLocation(loc);
	}

	public void setCellRenderer(ListCellRenderer<Object> render) {
		visibleList.setCellRenderer(render);
	}

	public void setListModel(ListModel<O> model) {
		visibleList.setModel(model);
	}

	public void setLeaveOnCommit(boolean b) {
		leaveOnCommit = b;
	}

	public void setEditable() {
		extraField.setVisible(true);
	}

	private void addSelectedValues(List<O> toSelect) {
		if (toSelect == null) {
			return;
		}

		ListModel<O> model = visibleList.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			Object element = model.getElementAt(i);

			if (toSelect.contains(element)) {
				visibleList.addSelectionInterval(i, i);
			}
		}

		visibleList.ensureIndexIsVisible(visibleList.getMaxSelectionIndex());
	}

	public void initSelection() {
		if (visibleList.getModel().getSize() > 0) {
			visibleList.setSelectedIndex(0);
		}
	}

	public void setSelectedValues(List<O> toSelect) {
		initiallySelected = toSelect;

		visibleList.clearSelection();

		addSelectedValues(toSelect);
	}

	public void setSelectedValue(O ob) {
		visibleList.setSelectedValue(ob, true);
	}

	protected void addElement(O element) {
		ListModel<O> limo = visibleList.getModel();
		if (limo instanceof DefaultListModel) {
			if (!((DefaultListModel<O>) limo).contains(element)) {
				((DefaultListModel<O>) limo).addElement(element);
				List<O> list = new ArrayList<>();
				list.add(element);
				addSelectedValues(list);
			}
		} else {
			Logging.error(this, "for element adding required instance of DefaultListModel");
		}
	}

	public void setPreferredScrollPaneSize(Dimension size) {
		scrollpane.setPreferredSize(size);
	}

	public void setStartValue(Object s) {
		initialValue = s;
	}

	private void cancel() {
		setSelectedValues(initiallySelected);

		setStartValue(initialValue);
		setVisible(false);
	}

	private static boolean forbidEditingTargetSpecific() {
		boolean forbidEditing = false;

		Logging.debug("forbidEditing for target ", ConfigedMain.getEditingTarget(), "?");

		if (ConfigedMain.getEditingTarget() == ConfigedMain.EditingTarget.SERVER) {
			forbidEditing = !PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
					.hasServerFullPermissionPD();
		} else {
			forbidEditing = PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
					.isGlobalReadOnly();
		}

		Logging.debug("forbidEditing ", forbidEditing);

		return forbidEditing;
	}

	protected void commit() {
		Logging.debug(this, "FEditList.commit");

		if (forbidEditingTargetSpecific()) {
			Logging.debug(this, "commit: forbidden");
			cancel();
		} else {
			if (leaveOnCommit) {
				setVisible(false);
			}
		}
	}

	@Override
	public void setModal(boolean b) {
		super.setModal(b);
		Logging.debug(this, "setModal ", b);
		if (b) {
			setAlwaysOnTop(true);
		}
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}

		super.processWindowEvent(e);
	}

	// interface ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		Logging.debug(this, "actionPerformed");

		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getSource() == buttonCancel) {
			cancel();
		} else if (e.getSource() == buttonAdd) {
			// This button will be used only in subclasses
		} else {
			Logging.warning(this, "unexpected action on source ", e.getSource());
		}
	}

	// interface KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			commit();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		} else {
			// Do nothing on other keys
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	// interface MouseListener

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			String txt = "" + visibleList.getModel().getElementAt(visibleList.locationToIndex(e.getPoint()));

			extraField.setText(txt);
			extraFieldChanged(false);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}

	public void init() {
		setStartValue(selValue);
		initComponents();
		initEditing();
	}

	public List<O> getSelectedList() {
		List<O> result = new ArrayList<>();

		ListModel<O> model = visibleList.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			O element = model.getElementAt(i);
			if (visibleList.isSelectedIndex(i)) {
				result.add(element);
			}
		}

		return result;
	}

	private void initEditing() {
		Logging.debug(this, "FEditList.initEditing");
		setDataChanged(false);
		extraField.setText("");
	}

	// interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		setDataChanged(true);
	}

	public void setDataChanged(boolean b) {
		Logging.debug(this, "setDataChanged ", b);

		if (forbidEditingTargetSpecific() && b) {
			return;
		}

		buttonCommit.setEnabled(b);
	}

	protected void extraFieldChanged(boolean b) {
		buttonAdd.setEnabled(b);
	}

	// -------------
	// interface DocumentListener
	@Override
	public void changedUpdate(DocumentEvent e) {
		extraFieldChanged(true);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		extraFieldChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		extraFieldChanged(true);
	}

}
