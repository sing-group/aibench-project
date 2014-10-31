/*

Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola and Ruben Dominguez Carbajales


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
 * AIBenchLogger.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on ${date}
 */
package es.uvigo.ei.aibench;

import java.util.Calendar;

import org.platonos.pluginengine.logging.ILogger;
import org.platonos.pluginengine.logging.LoggerLevel;

/**
 * @author Ruben Dominguez Carbajales 11-oct-2005 - 2005
 */
public class AIBenchLogger implements ILogger {

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.platonos.pluginengine.logging.ILogger#log(org.platonos.pluginengine.logging.LoggerLevel,
	 *      java.lang.String, java.lang.Throwable)
	 */
	public void log(LoggerLevel arg0, String mensaje, Throwable arg2) {
		
		Calendar calendar = Calendar.getInstance();
		
		String hora = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + " " + calendar.get(Calendar.MILLISECOND);

		String prompt = "[" + hora + "]: ";

		if (arg0.equals(LoggerLevel.FINE))
			; //System.out.println(prompt + "FINE :" + mensaje);
		else if (arg0.equals(LoggerLevel.INFO))
			;//System.out.println(prompt + "INFO :" + mensaje);
		else if (arg0.equals(LoggerLevel.SEVERE))
			System.out.println(prompt + "SEVERE :" + mensaje);
		else if (arg0.equals(LoggerLevel.WARNING))
			System.out.println(prompt + "WARNING :" + mensaje);
		else System.out.println(prompt + mensaje);
	}

}
