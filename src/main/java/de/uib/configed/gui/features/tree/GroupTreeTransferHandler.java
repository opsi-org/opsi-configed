/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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

		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService().isGlobalReadOnly()
				|| !support.isDataFlavorSupported(DataFlavor.stringFlavor) || !support.isDrop()) {
			return false;
		}

		if (!canImportToThisComponent(support.getComponent())) {
			return false;
		}

		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		Logging.debug(this, "ClientTreeTransferHandler, dropLocation.getPath() ", dropLocation.getPath());

		if (dropLocation.getPath() == null) {
			return false;
		}

		GroupNode targetNode = tree.getGroupNode(dropLocation.getPath().getLastPathComponent().toString());

		if (targetNode != null) {
			Logging.debug(this, "canImport targetNode.allowsOnlyGroupChilds() ", targetNode.allowsOnlyGroupChilds());
			Logging.debug(this, "canImport !allows subgroups ",
					ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(targetNode.toString()));
		}

		return canImport(targetNode);
	}

	private boolean canImport(GroupNode targetNode) {
		if (targetNode == null) {
			return false;
		}

		boolean canImportGroupNode = !ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(targetNode.toString());
		boolean canImportNonGroupNode = !targetNode.allowsOnlyGroupChilds();

		boolean result = !targetNode.isImmutable() && canImportGroupNode && canImportNonGroupNode;

		Logging.debug(this, "canImport: ", result);
		return result;
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

	private void handleObjectID(String importID, TreePath sourcePath, GroupNode sourceParentNode, String sourceParentID,
			TreePath dropPath, DefaultMutableTreeNode dropParentNode, String dropParentID) {
		Logging.debug(this, "handleClientID importID, sourcePath, sourceParentID, sourceParentNode, dropParentID,  ",
				importID, ", ", sourcePath, " , ", sourceParentID, ", ", sourceParentNode, ", ", dropParentID);

		boolean moving = false;

		// we are in table and did not get a real souce path if sourcePath is null
		if (sourcePath == null) {
			String firstDIRECTORYgroupname = null;
			Set<GroupNode> locations = tree.getLocationsInDirectory(importID);
			if (!locations.isEmpty()) {
				Logging.debug(this, "handleClientID tree.getLocationsInDirectory 1");
				Iterator<GroupNode> iter = locations.iterator();
				firstDIRECTORYgroupname = iter.next().toString();
				Logging.debug(this, "handleClientID tree.getLocationsInDirectory firstDIRECTORYgroupname ",
						firstDIRECTORYgroupname);
				sourceParentID = firstDIRECTORYgroupname;
				moving = chooseMove(firstDIRECTORYgroupname, dropPath, false);

				sourceParentNode = tree.getGroupNode(sourceParentID);
			}
		} else {
			moving = chooseMove(sourceParentID, dropPath, false);
		}

		if (moving) {
			tree.moveObjectTo(importID, sourcePath, sourceParentID, sourceParentNode, dropParentNode, dropPath,
					dropParentID);
		} else {
			tree.copyObjectTo(importID, sourcePath, dropParentID, dropParentNode, dropPath);
		}
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		// we are at a group node
		// where we want to move/copy to
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		TreePath dropPath = dropLocation.getPath();

		DefaultMutableTreeNode dropParentNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
		String dropParentID = dropParentNode.getUserObject().toString();

		Logging.debug(this, "dropPath ", dropPath);

		// what is to be moved/copied

		Logging.debug(this, "importData, getActivePaths(): ", Arrays.toString(tree.getSelectionPaths()));

		Set<String> selectedObjects = tree.getSelectedObjectsInTable();
		// possibly transfer of a group node
		if (selectedObjects.isEmpty()) {
			TreePath[] activePaths = tree.getSelectionPaths();
			if (activePaths != null && activePaths.length == 1) {
				String importID = (String) (((DefaultMutableTreeNode) (activePaths[0]).getLastPathComponent())
						.getUserObject());
				selectedObjects = Collections.singleton(importID);
			}
		}

		Logging.debug(this, "importData, values: ", selectedObjects);

		// if the source is the tree then we arranged lines for the transfer
		// the other possible source are lines from the JTable, as well arranged to
		// lines

		// Perform the actual import, but in sorted order
		for (String selectedObject : new TreeSet<>(selectedObjects)) {
			String sourceParentID = null;

			Logging.debug(this, "importData ", selectedObject);

			TreePath sourcePath = tree.getActiveTreePath(selectedObject);
			Logging.debug(this, "active source tree path for selectedObject ", selectedObject, ": ", sourcePath);

			GroupNode sourceParentNode = null;
			GroupNode groupNode = null;

			if (sourcePath != null) {
				sourceParentID = (String) ((DefaultMutableTreeNode) sourcePath.getParentPath().getLastPathComponent())
						.getUserObject();
				sourceParentNode = tree.getGroupNode(sourceParentID);
				groupNode = tree.getGroupNode(selectedObject);
			} else {
				// coming from table, replace!
				Logging.debug(this, "importData, sourceParentID ", sourceParentID);
			}
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

				handleObjectID(selectedObject, sourcePath, sourceParentNode, sourceParentID, dropPath, dropParentNode,
						dropParentID);
			}

			Logging.debug(this, "importData ready, selectedObject ", selectedObject);
		}

		return true;
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		super.exportToClipboard(comp, clip, action);
	}
}
