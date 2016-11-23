/*
 * #%L
 * The AIBench Core Plugin
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
package es.uvigo.ei.aibench.core.clipboard;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

/**
 * Implements the AIBench's core data structure to keep the objects generated
 * during a session coming from the output of the executed operations.
 * 
 * @author Daniel Glez-Peña
 * 
 */
public class Clipboard {

	static Logger logger = Logger.getLogger(Clipboard.class.getName());

	private HashMap<Class<?>, List<ClipboardItem>> items = new HashMap<Class<?>, List<ClipboardItem>>();

	private List<ClipboardListener> listeners = Collections.synchronizedList(new ArrayList<ClipboardListener>());

	private int id = 0; // id counter...

	private HashMap<ClipboardItem, List<ClipboardItem>> complexSubItems = new HashMap<ClipboardItem, List<ClipboardItem>>();

	private HashMap<ClipboardItem, List<ClipboardItem>> listSubItems = new HashMap<ClipboardItem, List<ClipboardItem>>();

	private HashMap<ClipboardItem, List<ClipboardItem>> arraySubItems = new HashMap<ClipboardItem, List<ClipboardItem>>();

	private HashMap<ClipboardItem, ClipboardItem> parents = new HashMap<ClipboardItem, ClipboardItem>();
	
	private List<ClipboardItem> rootClipboardItems = new Vector<ClipboardItem>();
	
	private HashMap<ClipboardItem, Method> subItemsGetters = new HashMap<ClipboardItem, Method>();
	//NOTE: added by paulo maia
	private HashMap<Integer,Integer> itemsOrdered = new HashMap<Integer,Integer>();

	public List<ClipboardItem> getComplexSubItems(ClipboardItem item) {
		return this.complexSubItems.get(item);

	}

	public List<ClipboardItem> getArraySubItems(ClipboardItem item) {
		return this.arraySubItems.get(item);
	}

	public List<ClipboardItem> getListSubItems(ClipboardItem item) {
		return this.listSubItems.get(item);
	}

	public List<ClipboardItem> getRootItems() {
		return this.rootClipboardItems;
	}
	
	public ClipboardItem getParent(ClipboardItem item){
		return this.parents.get(item);
	}
	
	/**
	 * NOTE: added by paulo maia
	 * @param id
	 * @return
	 */
	public int getOrder(int id){
		Integer order = itemsOrdered.get(id);
		
		return (order!=null) ? order.intValue() : -1;
	}
	
	/**
	 * NOTE: added by paulo maia
	 * @param id
	 * @param order
	 */
	public void setOrder(int id,int order){
		itemsOrdered.put(new Integer(id), new Integer(order));
	}
	
	/**
	 * NOTE: added by paulo maia
	 * @param id
	 */
	public void removeOrder(int id){
		itemsOrdered.remove(new Integer(id));
	}

	/**
	 * Creates an empty clipboard
	 */
	public Clipboard() {
	}

	/**
	 * Searches the clipboard for one clipboard item having a given object in its payload
	 * 
	 * @param o The payload
	 * @return The clipboard item
	 */
	public ClipboardItem getClipboardItem(Object o) {
		for (ClipboardItem item : this.getItemsByClass(o.getClass())) {
			if (item.getUserData()==o)
				return item;
		}
		return null;
	}
	/**
	 * @return A list with the clipboard items
	 */
	public List<ClipboardItem> getAllItems() {
		List<ClipboardItem> all = new Vector<ClipboardItem>();
		for (Class<?> key : items.keySet()) {
			all.addAll(items.get(key));
		}
		return all;
	}

	/**
	 * @param type
	 *            The class of the items to search in the clipboard
	 * @return The list of matching clipboard elements
	 */
	public synchronized List<ClipboardItem> getItemsByClass(Class<?> type) {
		ArrayList<ClipboardItem> toret = new ArrayList<ClipboardItem>();
		for (Class<?> clazz : this.items.keySet()) {
			if (type.isAssignableFrom(clazz)) {
				toret.addAll(this.items.get(clazz));
			}
		}
		return toret;
	}

	/**
	 * Adds a listener for clipboard-related events
	 * 
	 * @see ClipboardListener
	 * @param listener
	 */
	public void addClipboardListener(ClipboardListener listener) {
		this.listeners.add(listener);
	}

	public void removeClipboardListener(ClipboardListener listener){
		this.listeners.remove(listener);
	}

