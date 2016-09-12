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
 * Util.java
 * This class is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 13/03/2008
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
		try{
			URL url = Util.class.getProtectionDomain().getCodeSource().getLocation();
			
			try {
				if (url.getFile().endsWith(".jar")){
					url = new URL(url.toString().substring(0,url.toString().lastIndexOf('/'))+"/../"+resourcePath);
				}else{
					url = new URL(url+"../"+resourcePath);
				}
				return url;
			} catch (MalformedURLException e1) {
				throw new RuntimeException("Not found a aibench configuration file, searching in url: "+url.getFile());
			}
		} catch (NullPointerException e) {
			try {
				return new File(resourcePath).toURI().toURL();
			} catch (Exception e1) {
				throw new RuntimeException("Can't find resource in path "+resourcePath+" due to "+e1);
			}
		}
	}
}
