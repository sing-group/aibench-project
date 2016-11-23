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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import javax.help.HelpBroker;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.CoreUtils;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.Transformer;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.aibench.core.history.HistoryElement;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.OperationWrapper;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.inputgui.ParamsWindow;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;



/**
 * @author Ruben Dominguez Carbajales 28-ene-2006
 */
public class AIBenchTreeMouseListener extends MouseAdapter {
	

	public AIBenchTreeMouseListener() {
		super();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 1 && !e.isPopupTrigger()) {
			final Component comp = e.getComponent();
			if (comp instanceof JTree) {
				final JTree tree = (JTree) comp;
				final TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				
				tree.setSelectionPath(selPath);
				if (selPath != null && selPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
					
					if (node.getUserObject() instanceof ClipboardItem){
						Workbench.getInstance().showData(((ClipboardItem) node.getUserObject()));
					}
				}
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		this.doMouseAction(e);
	}

	public void mouseReleased(MouseEvent e) {
		this.doMouseAction(e);
	}
	
	private void doMouseAction(MouseEvent e){
		if (e.isPopupTrigger()){
			final Component comp = e.getComponent();
			if (comp instanceof JTree) {
				final JTree tree = (JTree) comp;
				final TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(selPath);
				
				if (selPath != null && selPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
					
					if (node.getUserObject() instanceof ClipboardItem){
						final ClipboardItem item = (ClipboardItem) node.getUserObject();
						ParamsWindow.preferredClipboardItem = item;
						Object data = item.getUserData();
						JPopupMenu popup = getApplicableOperationsPopup(data);
						popup.addSeparator();
						
						Datatype datatypeAnnot = data.getClass().getAnnotation(Datatype.class);
//						NOTE: Core.getInstance().getClipboard().getListSubItems(item);
						
						// verify if the datatype object is removable
						if (datatypeAnnot != null) {
							if (datatypeAnnot.removable()) {
								if (datatypeAnnot.removeMethod().equalsIgnoreCase("")) {
									
									if (datatypeAnnot.structure() == Structure.LIST){
										DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
											
										if (parent.getUserObject() instanceof ClipboardItem) {
		
											ClipboardItem itemcontainer = (ClipboardItem) parent.getUserObject();
		
											for (Method m : itemcontainer.getUserData().getClass().getMethods()) {
												ListElements le = m.getAnnotation(ListElements.class);
												if ((le != null) && (le.modifiable())) { //see if is a list elements instance
													try {
														Object containerlist = m.invoke(itemcontainer.getUserData());
														if (containerlist instanceof List<?>) {
															addListRemove(
																popup,
																item,
																itemcontainer,
																(List<?>) containerlist
															);
														}
													} catch (IllegalArgumentException e1) {
														// TODO Auto-generated catch
														// block
														e1.printStackTrace();
													} catch (IllegalAccessException e1) {
														// TODO Auto-generated catch
														// block
														e1.printStackTrace();
													} catch (InvocationTargetException e1) {
														// TODO Auto-generated catch
														// block
														e1.printStackTrace();
													}
												}
											}
											
										}
									}
									this.addRemove(popup, item);
								} else {
									try {
										Method m = item.getUserData().getClass().getMethod(datatypeAnnot.removeMethod(), new Class[]{});
										addSpecificRemove(popup,item,m);
									} catch (SecurityException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (NoSuchMethodException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} 
							}
							
							if (datatypeAnnot.renameable() && datatypeAnnot.setNameMethod().equalsIgnoreCase("")) {
								this.addRename(popup, item);
							}
						} else {
							this.addRemove(popup, item);
							this.addRename(popup, item);
						}
						
						
						if (datatypeAnnot != null)
							addHelp(popup, datatypeAnnot);
						
						if (popup.getSubElements().length > 0) {
							popup.show(comp, e.getX(), e.getY());
							
						}
					} else if (node.getUserObject() instanceof HistoryElement) {
						//adding remove operations
						JPopupMenu popup = new JPopupMenu();
						Action action = new AbstractAction(){
							
							
							private static final long serialVersionUID = 1L;
				
							public void actionPerformed(ActionEvent e) {
								Core.getInstance().getHistory().removeItem((HistoryElement) node.getUserObject());
								
								
								
							}
							
						};
						action.putValue(Action.NAME, "remove element...");
						popup.add(action);
						popup.show(comp, e.getX(), e.getY());
					}
				} else {					
					JPopupMenu popup = getOperationsWithAnyClipboardPopup();
					popup.show(comp, e.getX(), e.getY());
					
					
				}
			}
		}

	}
	
	
	

	
	private void addRename(JPopupMenu pop, final ClipboardItem item){
		final Action action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				String newName = JOptionPane.showInputDialog(Workbench.getInstance().getMainFrame(),"New name: ",item.getName());
				if (newName != null) {
				
					item.setName(newName);
					
					/**
					 * NOTE: MODIFIED BY PAULO MAIA - CHANGING NAMES WAS NOT WORKING CORRECTLY WHEN RENAME FROM POPUP WAS USED
					 */
					Datatype datatypeAnnot = item.getUserData().getClass().getAnnotation(Datatype.class);
					
					// try to use a method to set the name of the Datatype
					if (datatypeAnnot != null) {
						if (datatypeAnnot.renameable() && !datatypeAnnot.setNameMethod().equals("")) {
							try {
								Method m = item.getUserData().getClass().getMethod(datatypeAnnot.setNameMethod(), new Class[]{String.class});
								m.invoke(item.getUserData(), newName);
								
							} catch (SecurityException f) {
								// TODO Auto-generated catch block
								f.printStackTrace();
							} catch (NoSuchMethodException f) {
//								logger.error("Couldn't find the set name method in the Complex item "+item.getUserData().getClass()+":  "+datatypeAnnot.setNameMethod());
								f.printStackTrace();
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
					
					JTree clipboardTree = Workbench.getInstance().getTreeManager().getAIBenchClipboardTree();
					DefaultMutableTreeNode _node = Workbench.getInstance().getTreeManager().findNodeForUserObject(item, clipboardTree);
									
					((DefaultTreeModel) clipboardTree.getModel()).nodeChanged(_node);
					
					JTree sessionTree = Workbench.getInstance().getTreeManager().getAIBenchTree();
					 _node = Workbench.getInstance().getTreeManager().findNodeForUserObject(item, sessionTree);
					((DefaultTreeModel) sessionTree.getModel()).nodeChanged(_node);
				}
			}
		};
		
		action.putValue(Action.NAME, "rename element...");
		pop.add(action);
	}
	
	private void addRemove(JPopupMenu pop, final ClipboardItem item){
		
		// only root items can be removed
		if (Core.getInstance().getClipboard().getRootItems().indexOf(item)!=-1){
			Action action = new AbstractAction(){
				private static final long serialVersionUID = 1L;
	
				public void actionPerformed(ActionEvent e) {
					Core.getInstance().getClipboard().removeClipboardItem(item);
				}
				
			};
			action.putValue(Action.NAME, "remove element...");
			pop.add(action);
		}
		
		// How to add a generic remove operation for ListElements Items...?
//		Core.getInstance().getClipboard().getListSubItems(item)
	}
	
	private void addHelp(JPopupMenu popup, final Datatype itemDatatype) {
		if (itemDatatype.help() != null && !itemDatatype.help().trim().equals("")) {
			if (CoreUtils.isValidURL(itemDatatype.help())) {
				popup.add(new AbstractAction("what's this?") {
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e) {
						try {
							CoreUtils.openURL(itemDatatype.help());
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(
								Workbench.getInstance().getMainFrame(), 
								"The help URL(" + itemDatatype.help() + ") couldn't be opened.", 
								"Help Unavailable", 
								JOptionPane.ERROR_MESSAGE
							);
						}
					}
				});
			} else {
				HelpBroker broker = Core.getInstance().getHelpBroker();
				if (broker != null) {
					JMenuItem helpItem = new JMenuItem("what's this?");
					broker.enableHelpOnButton(helpItem, itemDatatype.help(), broker.getHelpSet());
					popup.add(helpItem);
				}
			}
		}
	}
	
	/**
	 * NOTE: added by paulo maia
	 * @param pop
	 * @param item
	 * @param parentItem
	 * @param containerlist
	 */
	private void addListRemove(JPopupMenu pop, final ClipboardItem item,final ClipboardItem parentItem,final List<?> containerlist){
		
		
		if (Core.getInstance().getClipboard().getListSubItems(parentItem).indexOf(item)!=-1){
			Action action = new AbstractAction(){
	
				private static final long serialVersionUID = 1L;
				
				public void actionPerformed(ActionEvent e) {
					System.out.println("========LIST REMOVE ACTIVATED!!!!!!!!!!!!!!!==============");
					Object data = item.getUserData();
					Core.getInstance().getClipboard().removeClipboardItem(item);
					containerlist.remove(data);
				}
				
			};
			action.putValue(Action.NAME, "remove element...");
			pop.add(action);
		}
	}
	
	/**
	 * NOTE: added by paulo maia
	 * @param pop
	 * @param item
	 * @param m
	 */
	private void addSpecificRemove(JPopupMenu pop, final ClipboardItem item,final Method m){
		
			Action action = new AbstractAction(){
	
				private static final long serialVersionUID = 1L;
				
				public void actionPerformed(ActionEvent e) {
					System.out.println("========SPECIFIC REMOVE ACTIVATED!!!!!!!!!!!!!!!==============");
					try {
						m.invoke(item.getUserData());
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					Core.getInstance().getClipboard().removeClipboardItem(item);
				}
				
			};
			action.putValue(Action.NAME, "remove element...");
			pop.add(action);
	}
	
	private boolean isPrimitive(Class<?> clazz){
		if (!clazz.isArray()){
			return clazz.isPrimitive() || clazz.getEnumConstants()!=null || File.class.isAssignableFrom(clazz);
		}else{
			return isPrimitive(clazz.getComponentType());
		}
	}
	private JPopupMenu getOperationsWithAnyClipboardPopup() {

		JPopupMenu popup =new JPopupMenu();
		for (OperationWrapper wrapper : Workbench.getInstance().getInterceptedOperations()){
			boolean hasClipboardInput = false;
			int counter=0;
			for (Object incomingType: wrapper.getOperationDefinition().getIncomingArgumentTypes()){
				
				Class<?> clazz = (Class<?>) incomingType;
				if (isPrimitive(clazz)){
						
					
				}else if(((Port)wrapper.getOperationDefinition().getPorts().get(counter++)).allowNull()){
					// next
				}
				else{
					try {
						clazz.getConstructor(new Class[]{String.class});
					
					
					
					}catch(NoSuchMethodException e){
						hasClipboardInput = true;
					}
					
				}
				
			}
			if (!hasClipboardInput && Workbench.getInstance().isOperationViewableIn(wrapper.getOperationDefinition(), "POPUP"))
				Utilities.putOperationInMenu(popup, wrapper);
		}
		
		

		return popup;
	}
	private JPopupMenu getApplicableOperationsPopup(Object data) {
		
		HashMap<OperationWrapper, Integer> added= new HashMap<OperationWrapper, Integer>(); //we want to add an operation once
		JPopupMenu popup =new JPopupMenu();
		for (OperationWrapper wrapper : Workbench.getInstance().getInterceptedOperations()){
			for (Object incomingType: wrapper.getOperationDefinition().getIncomingArgumentTypes()){
				Class<?> clazz = (Class<?>) incomingType;
				if (clazz.isAssignableFrom(data.getClass())){
					if (added.get(wrapper)==null){
						added.put(wrapper, 0);
						if (Workbench.getInstance().isOperationViewableIn(wrapper.getOperationDefinition(), "POPUP")) Utilities.putOperationInMenu(popup, wrapper);
					}else{
						continue;
					}
					
				}
				List<Transformer> transformers = Core.getInstance().getTransformersByDestiny(clazz);
				for (Transformer t : transformers){
					if (t.getSourceType().isAssignableFrom(data.getClass())){
						if (Workbench.getInstance().isOperationViewableIn(wrapper.getOperationDefinition(), "POPUP")){
							if (added.get(wrapper)==null){
								added.put(wrapper, 0);
								Utilities.putOperationInMenu(popup, wrapper);
							}							
						}
					}
				}				
			}
		}
		
		

		return popup;
	}

	
}
