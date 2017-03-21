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
package es.uvigo.ei.aibench.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.Transformer;

/**
 * @author Daniel González Peña 12-sep-2006
 *
 */
public class ParamSpec {
	private Class<?> type;
	private Object value; // if this keeps information of a simple value (most cases) we only use the 0 position
	private Object transformedValue;
	
	private ParamSource source;
	private String name;
	private String transformerSignature;

	private Object unserializedInstance = null; //when ParamSource.SERIALIZED
	
	/**
	 * Constructs a ParamSpec of an array parameter.
	 * 
	 * @param name name of the parameter.
	 * @param type type of the parameter. An array type is required.
	 * @param values values of the parameter.
	 * @throws IllegalArgumentException if the class type is not an array.
	 */
	public ParamSpec(String name, Class<?> type,  ParamSpec[] values) throws IllegalArgumentException{
		super();

		if (!type.isArray()) throw new IllegalArgumentException("The type must be array");
		this.type = type;
		this.value = values;
		this.source = ParamSource.MIXED;
		this.name = name;
	}
	
	/**
	 * Constructs a ParamSpect with a single value.
	 * 
	 * @param name name of the parameter.
	 * @param type type of the parameter.
	 * @param value the value of the parameter.
	 * @param source the source of the parameter. If the type of the parameter is array,
	 * then the source must be ParamSource.CLIBOARD. ParamSource.MIXED are not allowed. 
	 * @throws IllegalArgumentException if this class is an array or the source is MIXED.
	 */
	public ParamSpec(String name, Class<?> type, Object value, ParamSource source) throws IllegalArgumentException {
		super();
		if ((type.isArray() && source != ParamSource.CLIPBOARD) || source == ParamSource.MIXED)
			throw new IllegalArgumentException("can't use MIXED source with only one value");
		this.type = type;
		this.value = value;
		this.source = source;
		this.name = name;
	}

	/**
	 * @return the value used in the constructor.
	 */
	public Object getValue(){
		return this.value;
	}
	
	/**
	 * Returns the raw value of this parameter specification.
	 * 
	 * @return the raw value of this parameter specification.
	 * 
	
	/**
	 * Returns the "usable" object.
	 * 
	 * It the {@code value} attribute is {@code null}, returns {@code null}. 
	 * If {@code this} was created with {@code ParamSource.MIXED}, this method returns the array with
	 * the real values of each sub-ParamSpec (not the ParamSpec[] array).
	 * If {@code this} was created with {@code ParamSource.STRING_CONSTRUCTOR}, it uses the type and ParamSource params used
	 * in this constructor to create the return value. For example, if the type was Integer.class and the value
	 * was "7", this method returns a new Integer("7").
	 * If {@code this} was created with {@code ParamSource.CLIPBOARD}, it returns the real object. If there
	 * is a transformer defined, it will invoke it, but only once
	 * 4. Else, returns the value used in the constructor.
	 * 
	 * @return the "usable" object.
	 */
	public Object getRawValue() {
		if (this.value == null) return null; //Bomb launched...

		if (source == ParamSource.MIXED){
			Object arrayResult = Array.newInstance(this.type.getComponentType(), ((ParamSpec[]) this.value).length);

			int i = 0;
			for (ParamSpec spec : (ParamSpec[]) this.value){
				Array.set(arrayResult, i++, spec.getRawValue());
			}
			return arrayResult;
		}else if (source == ParamSource.STRING_CONSTRUCTOR){

			try{
				Class<?> inClass = this.type;

				//Check if is very primitive ;-)
				if (inClass.equals(float.class)){
					inClass = Float.class;
				}else if (inClass.equals(int.class)){
					inClass = Integer.class;
				}else if (inClass.equals(long.class)){
					inClass = Long.class;
				}else if (inClass.equals(double.class)){
					inClass = Double.class;
				}else if (inClass.equals(char.class)){
					inClass = Character.class;
				}else if (inClass.equals(boolean.class)){
					inClass = Boolean.class;
				}
				Constructor<?> c =null;
				Object o = null;
				if (inClass == Character.class){
					c = Character.class.getConstructor(Character.TYPE);
					o = c.newInstance(this.value.toString().charAt(0));
				}else{
					c = inClass.getConstructor(new Class[]{String.class});
					o = c.newInstance(this.value.toString());
				}	


				if (inClass.equals(float.class)){
					return ((Float)o).floatValue();
				}else if (inClass.equals(int.class)){
					return ((Integer)o).intValue();
				}else if (inClass.equals(long.class)){
					return ((Long)o).longValue();
				}else if (inClass.equals(double.class)){
					return ((Double)o).doubleValue();
				}else if (inClass.equals(char.class)){
					return ((Character)o).charValue();
				}
				return o;
			}catch(NoSuchMethodException e){
				e.printStackTrace();
				return null;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} catch (InstantiationException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		}else if (source == ParamSource.CLIPBOARD){
			if (this.getTransformerSignature()!=null){
				Transformer t = Core.getInstance().getTransformerBySignature(this.getTransformerSignature());
				if (t !=null){
					if (transformedValue == null){
						transformedValue = t.transform(((ClipboardItem)this.value).getUserData());
					}
					return transformedValue;
				}else{
					throw new RuntimeException("Transformer for signature: "+this.getTransformerSignature()+" not available in the Core");
				}
			}
			return ((ClipboardItem)this.value).getUserData();
		}else if (source == ParamSource.SERIALIZED){
			
			// deserialize
			if (this.unserializedInstance==null){
				ObjectInputStream ois = null;
				try {
					ois = new PluginsObjectInputStream(new ByteArrayInputStream(Base64Coder.decode(this.value.toString())));
					this.unserializedInstance = ois.readObject();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						ois.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return this.unserializedInstance;
		}
		else{
			return this.value;
		}
	}

	/**
	 * Returns the source from which this param was obtained.
	 * 
	 * @return the source from which this param was obtained.
	 */
	public ParamSource getSource(){
		return this.source;
	}

	@Override
	public String toString(){
		String toret = "---ParamSpec----";
		toret+="\nClass: "+this.type;
		toret+="\nValue: "+this.value;
		toret+="\nSource: "+this.source;
		if (this.source == ParamSource.MIXED){
			toret+="\nSubSpec: "+this.source;
			for (ParamSpec sub : (ParamSpec[]) this.value){
				toret+=sub.toString();
			}
		}
		toret+="\nTransformer: "+this.transformerSignature;
		toret+="\n---------------";

		//ups
		//toret =  this.getRawValue().toString();
		if (this.transformerSignature==null){
			if (this.getRawValue()!=null){
				toret = this.getRawValue().toString();
			}else{
				toret = "<null>";
			}
		}else{
			//avoid transformation
			toret = this.getValue().toString()+" (pending transformation)";
		}
		return toret;
	}
	
	/**
	 * @return the name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return the type.
	 */
	public Class<?> getType() {
		return this.type;
	}
	
	/**
	 * @param type the type to set.
	 */
	public void setType(Class<?> type) {
		this.type = type;
	}
	
	/**
	 * @return the transformer signature.
	 */
	public String getTransformerSignature() {
		return this.transformerSignature;
	}
	
	/**
	 * @param transformerSignature the transformer signature to set.
	 */
	public void setTransformerSignature(String transformerSignature) {
		this.transformerSignature = transformerSignature;
	}

}


