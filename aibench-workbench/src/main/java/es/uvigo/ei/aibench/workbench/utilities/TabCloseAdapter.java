/*
 * #%L
 * The AIBench Workbench Plugin
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
package es.uvigo.ei.aibench.workbench.utilities;

/**
 * @author Daniel Glez-Peña
 *
 */
public class TabCloseAdapter implements TabCloseListener {
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.utilities.TabCloseListener#tabClosed(es.uvigo.ei.aibench.workbench.utilities.TabCloseEvent)
	 */
	@Override
	public void tabClosed(TabCloseEvent event) {
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.utilities.TabCloseListener#tabClosing(es.uvigo.ei.aibench.workbench.utilities.TabCloseEvent)
	 */
	@Override
	public void tabClosing(TabCloseEvent event) {
	}
}
