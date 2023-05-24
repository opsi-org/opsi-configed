/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */


package de.uib.configed.gui.productpage;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.guidata.DependenciesTreeModel;

public class DependenciesTreePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

	private DependenciesTreeModel dependenciesTreeModel;

	private JRadioButton dependenciesNeedsButton;
	private JRadioButton dependenciesNeededByButton;
	private JButton copyListButton;
	private boolean treeAbhaengigkeiten = true;
	private JTree dependenciesTree;
	private JLabel dependenciesTreePathLabel;

	private boolean isActive;

	public DependenciesTreePanel() {
		dependenciesTreeModel = null;
		treeAbhaengigkeiten = true;

		initTree();

		initComponents();
	}

	private void initTree() {
		// Den Tree bauen
		dependenciesTree = new JTree();
		dependenciesTree.setToggleClickCount(0);
		if (!Main.THEMES) {
			dependenciesTree.setBackground(Globals.BACKGROUND_COLOR_8);
		}

		dependenciesTree.addMouseListener(this);
		dependenciesTree.addMouseMotionListener(this);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {

			@Override
			public Component getTreeCellRendererComponent(final JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {

				JLabel label = new JLabel(value.toString()); // (JLabel)

				if (sel) {
					label.setBorder(BorderFactory.createLineBorder(Globals.DEPENDENCIES_TREE_PANEL_BORDER_COLOR, 1));
				} else {
					label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				}

				return label;
			}
		};

		dependenciesTree.setCellRenderer(renderer);

		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel() {

			private void selectAllWithSameProductId(TreePath selectedPath) {

				String productIdOfSelectedPath = selectedPath.getLastPathComponent().toString();

				for (int i = 0; i < dependenciesTree.getRowCount(); i++) {
					TreePath path = dependenciesTree.getPathForRow(i);

					if (path.getLastPathComponent().toString().equals(productIdOfSelectedPath)) {
						dependenciesTree.addSelectionPath(path);
					}
				}
			}

			@Override
			public void setSelectionPath(TreePath path) {
				if (dependenciesTree.isPathSelected(path)) {
					clearSelection();
				} else {
					clearSelection();
					selectAllWithSameProductId(path);
				}
			}
		};

		dependenciesTree.setSelectionModel(selectionModel);
	}

	private void initComponents() {
		JScrollPane dependenciesTreeScrollPanel = new JScrollPane();

		dependenciesTreeScrollPanel.setViewportView(dependenciesTree);
		if (!Main.THEMES) {
			dependenciesTreeScrollPanel.getViewport().setBackground(Globals.BACKGROUND_COLOR_7);
		}

		dependenciesNeedsButton = new JRadioButton(
				Configed.getResourceValue("DependenciesTree.dependenciesNeedsButton"));
		dependenciesNeededByButton = new JRadioButton(
				Configed.getResourceValue("DependenciesTree.dependenciesNeededByButton"));

		copyListButton = new JButton(Configed.getResourceValue("DependenciesTree.copyListButton"));
		if (!Main.FONT) {
			copyListButton.setFont(Globals.defaultFont);
		}
		if (!Main.THEMES) {
			copyListButton.setForeground(Globals.lightBlack);
		}

		dependenciesTreePathLabel = new JLabel();
		dependenciesTreePathLabel.setBorder(BorderFactory.createLineBorder(Globals.greyed, 1));
		dependenciesTreePathLabel.setOpaque(true);
		if (!Main.THEMES) {
			dependenciesTreePathLabel.setBackground(Globals.BACKGROUND_COLOR_8);
		}

		dependenciesNeedsButton.addActionListener(this);
		dependenciesNeededByButton.addActionListener(this);

		// The tree
		updateSelectedButtons();

		copyListButton.addActionListener(this);

		GroupLayout dependenciesTreeGroupLayout = new GroupLayout(this);
		this.setLayout(dependenciesTreeGroupLayout);

		// Grouplayout
		dependenciesTreeGroupLayout.setHorizontalGroup(dependenciesTreeGroupLayout.createSequentialGroup()
				.addGroup(dependenciesTreeGroupLayout.createParallelGroup()
						.addGroup(dependenciesTreeGroupLayout.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(dependenciesNeedsButton, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(dependenciesTreeGroupLayout.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(dependenciesNeededByButton, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(dependenciesTreeGroupLayout.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(copyListButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)))
				.addComponent(dependenciesTreeScrollPanel));

		dependenciesTreeGroupLayout.setVerticalGroup(dependenciesTreeGroupLayout.createParallelGroup()
				.addGroup(dependenciesTreeGroupLayout.createSequentialGroup().addComponent(dependenciesNeedsButton)
						.addComponent(dependenciesNeededByButton).addGap(0, 0, Short.MAX_VALUE).addComponent(
								copyListButton, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addComponent(dependenciesTreeScrollPanel));
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == dependenciesNeedsButton) {
			if (isActive) {
				treeAbhaengigkeiten = true;

				updateSelectedButtons();
				updateTree();
			}
		} else if (event.getSource() == dependenciesNeededByButton) {
			if (isActive) {
				treeAbhaengigkeiten = false;

				updateSelectedButtons();
				updateTree();
			}
		} else if (event.getSource() == copyListButton) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) dependenciesTree.getModel().getRoot();

			if (root != null) {
				String myString = dependenciesTreeModel.getListOfTreeNodes(root);
				StringSelection stringSelection = new StringSelection(myString);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, stringSelection);
			}
		}
	}

	public JLabel getDependenciesTreePathPanel() {
		return dependenciesTreePathLabel;
	}

	private void updateSelectedButtons() {
		dependenciesNeedsButton.setSelected(treeAbhaengigkeiten);
		dependenciesNeededByButton.setSelected(!treeAbhaengigkeiten);
	}

	public void setDependenciesTreeModel(DependenciesTreeModel dependenciesTreeModel) {
		isActive = true;

		this.dependenciesTreeModel = dependenciesTreeModel;
	}

	public void updateTree() {
		DefaultMutableTreeNode dependenciesTreeNode = dependenciesTreeModel
				.getTreeNodeForProductDependencies(treeAbhaengigkeiten);

		updateTree(dependenciesTreeNode);
	}

	private void updateTree(DefaultMutableTreeNode dependenciesTreeNode) {
		dependenciesTree.setModel(new DefaultTreeModel(dependenciesTreeNode));

		// Expand the whole tree
		for (int i = 0; i < dependenciesTree.getRowCount(); i++) {
			dependenciesTree.expandRow(i);
		}
	}

	private void setPathLabel(TreePath path) {

		if (path != null) {
			String pathString = path.toString();

			pathString = pathString.substring(1, pathString.length() - 1);
			pathString = pathString.replace(", ", " ▸ "); // ↦

			dependenciesTreePathLabel.setText(" " + pathString);
		} else {
			clearPathLabel();
		}
	}

	private void clearPathLabel() {
		dependenciesTreePathLabel.setText("");
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		setPathLabel(dependenciesTree.getPathForLocation(event.getX(), event.getY()));
	}

	@Override
	public void mouseExited(MouseEvent event) {
		clearPathLabel();
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		setPathLabel(dependenciesTree.getPathForLocation(event.getX(), event.getY()));
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		// Do nothing because MouseListener demands implementation
	}

	@Override
	public void mousePressed(MouseEvent event) {
		// Do nothing because MouseListener demands implementation
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		// Do nothing because MouseListener demands implementation
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		// Do nothing because MouseListener demands implementation
	}
}
