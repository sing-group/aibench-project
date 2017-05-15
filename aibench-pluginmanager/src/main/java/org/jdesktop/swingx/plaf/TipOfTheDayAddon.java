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
 * $Id: TipOfTheDayAddon.java,v 1.1 2009-04-13 22:17:51 mrjato Exp $
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
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.LookAndFeel;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.swingx.JXTipOfTheDay;
import org.jdesktop.swingx.plaf.basic.BasicTipOfTheDayUI;
import org.jdesktop.swingx.plaf.windows.WindowsTipOfTheDayUI;

/**
 * Addon for <code>JXTipOfTheDay</code>.<br>
 * 
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class TipOfTheDayAddon extends AbstractComponentAddon {

  public TipOfTheDayAddon() {
    super("JXTipOfTheDay");
  }

  @Override
  protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
      super.addBasicDefaults(addon, defaults);
      
      Font font = UIManagerExt.getSafeFont("Label.font", new Font("Dialog", Font.PLAIN, 12));
      font = font.deriveFont(Font.BOLD, 13f);
      
      defaults.add(JXTipOfTheDay.uiClassID, BasicTipOfTheDayUI.class.getName());
      defaults.add("TipOfTheDay.font", UIManagerExt.getSafeFont("TextPane.font",
                new FontUIResource("Serif", Font.PLAIN, 12)));
      defaults.add("TipOfTheDay.tipFont", new FontUIResource(font));
      defaults.add("TipOfTheDay.background", new ColorUIResource(Color.WHITE));
      defaults.add("TipOfTheDay.icon",
              LookAndFeel.makeIcon(BasicTipOfTheDayUI.class, "resources/TipOfTheDay24.gif"));
      defaults.add("TipOfTheDay.border", new BorderUIResource(
              BorderFactory.createLineBorder(new Color(117, 117, 117))));

    UIManagerExt.addResourceBundle(
            "org.jdesktop.swingx.plaf.basic.resources.TipOfTheDay");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addWindowsDefaults(addon, defaults);

    Font font = UIManagerExt.getSafeFont("Label.font",
            new Font("Dialog", Font.PLAIN, 12));
    font = font.deriveFont(13f);
    
    defaults.add(JXTipOfTheDay.uiClassID, WindowsTipOfTheDayUI.class.getName());
    defaults.add("TipOfTheDay.background", new ColorUIResource(Color.GRAY));
    defaults.add("TipOfTheDay.font", new FontUIResource(font));
    defaults.add("TipOfTheDay.icon",
            LookAndFeel.makeIcon(WindowsTipOfTheDayUI.class, "resources/tipoftheday.png"));
    defaults.add("TipOfTheDay.border" ,new BorderUIResource(new WindowsTipOfTheDayUI.TipAreaBorder()));

    UIManagerExt.addResourceBundle(
        "org.jdesktop.swingx.plaf.windows.resources.TipOfTheDay");
  }

}
