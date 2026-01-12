/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.data.DependenciesTreeModel;
import net.miginfocom.swing.MigLayout;

public class DependenciesTreePanel extends JPanel implements MouseListener, MouseMotionListener {
	private DependenciesTreeModel dependenciesTreeModel;

	private JRadioButton dependenciesNeedsButton;
	private JRadioButton dependenciesNeededByButton;
	private boolean treeInverted;
	private JTree dependenciesTree;
	private JLabel dependenciesTreePathLabel;

	private boolean isActive;

	public DependenciesTreePanel() {
		dependenciesTreeModel = null;
		treeInverted = false;

		initTree();

		initComponents();
	}

	private void initTree() {
		// Den Tree bauen
		dependenciesTree = new JTree();
		dependenciesTree.setToggleClickCount(0);

		dependenciesTree.addMouseListener(this);
		dependenciesTree.addMouseMotionListener(this);

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

		dependenciesNeedsButton = new JRadioButton(
				Configed.getResourceValue("DependenciesTree.dependenciesNeedsButton"));
		dependenciesNeededByButton = new JRadioButton(
				Configed.getResourceValue("DependenciesTree.dependenciesNeededByButton"));

		JButton copyListButton = new JButton(Configed.getResourceValue("DependenciesTree.copyListButton"));

		dependenciesTreePathLabel = new JLabel();
		// We need to set a different font, otherwise the arrow is not shown
		dependenciesTreePathLabel
				.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, dependenciesTreePathLabel.getFont().getSize()));
		dependenciesTreePathLabel
				.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1));

		dependenciesNeedsButton.addActionListener(event -> dependenciesNeededAction(false));
		dependenciesNeededByButton.addActionListener(event -> dependenciesNeededAction(true));

		// The tree
		updateSelectedButtons();

		copyListButton.addActionListener(event -> copyList());

		this.setLayout(new MigLayout("insets 0, fill", "[pref!][grow, fill]", "[grow, fill]0"));

		// this.add(dependenciesNeedsButton, "cell 0 0, growx");
		// this.add(dependenciesNeededByButton, "cell 0 1, growx, gapafter push");
		// this.add(copyListButton, "cell 0 3, growx, aligny bottom");
		JPanel leftSidePanel = new JPanel(new MigLayout("insets 0, filly, wrap 1", "", "[][]push[]"));
		leftSidePanel.add(dependenciesNeedsButton);
		leftSidePanel.add(dependenciesNeededByButton);
		leftSidePanel.add(copyListButton, "gapbefore push");

		this.add(leftSidePanel, "grow");
		this.add(dependenciesTreeScrollPanel, "grow");
	}

	private void dependenciesNeededAction(boolean treeInverted) {
		if (isActive) {
			this.treeInverted = treeInverted;

			updateSelectedButtons();
			updateTree();
		}
	}

	private void copyList() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) dependenciesTree.getModel().getRoot();

		if (root != null) {
			String myString = dependenciesTreeModel.getListOfTreeNodes(root);
			StringSelection stringSelection = new StringSelection(myString);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, stringSelection);
		}
	}

	public JLabel getDependenciesTreePathPanel() {
		return dependenciesTreePathLabel;
	}

	private void updateSelectedButtons() {
		dependenciesNeedsButton.setSelected(!treeInverted);
		dependenciesNeededByButton.setSelected(treeInverted);
	}

	public void setDependenciesTreeModel(DependenciesTreeModel dependenciesTreeModel) {
		isActive = true;

		this.dependenciesTreeModel = dependenciesTreeModel;
	}

	public void updateTree() {
		DefaultMutableTreeNode dependenciesTreeNode = dependenciesTreeModel
				.getTreeNodeForProductDependencies(treeInverted);

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
