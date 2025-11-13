/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ClientTable;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.productpage.ProductTable;
import de.uib.configed.share.logging.Logging;

public class GroupTreeTransferHandler extends TransferHandler {
	private AbstractGroupTree tree;

	private JComponent source;

	public GroupTreeTransferHandler(AbstractGroupTree tree) {
		super();
		this.tree = tree;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		Logging.debug(this, "can import?");
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return false;
		}

		if (!canImportToThisComponent(support.getComponent())) {
			return false;
		}

		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		Logging.debug(this, "ClientTreeTransferHandler, dropLocation.getPath() ", dropLocation.getPath());

		return canImportToThisLocation(dropLocation);
	}

	private boolean canImportToThisLocation(JTree.DropLocation dropLocation) {
		if (dropLocation.getPath() == null) {
			return false;
		}

		GroupNode targetNode = tree.getGroupNode(dropLocation.getPath().getLastPathComponent().toString());
		if (targetNode == null) {
			return false;
		}

		return canImportToThisGroupNode(targetNode);
	}

	private boolean canImportToThisGroupNode(GroupNode targetNode) {
		if (source instanceof ClientTable || source instanceof ProductTable) {
			// Objects in Table are selected
			return isNormalGroup(targetNode);
		} else {
			return canImportFromTree(targetNode);
		}
	}

	private static boolean isNormalGroup(GroupNode targetNode) {
		return !targetNode.isImmutable() && !targetNode.allowsOnlyGroupChilds()
				&& !ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(targetNode.toString());
	}

	private boolean canImportFromTree(GroupNode targetNode) {
		AbstractGroupTree sourceTree = (AbstractGroupTree) source;
		TreePath[] sourcePaths = sourceTree.getSelectionPaths();

		if (sourcePaths == null || sourcePaths.length > 1) {
			return false;
		} else {
			GroupNode sourceNode = tree.getGroupNode(sourcePaths[0].getLastPathComponent().toString());
			if (sourceNode == null) {
				// An object in the tree is selected
				return isNormalGroup(targetNode);
			} else {
				// A group in the tree is selected
				return isNormalGroup(sourceNode) && !targetNode.isImmutable()
						&& !ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(targetNode.toString())
						&& nodesAreNotEqualOrAncestor(sourceNode, targetNode);
			}
		}
	}

	/*
	 * We check if the source and target node are not equal and not ancestor of each other.
	 * But we exclude the ALL_GROUPS node as source, because it's not a "real" group.
	*/
	private static boolean nodesAreNotEqualOrAncestor(GroupNode sourceNode, GroupNode targetNode) {
		return AbstractGroupTree.ALL_GROUPS_NAME.equals(targetNode.toString()) || (!sourceNode.equals(targetNode)
				&& !sourceNode.isNodeAncestor(targetNode) && !targetNode.isNodeAncestor(sourceNode));
	}

	private boolean canImportToThisComponent(Component target) {
		if (target instanceof ClientTree) {
			return source instanceof ClientTable || source instanceof ClientTree;
		} else if (target instanceof ProductTree) {
			return source instanceof ProductTable || source instanceof ProductTree;
		} else {
			Logging.debug(this, "The target is not a Client or product tree, but ", target.getClass().getName());
			return false;
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent source) {
		this.source = source;
		if (source instanceof JTable tableSource) {
			return createTransferableForJTable(tableSource);
		} else {
			return createTransferableForJTree((AbstractGroupTree) source);
		}
	}

	private static Transferable createTransferableForJTable(JTable tableSource) {
		int[] rows = tableSource.getSelectedRows();
		int cols = tableSource.getColumnCount();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rows.length; i++) {
			if (i > 0) {
				sb.append(System.lineSeparator());
			}
			int viewRow = rows[i];
			for (int c = 0; c < cols; c++) {
				if (c > 0) {
					sb.append('\t');
				}
				Object value = tableSource.getValueAt(viewRow, c);
				if (value != null) {
					sb.append(value.toString());
				}
			}
		}
		return new StringSelection(sb.toString());
	}

	private static Transferable createTransferableForJTree(AbstractGroupTree treeSource) {
		TreePath[] paths = treeSource.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < paths.length; i++) {
			if (i > 0) {
				sb.append(System.lineSeparator());
			}
			sb.append(paths[i].getLastPathComponent().toString());
		}
		return new StringSelection(sb.toString());
	}

	/**
	 * We want to move only if the source and target are both in DIRECTORY or
	 * both in GROUPS when it is a group or we want to move an object only
	 * inside the directory (because it can appear only once anyways), otherwise
	 * we copy the object.
	 */
	private boolean chooseMove(String sourceGroupName, TreePath dropPath, boolean isLeaf) {
		Logging.info(this, "chooseMOVE  sourceGroupName, dropPath ", sourceGroupName, " , ", dropPath);

		boolean result = false;

		boolean stayInsideDIRECTORY = tree.isInDirectory(sourceGroupName) && tree.isInDirectory(dropPath);
		boolean stayInsideGROUPS = tree.isInGROUPS(sourceGroupName) && tree.isInGROUPS(dropPath) && isLeaf;

		Logging.info(this, "chooseMOVE  stayInsideDIRECTORY,  stayInsideGROUPS ", stayInsideDIRECTORY, ", ",
				stayInsideGROUPS);

		Logging.debug(this, "chooseMOVE  ", result);

		return stayInsideDIRECTORY || stayInsideGROUPS;
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (source instanceof ProductTable || source instanceof ClientTable) {
			return importFromTable(support);
		} else {
			return importFromTree(support);
		}
	}

	private boolean importFromTree(TransferSupport support) {
		String selectedObject = ((AbstractGroupTree) source).getSelectionPath().getLastPathComponent().toString();

		return importObjects(Set.of(selectedObject), support);
	}

	private boolean importFromTable(TransferSupport support) {
		Set<String> selectedObjects;
		if (source instanceof ClientTable clientTable) {
			selectedObjects = clientTable.getSelectedSet();
		} else {
			selectedObjects = ((ProductTable) source).getSelectedIDs();
		}

		return importObjects(selectedObjects, support);
	}

	private boolean importObjects(Set<String> selectedObjects, TransferSupport support) {
		if (selectedObjects.isEmpty()) {
			return false;
		}
		// we are at a group node
		// where we want to move/copy to
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		TreePath dropPath = dropLocation.getPath();

		DefaultMutableTreeNode dropParentNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
		String dropParentID = dropParentNode.getUserObject().toString();

		Logging.debug(this, "dropPath ", dropPath);

		// Perform the actual import, but in sorted order
		for (String selectedObject : new TreeSet<>(selectedObjects)) {
			Logging.debug(this, "importData ", selectedObject);

			Logging.debug(this, "active source tree path for selectedObject ", selectedObject);

			if (source instanceof ClientTable || source instanceof ProductTable) {
				// object is selected in table
				tree.copyObjectTo(selectedObject, dropParentID, dropParentNode, dropPath);
			} else {
				// object is selected in tree
				moveObjectInTree(selectedObject, dropParentNode, dropPath, dropParentID);
			}
		}

		return true;
	}

	private void moveObjectInTree(String selectedObject, DefaultMutableTreeNode dropParentNode, TreePath dropPath,
			String dropParentID) {
		GroupNode sourceParentNode = null;
		GroupNode groupNode = null;
		TreePath activeTreePath = tree.getActiveTreePath(selectedObject);

		String sourceParentID = null;
		if (activeTreePath != null) {
			sourceParentID = (String) ((DefaultMutableTreeNode) activeTreePath.getParentPath().getLastPathComponent())
					.getUserObject();
			sourceParentNode = tree.getGroupNode(sourceParentID);
			groupNode = tree.getGroupNode(selectedObject);
		}

		Logging.debug(this, "importData, sourceParentNode ", sourceParentNode);
		Logging.debug(this, "importData, groupNode ", groupNode);

		if (groupNode != null) {
			// it is a group, and it will be moved, but only inside one partial tree
			if (chooseMove(sourceParentID, dropPath, true)) {
				tree.moveGroupTo(selectedObject, groupNode, sourceParentNode, dropParentNode, dropPath, dropParentID);
			} else {
				JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("GroupTreeTransferHandler.cannotMoveGroup.message"),
						Configed.getResourceValue("GroupTreeTransferHandler.cannotMoveGroup.title"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				Logging.warning(this, "importData: this group will not be moved");
			}
		} else {
			// import node
			Logging.debug(this, "importData handling selectedObject ", selectedObject);
			if (chooseMove(sourceParentID, dropPath, false)) {
				tree.moveObjectTo(selectedObject, sourceParentID, sourceParentNode, dropParentNode, dropPath,
						dropParentID);
			} else {
				tree.copyObjectTo(selectedObject, dropParentID, dropParentNode, dropPath);
			}
		}

		Logging.debug(this, "importData ready, selectedObject ", selectedObject);
	}
}
