/*
 * #%L
 * The AIBench Shell Plugin
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
package bsh;

import java.util.Hashtable;

/**
 * 
 * @author Pat Niemeyer (pat@pat.net)
 */
/*
 * Note: which of these things should be checked at parse time vs. run time?
 */
public class Modifiers implements java.io.Serializable {
	public static final int CLASS = 0, METHOD = 1, FIELD = 2;

	Hashtable modifiers;

	/**
	 * @param context
	 *            is METHOD or FIELD
	 * @param name
	 *            the name of the modifier.
	 */
	public void addModifier(int context, String name) {
		if (modifiers == null)
			modifiers = new Hashtable();

		Object existing = modifiers.put(name, Void.TYPE/*
														 * arbitrary flag
														 */);
		if (existing != null)
			throw new IllegalStateException("Duplicate modifier: " + name);

		int count = 0;
		if (hasModifier("private"))
			++count;
		if (hasModifier("protected"))
			++count;
		if (hasModifier("public"))
			++count;
		if (count > 1)
			throw new IllegalStateException("public/private/protected cannot be used in combination.");

		switch (context) {
		case CLASS:
			validateForClass();
			break;
		case METHOD:
			validateForMethod();
			break;
		case FIELD:
			validateForField();
			break;
		}
	}

	public boolean hasModifier(String name) {
		if (modifiers == null)
			modifiers = new Hashtable();
		return modifiers.get(name) != null;
	}

	// could refactor these a bit
	private void validateForMethod() {
		insureNo("volatile", "Method");
		insureNo("transient", "Method");
	}

	private void validateForField() {
		insureNo("synchronized", "Variable");
		insureNo("native", "Variable");
		insureNo("abstract", "Variable");
	}

	private void validateForClass() {
		validateForMethod(); // volatile, transient
		insureNo("native", "Class");
		insureNo("synchronized", "Class");
	}

	private void insureNo(String modifier, String context) {
		if (hasModifier(modifier))
			throw new IllegalStateException(context + " cannot be declared '" + modifier + "'");
	}

	public String toString() {
		return "Modifiers: " + modifiers;
	}

}
