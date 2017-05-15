/*
 * #%L
 * The AIBench Plugin Manager Plugin
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
/*
 * $Id: TargetableSupport.java,v 1.1 2009-04-13 22:17:50 mrjato Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdesktop.swingx.action;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;

/**
 *
 * @author rbair
 */
public class TargetableSupport {
    private JComponent component;
    
    /** Creates a new instance of TargetableSupport */
    public TargetableSupport(JComponent component) {
        this.component = component;
    }
    
    public boolean doCommand(Object command, Object value) {
        // Look at the internal component first.
        ActionMap map = component.getActionMap();
        Action action = map.get(command);

        if (action != null) {
            if (value instanceof ActionEvent) {
                action.actionPerformed( (ActionEvent) value);
            }
            else {
                // XXX should the value represent the event source?
                action.actionPerformed(new ActionEvent(value, 0,
                    command.toString()));
            }
            return true;
        }
        return false;
    }

    public Object[] getCommands() {
        ActionMap map = component.getActionMap();
        return map.allKeys();
    }

    public boolean hasCommand(Object command) {
        Object[] commands = getCommands();
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].equals(command)) {
                return true;
            }
        }
        return false;
    }
}
