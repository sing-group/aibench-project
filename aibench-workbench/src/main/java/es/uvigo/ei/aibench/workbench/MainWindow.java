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
import java.util.Iterator;
import java.util.List;
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
import javax.swing.JMenuBar;
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
 * @see Workbench
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MainWindow.class);

	private final int MAX_TAB_TITLE = 20;


	// Operations as Actions
	private List<OperationWrapper>	interceptedOperations = null;

	// Document Viewer JTabbedPane Management
	private HashMap<ClipboardItem, Integer> tabIndexByData	= new HashMap<ClipboardItem, Integer>();
	private HashMap<Integer, ClipboardItem>	dataByTab = new HashMap<Integer, ClipboardItem>();

	
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
	
	// COMPONENTS
	private JMenuBar jJMenuBar	= null;
	private CloseableJTabbedPane documentTabbedPane = null;
//	private CloseAndMaxTabbedPane documentTabbedPane = null;
	
	public MainWindow(List<OperationWrapper> operaciones) {
		super();
		logger.info("MainWindow creation");
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
			logger.warn("Not found a template file, searching in: "+url+" Using default layout....");
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
//				JOptionPane pane = new JOptionPane("Finishing application...", JOptionPane.INFORMATION_MESSAGE);

//		this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
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
		if (title == null) title = "AIBench";
		this.setTitle(title);
		
		JMenuBar menuBar = getJJMenuBar();
		if (Workbench.CONFIG.getProperty("mainwindow.menubar.visible")==null || !Workbench.CONFIG.getProperty("mainwindow.menubar.visible").equals("false")){
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
			
			//separators
			String separatorString = Workbench.CONFIG.getProperty("toolbar.separators");
			ArrayList<String>separators = new ArrayList<String>(); 
			if(separatorString!=null){
				
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
						logger.error("The toolbar separators must be numbers!, found: "+sep);
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
			
			if(positionString !=null){				
				if(positionString.equalsIgnoreCase("SOUTH")){
					orientation = SwingConstants.HORIZONTAL;
					position = BorderLayout.SOUTH;
				}
				else if(positionString.equalsIgnoreCase("EAST")){
					orientation = SwingConstants.VERTICAL;
					position = BorderLayout.EAST;
				}
				else if(positionString.equalsIgnoreCase("WEST")){
					orientation = SwingConstants.VERTICAL;
					position = BorderLayout.WEST;
				}
					
			}
				
			this.toolbar.setOrientation(orientation);
			this.add(toolbar,position);
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
						final ClipboardItem data = MainWindow.this.dataByTab.get(index);
						
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
					ClipboardItem clipboardItem = MainWindow.this.dataByTab.get(event.getTabIndex());
					if (clipboardItem != null) {
						Workbench.getInstance().hideData(clipboardItem);
					} else {
						documentTabbedPane.removeTabAt(event.getTabIndex());
					}
				}
			});
			
			this.documentTabbedPane.setPreferredSize(new Dimension(640, 480));
		}
		
		return this.documentTabbedPane;
	}
