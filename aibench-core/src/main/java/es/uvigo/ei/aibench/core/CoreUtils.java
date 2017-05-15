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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

/**
 * @author Daniel Glez-Peña
 * 
 */
public final class CoreUtils {
	private static final Logger LOGGER = Logger.getLogger(CoreUtils.class.getName());
	// TODO �abstraer todos los createParams() en uno solo??
	
	public final static Comparator<Port> PORT_COMPARATOR = new Comparator<Port>() {
		@Override
		public int compare(Port o1, Port o2) {
			final int cmpOrder = o1.order() - o2.order();
			
			if (cmpOrder == 0) {
				if (o1.direction() == o2.direction()) {
					final int cmpName = o1.name().compareTo(o2.name());
					
					if (cmpName == 0) {
						return o1.description().compareTo(o2.description());
					} else {
						return cmpName;
					}
				} else {
					return (o1.direction() == Direction.INPUT)?-1:1; // INPUT before OUTPUT
				}
			} else {
				return cmpOrder;
			}
		}
	};
	
	private static final Class<?>[] WRAPPER_CLASSES = new Class<?>[] { 
		Character.class,
		Boolean.class, 
		Byte.class, Short.class, Integer.class, Long.class, 
		Float.class, Double.class 
	};
	
	private final static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return true;
		} else {
			for (Class<?> wrapper : CoreUtils.WRAPPER_CLASSES) {
				if (wrapper.equals(clazz))
					return true;
			}
			
			return false;
		}
	}
	
	private final static Object stringToPrimitive(Class<?> type, String value) 
	throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
		if (value == null)
			throw new NullPointerException("value can't be null");
		if (!(type.isPrimitive() || Arrays.asList(WRAPPER_CLASSES).contains(type)))
			throw new IllegalArgumentException("type must be primitive or wrapper class");
		
		if (type.equals(char.class) || type.equals(Character.class)) {
			return value.charAt(0);
		} else {
			for (Class<?> wrapperClass : WRAPPER_CLASSES) {
				final Class<?> fieldType = (Class<?>) wrapperClass.getField("TYPE").get(wrapperClass);
				if (type.equals(wrapperClass) || fieldType.equals(type)) {
					final String methodName = "parse" + Character.toUpperCase(fieldType.getName().charAt(0)) + fieldType.getName().substring(1);
					
					return wrapperClass.getMethod(methodName, String.class).invoke(null, value);
				}
			}
			
			throw new IllegalArgumentException("type must be a primitive");
		}
	}
	
	public static void setPortValue(Object operation, Method method, String value) 
	throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException, NoSuchMethodException, InstantiationException {
		final Port port = method.getAnnotation(Port.class);
		if (port != null && port.direction() == Direction.INPUT 
			&& method.getParameterTypes().length == 1
		) {
			if (value == null) {
				if (port.allowNull())
					method.invoke(operation, (Object) null);
				else
					throw new NullPointerException(port.name() + " does not allow null values");
			} else {
				final Class<?> parameterType = method.getParameterTypes()[0];
				
				final Object parameterValue;
				if (CoreUtils.isPrimitiveOrWrapper(parameterType)) {
					parameterValue = CoreUtils.stringToPrimitive(parameterType, value);
				} else if (parameterType.isEnum()) {
					final Method valueOf = parameterType.getMethod("valueOf", String.class);
					parameterValue = valueOf.invoke(null, value); 
				} else {
					final Constructor<?> constructor = parameterType.getConstructor(String.class);
					parameterValue = constructor.newInstance(value);
				}
				
				method.invoke(operation, parameterValue);
			}
		} else {
			throw new IllegalArgumentException("method is not an input port");
		}
	}
	
	public static void setDefaultPortValues(Object operation) 
	throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException, NoSuchMethodException, InstantiationException {
		CoreUtils.setDefaultPortValues(operation, false);
	}
	
	public static void setDefaultPortValues(Object operation, boolean setEmptyDefaultValues) 
	throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException, NoSuchMethodException, InstantiationException {
		final Class<?> clazz = operation.getClass();
		
		
		final SortedMap<Port, Method> inPorts = 
			new TreeMap<Port, Method>(CoreUtils.PORT_COMPARATOR);
		
		for (Method method : clazz.getMethods()) {
			final Port port = method.getAnnotation(Port.class);
			
			if (port != null && method.getParameterTypes().length == 1) {
				inPorts.put(port, method);
			}
		}
		
		for (Map.Entry<Port, Method> entry : inPorts.entrySet()) {
			final Port port = entry.getKey();
			final Method method = entry.getValue();
			
			
			if (port.defaultValue().equals("")) {
				if (setEmptyDefaultValues)
					CoreUtils.setPortValue(operation, method, port.defaultValue());					
			} else {
				CoreUtils.setPortValue(operation, method, port.defaultValue());				
			}
		}
	}

	public static ParamSpec[] createParams(List<?> params) {
		ParamSpec[] paramsSpec = new ParamSpec[params.size()];
		for (int p = 0; p < params.size(); p++)
			paramsSpec[p] = createParam(params.get(p));
		return paramsSpec;
	}

	private static ParamSpec[] createParams(Object[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(array[i]);
		return specArray;
	}

	private static ParamSpec[] createParams(int[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Integer(array[i]));
		return specArray;
	}

	private static ParamSpec[] createParams(boolean[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Boolean(array[i]));
		return specArray;
	}

	private static ParamSpec[] createParams(char[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Character(array[i]));
		return specArray;
	}

	private static ParamSpec[] createParams(double[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Double(array[i]));
		return specArray;
	}

	private static ParamSpec[] createParams(float[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Float(array[i]));
		return specArray;
	}

	private static ParamSpec[] createParams(long[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Long(array[i]));
		return specArray;
	}

	private static ParamSpec[] createParams(short[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Short(array[i]));
		return specArray;
	}

	private static ParamSpec[] createParams(byte[] array) {
		ParamSpec[] specArray = new ParamSpec[array.length];
		for (int i = 0; i < specArray.length; i++)
			specArray[i] = createParam(new Byte(array[i]));
		return specArray;
	}

	private static ParamSpec createParam(Object param) {
		if (param==null) return new ParamSpec("null", Object.class, null, ParamSource.CLIPBOARD);
		Class<?> clazz = param.getClass();
		
		if (clazz.isEnum()){
			return new ParamSpec("enum", clazz, param, ParamSource.ENUM);
			
		}
		else if (clazz.isArray()) {
			// Primero hay que descartar los tipos b�sicos para que no pete el
			// cast a Object[]
			if (clazz.equals((new int[0]).getClass()))
				return new ParamSpec("array", Integer[].class,
						createParams((int[]) param));
			if (clazz.equals((new byte[0]).getClass()))
				return new ParamSpec("array", Byte[].class,
						createParams((byte[]) param));
			if (clazz.equals((new short[0]).getClass()))
				return new ParamSpec("array", Short[].class,
						createParams((short[]) param));
			if (clazz.equals((new long[0]).getClass()))
				return new ParamSpec("array", Long[].class,
						createParams((long[]) param));
			if (clazz.equals((new float[0]).getClass()))
				return new ParamSpec("array", Float[].class,
						createParams((float[]) param));
			if (clazz.equals((new double[0]).getClass()))
				return new ParamSpec("array", Double[].class,
						createParams((double[]) param));
			if (clazz.equals((new char[0]).getClass()))
				return new ParamSpec("array", Character[].class,
						createParams((char[]) param));
			if (clazz.equals((new boolean[0]).getClass()))
				return new ParamSpec("array", Boolean[].class,
						createParams((boolean[]) param));
			return new ParamSpec("array", clazz, createParams((Object[]) param));
		} else {
			// Si no es array lo buscamos en el clipboard
			ClipboardItem item = Core.getInstance().getClipboard().getClipboardItem(param);
			if (item != null) {
				return new ParamSpec("item", clazz, item, ParamSource.CLIPBOARD);
			} else {
				// Si no, compruebo los tipos b�sicos...
				if (clazz.equals(Integer.class))
					return new ParamSpec("integer", Integer.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(String.class))
					return new ParamSpec("string", String.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(Byte.class))
					return new ParamSpec("byte", Byte.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(Short.class))
					return new ParamSpec("short", Short.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(Long.class))
					return new ParamSpec("long", Long.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(Float.class))
					return new ParamSpec("float", Float.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(Double.class))
					return new ParamSpec("double", Double.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(Character.class))
					return new ParamSpec("char", Character.class, param,
							ParamSource.STRING_CONSTRUCTOR);
				if (clazz.equals(Boolean.class))
					return new ParamSpec("boolean", Boolean.class, param,
							ParamSource.STRING_CONSTRUCTOR);

				if (Serializable.class.isAssignableFrom(clazz)) {
					return new ParamSpec("param", clazz, Base64Coder
							.encodeSerializableObject((Serializable) param),
							ParamSource.SERIALIZED);
				}
			}
		}
		return null;
	}
	
	public static boolean isValidURL(String url) {
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	public static void openURL(String url) throws Exception {
		try {
			String osName = System.getProperty("os.name");
			if (osName.startsWith("Mac OS")) {
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec("rundll32 url.dll, FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0) {
						browser = browsers[count];
					}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[] { browser, url });
				}
			}
		} catch (Exception e) {
			LOGGER.warn("The url '" + url + "' could not be opened", e);
			throw e;
		}
	}
}
