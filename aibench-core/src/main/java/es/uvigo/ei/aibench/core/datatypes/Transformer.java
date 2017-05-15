/*
 * #%L
 * The AIBench Core Plugin
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
package es.uvigo.ei.aibench.core.datatypes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Daniel Glez-Peña
 *
 */
public class Transformer {
	private Method method;
	private Object instance;
	private String name;
	public Transformer(Method m){
		this.method = m;
		if (m == null) throw new NullPointerException("method cant be null");
		if (!Modifier.isStatic(m.getModifiers())){
			try {
				Class<?> c = m.getDeclaringClass();
				this.instance = c.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public Object transform(Object source) {
		try {
			return method.invoke(instance, new Object[]{source});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Class<?> getSourceType(){
		Class<?>[] params =  this.method.getParameterTypes();
		return params[0];
	}
	public Class<?> getDestinyType(){
		return this.method.getReturnType();
	}
	public String getSignature(){
		return ""+this.getSourceType().getName()+"->"+this.method.getDeclaringClass().getName()+"."+this.method.getName()+"->"+this.getDestinyType().getName();
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	

}