	private Vector<ClipboardItem> itemsPutting = new Vector<ClipboardItem>();
	private void prepareFireClipboardElementAdded(final ClipboardItem item) {
		this.itemsPutting.add(item);
	}
	
	private synchronized void fireClipboardElementAdded() {
		final List<ClipboardListener> listeners;
		
		synchronized(this.listeners) {
			listeners = new ArrayList<ClipboardListener>(this.listeners);
		}
		
		for (ClipboardItem item : this.itemsPutting) {
			for (ClipboardListener listener : listeners) {
				if (logger.getEffectiveLevel().equals(Level.DEBUG)) 
					logger.debug("firing clipboard element added to listener of class: " + listener.getClass());
				
				listener.elementAdded(item);
				
				if (logger.getEffectiveLevel().equals(Level.DEBUG)) 
					logger.debug("listener of class: " + listener.getClass() + " finished");
			}
		}
		
		this.itemsPutting.clear();
	}
	
	private ClipboardItem putItem_(Object data, String name) {
		ClipboardItem toret= null;
		if (data == null){
			
			
			List<ClipboardItem> classItems = this.items.get(Object.class);
			if (classItems == null) {
				classItems = new ArrayList<ClipboardItem>();
				this.items.put(Object.class, classItems);
			}
		
	
			toret = new ClipboardItemImpl(this.id++, data, name, Object.class);
			classItems.add(toret);
			prepareFireClipboardElementAdded(toret);
		} else {

			if (data != null)
				if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("Putting a new object: Type: " + data.getClass()
						+ " data: " + data + " name: " + name);
	
			List<ClipboardItem> classItems = this.items.get(data.getClass());
			if (classItems == null) {
				classItems = new ArrayList<ClipboardItem>();
				this.items.put(data.getClass(), classItems);
			}
			toret= new ClipboardItemImpl(this.id++, data, name, data
					.getClass());
	
			classItems.add(toret);
	
			prepareFireClipboardElementAdded(toret);
		}
		return toret;
	}

	private class ClipboardItemImpl extends Observable implements ClipboardItem {
		private int id;

		private Object data;

		private String name;

		private Class<?> clazz;
		
		private boolean removed = false;

