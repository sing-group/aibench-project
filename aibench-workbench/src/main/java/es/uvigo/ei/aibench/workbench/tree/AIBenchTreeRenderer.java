/*
 * #%L
 * The AIBench Workbench Plugin
 * %%
 * Copyright (C) 2006 - 2016 Daniel Glez-Peña and Florentino Fdez-Riverola
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package es.uvigo.ei.aibench.workbench.tree;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.history.HistoryElement;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author Ruben Dominguez Carbajales 07-feb-2006
 *
 */
public class AIBenchTreeRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
		int row, boolean hasFocus
	) {

		JLabel c = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

		if (parent != null && parent.getUserObject() instanceof OperationDefinition<?>) {
			// its a params node

		} else if (leaf && !(node.getUserObject() instanceof ClipboardItem)) {
			c.setIcon(null);
		}

		if (node.getUserObject() instanceof ClipboardItem) {
			c.setText(((ClipboardItem) node.getUserObject()).getName());
			ImageIcon icon = Workbench.getInstance()
					.getDataTypeIcon(((ClipboardItem) node.getUserObject()).getRegisteredUserClass());
			if (icon == null) {
				c.setIcon(new ImageIcon(getClass().getResource("/images/attach.png")));
			} else {
				c.setIcon(icon);
			}
		} else if (node.getUserObject() instanceof HistoryElement) {
			c.setText(((HistoryElement) node.getUserObject()).getOperation().getName());
			ImageIcon icon = Workbench.getInstance()
					.getOperationIcon(((HistoryElement) node.getUserObject()).getOperation());
			if (icon == null) {
				c.setIcon(new ImageIcon(getClass().getResource("/images/process.png")));
			} else {
				c.setIcon(icon);
			}

		} else if (node.equals(tree.getModel().getRoot())) {
			c.setIcon(new ImageIcon(getClass().getResource("/images/session.gif")));
			c.setText("");
		} else {
			c.setText(value.toString());

			if (leaf) {
				c.setToolTipText(node.getUserObject().toString());
			} else {
				c.setToolTipText(null);
			}
		}

		return c;
	}
}
