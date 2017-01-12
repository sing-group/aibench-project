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
package es.uvigo.ei.aibench.core.operation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Port {
/*	Class[] args() default {};*/

	Direction direction() default Direction.BOTH;

	ResultTreatment resultTreatment() default ResultTreatment.ELEMENT;

	/**
	 * Ignored unless {@code resultTreatment} is {@code DataSource} and
	 * the return type is not an array.
	 *
	 * @return the class of the results. 
	 */
	Class<?> resultClass() default Object.class;

	String name() default "";

	String description() default "";

	/**
	 * The ports will be invoked in the order specified by {@code order}.
	 * 
	 * @return the position in the port's invocation order.
	 */
	int order() default -1;

	String defaultValue() default "";

	String validateMethod() default "";
	
	boolean allowNull() default false;
	
	boolean lock() default false;
	
	String extras() default "";

	boolean advanced() default false;
}
