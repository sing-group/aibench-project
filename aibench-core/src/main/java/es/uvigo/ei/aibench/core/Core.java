/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part of the AIBench Project. 

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
 * Core.java
 * This file is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 29/09/2005
 */
package es.uvigo.ei.aibench.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;

import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.platonos.pluginengine.Extension;
import org.platonos.pluginengine.Plugin;
import org.platonos.pluginengine.PluginEngine;
import org.platonos.pluginengine.PluginXmlNode;

import es.uvigo.ei.aibench.Util;
import es.uvigo.ei.aibench.core.clipboard.Clipboard;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.Transformer;
import es.uvigo.ei.aibench.core.history.History;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.ExecutionSession;
import es.uvigo.ei.aibench.core.operation.execution.IncomingEndPoint;
import es.uvigo.ei.aibench.core.operation.execution.SynchronousResultCollector;

/**
 * The AiBench's Core plugins does:
 * <ol>
 *  <li> Registers operations</li>
 *  <li> Executes operations</li>
 *  <ol>
 *  
 *   	<li>Retrive user params.</li>
 *   	<li>Executes.</li>
 *   	<li>Collect the results and puts them in the clipboard.</li>
 *   	<li>Keeps a history of all the operations executed.</li>
 *   </ol>
 * </ol>
 * Uses the GUI for:
 * <ol>
 * <li>Retrieve user params.</li>
 * <li>Display operations results.</li>
 * </ol>
 *
 * @author Ruben Dominguez Carbajales, Daniel Glez-Peña
 */
/**
 * @author Daniel Glez-Peña
 *
 */
public class Core {
	private final static String HELP_HS_PATH = String.format("help%shelpset.hs", File.separator);
	
	static Logger logger = Logger.getLogger(Core.class.getName());

	/**
	 * Core configuration
	 */
	public static Properties CONFIG = new Properties();
	
	
	private static Core _instance = null;

	// Operations plugged
	private List<OperationDefinition<?>> operations = new ArrayList<OperationDefinition<?>>();

	//	Internal mappings
	private HashMap<OperationDefinition<?>, Object> operationInstances = new HashMap<OperationDefinition<?>, Object>();
	private HashMap<OperationDefinition<?>, Class<?>> operationAnnotatedClasses = new HashMap<OperationDefinition<?>, Class<?>>();
	
	// Enabled operations
	private HashSet<OperationDefinition<?>> enabledOperations = new HashSet<OperationDefinition<?>>();

	// Transformers
	private HashMap<Class<?>, List<Transformer>> transformersBySource = new HashMap<Class<?>, List<Transformer>>();
	private HashMap<Class<?>, List<Transformer>> transformersByDestiny = new HashMap<Class<?>, List<Transformer>>();
	private HashMap<String, Transformer> transformersBySignature = new HashMap<String, Transformer>();
	
	// OperationListeners
	private List<CoreListener> coreListeners = new LinkedList<CoreListener>();
	
	// The GUI
	private IGenericGUI gui = null;

	// The Clipboard
	private Clipboard clipboard = new Clipboard();

	// The History
	private History history = new History();
	
	// The pool
	private ExecutorService pool; 

	// Running counter
	private int runningCount = 0;
	
	// Help broker (JavaHelp)
	private HelpBroker helpBroker;
	
	// Multicore for web-workbench users
	private static HashMap<Integer, Core> _coreInstances = new HashMap<Integer, Core>();
	
	private Core() {
		Core.readConfig();
		createThreadPool();
		createOperations();
		createTransformers();
	}
	
	synchronized public static Core getInstance() {
		if (_instance == null) {
			_instance = new Core();
		}
		return _instance;
	}
	
	synchronized public static Core getInstance(Integer key) {
		Core i = _coreInstances.get(key);
		if (i == null) {
			i = new Core();
			_coreInstances.put(key, i);
		}
		return i;
	}
	
	synchronized public static Core removeInstance(Integer key) {
		return _coreInstances.remove(key);
	}
	
	
	public void addCoreListener(CoreListener listener){
		this.coreListeners.add(listener);
	}
	public void removeCoreListener(CoreListener listener){
		this.coreListeners.remove(listener);
	}
	
