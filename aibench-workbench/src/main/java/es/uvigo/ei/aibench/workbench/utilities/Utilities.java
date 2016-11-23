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
package es.uvigo.ei.aibench.workbench.utilities;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.filechooser.FileFilter;

import es.uvigo.ei.aibench.workbench.OperationWrapper;
import es.uvigo.ei.aibench.workbench.Workbench;

public class Utilities {	
	public static Frame getParentFrame(Component component) {
		if (component == null) {
			return null;
		} else {
			Component parent = component.getParent();
			while (parent != null && !(parent instanceof Frame)) {
				parent = parent.getParent();
			}
			return (Frame) parent;
		}
	}

	public static void centerOnWindow(Component component) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle pantalla = ge.getMaximumWindowBounds();
		Dimension componentDimension = component.getPreferredSize();

		int x = (pantalla.width - (int) componentDimension.getWidth()) / 2;
		int y = (pantalla.height - (int) componentDimension.getHeight()) / 2;

		component.setLocation(x, y);
	}

	public static void centerOnOwner(Component component) {
		Rectangle rectOwner;
		Rectangle rectDialog;

		rectOwner = component.getParent().getBounds();
		rectDialog = component.getBounds();

		component.setLocation((rectOwner.x + (rectOwner.width / 2)) - (rectDialog.width / 2), (rectOwner.y + (rectOwner.height / 2)) - (rectDialog.height / 2));

	}

	public static File getFile(FileFilter filter) {
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));

		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
	
	public static void putOperationInMenu(MenuElement root, OperationWrapper opw){
		String path = opw.getOperationDefinition().getPath();
		if (path==null || path.equals("")) return;

		Object[] res = getOperationMenu(root, path);

		constructMenu((JComponent)res[0], (String) res[1], opw);


	}
//	Return the JMenu most closer to the path and the piece of the String not matched
	private static Object[] getOperationMenu(MenuElement currentMenu, String path){


		if (path == null || path.equals("")) return new Object[]{currentMenu, path};

		ArrayList<JMenu> menus = new ArrayList<JMenu>();

		//populate menus
		if ((Object) currentMenu instanceof JMenuBar){
			JMenuBar bar = (JMenuBar) currentMenu;
			int count = bar.getMenuCount();

			for (int i = 0; i < count ; i++ ){
				menus.add(bar.getMenu(i));
			}
		}
		else if( (Object) currentMenu instanceof JPopupMenu){
			JPopupMenu bar = (JPopupMenu) currentMenu;
			int count = bar.getSubElements().length;

			for (int i = 0; i < count ; i++ ){
				if ((JMenu)bar.getSubElements()[i] instanceof JMenu){
					menus.add((JMenu)bar.getSubElements()[i]);
				}
			}
		}
		else{
			// is a instance of JMenu
			JMenu menu = (JMenu) currentMenu;
			int count = menu.getMenuComponentCount();

			for (int i = 0; i < count ; i++ ){
				Object comp = menu.getMenuComponent(i);
				if (comp instanceof JMenu){
					menus.add((JMenu) comp);
				}
			}
		}

		//get the token
		StringTokenizer tk = new StringTokenizer(path, "/");
		String pathElement=null;
		if (tk.hasMoreTokens()){
			pathElement = tk.nextToken();

			for (int i = 0; i < menus.size(); i++){
				

				//throwing out @ char
				String noAt = pathElement;
				if (pathElement.indexOf("@")!=-1){
					noAt=pathElement.substring(pathElement.indexOf("@")+1);
				}
				if (menus.get(i).getText()!=null && menus.get(i).getText().equals(noAt)){
					if (tk.hasMoreTokens()){
						return getOperationMenu(menus.get(i), path.substring(pathElement.length()+1));
					}else{
						return getOperationMenu(menus.get(i), "");
					}
				}
			}
		}
		return new Object[]{currentMenu, path};
	}
//	Makes a path of menus and submenus based on the path parameters. Finally it inserts a menu item
	//for the opw operation
	private static int MAX_MENUITEMS =(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/25;
	private static void constructMenu(JComponent root, String path, OperationWrapper opw){
		StringTokenizer tk = new StringTokenizer(path, "/");
		String pathElement=null;

		while (tk.hasMoreTokens()){
			pathElement = tk.nextToken();

			String noAt = pathElement;
			if (pathElement.indexOf("@")!=-1){
				noAt=pathElement.substring(pathElement.indexOf("@")+1);
			}
			JMenu child = new JMenu(noAt);

			if (root instanceof JMenuBar) {
				((JMenuBar) root).add(child);
			} else if (root instanceof JPopupMenu) {
				((JPopupMenu) root).add(child);
			}
			else{
				JMenu rootMenu = (JMenu) root;

				rootMenu.insert(child, ((JMenu)root).getItemCount());
				if(rootMenu.getItemCount() > MAX_MENUITEMS){
					GridLayout menuGrid = new GridLayout(/*MAX_MENUITEMS*/rootMenu.getItemCount()/((rootMenu.getItemCount() / MAX_MENUITEMS)+1)+1,rootMenu.getItemCount() / (MAX_MENUITEMS+1));
					rootMenu.getPopupMenu().setLayout(menuGrid);
				}
			}
			root = child;
		}

		JMenuItem item = new JMenuItem(opw);
		
		/**
		 * NOTE: modified by paulo maia
		 */
		Icon icon = Workbench.getInstance().getOperationIcon(opw.getOperationDefinition());
		if(icon!=null){
			item.setIcon(icon);
		}
		//end
		
		root.add(item);
		if (! (root instanceof JMenuBar)){

//			Layout for large menus
			JMenu rootMenu = (JMenu) root;

			if(rootMenu.getItemCount() > MAX_MENUITEMS){
				
				GridLayout menuGrid = new GridLayout(/*MAX_MENUITEMS*/rootMenu.getItemCount()/((rootMenu.getItemCount() / MAX_MENUITEMS)+1)+1,rootMenu.getItemCount() / (MAX_MENUITEMS+1));
				//GridLayout menuGrid = new GridLayout(MAX_MENUITEMS,/*rootMenu.getItemCount() / MAX_MENUITEMS*/0);
				rootMenu.getPopupMenu().setLayout(menuGrid);
			}

		} else {
		}
	}
}
