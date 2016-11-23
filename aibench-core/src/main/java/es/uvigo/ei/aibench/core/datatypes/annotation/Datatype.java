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
package es.uvigo.ei.aibench.core.datatypes.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Daniel Glez-Peña
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Datatype {
	/**
	 * The structure of this datatype
	 * @see Structure
	 * @return the structure
	 */
	Structure structure() default Structure.SIMPLE;
	
	/**
	 * A method to get a symbolic name to each instance of this datatype. This name will be used, for example, when an instance of this datatype is placed in the Clipboard.
	 * If you don't use this attribute, AIBench will name it automatically
	 * @return the name of the namingMethod to be used.
	 */
	String namingMethod() default "";
	
	/**
	 * If the only view available to this type is the default view, and this attribute is set to false, it will not be displayed
	 * @return If this item should be displayed or not with the available views
	 */
	boolean viewable() default true;
	
	/**
	 * NOTE: added by paulo maia
	 * A method to define a symbolic name to an instance of this datatype. This method will be used, for instance, when a user manually edits the name of a node in the Clipboard.
	 * This Method will be raised and the new name will be merged both with the ClipboardItem name and with that Datatype name (the usedData of that ClipboardItem).
	 * @return the name of  of the setNameMethod to be used.
	 */
	String setNameMethod() default "";
	
	/**
	 * NOTE: added by paulo maia
	 * This method sets the remove permissions for the object. It can either be removable or not. By default it is set to true.
	 * @return true if the object has permissions to be removed or false if not.
	 */
	boolean removable() default true;
	
	/**
	 * NOTE: added by Miguel Reboiro-Jato
	 * This method sets the rename permissions for the object. It can either be renameable or not. By default it is set to true.
	 * @return true if the object has permissions to be renamed or false if not.
	 */
	boolean renameable() default true;
	
	/**
	 * NOTE: added by paulo maia
	 * This method allows for the definition of a specific remove method for a given datatype. If it is defined, the Workbench will use it
	 * @return the name of the removeMethod to be used.
	 */
	String removeMethod() default "";

	/**
	 * Returns the url or the id of the help documentation for the current datatype.
	 * @return the url or the id of the help documentation.
	 */
	String help() default "";
	
	/**
	 * Returns the clipboard name for the current datatype's class.
	 * @return the clipboard name for the current datatype's class.
	 */
	String clipboardClassName() default "";
	
	/**
	 * This method sets whether the datatype should be automatically shown. 
	 * @return true if the datatype should be automatically shown and false if not.
	 */
	boolean autoOpen() default false;
}
