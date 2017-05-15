/*
 * #%L
 * The AIBench Plugin Manager Plugin
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
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
package es.uvigo.ei.sing.aibench.pluginmanager.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.platonos.pluginengine.Dependency;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngineException;

import es.uvigo.ei.aibench.repository.info.PluginInfo;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManager;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class PluginInformationPane extends JScrollPane {
	private final static Logger logger = Logger.getLogger(PluginInformationPane.class);
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private static final int UID_INDEX = 0;
//	/**
//	 * 
//	 */
//	private static final int ACTIVE_INDEX = 3;
	/**
	 * 
	 */
	private static final int ENABLED_INDEX = 4;
	/**
	 * 
	 */
	private static final int DOWNLOAD_INDEX = 5;
	private static final String ROOT_LABEL = "Plugins";
	
	private static final List<String> COLUMN_NAMES = Arrays.asList(new String[] {
		"Plugin/Dependency",
		"Version",
		"Version Required",
		"Active",
		"Enabled",
		"Update"
	});
	
	private final JXTreeTable pluginsTreeTable = new JXTreeTable();
	private PluginInformationTreeTableModel treeTableModel;
	
	public PluginInformationPane() {
		super();
		
		this.updateModel();
		
		this.setViewportView(this.pluginsTreeTable);
		this.pluginsTreeTable.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.pluginsTreeTable.getColumnExt(PluginInformationPane.ENABLED_INDEX).setCellEditor(
			new BooleanCellEditor()
		);
		this.pluginsTreeTable.getColumnExt(PluginInformationPane.DOWNLOAD_INDEX).setCellRenderer(
			new DefaultTableRenderer(new PluginActionProvider())
		);
		this.pluginsTreeTable.addHighlighter(new PluginInformationHighlighter());
		
		this.pluginsTreeTable.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				PluginInformationPane.this.pluginsTreeTableMouseClicked(e);
			}
		});
	}
	
	private synchronized void updateModel() {
		final InformationTreeTableNode root = 
			new InformationTreeTableNode(PluginInformationPane.ROOT_LABEL);
		InformationTreeTableNode node, leaf;
		
		for (Plugin plugin:PluginManager.getInstance().getActivePlugins()) {
			node = new PluginTreeTableNode(plugin);
			for (Dependency dependency:plugin.getDependencies()) {
				leaf = new DependencyTreeTableNode(dependency);
				node.add(leaf);
			}
			
			root.add(node);
		}
		
		for (Plugin plugin:PluginManager.getInstance().getInactivePlugins()) {
			node = new PluginTreeTableNode(plugin);
			for (Dependency dependency:plugin.getDependencies()) {
				leaf = new DependencyTreeTableNode(dependency);
				node.add(leaf);
			}
			
			root.add(node);
		}
		this.treeTableModel = new PluginInformationTreeTableModel(root);
		this.pluginsTreeTable.setTreeTableModel(this.treeTableModel);
		
		this.pluginsTreeTable.packAll();
	}
	
	private void pluginsTreeTableMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			synchronized(this) {
				int selectedRow = this.pluginsTreeTable.getSelectedRow();
				if (selectedRow != -1) {
					TreePath path = this.pluginsTreeTable.getPathForRow(selectedRow);
					Object node = path.getLastPathComponent();
					if (node instanceof DependencyTreeTableNode) {
						Dependency dependency = ((DependencyTreeTableNode) node).getUserObject();
						Enumeration<? extends TreeTableNode> children = this.treeTableModel.getRoot().children();
						TreeTableNode child;
						Object value;
						while (children.hasMoreElements()) {
							child = children.nextElement();
							value = child.getUserObject();
							if (value instanceof Plugin) {
								if (((Plugin) value).getUID().equalsIgnoreCase(dependency.getResolveToPluginUID())) {
									TreePath treePath = new TreePath(new Object[]{this.treeTableModel.getRoot(), child});
									int index = this.pluginsTreeTable.getRowForPath(treePath);
									this.pluginsTreeTable.setRowSelectionInterval(index, index);
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * @author Miguel Reboiro Jato
	 *
	 */
	private final class PluginInformationHighlighter extends ColorHighlighter {
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.decorator.ColorHighlighter#applyBackground(java.awt.Component, org.jdesktop.swingx.decorator.ComponentAdapter)
		 */
		@Override
		protected void applyBackground(Component renderer, ComponentAdapter adapter) {
			if (!adapter.isSelected()) {
				Color ok, error, disabled;
				if (adapter.getDepth() == 1) {
					ok = new Color(157, 204, 88);
					error = new Color(204, 0, 20);
					disabled = Color.darkGray;
				} else {
					ok = new Color(208, 217, 146);
					error = new Color(230, 80, 70);
					disabled = Color.lightGray;
				}
				Object value = adapter.getValue(PluginInformationPane.UID_INDEX);
				if (value instanceof String) {
					String uid = (String) value;
					if (PluginManager.getInstance().isActive(uid) && PluginManager.getInstance().isCurrentlyEnabled(uid)) {
						renderer.setBackground(ok);
					} else if (!PluginManager.getInstance().isCurrentlyEnabled(uid)) {
						renderer.setBackground(disabled);
					} else if (!PluginManager.getInstance().isActive(uid)) {
						renderer.setBackground(error);
					}
				}
			}
			super.applyBackground(renderer, adapter);
		}
	}
	
	private final class PluginInformationTreeTableModel extends DefaultTreeTableModel {
		public PluginInformationTreeTableModel(TreeTableNode root) {
			super(root, PluginInformationPane.COLUMN_NAMES);
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int column) {
			if (column == PluginInformationPane.ENABLED_INDEX) {
				return Boolean.class;
			} else {
				return super.getColumnClass(column);
			}
		}
	}
	
	private class InformationTreeTableNode extends DefaultMutableTreeTableNode {
		/**
		 * @param userObject
		 */
		public InformationTreeTableNode(Object userObject) {
			super(userObject);
		}
		
		/**
		 * @param userObject
		 * @param allowsChildren
		 */
		public InformationTreeTableNode(Object userObject, boolean allowsChildren) {
			super(userObject, allowsChildren);
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode#isEditable(int)
		 */
		@Override
		public boolean isEditable(int column) {
			return false;
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return PluginInformationPane.COLUMN_NAMES.size();
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode#getValueAt(int)
		 */
		@Override
		public Object getValueAt(int column) {
			return (column==0)?this.getUserObject():"";
		}
	}
	
	private final class PluginTreeTableNode extends InformationTreeTableNode {
		private final Plugin plugin;
		
		public PluginTreeTableNode(Plugin plugin) {
			super(plugin, true);
			this.plugin = plugin;
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode#getUserObject()
		 */
		@Override
		public Plugin getUserObject() {
			return this.plugin;
		}
		
		/* (non-Javadoc)
		 * @see es.uvigo.ei.sing.aibench.pluginmanager.PluginInformationComponent.InformationTreeTableNode#isEditable(int)
		 */
		@Override
		public boolean isEditable(int column) {
			return (column == PluginInformationPane.ENABLED_INDEX);
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode#setValueAt(java.lang.Object, int)
		 */
		@Override
		public void setValueAt(Object value, int column) {
			if (column == PluginInformationPane.ENABLED_INDEX && value instanceof Boolean) {
				PluginManager.getInstance().setEnabled(this.plugin, (Boolean) value);
				PluginInformationPane.this.repaint();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode#getValueAt(int)
		 */
		@Override
		public Object getValueAt(int column) {
			switch(column) {
			case 0:
				return this.plugin.getUID();
			case 1:
				return this.plugin.getVersion();
			case 3:
				return PluginManager.getInstance().isActive(this.plugin);
			case 4:
				return PluginManager.getInstance().isEnabled(this.plugin);
			case 5:
				return this.plugin.getUID();
			default:
				return super.getValueAt(column);
			}
		}
	}
	
	private final class DependencyTreeTableNode extends InformationTreeTableNode {
		private final Dependency dependency;
		private final Plugin plugin;
		
		public DependencyTreeTableNode(Dependency dependency) {
			super(dependency, false);
			this.dependency = dependency;
			Plugin plugin = this.dependency.getResolvedToPlugin();
			if (plugin == null) {
				plugin = PluginManager.getInstance().getInstalledPlugin(dependency.getResolveToPluginUID());
			}
			this.plugin = plugin;
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode#getUserObject()
		 */
		@Override
		public Dependency getUserObject() {
			return this.dependency;
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode#getValueAt(int)
		 */
		@Override
		public Object getValueAt(int column) {
			if (this.dependency == null) return null;
			switch(column) {
			case 0:
				return this.dependency.getResolveToPluginUID();
			case 1:
				return (this.plugin == null)?"none":this.plugin.getVersion();
			case 2:
				String toret = "";
				if (this.dependency.getRequiredVersion() != null) {
					toret = this.dependency.getRequiredVersion().toString();
				}
				if (toret.length() == 0) {
					if (this.dependency.isOptional())
						toret += "Optional";
				} else {
					if (this.dependency.isOptional())
						toret += " - Optional";
				}
				return toret;
			case 3:
				return PluginManager.getInstance().isActive(this.dependency.getResolveToPluginUID());
			case 4:
				return PluginManager.getInstance().isEnabled(this.dependency.getResolveToPluginUID());
			case 5:
				if (PluginManager.getInstance().isDownloaderActive()) {
					PluginInfo install = PluginManager.getInstance().getDownloadPluginInfo(this.dependency.getResolveToPluginUID());
					if (!PluginManager.getInstance().pluginExists(this.dependency.getResolveToPluginUID()) && install != null) {
						try {
							if (this.dependency.getRequiredVersion().compareTo(install.getPluginVersion()) == 0) {
								return install.getUID();
							}
						} catch (PluginEngineException e) {
							PluginInformationPane.logger.warn("Error getting PluginInfo required version", e);
						}
					}
				}
				return null;
			default:
				return super.getValueAt(column);
			}
		}
	}
}