		private ReentrantReadWriteLock lock;
		public ClipboardItemImpl(int id, final Object data, String name, Class<?> clazz) {
			super();
			if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("creating a clipboard item");
			this.id = id;
			this.data = data;
			this.name = name;
			this.clazz = clazz;
			this.lock = new ReentrantReadWriteLock(true);
			
			
			
			///////////// EXPERIMENTAL LISTEN TO OBSERVABLES
			if (Core.CONFIG.getProperty("clipboard.listenobservables")!= null && Core.CONFIG.getProperty("clipboard.listenobservables").equals("true")){
				if (data!=null && data instanceof Observable){
					if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("Its observable "+data);
					((Observable) data).addObserver(new Observer(){

						public void update(Observable o, Object arg) {
									
							if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("clipboard item: "+ClipboardItemImpl.this.name+" changed");
								
							if (ClipboardItemImpl.this.getUserData()!=data){
								if (logger.getEffectiveLevel().equals(Level.DEBUG)) logger.debug("Receiving an update event from a clipboard item whose observed object is different from the userdata object, removing the observer");
								o.deleteObserver(this);
								return;
							}
							Datatype datatypeAnnot = ClipboardItemImpl.this.getUserData().getClass().getAnnotation(Datatype.class);
							
							// try to use a naming method in the Datatype
							if (datatypeAnnot!=null){
								if (!datatypeAnnot.namingMethod().equals("")){
									//invoke naming method
									try {
										Method m = ClipboardItemImpl.this.getUserData().getClass().getMethod(datatypeAnnot.namingMethod(), new Class[]{});
										Object nameResult = m.invoke(ClipboardItemImpl.this.getUserData(), (Object[]) null);
										if (nameResult!= null){
											
											ClipboardItemImpl.this.name = nameResult.toString();
										}
										else ClipboardItemImpl.this.name="null";
									} catch (SecurityException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (NoSuchMethodException e) {
										logger.error("Couldn't find the naming method in the Complex item "+ClipboardItemImpl.this.getUserData().getClass()+":  "+datatypeAnnot.namingMethod());
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
							checkSubItemsUpdated(ClipboardItemImpl.this);
							ClipboardItemImpl.this.setChanged();
							ClipboardItemImpl.this.notifyObservers();
						
							fireClipboardElementAdded(); //added by lipido in 23/04/2008 may cause problems, Clipboard has poltergeists fenomena!
						}
						
					});
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardItem#getID()
		 */
		public int getID() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardItem#getUserData()
		 */
		public Object getUserData() {
			return data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardItem#getName()
		 */
		public String getName() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardItem#getRegisteredUserClass()
		 */
		public Class<?> getRegisteredUserClass() {
			return clazz;
		}

		public String toString() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardItem#setName()
		 */
		public void setName(String name) {

			this.name = name;
		}

		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardItem#wasRemoved()
		 */
		public boolean wasRemoved() {
			return removed;
		}

		/* (non-Javadoc)
		 * @see es.uvigo.ei.aibench.core.clipboard.ClipboardItem#getLock()
		 */
		public ReentrantReadWriteLock getLock() {
			return this.lock;
		}

	}

	public ClipboardItem findByID(int id) {
		for (List<ClipboardItem> list : this.items.values()) {
			for (ClipboardItem item : list) {
				if (item.getID() == id) {
					return item;
				}
			}
		}
		return null;
	}

	public synchronized List<ClipboardItem> putItem(Object data, String name) {		
		this.itemsPutting = new Vector<ClipboardItem>();
		
		if (data == null)
			return new ArrayList<ClipboardItem>();
		List<ClipboardItem> list = new ArrayList<ClipboardItem>();

		ClipboardItem item = putElemInClipboard(data, list, name);
		this.rootClipboardItems.add(item);
		
		this.fireClipboardElementAdded();
		
		return list;
	}

	private ClipboardItem putElemInClipboard(Object elem, List<ClipboardItem> clipboardItems, String name) {

		ClipboardItem toret = null;

		if (elem != null) {
			Datatype aibenchType = elem.getClass()
					.getAnnotation(Datatype.class);
			if (aibenchType != null
					&& aibenchType.structure() != Structure.SIMPLE) {

				if (aibenchType.structure() == Structure.COMPLEX) {
					toret = putComplexInClipboard(elem, clipboardItems, name);
				} else {
					toret = putListInClipboard(elem, clipboardItems, name);
				}
			} else if (!elem.getClass().isArray()) {
				toret = putSimpleInClipboard(elem, clipboardItems, name);
			}

			if (elem.getClass().isArray()) {
				List<ClipboardItem> subItems = new ArrayList<ClipboardItem>();
				for (int i = 0; i < Array.getLength(elem); i++) {
					Object elem_i = Array.get(elem, i);
					// UPDATE THE CLIPBOARD
					// ClipboardItem item_i = this.putItem_( elem_i,
					// elem.getClass().getSimpleName()+" (instance
					// "+(this.getItemsByClass(elem.getClass()).size()-1)+")
					// ["+i+"]: "+elem_i.getClass().getSimpleName()+" (instance
					// "+this.getItemsByClass(elem_i.getClass()).size()+")");
					// clipboardItems.add(item_i);
					subItems.add(putElemInClipboard(elem_i, clipboardItems,
							null));
					// putSimpleInClipboard(elem_i, clipboardItems, name);

				}

				toret = putSimpleInClipboard(elem, clipboardItems, name);
				this.arraySubItems.put(toret, subItems);
			}
		} else {
			//////// WHAT THE HELL WE DO WITH NULLS???????
			toret = putSimpleInClipboard(elem, clipboardItems, name);
			
		}
		return toret;

	}

	private HashMap<Class<?>, List<ElemAndNumber>> uniqueItems = new HashMap<Class<?>, List<ElemAndNumber>>();
	private ClipboardItem putSimpleInClipboard(Object elem,	List<ClipboardItem> clipboardItems, String name) {
		String finalName = name;
		
		
		if (elem == null) {
			finalName = name + ":  null";
			
		} else {
			String surname = null;
			Datatype datatypeAnnot = elem.getClass().getAnnotation(Datatype.class);
			
			// try to use a naming method in the Datatype
			if (datatypeAnnot!=null){
				if (!datatypeAnnot.namingMethod().equals("")){
					//invoke naming method
					try {
						Method m = elem.getClass().getMethod(datatypeAnnot.namingMethod(), new Class<?>[]{});
						Object nameResult = m.invoke(elem, (Object[]) null);
						if (nameResult!= null) surname = nameResult.toString();
						else nameResult="null";
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						logger.error("Couldn't find the naming method in the Complex item "+elem.getClass()+":  "+datatypeAnnot.namingMethod());
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
			
			// no naming method
			if (surname == null || surname ==""){
				
				
				int number=-1;
				
				if (this.uniqueItems.get(elem.getClass())==null){
					this.uniqueItems.put(elem.getClass(), new ArrayList<ElemAndNumber>());
				}
				for (ElemAndNumber item : this.uniqueItems.get(elem.getClass())) {
					if (item.elem == elem){
						number = item.number;
						break;
					}
					
				}
				if (number==-1){
					if (this.uniqueItems.get(elem.getClass()).size()>0){
						ElemAndNumber last = this.uniqueItems.get(elem.getClass()).get(this.uniqueItems.get(elem.getClass()).size()-1);
						number = last.number+1;
					}else{
						number = 0;
					}
					
					ElemAndNumber en = new ElemAndNumber();
					en.number = number;
					en.elem = elem;
					this.uniqueItems.get(elem.getClass()).add(en);
					
				}
				surname = elem.getClass().getSimpleName() + " (instance " + number
				+ ")";
				
				finalName = (name == null ? "" : name + ": ")
				+ surname;
			}
			else finalName = surname; //NOTE: modified by paulo maia -  new behavior puts the surname above the name (if surname exists and is != "" then use it as final name, if not then AIBench generates a name)
			
		}
		
		/**
		 * NOTE: added by paulo maia
		 * Need to see if a setNameMethod exists. If it exists, then it should be invoked here as well
		 */
		if(elem!=null){
			Datatype datatypeAnnot = elem.getClass().getAnnotation(Datatype.class);
			if(datatypeAnnot!=null){
				if (!datatypeAnnot.setNameMethod().equals("")){
					//invoke set name method
					try {
						Method m = elem.getClass().getMethod(datatypeAnnot.setNameMethod(), new Class<?>[]{String.class});
						m.invoke(elem, finalName); // final name used here as default name for the inner object
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						logger.error("Couldn't find the set name method in the Complex item "+elem.getClass()+":  "+datatypeAnnot.setNameMethod());
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
		// end add

		ClipboardItem item = this.putItem_(elem, finalName);
		
		
		if (item != null)
			clipboardItems.add(item);
		

		return item;
	}
	
	// help class for instance auto-naming
	class ElemAndNumber{
		public Object elem;
		public int number;
	}
	private ClipboardItem putComplexInClipboard(Object elem,
			List<ClipboardItem> clipboardItems, String name) {

		//List<ClipboardItem> subItems = new ArrayList<ClipboardItem>();
		List<ClipboardItem> subItems = new LinkedList<ClipboardItem>();
		HashMap<Integer,Method> order = new HashMap<Integer,Method>();
		for (Method m : elem.getClass().getMethods()) {
			es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard clip = m
					.getAnnotation(es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard.class);
			if (clip != null) {
				try {

					//NOTE: modified by paulo maia
					if(clip.order()!=-1){
						int ordem = (order.containsKey(clip.order())) ? ordem = -1 :  clip.order(); //order was repeated by the user: notification required...
						if(ordem==-1)
							throw new IllegalArgumentException("Please verify the order of the subitems of the complex type <"+elem.getClass().getName()+">.\n" +
									"Equally ordered elements were found.\n");
						else{ 
							order.put(new Integer(clip.order()), m);
						}
					} else {
						final ClipboardItem item = putElemInClipboard(m.invoke(elem,(Object[]) null), clipboardItems, clip.name());
						subItems.add(item); // no order specified, behave as usual (the user has the obligation to correctly specify the order for all the subitems)
						this.subItemsGetters.put(item, m);
					}
					//end of modifications
					
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
		
		//modified by paulo maia
		int[] sortKeys = new int[order.keySet().size()];
		int i=0;
		for(Integer key : order.keySet()){
			sortKeys[i] = key.intValue();
			i++;
		}
			
		Arrays.sort(sortKeys);
		
		for(int k:sortKeys){
			Method m =order.get(new Integer(k));
			es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard clip = m.getAnnotation(es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard.class);
			try {
				ClipboardItem item = putElemInClipboard(m.invoke(elem,(Object[]) null), clipboardItems, clip.name());
				this.subItemsGetters.put(item, m);
				itemsOrdered.put(new Integer(item.getID()), new Integer(k));
				subItems.add(item);
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

		//end of modifications
		
		ClipboardItem toret = putSimpleInClipboard(elem, clipboardItems, name);
		this.complexSubItems.put(toret, subItems);
		
		//set parents
		for (ClipboardItem item: subItems){
			this.parents.put(item, toret);
		}
		
		return toret;
	}

	private ClipboardItem putListInClipboard(Object elem,
			List<ClipboardItem> clipboardItems, String name) {

		List<ClipboardItem> subItems = new ArrayList<ClipboardItem>();
		for (Method m : elem.getClass().getMethods()) {
			ListElements annot = m.getAnnotation(ListElements.class);
			if (annot != null) {
				try {
					Collection<?> subElems = (Collection<?>) m.invoke(elem, (Object[]) null);
					
					if (subElems != null){
						for (Object o : subElems) {
							subItems.add(putElemInClipboard(o, clipboardItems, null));
						}
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
			}
		}
		ClipboardItem toret = putSimpleInClipboard(elem, clipboardItems, name);
		this.listSubItems.put(toret, subItems);
		
		//set parents
		for (ClipboardItem item: subItems){
			this.parents.put(item, toret);
		}
		
		return toret;

	}
	
	public synchronized void removeClipboardItem(ClipboardItem item){
		//if (this.getRootItems().indexOf(item)== -1) return;
		if (logger.getEffectiveLevel().equals(Level.DEBUG))
			logger.debug("attempting to remove item: "+item);
		
		if (complexSubItems.containsKey(item)){
			if (logger.getEffectiveLevel().equals(Level.DEBUG))
				logger.debug("remove: is a complex item, removing subitems");
			
			List<ClipboardItem> subItems = complexSubItems.get(item);
			for (ClipboardItem c: subItems){
				if (!c.wasRemoved())removeClipboardItem(c);
				
			}
			complexSubItems.remove(item);
		}
		
		if (listSubItems.containsKey(item)){
			if (logger.getEffectiveLevel().equals(Level.DEBUG))
				logger.debug("remove: is list-complex item, removing subitems");
			
			List<ClipboardItem> subItems = listSubItems.get(item);
			for (ClipboardItem c: subItems){
				if (!c.wasRemoved())removeClipboardItem(c);
			}
			listSubItems.remove(item);
		}
		
		if (arraySubItems.containsKey(item)){
			if (logger.getEffectiveLevel().equals(Level.DEBUG))
				logger.debug("remove: is an array item, removing subitems");
			
			List<ClipboardItem> subItems = arraySubItems.get(item);
			for (ClipboardItem c: subItems){
				if (!c.wasRemoved()) removeClipboardItem(c);
			}
			arraySubItems.remove(item);
		}
		
		for (Class<?> c: this.items.keySet()){			
			if (this.items.get(c).contains(item)){
				if (logger.getEffectiveLevel().equals(Level.DEBUG))
					logger.debug("remove: found, removing");
				
				this.items.get(c).remove(item);
			}
		}
		
		if (rootClipboardItems.contains(item)){
			if (logger.getEffectiveLevel().equals(Level.DEBUG))
				logger.debug("remove: is a root item");
			
			rootClipboardItems.remove(item);
		}
		
		//remove unique items (thanks hprof!!)
		for (Class<?> c : this.uniqueItems.keySet()){
			List<ElemAndNumber> objs = this.uniqueItems.get(c);
			for (int i = objs.size()-1; i>=0; i--){
				if (objs.get(i).elem==item.getUserData()){
					if (logger.getEffectiveLevel().equals(Level.DEBUG))
						logger.debug("removed from unique items");
					
					objs.remove(i);
				}
			}
		}
		
		final List<ClipboardListener> listeners;
		
		synchronized (this.listeners) {
			listeners = new ArrayList<ClipboardListener>(this.listeners);
		}
		
		for (ClipboardListener listener : listeners){
			listener.elementRemoved(item);
		}
		
		((ClipboardItemImpl) item).removed = true;
		((ClipboardItemImpl) item).data = null;
	}
	
	
	
	//////// EXPERIMENTAL LISTEN TO OBSERVABLES
	private void checkSubItemsUpdated(ClipboardItem item){
		int counter = 0;
		if (isComplex(item)){
			
			for (ClipboardItem possibleOld : this.getComplexSubItems(item)){
				Method m = this.subItemsGetters.get(possibleOld);
			
				es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard clip = m
						.getAnnotation(es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard.class);
				if (clip != null) {
					try {
						Object value = m.invoke(item.getUserData(),(Object[]) null);
						if (value != possibleOld.getUserData()){
							if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("A child in a complex item has changed");
							
							
							//NOTE: added by paulo maia
							int relativeOrder = clip.order();
							ClipboardItem newItem = putElemInClipboard(value, new ArrayList<ClipboardItem>(), clip.name() );
							
							if(relativeOrder!=-1){
								int newID = newItem.getID();
								this.setOrder(newID, relativeOrder);
								this.removeOrder(possibleOld.getID());
							}
							if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("UPDATED "+newItem.getName()+" to ID:"+newItem.getID()+" with order = "+relativeOrder);
							
							//end add
							
							this.getComplexSubItems(item).set(counter,newItem);
							this.subItemsGetters.remove(possibleOld);
							this.subItemsGetters.put(newItem, m);
							this.itemsPutting = new Vector<ClipboardItem>();
							this.removeClipboardItem(possibleOld);
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
					counter++;
				}
			}
		}
		if (isList(item)){
			
			for (Method m : item.getUserData().getClass().getMethods()) {
				ListElements annot = m.getAnnotation(ListElements.class);
				if (annot != null) {
					try {
						List<?> subElems = (List<?>) m.invoke(item.getUserData(), (Object[]) null);
						if (subElems != null){
							for (Object o : subElems) {
								if (this.getListSubItems(item).size() <= counter){
									// the list has growed
									ClipboardItem newitem = putElemInClipboard(o, new ArrayList<ClipboardItem>(), null );
									this.getListSubItems(item).add(newitem);
									this.parents.put(newitem, item);
								}
								ClipboardItem possibleOld = this.getListSubItems(item).get(counter); 
								if (o != possibleOld.getUserData()){
									if (logger.getEffectiveLevel().equals(Level.DEBUG))logger.debug("A child in a complex-list item has changed");
									this.getListSubItems(item).set(counter, putElemInClipboard(o, new ArrayList<ClipboardItem>(),null ));
									this.removeClipboardItem(possibleOld);
								}
								counter++;
							}
							if (counter < this.getListSubItems(item).size()){
								
								
								// the list has decreased
								for (int i = this.getListSubItems(item).size()-1; i >= counter; i--){
									ClipboardItem old = this.getListSubItems(item).get(i);
									this.getListSubItems(item).remove(i);
									this.removeClipboardItem(old);
								}
							}
						}else{
							// TODO: repair this
							throw new RuntimeException("please dont make null a list");
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
					break;
				}
			}
		}
		if (isArray(item)){	
			int i =0;
			for ( i = 0; i < Array.getLength(item.getUserData()); i++) {
				Object elem_i = Array.get(item.getUserData(), i);
				
				if (this.getArraySubItems(item).size() <= i){
					// the array has growed
					ClipboardItem newitem = putElemInClipboard(elem_i, new ArrayList<ClipboardItem>(), null );
					this.getArraySubItems(item).add(newitem);
					this.parents.put(newitem, item);
				}
				
				ClipboardItem possibleOld= this.getArraySubItems(item).get(i);				
				if (elem_i != possibleOld.getUserData()){
					
					this.getListSubItems(item).set(counter, putElemInClipboard(elem_i, new ArrayList<ClipboardItem>(), null ));
					this.removeClipboardItem(possibleOld);
				}
			}
			if (i < this.getArraySubItems(item).size()){
				
				
				// the array has decreased
				for (int j = this.getArraySubItems(item).size()-1; j >= i; j--){
					ClipboardItem old = this.getArraySubItems(item).get(j);
					this.getArraySubItems(item).remove(j);
					this.removeClipboardItem(old);
				}
			}
		}
	
		
	}
	
	private boolean isComplex(ClipboardItem item){
		if (item.getUserData() == null) return false;
		Datatype annot = item.getUserData().getClass().getAnnotation(Datatype.class); 
		if (annot!= null && annot.structure() == Structure.COMPLEX) return true;
		return false;
	}
	private boolean isList(ClipboardItem item){
		if (item.getUserData() == null) return false;
		Datatype annot = item.getUserData().getClass().getAnnotation(Datatype.class); 
		if (annot!= null && annot.structure() == Structure.LIST) return true;
		return false;
	}
	private boolean isArray(ClipboardItem item){
		if (item.getUserData() == null) return false;
		if (item.getUserData().getClass().isArray()) return true;
		return false;
	}
	
}
