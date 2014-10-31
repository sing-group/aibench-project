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
 * ParamSource.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 12/09/2006
 */
/**
 *This class represents from where a param value was taken.
 *
 *@see ParamSpec
 */
package es.uvigo.ei.aibench.core;

/**
 * @author Daniel González Peña 12-sep-2006
 *
 */
public enum ParamSource {
	STRING,
	STRING_CONSTRUCTOR,
	ENUM,
	CLIPBOARD,
	SERIALIZED,
	MIXED;



}
