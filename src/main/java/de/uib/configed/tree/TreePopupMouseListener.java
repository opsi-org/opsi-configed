package de.uib.configed.tree;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;

public class TreePopupMouseListener extends utils.PopupMouseListener {

	private ClientTree tree;

	private TreePath mousePath = null;

	private JMenuItem menuItemCreateNode;
	private JMenuItem menuItemEditNode;
	private JMenuItem menuItemDeleteNode;
	private JMenuItem menuItemDeleteGroupNode;
	private JMenuItem menuItemActivateElements;
	private JMenuItem menuItemRemoveElements;

	public TreePopupMouseListener(JPopupMenu jPopupMenu, ClientTree tree, ConfigedMain configedMain) {
		super(jPopupMenu);
		this.tree = tree;

		menuItemCreateNode = new JMenuItem(Configed.getResourceValue("ClientTree.addNode"));
		menuItemCreateNode.addActionListener(actionEvent -> makeSubGroup());
		jPopupMenu.add(menuItemCreateNode);

		menuItemEditNode = new JMenuItem(Configed.getResourceValue("ClientTree.editNode"));
		menuItemEditNode.addActionListener(actionEvent -> tree.editGroupNode(mousePath));
		jPopupMenu.add(menuItemEditNode);

		menuItemDeleteNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteNode"));
		menuItemDeleteNode.addActionListener(actionEvent -> tree.deleteNode(mousePath));
		jPopupMenu.add(menuItemDeleteNode);

		menuItemDeleteGroupNode = new JMenuItem(Configed.getResourceValue("ClientTree.deleteGroupNode"));
		menuItemDeleteGroupNode.addActionListener(actionEvent -> tree.deleteNode(mousePath));
		jPopupMenu.add(menuItemDeleteGroupNode);

		menuItemActivateElements = new JMenuItem(Configed.getResourceValue("ClientTree.selectAllElements"));
		menuItemActivateElements.addActionListener(actionEvent -> activateElements(configedMain));
		jPopupMenu.add(menuItemActivateElements);

		menuItemRemoveElements = new JMenuItem(Configed.getResourceValue("ClientTree.removeAllElements"));
		menuItemRemoveElements.addActionListener(actionEvent -> removeElements(configedMain));
		jPopupMenu.add(menuItemRemoveElements);
	}

	private void makeSubGroup() {
		IconNode resultNode = tree.makeSubgroupAt(mousePath);
		if (resultNode != null) {
			tree.makeVisible(tree.pathByAddingChild(mousePath, resultNode));
			tree.repaint();
		}
	}

	private void activateElements(ConfigedMain configedMain) {

		TreePath sourcePath = mousePath;
		if (sourcePath != null && sourcePath.getPathComponent(sourcePath.getPathCount() - 1) instanceof GroupNode) {

			GroupNode node = (GroupNode) sourcePath.getPathComponent(sourcePath.getPathCount() - 1);
			configedMain.setGroup(node.toString());
		}
	}

	private void removeElements(ConfigedMain configedMain) {

		if (mousePath != null && mousePath.getPathComponent(mousePath.getPathCount() - 1) instanceof GroupNode) {

			GroupNode node = (GroupNode) mousePath.getPathComponent(mousePath.getPathCount() - 1);

			Enumeration<TreeNode> enumer = node.breadthFirstEnumeration();

			List<DefaultMutableTreeNode> clientNodesToRemove = new ArrayList<>();

			while (enumer.hasMoreElements()) {
				DefaultMutableTreeNode element = (DefaultMutableTreeNode) enumer.nextElement();
				if (!element.getAllowsChildren())
					clientNodesToRemove.add(element);
			}

			if (tree.removeClientNodes(clientNodesToRemove)) {
				configedMain.setGroup(node.toString()); // refresh internal view
			}
		}
	}

	protected boolean checkAccepted(MouseEvent e) {
		if (!tree.isEnabled())
			return false;

		int mouseRow = tree.getRowForLocation(e.getX(), e.getY());
		mousePath = tree.getPathForLocation(e.getX(), e.getY());

		if (mouseRow == -1) // no node selection area
		{
			mousePath = null;
			return false;
		}

		Logging.debug(this, "checkAccepted clickPath  " + mousePath);

		DefaultMutableTreeNode clickNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent();

		String nodeName = clickNode.getUserObject().toString();

		if (tree.getGroupNode(nodeName) != null && clickNode != tree.getGroupNode(nodeName)) {
			Logging.warning(this, "checkAccepted clickNode != tree.getGroupNode(nodeName)");
			clickNode = tree.getGroupNode(nodeName);
		}

		Logging.debug(this, "checkAccepted clickNode.getParent() " + clickNode.getParent());

		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) clickNode.getParent();

		if (nodeName.equals(ClientTree.ALL_CLIENTS_NAME)
				|| (((DefaultMutableTreeNode) clickNode.getParent()).getUserObject().toString()
						.equals(ClientTree.ALL_CLIENTS_NAME) && !nodeName.equals(ClientTree.ALL_GROUPS_NAME)))

			return false; // dont show here any menu

		menuItemCreateNode.setVisible(false); // creation of subgroup
		menuItemEditNode.setVisible(false); // edit
		menuItemDeleteNode.setVisible(false); // deletion
		menuItemDeleteGroupNode.setVisible(false); // deletion
		menuItemActivateElements.setVisible(false); // removal of non-groupnode
													// elements
		menuItemRemoveElements.setVisible(false); // edit

		int numberVisibleItems = 0;

		if (clickNode.getAllowsChildren()) {

			if (((GroupNode) clickNode).allowsSubGroups()) {
				menuItemCreateNode.setVisible(true); // creation of subgroup
				numberVisibleItems++;
			}

			if (((GroupNode) clickNode).allowsSubGroups() && !(((GroupNode) clickNode).isFixed())) {
				menuItemEditNode.setVisible(true); // edit this node
				numberVisibleItems++;
			}

			if (!(((GroupNode) clickNode).isFixed())) {
				menuItemDeleteGroupNode.setVisible(true); // deletion
				numberVisibleItems++;
			}

			menuItemActivateElements.setVisible(true); // activate elements
			numberVisibleItems++;

			if (!(((GroupNode) clickNode).isFixed())) {
				menuItemRemoveElements.setVisible(true); // delete non-groupnode
															// elements
				numberVisibleItems++;
			}

		} else if (!(((GroupNode) parentNode).isFixed())) {
			menuItemDeleteNode.setVisible(true);
			numberVisibleItems++;
		}

		return (numberVisibleItems > 0);
	}

	@Override
	protected void maybeShowPopup(MouseEvent e) {
		if (checkAccepted(e))
			super.maybeShowPopup(e);
	}

}
