/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part the AIBench Project. 

AIBench Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

AIBench Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with AIBench Project.  If not, see <http://www.gnu.org/licenses/>.
 */

/*  
 * AIBenchJTreeManager.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 * 
 * Created on 15/10/2005
 *
 */
package es.uvigo.ei.aibench.workbench.tree;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.Launcher;
import es.uvigo.ei.aibench.Util;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Property;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.aibench.core.history.HistoryElement;
import es.uvigo.ei.aibench.core.history.HistoryListener;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author Rubn Domnguez Carbajales 15-oct-2005
 */
public class AIBenchJTreeManager implements HistoryListener, ClipboardListener, Serializable {
	private static final long serialVersionUID = 1L;

	private static AIBenchJTreeManager instance = null;
	
	private final static String CLIPBOARD_LABEL = "Clipboard";
	private final static String SESSION_HISTORY_LABEL = "Session History";

	private JTree sessionTree = null;

	private JTree clipboardTree = null;
	
//	private HashMap<Class<?>, DefaultMutableTreeNode> classNodes = new HashMap<Class<?>, DefaultMutableTreeNode>();

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(AIBenchJTreeManager.class.getName());

	/*
	 * Used to listen to editing events in nodes (clipboard nodes)
	 */
	class MyTreeCellEditor extends DefaultTreeCellEditor {
		private Object lastEditingObject = null;

