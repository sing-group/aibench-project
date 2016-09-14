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
 * Workbench.java
 *
 * This file is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 29/09/2005
 */
package es.uvigo.ei.aibench.workbench;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.help.HelpBroker;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.platonos.pluginengine.Extension;
import org.platonos.pluginengine.ExtensionPoint;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngine;
import org.platonos.pluginengine.PluginXmlNode;

import es.uvigo.ei.aibench.TextAreaAppender;
import es.uvigo.ei.aibench.Util;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.CoreListener;
import es.uvigo.ei.aibench.core.CoreUtils;
import es.uvigo.ei.aibench.core.IGenericGUI;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.ProgressHandler;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.clipboard.ClipboardListener;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.inputgui.ParamsWindow;
import es.uvigo.ei.aibench.workbench.interfaces.AbstractViewFactory;
import es.uvigo.ei.aibench.workbench.interfaces.IViewFactory;
import es.uvigo.ei.aibench.workbench.interfaces.WorkbenchListener;
import es.uvigo.ei.aibench.workbench.tree.AIBenchJTreeManager;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;

/**
 * This class is the AIBench's first Workbench, that is, the default GUI
 * <ul>
 * <li>Puts the operations in menus</li>
 * <li>Creates default input dialogs</li>
 * <li>Creates default progress monitors</li>
 * <li>Creates a default viewer to clipboard items</li>
 * <li>Shows the Core's clipboard and History through trees</li>
 * <li>Manages the operation's custom views, icons and input dialogs, and uses
 * them instead the default solutions, when available</li>
 * <li>Gives an abstract and flexible layout system based on slots (with ids),
 * configurable through the /conf/template.xml file</li>
 * </ul>
 * 
 * @author Ruben Dominguez Carbajales, Daniel Glez-Peña
 */
public class Workbench implements IGenericGUI, ClipboardListener {
	/**
	 * Logger
	 */
	final static Logger logger = Logger.getLogger(Workbench.class.getName());

	/**
	 * Singleton reference
	 */
	private static Workbench _instance = null;

	/**
	 * The main window
	 */
	private MainWindow mainWindow = null;

	/**
	 * The operations as Actions
	 */
	private final List<OperationWrapper>	interceptedOperations	= new ArrayList<OperationWrapper>();
	
	/**
	 * OperationWrappers associated to OperationDefinitions
	 */
	private final Map<OperationDefinition<?>, OperationWrapper>	operationDefinition2Wrapper = new HashMap<OperationDefinition<?>, OperationWrapper>();

	/**
	 * Custom input GUI connected
	 */
	private final Map<OperationDefinition<?>, Class<?>> operationGUI = new HashMap<OperationDefinition<?>, Class<?>>();

	
	/**
	 * Custom data-types views
	 */
	private final Map<Class<?>, List<IViewFactory>> dataTypeViews = new HashMap<Class<?>, List<IViewFactory>>();

	private JComponent welcomeScreen;
	private String welcomeScreenTitle;
	
	/**
	 * Custom operation icons
	 */
	private final Map<OperationDefinition<?>, ImageIcon> operationIcons = new HashMap<OperationDefinition<?>, ImageIcon>();
	
	/**
	 * Custom operation big icons
	 * NOTE: added by paulo maia
	 */
	private final Map<OperationDefinition<?>, ImageIcon> operationBigIcons = new HashMap<OperationDefinition<?>, ImageIcon>();
	
	/**
	 * Custom datatypes icons
	 */
	private final Map<Class<?>, ImageIcon> dataTypeIcon = new HashMap<Class<?>, ImageIcon>();

	/**
	 * Custom operation visibility
	 */
	private final Map<OperationDefinition<?>, Vector<String>> operationVisibility = new HashMap<OperationDefinition<?>, Vector<String>>();
	
	/**
	 * Opened clipboard items in the document view
	 */
	private List<ClipboardItem> openedItems = new Vector<ClipboardItem>();
	
	/**
	 * Closed clipboard items that was showed before
	 */
	private final List<ClipboardItem> closedItems = new Vector<ClipboardItem>();

	/**
	 * Workbench listeners
	 */
	private final List<WorkbenchListener> workbenchListeners = new ArrayList<WorkbenchListener>();

	/**
	 * Hashtable mapping operation's session keys and their monitorizing dialogs
	 */
	private final Map<Object, MonitorizeDialog> monitors = new HashMap<Object, MonitorizeDialog>();
	
	/**
	 * The current showing item
	 */
	private ClipboardItem activeItem = null;
	
	/**
	 * Configuration
	 */
	public final static Properties CONFIG = new Properties();


	///////////////////////////////////////////////
	
	static {
		Workbench.readConfig();
	}
	
	/**
	 * This method must only be called from the plugin engine. Use <code>getInstance()</code>
	 * @see getInstance
	 */
	public Workbench() {
		synchronized (Workbench.class) {
			if (_instance == null) {
				_instance = this;
			} else {
				throw new RuntimeException("This is a singleton.");
			}
			Core.getInstance().getClipboard().addClipboardListener(this);
			
			//listen to operation enabled/disabled
			Core.getInstance().addCoreListener(new CoreListener(){
				public void operationDisabled(OperationDefinition<?> definition) {
					operationDefinition2Wrapper.get(definition).setEnabled(false);
				}
	
				public void operationEnabled(OperationDefinition<?> definition) {
					operationDefinition2Wrapper.get(definition).setEnabled(true);
				}
			});
		}
	}

	/**
	 * Gives access to the main window
	 * @return the main window
	 */
	public JFrame getMainFrame() {
		return mainWindow;
	}
	
	/**
	 * Creates the singleton instance.
	 */
	private static synchronized void createInstance() {
		if (_instance == null) {
			new Workbench();
		}
	}

	/**
	 * Gives access to the Workbench instance
	 * @return
	 */
	public static Workbench getInstance() {
		if (_instance == null) {
//			_instance = new Workbench();
			Workbench.createInstance();
		}
		return _instance;
	}
	
	/**
	 * Sets the active data.
	 * CAUTION: This method should not be called outside the core plugins. It need to be public due to class loaders issues
	 * @param data The data to set active
	 */
	public void setActiveData(ClipboardItem data) {
		if (this.activeItem != data) {
			this.activeItem = data;
		}
		
		this.mainWindow.bringToFront(data);	
	}
	
