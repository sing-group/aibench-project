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
package es.uvigo.ei.aibench.workbench.inputgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSource;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;
import es.uvigo.ei.aibench.core.datatypes.Transformer;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.OperationResultsCollector;
import es.uvigo.ei.aibench.workbench.OperationWrapper;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.WaitOperationWrapper;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;


public class ClipboardParamProvider extends AbstractParamProvider {
	final static ImageIcon ICON_OPERATIONS = new ImageIcon(ArrayParamProvider.class.getResource("images/operations.png"));
	
	private final static FocusListener FOCUS_LISTENER = new FocusAdapter() {
		public void focusGained(FocusEvent e) {
			if (e.getComponent() instanceof JComboBox) {
				@SuppressWarnings("unchecked")
				JComboBox<Object> combo = (JComboBox<Object>) e.getComponent();
				if (combo.isEditable()) {
					combo.getEditor().selectAll();
				}
			} else if (e.getComponent() instanceof JTextField) {
				((JTextField) e.getComponent()).selectAll();
			}
		};
	};

	private JPanel panel = new JPanel(new BorderLayout());
	private final boolean hasStringConstructor; 
	private JComboBox<Object> combo = null;
	private JTextField textField = null;
	private boolean showCreateButton = true;
	private int countItemsInClipboard = 0;

	private JMenuBar createButton;
	
	private String nullItemText = "<NULL>";

	public ClipboardParamProvider(ParamsReceiver receiver, Port p, Class<?> clazz, Object operationObject) {
		super(receiver, p, clazz, operationObject);	

		boolean hasStringConstructor;
		try {
			clazz.getConstructor(new Class[]{String.class});
			hasStringConstructor = true;
		} catch (Exception e) {
			hasStringConstructor = false;
		}
		this.hasStringConstructor = hasStringConstructor;
		
		List<ClipboardItem> listItems = this.listClibpoardItems();
		List<ClipboardItem> listTransformables = this.listClipboardTransformables();
		listTransformables.removeAll(listItems);
		
		this.countItemsInClipboard = listItems.size() + listTransformables.size();
	}
	
	public synchronized JComponent getComponent() {
		this.reloadComponent();

		this.createButton = new JMenuBar();
		JMenu menu = this.getOperationsWhichGeneratesPopup(clazz); 
		if (menu != null) {
			this.createButton.add(menu);
		}
		
		if (this.showCreateButton){
			this.panel.add(this.createButton, BorderLayout.EAST);
		}
		
		return this.panel;
		
	}
	private ClipboardParamListener listener = null;
	private class ClipboardParamListener implements ClipboardListener {
		public void elementAdded(ClipboardItem item) {
			reloadComponent();
		}

		public void elementRemoved(ClipboardItem item) {
			reloadComponent();
		}
	};
	
	/**
	 * @param nullItemText the nullItemText to set
	 */
	public void setNullItemText(String nullItemText) {
		this.nullItemText = nullItemText;
	}
	
	/**
	 * @return the nullItemText
	 */
	public String getNullItemText() {
		return this.nullItemText;
	}
	
	public int countClipboardItems() {
		return this.countItemsInClipboard;
	}
	
	public boolean isShowingCombo() {
		return this.combo != null;
	}
	
	public boolean isShowingTextField() {
		return this.textField != null;
	}
	
	public boolean hasStringConstructor() {
		return this.hasStringConstructor;
	}
	
	public JComboBox<Object> getCombo() {
		return this.combo;
	}
	
	public JTextField getTextField() {
		return this.textField;
	}
	
	public synchronized JComponent getCurrentComponent() {
		if (this.isShowingCombo()) {
			return this.getCombo();
		} else if (this.isShowingTextField()) {
			return this.getTextField();
		} else {
			return null;
		}
	}
	
	public synchronized String getCurrentString() {
		if (this.isShowingCombo()) {
			if (this.combo.getSelectedItem() instanceof NullItem) {
				return null;
			} else {
				if (this.combo.isEditable() && this.combo.getSelectedIndex() == -1) {
					return this.combo.getSelectedItem().toString();
				} else {
					return "";
				}
			}
		} else if (this.isShowingTextField()) {
			return this.textField.getText();
		} else {
			return "";
		}
	}
	
	public synchronized void clearFields() {
		if (this.isShowingCombo())
			this.combo.setSelectedIndex(-1);
		else if (this.isShowingTextField())
			this.textField.setText("");
	}
	
	public synchronized void setCurrentString(String string) {
		if (this.hasStringConstructor) {
			if (this.isShowingCombo()) {
				this.combo.setSelectedItem(string);
			} else if (this.isShowingTextField()) {
				this.textField.setText(string);
			}
		}
	}
	
