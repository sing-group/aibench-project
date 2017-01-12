/*
 * #%L
 * The AIBench basic runtime and plugin engine
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
package es.uvigo.ei.aibench;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Daniel Gonzalez Peña
 *
 */
public class Util {
	public static File urlToFile(URL url) {
		return urlToFile(url, false);
	}
	
	public static File urlToFile(URL url, boolean usePath) {
		try {
			return new File(url.toURI());
		} catch (Exception e) {
			return new File(usePath ? url.getPath() : url.getFile());
		}
	}
	
	public static URL getGlobalResourceURL(String resourcePath){
		try {
			URL url = Util.class.getProtectionDomain().getCodeSource().getLocation();
			
			try {
				if (url.getFile().endsWith(".jar")) {
					url = new URL(url.toString().substring(0,url.toString().lastIndexOf('/'))+"/../"+resourcePath);
				} else {
					url = new URL(url+"../"+resourcePath);
				}
				
				if (!new File(url.getFile()).exists()) {
					//fallback to current dir
					File fallbackFile = new File(resourcePath);
					if (!fallbackFile.exists()) {
						throw new IllegalArgumentException("cannot find global resource "+resourcePath);
					}
					return fallbackFile.toURI().toURL();
				}
				return url;
			} catch (MalformedURLException e1) {
				throw new RuntimeException("Not found a aibench configuration file, searching in url: "+url.getFile(), e1);
			}
		} catch(NullPointerException e) {
			try {
				return new File(resourcePath).toURI().toURL();
			} catch (Exception e1) {
				throw new RuntimeException("Can't find resource in path " + resourcePath + " due to " + e1, e1);
			}
		}
	}
}
