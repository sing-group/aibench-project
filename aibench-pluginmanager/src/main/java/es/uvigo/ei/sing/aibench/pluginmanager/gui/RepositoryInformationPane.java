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
import java.util.ArrayList;
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
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngineException;

import es.uvigo.ei.aibench.repository.NotInitializedException;
import es.uvigo.ei.aibench.repository.info.DependencyInfo;
import es.uvigo.ei.aibench.repository.info.PluginInfo;
import es.uvigo.ei.sing.aibench.pluginmanager.PluginManager;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class RepositoryInformationPane extends JScrollPane {
	private final static Logger logger = Logger.getLogger(RepositoryInformationPane.class);
	
	/**
	 * 
	 */
	private static final int DOWNLOAD_INDEX = 4;

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String ROOT_LABEL = "Repository";
	
	private static final List<String> COLUMN_NAMES = new ArrayList<String>();
	
	static {
		COLUMN_NAMES.add("Plugin/Dependencies");
		COLUMN_NAMES.add("Installed Version");
		COLUMN_NAMES.add("Repository Version");
		COLUMN_NAMES.add("Required Version");
		COLUMN_NAMES.add("Download");
	}
	
	private final JXTreeTable pluginsTreeTable = new JXTreeTable();
	private PluginInformationTreeTableModel treeTableModel;
	
	public RepositoryInformationPane() throws NotInitializedException {
		super();
		this.setViewportView(this.pluginsTreeTable);
		this.updateModel();
		this.pluginsTreeTable.getColumnExt(RepositoryInformationPane.DOWNLOAD_INDEX).setCellRenderer(
			new DefaultTableRenderer(new PluginActionProvider())
		);
		
		this.pluginsTreeTable.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.pluginsTreeTable.addHighlighter(new RepositoryHighlighter());
		
		this.pluginsTreeTable.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				RepositoryInformationPane.this.pluginsTreeTableMouseClicked(e);
			}
		});
	}
	
	public void packColumns() {
		this.pluginsTreeTable.packAll();
	}
	
	private synchronized void updateModel() throws NotInitializedException {
		DefaultMutableTreeTableNode root = new InformationTreeTableNode(RepositoryInformationPane.ROOT_LABEL);
		DefaultMutableTreeTableNode node, leaf;
		
		for (PluginInfo plugin:PluginManager.getInstance().getRepositoryInfo()) {
			node = new PluginTreeTableNode(plugin);
			for (DependencyInfo dependency:plugin.getListNeeds()) {
				leaf = new DependencyTreeTableNode(dependency);
				node.add(leaf);
			}
			
			root.add(node);
		}
		
		this.treeTableModel = new PluginInformationTreeTableModel(root);
		this.pluginsTreeTable.setTreeTableModel(this.treeTableModel);
		
		this.pluginsTreeTable.packAll();
		this.repaint();
	}
	
	private void pluginsTreeTableMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			synchronized(this) {
				int selectedRow = this.pluginsTreeTable.getSelectedRow();
				if (selectedRow != -1) {
					TreePath path = this.pluginsTreeTable.getPathForRow(selectedRow);
					Object node = path.getLastPathComponent();
					if (node instanceof DependencyTreeTableNode) {
						DependencyInfo dependency = ((DependencyTreeTableNode) node).getUserObject();
						Enumeration<? extends TreeTableNode> children = this.treeTableModel.getRoot().children();
						TreeTableNode child;
						Object value;
						while (children.hasMoreElements()) {
							child = children.nextElement();
							value = child.getUserObject();
							if (value instanceof PluginInfo) {
								if (((PluginInfo) value).getUID().equalsIgnoreCase(dependency.getUid())) {
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
	
	private final class RepositoryHighlighter extends ColorHighlighter {
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.decorator.ColorHighlighter#applyBackground(java.awt.Component, org.jdesktop.swingx.decorator.ComponentAdapter)
		 */
		@Override
		protected void applyBackground(Component renderer, ComponentAdapter adapter) {
			if (!adapter.isSelected()) {
				Color installable = new Color(100, 100, 200);
				Color updatable = new Color(100, 200, 100);
				Color depInstallable = new Color(150, 150, 220);
				Color depUpdatable = new Color(150, 220, 150);
				Color depLost = new Color(200, 125, 125);
				
				Object row = RepositoryInformationPane.this.pluginsTreeTable.getPathForRow(adapter.row).getLastPathComponent();
				if (row instanceof InformationTreeTableNode) {
					Object value = ((InformationTreeTableNode) row).getUserObject();
					if (value instanceof PluginInfo) {
						PluginInfo info = (PluginInfo) value;
						
						try {
							Plugin plugin = PluginManager.getInstance().getInstalledPlugin(info.getUID());
							if (plugin == null) {
								renderer.setBackground(installable);
							} else if (info.getPluginVersion().compareTo(plugin.getVersion()) > 0) {
								renderer.setBackground(updatable);
							}
						} catch (PluginEngineException pee) {
							RepositoryInformationPane.logger.warn("Error getting PluginInfo version", pee);
						}
					} else if (value instanceof DependencyInfo) {
						DependencyInfo info = (DependencyInfo) value;
						PluginInfo update = PluginManager.getInstance().getDownloadPluginInfo(info.getUid());
						Plugin plugin = PluginManager.getInstance().getInstalledPlugin(info.getUid());
						if (plugin == null && update == null) {
							renderer.setBackground(depLost);
						} else if (plugin == null && update != null) {
							try {
								if (info.getDependencyVersion().compareTo(update.getPluginVersion()) == 0) {
									renderer.setBackground(depInstallable);							
								} else {
									renderer.setBackground(depLost);
								}
							} catch (PluginEngineException pee) {
								RepositoryInformationPane.logger.warn("Error getting DependencyInfo version", pee);
							}
						} else if (plugin != null && update == null) {
							if (info.getDependencyVersion().compareTo(plugin.getVersion()) != 0) {
								renderer.setBackground(depLost);
							}
						} else {
							if (info.getDependencyVersion().compareTo(plugin.getVersion()) != 0) {
								try {
									if (info.getDependencyVersion().compareTo(update.getPluginVersion()) == 0) {
										renderer.setBackground(depUpdatable);							
									} else {
										renderer.setBackground(depLost);
									}
								} catch (PluginEngineException pee) {
									RepositoryInformationPane.logger.warn("Error getting DependencyInfo version", pee);
								}
							}
							
						}
					}
				}
			}
			
			super.applyBackground(renderer, adapter);
		}
	}
	
	private final class PluginInformationTreeTableModel extends DefaultTreeTableModel {
		public PluginInformationTreeTableModel(TreeTableNode root) {
			super(root, RepositoryInformationPane.COLUMN_NAMES);
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
		public InformationTreeTableNode(Object userObject,
				boolean allowsChildren) {
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
			return RepositoryInformationPane.COLUMN_NAMES.size();
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode#getValueAt(int)
		 */
		@Override
		public Object getValueAt(int column) {
			return (column==0)?this.getUserObject():"";
		}
	}
	
	private class PluginTreeTableNode extends InformationTreeTableNode {
		private final PluginInfo plugin;
		
		public PluginTreeTableNode(PluginInfo plugin) {
			super(plugin, true);
			this.plugin = plugin;
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode#getUserObject()
		 */
		@Override
		public PluginInfo getUserObject() {
			return this.plugin;
		}
		
		/* (non-Javadoc)
		 * @see es.uvigo.ei.sing.aibench.pluginmanager.gui.RepositoryInformationPane.InformationTreeTableNode#getValueAt(int)
		 */
		@Override
		public Object getValueAt(int column) {
			Plugin plugin = PluginManager.getInstance().getInstalledPlugin(this.plugin.getUID());
			switch(column) {
			case 0:
				return this.plugin.getUID();
			case 1:
				return (plugin == null)?null:plugin.getVersion().toString();
			case 2:
				try {
					return this.plugin.getPluginVersion().toString();
				} catch (PluginEngineException e) {
					return this.plugin.getVersion();
				}
			case 3:
				return null;
			case 4:
				return this.plugin.getUID();
			}
			return super.getValueAt(column);
		}
	}
	
	private class DependencyTreeTableNode extends InformationTreeTableNode {
		private final DependencyInfo dependency;
		public DependencyTreeTableNode(DependencyInfo dependency) {
			super(dependency, true);
			this.dependency = dependency;
		}
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode#getUserObject()
		 */
		@Override
		public DependencyInfo getUserObject() {
			return this.dependency;
		}
		
		/* (non-Javadoc)
		 * @see es.uvigo.ei.sing.aibench.pluginmanager.gui.RepositoryInformationPane.InformationTreeTableNode#getValueAt(int)
		 */
		@Override
		public Object getValueAt(int column) {
			Plugin plugin = PluginManager.getInstance().getInstalledPlugin(this.dependency.getUid());
			PluginInfo infoPlugin = PluginManager.getInstance().getDownloadPluginInfo(this.dependency.getUid());
			switch(column) {
			case 0:
				return this.dependency.getUid();
			case 1:
				return (plugin == null)?null:plugin.getVersion();
			case 2:
				try {
					return (infoPlugin == null)?null:infoPlugin.getPluginVersion();
				} catch (PluginEngineException e) {
					RepositoryInformationPane.logger.warn("Error getting PluginInfo version.", e);
					return null;
				}
			case 3:
				return this.dependency.getDependencyVersion();
			case 4:
				return null;
			}
			return super.getValueAt(column);
		}
	}
}