	/**
	 * Gives access to the tree manager and the aibench's session and clipboard's trees
	 * @return The AIBench Tree Manager
	 */
	public AIBenchJTreeManager getTreeManager() {
		return AIBenchJTreeManager.getInstance();
	}

	/**
	 * Gives access to the WorkBench's Menu Bar
	 * @return the menu bar
	 */
	public JMenuBar getMenuBar() {
		if(System.getProperty("aibench.nogui") != null){
			throw new RuntimeException("no menu bar if aibench.nogui property was set");
		}
		return this.mainWindow.getJMenuBar();
	}

	/**
	 * Gives access to the WorkBench's Tool Bar
	 * @return the tool bar
	 */
	public JToolBar getToolBar() {
		if(System.getProperty("aibench.nogui") != null){
			throw new RuntimeException("no tool bar if aibench.nogui property was set");
		}
		return this.mainWindow.getToolbar();
	}


	/**
	 * Shows a clipboard item within a view (custom or default)
	 * @param data The clipboard item
	 */
	public void showData(final ClipboardItem data) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showData(data);
				}
			});
		} else {
			if (data != null) {
				List<IViewFactory> availableViews = searchView(data.getUserData());
				List<IViewFactory> copied = null;
				if (availableViews!=null) {
					copied = new ArrayList<IViewFactory>();
					copied.addAll(availableViews);
				}
				if (copied ==null) {
					copied = new ArrayList<IViewFactory>();
					copied.add(new DefaultViewFactory());
				}else{
					if (CONFIG.getProperty("documentviewer.allways_show_default_view")!=null && CONFIG.getProperty("documentviewer.allways_show_default_view").equals("true")) {
						copied.add(new DefaultViewFactory());
					}
						
				}

				// Handle Datatype viewable annotation
				if (data.getUserData()!=null) {
					Datatype annot = data.getUserData().getClass().getAnnotation(Datatype.class);
					if (annot != null && !annot.viewable() && copied.size() == 1 && copied.get(0) instanceof DefaultViewFactory) {
						return;
					}
				}
				if (copied.size() != 0) {
					if (this.openedItems.contains(data)) {
						/*
						 * data have already been showed, so we only have to bring
						 * them to front
						 */
						this.mainWindow.bringToFront(data);

					} else if (this.closedItems.contains(data)) {
						/*
						 * Data have already been showed, but it's closed. We have
						 * to show it again.
						 */
						this.mainWindow.showViews(copied, data);
						this.openedItems.add(data);
						this.closedItems.remove(data);
					} else {
						/*
						 * Data have never been showed before. We have to show it
						 * and add a node in the tree.
						 */
						this.mainWindow.showViews(copied, data);
						//AIBenchJTreeManager.getInstance().showData(data);
						this.openedItems.add(data);

					}

					if (this.getActiveData()!=null) {
						this.fireDataHided(this.getActiveData());
					}
					
					this.setActiveData(data);
					
					this.fireDataShowed(data);

				} else {
					logger.warn("Not available views");
				}
			} else {
				logger.warn("Workbench.showData(): Data is NULL");
			}
		}
	}

	/**
	 * Hides the views of a clipboard item
	 * @param data the data
	 */
	public void hideData(ClipboardItem data) {
		this.openedItems.remove(data);
		this.closedItems.add(data);
		
		this.mainWindow.hideData(data);
		
		this.fireDataClosed(data);
		
		if (this.openedItems.size() == 0) {
			this.setActiveData(null);
		} else if (this.getActiveData() == data) {
			this.setActiveData(this.openedItems.get(0));
			this.showData(this.activeItem);
		}
		
		if (logger.getEffectiveLevel().equals(Level.DEBUG))
			logger.debug("Closed " + (data.getUserData()!=null?data.getUserData().toString().substring(0, Math.min(data.getUserData().toString().length(),100)):" null item"));
		
	}
	
	/**
	 * Hides all the views of all showing clipboard items
	 */
	public void hideAllData() {
		if (openedItems.size() > 0) {
			this.hideData(this.openedItems.get(0));
			this.hideAllData();
		}
	}

	/**
	 * Gives acces to the current active data, that is, the data showing
	 * @return the active data
	 */
	public ClipboardItem getActiveData() {
		return this.activeItem;
	}

	/**
	 * Returns the custom icon of an operation, if any
	 * @param operation The operation
	 * @return The icon, null if it was not established any
	 */
	public ImageIcon getOperationIcon(OperationDefinition<?> operation) {
		if (operation == null) return null;
		else return this.operationIcons.get(operation);
	}

	/**
	 * Returns the custom icon of an operation, if any
	 * @param uid The operation UID
	 * @return The icon, null if it was not established any
	 */
	public ImageIcon getOperationIcon(String uid) {
		return this.getOperationIcon(Core.getInstance().getOperationById(uid));
	}
	
	/**
	 * NOTE: added by paulo maia
	 * Returns the custom big icon of an operation, if any
	 * @param operation The operation
	 * @return The icon, null if it was not established any
	 */
	public ImageIcon getOperationBigIcon(OperationDefinition<?> operation) {
		if (operation == null) return null;
		else return this.operationBigIcons.get(operation);
	}
	
	/**
	 * NOTE: added by miguel reboiro
	 * Returns the custom big icon of an operation, if any
	 * @param uid The operation UID
	 * @return The icon, null if it was not established any
	 */
	public ImageIcon getOperationBigIcon(String uid) {
		return this.getOperationBigIcon(Core.getInstance().getOperationById(uid));
	}

	/**
	 * Returns the custom icon of a datatype, if any
	 * @param dataType The datatype
	 * @return The icon, null if it was not established any
	 */
	public ImageIcon getDataTypeIcon(Class<?> dataType) {
		if (this.dataTypeIcon.containsKey(dataType)) {
			return this.dataTypeIcon.get(dataType);
		} else {
			Class<?> currentClass = dataType;
			while ((currentClass = currentClass.getSuperclass()) != null) {
				for (Class<?> key : this.dataTypeIcon.keySet()) {
					if (key.equals(currentClass)) {
						return this.dataTypeIcon.get(key);
					}
				}
			}

			for (Class<?> key : this.dataTypeIcon.keySet()) {
				if (key.isAssignableFrom(dataType)) {
					return this.dataTypeIcon.get(key);
				}
			}
			return null;
		}
	}
	
	public List<IViewFactory> getDataTypeViews(Class<?> dataType) {
		List<IViewFactory> toret = new LinkedList<IViewFactory>();
		for (Class<?> key : this.dataTypeViews.keySet()) {
			if (key.isAssignableFrom(dataType)) {
				toret.addAll(dataTypeViews.get(key));
			}
		}
		return toret;		
	}

	
	/**
	 * Returns the custom class of an custom input GUI component
	 * @param operation The operation
	 * @return The custom class
	 */
	public Class<?> getInputGUIClass(OperationDefinition<?> operation) {
		if (operation == null) return null;
		else return this.operationGUI.get(operation);
	}
	
	/**
	 * Returns the custom class of an custom input GUI component
	 * @param uid The operation UID
	 * @return The custom class
	 */
	public Class<?> getInputGUIClass(String uid) {
		return this.operationGUI.get(Core.getInstance().getOperationById(uid));
	}

	public boolean isOperationViewableIn(String uid, String identifier) {
		return this.isOperationViewableIn(Core.getInstance().getOperationById(uid), identifier);
	}
	
	public boolean isOperationViewableIn(OperationDefinition<?> operation, String identifier) {
		if (operation == null) return false;

		//look in configuration (.conf files)
		String visibility = Workbench.CONFIG.getProperty(operation.getID()+".visibility");
		
		//look in configuration from its plugin (plugin.xml)
		if (visibility == null && this.operationVisibility.get(operation)!=null) {
			visibility = this.operationVisibility.get(operation).toString();
		}
		
		
		if (visibility != null) {
			
			//NONE means that it is hidden everywhere
			if (visibility.toUpperCase().indexOf("NONE")!=-1) return false;
			
			if (visibility.toUpperCase().indexOf(identifier)!=-1) {
				return true;
			}else{
				return false;
			}
		}else{			
			//by default, it is visible
			return true;
		}
		
	}
	
	

	/**
	 * Executes an operation, but this method returns all the clipboard items generated, blocking the calling thread until
	 * the results are generated. If the operation generates some error, null is returned.
	 * 
	 * @param operation The operation to execute

	 * @return The clipboardItems generated as result of the operation's execution
	 */
	public List<ClipboardItem> executeOperationAndWait(final OperationDefinition<?> operation) {
		
		final Object pleaseWait = new Object(); //synchronization
		
		
		class MyParamsReceiver implements ParamsReceiver, ProgressHandler{
			
			InputGUI gui;
			
			private List<ClipboardItem> toRemove;
			
			private void doRemove() {
				if (toRemove != null) {
					for (int i = toRemove.size()-1; i>=0; i--) {
						Core.getInstance().getClipboard().removeClipboardItem(toRemove.get(i));
					}
				}		
			}
			
			public MyParamsReceiver(InputGUI gui) {
				this.gui = gui;
				
			}

			public void paramsIntroduced(ParamSpec[] params) {
				Core.getInstance().executeOperation(operation,  this  , params);
			}

			private boolean finished = false;
			private List<ClipboardItem> clipboardItems;
			
			Object operationKey;
			public void operationStart(final Object progressBean, final Object operationKey) {

				this.operationKey = operationKey;
				if (!SwingUtilities.isEventDispatchThread()) {
				
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							operationStart(progressBean, operationKey);
						}
					});
				}else{
					gui.finish();
					MonitorizeDialog monitorDialog = new MonitorizeDialog(Workbench.this.mainWindow,progressBean, operationKey, operation);
					monitorDialog.setModal(true);
					monitors.put(operationKey, monitorDialog);
					monitorDialog.setVisible(true);
				}
			}
					
		
			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#validationError(java.lang.Throwable)
			 */
			public void validationError(Throwable t) {
				gui.onValidationError(t);
				t.printStackTrace();

			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#operationFinished(java.util.List, java.util.List)
			 */
			public void operationFinished(final List<Object> results, final List<ClipboardItem> clipboardItems) {
				if (!SwingUtilities.isEventDispatchThread()) {
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							operationFinished(results, clipboardItems);
						}
					});
				}else{
					if (monitors.get(this.operationKey)!=null) {
					
						monitors.get(operationKey).setFinish(true);
						monitors.get(operationKey).setVisible(false);
						monitors.get(operationKey).dispose();
						monitors.remove(this.operationKey);
					}else{

					}
				}
				this.clipboardItems = clipboardItems;
				this.finished = true;
				
				synchronized(pleaseWait) {
					pleaseWait.notifyAll();
				}
				doRemove();
			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#operationError(java.lang.Throwable)
			 */
			public void operationError(Throwable t) {
				finished = true;
				synchronized(pleaseWait) {
					pleaseWait.notifyAll();
				}
				doRemove();
			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.workbench.ParamsReceiver#cancel()
			 */
			public void cancel() {
				gui.finish();
				
				this.finished = true;
				synchronized(pleaseWait) {
					pleaseWait.notifyAll();
				}
				doRemove();
				
			}

			public void removeAfterTermination(List<ClipboardItem> items) {
				this.toRemove = items;
			}

		}
		final InputGUI win = getInputGUI(operation);
		MyParamsReceiver callback = new MyParamsReceiver(win);
		win.init(callback, operation);
		synchronized(pleaseWait) {
			if (!callback.finished) {
				try {
					pleaseWait.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return callback.clipboardItems;
	}
	
	/**
	 * Executes an operation. Requests the core to execute an operation, and monitors its progress. 
	 * It also monitorizes the operation progress.
	 * @param operation 
	 * @param params The parameters to the operation
	 */
	public void executeOperation(final OperationDefinition<?> operation, ParamSpec[] params) {
		this.executeOperation(operation, null, params);
	}
	
	/**
	 * @param uid Operation id
	 * @param userHandler A Progress handler
	 * @param parameters A parameters given their real values. ParamSpec[] will be inferred
	 */
	public void executeOperation(String uid, ProgressHandler userHandler, List<?> parameters) {
		executeOperation(Core.getInstance().getOperationById(uid), userHandler, CoreUtils.createParams(parameters));
	}
	/**
	 * Executes an operation. Requests the core to execute an operation, and monitors its progress. 
	 * It also monitorizes the operation progress.
	 * @param operation
	 * @param userHandler A Progress handler
	 * @param params The parameters to the operation
	 */
	public void executeOperation(final OperationDefinition<?> operation, final ProgressHandler userHandler, ParamSpec[] params) {
		class MyProgressHandler implements ProgressHandler {

			private List<ClipboardItem> toRemove;
			
			
			private void doRemove() {
				if (toRemove != null) {
					//clone list, because the remove list is changed during removeClipboardItem method
					List<ClipboardItem> list = new ArrayList<ClipboardItem>();  
					java.util.Collections.copy(toRemove, list);
					for (int i = list.size()-1; i>=0; i--) {
						Core.getInstance().getClipboard().removeClipboardItem(list.get(i));
					}
				}		
			}
		
			Object operationKey;
			public void operationStart(final Object progressBean, final Object operationKey) {

				this.operationKey = operationKey;
				if (!SwingUtilities.isEventDispatchThread()) {
				
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							operationStart(progressBean, operationKey);
						}
					});
				}else{
					
					MonitorizeDialog monitorDialog = new MonitorizeDialog(Workbench.this.mainWindow,progressBean, operationKey, operation);
					monitors.put(operationKey, monitorDialog);
					monitorDialog.setVisible(true);
				}
				if (userHandler!=null) {
					userHandler.operationStart(progressBean, operationKey);
				}
			}
					
			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#validationError(java.lang.Throwable)
			 */
			public void validationError(Throwable t) {
				t.printStackTrace();
				if (userHandler!=null) {
					userHandler.validationError(t);
				}
			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#operationFinished(java.util.List, java.util.List)
			 */
			public void operationFinished(final List<Object> results, final List<ClipboardItem> clipboardItems) {
				if (!SwingUtilities.isEventDispatchThread()) {
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							operationFinished(results, clipboardItems);
						}
					});
				}else{
					if (monitors.get(this.operationKey)!=null) {
					
						monitors.get(operationKey).setFinish(true);
						monitors.get(operationKey).setVisible(false);
						monitors.get(operationKey).dispose();
						monitors.remove(this.operationKey);
						doRemove();
					}else{

					}
				}
				if (userHandler!=null) {
					userHandler.operationFinished(results, clipboardItems);
				}
				
			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#operationError(java.lang.Throwable)
			 */
			public void operationError(Throwable t) {
				doRemove();
				if (userHandler!=null) {
					userHandler.operationError(t);
				}
			}
		}
		
		MyProgressHandler handler = new MyProgressHandler();
		Core.getInstance().executeOperation(operation,  handler, params);
	}
	/**
	 * Executes an operation. First retrieves the params from the user and then requests the core to execute it
	 * It also monitorizes the operation progress.
	 * @param operation 
	 */
	public void executeOperation(final OperationDefinition<?> operation) {
		this.executeOperation(operation, (ProgressHandler)null);
		
	}
	/**
	 * Executes an operation. First retrieves the params from the user and then requests the core to execute it
	 * It also monitorizes the operation progress.
	 * @param operation
	 * @param userHandler 
	 */
	public void executeOperation(final OperationDefinition<?> operation, final ProgressHandler userHandler) {
		
		class MyParamsReceiver implements ParamsReceiver, ProgressHandler{

			private List<ClipboardItem> toRemove;
			
			
			private void doRemove() {
				if (toRemove != null) {
					//clone list, because the remove list is changed during removeClipboardItem method
					List<ClipboardItem> list = new ArrayList<ClipboardItem>();  
					java.util.Collections.copy(toRemove, list);
					for (int i = list.size()-1; i>=0; i--) {
						Core.getInstance().getClipboard().removeClipboardItem(list.get(i));
					}
				}		
			}
				
			
			InputGUI gui;
			public MyParamsReceiver(InputGUI gui) {
				this.gui = gui;
			}

			public void paramsIntroduced(ParamSpec[] params) {
				Core.getInstance().executeOperation(operation,  this  , params);
			}

			Object operationKey;
			public void operationStart(final Object progressBean, final Object operationKey) {

				this.operationKey = operationKey;
				if (!SwingUtilities.isEventDispatchThread()) {
				
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							operationStart(progressBean, operationKey);
						}
					});
				}else{
					gui.finish();
					MonitorizeDialog monitorDialog = new MonitorizeDialog(Workbench.this.mainWindow,progressBean, operationKey, operation);
					monitors.put(operationKey, monitorDialog);
					monitorDialog.setVisible(true);
				}
				
				if (userHandler!=null) {
					userHandler.operationStart(progressBean, operationKey);
				}
			}
					
		
			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#validationError(java.lang.Throwable)
			 */
			public void validationError(Throwable t) {
				gui.onValidationError(t);
				t.printStackTrace();
				if (userHandler!=null) {
					userHandler.validationError(t);
				}

			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#operationFinished(java.util.List, java.util.List)
			 */
			public void operationFinished(final List<Object> results, final List<ClipboardItem> clipboardItems) {
				if (!SwingUtilities.isEventDispatchThread()) {
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							operationFinished(results, clipboardItems);
						}
					});
				}else{
					if (monitors.get(this.operationKey)!=null) {
					
						monitors.get(operationKey).setFinish(true);
						monitors.get(operationKey).setVisible(false);
						monitors.get(operationKey).dispose();
						monitors.remove(this.operationKey);
						doRemove();
					}else{

					}
				}
				if (userHandler!=null) {
					userHandler.operationFinished(results, clipboardItems);
				}
				
			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.core.ProgressHandler#operationError(java.lang.Throwable)
			 */
			public void operationError(Throwable t) {
				doRemove();
				if (userHandler!=null) {
					userHandler.operationError(t);
				}
			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.workbench.ParamsReceiver#cancel()
			 */
			public void cancel() {
				gui.finish();
				doRemove();
				
			}

			/* (non-Javadoc)
			 * @see es.uvigo.ei.aibench.workbench.ParamsReceiver#removeAfterTermination(java.util.List)
			 */
			public void removeAfterTermination(List<ClipboardItem> items) {
				this.toRemove = items;
				
			}
		}
		
		final InputGUI win = getInputGUI(operation);
		MyParamsReceiver callback = new MyParamsReceiver(win);
		win.init(callback, operation);
	}

	/**
	 * Returns a list of Operation Wrappers (also Swing Actions) of each Operation connected to the core
	 * @return the operations wrappers
	 */
	public List<OperationWrapper> getInterceptedOperations() {
		return this.interceptedOperations;
	}
	
	/**
	 * Put a custom component in a slot
	 * @param slotID
	 * @param componentName
	 * @param componentID
	 * @param component
	 */
	public void putItemInSlot(final String slotID, final String componentName, final String componentID, final JComponent component) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Workbench.this.putItemInSlot(slotID, componentName, componentID, component);
				}
			});
		}else{
			if (this.mainWindow == null) return;
			this.mainWindow.putItemInSlot(slotID, componentName, componentID, component);
			this.fireComponentAdded(slotID, componentName, componentID, component);
		}
	}
	
	/**
	 * Returns all the available slot IDs
	 * @return The available slot IDs
	 */
	public List<String> getAvailableSlotIDs() {
		return this.mainWindow.getAvailableSlotIDs();
	}
	
	// temporal variable used in removeComponentFromSlot
	private JComponent toret;


	/**
	 * 
	 */
	static final String AIBENCH_HELP_PROPERTY = "AIBench.help";	
	/**
	 * Removes some item from its slot
	 * @param componentID The ID of the component
	 * @return
	 */
	public JComponent removeComponentFromSlot(final String componentID) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						toret= Workbench.this.removeComponentFromSlot(componentID);
					}
				});
				return toret;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JComponent removed = this.mainWindow.removeComponentFromSlot(componentID);
		this.fireComponentRemoved(componentID, removed);
		return removed;
	}

	/**
	 * Returns the status text
	 * @return The status text
	 */
	public String getStatusText() {
		return this.mainWindow.getStatusBar().getText();
	}
	
	/*
	 * =========================================================================================
	 * IGenericGUI implementation
	 * =========================================================================================
	 */
	public void init() {
		createViewFactories();
		createWelcomeScreen();
		createOperationWrappers();
		createIconMappings();
		createInputGUIMappings();
		createOperationVisibility();
		if(System.getProperty("aibench.nogui")==null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					createMainWindow();
					putCoreComponents();
					createComponents();
					Workbench.this.mainWindow.pack();
					
					Workbench.this.mainWindow.setLocationRelativeTo(null);
				}
			});
