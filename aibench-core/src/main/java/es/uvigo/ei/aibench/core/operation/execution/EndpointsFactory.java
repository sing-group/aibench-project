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
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.ResultTreatment;
import es.uvigo.ei.pipespecification.InvalidAnnotationsException;

public class EndpointsFactory<T> {

	private final Direction direction;

	private final ResultTreatment treatment;

	private final java.lang.reflect.Method targetMethod;

	private final Class<T> klass;

	private final Class<?> resultKlass;

	private final Class<?> argsClass;

	private static final Map<Class<?>, Class<?>> WRAPPERS = new HashMap<Class<?>, Class<?>>();

	static {
		WRAPPERS.put(byte.class, Byte.class);
		WRAPPERS.put(short.class, Short.class);
		WRAPPERS.put(char.class, Character.class);
		WRAPPERS.put(int.class, Integer.class);
		WRAPPERS.put(long.class, Long.class);
		WRAPPERS.put(float.class, Float.class);
		WRAPPERS.put(double.class, Double.class);
		WRAPPERS.put(boolean.class, Boolean.class);
	}

	private Class<?> toWrapper(Class<?> klass) {
		if (WRAPPERS.containsKey(klass)) {
			return WRAPPERS.get(klass);
		} else
			return klass;
	}

	private EndpointsFactory(Class<T> klass, Class<?>[] args, String methodName,
			Direction direction, ResultTreatment resultTreatment,
			Class<?> resultClass) throws InvalidAnnotationsException,
			IllegalArgumentException {

		if (klass == null)
			throw new IllegalArgumentException("klass can't be null");
		if (args.length > 1)
			throw new IllegalArgumentException(
					"the method can have at most one argument");
		if (methodName.length() == 0)
			throw new IllegalArgumentException(
					"methodName must not be the empty string");
		if (direction == null)
			throw new InvalidAnnotationsException(
					"The visibility specified can't be null");
		if (resultTreatment == null)
			throw new InvalidAnnotationsException("The treatment can't be null");
		if (resultClass == null)
			throw new NullPointerException("resultClass can't be null");
		this.klass = klass;
		try {
			targetMethod = klass.getMethod(methodName, args);
		} catch (Exception e) {
			throw new InvalidAnnotationsException(
					"The method specified doesn't exist in the class");
		}
		assert targetMethod != null;
		this.argsClass = targetMethod.getParameterTypes().length > 0 ? targetMethod
				.getParameterTypes()[0]
				: Void.TYPE;
		this.direction = direction;
		if (direction == Direction.OUTPUT
				&& targetMethod.getParameterTypes().length > 0)
			throw new InvalidAnnotationsException(
					"An output method cannot have arguments");
		this.treatment = resultTreatment;
		Class<?> targetMethodReturnType = toWrapper(targetMethod
				.getReturnType());
		if (treatment == ResultTreatment.DATASOURCE) {
			if (!isDataSourceCompatible(targetMethodReturnType))
				throw new InvalidAnnotationsException("The return type "
						+ targetMethodReturnType
						+ " is incompatible with being a dataSource");
			this.resultKlass = targetMethodReturnType.getComponentType() == null ? resultClass
					: targetMethodReturnType.getComponentType();
		} else
			this.resultKlass = targetMethodReturnType;

	}

	private static boolean isDataSourceCompatible(Class<?> resultKlass) {
		return Iterable.class.isAssignableFrom(resultKlass)
				|| Iterator.class.isAssignableFrom(resultKlass)
				|| resultKlass.isArray();
	}

	public static <T> EndpointsFactory<T> createEndpointsFactory(
			Class<T> klass, Port port, Class<?>[] args, String methodName) throws InvalidAnnotationsException,
			IllegalArgumentException {
		if (port == null)
			throw new IllegalArgumentException("port can't be null");

		return new EndpointsFactory<T>(klass, args,  methodName,
				port.direction(), port.resultTreatment(), port.resultClass());
	}

	public static <T> EndpointsFactory<T> createEndPointsFactory(
			Class<T> klass, final Element portElement) {
		if (portElement.getLocalName().equals("port")) {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				xPath.setNamespaceContext(new NamespaceContext() {
					public String getNamespaceURI(String prefix) {
						return portElement.getNamespaceURI();
					}

					public String getPrefix(String namespaceURI) {
						return null;
					}

					public Iterator<?> getPrefixes(String namespaceURI) {
						return null;
					}
				});
				String methodName = xPath
						.evaluate("p:method_name", portElement);
				Class<?> resultClass = Class.forName(xPath.evaluate(
						"@result_class", portElement));
				ResultTreatment resultTreatment = ResultTreatment
						.valueOf(portElement.getAttribute("result_treatment")
								.toUpperCase());
				Direction direction = Direction.valueOf(portElement
						.getAttribute("direction").toUpperCase());
				Class<?>[] args = loadClasses(xPath
						.evaluate("p:args", portElement).split("\\s+"));
				return new EndpointsFactory<T>(klass, args, methodName,
						direction, resultTreatment, resultClass);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else
			throw new IllegalArgumentException("it's necessary a port element");
	}

	private static Class<?>[] loadClasses(String[] strings)
			throws ClassNotFoundException {
		Class<?>[] classes = new Class[strings.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = Class.forName(strings[i]);
		}
		return classes;
	}

	SimpleIncomingEndPoint instantiate(T target) {
		if (!klass.isInstance(target))
			throw new IllegalArgumentException(
					"The target object is not of the target class");
		if(direction==Direction.OUTPUT) return new OneCallEndPoint(targetMethod,target);
		return new SimpleIncomingEndPoint(targetMethod, target);

	}

	public void addArguments(List<Class<?>> incomeArguments, List<Class<?>> outcomeArguments) {
		if (direction != Direction.OUTPUT) {
			incomeArguments.add(argsClass);
		}
		if (direction != Direction.INPUT)
			outcomeArguments.add(resultKlass);
	}

	protected final Direction getDirection() {
		return direction;
	}

	protected final ResultTreatment getTreatment() {
		return treatment;
	}

}
