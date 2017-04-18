/*
 * #%L
 * The AIBench Workbench Plugin
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
package es.uvigo.ei.aibench.workbench.inputgui;

import java.io.File;

import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;

/**
 * This class instances an appropriate {@code ParamProvider} implementation
 * based on the required class of the {@code port}.
 * 
 * @author Daniel Glez-Peña
 * @author Hugo López-Fernández
 *
 */
public class ParamProviderFactory {
	public static ParamProvider createParamProvider(ParamsReceiver receiver, Port port, Class<?> clazz,
			Object operationObject) {
		if (clazz.isArray()) {
			return new ArrayParamProvider(receiver, port, clazz, operationObject);
		} else if (clazz.isPrimitive()) {
			return new PrimitiveParamProvider(receiver, port, clazz, operationObject);
		} else if (clazz.getEnumConstants() != null) {
			return new EnumParamProvider(receiver, port, clazz, operationObject);
		} else if (File.class.isAssignableFrom(clazz)) {
			return new FileParamProvider(receiver, port, clazz, operationObject);
		} else {
			return new ClipboardParamProvider(receiver, port, clazz, operationObject);
		}
	}
}
