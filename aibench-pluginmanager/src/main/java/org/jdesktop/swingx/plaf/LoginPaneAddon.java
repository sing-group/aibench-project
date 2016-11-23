/*
 * #%L
 * The AIBench Plugin Manager Plugin
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
/*
 * $Id: LoginPaneAddon.java,v 1.1 2009-04-13 22:17:51 mrjato Exp $
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
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.jdesktop.swingx.JXLoginPane;

/**
 *
 * @author rbair
 */
public class LoginPaneAddon extends AbstractComponentAddon {

    /** Creates a new instance of LoginPaneAddon */
    public LoginPaneAddon() {
        super("JXLoginPane");
    }

  @Override
  protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addBasicDefaults(addon, defaults);
    Color errorBG = new Color(255, 215, 215);

    defaults.add(JXLoginPane.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicLoginPaneUI");
    defaults.add("JXLoginPane.errorIcon",
            LookAndFeel.makeIcon(LoginPaneAddon.class, "basic/resources/error16.png"));
    defaults.add("JXLoginPane.bannerFont", new FontUIResource("Arial Bold", Font.PLAIN, 36));
    //#911 Not every LAF has Label.font defined ...
    Font labelFont = UIManager.getFont("Label.font");
    Font boldLabel = labelFont != null ? labelFont.deriveFont(Font.BOLD) : new Font("SansSerif", Font.BOLD, 12);
    defaults.add("JXLoginPane.pleaseWaitFont",
            new FontUIResource(boldLabel));
    defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
    defaults.add("JXLoginPane.bannerDarkBackground", new ColorUIResource(Color.GRAY));
    defaults.add("JXLoginPane.bannerLightBackground", new ColorUIResource(Color.LIGHT_GRAY));
    defaults.add("JXLoginPane.errorBackground", new ColorUIResource(errorBG));
    defaults.add("JXLoginPane.errorBorder",
            new BorderUIResource(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 36, 0, 11),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.GRAY.darker()),
                            BorderFactory.createMatteBorder(5, 7, 5, 5, errorBG)))));

    UIManagerExt.addResourceBundle(
        "org.jdesktop.swingx.plaf.basic.resources.LoginPane");
  }

  @Override
  protected void addMetalDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addMetalDefaults(addon, defaults);

    if (isPlastic()) {
      defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
      defaults.add("JXLoginPane.bannerDarkBackground", new ColorUIResource(Color.GRAY));
      defaults.add("JXLoginPane.bannerLightBackground", new ColorUIResource(Color.LIGHT_GRAY));
    } else {
        defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
        defaults.add("JXLoginPane.bannerDarkBackground",
                MetalLookAndFeel.getCurrentTheme().getPrimaryControlDarkShadow());
        defaults.add("JXLoginPane.bannerLightBackground",
                MetalLookAndFeel.getCurrentTheme().getPrimaryControl());
    }
  }

  @Override
  protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addWindowsDefaults(addon, defaults);

    defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
    defaults.add("JXLoginPane.bannerDarkBackground", new ColorUIResource(49, 121, 242));
    defaults.add("JXLoginPane.bannerLightBackground", new ColorUIResource(198, 211, 247));
  }
}
