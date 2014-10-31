/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part the AIBench Project. 

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
 * ArgsRenamedPipeDefinition.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.pipespecification.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.IncompatibleContraintsException;

class ArgsRenamerPipeDefinition extends PipeDefinition {
	private final PipeDefinition pipeDef;

	private final Map<String, String> renamer;

	private final Map<String, String> inverseRenamer;

	ArgsRenamerPipeDefinition(Map<String, String> renamer,
			PipeDefinition pipeDef) {
		super(pipeDef.getIncomeTypes(),pipeDef.getOutcomeTypes());
		if (renamer == null)
			throw new IllegalArgumentException("renamer cannot be null");
		this.pipeDef = pipeDef;
		this.renamer = renamer;
		Map<String, String> inverse = new HashMap<String, String>();
		for (Entry<String, String> entry : renamer.entrySet()) {
			if (inverse.containsKey(entry.getValue()))
				throw new IllegalArgumentException(
						"You cannot rename two arguments to the same name");
			inverse.put(entry.getValue(), entry.getKey());
		}
		this.inverseRenamer = Collections.unmodifiableMap(inverse);
	}

	@Override
	public PipeDefinition join(PipeDefinition rightPart)
			throws IncompatibleContraintsException {
		return new CompositedPipeDefinition(this, rightPart);
	}

	@Override
	public int length() {
		return pipeDef.length();
	}

	@Override
	protected Executable instantiate_(Map<String, Object> args) {
		return pipeDef.instantiate_(rename(renamer, args));
	}

	private static <T> Map<String, T> rename(Map<String, String> renamer,
			Map<String, T> args) {
		Map<String, T> map = new HashMap<String, T>();
		Set<Entry<String, T>> entrys = args.entrySet();
		for (Entry<String, T> entry : entrys) {
			String newKeyName = renamer.get(entry.getKey());
			map.put(newKeyName == null ? entry.getKey() : newKeyName, entry
					.getValue());
		}
		return map;
	}

	@Override
	public Map<String, Class<?>> getArgumentsSpecification() {
		return rename(inverseRenamer, pipeDef.getArgumentsSpecification());
	}

	@Override
	public int totalLength() {
		return pipeDef.totalLength();
	}

}
