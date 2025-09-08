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
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ClientTable;
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

	private static boolean nodesAreNotEqualOrAncestor(GroupNode sourceNode, GroupNode targetNode) {
		return !sourceNode.equals(targetNode) && !sourceNode.isNodeAncestor(targetNode)
				&& !targetNode.isNodeAncestor(sourceNode);
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
		return new StringSelection(source.getClass().getName());
	}

	private boolean chooseMove(String sourceGroupName, TreePath dropPath, boolean isLeaf) {
		Logging.info(this, "chooseMOVE  sourceGroupName, dropPath ", sourceGroupName, " , ", dropPath);

		boolean result = false;

		boolean stayInsideDIRECTORY = tree.isInDirectory(sourceGroupName) && tree.isInDirectory(dropPath);
		boolean stayInsideGROUPS = tree.isInGROUPS(sourceGroupName) && tree.isInGROUPS(dropPath);

		Logging.info(this, "chooseMOVE  stayInsideDIRECTORY,  stayInsideGROUPS ", stayInsideDIRECTORY, ", ",
				stayInsideGROUPS);

		if (stayInsideDIRECTORY || (stayInsideGROUPS && isLeaf)) {
			result = true;
		}

		Logging.debug(this, "chooseMOVE  ", result);

		return result;
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
			String sourceParentID = null;

			Logging.debug(this, "importData ", selectedObject);

			Logging.debug(this, "active source tree path for selectedObject ", selectedObject);

			GroupNode sourceParentNode = null;
			GroupNode groupNode = null;

			sourceParentID = (String) ((DefaultMutableTreeNode) tree.getActiveTreePath(selectedObject).getParentPath()
					.getLastPathComponent()).getUserObject();
			sourceParentNode = tree.getGroupNode(sourceParentID);
			groupNode = tree.getGroupNode(selectedObject);

			Logging.debug(this, "importData, sourceParentNode ", sourceParentNode);
			Logging.debug(this, "importData, groupNode ", groupNode);

			if (groupNode != null) {
				// it is a group and it could be moved
				// it is a group, and it will be moved, but only inside one partial tree
				if (chooseMove(sourceParentID, dropPath, true)) {
					tree.moveGroupTo(selectedObject, groupNode, sourceParentNode, dropParentNode, dropPath,
							dropParentID);
				} else {
					Logging.info(this, "importData: this group will not be moved");
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

		return true;
	}
}