	private List<ClipboardItem> listClibpoardItems() {
		List<ClipboardItem> items = new ArrayList<ClipboardItem>();
		
		List<ClipboardItem> clipboardItems = Core.getInstance().getClipboard().getItemsByClass(this.clazz);
		ext: for (ClipboardItem item : clipboardItems) {
			for (ClipboardItem yetAdded : items) {
				if (item.getUserData() == yetAdded.getUserData()) continue ext;
			}
			items.add(item);
		}
		
		return items;
	}
	
	
	private List<ClipboardItem> listClipboardTransformables() {
		List<ClipboardItem> items = new ArrayList<ClipboardItem>();
		
		//transformable elements
		List<Transformer> transformers = Core.getInstance().getTransformersByDestiny(this.clazz);
		List<ClipboardItem> clipboardItems = Core.getInstance().getClipboard().getAllItems();
		ext: for (ClipboardItem item : clipboardItems) {
			for (ClipboardItem yetAdded : items) {
				if (yetAdded == items) continue ext;
			}
			for (Transformer t : transformers) {
				if (item.getUserData() != null && t.getSourceType().isAssignableFrom(item.getUserData().getClass())) {
					items.add(item);
					break;
				}
			}
		}
		
		return items;
	}
	
	private synchronized void reloadComponent() {
		List<ClipboardItem> listItems = this.listClibpoardItems();
		List<ClipboardItem> listTransformables = this.listClipboardTransformables();
		listTransformables.removeAll(listItems);
		
		this.countItemsInClipboard = listItems.size() + listTransformables.size();
		
		final String lastText = this.getCurrentString();
		if (!this.hasStringConstructor || this.countItemsInClipboard > 0 || this.port.allowNull()) {
			final Object lastItem;  // for selecting again if this item continues in the clipboard
			boolean valueSetted = false;
			if (this.isShowingCombo()) {
				lastItem = (this.combo.getSelectedIndex() == -1)?null:this.combo.getSelectedItem();
			} else {
				lastItem = null;
				if (this.textField != null) {
					this.panel.remove(this.textField);
					this.textField.removeFocusListener(ClipboardParamProvider.FOCUS_LISTENER);
					this.textField.removeActionListener(this);
					this.textField.removeKeyListener(this);
					this.textField = null;
				}
				this.combo = new JComboBox<Object>();
				this.combo.addActionListener(this);
				this.combo.addFocusListener(ClipboardParamProvider.FOCUS_LISTENER);
			}
			
			combo.removeAllItems();
			if (this.hasStringConstructor) {
				combo.setEditable(true);
				combo.addKeyListener(this);
			} else {
				combo.setEditable(false);
				combo.removeKeyListener(this);
			}
			
			int itemindex = 0;
			if (listItems != null){
				for (ClipboardItem item : listItems) {
					combo.addItem(item);
					
					if (lastItem != null && item == lastItem){
						combo.setSelectedIndex(itemindex); 
						valueSetted = true;
					}
					
					itemindex++;
					if (!combo.isEditable() || lastText == null || lastText.length() == 0) {
						if (ParamsWindow.preferredClipboardItem!=null && ParamsWindow.preferredClipboardItem == item){
							combo.setSelectedItem(item);
							valueSetted = true;
						}
					}
				}
			}
			
			if (combo.isEditable() && !valueSetted) {
				if (lastText != null && lastText.length() > 0) {
					combo.setSelectedItem(lastText);
					valueSetted = true;					
				} else if (!this.port.defaultValue().equals("")) {
					combo.setSelectedItem(this.port.defaultValue());
					valueSetted = true;
				}
			}
			
			//transformable elements
			List<Transformer> transformers = Core.getInstance().getTransformersByDestiny(this.clazz);
			for(ClipboardItem item : listTransformables/*Core.getInstance().getClipboard().getAllItems()*/){
//				if (listItems.indexOf(item)==-1){ //not added yet!
					for (Transformer t : transformers) {
						if (item.getUserData() != null && t.getSourceType().isAssignableFrom(item.getUserData().getClass())){
							TransformerClipboardItem tci =new TransformerClipboardItem(item, t); 
							combo.addItem(tci);
							if (ParamsWindow.preferredClipboardItem!=null && ParamsWindow.preferredClipboardItem == item){
								combo.setSelectedItem(tci);
							}
						}
					}
//				}
			}
			
			//allowNull
			if (this.port.allowNull()) {
				combo.addItem(new NullItem());
				if (!valueSetted && this.port.defaultValue().equalsIgnoreCase("null")) {
					combo.setSelectedIndex(combo.getItemCount()-1);
				}
			}
		} else {
			if (this.textField == null) {
				if (this.combo != null) {
					this.panel.remove(this.combo);
					this.combo.removeActionListener(this);
					this.combo.removeKeyListener(this);
					this.combo.removeFocusListener(ClipboardParamProvider.FOCUS_LISTENER);
					this.combo = null;
				}
				this.textField = new JTextField();
				this.textField.addActionListener(this);
				this.textField.addKeyListener(this);
				this.textField.addFocusListener(ClipboardParamProvider.FOCUS_LISTENER);
			}
			if (lastText == null || lastText.equals("")) {
				this.textField.setText(this.port.defaultValue());
			} else {
				this.textField.setText(lastText);
			}
		}
		
		this.adjustComponentWidth();
		this.addComponentToPanel();

		if (this.listener == null) {
			this.listener = new ClipboardParamListener();
			Core.getInstance().getClipboard().addClipboardListener(this.listener);
		}
	}
	
