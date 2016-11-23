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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;

import org.platonos.pluginengine.Plugin;

import es.uvigo.ei.aibench.Launcher;

/**
 * @author Daniel Gonzalez Peña
 * 
 */
class PluginsObjectInputStream extends ObjectInputStream {
	public PluginsObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	protected Class<?> resolveClass(ObjectStreamClass desc)
	throws ClassNotFoundException {
		Class<?> c = null;
		
		final String descName = desc.getName();
		if (descName.matches("\\[L.*;")) { // Is an array?
			final String className = descName.substring(2, descName.length()-1);
			
			c = Array.newInstance(loadClass(className), 0).getClass(); // Creates an empty array to get its class
		} else {
			c = loadClass(descName);
		}
		
		if (c == null)
			throw new ClassNotFoundException(descName);
		
		return c;
	}

	/**
	 * @param c
	 * @param descName
	 * @return
	 */
	private Class<?> loadClass(final String descName) {
		for (Object o : Launcher.pluginEngine.getPlugins()) {
			final Plugin p = (Plugin) o;
			try {
				return p.getPluginClassLoader().loadClass(descName);
			} catch (ClassNotFoundException e) {}
		}
		
		return null;
	}
}