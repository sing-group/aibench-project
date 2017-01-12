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
package es.uvigo.ei.pipespecification.storage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.w3c.dom.Element;

import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.IncompatibleConstraintsException;
import es.uvigo.ei.pipespecification.InvalidAnnotationsException;

class StandardPipeDefinition<T> extends PipeDefinition {

	private static final String[] emptyArgsNames = {};

	private static final Class<?>[] emptyTypes = {};

	private final Constructor<T> constructor;

	private final String[] argsNames;

	private final Class<?>[] classes;

	private Map<String, Class<?>> argumentsSpecification = null;

	private final OperationDefinition<T> metadata;

	private final ExecutorService executorService;

	StandardPipeDefinition(Class<T> klass, ExecutorService executor)
			throws InvalidAnnotationsException {
		this(klass, OperationDefinition.createOperationDefinition(klass), emptyArgsNames, emptyTypes,
				executor);
	}

	StandardPipeDefinition(Class<T> klass, String[] argsNames, Class<?>[] types,
			ExecutorService executor) throws InvalidAnnotationsException {
		this(klass, OperationDefinition.createOperationDefinition(klass), argsNames, types, executor);
	}

	StandardPipeDefinition(Class<T> klass, List<Element> ports,
			String[] argsNames, Class<?>[] types, ExecutorService executor) {
		this(klass, OperationDefinition.createOperationDefinition(klass, ports), argsNames, types,
				executor);
	}

	StandardPipeDefinition(Class<T> klass, OperationDefinition<T> metadata,
			String[] argsNames, Class<?>[] types, ExecutorService executor) {
		super(metadata.getIncomingArgumentTypes(), metadata
				.getOutcomingArgumentTypes());
		if (executor == null)
			throw new NullPointerException("executor can't be null");
		if (argsNames.length != types.length)
			throw new IllegalArgumentException(
					"argsNames and classes must have the same length");
		try {
			Constructor<T> constructor = klass.getConstructor(types);
			if (!Modifier.isPublic(constructor.getModifiers()))
				throw new IllegalArgumentException("The constructor"
						+ constructor + "must be public");
			this.argsNames = argsNames;
			this.classes = types;
			this.constructor = constructor;
			this.metadata = metadata;
			this.executorService = executor;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"There is no constructor for the types specified in the class "
							+ klass);
		}

	}

	protected @Override
	Executable instantiate_(Map<String, Object> args) {
		Object[] values = new Object[argsNames.length];
		for (int i = 0; i < argsNames.length; i++) {
			final String paramName = argsNames[i];
			if (args.get(paramName) == null)
				throw new IllegalArgumentException(
						"There is no value for the parameter " + paramName);
			values[i] = args.get(paramName);
		}
		for (int i = 0; i < values.length; i++) {
			if (!classes[i].isAssignableFrom(values[i].getClass())) {
				throw new IllegalArgumentException("The argument "
						+ argsNames[i] + " is not of the class required: "
						+ classes[i]);
			}
		}
		try {
			return metadata
					.makeExecutable(constructor.newInstance((Object[])constructor.getParameterTypes()), executorService, values);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected execption calling "
					+ constructor.toString() + " with " + Arrays.asList(values));
		}
	}

	@Override
	public Map<String, Class<?>> getArgumentsSpecification() {
		if (argumentsSpecification == null) {
			argumentsSpecification = new HashMap<String, Class<?>>();
			assert argsNames.length == classes.length;
			for (int i = 0; i < argsNames.length; i++) {
				argumentsSpecification.put(argsNames[i], classes[i]);
			}
			argumentsSpecification = Collections
					.unmodifiableMap(argumentsSpecification);
		}
		return argumentsSpecification;
	}

	@Override
	public PipeDefinition join(PipeDefinition rightPart)
			throws IncompatibleConstraintsException {
		return new CompositedPipeDefinition(this, rightPart);
	}

	@Override
	public int length() {
		return 1;
	}

	@Override
	public int totalLength() {
		return 1;
	}
}