	private void addComponentToPanel() {
		if (this.combo != null) {
			this.panel.add(this.combo, BorderLayout.CENTER);
		} else if (this.textField != null) {
			this.panel.add(this.textField, BorderLayout.CENTER);
		}
	}
	
	private void adjustComponentWidth() {
		JComponent component = this.getCurrentComponent();
		if (component != null) {
			//adjust width
			if (component.getPreferredSize().width<50){
				component.setPreferredSize(new Dimension(100, component.getPreferredSize().height));
			}
			if (component.getPreferredSize().width>500){
				component.setPreferredSize(new Dimension(500, component.getPreferredSize().height));
			}
		}
	}
	
	public void finish() {
		if (this.listener != null)
			Core.getInstance().getClipboard().removeClipboardListener(this.listener);
	}

	protected class NullItem {
		public String toString(){
			return ClipboardParamProvider.this.nullItemText;
		}
	}
	
	protected class TransformerClipboardItem {
		ClipboardItem item;
		Transformer transformer;
		TransformerClipboardItem(ClipboardItem item, Transformer t){
			this.item = item;
			this.transformer = t;
		}
		public String toString(){
			if (Boolean.valueOf(Workbench.CONFIG.getProperty("inputdialog.providers.clipboard.showtransformername", "true"))) {
				return this.item.toString()+" ("+transformer.getName()+")";
			} else {
				return this.item.toString();
			}
		}
	}
	
//	class ParamsList {
//		public final ParamSpec[] params;
//		public final String[] names;
//		
//		public ParamsList(List<ParamSpec> params, List<String> names) {
//			this(
//				params.toArray(new ParamSpec[params.size()]),
//				names.toArray(new String[names.size()])
//			);
//		}
//		
//		
//		/**
//		 * @param params
//		 * @param names
//		 */
//		public ParamsList(ParamSpec[] params, String[] names) {
//			this.params = params;
//			this.names = names;
//		}
//	}
	
	synchronized ParamSpec[] listParamSpecs() {
		if (this.isShowingCombo() && this.combo.getItemCount() > 0) {
			final List<ParamSpec> params = new ArrayList<ParamSpec>(this.combo.getItemCount());
//			final List<String> names = new ArrayList<String>(this.combo.getItemCount());
			
			for (int i = 0; i < this.combo.getItemCount(); i++) {
				Object item = this.combo.getItemAt(i);
				
				if (!(item == null || item instanceof NullItem)) {
					/*if (this.combo.isEditable()) {
						params.add(new ParamSpec(this.port.name(), clazz, item.toString(), ParamSource.STRING_CONSTRUCTOR));
//						names.add(item.toString());
					} else {*/
						if (item instanceof TransformerClipboardItem) {
							ParamSpec transformerParam = new ParamSpec(this.port.name(), clazz, ((TransformerClipboardItem) item).item, ParamSource.CLIPBOARD);
							transformerParam.setTransformerSignature(((TransformerClipboardItem) item).transformer.getSignature());
							params.add(transformerParam);
						} else {
							params.add(new ParamSpec(this.port.name(), clazz, item, ParamSource.CLIPBOARD));
						}
					//}
				}
			}
			
			return params.toArray(new ParamSpec[params.size()]);
		} else if (this.isShowingTextField()) {
			return new ParamSpec[] { new ParamSpec(this.port.name(), this.clazz, this.textField.getText(), ParamSource.STRING_CONSTRUCTOR) };
		} else {
			return new ParamSpec[0];
		}
	}