	private void createThreadPool(){
		int poolSize=Runtime.getRuntime().availableProcessors()*2;
		if (CONFIG.getProperty("threadpool.size")!=null){
			try{
			poolSize = Integer.parseInt(CONFIG.getProperty("threadpool.size"));
			}catch(NumberFormatException e){
				logger.warn("configuration parameter: threadpool.size is not an integer, using default value: "+poolSize);
			}
		}
		if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("Using a pool size of "+poolSize+" threads");
		this.pool = Executors.newFixedThreadPool(poolSize, new ThreadFactory(){
			public Thread newThread(Runnable arg0) {
			Thread t = new Thread(arg0);
			//	t.setPriority(Thread.MIN_PRIORITY);
				return t;
			}

		});
	}


	/**
	 * Returns the operations plugged
	 * @return
	 */
	public List<OperationDefinition<?>> getOperations(){
		return this.operations;
	}
	
	
	/**
	 * Returns an operation given its uid
	 * @param uid the operation uid
	 * @return
	 */
	public OperationDefinition<?> getOperationById(String uid) {
		OperationDefinition<?> op = null;
		for (OperationDefinition<?> desc : Core.getInstance().getOperations()) {
			if (desc.getID().equals(uid)) {
				op = desc;
				break;
			}
		}
		return op;
	}
	
	
	/**
	 * Returns an operation given its uid of some active session
	 * @param key user session identifier
	 * @param uid the operation uid
	 * @return
	 */
	public OperationDefinition<?> getOperationById(Integer key, String uid) {
		OperationDefinition<?> op = null;
		for (OperationDefinition<?> desc : Core.getInstance(key).getOperations()) {
			if (desc.getID().equals(uid)) {
				op = desc;
				break;
			}
		}
		return op;
	}
	
	/**
	 * Returns all compatible transformers with a given source. That is, all transformers whose source type is
	 * the given class or superclass.
	 * @param sourceType The source type
	 * @return The compatible transformers
	 */
	public List<Transformer> getTransformersBySource(Class<?> sourceType){
		List<Transformer> result = new ArrayList<Transformer>();
		for (Class<?> source : this.transformersBySource.keySet()){
			if (source.isAssignableFrom(sourceType)){
				result.addAll(this.transformersBySource.get(source));
			}
		}
		return result;
	}
	
	/**
	 * Returns all compatible transformers with a given destiny. That is, all transformers whose destiny type is
	 * the given class or sublcass.
	 * @param sourceType The destiny type
	 * @return The compatible transformers
	 */
	public List<Transformer> getTransformersByDestiny(Class<?> destinyType){
		List<Transformer> result = new ArrayList<Transformer>();
		for (Class<?> destiny : this.transformersByDestiny.keySet()){
			if (destinyType.isAssignableFrom(destiny)){
				result.addAll(this.transformersByDestiny.get(destiny));
			}
		}
		return result;
	}
	
	/**
	 * Returns all declared Transformers 
	 * @return
	 */
	public List<Transformer> getAllTransformers(){
		List<Transformer> list = new ArrayList<Transformer>();
		list.addAll(this.transformersBySignature.values());
		return list;
	}
	/**
	 * Returns a transformer given its signature
	 * @param signature
	 * @return
	 */
	public Transformer getTransformerBySignature(String signature){
		return this.transformersBySignature.get(signature);
	}

