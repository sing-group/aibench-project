/*
 * #%L
 * The AIBench Workbench Plugin
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
package es.uvigo.ei.aibench.workbench;

import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableModel;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.workbench.interfaces.AbstractViewFactory;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;

/**
 * @author Daniel Glez-Peña 22-sep-2006
 *
 */
public class DefaultViewFactory extends AbstractViewFactory implements Observer {
	private JTextArea textarea = null;
	private JTable table = null;
	private JTable arrayTable = null;
	
	private Object data = null;
	
	public DefaultViewFactory() {
		this.name = "Default view";
	}

	@Override
	public JComponent getComponent(Object data) {
		this.data = data;
		
		if (Core.CONFIG.containsKey("clipboard.listenobservables") && Boolean.parseBoolean(Core.CONFIG.getProperty("clipboard.listenobservables"))){
			if (data instanceof Observable){
				((Observable) data).addObserver(this);
			}
		}
		
		final JComponent componentExtensionIntance = this.getSplitter(data);
		if (Boolean.parseBoolean(Core.CONFIG.getProperty("help.enabled", "false"))) {
			final ComponentAdapter adapter = new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					final Frame parentFrame = Utilities.getParentFrame(componentExtensionIntance);
					if (parentFrame instanceof JFrame) {
						Container container = ((JFrame) parentFrame).getContentPane();
						if (container instanceof JComponent) {
							final JComponent containerComponent = (JComponent) container;
							containerComponent.putClientProperty(Workbench.AIBENCH_HELP_PROPERTY, null);
						}
					}
				}
			};
			
			componentExtensionIntance.addAncestorListener(new AncestorListener() {
				/* (non-Javadoc)
				 * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
				 */
				public void ancestorAdded(AncestorEvent event) {
					for (ComponentListener listener:event.getAncestor().getListeners(ComponentListener.class)) {
						if (listener == adapter) {
							break;
						}
					}
					
					event.getAncestor().addComponentListener(adapter);
					if (event.getAncestor().isVisible()) {
						adapter.componentShown(null);
					}
				}
				public void ancestorMoved(AncestorEvent event) {}
				public void ancestorRemoved(AncestorEvent event) {}
			});
		}
		return componentExtensionIntance;
	}
	
	public JSplitPane getSplitter(Object data){
		JSplitPane splitter = new JSplitPane();
		splitter.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		// use tables to show arrays and collections
		
		
		if ( data!=null && (data instanceof java.util.Collection<?> || data.getClass().isArray()) && Workbench.CONFIG.getProperty("defaultview.use_table_for_collections_and_arrays")!=null && Workbench.CONFIG.getProperty("defaultview.use_table_for_collections_and_arrays").equals("true")){
			splitter.setTopComponent(new JScrollPane(getCollectionTable()));
		}else{
			splitter.setTopComponent(new JScrollPane(getTextArea()));
		}
		splitter.setBottomComponent(new JScrollPane(getPropertyTable()));
		splitter.setResizeWeight(0.5f);
		return splitter;
		
	}
	

	private JTable getCollectionTable() {
		if (arrayTable == null){
			arrayTable = new JTable();
			arrayTable.setCellSelectionEnabled(true);
			DefaultTableModel model = (DefaultTableModel) arrayTable.getModel();
			model.setColumnIdentifiers(new Object[]{"index", "toString"});
			int maxContentSize=0;
			
			if (this.data.getClass().isArray()){
				for (int i = 0; i<Array.getLength(this.data); i++){
					
					String content = "null";
					if ( Array.get(this.data, i)!=null){
						content = Array.get(this.data, i).toString();
					}
					maxContentSize= Math.max(maxContentSize, arrayTable.getFontMetrics(arrayTable.getFont()).stringWidth(content)+5);
					model.addRow(new Object[]{i, Array.get(this.data, i)});
					
				}
			}else{
				Collection<?> c = (Collection<?>) this.data;
				int counter=0;
				for (Object elem : c){
					String content = "null";
					if (elem!=null) content = elem.toString();
					maxContentSize= Math.max(maxContentSize, arrayTable.getFontMetrics(arrayTable.getFont()).stringWidth(content)+5);
					
					model.addRow(new Object[]{counter++, elem});
				}
			}
			
		
			arrayTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			int firstColumnSize = Math.max(arrayTable.getFontMetrics(arrayTable.getFont()).stringWidth("index")+5,arrayTable.getFontMetrics(arrayTable.getFont()).stringWidth(""+model.getRowCount())+5) ;
		
			maxContentSize = Math.max(arrayTable.getFontMetrics(arrayTable.getFont()).stringWidth("toString")+5, maxContentSize);
			arrayTable.getColumnModel().getColumn(0).setPreferredWidth(firstColumnSize);
			arrayTable.getColumnModel().getColumn(1).setPreferredWidth(maxContentSize);
			
			
			
		}
		return arrayTable;
	}

	public JTextArea getTextArea(){
		if (this.textarea == null){
			final String text = (this.data == null)?"<NULL>":this.data.toString();
			this.textarea = new JTextArea(text);
			this.textarea.setEditable(false);
			
			final Font defaultFont = this.textarea.getFont();
			this.textarea.setFont(new Font("Courier", defaultFont.getStyle(), defaultFont.getSize()));
		}
		
		return this.textarea;
	}
	
	private void updateTable() {
		
		// clear...
		while(((DefaultTableModel)table.getModel()).getRowCount()>0){
			((DefaultTableModel)table.getModel()).removeRow(0);
		}
		
		((DefaultTableModel)table.getModel()).setColumnCount(2);
		
		((DefaultTableModel)table.getModel()).setColumnIdentifiers(new Object[]{"property", "value"});
		
		if (data == null) return;
		try {
			BeanInfo info = java.beans.Introspector.getBeanInfo(data.getClass());
			PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
			if (descriptors != null){
				for (PropertyDescriptor descriptor : descriptors){
					
					Method read = descriptor.getReadMethod();
					if (read != null){
						try{
						Object readed = read.invoke(data);
						((DefaultTableModel)table.getModel()).addRow(new Object[]{descriptor.getName(), readed});
						}catch(Exception e){
							((DefaultTableModel)table.getModel()).addRow(new Object[]{descriptor.getName(), "<the method reported an "+e.getClass().getSimpleName()+" exception>"});
						}
						
					}
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	public JTable getPropertyTable(){
		if (table == null){
			
			table = new JTable();
			
			table.setEnabled(false);
			updateTable();
		}
		return table;
		
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				getTextArea().setText(data.toString());
				updateTable();
				
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.interfaces.IViewFactory#getComponentClass()
	 */
	public Class<?> getComponentClass() {
		return JComponent.class;
	}
}
