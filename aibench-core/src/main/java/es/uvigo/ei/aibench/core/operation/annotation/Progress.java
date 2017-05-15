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
package es.uvigo.ei.aibench.core.operation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines a method whose return parameter is a POJO that
 * monitors a progress
 * 
 * @author Daniel Glez-Peña
 * @author Hugo López-Fernández
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Progress {
	public static final String DEFAULT_DIALOG_TITLE = "Progress...";
	public static final boolean DEFAULT_DIALOG_MODAL = false;
	public static final String DEFAULT_WORKING_LABEL = "Working";
	public static final int DEFAULT_PREFERRED_SIZE = Integer.MIN_VALUE;

	String progressDialogTitle() default DEFAULT_DIALOG_TITLE;

	boolean modal() default DEFAULT_DIALOG_MODAL;

	String workingLabel() default DEFAULT_WORKING_LABEL;

	int preferredWidth() default DEFAULT_PREFERRED_SIZE;

	int preferredHeight() default DEFAULT_PREFERRED_SIZE;
}