	public synchronized ParamSpec getParamSpec() {
		if (this.isShowingCombo()) {
			if (this.combo.isEditable() && this.combo.getSelectedIndex()==-1){
				if (this.combo.getSelectedItem()==null){
					return new ParamSpec(this.port.name(), clazz, "", ParamSource.STRING_CONSTRUCTOR);
				}
				return new ParamSpec(this.port.name(), clazz, this.combo.getSelectedItem().toString(), ParamSource.STRING_CONSTRUCTOR);
			} else {
				if (combo.getSelectedItem() instanceof TransformerClipboardItem){
					ParamSpec toret = new ParamSpec(this.port.name(), clazz, ((TransformerClipboardItem) combo.getSelectedItem()).item, ParamSource.CLIPBOARD);
					toret.setTransformerSignature(((TransformerClipboardItem)combo.getSelectedItem()).transformer.getSignature());
					return toret;
				} else {
					if (combo.getSelectedItem() instanceof NullItem){
						return new ParamSpec(this.port.name(), clazz, null, ParamSource.CLIPBOARD);
					}else{
						return new ParamSpec(this.port.name(), clazz, combo.getSelectedItem(), ParamSource.CLIPBOARD);
					}
				}
			}
		} else if (this.isShowingTextField()) {
			return new ParamSpec(this.port.name(), this.clazz, this.textField.getText(), ParamSource.STRING_CONSTRUCTOR);
		} else {
			return new ParamSpec(this.port.name(), this.clazz, "", ParamSource.STRING_CONSTRUCTOR);
		}
	}
	
	protected boolean classHasOperations(Class<?> klazz) {
		for (OperationWrapper wrapper : Workbench.getInstance().getInterceptedOperations()) {
			for (Object outcomingType : wrapper.getOperationDefinition().getOutcomingArgumentTypes()) {
				Class<?> clazz = (Class<?>) outcomingType;
				if (klazz.isAssignableFrom(clazz)) {
					return true;
				}
				
				for (Transformer t:Core.getInstance().getTransformersBySource(clazz)) {
					if (klazz.isAssignableFrom(t.getDestinyType())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected JMenu getOperationsWhichGeneratesPopup(Class<?> klazz) {
		HashMap<OperationWrapper, Integer> added= new HashMap<OperationWrapper, Integer>(); //we want to add an operation once
		JMenu popup = new JMenu();
		for (OperationWrapper wrapper : Workbench.getInstance().getInterceptedOperations()){
			WaitOperationWrapper waitWrapper = new WaitOperationWrapper(wrapper.getOperationDefinition(), new OperationResultsCollector() {
				public void resultsGetted(List<ClipboardItem> results) {
					if (results!=null){
						for (ClipboardItem item : results){
							Workbench.getInstance().getTreeManager().elementRemoved(item);
						}	
						getReceiver().removeAfterTermination(results);
					}
				}
			});
			
			for (Object outcomingType: wrapper.getOperationDefinition().getOutcomingArgumentTypes()){
				Class<?> clazz = (Class<?>) outcomingType;
				if (klazz.isAssignableFrom(clazz)){
					if (added.get(wrapper)==null){
						if (Workbench.getInstance().isOperationViewableIn(wrapper.getOperationDefinition(), "DIALOG")){
							added.put(wrapper, 0);
							Utilities.putOperationInMenu(popup, waitWrapper);
						}
					} else {
						continue;
					}
				}
				List<Transformer> transformers = Core.getInstance().getTransformersBySource(clazz);
				for (Transformer t : transformers){
					if (klazz.isAssignableFrom(t.getDestinyType())){
						if (Workbench.getInstance().isOperationViewableIn(wrapper.getOperationDefinition(), "DIALOG")){
							Utilities.putOperationInMenu(popup, waitWrapper);
						}
					}
				}
			}
		}
		if (popup.getItemCount()==0){
			return null;
		}
		
		popup.setIcon(ClipboardParamProvider.ICON_OPERATIONS);
		popup.setToolTipText("Create parameter with an operation.");
//		popup.setText("...");
		return popup;
	}
	
	/**
	 * @return the showCreateButton
	 */
	public boolean isShowCreateButton() {
		return this.showCreateButton;
	}
	
	/**
	 * @param showCreateButton the showCreateButton to set
	 */
	public void setShowCreateButton(boolean showCreateButton) {
		this.showCreateButton = showCreateButton;
		if (showCreateButton) {
			panel.add(createButton, BorderLayout.EAST);
		} else {
			panel.remove(createButton);
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.inputgui.ParamProvider#isValidValue()
	 */
	public synchronized boolean isValidValue() {
		if (!this.port.allowNull()) {
			if (this.combo != null) {
				return this.combo.getSelectedItem() != null;
			} else if (this.textField != null) {
				return true;
			}
		}
		return true;
	}
}
