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
package es.uvigo.ei.aibench.workbench;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.platonos.pluginengine.Extension;
import org.platonos.pluginengine.PluginXmlNode;

import es.uvigo.ei.aibench.Launcher;
import es.uvigo.ei.aibench.Util;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.CoreListener;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.interfaces.IViewFactory;
import es.uvigo.ei.aibench.workbench.tablelayout.TableLayout;
import es.uvigo.ei.aibench.workbench.tablelayout.TableLayout.Slot;
import es.uvigo.ei.aibench.workbench.utilities.CloseableJTabbedPane;
import es.uvigo.ei.aibench.workbench.utilities.TabCloseAdapter;
import es.uvigo.ei.aibench.workbench.utilities.TabCloseEvent;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;

/**
 * This class is the main frame of the AIBench's Workbench.
 * 
 * @author Ruben Dominguez Carbajales
 * @author Hugo López-Fernández
 * @see Workbench
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

	private static final int MAX_TAB_TITLE = 20;

	// Operations as Actions
	private List<OperationWrapper> interceptedOperations = null;

	// Document Viewer JTabbedPane Management
	private HashMap<ClipboardItem, Integer> itemToTabIndex = new HashMap<ClipboardItem, Integer>();
	private HashMap<Integer, ClipboardItem> tabIndexToItem = new HashMap<Integer, ClipboardItem>();
	
	// Plugin's Components management. This hashtable maps strings (components ids) and JComponents 
	private HashMap<String, JComponent> componentMappings = new HashMap<String, JComponent>();
	private HashMap<String, Slot> slotMappings = new HashMap<String, Slot>();
		
	// STATUS BAR
	String STATUS_DEFAULT_TEXT = Workbench.CONFIG.getProperty("mainwindow.statusbar.text");
	private JLabel statusBar = new JLabel(STATUS_DEFAULT_TEXT);
	
	// TOOLBAR NOTE: added by paulo maia
	private JToolBar toolbar = null;

	// LAYOUT SYSTEM
	private TableLayout tableLayout; 
	private String DEFAULT_LAYOUT=
	"<table>"+
		"<row>"+
			"<cell>"+
				"<components id='left'/>"+
			"</cell>"+
			"<cell>"+
				"<table>"+
					"<row>"+
						"<cell>"+
							"<document_viewer />"+
						"</cell>"+
						"<cell>"+
							"<components id='right'/>"+
						"</cell>"+
					"</row>"+
					"<row>"+
						"<cell>"+
							"<components id='bottom'/>"+
						"</cell>"+

					"</row>"+
				"</table>"+
			"</cell>"+
			"<!--<cell>"+
				"<components id='left'/>"+
			"</cell>-->"+
		"</row>"+
	"</table>";
	
	private JMenuBar jMenuBar = null;
	private CloseableJTabbedPane documentTabbedPane = null;
	
	public MainWindow(List<OperationWrapper> operaciones) {
		super();

		this.interceptedOperations = operaciones;
		
		// locate the template.xml
//		URL url = MainWindow.class.getProtectionDomain().getCodeSource().getLocation();
//		try {
//			if (url.getFile().endsWith(".jar")){
//				url = new URL(url.toString().substring(0,url.toString().lastIndexOf('/'))+"/../conf/template.xml");
//			}else{
//				url = new URL(url+"/../../conf/template.xml");
//				
//			}
		String path = System.getProperty("aibench.paths.template.conf", "conf/template.xml");
		URL url = Util.getGlobalResourceURL(path);
		try {
			this.tableLayout = new TableLayout(url.openStream());
			
		}catch (IOException e) {
			LOGGER.warn("Not found a template file, searching in: "+url+" Using default layout....");
			this.tableLayout = new TableLayout(new ByteArrayInputStream(this.DEFAULT_LAYOUT.getBytes()));
			e.printStackTrace();
		}
//		} catch (MalformedURLException e1) {
//			logger.warn("Not found a template file, searching in url: "+url.getFile()+" Using default layout....");
//			this.tableLayout = new TableLayout(new ByteArrayInputStream(this.DEFAULT_LAYOUT.getBytes()));
//			e1.printStackTrace();
//		}
		
		initialize();

		this.setSize(new Dimension(800,600));
	}
	
	public void packSplitters() {
		this.tableLayout.packSplitters();
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
	}

	private void initialize() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				JDialog dialog = new JDialog((Frame) null, "Shutdown");
				JLabel lbl = new JLabel("Shutting down AIBench...");
				int borderSize = 16;
				lbl.setOpaque(true);
				lbl.setBackground(Color.WHITE);
				lbl.setAlignmentY(JLabel.CENTER_ALIGNMENT);
				lbl.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
				dialog.getContentPane().add(lbl);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
				e.getWindow().dispose();
				new Thread(){ //killer thread
					public void run(){						
						Launcher.getPluginEngine().shutdown();						
						System.exit(0);			
					}
				}.start();
			}
		});

		String title = Workbench.CONFIG.getProperty("mainwindow.title");
		if (title == null) {
			title = "AIBench";
		}
		this.setTitle(title);

		JMenuBar menuBar = getJJMenuBar();

		if (Workbench.CONFIG.getProperty("mainwindow.menubar.visible") == null || 
			!Workbench.CONFIG.getProperty("mainwindow.menubar.visible").equals("false")
		) {
			this.setJMenuBar(menuBar);
		}
		
		// ToolBar NOTE: added by paulo maia
		String toolbarVisible = Workbench.CONFIG.getProperty("toolbar.visible");
		if(toolbarVisible != null && toolbarVisible.equals("true")){
			boolean shownames = Workbench.CONFIG.getProperty("toolbar.showOperationNames").equals("true") ? true : false;
			HashMap<Integer,JButton> buttons = new HashMap<Integer,JButton>();  
			
			this.toolbar = new JToolBar("Operations Toolbar");
			
			for(final OperationDefinition<?> op : Core.getInstance().getOperations()){
				if (!Workbench.getInstance().isOperationViewableIn(op, "TOOLBAR"))
					continue;
				JButton button = new JButton(){
					/**
					 * Serial version
					 */
					private static final long serialVersionUID = 1L;
					{
						// enabling/disabling control
						Core.getInstance().addCoreListener(new CoreListener(){
							public void operationDisabled(OperationDefinition<?> definition) {
								if (definition == op){
									setEnabled(false);
								}
								
							}

							public void operationEnabled(OperationDefinition<?> definition) {
								if (definition == op){
									setEnabled(true);
								}
								
							}
							
						});
					}
				};
				button.setEnabled(op.isEnabled());
				
				// name
				if(shownames) button.setText(op.getName());
				// description
				button.setToolTipText(op.getDescription());
				// action
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						Workbench.getInstance().executeOperation(op);
					}					
				});
				// icon
				ImageIcon bigicon = Workbench.getInstance().getOperationBigIcon(op);
				if(bigicon!=null)
					button.setIcon(bigicon);
				else
					button.setIcon(Workbench.getInstance().getOperationIcon(op));
				
				if(!(op.getShortcut().replaceAll(" ","").length()==0))
					buttons.put(Integer.parseInt(op.getShortcut().replaceAll(" ","")), button);				
			}
			
			// separators
			String separatorString = Workbench.CONFIG.getProperty("toolbar.separators");
			ArrayList<String> separators = new ArrayList<String>();
			if (separatorString != null) {

				String[] separatorTokens = separatorString.replaceAll(" ","").split(",");
				for(String sep : separatorTokens){
					if (sep.length()>0) separators.add(sep);
				}
			}
			
			Integer[] buttonShortcuts = new Integer[buttons.size()];
			buttonShortcuts = buttons.keySet().toArray(buttonShortcuts);
			Arrays.sort(buttonShortcuts);

			
			for(Integer i: buttonShortcuts){				
				this.toolbar.add(buttons.get(i));
				for(String sep:separators){
					try{
						if((Integer.parseInt(sep))==i.intValue()){
							this.toolbar.addSeparator();
							break;
						}
					}
					catch(NumberFormatException e){
						LOGGER.error("The toolbar separators must be numbers!, found: "+sep);
					}
				}
			}			
			
		}		

		this.getContentPane().setLayout(new BorderLayout());
		this.tableLayout.getDocumentViewerPanel().setLayout(new BorderLayout());
		this.tableLayout.getDocumentViewerPanel().add(getDocumentTabbedPane(), BorderLayout.CENTER);
		
		this.getContentPane().add(this.tableLayout, BorderLayout.CENTER);
		
		if (Workbench.CONFIG.getProperty("mainwindow.statusbar.visible")==null || !Workbench.CONFIG.getProperty("mainwindow.statusbar.visible").equals("false")){
			this.add(this.statusBar, BorderLayout.SOUTH);
		}
		
		if(toolbarVisible != null && toolbarVisible.equals("true")){
			String positionString = Workbench.CONFIG.getProperty("toolbar.position");
			String position = BorderLayout.NORTH;
			int orientation = SwingConstants.HORIZONTAL;
			
			if (positionString != null) {
				if (positionString.equalsIgnoreCase("SOUTH")) {
					orientation = SwingConstants.HORIZONTAL;
					position = BorderLayout.SOUTH;
				} else if (positionString.equalsIgnoreCase("EAST")) {
					orientation = SwingConstants.VERTICAL;
					position = BorderLayout.EAST;
				} else if (positionString.equalsIgnoreCase("WEST")) {
					orientation = SwingConstants.VERTICAL;
					position = BorderLayout.WEST;
				}
			}
				
			this.toolbar.setOrientation(orientation);
			this.add(toolbar, position);
		}
	}
	
	public CloseableJTabbedPane getDocumentTabbedPane() {
		if (this.documentTabbedPane == null) {
			this.documentTabbedPane = new CloseableJTabbedPane();
			
			this.documentTabbedPane.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					final int index = MainWindow.this.documentTabbedPane.getSelectedIndex();
					if (index != -1) {
						final ClipboardItem data = MainWindow.this.tabIndexToItem.get(index);
						
						if (data != null) {
							Workbench.getInstance().setActiveData(data);
						}
					}
				}
			});
			this.documentTabbedPane.addTabCloseListener(new TabCloseAdapter() {
				@Override
				public void tabClosing(TabCloseEvent event) {
					event.cancel(); // Will be closed by the MainWindow

					ClipboardItem clipboardItem = MainWindow.this.tabIndexToItem.get(event.getTabIndex());
					if (clipboardItem != null) {
						Workbench.getInstance().hideData(clipboardItem);
					} else {
						removeTabAt(event.getTabIndex());
					}
				}
			});
			
			this.documentTabbedPane.setPreferredSize(new Dimension(640, 480));
		}
		
		return this.documentTabbedPane;
	}

	/**
	 * Removes the tab placed at the specified index and updates the data maps
	 * ({@code itemToTabIndex} and {@code tabIndexToTab}) in order to set the
	 * correct positions for those items placed in a tab index higher than
	 * {@code tabIndex}.
	 * 
	 * @param tabIndex the index of the tab that should be removed.
	 */
	protected void removeTabAt(int tabIndex) {
		this.documentTabbedPane.removeTabAt(tabIndex);
		this.updateDataMaps(tabIndex);
	}

	private JMenuBar getJJMenuBar() {
		if (jMenuBar == null) {
			jMenuBar = new JMenuBar();
			List<OperationWrapper> operations = this.interceptedOperations;
			this.sortOperations(operations);
			for (OperationWrapper opw : operations) {
				if (Workbench.getInstance().isOperationViewableIn(opw.getOperationDefinition(), "MENU")) {
					Utilities.putOperationInMenu(jMenuBar, opw);
				}
			}
			this.setJMenuBarIcons();
		}

		return jMenuBar;
	}

	private void setJMenuBarIcons() {
		setJMenuBarIcons(jMenuBar, getIconMappings());
	}

	private Map<String, ImageIcon> getIconMappings() {
		Map<String, ImageIcon> menuIcons = new HashMap<>();
		for (Extension extension : WorkbenchExtensionTools.getWorkbenchViewExtensions()) {
			for (PluginXmlNode node : extension.getExtensionXmlNode().getChildren()) {
				if (node.getName().equals("menu-icon")) {
					String menuName = node.getAttribute("menu");
					String menuIcon = node.getAttribute("icon");
					if (menuName != null && menuIcon != null) {
						if(menuIcons.containsKey(menuName)) {
							LOGGER.warn("Found duplicated menu-icon declaration. "
								+ "Overriding menu icon for path " + menuName +
								" with icon from plugin " + extension.getPlugin().getName());
						}
						menuIcons.put(menuName, new ImageIcon(Util.getGlobalResourceURL(menuIcon)));
					}
				}
			}
		}
		return menuIcons;
	}

	private void setJMenuBarIcons(JMenuBar menuBar, Map<String, ImageIcon> menuIcons) {
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			JMenu menu = menuBar.getMenu(i);
			String menuText = menu.getText();
			if (menuIcons.containsKey(menuText)) {
				menu.setIcon(menuIcons.get(menuText));
			}
			setJMenuIcons(menu, menuIcons);
		}
	}

	private void setJMenuIcons(JMenu parentMenu, Map<String, ImageIcon> menuIcons) {
		String parentMenuName = parentMenu.getText();
		for (int i = 0; i < parentMenu.getItemCount(); i++) {
			JMenuItem menu = parentMenu.getItem(i);
			if (menu instanceof JMenu) {
				JMenu jMenu = (JMenu) menu;
				String menuName = jMenu.getText();
				if (menuIcons.containsKey(parentMenuName + "/" + menuName)) {
					jMenu.setIcon(menuIcons.get(parentMenuName + "/" + menuName));
				}
			}
		}
	}

	private int getPositionForPathName(String pathElement){
		
		String noAt = pathElement;
		int res = 0;
		if (pathElement.indexOf("@")!=-1){
			noAt = noAt.substring(noAt.indexOf("@")+1);
		}
		if (Workbench.CONFIG.get("menu."+noAt)!=null){
			
			try{
				res = Integer.parseInt(Workbench.CONFIG.get("menu."+noAt).toString().trim());
			}catch(NumberFormatException ex){
				LOGGER.warn("Configuration error: Property menu."+noAt+" must be an integer");
			}
			
		}
		else if (pathElement.indexOf("@")!=-1){
			res= Integer.parseInt(pathElement.substring(0, pathElement.indexOf("@")));
		}else{
			res= 0;
		}
		//System.err.println("get position for "+pathElement+" : "+res);
		return res;
		
	}
	/*
	 * ============================================
	 * MENU DEPLOYING BASED ON OPERATION'S PATH
	 * ============================================
	 */
	private void sortOperations(List<OperationWrapper> operations){
		Vector<OperationWrapper> res = new Vector<OperationWrapper>();

		while(operations.size()>0){

			OperationWrapper currentOper = operations.remove(0);

			String path = currentOper.getOperationDefinition().getPath();
			if (path==null) continue;

			int currentNumber=0;

			//String currentNumberString="";
			StringTokenizer tk = new StringTokenizer(path,"/");

			int pow_i =5;
			while(tk.hasMoreTokens()){
				String pathElement = tk.nextToken();
				if (pathElement.indexOf("@")!=-1){
					currentNumber+=getPositionForPathName(pathElement)*Math.pow(10f,(double)pow_i);
					//currentNumberString+=pathElement.substring(0, pathElement.indexOf("@"))+"000";

				}
				pow_i--;
			}



			int i = 0;
			while(i < res.size()){
				String iPath = res.get(i).getOperationDefinition().getPath();
				int iNumber=0;

				//String iNumberString="";
				StringTokenizer tk2 = new StringTokenizer(iPath,"/");
				int pow_j=5;
				while(tk2.hasMoreTokens()){
					String pathElement = tk2.nextToken();
					if (pathElement.indexOf("@")!=-1){
						iNumber += getPositionForPathName(pathElement)*Math.pow(10f, (double)pow_j);
					}
					pow_j--;
				}





				if(iNumber>currentNumber){
					break;
				}
				i++;
			}
			res.insertElementAt(currentOper, i);

		}

		operations.addAll(res);
	}
	


	class RenderingMessageComponent extends JPanel{
		private static final long serialVersionUID = 1L;

		public RenderingMessageComponent(String message){
			this.setLayout(new BorderLayout());
			JLabel label = new JLabel(message);
			label.setHorizontalAlignment(JLabel.CENTER);
			this.add(label, BorderLayout.CENTER);
		}
	}

	/*
	 * =========================================================================================
	 * IMPLEMENTACION DE OPERACIONES NO RELACIONADAS DIRECTAMENTE CON
	 * COMPONENTES
	 * =========================================================================================
	 */
	public void showViews(final List<IViewFactory> views, final ClipboardItem data) {
		JComponent viewsComponent = null;
		final boolean hideTabs = views.size() == 1 && Workbench.CONFIG.getProperty("documentviewer.hide_tabs_when_single_view")!=null && Workbench.CONFIG.getProperty("documentviewer.hide_tabs_when_single_view").equals("true");

		if (hideTabs) {
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			viewsComponent = panel;
		} else {
			JTabbedPane tabbedViews = new JTabbedPane();
			tabbedViews.setTabPlacement(JTabbedPane.BOTTOM);
			viewsComponent = tabbedViews;
		}
		
		for (int i = 0; i < views.size(); i++) {
			if (LOGGER.getEffectiveLevel().equals(Level.DEBUG)) {
				LOGGER.debug("Adding view " + views.get(i));
			}
			
			final IViewFactory view= views.get(i);
			
			class RenderingThread extends Thread{
				private JComponent viewsComponent;
				public RenderingThread(JComponent viewsComponent) {
					this.viewsComponent = viewsComponent;
				}
				public void run(){				
					final RenderingMessageComponent renderingMessage = new RenderingMessageComponent("Rendering view \""+view.getViewName()+"\", please wait...");
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							synchronized(viewsComponent){
								if (!hideTabs){
									((JTabbedPane)viewsComponent).add(renderingMessage, view.getViewName());
								}else{
									viewsComponent.add(renderingMessage, BorderLayout.CENTER);
									((JPanel)viewsComponent).validate();
								}
							}
						}
					});
					
					final JComponent component = view.getComponent(data.getUserData());
					
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							synchronized(viewsComponent){
								//find tab placement of renderingMessage
								if (!hideTabs){
									JTabbedPane tabbedViews = (JTabbedPane) viewsComponent;
									for (int i = 0; i<tabbedViews.getTabCount(); i++){
										if (tabbedViews.getComponentAt(i) == renderingMessage){
											if (component instanceof Scrollable){
												tabbedViews.setComponentAt(i,new JScrollPane(component));
											}else{
												tabbedViews.setComponentAt(i,component);
											}
										}
									}
								}else{
									((JPanel)viewsComponent).remove(renderingMessage);
									if (component instanceof Scrollable){
										((JPanel)viewsComponent).add(new JScrollPane(component), BorderLayout.CENTER);
									}else{
										((JPanel)viewsComponent).add(component, BorderLayout.CENTER);
										((JPanel)viewsComponent).validate();
									}
								}
							}
						}
					});
				}
			};
			
			new RenderingThread(viewsComponent).start();
	
		}

		this.getDocumentTabbedPane().addTab(getTabTitle(data), null, viewsComponent);
		int viewsComponentIndex = getDocumentTabbedPane().indexOfComponent(viewsComponent);
		this.getDocumentTabbedPane().setSelectedIndex(viewsComponentIndex);

		this.itemToTabIndex.put(data, viewsComponentIndex);
		this.tabIndexToItem.put(viewsComponentIndex, data);
	}

	protected static String getTabTitle(ClipboardItem data) {
		String title = data.getName();
		if (title.length() > MAX_TAB_TITLE) {
			title = title.substring(0, MAX_TAB_TITLE);

			title += "...";
		}
		return title;
	}

	public synchronized List<Component> getDataViews(ClipboardItem data) {
		ArrayList<Component> components = new ArrayList<Component>();
		
		if (this.itemToTabIndex.containsKey(data)) {
			int position = this.itemToTabIndex.get(data);
			Component component = this.getDocumentTabbedPane().getComponentAt(position);
			if (component instanceof JTabbedPane) {
				JTabbedPane pane = (JTabbedPane) component;
				for (int i=0; i<pane.getComponentCount(); i++) {
					components.add(pane.getComponentAt(i));
				}
			} else {
				components.add(component);
			}
		}
		
		return components;
	}

	public synchronized void bringToFront(ClipboardItem data) {
		if(data == null) {
			LOGGER.warn("bringToFront(ClibpoardItem data): attempting to "
					+ "make visible a null ClipboardItem");
			return;
		}

		if (this.itemToTabIndex.containsKey(data)) {
			int position = this.itemToTabIndex.get(data);
			if (position < getDocumentTabbedPane().getTabCount()) {
				getDocumentTabbedPane().setSelectedIndex(position);
			} else {
				LOGGER.warn("bringToFront(ClibpoardItem data): attempting to "
						+ "make visible an index higher than actual tab "
						+ "count. Index = " + position + ". Data name " + 
						data.getName());
			}
		} else {
			LOGGER.warn("bringToFront(ClibpoardItem data): attempting to "
					+ "make visible a ClipboardItem which is not opened. "
					+ "Data name = " + data.getName());
		}
	}

	public synchronized void hideData(ClipboardItem data) {
		if (this.itemToTabIndex.containsKey(data)) {
			int dataToHideIndex = this.itemToTabIndex.remove(data);
			this.tabIndexToItem.remove(dataToHideIndex);
			this.removeTabAt(dataToHideIndex);
		} else {
			Core.getInstance().getGUI().warn("MainWindow.hideData(): Not opened data [ " + data.toString() + " ]");
		}
	}
	
	protected void updateDataMaps(int removeIndex) {
		Map<Integer, ClipboardItem> newPositions = new HashMap<>();
		for (Entry<ClipboardItem, Integer> entry : this.itemToTabIndex.entrySet()) {
			ClipboardItem item = entry.getKey();
			int oldPosition = entry.getValue();
			if (oldPosition > removeIndex) {
				int newPosition = oldPosition - 1;
				entry.setValue(newPosition);
				this.tabIndexToItem.remove(oldPosition);
				newPositions.put(newPosition, item);
			}
		}
		this.tabIndexToItem.putAll(newPositions);
	}

	public void closeData(ClipboardItem data) {}

	/*
	 * =========================================================================================
	 * FIN IMPLEMENTACION DE OPERACIONES NO RELACIONADAS DIRECTAMENTE CON
	 * COMPONENTES
	 * =========================================================================================
	 */

	public void dataActivated(Data data) {}

	public void dataSelected(boolean flag) {}

	public void dataChanged(Data data) {}

	public void dataUnloaded(Data data) {}

	/**
	 * CAUTION: This method shouldn't be used. Use {@link Workbench#putItemInSlot(String, String, String, JComponent)} instead.
	 * 
	 * @param slotName name of the slot where the item should be placed.
	 * @param componentName name of the component.
	 * @param componentID identifier of the component.
	 * @param component the component to be placed in the main window.
	 */
	public synchronized void putItemInSlot(String slotName, String componentName, String componentID, JComponent component){
		if (this.slotMappings.get(componentID)!=null){			
			this.slotMappings.get(componentID).remove(this.componentMappings.get(componentID));
			this.slotMappings.remove(componentID);
		}
		
		Slot slot = this.tableLayout.getSlotByID(slotName);
		this.slotMappings.put(componentID, slot);
		
		if (slot != null){
			/*JTabbedPane tabbed = null;
			if (slot.getComponentCount() == 0){
				slot.setLayout(new BorderLayout());
				tabbed = new JTabbedPane();
				
				slot.add(tabbed, BorderLayout.CENTER);
			}else{
				tabbed = (JTabbedPane) slot.getComponent(0);
				
			}
			
			tabbed.addTab(componentName, component);*/
			
			slot.addComponent(componentName, component);
			this.componentMappings.put(componentID, component);
			this.pack();
		}else{
			LOGGER.warn("slot not found to place some component: "+slotName);
		}
	}
	
	/**
	 * CAUTION: This method should't be used. Use {@link Workbench#getAvailableSlotIDs()} instead.
	 * 
	 * @return a list with the available slot ids.
	 */
	public List<String> getAvailableSlotIDs(){
		return this.tableLayout.getAvailableSlots();
	}
	
	/**
	 * Returns the component located at a given slot ID.
	 * 
	 * @param componentID The ID of the component.
	 * @return the component located at componentID.
	 */
	public JComponent getComponentAtSlot(final String componentID) {
		return this.componentMappings.get(componentID);
	}
	
	public synchronized JComponent removeComponentFromSlot(String componentID){
		JComponent component = this.componentMappings.get(componentID);
		
		//is there one component with the same id ?
		if (component != null){
			if (component.getParent() instanceof JTabbedPane){
				JTabbedPane theTabbed = (JTabbedPane) component.getParent();
				theTabbed.remove(component);				
			}
		}
		
		this.pack();
		return component;
	}

	/**
	 * @return the statusBar.
	 */
	public JLabel getStatusBar() {
		return this.statusBar;
	}

	public final JToolBar getToolbar() {
		return this.toolbar;
	}
}