	/**
	 * Creates the operations reading the operations plugins and parsing the annotations of the
	 * indicated class in the plugin.xml
	 */
	private void createOperations(){
		Plugin plugin = PluginEngine.getPlugin(this.getClass());
		List<?> extensions = plugin.getExtensionPoint("aibench.core.operation-definition").getExtensions();
		for (Iterator<?> iter = extensions.iterator(); iter.hasNext();) {

			Extension extension = (Extension) iter.next();

			Class<?> annotatedClass =  extension.getExtensionClass();

			if (annotatedClass.getAnnotation(Operation.class)==null) continue;
			try{
				OperationDefinition<?> operationDefinition = OperationDefinition.createOperationDefinition(annotatedClass);
				
				
				operationDefinition.setPluginName(extension.getPlugin().getName());
				operationDefinition.setPluginID(extension.getPlugin().getUID());
				
				List<?> children = extension.getExtensionXmlNode().getChildren();
				for (int i = 0; i < children.size(); i++) {
					PluginXmlNode node = (PluginXmlNode) children.get(i);
					if (node.getName().equals("operation-description")) {
						
						//name
						String configName = CONFIG.getProperty(node.getAttribute("uid")+".name");
						if (configName != null){
							//the path was overrided in the core.conf
							operationDefinition.setName(configName);
						}else {
							String name = node.getAttribute("name");
							if (name != null){
								operationDefinition.setName(name);
							}
						}
						
						
						operationDefinition.setID(node.getAttribute("uid"));
						
						//path
						String configPath = CONFIG.getProperty(node.getAttribute("uid")+".path");
						if (configPath != null){
							//the path was overrided in the core.conf
							operationDefinition.setPath(configPath);
						}else {
							String path = node.getAttribute("path");
							if (path != null){
								operationDefinition.setPath(path);
							}
						}
						//help
						String helpNode = CONFIG.getProperty(node.getAttribute("uid")+".help");
						if (helpNode != null){
							operationDefinition.setHelp(helpNode);
						}else {
							String help = node.getAttribute("help");
							if (help != null){
								operationDefinition.setHelp(help);
							}
						}
						
						//shortcut NOTE: added by paulo maia
						String shortcut = node.getAttribute("shortcut");
						if(shortcut!=null){						
							operationDefinition.setShortcut(shortcut);
						}
					}
				}
				try {
					this.operationInstances.put(operationDefinition, annotatedClass.newInstance());
					this.operationAnnotatedClasses.put(operationDefinition, annotatedClass);
					this.operations.add(operationDefinition);
					if (operationDefinition.isEnabledByDefault()){
						this.enabledOperations.add(operationDefinition);
					}
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}catch(NoClassDefFoundError e){
				//The operation whill omited
				e.printStackTrace();
				continue;
			}
		}
	}

	
	/**
	 * Creates the transformers reading the operations plugins and parsing the annotations of the
	 * indicated class in the plugin.xml
	 */
	private void createTransformers(){
		Plugin plugin = PluginEngine.getPlugin(this.getClass());
		List<?> extensions = plugin.getExtensionPoint("aibench.core.transformer-definition").getExtensions();
		for (Iterator<?> iter = extensions.iterator(); iter.hasNext();) {

			Extension extension = (Extension) iter.next();

			Plugin extensionPlugin = extension.getPlugin();
			try{
				List<?> children = extension.getExtensionXmlNode().getChildren();
				for (int i = 0; i < children.size(); i++) {
					PluginXmlNode node = (PluginXmlNode) children.get(i);
					if (node.getName().equals("transformer-description")) {
						
						Class<?> transformerClass = extensionPlugin.getPluginClassLoader().loadClass(node.getAttribute("transformerClass"));
						
						Class<?> sourceType = null;
						Class<?>[] params=null;
						if (node.getAttribute("sourceType")!=null){
							sourceType = extensionPlugin.getPluginClassLoader().loadClass(node.getAttribute("sourceType"));
							params = new Class[]{sourceType};
						}
						
						Method method = transformerClass.getMethod(node.getAttribute("methodName"), params);
						
						//check 
						Class<?> destinyType = extensionPlugin.getPluginClassLoader().loadClass(node.getAttribute("destinyType"));
						if (!destinyType.isAssignableFrom(method.getReturnType())){
							logger.warn("Cant create transformer");
						}
						
						Transformer transformer = new Transformer(method);
						
						if (node.getAttribute("name")!=null){
							transformer.setName(node.getAttribute("name"));
						}else{
							transformer.setName(transformerClass.getName()+"::"+sourceType.getSimpleName()+"->"+destinyType.getSimpleName());
						}
						List<Transformer> tlist = this.transformersByDestiny.get(destinyType);
						if (tlist == null){
							tlist = new ArrayList<Transformer>();
							this.transformersByDestiny.put(destinyType, tlist);
						}
						tlist.add(transformer );
						
						if (sourceType != null){
							List<Transformer> tlist2 = this.transformersBySource.get(sourceType);
							if (tlist2 == null){
								tlist2 = new ArrayList<Transformer>();
								this.transformersBySource.put(sourceType, tlist2);
							}
							tlist2.add(transformer);
							
						}
						this.transformersBySignature.put(transformer.getSignature(), transformer);
					}
				}
			}catch(Exception e){
				logger.warn("Cant create transformed defined in plugin "+extensionPlugin+": "+e);
			}
		
		}
	}

	/**
	 * Gets the GUI plugged
	 * @return The GUI currently plugged
	 */
	public IGenericGUI getGUI() {
		if (gui == null) {
			Plugin plugin = PluginEngine.getPlugin(this.getClass());
			List<?> extensions = plugin.getExtensionPoint("aibench.core.gui").getExtensions();
			if (extensions.size() == 0){
				//return a default GUI
				gui = new IGenericGUI(){

					@Override
					public void init() {
						
						
					}

					@Override
					public void update() {
						
						
					}

					@Override
					public void info(String info) {
						System.out.println(info);
						
					}

					@Override
					public void warn(String warning) {
						System.err.println(warning);
						
					}

					@Override
					public void error(Throwable error) {
						System.err.println(error);
						error.printStackTrace();
						
					}

					@Override
					public void error(String error) {
						System.err.println(error);
						
					}

					@Override
					public void setStatusText(String text) {
						System.out.println(text);
						
					}
					
				};
			}else{
				Extension _extension = (Extension) extensions.get(0);			
				gui = (IGenericGUI) _extension.getExtensionInstance();
			}
			
		}

		return gui;
	}

	/**
	 * Gives access to the AIBench's Clipboard
	 * @return Returns the Clipboard
	 * @see es.uvigo.ei.aibench.core.clipboard.Clipboard
	 */
	public Clipboard getClipboard(){
		return this.clipboard;
	}
	
	/**
	 * Gives access to the AIBench's History
	 * @return Returns the History
	 * @see es.uvigo.ei.aibench.core.history.History
	 */
	public History getHistory() {
		return this.history;
	}
	public String getHelpPath(){
		String path ="";
		if (this.helpBroker ==  null &&
				Boolean.parseBoolean(Core.CONFIG.getProperty("help.enabled", "false"))) {
				path = Core.CONFIG.getProperty("help.path");
				if (path == null) path = Core.HELP_HS_PATH;
		}
		return path;
	}
	private synchronized void createHelpBroker() {
		if (this.helpBroker ==  null &&
			Boolean.parseBoolean(Core.CONFIG.getProperty("help.enabled", "false"))) {
			String path = Core.CONFIG.getProperty("help.path");
			if (path == null) path = Core.HELP_HS_PATH;
			
//            SwingHelpUtilities.setContentViewerUI("BasicNativeContentViewerUI");
			// Find the HelpSet file and create the HelpSet object:
			try {
				URL hsURL = new File(path).toURI().toURL();//HelpSet.findHelpSet(cl, path);
				this.helpBroker = new DefaultHelpBroker(new HelpSet(null, hsURL));
				
				Core.logger.info("HelpSet " + path + " configured");
			} catch (Exception ee) {
				// Say what the exception really is
				Core.logger.error("HelpSet " + path + " not found", ee);
				this.helpBroker = null;
			}
		}
	}
	
	/**
	 * Creates and configures the JavaHelp help broker. The property "help.enabled" on the "core.conf" file must be true.
	 * The default help set file location is "help/helpset.hs" and can be changed with the property "help.path".
	 * @return the JavaHelp help broker or <code>null</code> if the help is not enabled or it couldn't be configured.
	 */
	public HelpBroker getHelpBroker() {
		if (this.helpBroker == null) {
			this.createHelpBroker();
		}
		return this.helpBroker;
	}

	/**
	 * Validates the input data using the method defined in the
	 * {@link Port#validateMethod()} annotation (if used)
	 * 
	 * @param operationDefinition
	 *            The operation
	 * @param operationObject
	 *            The operation class instance
	 * @param specs
	 *            The params
	 * @throws Throwable
	 *             if the data couldn't be validated
	 */
	private void validate(OperationDefinition<?> operationDefinition, Object operationObject, ParamSpec[] specs) throws Throwable{
		ArrayList<Port> incomingPorts = new ArrayList<Port>();
		for (Port p: operationDefinition.getPorts()){
			if (p.direction()!=Direction.OUTPUT){
				incomingPorts.add(p);
			}
		}
		int i = 0;
		for (Port p : incomingPorts){

			if (!p.validateMethod().equals("")){
				ParamSpec spec = specs[i];
				// default validations
				if (spec.getType().isPrimitive() && spec.getValue().toString().equals("")){
					throw new IllegalArgumentException(spec.getName()+": Primitive params can't be null");
				}
				
				// validating method
				try {
					Method validateMethod = operationObject.getClass().getMethod(p.validateMethod(), new Class[]{spec.getType()});
					try{
						validateMethod.invoke(operationObject, spec.getRawValue());
					}catch(InvocationTargetException e){
						//the validate method doesn't validate the input
						throw e.getCause();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			i++;
		}
	}
	
	
	
	
	
	class OperationKey{
		Object operationInstance;
		OperationDefinition<?> definition;
		boolean cancelled = false;
	}
	
	/**
	 * Executes an AIBench Operation
	 * @param operation The operation
	 * @param handler A (optional) progress handler to monitorize the start and finish of the operation. May be null
	 * @param specs The params of the operation given in the form of {@link ParamSpec} object
	 * @see ParamSpec
	 * @see OperationDefinition
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> void executeOperation(final OperationDefinition<T> operation, final ProgressHandler handler, final ParamSpec[] specs){
		Thread working = new Thread("processThread"){
			public void run(){
				final ExecutionSession session;
				if(operations.indexOf(operation)==-1)throw new RuntimeException("Core: not a registered operation");
				
				Thread t=null;
				//ExecutionSession session=null;
				try {

					//**** CHANGE THE BEHAVIUR OF: ONLY ONE INSTANCE PER OPERATION OR ONE INSTANCE PER OPERATION ****
					//Object operationInstance = this.operationInstances.get(operation);
					Object operationInstance = operationAnnotatedClasses.get(operation).newInstance();
					// **********************************************************************************************
					
					
					/* Prepare the key */
					final OperationKey key = new OperationKey();
					key.operationInstance = operationInstance;
					key.definition = operation;
					
					

					// VALIDATING
					try{
						validate(operation, operationInstance, specs);
					}catch(Throwable e){
						handler.validationError(e);
						return;
					}

					// 1. Collect user params
					//final ParamSpec[] specs = getGUI().collectUserParams(operation, operationInstance);
					if (specs == null) return ; // the user cancelled the operation

					if (logger.getEffectiveLevel().equals(Level.DEBUG)){
						for (ParamSpec spec : specs){
							logger.debug(spec);
						}
					}
					
					
					Executable ex = operation.makeExecutable((T) operationInstance, pool);

					final SynchronousResultCollector collector = new SynchronousResultCollector();
					session = ex.openExecutionSession(collector);
					try {
						if (operation.getMonitorBeanMethod()!=null){
							Object monitorBean = operation.getMonitorBeanMethod().invoke(operationInstance, new Object[]{});
							operationInstance = null;
							if(handler!=null) handler.operationStart(monitorBean, key);
						}else {
							if(handler!=null) handler.operationStart(null, key);
						}


					// 2. Execute
					t = new Thread(){
						public void run(){		
							class ClipboardItemNeed implements Comparable<ClipboardItemNeed>{
								private ClipboardItem item;
								private char type; // 'r' or 'w'
								public ClipboardItemNeed(ClipboardItem item, char type){
									this.item = item;
									this.type = type;
								}
								
								@Override
								public int compareTo(ClipboardItemNeed other){
									return this.item.getID()-other.item.getID();
								}
								
								public boolean equals(Object o){
									ClipboardItemNeed other = (ClipboardItemNeed) o;
									return this.item == other.item;
								}
							}

							Vector<Lock> aquiredLocks = new Vector<Lock>();
							Vector<ClipboardItemNeed> allNeeded = new Vector<ClipboardItemNeed>();
							
							int i =0;
					
							// RETRIEVE PARAMS
							for (IncomingEndPoint port : session.getIncomingEndpoints()){
								Object value = specs[i].getRawValue();
								
								// PREPARE LOCKS NEEDED								
								if (((Port) operation.getPorts().get(i)).lock() && specs[i].getSource()==ParamSource.CLIPBOARD){
									ClipboardItem item = (ClipboardItem)specs[i].getValue();
									if (item!=null){
										ClipboardItemNeed need = new ClipboardItemNeed(item, 'w');
										if (allNeeded.indexOf(need)==-1){
											allNeeded.add(need);
										}else{
											ClipboardItemNeed need2 = allNeeded.get(allNeeded.indexOf(need));
											if (need2.type == 'r'){
												//replace with the more restrictive
												need2.type='w';
											}
										}
									}
								}else if (specs[i].getSource()==ParamSource.CLIPBOARD){
									ClipboardItem item = (ClipboardItem)specs[i].getValue();									
									if(item!=null){
										ClipboardItemNeed need = new ClipboardItemNeed(item, 'r');
										if (allNeeded.indexOf(need)==-1){
											allNeeded.add(need);			
										}
									}
								}
								// END PREPARE LOCK NEEDED
								
								
								if (port.getArgumentTypes().length==0){
									port.call();
								}else{
									
									/*// Transform
									Transformer trans = Core.this.transformersBySignature.get(specs[i].getTransformerSignature());
									if (trans!= null) value = trans.transform(value);
									*/
								}
								port.call(value);
								i++;
								
							}

							// AQUIRE LOCKS TO CLIPBOARD ITEMS
							// sort by ID to avoid deadlocks...
							Collections.sort(allNeeded);
							
							for (ClipboardItemNeed need : allNeeded ){
								if (need.type=='r'){
									Lock lk = need.item.getLock().readLock();
									if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("Thread "+Thread.currentThread()+" trying to get READ lock "+lk+" item "+need.item);
									
									lk.lock();
									aquiredLocks.add(lk);
									if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("Thread "+Thread.currentThread()+" aquired READ lock "+lk+" item "+need.item);
								}else{
									Lock lk = need.item.getLock().writeLock();
									if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("Thread "+Thread.currentThread()+" trying to get WRITE lock "+lk+" item "+need.item);
									lk.lock();
									aquiredLocks.add(lk);
									if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("Thread "+Thread.currentThread()+" aquired WRITE lock "+lk+" item "+need.item);
								}
							}
							// ALL LOCKS AQUIRED
						
							session.finish();
							
							
							List<Object> allResults=new Vector<Object>();
							List<ClipboardItem> clipboardItems = new ArrayList<ClipboardItem>();
							try{
								List<List<Object>> results =collector.getResults();
				
								if (!key.cancelled){
									// 3. Get Results
									List<Object> portOutputs = new ArrayList<Object>();
									
									if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("Getting outputs and sending them to clipboard");
									for (List<Object> list : results){
										for (Object elem : list){									
											portOutputs.add(elem);
											allResults.add(elem);
											
											//recursively populate the clipboard...
											
											clipboardItems.addAll(getClipboard().putItem(elem, null)); //this null could be the port names....
										}
									}
									if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("Creating a new History item");
									// Keep history...
//									if (clipboardItems == null) clipboardItems = new ArrayList<ClipboardItem>();
									Core.this.getHistory().putHistoryElement(specs, operation, clipboardItems, portOutputs );
									results = null;
									portOutputs = null;
								}
								
							}catch(Throwable e){
								e.printStackTrace();
								if(handler!=null){ 
									handler.operationError(e);
									handler.operationFinished(allResults, clipboardItems);
								
								}
								gui.error(e);

							}finally{
								
																
								for (Lock lk : aquiredLocks){
									if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("Thread "+Thread.currentThread()+" releasing lock: "+lk);
									lk.unlock();
								}
								
								if(handler!=null) handler.operationFinished(allResults, clipboardItems);
								
								allResults = null;
								
								decreaseRunningCount();
								
								if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("Operation finished");	
								
							}

						}
					};
					
					t.start();

					increaseRunningCount();


					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						if (t!=null) t.interrupt();

						if(handler!=null) handler.operationFinished(null,null);
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
//						if (t!=null) t.interrupt();

						if(handler!=null) handler.operationFinished(null,null);
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
//						if (t!=null) t.interrupt();

						if(handler!=null) handler.operationFinished(null,null);
					} catch(Exception e){
						if (t!=null) t.interrupt();

						if(handler!=null) handler.operationFinished(null,null);
						
					}
					finally{
						
						
						
						
						
					}
					// TODO: is this util?
					Core.getInstance().getGUI().update();
				
					if (Core.this.gui != null)
						Core.this.gui.update();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (t!=null) t.interrupt();

				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//					if (t!=null) t.interrupt();

				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//					if (t!=null) t.interrupt();

				}finally{
					

				}
			}
		};
		
		
		working.start();
	}

