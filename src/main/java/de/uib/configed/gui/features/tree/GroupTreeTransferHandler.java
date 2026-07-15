/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.logging.Logging;

public class GroupTreeTransferHandler extends TransferHandler {
	public static final String CLIENT_TABLE = "clientTable";
	public static final String PRODUCT_TABLE = "productTable";

	private AbstractGroupTree tree;

	private JComponent source;
	private String tableType;

	public GroupTreeTransferHandler(AbstractGroupTree tree, String tableType) {
		super();
		this.tree = tree;
		this.tableType = tableType;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		Logging.debug(this, "can import?");
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()
				|| !canImportToThisComponent(support.getComponent())) {
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
		if (source instanceof JTable) {
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

		if (sourcePaths == null || sourcePaths.length == 0) {
			return false;
		}

		boolean result = false;
		GroupNode selectedGroup = null;
		int groupCount = 0;

		for (TreePath path : sourcePaths) {
			GroupNode node = tree.getGroupNode(path.getLastPathComponent().toString());

			if (node != null) {
				groupCount++;
				selectedGroup = node;
			}
		}

		if (groupCount == 0) {
			result = isNormalGroup(targetNode);
		} else if (groupCount == 1 && sourcePaths.length == 1) {
			result = isValidDropTarget(selectedGroup, targetNode)
					&& shouldMoveNode((String) selectedGroup.getUserObject(), new TreePath(targetNode.getPath()), true);
		} else {
			// Do nothing.
		}

		return result;
	}

	private static boolean isValidDropTarget(GroupNode sourceNode, GroupNode targetNode) {
		return isNormalGroup(sourceNode) && !targetNode.isImmutable()
				&& !ClientTree.DIRECTORY_NOT_ASSIGNED_NAME.equals(targetNode.toString())
				&& nodesAreNotEqualOrAncestor(sourceNode, targetNode);
	}

	/*
	 * We check if the source and target node are not equal and not ancestor of each other.
	 * But we exclude the ALL_GROUPS node as source, because it's not a "real" group.
	*/
	private static boolean nodesAreNotEqualOrAncestor(GroupNode sourceNode, GroupNode targetNode) {
		return (!sourceNode.equals(targetNode) && !targetNode.isNodeAncestor(sourceNode)
				&& sourceNode.getParent() != targetNode);
	}

	private boolean canImportToThisComponent(Component target) {
		return switch (target) {
		case ClientTree _ -> CLIENT_TABLE.equals(tableType) || source instanceof ClientTree;
		case ProductTree _ -> PRODUCT_TABLE.equals(tableType) || source instanceof ProductTree;
		default -> {
			Logging.debug(this, "The target is not a Client or product tree, but ", target.getClass().getName());
			yield false;
		}
		};
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
	private boolean shouldMoveNode(String sourceGroupName, TreePath dropPath, boolean isLeaf) {
		Logging.info(this, "shouldMoveNode sourceGroupName, dropPath ", sourceGroupName, " , ", dropPath);

		boolean stayInsideDIRECTORY = tree.isInDirectory(sourceGroupName) && tree.isInDirectory(dropPath);
		boolean stayInsideGROUPS = tree.isInGROUPS(sourceGroupName) && tree.isInGROUPS(dropPath) && isLeaf;

		Logging.info(this, "shouldMovenOde stayInsideDIRECTORY,", stayInsideDIRECTORY, ", stayInsideGROUPS",
				stayInsideGROUPS);

		boolean result = stayInsideDIRECTORY || stayInsideGROUPS;
		Logging.debug(this, "shouldMoveNode", result);

		return result;
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (source instanceof JTable) {
			return importFromTable(support);
		} else {
			return importFromTree(support);
		}
	}

	private boolean importFromTree(TransferSupport support) {
		TreePath[] selectionPaths = ((AbstractGroupTree) source).getSelectionPaths();
		Set<String> selectedObjects = selectionPaths == null ? Collections.emptySet()
				: Arrays.stream(selectionPaths).map(path -> path.getLastPathComponent().toString())
						.collect(Collectors.toSet());

		return importObjects(selectedObjects, support);
	}

	private boolean importFromTable(TransferSupport support) {
		Set<String> selectedObjects;
		if (CLIENT_TABLE.equals(tableType)) {
			selectedObjects = ConfigedMain.getMainFrame().getClientTablePanel().getSelectedSet();
		} else {
			selectedObjects = ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration()
					.getProductPageManager().getPanelInUse().getProductTableModified().getSelectedIDs();
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

		if (!confirmMovingNodes(selectedObjects.size(), dropParentID)) {
			return false;
		}

		Logging.debug(this, "dropPath ", dropPath);

		// Perform the actual import, but in sorted order
		for (String selectedObject : new TreeSet<>(selectedObjects)) {
			Logging.debug(this, "importData ", selectedObject);

			Logging.debug(this, "active source tree path for selectedObject ", selectedObject);

			if (CLIENT_TABLE.equals(tableType) || PRODUCT_TABLE.equals(tableType)) {
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
			tree.moveGroupTo(selectedObject, groupNode, sourceParentNode, dropParentNode, dropPath, dropParentID);
		} else {
			// import node
			Logging.debug(this, "importData handling selectedObject ", selectedObject);
			if (shouldMoveNode(sourceParentID, dropPath, false)) {
				tree.moveObjectTo(selectedObject, sourceParentID, sourceParentNode, dropParentNode, dropPath,
						dropParentID);
			} else {
				tree.copyObjectTo(selectedObject, dropParentID, dropParentNode, dropPath);
			}
		}

		Logging.debug(this, "importData ready, selectedObject ", selectedObject);
	}

	private static boolean confirmMovingNodes(int amount, String group) {
		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
				String.format(Configed.getResourceValue("GroupTreeTransferHandler.confirmMovingNode.message"), amount,
						group),
				Configed.getResourceValue("GroupTreeTransferHandler.confirmMovingNode.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		return answer == JOptionPane.YES_OPTION;
	}
}
