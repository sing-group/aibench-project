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
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.uvigo.ei.pipespecification.storage.PipeDefinition;

public class Unifier {

	static boolean unify(Executable a, Executable b) {
		return unify(a.getIncomeArgumentTypes(), b.getIncomeArgumentTypes()) == null;
	}

	static Class<?> unify(Class<?> temp, Class<?> next) {
		if (temp.isAssignableFrom(next) || next.equals(Void.TYPE))
			return temp;
		else if (next.isAssignableFrom(temp) || temp.equals(Void.TYPE))
			return next;
		else
			return null;
	}

	public static List<Class<?>> unifier(List<Executable> executables) {
		Iterator<Executable> iterator = executables.iterator();
		List<Class<?>> result = iterator.next().getIncomeArgumentTypes();
		while (iterator.hasNext()) {
			result = unify(result, iterator.next().getIncomeArgumentTypes());
			if (result == null)
				return null;
		}
		return result;
	}
	public static boolean unifies(List<PipeDefinition> executables) {
		Iterator<PipeDefinition> iterator = executables.iterator();
		List<Class<?>> result = iterator.next().getIncomeTypes();
		while (iterator.hasNext()) {
			result = unify(result, iterator.next().getIncomeTypes());
			if (result == null)
				return false;
		}
		return true;
	}

	static List<Class<?>> unify(List<Class<?>> temp, List<Class<?>> next) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		if (temp.size() != next.size())
			return null;
		for (int i = 0; i < temp.size(); i++) {
			Class<?> unified = Unifier.unify(temp.get(i), next.get(i));
			if (unified == null)
				return null;
			list.add(unified);
		}
		assert list.size() == temp.size();
		return list;
	}

}