//	
//	private CloseAndMaxTabbedPane getDocumentTabbedPane() {
//		if (documentTabbedPane == null) {
//			documentTabbedPane = new CloseAndMaxTabbedPane(true);
//			documentTabbedPane.setMaxIcon(false);
//
//			documentTabbedPane.addCloseListener(new CloseListener() {
//				public void closeOperation(MouseEvent arg0) {
//					CloseDataAction close = new CloseDataAction();
//					close.actionPerformed(null);
//				}
//			});
//
//			documentTabbedPane.addChangeListener(new ChangeListener() {
//				public void stateChanged(ChangeEvent arg0) {
//					int index = documentTabbedPane.getSelectedIndex();
//					ClipboardItem data = MainWindow.this.dataByTab.get(index);
//
//					if (data != null) {
//						Workbench.getInstance().setActiveData(data);
//					}
//				}
//			});
//			
//			documentTabbedPane.setPreferredSize(new Dimension(640, 480));
//		}
//
//		return documentTabbedPane;
//	}

	private JMenuBar getJJMenuBar() {

		if (jJMenuBar == null) {

			jJMenuBar = new JMenuBar();
			List<OperationWrapper> operations = this.interceptedOperations;

			sortOperations(operations);

			for (OperationWrapper opw: operations){
				if (Workbench.getInstance().isOperationViewableIn(opw.getOperationDefinition(), "MENU")){
					Utilities.putOperationInMenu(jJMenuBar, opw);
				}
//				putOperationInMenu(jJMenuBar, opw);
			}
		}

		return jJMenuBar;
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
				logger.warn("Configuration error: Property menu."+noAt+" must be an integer");
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
		final boolean hide_tabs = views.size() == 1 && Workbench.CONFIG.getProperty("documentviewer.hide_tabs_when_single_view")!=null && Workbench.CONFIG.getProperty("documentviewer.hide_tabs_when_single_view").equals("true");
		if (hide_tabs){
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			viewsComponent = panel;
		}else{
			JTabbedPane tabbedViews = new JTabbedPane();
			tabbedViews.setTabPlacement(JTabbedPane.BOTTOM);
			viewsComponent = tabbedViews;
		}


		
		for (int i = 0; i < views.size(); i++) {
			if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("Adding view "+views.get(i));
			
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
								if (!hide_tabs){
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
								if (!hide_tabs){
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

		//We don't want tab titles too long!!
		String title = data.getName();
		if (title.length()>MAX_TAB_TITLE){
			title = title.substring(0,MAX_TAB_TITLE);

			title += "...";
		}
		
		this.getDocumentTabbedPane().addTab(title, null, viewsComponent);

		this.getDocumentTabbedPane().setSelectedIndex(getDocumentTabbedPane().indexOfComponent(viewsComponent));

//		getDocumentTabbedPane().setMaxIcon(false);

		this.tabIndexByData.put(data, getDocumentTabbedPane().indexOfComponent(viewsComponent));
		this.dataByTab.put(getDocumentTabbedPane().indexOfComponent(viewsComponent), data);

	}
	
	public synchronized List<Component> getDataViews(ClipboardItem data) {
		ArrayList<Component> components = new ArrayList<Component>();
		
		if (this.tabIndexByData.containsKey(data)) {
			int posicion = this.tabIndexByData.get(data);
			Component component = this.getDocumentTabbedPane().getComponentAt(posicion);
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
		/*
		 * Tenemos que comprobar si está abierta o no, por si acaso.
		 */
		if (this.tabIndexByData.containsKey(data)) {

			int posicion = this.tabIndexByData.get(data);

			this.getDocumentTabbedPane().setSelectedIndex(posicion);

		} else {
			//Core.getInstance().getGUI().warn("MainWindow.bringToFront(): Not opened data [ " + data.toString() + " ]");
		}
	}

	public synchronized void hideData(ClipboardItem data) {
		if (this.tabIndexByData.containsKey(data)) {

			int index = this.tabIndexByData.get(data);

			getDocumentTabbedPane().remove(index);

			this.tabIndexByData.remove(data);
			
			int pos = index;
			while(pos<this.dataByTab.size()-1){
				this.dataByTab.put(pos, this.dataByTab.get(pos+1));
				pos ++;
			}
			this.dataByTab.remove(this.dataByTab.size()-1);

			Iterator<Entry<ClipboardItem, Integer>> iterator = this.tabIndexByData.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<ClipboardItem, Integer> entry = iterator.next();
				int posicion = entry.getValue().intValue();
				if (posicion > index) {
					entry.setValue(--posicion);
				}
			}
		} else {
			Core.getInstance().getGUI().warn("MainWindow.hideData(): Not opened data [ " + data.toString() + " ]");
		}
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
			logger.warn("slot not found to place some component: "+slotName);
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
