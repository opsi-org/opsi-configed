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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class ClientTreeTransferHandler extends TransferHandler {
	private ClientTree tree;

	public ClientTreeTransferHandler(ClientTree tree) {
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

		if (Globals.isGlobalReadOnly()) {
			return false;
		}

		if (!support.isDataFlavorSupported(DataFlavor.stringFlavor) || !support.isDrop()) {
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
		} catch (Exception ex) {
			Logging.warning(this, "canImport " + ex);
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
			Logging.debug(this, "canImport !targetNode.allowsSubGroups() " + !targetNode.allowsSubGroups());
		}
		if (sourceGroupNode == null && targetNode != null) {
			Logging.debug(this, "canImport targetNode.allowsOnlyGroupChilds() " + targetNode.allowsOnlyGroupChilds());
		}

		Logging.debug(this, "canImport, dropOnThis  path " + Arrays.toString(dropObjectPath));
		Logging.debug(this, "canImport source path " + Arrays.toString(sourceObjectPath));

		boolean result = true;

		if (targetNode == null || targetNode.isImmutable() || (sourceGroupNode != null && !targetNode.allowsSubGroups())
				|| (sourceGroupNode == null && targetNode.allowsOnlyGroupChilds()) || (sourceGroupNode != null // group
						&& sourceObjectPath.length > 1 && dropObjectPath.length > 1
						&& !(sourceObjectPath[1].equals(dropObjectPath[1])))) {
			result = false;
		}

		Logging.debug(this, "canImport, dropOnThis " + dropOnThis.getUserObject());
		Logging.debug(this, "can import: " + result);

		return result;
	}

	@Override
	public int getSourceActions(JComponent c) {

		Logging.debug(this, "getSourceActions,  activePaths " + tree.getActivePaths());

		if (tree.getActivePaths() == null || tree.getActivePaths().isEmpty()) {
			Logging.debug(this, "getSourceActions no active pathes, TransferHandler.NONE");
			return TransferHandler.NONE;
		}

		DefaultMutableTreeNode dropThis = (DefaultMutableTreeNode) tree.getActivePaths().get(0).getLastPathComponent();

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

		Iterator<TreePath> iterPaths = tree.getActivePaths().iterator();

		while (iterPaths.hasNext()) {
			TreePath path = iterPaths.next();

			if (tree.isChildOfALL((DefaultMutableTreeNode) path.getLastPathComponent())) {
				Logging.debug(this, "getSourceActions path " + path + " childOfALL, should be TransferHandler.COPY");
				return TransferHandler.COPY;
				// we dont accept to move any item out of ALL
			}

			if (tree.isInDIRECTORY(path)) {
				Logging.debug(this, "getSourceActions , isInDIRECTORY true");
				// action depends additionally from target

			}
		}

		Logging.debug(this, "getSourceActions all remaining, TransferHandler.COPY_OR_MOVE");

		return TransferHandler.COPY_OR_MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {

		StringBuilder buff = new StringBuilder();
		Iterator<TreePath> iterPaths = tree.getActivePaths().iterator();

		while (iterPaths.hasNext()) {
			TreePath path = iterPaths.next();

			// String id = ((DefaultMutableTreeNode)

			int len = path.getPath().length;
			for (int j = 0; j < len; j++) {
				buff.append(path.getPath()[j]);
				if (j < len - 1) {
					buff.append(",");
				}
			}

			if (iterPaths.hasNext()) {
				buff.append("\n");
			}
		}

		return new StringSelection(buff.toString());

	}

	private boolean chooseMOVE(TransferHandler.TransferSupport support, String sourceGroupName, TreePath dropPath,
			boolean isLeaf) {
		Logging.info(this, "chooseMOVE  support " + support);
		Logging.info(this, "chooseMOVE  sourceGroupName, dropPath " + sourceGroupName + " , " + dropPath);
		Logging.info(this,
				"chooseMOVE support.getUserDropAction() == TransferHandler.MOVE "
						+ (support.getUserDropAction() == TransferHandler.MOVE) + " support.getUserDropAction() "
						+ support.getUserDropAction());

		boolean result = false;

		boolean stayInsideDIRECTORY = tree.isInDIRECTORY(sourceGroupName) && tree.isInDIRECTORY(dropPath);
		boolean stayInsideGROUPS = tree.isInGROUPS(sourceGroupName) && tree.isInGROUPS(dropPath);

		Logging.info(this,
				"chooseMOVE  stayInsideDIRECTORY,  stayInsideGROUPS " + stayInsideDIRECTORY + ", " + stayInsideGROUPS);

		if (stayInsideDIRECTORY || stayInsideGROUPS && !isLeaf) {
			result = true;
		}

		Logging.debug(this, "chooseMOVE  " + result);

		return result;

	}

	private void handleClientID(String importID, TransferHandler.TransferSupport support, TreePath sourcePath,
			GroupNode sourceParentNode, String sourceParentID, TreePath dropPath, DefaultMutableTreeNode dropParentNode,
			String dropParentID) {
		Logging.debug(this,
				"handleClientID importID, sourcePath, sourceParentID, sourceParentNode, dropParentID,  " + importID
						+ ", " + sourcePath + " , " + sourceParentID + ", " + sourceParentNode + ", " + dropParentID);

		boolean moving = false;

		String adaptedSourceParentID = sourceParentID;
		GroupNode adaptedSourceParentNode = sourceParentNode;

		// we are in table and did not get a real souce path
		if (sourcePath == null) {
			String firstDIRECTORYgroupname = null;
			Set<GroupNode> locations = tree.getLocationsInDIRECTORY(importID);
			if (locations != null && !locations.isEmpty()) {
				Logging.debug(this, "handleClientID tree.getLocationsInDIRECTORY 1");
				Iterator<GroupNode> iter = tree.getLocationsInDIRECTORY(importID).iterator();
				firstDIRECTORYgroupname = iter.next().toString();
				Logging.debug(this, "handleClientID tree.getLocationsInDIRECTORY firstDIRECTORYgroupname "
						+ firstDIRECTORYgroupname);
				adaptedSourceParentID = firstDIRECTORYgroupname;
				moving = chooseMOVE(support, firstDIRECTORYgroupname, dropPath, true);

				adaptedSourceParentNode = tree.getGroupNode(adaptedSourceParentID);
			}
		} else {
			moving = chooseMOVE(support, adaptedSourceParentID, dropPath, true);
		}

		tree.clientCopyOrMoveTo(importID, sourcePath, adaptedSourceParentID, adaptedSourceParentNode, dropParentNode,
				dropPath, dropParentID, moving);
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		// we are at a group node

		// where we want to move/copy to
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		TreePath dropPath = dropLocation.getPath();

		DefaultMutableTreeNode dropParentNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
		String dropParentID = dropParentNode.getUserObject().toString();

		Logging.debug(this, "dropPath " + dropPath);

		// what is to be moved/copied

		Logging.debug(this, "importData, getActivePaths(): " + tree.getActivePaths());

		String[] values = tree.getSelectedClientsInTable().toArray(new String[] {});

		// possibly transfer of a group node
		if (values.length == 0) {
			List<TreePath> activePaths = tree.getActivePaths();
			if (activePaths != null && activePaths.size() == 1) {
				String importID = (String) (((DefaultMutableTreeNode) (activePaths.get(0)).getLastPathComponent())
						.getUserObject());
				values = new String[] { importID };
			}
		}

		Logging.debug(this, "importData, values: " + Arrays.toString(values));

		TreePath groupPathActivatedByTree = tree.getGroupPathActivatedByTree();
		Logging.debug(this, "importData, groupPathActivatedByTree: " + groupPathActivatedByTree);

		// if the source is the tree then we arranged lines for the transfer
		// the other possible source are lines from the JTable, as well arranged to
		// lines

		// Perform the actual import.
		for (int i = 0; i < values.length; i++) {
			String value = values[i];

			String importID = null;
			String sourceParentID = null;
			try {
				// if values not got from transferable, the following reduces

				if (value.split("\t").length > 1) {
					// probably an import from the JTable

					// we assume a table source with first fieldvalue being a clientID
					importID = value.split("\t")[0];
				} else {
					String[] parts = value.split(",");

					importID = parts[parts.length - 1];

				}
			} catch (Exception ex) {
				Logging.info(this, " no tree parts got " + ex);
			}

			Logging.debug(this, "importData " + i + " values[i] " + importID);

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
				if (chooseMOVE(support, sourceParentID, dropPath, false)) {
					tree.moveGroupTo(importID, groupNode, sourceParentNode, dropParentNode, dropPath, dropParentID);
				} else {
					Logging.info(this, "importData: this group will not be moved");
				}
			} else {
				// client node
				Logging.debug(this, "importData handling client ID " + importID);

				handleClientID(importID, support, sourcePath, sourceParentNode, sourceParentID, dropPath,
						dropParentNode, dropParentID);

			}

			Logging.debug(this, "importData ready " + i + " importID " + importID);

		}

		return true;
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		Logging.debug(this, " exportToClipboard " + comp + " , " + clip + ", " + action);
		super.exportToClipboard(comp, clip, action);
	}
}