	/**
	 * Executes an AIBench Operation. This is a high-level method since the parameters can be given by its 'real' value.
	 * Each param is smartly transformed into a ParamSpec, looking for suitable clipboard items in the case of complex objects.
	 *
	 * @param opName The operation uid
	 * @param handler A (optional) progress handler to monitorize the start and finish of the operation. May be null
	 * @param params The parameters given by its real values
	 */
	public void executeOperation(String opName, ProgressHandler handler, List<?> params) {
		OperationDefinition<?> op = getOperationById(opName);
		if (op == null)
			return;
		ParamSpec[] paramsSpecs = CoreUtils.createParams(params);
		this.executeOperation(op, handler, paramsSpecs);
	}
	private void increaseRunningCount(){
		runningCount++;
		getGUI().setStatusText("Core Running "+runningCount+" operations");
	}
	private void decreaseRunningCount(){
		runningCount--;
		if (runningCount == 0){
			getGUI().setStatusText("AIBench");
		}else{
			getGUI().setStatusText("Core Running "+runningCount+" operations");
		}
	}
	/**
	 * Request the finalization of the operations. The operations will receive a {@link Thread#interrupt()} call, so if they mask this signal or fails, they may never finish.
	 * @param key the operation to cancel
	 */
	synchronized public void cancelOperation(Object key){
		OperationKey _key = (OperationKey) key;
		Method  cancel = _key.definition.getCancelMethod();
		if (cancel!=null){
			try {
				cancel.invoke(_key.operationInstance, (Object[]) null);
				_key.cancelled = true;
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
	
	
	
	public void enableOperation(String uid){
		this.enableOperation(this.getOperationById(uid));
	}
	public void enableOperation(OperationDefinition<?> def){
		this.enabledOperations.add(def);
		
		for (CoreListener listener: this.coreListeners){
			listener.operationEnabled(def);
		}
	}
	
	public void disableOperation(String uid){
		this.disableOperation(this.getOperationById(uid));
	}
	public void disableOperation(OperationDefinition<?> def){
		this.enabledOperations.remove(def);
		for (CoreListener listener: this.coreListeners){
			listener.operationDisabled(def);
		}
	}
	
	public boolean isOperationEnabled(OperationDefinition<?> def){
		return this.enabledOperations.contains(def);
	}
	/**
	 * Reads the config from <AIBench_directory>/conf/core.conf
	 */
	private static void readConfig(){
		String path = System.getProperty("aibench.paths.core.conf", "conf/core.conf");
		
		URL url = Util.getGlobalResourceURL(path);
		try {
			Core.CONFIG.load(url.openStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}