//			this.mainWindow.packSplitters();
		}
//		this.mainWindow.setVisible(true);
	}


	
	public void update() {
		List<OperationWrapper> temp = this.interceptedOperations;
		for (OperationWrapper wrapper : temp) {
			wrapper.updateState();
		}
	}

	public void info(String info) {
		logger.info(info);
		if(System.getProperty("aibench.nogui")!=null) return;
		JOptionPane.showMessageDialog(null, info, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	public void warn(String info) {
		logger.warn(info);
		if(System.getProperty("aibench.nogui")!=null)return;
		JOptionPane.showMessageDialog(null, info, "Warning", JOptionPane.WARNING_MESSAGE);

	}

	public void error(final String message) {
		if (logger.getEffectiveLevel().equals(Level.DEBUG))
			logger.debug("Error message received: " + message);
		
		logger.error(message);
		if (System.getProperty("aibench.nogui") != null)
			return;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
	
	public void error(final Throwable exception) {
		logger.error(exception.toString());
		JDialog errorDialog = new ErrorDialog(this.mainWindow, exception);
		errorDialog.setVisible(true);
	}
	
	public void error(final Throwable exception, final String message) {
		logger.error(message, exception);
		
		JDialog errorDialog = new ErrorDialog(this.mainWindow, exception, message);
		errorDialog.setVisible(true);
	}

	public void setStatusText(final String text) {
		if(System.getProperty("aibench.nogui")!=null) return;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainWindow.getStatusBar().setText(text);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementAdded(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
	 */
	public void elementAdded(ClipboardItem item) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardListener#elementRemoved(es.uvigo.ei.aibench.core.clipboard.ClipboardItem)
	 */
	public void elementRemoved(ClipboardItem item) {
		if (openedItems.contains(item)) {
			this.hideData(item);
		}
		this.openedItems.remove(item);
		this.closedItems.add(item);
		
	}
	
	
	/*===============================
	 * WORKBENCH LISTENERS MANAGEMENT
	 =================================*/
	public void addWorkbenchListener(WorkbenchListener listener) {
		this.workbenchListeners.add(listener);
	}
	
	/*==============================
	 * PRIVATE METHODS
	 ===============================*/
	
	private List<IViewFactory> searchView(Object data) {
		if (data == null) {
			return null;
		}
		return this.getDataTypeViews(data.getClass());
	}
	
	private void fireDataShowed(ClipboardItem data) {
		for (WorkbenchListener listener: this.workbenchListeners) {
			listener.dataShowed(data);
		}
	}
	
	private void fireDataHided(ClipboardItem data) {
		for (WorkbenchListener listener: this.workbenchListeners) {
			listener.dataHidded(data);
		}
	}
	
	private void fireDataClosed(ClipboardItem data) {
		for (WorkbenchListener listener: this.workbenchListeners) {
			listener.dataClosed(data);
		}
	}
	private void fireComponentAdded(String slotID, String componentName,  String componentID, JComponent component) {
		for (WorkbenchListener listener: this.workbenchListeners) {
			listener.componentAdded(slotID, componentName, componentID, component);
		}
	}
	
	private void fireComponentRemoved(String slotID, JComponent component) {
		for (WorkbenchListener listener: this.workbenchListeners) {
			listener.componentRemoved(slotID, component);
		}
	}
	
	public void showViewHelp(JComponent view) {
		Object value = view.getClientProperty(Workbench.AIBENCH_HELP_PROPERTY);
		String help = null;
		if (value instanceof String) help = (String) value;
		this.showHelpTopic(help, "There's no help available for the view.");
	}
	
	public void showDatatypeHelp(Datatype datatype) {
		this.showHelpTopic(datatype.help(), "There's no help available for the datatype.");
	}
	
	public void showOperationHelp(OperationDefinition<?> operation) {
		this.showHelpTopic(operation.getHelp(), "There's no help available for the operation.");
	}
	
	public void showHelpTopic(String help) {
		this.showHelpTopic(help, "No help available.");
	}
	
	public void showHelpTopic(String help, String errorMessage) {
		if (help == null || help.trim().equals("")) {
			this.showHelp(errorMessage);
		} else {
			if (CoreUtils.isValidURL(help)) {
				try {
					CoreUtils.openURL(help);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(
						Workbench.this.mainWindow, 
						errorMessage, 
						"Help Unavailable", 
						JOptionPane.ERROR_MESSAGE
					);					
				}
			} else {
				final HelpBroker helpBroker = Core.getInstance().getHelpBroker();
				if (helpBroker == null) {
					JOptionPane.showMessageDialog(
						Workbench.this.mainWindow, 
						errorMessage, 
						"Help Unavailable", 
						JOptionPane.ERROR_MESSAGE
					);
				} else {
					helpBroker.setCurrentID(help);
					helpBroker.setDisplayed(true);
				}
			}
		}
	}
	
	public void showHelp() {
		this.showHelp("No help available.");
	}
	
	public void showHelp(String errorMessage) {
		String homeURL = Core.CONFIG.getProperty("help.homeurl");
		if (homeURL == null || homeURL.trim().equals("")) {
			final HelpBroker helpBroker = Core.getInstance().getHelpBroker();
			if (helpBroker == null) {
				JOptionPane.showMessageDialog(
						Workbench.this.mainWindow, 
						errorMessage, 
						"Help Unavailable", 
						JOptionPane.ERROR_MESSAGE
				);
			} else {
				helpBroker.setDisplayed(true);
			}
		} else {
			try {
				CoreUtils.openURL(homeURL);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(
						Workbench.this.mainWindow, 
						errorMessage, 
						"Help Unavailable", 
						JOptionPane.ERROR_MESSAGE
				);
			}
		}
	}

	private void createMainWindow() {
		this.mainWindow = new MainWindow(interceptedOperations);
		
		if (this.welcomeScreen != null) {
			this.mainWindow.getDocumentTabbedPane().addTab(this.welcomeScreenTitle, this.welcomeScreen);
		}
		
		if (Boolean.parseBoolean(Core.CONFIG.getProperty("help.enabled", "false"))) {
			Container container = this.mainWindow.getContentPane();
			if (container instanceof JComponent) {
				final JComponent containerComponent = (JComponent) container;
				
				containerComponent.setFocusable(true);
				
				containerComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
						KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), KeyEvent.VK_F1);
				containerComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
						KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0), KeyEvent.VK_HELP);
				
				final AbstractAction actionHelp = new AbstractAction() {
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e) {
						final Object value = containerComponent.getClientProperty(Workbench.AIBENCH_HELP_PROPERTY);
						final String errorMessage = "There's no help available for the current view.";
//						if (value != null && value instanceof String) {
						Workbench.this.showHelpTopic((String) value, errorMessage);
//							final String help = (String) value;
//							if (CoreUtils.isValidURL(help)) {
//								CoreUtils.openURL(help);
//							} else {
//								final HelpBroker helpBroker = Core.getInstance().getHelpBroker();
//								if (helpBroker == null) {
//									JOptionPane.showMessageDialog(
//										Workbench.this.mainWindow, 
//										errorMessage, 
//										"Help Unavailable", 
//										JOptionPane.ERROR_MESSAGE
//									);
//								} else {
//									helpBroker.setCurrentID(help);
//									helpBroker.setDisplayed(true);
//								}
//							}
//						} else {
//							JOptionPane.showMessageDialog(
//								Workbench.this.mainWindow, 
//								errorMessage, 
//								"Help Unavailable", 
//								JOptionPane.ERROR_MESSAGE
//							);
//						}
					}
				};
				
				containerComponent.getActionMap().put(KeyEvent.VK_F1, actionHelp);
				containerComponent.getActionMap().put(KeyEvent.VK_HELP, actionHelp);
//				
//				if (CoreUtils.isValidURL(this.help)) {
//					actionHelp = new AbstractAction() {
//						private static final long serialVersionUID = 1L;
//						
//						public void actionPerformed(ActionEvent e) {
//							CoreUtils.openURL(help);
//						}
//					};
//				} else {
//					final HelpBroker helpBroker = Core.getInstance().getHelpBroker();
//					if (helpBroker == null) {
//						actionHelp = null;
//					} else {
//						actionHelp = new AbstractAction() {
//							private static final long serialVersionUID = 1L;
//							
//							public void actionPerformed(ActionEvent e) {
//								helpBroker.setCurrentID(help);
//								helpBroker.setDisplayed(true);
//							}
//						};
//					}
//				}
			}
		}
	}

	private void createOperationWrappers() {
		//Get the core operations creating a operation wrapper
		List<OperationDefinition<?>> operations = Core.getInstance().getOperations();
		for (OperationDefinition<?> operation : operations ) {
				OperationWrapper newWrapper = new OperationWrapper(operation);
				
				this.interceptedOperations.add(newWrapper);
				this.operationDefinition2Wrapper.put(operation, newWrapper);
		}
	}
	
	private void createIconMappings() {
		Plugin plugin = PluginEngine.getPlugin(this.getClass());
		ExtensionPoint point = plugin.getExtensionPoint("aibench.workbench.view");
		List<?> extensions = point.getExtensions();
		Iterator<?> it = extensions.iterator();
		while (it.hasNext()) {
			Extension extension = (Extension) it.next();
			List<?> children = extension.getExtensionXmlNode().getChildren();
			for (int i = 0; i < children.size(); i++) {
				PluginXmlNode node = (PluginXmlNode) children.get(i);
				if (node.getName().equals("icon-operation")) {
					String operationID = node.getAttribute("operation");

					ImageIcon icon = new ImageIcon(extension.getPlugin().getPluginClassLoader().getResource(node.getAttribute("icon")));
					//Search for the operation
					for (OperationWrapper wrapper : this.interceptedOperations) {
						if (wrapper.getOperationDefinition().getID().equals(operationID)) {

							this.operationIcons.put(wrapper.getOperationDefinition(),icon);
						}
					}
				}
				//NOTE: added by paulo maia
				if (node.getName().equals("big-icon-operation")) {
					String operationID = node.getAttribute("operation");

					ImageIcon icon = new ImageIcon(extension.getPlugin().getPluginClassLoader().getResource(node.getAttribute("icon")));					
					//Search for the operation
					for (OperationWrapper wrapper : this.interceptedOperations) {
						if (wrapper.getOperationDefinition().getID().equals(operationID)) {

							this.operationBigIcons.put(wrapper.getOperationDefinition(),icon);							
						}
					}
				}
				//end added by paulo maia
				if (node.getName().equals("icon-datatype")) {
					String dataType= node.getAttribute("datatype");
					ImageIcon icon = new ImageIcon(extension.getPlugin().getPluginClassLoader().getResource(node.getAttribute("icon")));

					try {
						this.dataTypeIcon.put(extension.getPlugin().getPluginClassLoader().loadClass(dataType),icon);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void createOperationVisibility() {
		Plugin plugin = PluginEngine.getPlugin(this.getClass());
		ExtensionPoint point = plugin.getExtensionPoint("aibench.workbench.view");
		List<?> extensions = point.getExtensions();
		Iterator<?> it = extensions.iterator();
		while (it.hasNext()) {
			Extension extension = (Extension) it.next();
			List<?> children = extension.getExtensionXmlNode().getChildren();
			for (int i = 0; i < children.size(); i++) {
				PluginXmlNode node = (PluginXmlNode) children.get(i);
				if (node.getName().equals("operation-visibility")) {
					String operationID = node.getAttribute("operation");

					String visibility = node.getAttribute("visibility");
					//Search for the operation
					for (OperationWrapper wrapper : this.interceptedOperations) {
						if (wrapper.getOperationDefinition().getID().equals(operationID)) {
							Vector<String> visibilityV = new Vector<String>();
							StringTokenizer tk = new StringTokenizer(visibility);
							while (tk.hasMoreTokens()) {
								visibilityV.addElement(tk.nextToken());
							}
							this.operationVisibility.put(wrapper.getOperationDefinition(),visibilityV);
						}
					}
				}
			}
		}
		
	}
	
	private void createComponents() {
		Plugin plugin = PluginEngine.getPlugin(this.getClass());
		ExtensionPoint point = plugin.getExtensionPoint("aibench.workbench.view");
		List<?> extensions = point.getExtensions();
		Iterator<?> it = extensions.iterator();
		while (it.hasNext()) {
			Extension extension = (Extension) it.next();
			List<?> children = extension.getExtensionXmlNode().getChildren();
			for (int i = 0; i < children.size(); i++) {
				PluginXmlNode node = (PluginXmlNode) children.get(i);
				if (node.getName().equals("component")) {
					String slotID = node.getAttribute("slotid");
					String className = node.getAttribute("class");
					String name = node.getAttribute("name");
					String componentID = node.getAttribute("componentID");
					String staticMethod = node.getAttribute("singletonMethod");
					
					// Instantiation
					
					try {
						Class<?> c = extension.getPlugin().getPluginClassLoader().loadClass(className);
						
						JComponent component=null;
						if(staticMethod == null) {
							component = (JComponent) c.newInstance();
						}else{
							Method m = c.getMethod(staticMethod, new Class[]{});
							component = (JComponent) m.invoke(null);
						}
						
						putItemInSlot(slotID, name, componentID, component);
					} catch (InstantiationException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						
						e.printStackTrace();
						
					}catch(NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	private void putCoreComponents() {
		// Session Tree
		String sessionTreeVisible = CONFIG.getProperty("sessiontree.visible");
		if (sessionTreeVisible == null || !sessionTreeVisible.equals("false")) {
			JScrollPane sessionTreeSP = new JScrollPane(this.getTreeManager().getAIBenchTree());
			sessionTreeSP.setPreferredSize(new Dimension(200, 200));
			
			String sessionTreeSlot = CONFIG.getProperty("sessiontree.slot");
			
			String sessionTreeName = "Session";
			if (CONFIG.getProperty("sessiontree.name")!=null){
				sessionTreeName = CONFIG.getProperty("sessiontree.name");
			}
			
			if (sessionTreeSlot == null) sessionTreeSlot = "left";			
			putItemInSlot(sessionTreeSlot, sessionTreeName, "aibench.sessiontree", sessionTreeSP);
		}
		
		// Clipboard Tree
		
		String clipboardTreeVisible = CONFIG.getProperty("clipboardtree.visible");
		
		if (clipboardTreeVisible == null || !clipboardTreeVisible.equals("false")) {
			JScrollPane clipboardTreeSP = new JScrollPane(this.getTreeManager().getAIBenchClipboardTree());
			clipboardTreeSP.setPreferredSize(new Dimension(200, 200));
			String clipboardTreeSlot = CONFIG.getProperty("clipboardtree.slot");
			String clipboardTreeName = "Clipboard";
			if (CONFIG.getProperty("clipboardtree.name")!=null){
				clipboardTreeName = CONFIG.getProperty("clipboardtree.name");
			}
			if (clipboardTreeSlot == null) clipboardTreeSlot = "right";			
			putItemInSlot(clipboardTreeSlot, clipboardTreeName, "aibench.clipboardtree", clipboardTreeSP);
		}
		
		// Log Area
		String logAreaVisible = CONFIG.getProperty("logarea.visible");
		if (logAreaVisible == null || !logAreaVisible.equals("false")) {
			String logAreaSlot = CONFIG.getProperty("logarea.slot");
			if (logAreaSlot == null) logAreaSlot = "bottom";			
			putItemInSlot(logAreaSlot, "Log", "aibench.log", createLogTextArea());
			
			if (CONFIG.getProperty("logarea.maxsize")!=null) {
				try{
					int maxSize = Integer.parseInt(CONFIG.getProperty("logarea.maxsize"));
					TextAreaAppender.MAXSIZE = maxSize;
				}catch(NumberFormatException e) {
					logger.warn("logarea.maxsize should be integer");
				}
			}
		}
		
		// Memory Monitor
		String memoryVisible = CONFIG.getProperty("memorymonitor.visible");
		if (memoryVisible == null || !memoryVisible.equals("false")) {
			String memorySlot = CONFIG.getProperty("memorymonitor.slot");
			if (memorySlot == null) memorySlot = "bottom";			
			putItemInSlot(memorySlot, "Memory", "aibench.memory", new MemoryMonitor());		
		}
	}
	private JComponent createLogTextArea() {
		JComponent logArea = TextAreaAppender.getGUIComponent();
		logArea.setPreferredSize(new Dimension(200, 100));
		return logArea;
	}
	
	private void createInputGUIMappings() {
		final Plugin plugin = PluginEngine.getPlugin(this.getClass());
		final ExtensionPoint point = plugin.getExtensionPoint("aibench.workbench.view");

		for (Extension extension:point.getExtensions()) {
			for (PluginXmlNode node:extension.getExtensionXmlNode().getChildren()) {
				if (node.getName().equals("gui-operation")) {
					final String operationID = node.getAttribute("operation");
					final String className = node.getAttribute("class");

					//Search for the operation
					for (OperationWrapper wrapper : this.interceptedOperations) {
						if (wrapper.getOperationDefinition().getID().equals(operationID)) {
							try {
								this.operationGUI.put(
									wrapper.getOperationDefinition(), 
									extension.getPlugin().getPluginClassLoader().loadClass(className)
								);
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
	}
	
	private void createWelcomeScreen() {
		final Plugin plugin = PluginEngine.getPlugin(this.getClass());
		final ExtensionPoint point = plugin.getExtensionPoint("aibench.workbench.view");

		for (Extension extension : point.getExtensions()) {
			for (PluginXmlNode node : extension.getExtensionXmlNode().getChildren()) {
				if (node.getName().equals("welcomescreen")) {
					String className = node.getAttribute("class");
					if (className == null) {
						continue;
					}
					
					String title = node.getAttribute("title");
					if (title == null) {
						title = "Welcome screen";
					}

					try {
						final Class<?> extensionClass = extension.getPlugin().getPluginClassLoader().loadClass(className);
						if (JComponent.class.isAssignableFrom(extensionClass)) {
							try {
								this.welcomeScreen = (JComponent) extensionClass.newInstance();
								this.welcomeScreenTitle = title;
							} catch (InstantiationException | IllegalAccessException e) {
								logger.warn(extensionClass + " can't be instantiated, ignoring extensiong point");
								continue;
							}
						} else {
							logger.warn(extensionClass + " is not a JComponent, ignoring extensiong point");
							continue;
						}
					} catch (ClassNotFoundException e) {
						logger.warn(e.getMessage() + " ignoring extensiong point");
					}
					
					/**
					 * Just take the first welcomescreen in each plugin.xml
					 */
					break;
				}
			}
		}
	}
	
	private void createViewFactories() {
		final Plugin plugin = PluginEngine.getPlugin(this.getClass());
		final ExtensionPoint point = plugin.getExtensionPoint("aibench.workbench.view");

		for (Extension extension : point.getExtensions()) {
			for (PluginXmlNode node : extension.getExtensionXmlNode().getChildren()) {
				if (node.getName().equals("view")) {
					String className = node.getAttribute("class");
					if (className == null) continue;
					
					try {
						final Class<?> extensionClass = extension.getPlugin().getPluginClassLoader().loadClass(className);
						final IViewFactory view = this.createViewFactory(extensionClass);

						view.setPluginName(extension.getPlugin().getName());
						view.setPluginUID(extension.getPlugin().getUID());
						
						view.setViewName(node.getAttribute("name"));
						view.setDataType(extension.getPlugin().getPluginClassLoader().loadClass(node.getAttribute("datatype")));
						
						final String helpAtt = node.getAttribute("help");
						if (helpAtt != null) {
							view.setHelp(helpAtt);
						}
						
						final String iconAtt = node.getAttribute("icon");
						if (iconAtt != null) {
							view.setViewIcon(new ImageIcon(extension.getPlugin().getPluginClassLoader().getResource(iconAtt)));
						}
						
						try {
							view.setViewPreferredPosition(Integer.parseInt(node.getAttribute("order")));
						} catch (NumberFormatException e) {
							// order is missing
							view.setViewPreferredPosition(0);
						}
						
						if (view.getDataType() != null) {
							final List<IViewFactory> availableViewsForType = this.dataTypeViews.get(view.getDataType());
							if (availableViewsForType == null) {
								this.dataTypeViews.put(view.getDataType(), new ArrayList<IViewFactory>());

							}
							
							this.dataTypeViews.get(view.getDataType()).add(view);
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	private IViewFactory createViewFactory(Class<?> viewExtensionInstance) {
		return new IViewFactoryImpl(viewExtensionInstance);
	}

	private InputGUI getInputGUI(OperationDefinition<?> operation) {
		final Class<?> c = this.operationGUI.get(operation);
		if (c != null) {
			try {
				return (InputGUI) c.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return new ParamsWindow();
	}
	private class IViewFactoryImpl extends AbstractViewFactory{

		private Class<?> extensionClass;
		public IViewFactoryImpl(Class<?> extensionClass) {
			if (JComponent.class.isAssignableFrom(extensionClass)) {
				this.extensionClass = extensionClass;
			} else {
				System.err.println(extensionClass + "is not a JComponent");
				System.exit(0);
			}
		}
		
		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.workbench.interfaces.IViewFactory#getComponent(java.lang.Object)
		 */
		public JComponent getComponent(Object data) {
			//invoke the property method

			Object extensionInstance=null;
			try {
				try {
					//Constructor c = extensionClass.getConstructor(new Class[]{data.getClass()});
					for (Constructor<?> c : extensionClass.getConstructors()) {
						if (c.getParameterTypes().length==1) {
							if (c.getParameterTypes()[0].isAssignableFrom(data.getClass())) {
								extensionInstance = c.newInstance(new Object[]{data});
							}
						}
					}
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}  catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (extensionInstance == null) extensionInstance = extensionClass.newInstance();
				for (Field field: extensionClass.getDeclaredFields()) {
					Data dataAnnotation = field.getAnnotation(Data.class);
					if (dataAnnotation != null) {
						try {
							BeanInfo info = Introspector.getBeanInfo(extensionInstance.getClass());
							for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
								if (desc.getName().equals(field.getName())) {
									Method setMethod = desc.getWriteMethod();
									setMethod.invoke(extensionInstance, data);
								}
							}
						} catch (IntrospectionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
				
				final JComponent componentExtensionIntance = (JComponent) extensionInstance;
				componentExtensionIntance.putClientProperty(Workbench.AIBENCH_HELP_PROPERTY, help);
				if (Boolean.parseBoolean(Core.CONFIG.getProperty("help.enabled", "false"))) {
					final ComponentAdapter adapter = new ComponentAdapter() {
						@Override
						public void componentShown(ComponentEvent e) {
							final Frame parentFrame = Utilities.getParentFrame(componentExtensionIntance);
							if (parentFrame instanceof JFrame) {
								Container container = ((JFrame) parentFrame).getContentPane();
								if (container instanceof JComponent) {
									final JComponent containerComponent = (JComponent) container;
									if (help != null && !help.trim().equals("")) {
										containerComponent.putClientProperty(Workbench.AIBENCH_HELP_PROPERTY, help);
									} else {
										containerComponent.putClientProperty(Workbench.AIBENCH_HELP_PROPERTY, null);
									}
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
				
				// return this
				return componentExtensionIntance;
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.workbench.interfaces.IViewFactory#getComponentClass()
		 */
		public Class<?> getComponentClass() {
			return this.extensionClass;
		}
	}
	
	/**
	 * Reads the config from <AIBench_directory>/conf/workbench.conf
	 */
	private static void readConfig() {
		try {
			String path = System.getProperty("aibench.paths.workbench.conf", "conf/workbench.conf");
			
			Workbench.CONFIG.load(Util.getGlobalResourceURL(path).openStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


