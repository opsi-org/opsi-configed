/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class GroupTreeTransferHandler extends TransferHandler {
	private AbstractGroupTree tree;

	public GroupTreeTransferHandler(AbstractGroupTree tree) {
		super();
		this.tree = tree;
	}

	private GroupNode transferRepresentsGroup(String treeRepresentation) {
		if (treeRepresentation == null) {
			return null;
		}

		String[] parts = treeRepresentation.split(",");

		if (parts.length == 1) {
			return null;
		}

		String node = parts[parts.length - 1];
		GroupNode result = tree.getGroupNode(node);

		Logging.debug(this, "transferRepresentsGroup : " + treeRepresentation + ", result " + result);

		return result;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		Logging.debug(this, "can import?");

		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService().isGlobalReadOnly()
				|| !support.isDataFlavorSupported(DataFlavor.stringFlavor) || !support.isDrop()) {
			return false;
		}

		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		Logging.debug(this, "ClientTreeTransferHandler, dropLocation.getPath() " + dropLocation.getPath());

		if (dropLocation.getPath() == null) {
			return false;
		}

		String transferData = null;

		try {
			transferData = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException ex) {
			Logging.warning(this, " unsupported data flavor " + ex);
		} catch (IOException ex) {
			Logging.warning(this, " transferable io exception " + ex);
		}

		GroupNode sourceGroupNode = transferRepresentsGroup(transferData);
		Object[] sourceObjectPath = new Object[0];
		if (sourceGroupNode != null) {
			sourceObjectPath = sourceGroupNode.getUserObjectPath();
		}

		DefaultMutableTreeNode dropOnThis = (DefaultMutableTreeNode) dropLocation.getPath().getLastPathComponent();
		String id = dropOnThis.getUserObject().toString();
		GroupNode targetNode = tree.getGroupNode(id);
		Object[] dropObjectPath = new Object[0];
		if (targetNode != null) {
			dropObjectPath = targetNode.getUserObjectPath();
		}

		// debugging the if clause

		if (targetNode != null) {
			Logging.debug(this, "canImport targetNode.isImmutable() " + targetNode.isImmutable());
		}

		Logging.debug(this, "canImport sourceGroupNode " + sourceGroupNode);
		if (sourceGroupNode != null && targetNode != null) {
			Logging.debug(this, "canImport targetNode.allowsOnlyGroupChilds() " + targetNode.allowsOnlyGroupChilds());
			Logging.debug(this, "canImport !allows subgroups "
					+ ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(targetNode.toString()));
		}
		if (sourceGroupNode == null && targetNode != null) {
			Logging.debug(this, "canImport targetNode.allowsOnlyGroupChilds() " + targetNode.allowsOnlyGroupChilds());
		}

		Logging.debug(this, "canImport, dropOnThis  path " + Arrays.toString(dropObjectPath));
		Logging.debug(this, "canImport source path " + Arrays.toString(sourceObjectPath));

		if (targetNode == null) {
			return false;
		}

		boolean result = true;
		boolean canNotImportGroupNode = sourceGroupNode != null
				&& ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(targetNode.toString());
		boolean canNotImportNonGroupNode = sourceGroupNode == null && targetNode.allowsOnlyGroupChilds();
		boolean isDifferentGroupBranch = (sourceGroupNode != null && sourceObjectPath.length > 1
				&& dropObjectPath.length > 1 && !(sourceObjectPath[1].equals(dropObjectPath[1])));

		if (targetNode.isImmutable() || canNotImportGroupNode || canNotImportNonGroupNode || isDifferentGroupBranch) {
			result = false;
		}

		Logging.debug(this, "canImport, dropOnThis " + dropOnThis.getUserObject());
		Logging.debug(this, "canImport: " + result);
		return result;
	}

	@Override
	public int getSourceActions(JComponent c) {
		Logging.debug(this, "getSourceActions,  activePaths " + Arrays.toString(tree.getSelectionPaths()));

		if (tree.getSelectionPaths() == null || tree.getSelectionPaths().length == 0) {
			Logging.debug(this, "getSourceActions no active pathes, TransferHandler.NONE");
			return TransferHandler.NONE;
		}

		DefaultMutableTreeNode dropThis = (DefaultMutableTreeNode) tree.getSelectionPaths()[0].getLastPathComponent();

		if (dropThis instanceof GroupNode) {
			GroupNode dropThisVariant = tree.getGroupNode(dropThis.toString());

			if (dropThis != dropThisVariant) {
				Logging.warning(this, "getSourceActions,  dropThis != dropThisVariant");
				Logging.warning(this, "getSourceActions,  dropThis " + dropThis);
				Logging.warning(this, "getSourceActions,  dropThisVariant " + dropThisVariant);
			}

			GroupNode parent = (GroupNode) dropThisVariant.getParent();

			Logging.debug(this, "getSourceActions,  dropThis " + dropThis + " parent " + parent);

			if (parent.isImmutable()) {
				Logging.debug(this, "getSourceActions dropObject is immutable, TransferHandler.NONE");
				return TransferHandler.NONE;
			}
		}

		String nodeString = dropThis.getUserObject().toString();

		if (tree.getGroups().keySet().contains(nodeString)) {
			Logging.debug(this, "getSourceActions object already there, TransferHandler.MOVE");
			return TransferHandler.MOVE;
		}

		// there can only be one group selected, and it can only be moved
		// (for top groups the NONE handler was already returned)
		for (TreePath path : tree.getSelectionPaths()) {

			if (tree.isChildOfALL((DefaultMutableTreeNode) path.getLastPathComponent())) {
				Logging.debug(this, "getSourceActions path " + path + " childOfALL, should be TransferHandler.COPY");
				return TransferHandler.COPY;
				// we dont accept to move any item out of ALL
			}

			if (tree.isInDirectory(path)) {
				Logging.debug(this, "getSourceActions , isInDirectory true");
				// action depends additionally from target
			}
		}

		Logging.debug(this, "getSourceActions all remaining, TransferHandler.COPY_OR_MOVE");
		return TransferHandler.COPY_OR_MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		StringBuilder buff = new StringBuilder();

		for (TreePath path : tree.getSelectionPaths()) {
			int len = path.getPath().length;
			for (int j = 0; j < len; j++) {
				buff.append(path.getPath()[j]);
				if (j < len - 1) {
					buff.append(",");
				}
			}

			buff.append("\n");
		}

		// We want to get the string without the last character "\n"
		return new StringSelection(buff.substring(0, buff.length() - 1));
	}

	private boolean chooseMove(TransferHandler.TransferSupport support, String sourceGroupName, TreePath dropPath,
			boolean isLeaf) {
		Logging.info(this, "chooseMOVE  support " + support);
		Logging.info(this, "chooseMOVE  sourceGroupName, dropPath " + sourceGroupName + " , " + dropPath);
		Logging.info(this,
				"chooseMOVE support.getUserDropAction() == TransferHandler.MOVE "
						+ (support.getUserDropAction() == TransferHandler.MOVE) + " support.getUserDropAction() "
						+ support.getUserDropAction());

		boolean result = false;

		boolean stayInsideDIRECTORY = tree.isInDirectory(sourceGroupName) && tree.isInDirectory(dropPath);
		boolean stayInsideGROUPS = tree.isInGROUPS(sourceGroupName) && tree.isInGROUPS(dropPath);

		Logging.info(this,
				"chooseMOVE  stayInsideDIRECTORY,  stayInsideGROUPS " + stayInsideDIRECTORY + ", " + stayInsideGROUPS);

		if (stayInsideDIRECTORY || (stayInsideGROUPS && !isLeaf)) {
			result = true;
		}

		Logging.debug(this, "chooseMOVE  " + result);

		return result;
	}

	private void handleObjectID(String importID, TransferHandler.TransferSupport support, TreePath sourcePath,
			GroupNode sourceParentNode, String sourceParentID, TreePath dropPath, DefaultMutableTreeNode dropParentNode,
			String dropParentID) {
		Logging.debug(this,
				"handleClientID importID, sourcePath, sourceParentID, sourceParentNode, dropParentID,  " + importID
						+ ", " + sourcePath + " , " + sourceParentID + ", " + sourceParentNode + ", " + dropParentID);

		boolean moving = false;

		String adaptedSourceParentID = sourceParentID;
		GroupNode adaptedSourceParentNode = sourceParentNode;

		// we are in table and did not get a real souce path
		// TODO necessary?
		if (sourcePath == null) {
			String firstDIRECTORYgroupname = null;
			Set<GroupNode> locations = tree.getLocationsInDirectory(importID);
			if (locations != null && !locations.isEmpty()) {
				Logging.debug(this, "handleClientID tree.getLocationsInDirectory 1");
				Iterator<GroupNode> iter = tree.getLocationsInDirectory(importID).iterator();
				firstDIRECTORYgroupname = iter.next().toString();
				Logging.debug(this, "handleClientID tree.getLocationsInDirectory firstDIRECTORYgroupname "
						+ firstDIRECTORYgroupname);
				adaptedSourceParentID = firstDIRECTORYgroupname;
				moving = chooseMove(support, firstDIRECTORYgroupname, dropPath, true);

				adaptedSourceParentNode = tree.getGroupNode(adaptedSourceParentID);
			}
		} else {
			moving = chooseMove(support, adaptedSourceParentID, dropPath, true);
		}

		if (moving) {
			tree.moveObjectTo(importID, sourcePath, adaptedSourceParentID, adaptedSourceParentNode, dropParentNode,
					dropPath, dropParentID);
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

		Logging.debug(this, "dropPath " + dropPath);

		// what is to be moved/copied

		Logging.debug(this, "importData, getActivePaths(): " + Arrays.toString(tree.getSelectionPaths()));

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

		Logging.debug(this, "importData, values: " + selectedObjects);

		// if the source is the tree then we arranged lines for the transfer
		// the other possible source are lines from the JTable, as well arranged to
		// lines

		// Perform the actual import.
		for (String selectedObject : selectedObjects) {
			String importID = null;
			String sourceParentID = null;

			// if values not got from transferable, the following reduces
			if (selectedObject.split("\t").length > 1) {
				// probably an import from the JTable

				// we assume a table source with first fieldvalue being a clientID
				importID = selectedObject.split("\t")[0];
			} else {
				String[] parts = selectedObject.split(",");

				importID = parts[parts.length - 1];
			}

			Logging.debug(this, "importData " + importID);

			TreePath sourcePath = tree.getActiveTreePath(importID);
			Logging.debug(this, "active source tree path for importID " + importID + ": " + sourcePath);

			GroupNode sourceParentNode = null;
			GroupNode groupNode = null;

			if (sourcePath != null) {
				sourceParentID = (String) ((DefaultMutableTreeNode) sourcePath.getParentPath().getLastPathComponent())
						.getUserObject();
				sourceParentNode = tree.getGroupNode(sourceParentID);
				groupNode = tree.getGroupNode(importID);
			} else {
				// coming from table, replace!
				Logging.debug(this, "importData, sourceParentID " + sourceParentID);
			}
			Logging.debug(this, "importData, sourceParentNode " + sourceParentNode);
			Logging.debug(this, "importData, groupNode " + groupNode);

			if (groupNode != null) {
				// it is a group and it could be moved
				// it is a group, and it will be moved, but only inside one partial tree
				if (chooseMove(support, sourceParentID, dropPath, false)) {
					tree.moveGroupTo(importID, groupNode, sourceParentNode, dropParentNode, dropPath, dropParentID);
				} else {
					Logging.info(this, "importData: this group will not be moved");
				}
			} else {
				// import node
				Logging.debug(this, "importData handling import ID " + importID);

				handleObjectID(importID, support, sourcePath, sourceParentNode, sourceParentID, dropPath,
						dropParentNode, dropParentID);
			}

			Logging.debug(this, "importData ready, importID " + importID);
		}

		return true;
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		Logging.debug(this, " exportToClipboard " + comp + " , " + clip + ", " + action);
		super.exportToClipboard(comp, clip, action);
	}
}