		public MyTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
			super(tree, renderer);

			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					lastEditingObject = null;
				}
			});

			this.addCellEditorListener(new CellEditorListener() {
				public void editingCanceled(ChangeEvent e) {
					// TODO Auto-generated method stub

				}

				public void editingStopped(ChangeEvent e) {
					if (lastEditingObject != null
							&& lastEditingObject instanceof ClipboardItem) {
						ClipboardItem item = (ClipboardItem) lastEditingObject;

						item.setName(MyTreeCellEditor.this.getCellEditorValue().toString());

						/**
						 * NOTE: Added by paulo maia
						 */
						Datatype datatypeAnnot = item.getUserData().getClass()
								.getAnnotation(Datatype.class);

						// try to use a method to set the name of the Datatype
						if (datatypeAnnot != null) {
							if (!datatypeAnnot.setNameMethod().equals("")) {
								try {
									Method m = item.getUserData().getClass().getMethod(datatypeAnnot.setNameMethod(), new Class[] { String.class });
									m.invoke(item.getUserData(), MyTreeCellEditor.this.getCellEditorValue().toString());

								} catch (SecurityException f) {
									// TODO Auto-generated catch block
									f.printStackTrace();
								} catch (NoSuchMethodException f) {
									logger.error("Couldn't find the set name method in the Complex item "
										+ item.getUserData().getClass()
										+ ":  "
										+ datatypeAnnot.setNameMethod()
									);
								} catch (IllegalArgumentException f) {
									// TODO Auto-generated catch block
									f.printStackTrace();
								} catch (IllegalAccessException f) {
									// TODO Auto-generated catch block
									f.printStackTrace();
								} catch (InvocationTargetException f) {
									// TODO Auto-generated catch block
									f.printStackTrace();
								}
							}
						}

						TreePath selPath = MyTreeCellEditor.this.tree.getSelectionPath();
						MyTreeCellEditor.this.tree.setSelectionPath(selPath);
						if (selPath != null) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
							node.setUserObject(lastEditingObject);
						}

						JTree clipboardTree = Workbench.getInstance().getTreeManager().getAIBenchClipboardTree();
						DefaultMutableTreeNode _node = Workbench.getInstance().getTreeManager().findNodeForUserObject(item,	clipboardTree);

						((DefaultTreeModel) clipboardTree.getModel()).nodeChanged(_node);

						JTree sessionTree = Workbench.getInstance().getTreeManager().getAIBenchTree();
						_node = Workbench.getInstance().getTreeManager().findNodeForUserObject(item, sessionTree);
						((DefaultTreeModel) sessionTree.getModel()).nodeChanged(_node);
					}
					lastEditingObject = null;
				}
			});
		}

		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row) {
			// if (this.lastEditingObject instanceof ClipboardItem){

			// }
			Component comp = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
			super.editingIcon = ((JLabel) this.renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true)).getIcon();
			return comp;
		}

		public boolean isCellEditable(EventObject anEvent) {
			if (lastEditingObject != null) {
				return false;
			}

			if (anEvent == null) {
				TreePath selPath = tree.getSelectionPath();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
				if (node.getUserObject() instanceof ClipboardItem) {
					
					ClipboardItem citem = (ClipboardItem) node.getUserObject();
					Datatype datatypeAnnot = citem.getUserData().getClass().getAnnotation(Datatype.class);
					if (datatypeAnnot != null && !datatypeAnnot.renameable()){
						return false;
					}
					else{
						return true;
					}
				}
			} else if (anEvent != null && anEvent instanceof MouseEvent) {
				if (((MouseEvent) anEvent).isPopupTrigger())
					return false;
				TreePath selPath = tree.getPathForLocation(
						((MouseEvent) anEvent).getPoint().x,
						((MouseEvent) anEvent).getPoint().y);
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
						.getLastPathComponent();
				if (node.getUserObject() instanceof ClipboardItem) {
					ClipboardItem citem = (ClipboardItem) node.getUserObject();
					Datatype datatypeAnnot = citem.getUserData().getClass().getAnnotation(Datatype.class);
					System.err.println("annotation: "+datatypeAnnot);
					if (datatypeAnnot != null && !datatypeAnnot.renameable()){
						return false;
					}
					else{
						if (selPath.equals(tree.getSelectionPath())) {
							return true;
						}
					}					
					return false;
				}
			}

			return false;
		}

		protected void prepareForEditing() {
			super.prepareForEditing();

			TreePath selPath = MyTreeCellEditor.this.tree.getSelectionPath();

			if (selPath != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
				if (node.getUserObject() instanceof ClipboardItem) {
					this.lastEditingObject = node.getUserObject();
				}
			}
		}
	}

	public AIBenchJTreeManager() {
		if (sessionTree == null) {
			sessionTree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode(AIBenchJTreeManager.SESSION_HISTORY_LABEL)));
			sessionTree.addMouseListener(new AIBenchTreeMouseListener());
			sessionTree.setEditable(false);
			sessionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			sessionTree.setShowsRootHandles(true);

			DefaultTreeCellRenderer renderer = new AIBenchTreeRenderer();
			sessionTree.setCellRenderer(renderer);
			sessionTree.setEditable(true);
			final DefaultTreeCellEditor editor = new MyTreeCellEditor(sessionTree, renderer);
			sessionTree.setCellEditor(editor);

			Core.getInstance().getHistory().addHistoryListener(this);
		}

		if (clipboardTree == null) {
			clipboardTree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode(AIBenchJTreeManager.CLIPBOARD_LABEL)));
			clipboardTree.addMouseListener(new AIBenchTreeMouseListener());
			clipboardTree.setEditable(false);
			clipboardTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			clipboardTree.setShowsRootHandles(true);
			ToolTipManager.sharedInstance().registerComponent(clipboardTree);
			
			clipboardTree.setCellRenderer(new AIBenchTreeRenderer() {

				private static final long serialVersionUID = 1L;

				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean sel, boolean expanded,
						boolean leaf, int row, boolean hasFocus) {

					JLabel c = (JLabel) super.getTreeCellRendererComponent(
							tree, value, sel, expanded, leaf, row, hasFocus);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

					if (leaf == false
							&& !(node.getUserObject() instanceof ClipboardItem)) {
						final String iconDatatype = Workbench.CONFIG.getProperty("icon.datatype");
						URL imageURL = Util.getGlobalResourceURL(iconDatatype);
						c.setIcon(new ImageIcon(iconDatatype == null ? 
							getClass().getResource("/images/datatype.png") : imageURL));
					}
					if (node.equals(tree.getModel().getRoot())) {
						final String iconClipboard = Workbench.CONFIG
								.getProperty("icon.clipboard");
						URL imageURL = Launcher.class.getProtectionDomain()
								.getCodeSource().getLocation();
						try {
							if (imageURL.getFile().endsWith(".jar")) {
								imageURL = new URL(imageURL.toString()
										.substring(
												0,
												imageURL.toString()
														.lastIndexOf('/'))
										+ "/../" + iconClipboard);
							} else {
								imageURL = new URL(imageURL + "../"
										+ iconClipboard);
							}
						} catch (MalformedURLException e) {
						}
						c.setIcon(new ImageIcon(
								iconClipboard == null ? getClass().getResource(
										"/images/clipboard.gif") : imageURL));
						c.setText("");
					}
					
					return c;
				}
			});
			clipboardTree.setEditable(true);
			final DefaultTreeCellEditor editor = new MyTreeCellEditor(clipboardTree, (DefaultTreeCellRenderer) clipboardTree.getCellRenderer());
			clipboardTree.setCellEditor(editor);

			Core.getInstance().getClipboard().addClipboardListener(this);

		}
	}
	
	public JTree getAIBenchTree() {
		return sessionTree;
	}

	public JTree getAIBenchClipboardTree() {
		return clipboardTree;
	}

	/**
	 * @return Returns the instance.
	 */
	public static AIBenchJTreeManager getInstance() {
		if (instance == null) {
			instance = new AIBenchJTreeManager();

		}
		return instance;
	}

	private HashMap<ClipboardItem, MutableTreeNode> clipboardMapping = new HashMap<ClipboardItem, MutableTreeNode>();

	public DefaultMutableTreeNode createClipNode(ClipboardItem clipItem, DefaultMutableTreeNode parent, final boolean mapping) {
		DefaultMutableTreeNode toret = null;

		Datatype aibenchType = clipItem.getRegisteredUserClass().getAnnotation(Datatype.class);

		if (aibenchType != null && aibenchType.structure() != Structure.SIMPLE) {
			if (aibenchType.structure() == Structure.COMPLEX) {
				toret = createComplexClipboardNode(
					Core.getInstance().getClipboard().getComplexSubItems(clipItem), 
					clipItem,
					mapping
				);
			} else if (aibenchType.structure() == Structure.LIST) {
				toret = createListClipboardNode(
					Core.getInstance().getClipboard().getListSubItems(clipItem), 
					clipItem,
					mapping
				);
			}
		} else if (clipItem.getRegisteredUserClass().isArray()) {
			toret = createArrayClipboardNode(clipItem, mapping);
		} else {
			toret = createSimpleClipboardNode(clipItem, mapping);
		}

		if (parent != null) {
			parent.insert(toret, parent.getChildCount());
		}

		// // OBSERVING TO UPDATE ONLY PROPERTIES
		class NodeObserver implements Observer {
			private DefaultMutableTreeNode node;
			private ClipboardItem item;

			public NodeObserver(DefaultMutableTreeNode node, ClipboardItem item) {
				this.node = node;
				this.item = item;
			}

			public void update(final Observable o, Object arg){
				Runnable runnable = new Runnable() {
					public void run() {
						if (logger.getEffectiveLevel().equals(Level.DEBUG))
							logger.debug("updating clipboarditem " + o);
						if (item.getUserData() != null) {
							// trick to fire a property reload
							Datatype annot = item.getUserData().getClass().getAnnotation(Datatype.class);
							if (annot != null && (annot.structure() == Structure.COMPLEX || annot.structure() == Structure.LIST)) {
								for (int i = 0; i < node.getChildCount(); i++) {
									((DefaultTreeModel) sessionTree.getModel()).nodeChanged(node.getChildAt(i));
									((DefaultTreeModel) clipboardTree.getModel()).nodeChanged(node.getChildAt(i));
								}
							}
							((DefaultTreeModel) sessionTree.getModel()).nodeChanged(node);
							((DefaultTreeModel) clipboardTree.getModel()).nodeChanged(node);

							// lets see if a subitem of this node was replaced
							synchronized (Core.getInstance().getClipboard()) { // Clipboard has its methods synchronized
								boolean isComplexSubItem = false;
								List<ClipboardItem> subItems = Core.getInstance().getClipboard().getComplexSubItems(item);

								if (subItems == null)
									subItems = Core.getInstance().getClipboard().getArraySubItems(item);
								else
									isComplexSubItem = true;
								if (subItems == null)
									subItems = Core.getInstance().getClipboard().getListSubItems(item);

								if (subItems == null)
									return;

								// NOTE: added by paulo maia
								LinkedHashMap<Integer, TreeNode> orderedNodes = new LinkedHashMap<Integer, TreeNode>();
								ArrayList<TreeNode> unorderedNodes = new ArrayList<TreeNode>();
								
								int newNodes = 0;
								// end of add

								// add current items to ordered and unorderedNodes lists
								// conserve if they was expanded in their corresponding tree
								HashSet<TreeNode> expandedCTree = new HashSet<TreeNode>();
								HashSet<TreeNode> expandedHTree = new HashSet<TreeNode>();
								for (int i = 0; i < node.getChildCount(); i++) {
									DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);

									// see if it is expanded
									TreePath path = new TreePath(child.getPath());
									if (sessionTree.isExpanded(path)) {
										expandedHTree.add(child);
									} else if (clipboardTree.isExpanded(path)) {
										expandedCTree.add(child);
									}
									// end of see

									if (child.getUserObject() instanceof ClipboardItem) {
										int childOrder = Core.getInstance().getClipboard().getOrder((((ClipboardItem) child.getUserObject()).getID()));
										if (childOrder != -1)
											orderedNodes.put(childOrder, child);
										else
											unorderedNodes.add(child);
									}
								}
								
								newItem: for (ClipboardItem item : subItems) {
									for (int i = 0; i < node.getChildCount(); i++) {
										DefaultMutableTreeNode _node = (DefaultMutableTreeNode) node.getChildAt(i);
										if (_node.getUserObject() == item) {
											continue newItem;
										}
									}
									
									if (logger.getEffectiveLevel().equals(Level.DEBUG))
										logger.debug("A complex item in the tree has a changed child! " + item);

									if (isComplexSubItem
										&& item.getUserData() == null
										&& Boolean.parseBoolean(Workbench.CONFIG.getProperty("trees.shownulls", "false"))) {
										continue;
									}

									// NOTE: modified by paulo maia

									final DefaultMutableTreeNode nodeNew = createClipNode(item, null, mapping);
									newNodes++;
									int relativeOrder = Core.getInstance().getClipboard().getOrder(item.getID());
									if (logger.getEffectiveLevel().equals(Level.DEBUG))
										logger.debug("trees: modifying " + item.getName() + "(id: " + item.getID() + ") with order=" + relativeOrder);

									if (relativeOrder != -1)
										orderedNodes.put(relativeOrder, nodeNew);
									else
										unorderedNodes.add(nodeNew);
								}

								Integer[] order = orderedNodes.keySet().toArray(new Integer[orderedNodes.keySet().size()]);
								Arrays.sort(order);

								// node.removeAllChildren(); //commented by
								// lipido

								int counter = node.getChildCount()-orderedNodes.size()-unorderedNodes.size()+newNodes;
								TreePath selectedPath = null;
								TreePath path = null;
								for (Integer i : order) {
									DefaultMutableTreeNode orderedNode = (DefaultMutableTreeNode) orderedNodes.get(i);

									if (orderedNode.getParent() != null && orderedNode.getParent() == node) {
										counter++;
//									continue;
										
										if (expandedHTree.contains(orderedNode)) {
											path = new TreePath(orderedNode.getPath());
											sessionTree.expandPath(path);
//										sessionTree.setSelectionPath(new TreePath(path));
//										sessionTree.scrollPathToVisible(new TreePath(path));
										} else if (expandedCTree.contains(orderedNode)) {
											path = new TreePath(orderedNode.getPath());
											clipboardTree.expandPath(path);
//										clipboardTree.setSelectionPath(new TreePath(path));
//										clipboardTree.scrollPathToVisible(new TreePath(path));
										}
									} else {
										node.insert(orderedNode, counter++);
										// node.add(nodeNew);

										// ((DefaultTreeModel)jTree.getModel()).nodeStructureChanged(node);
										// ((DefaultTreeModel)clipboardTree.getModel()).nodeStructureChanged(node);

										selectedPath = new TreePath(orderedNode.getPath());
									}
								}

								for (TreeNode un : unorderedNodes) {
									DefaultMutableTreeNode unorderedNode = (DefaultMutableTreeNode) un;

									// if the node is present, do not insert it
									// again
									if (unorderedNode.getParent() != null && unorderedNode.getParent() == node) {
										counter++;
//									continue;

										if (expandedHTree.contains(unorderedNode)) {
											path = new TreePath(unorderedNode.getPath());
											sessionTree.expandPath(path);
//										sessionTree.setSelectionPath(path);
//										sessionTree.scrollPathToVisible(path);
										} else if (expandedCTree.contains(unorderedNode)) {
											path = new TreePath(unorderedNode.getPath());
											clipboardTree.expandPath(path);
//										clipboardTree.setSelectionPath(path);
//										clipboardTree.scrollPathToVisible(path);
										}
									} else {
										node.insert(unorderedNode, counter++);
										// node.add(mutnodeNew);
										
										// ((DefaultTreeModel)jTree.getModel()).nodeStructureChanged(node);
										// ((DefaultTreeModel)clipboardTree.getModel()).nodeStructureChanged(node);
										
										selectedPath = new TreePath(unorderedNode.getPath());
									}
								}

								if (selectedPath != null) {
									((DefaultTreeModel) sessionTree.getModel()).nodeStructureChanged(node);
									((DefaultTreeModel) clipboardTree.getModel()).nodeStructureChanged(node);

									sessionTree.expandPath(selectedPath);
									sessionTree.scrollPathToVisible(selectedPath);
									sessionTree.setSelectionPath(selectedPath);

									clipboardTree.expandPath(selectedPath);
									clipboardTree.scrollPathToVisible(selectedPath);
									clipboardTree.setSelectionPath(selectedPath);
								}
								// end of modification
							}
						}
					}
				};
				if (!SwingUtilities.isEventDispatchThread()){
				try {
					SwingUtilities.invokeAndWait(runnable);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}else{
					runnable.run();
				}
			}
		}
		if (Boolean.parseBoolean(Core.CONFIG.getProperty("clipboard.listenobservables", "false"))) {
			if (clipItem instanceof Observable) {
				if (logger.getEffectiveLevel().equals(Level.DEBUG))
					logger.debug("tree: its observable " + clipItem);
				((Observable) clipItem).addObserver(new NodeObserver(toret, clipItem));
			}
		}
		return toret;

	}

	private DefaultMutableTreeNode createSimpleClipboardNode(
			ClipboardItem clipItem, boolean mapping) {
		DefaultMutableTreeNode toret = new DefaultMutableTreeNode(clipItem);

		if (mapping)
			clipboardMapping.put(clipItem, toret);
		return toret;

	}

	private DefaultMutableTreeNode createComplexClipboardNode(
			List<ClipboardItem> clipboardItems, final ClipboardItem clipItem,
			boolean mapping) {
		DefaultMutableTreeNode toret = createSimpleClipboardNode(clipItem, mapping);

		for (final Method m : clipItem.getUserData().getClass().getMethods()) {
			final Property property = m.getAnnotation(Property.class);

			if (property != null) {
				if (Boolean.parseBoolean(Core.CONFIG.getProperty("clipboard.listenobservables", "false"))) {
					Object propertyReader = new Object() {
						public String toString() {
							try {
								if (!clipItem.wasRemoved()) {
									return property.name()
									+ (property.name() == null ? "" : ": ")
									+ m.invoke(clipItem.getUserData(), (Object[]) null);
								}
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return "";
						}
					};
					
					toret.insert(new DefaultMutableTreeNode(propertyReader), toret.getChildCount());
				} else {
					try {
						toret.insert(new DefaultMutableTreeNode(property.name()
								+ (property.name() == null ? "" : ": ")
								+ m.invoke(clipItem.getUserData(), (Object[]) null)), 
								toret.getChildCount());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		for (ClipboardItem subItem : Core.getInstance().getClipboard().getComplexSubItems(clipItem)) {
			// es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard clip =
			// m.getAnnotation(es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard.class);
			if (Boolean.parseBoolean(Workbench.CONFIG.getProperty("trees.shownulls", "true"))
				&& subItem.getUserData() == null) {
					continue;
			}
			createClipNode(subItem, toret, mapping);
		}
		return toret;

	}

	private DefaultMutableTreeNode createListClipboardNode(
			List<ClipboardItem> clipboardItems, final ClipboardItem clipItem,
			boolean mapping) {
		DefaultMutableTreeNode toret = createSimpleClipboardNode(clipItem,
				mapping);

		for (final Method m : clipItem.getUserData().getClass().getMethods()) {

			final Property property = m.getAnnotation(Property.class);
			if (property != null) {

				Object propertyReader = new Object() {
					public String toString() {
						try {
							if (!clipItem.wasRemoved()) {
								return property.name()
										+ (property.name() == null ? "" : ": ")
										+ m.invoke(clipItem.getUserData(),
												(Object[]) null);
							}
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return "";
					}
				};
				if (Core.CONFIG.getProperty("clipboard.listenobservables") != null
						&& Core.CONFIG.getProperty("clipboard.listenobservables").equals("true")) {
					toret.insert(new DefaultMutableTreeNode(propertyReader), toret.getChildCount());
				} else {
					try {
						toret.insert(new DefaultMutableTreeNode(property.name()
								+ (property.name() == null ? "" : ": ")
								+ m.invoke(clipItem.getUserData(),
										(Object[]) null)), toret
								.getChildCount());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}

		for (ClipboardItem subItem : Core.getInstance().getClipboard().getListSubItems(clipItem)) {
			createClipNode(subItem, toret, mapping);

		}
		return toret;

	}

	private DefaultMutableTreeNode createArrayClipboardNode(
			ClipboardItem clipItem, boolean mapping) {
		DefaultMutableTreeNode toret = createSimpleClipboardNode(clipItem, mapping);

		for (ClipboardItem subItem : Core.getInstance().getClipboard().getArraySubItems(clipItem)) {
			createClipNode(subItem, toret, mapping);
		}

		return toret;

	}

	public void historyElementAdded(final HistoryElement history) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				synchronized (history) {
					DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(history);

					for (Object result : history.getOutputs()) {
						// find clipboard item for result
						synchronized (Core.getInstance().getClipboard()) { // Clipboard has its methods synchronized
							for (ClipboardItem rootItem : Core.getInstance().getClipboard().getRootItems()) { // should be an output direct clipboard item
								if (rootItem.getUserData() == result) {
									for (ClipboardItem resultItem : history.getClipboardItems()) { // should be one of the outputs
										if (rootItem == resultItem) {
											createClipNode(rootItem, operationNode, true);
										}
									}
								}
							}
						}
					}

					boolean insertedAnywhere = false;
					Vector<ClipboardItem> noRepeat = new Vector<ClipboardItem>();
					DefaultTreeModel model = (DefaultTreeModel) AIBenchJTreeManager.this.sessionTree.getModel();
					for (ParamSpec spec : history.getParams()) {
						if (spec.getSource() == ParamSource.CLIPBOARD) {
							ClipboardItem item = (ClipboardItem) spec.getValue();

							if (noRepeat.indexOf(item) != -1)
								continue;
							noRepeat.addElement(item);
							MutableTreeNode node = AIBenchJTreeManager.this.clipboardMapping.get(item);
							if (node != null) {
								try {
									model.insertNodeInto(operationNode,	node, node.getChildCount());
								} catch (IllegalArgumentException e) {
									// new child is an ancestor, this operation
									// returned the same input param
									e.printStackTrace();
									operationNode.remove(node);
									model.insertNodeInto(operationNode,	node, node.getChildCount());
								}
								insertedAnywhere = true;
							}
						} else {
							DefaultMutableTreeNode paramNode = new DefaultMutableTreeNode(spec.getName() + ": " + spec.getRawValue());
							model.insertNodeInto(paramNode, operationNode, 0);
						}
					}
					if (!insertedAnywhere) {
						// inserting in the root
						model.insertNodeInto(operationNode, (MutableTreeNode) model.getRoot(), ((MutableTreeNode) model.getRoot()).getChildCount());
					}

					sessionTree.expandPath(new TreePath(operationNode.getPath()));
					sessionTree.scrollPathToVisible(new TreePath(operationNode.getPath()));
					sessionTree.setSelectionPath(new TreePath(operationNode.getPath()));
				}
			}
		});
	}
	
	public final static class ClassTreeNode extends DefaultMutableTreeNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final Class<?> clazz;
		public ClassTreeNode(String name, Class<?> clazz) {
			super(name);
			this.clazz = clazz;
		}
	}
	
	private final static ClassTreeNode locateClassNode(JTree tree, Class<?> clazz) {
		TreeModel model = tree.getModel();
		if (model == null || model.getRoot() == null)
			return null;
		
		for (int i=0; i<model.getChildCount(model.getRoot()); i++) {
			Object node = model.getChild(model.getRoot(), i);
			if (node instanceof ClassTreeNode) {
				ClassTreeNode classNode = (ClassTreeNode) node;
				if (classNode.clazz.equals(clazz)) {
					return classNode;
				}
			}
		}
		
		return null;
	}
	
	private void clipboardTreeElementAdded(ClipboardItem item) {
		if (logger.getEffectiveLevel().equals(Level.DEBUG))
			logger.debug("Adding clipboard item " + item.getName());

		DefaultTreeModel cTreeModel = (DefaultTreeModel) AIBenchJTreeManager.this.clipboardTree.getModel();
		MutableTreeNode cRootNode = (MutableTreeNode) cTreeModel.getRoot();
		for (ClipboardItem item2 : Core.getInstance().getClipboard().getRootItems()) {
			if (item2 == item) {
				DefaultMutableTreeNode newNode = this.createClipNode(item, null, false);
				
				if (newNode != null) {
					DefaultMutableTreeNode parentNode = null;
					if (Boolean.parseBoolean(Workbench.CONFIG.getProperty("clipboardtree.organizebyclass", "true"))) {
						parentNode = AIBenchJTreeManager.locateClassNode(AIBenchJTreeManager.this.clipboardTree, item.getUserData().getClass());//classNodes.get(item.getUserData().getClass());
						if (parentNode == null) {
							String name = item.getUserData().getClass().getSimpleName();
							
							Datatype datatypeAnnot = item.getUserData().getClass().getAnnotation(Datatype.class);
							if(datatypeAnnot!= null && !datatypeAnnot.clipboardClassName().equals("")) {
								name = datatypeAnnot.clipboardClassName();
							}
							
							parentNode = new ClassTreeNode(name, item.getUserData().getClass());
//							classNodes.put(item.getUserData().getClass(), classNode);
							
							// Alphabetic order
							int order = 0;
							while (order < cRootNode.getChildCount() && name.compareTo(cRootNode.getChildAt(order).toString()) > 0) {
								order++;
							}
							cTreeModel.insertNodeInto(parentNode, cRootNode, order);
						}
					} else {
						parentNode = (DefaultMutableTreeNode) cTreeModel.getRoot();
					}
					
					if (parentNode != null) {
						cTreeModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
						
						clipboardTree.expandPath(new TreePath(newNode.getPath()));
						clipboardTree.scrollPathToVisible(new TreePath(newNode.getPath()));
						clipboardTree.setSelectionPath(new TreePath(newNode.getPath()));
					}
				}
				
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementAdded(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
	 */
	public synchronized void elementAdded(final ClipboardItem item) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				clipboardTreeElementAdded(item);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementLoaded(es.uvigo.ei.aibench.core.clipboard.ClipboardItem,
	 *      java.io.File)
	 */
	public void elementLoaded(File f) {
		// TODO Auto-generated method stub

	}

	public DefaultMutableTreeNode findNodeForUserObject(Object item, JTree tree) {
		return this.findNodeForUserObject(tree.getModel().getRoot(), item, tree);
	}

	private DefaultMutableTreeNode findNodeForUserObject(Object parent,	Object item, JTree tree) {
		if (parent == null) {
			return null;
		}
		if (((DefaultMutableTreeNode) parent).getUserObject().equals(item))
			return (DefaultMutableTreeNode) parent;
		for (int i = 0; i < tree.getModel().getChildCount(parent); i++) {
			DefaultMutableTreeNode search = findNodeForUserObject(tree.getModel().getChild(parent, i), item, tree);
			if (search != null)
				return search;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementRemoved(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
	 */
	public void elementRemoved(final ClipboardItem item) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MutableTreeNode rootNode = (MutableTreeNode) AIBenchJTreeManager.this.sessionTree.getModel().getRoot();
//				MutableTreeNode cRootNode = (MutableTreeNode) AIBenchJTreeManager.clipboardTree.getModel().getRoot();
				
				if (logger.getEffectiveLevel().equals(Level.DEBUG))	AIBenchJTreeManager.logger.debug("Tree removing item: "	+ item);
				DefaultMutableTreeNode node = findNodeForUserObject(item, sessionTree);
				if (node != null) {
					((DefaultTreeModel) sessionTree.getModel()).removeNodeFromParent(node);

					node.setUserObject("");

					// does this item have any child operations
					for (int i = 0; i < node.getChildCount(); i++) {
						DefaultMutableTreeNode nodeChild = (DefaultMutableTreeNode) node.getChildAt(i);
						if (nodeChild.getUserObject() instanceof HistoryElement) {
							if (logger.getEffectiveLevel().equals(Level.DEBUG))
								logger.debug("This clipboard has a child operation. moved it to root");
							nodeChild.removeFromParent();
							((DefaultTreeModel) sessionTree.getModel()).insertNodeInto(nodeChild, rootNode, rootNode.getChildCount());
						}
					}
				}
				node = findNodeForUserObject(item, clipboardTree);
				if (node != null) {
					if (/*node.getParent().getParent() == cRootNode*/node.getParent() instanceof ClassTreeNode 
						&& node.getParent().getChildCount() == 1) {
//						AIBenchJTreeManager.this.classNodes.remove(item.getRegisteredUserClass());
						ClassTreeNode classNode = AIBenchJTreeManager.locateClassNode(AIBenchJTreeManager.this.clipboardTree, item.getRegisteredUserClass());
						if (classNode != null) {
							((DefaultTreeModel) clipboardTree.getModel()).removeNodeFromParent((DefaultMutableTreeNode) classNode);
						}
					} else {
						((DefaultTreeModel) clipboardTree.getModel()).removeNodeFromParent(node);
					}
					node.setUserObject("");
				}
				clipboardMapping.remove(item);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.uvigo.ei.aibench.core.history.HistoryListener#historyElementRemoved(es.uvigo.ei.aibench.core.history.HistoryElement)
	 */
	public void historyElementRemoved(final HistoryElement history) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (logger.getEffectiveLevel().equals(Level.DEBUG))
					AIBenchJTreeManager.logger.debug("Tree removing history: "
							+ history);
				DefaultMutableTreeNode node = findNodeForUserObject(history,
						sessionTree);
				if (node != null) {

					((DefaultTreeModel) sessionTree.getModel())
							.removeNodeFromParent(node);

					node.setUserObject("");

				}

			}

		});

	}

}
