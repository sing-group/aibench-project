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
package es.uvigo.ei.aibench.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;

import org.platonos.pluginengine.Plugin;

import es.uvigo.ei.aibench.Launcher;

/**
 * An extension of {@code ObjectInputStream} to help in the deserialization of
 * objects, looking for its classes in one or more plugins.
 * 
 * @author Daniel Gonzalez Peña
 * 
 */
public class PluginsObjectInputStream extends ObjectInputStream {
	private Plugin plugin;

	/**
	 * Creates a new {@code PluginsObjectInputStream} that attempts to load
	 * classes looking for them in the specified {@code plugin}.
	 * 
	 * @param in input stream to read from
	 * @param plugin the {@code Plugin} where classes must be find.
	 * @throws IOException if an I/O error occurs while reading stream header
	 */
	public PluginsObjectInputStream(InputStream in, Plugin plugin) throws IOException {
		super(in);
		this.plugin = plugin;
	}

	/**
	 * Creates a new {@code PluginsObjectInputStream} that attempts to load
	 * classes looking for them in all available plugins obtained using {@code Launcher.pluginEngine.getPlugins()}
	 * 
	 * @param in input stream to read from
	 * @throws IOException if an I/O error occurs while reading stream header
	 */
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

	private Class<?> loadClass(final String descName) {
		try {
			if (this.plugin != null) {
				return plugin.getPluginClassLoader().loadClass(descName);
			} else {
				for (Object o : Launcher.pluginEngine.getPlugins()) {
					final Plugin p = (Plugin) o;
					return p.getPluginClassLoader().loadClass(descName);
				}
			}
		} catch (ClassNotFoundException e) {
		}
		return null;
	}
}
