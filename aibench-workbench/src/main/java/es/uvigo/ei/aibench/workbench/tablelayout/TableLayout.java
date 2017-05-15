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
package es.uvigo.ei.aibench.workbench.tablelayout;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Daniel Glez-Peña
 *
 */
public class TableLayout extends JPanel{
	private final static String PROPERTY_SPLIT_LOCATION = "split location";
	private final static String PROPERTY_SPLIT_LOCATION_TYPE = "split location type";
	private enum SplitLocationType { PERCENTAGE, ABSOLUTE, HEIGHT_RELATIVE, WIDTH_RELATIVE };
	
	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 1L;

	private Document doc;
	
	
	private final HashMap<String, Slot> idmappings = new HashMap<String, Slot>();
	private final List<JSplitPane> splitPanes = new Vector<JSplitPane>();
	
	public List<String> getAvailableSlots(){
		List<String> toret = new ArrayList<String>();
		for (String s :idmappings.keySet()){
			toret.add(s);
		}
		return toret;
	}
	
	public Slot getSlotByID(String id){
		return idmappings.get(id);
	}
	
	private JPanel documentViewer=null;
	private boolean hideTabs=false;
	public boolean isHideTabs() {
		return this.hideTabs;
	}

	public void setHideTabs(boolean hideTabs) {
		this.hideTabs = hideTabs;
	}

	public JPanel getDocumentViewerPanel(){
		return this.documentViewer;
	}
	
	public TableLayout(InputStream stream){
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String allFile="";
		String line="";
		try {
			while ((line=reader.readLine())!=null){
				allFile+=line;
			}
			init(allFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init(String xml_description){
		// throw out spaces between elements
		xml_description = xml_description.replaceAll(">[^<>]*<", "><");
		
		// parse xml
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.doc = builder.parse(new ByteArrayInputStream(xml_description.getBytes()));
			this.setLayout(new BorderLayout());
			parseTableElement(doc.getChildNodes().item(0), this);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {	
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Constructor from string
//	 * @param xml_description the string with the XML
//	 */
//	private TableLayout(String xml_description){
//		init(xml_description);
//	}
	
	private void parseTableElement(Node n, JPanel panel) {		
		parseRowSet(n.getChildNodes(), 0, panel, this.heightSizingType(n.getChildNodes()));
	}
	
	private Number getNodeHeight(Node node) {
		Node attribute = node.getAttributes().getNamedItem("height");
		if (attribute == null) {
			return null;
		} else {
			String height = attribute.getNodeValue();
			try {
				if (height.endsWith("%")) {
					return Double.parseDouble(height.substring(0, height.length()-1))/100;					
				} else {
					return Integer.parseInt(height);
				}
			} catch(NumberFormatException nfe) {
				nfe.printStackTrace();
				return null;
			}
		}
	}
	
	private Number getNodeWidth(Node node) {
		Node attribute = node.getAttributes().getNamedItem("width");
		if (attribute == null) {
			return null;
		} else {
			String width = attribute.getNodeValue();
			try {
				if (width.endsWith("%")) {
					return Double.parseDouble(width.substring(0, width.length()-1))/100;				
				} else {
					return Integer.parseInt(width);
				}
			} catch(NumberFormatException nfe) {
				nfe.printStackTrace();
				return null;
			}
		}
	}
	
	private SplitLocationType widthSizingType(NodeList list) {
		if (list.getLength() == 0) return null;
		
		Number firstWidth = this.getNodeWidth(list.item(0));
		
		boolean percentage = (firstWidth instanceof Double), absolute = (firstWidth instanceof Integer);
		Number width;
		for (int i=1; i<list.getLength(); i++) {
			width = this.getNodeWidth(list.item(i));
			if (percentage && !(width instanceof Double)) {
				return null;
			} else if (width instanceof Double) {
				percentage = true;
			} else if (width instanceof Integer) {
				absolute = true;
			}
			
			if (percentage && absolute) return null;
		}
		
		if (percentage == absolute) {
			return null;
		} else {
			return (percentage)?SplitLocationType.PERCENTAGE:SplitLocationType.ABSOLUTE;
		}
	}
	
	private SplitLocationType heightSizingType(NodeList list) {
		if (list.getLength() == 0) return null;
		
		Number firstHeight = this.getNodeHeight(list.item(0));
		
		boolean percentage = (firstHeight instanceof Double), absolute = (firstHeight instanceof Integer);
		Number height;
		for (int i=1; i<list.getLength(); i++) {
			height = this.getNodeHeight(list.item(i));
			if (percentage && !(height instanceof Double)) {
				return null;
			} else if (height instanceof Double) {
				percentage = true;
			} else if (height instanceof Integer) {
				absolute = true;
			}
			
			if (percentage && absolute) return null;
		}
		
		if (percentage == absolute) {
			return null;
		} else {
			return (percentage)?SplitLocationType.PERCENTAGE:SplitLocationType.ABSOLUTE;
		}
	}
	
	public void packSplitters() {
		for (JSplitPane pane:this.splitPanes) {
			Object propLocation = pane.getClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION);
			Object propType = pane.getClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION_TYPE);
			if (propLocation instanceof Number && propType instanceof SplitLocationType) {
				SplitLocationType type = (SplitLocationType) propType;
				if (type == SplitLocationType.ABSOLUTE) {
					pane.setDividerLocation(((Number) propLocation).intValue() + pane.getInsets().left);
				} else if (type == SplitLocationType.PERCENTAGE) {
					pane.setDividerLocation(((Number) propLocation).doubleValue());
				} else if (type == SplitLocationType.HEIGHT_RELATIVE) {
					pane.setDividerLocation(pane.getHeight() - pane.getInsets().bottom - pane.getDividerSize() - ((Number) propLocation).intValue());
				} else if (type == SplitLocationType.WIDTH_RELATIVE) {
					pane.setDividerLocation(pane.getSize().width - pane.getInsets().right - pane.getDividerSize() - ((Number) propLocation).intValue());
				}
				double location;
				if (pane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
					location = (double) pane.getDividerLocation()/(double) (pane.getWidth() - pane.getInsets().left - pane.getInsets().right);
				} else {
					location = (double) pane.getDividerLocation()/(double) (pane.getHeight() - pane.getInsets().top - pane.getInsets().bottom);
				}
				pane.setResizeWeight(location);
				pane.repaint();
			}
		}
	}
	
	/**
	 * @param childNodes
	 * @param component
	 */
	private void parseRowSet(NodeList childNodes, int offset, JPanel panel, SplitLocationType sizing) {
		if (offset == childNodes.getLength()) return;
		
		if (childNodes.item(offset).getNodeType() == Node.TEXT_NODE){
			parseRowSet(childNodes, offset+1, panel, sizing);
		} else {
			if (offset<childNodes.getLength() - 1) {
				Node attrOneTouchExpandable = childNodes.item(offset).getAttributes().getNamedItem("oneTouchExpandable");
				boolean oneTouchExpandable = 
					attrOneTouchExpandable == null || Boolean.valueOf(attrOneTouchExpandable.getTextContent());
				
				JSplitPane splitter = new JSplitPane();
				this.splitPanes.add(splitter);
				splitter.setResizeWeight(((double) childNodes.getLength() - offset - 1)/((double) childNodes.getLength()));
				splitter.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitter.setOneTouchExpandable(oneTouchExpandable);
				
				JPanel top = new JPanel();
				top.setLayout(new BorderLayout());
				splitter.setTopComponent(top);
				
				panel.add(splitter, BorderLayout.CENTER);
				
				parseRow(childNodes.item(offset), top);
				
				JPanel bottom = new JPanel();
				bottom.setLayout(new BorderLayout());
				parseRowSet(childNodes, offset+1, bottom, sizing);
				splitter.setBottomComponent(bottom);
				
				if (sizing != null) {
					if (sizing == SplitLocationType.ABSOLUTE) {
						Number height = this.getNodeHeight(childNodes.item(offset));
						if (height != null) {
							splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION, height);
							splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION_TYPE, sizing);
						} else if (offset == childNodes.getLength() - 2) {
							height = this.getNodeHeight(childNodes.item(offset+1));
							if (height != null) {
								splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION, height);
								splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION_TYPE, SplitLocationType.HEIGHT_RELATIVE);
							}
						}
					} else if (sizing == SplitLocationType.PERCENTAGE) {
						double cumulative = 0d;
						double total = 0d;
						double height;
						for (int i=offset; i<childNodes.getLength(); i++) {
							height = this.getNodeHeight(childNodes.item(i)).doubleValue();
							if (i==offset) cumulative += height;
							total += height;
						}
						splitter.setResizeWeight(cumulative/total);
						splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION, cumulative/total);
						splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION_TYPE, sizing);
					}
				}
			} else {
				JPanel panel2 = new JPanel();
				panel2.setLayout(new BorderLayout());
				
				panel.add(panel2, BorderLayout.CENTER);
				
				parseRow(childNodes.item(offset), panel2);
			}
		}
	}

	/**
	 * @param node
	 * @param component
	 */
	private void parseRow(Node node, JPanel panel) {
		parseCellSet(node.getChildNodes(), 0, panel, this.widthSizingType(node.getChildNodes()));
	}

	/**
	 * @param childNodes
	 * @param i
	 * @param panel
	 */
	private void parseCellSet(NodeList childNodes, int offset, JPanel panel, SplitLocationType sizing) {
		
		if (offset == childNodes.getLength()){
			return;
		}
		if (childNodes.item(offset).getNodeType()==Node.TEXT_NODE){
			parseCellSet(childNodes, offset+1, panel, sizing);
		}else{
			if (offset<childNodes.getLength()-1) {
				Node attrOneTouchExpandable = childNodes.item(offset).getAttributes().getNamedItem("oneTouchExpandable");
				boolean oneTouchExpandable = (attrOneTouchExpandable == null || attrOneTouchExpandable.getTextContent().equalsIgnoreCase("true"))?true:false;
				
				JSplitPane splitter = new JSplitPane();
				this.splitPanes.add(splitter);
				splitter.setResizeWeight(((double) childNodes.getLength() - offset - 1)/((double) childNodes.getLength()));
				splitter.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitter.setOneTouchExpandable(oneTouchExpandable);

				JPanel left = new JPanel();
				left.setLayout(new BorderLayout());
				
				splitter.setLeftComponent(left);
			
				panel.add(splitter, BorderLayout.CENTER);
				
				JPanel right = new JPanel();
				right.setLayout(new BorderLayout());
				
				parseCell(childNodes.item(offset), left);
				
				parseCellSet(childNodes, offset+1, right, sizing);
				splitter.setRightComponent(right);

				if (sizing != null) {
					if (sizing == SplitLocationType.ABSOLUTE) {
						Number width = this.getNodeWidth(childNodes.item(offset));
						if (width != null) {
							splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION, width);
							splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION_TYPE, sizing);
						} else if (offset == childNodes.getLength() - 2) {
							width = this.getNodeWidth(childNodes.item(offset+1));
							if (width != null) {
								splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION, width);
								splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION_TYPE, SplitLocationType.WIDTH_RELATIVE);
							}
						}
					} else if (sizing == SplitLocationType.PERCENTAGE) {
						double cumulative = 0d;
						double total = 0d;
						double width;
						for (int i=offset; i<childNodes.getLength(); i++) {
							width = this.getNodeWidth(childNodes.item(i)).doubleValue();
							if (i==offset) cumulative += width;
							total += width;
						}
						splitter.setResizeWeight(cumulative/total);
						splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION, cumulative/total);
						splitter.putClientProperty(TableLayout.PROPERTY_SPLIT_LOCATION_TYPE, sizing);
					}
				}
			} else {
				JPanel panel2 = new JPanel();
				panel2.setLayout(new BorderLayout());
				
				panel.add(panel2, BorderLayout.CENTER);
				
				parseCell(childNodes.item(offset), panel2);
			}
		}
		
	}

	/**
	 * @param node
	 * @param left
	 */
	private void parseCell(Node node, JPanel panel) {
		
		Node child = node.getChildNodes().item(0);
		int i = 1;
		while(child.getNodeType()== Node.TEXT_NODE){
			child = node.getChildNodes().item(i++);
		}
		if (child.getNodeName().equals("components")){
			boolean hide = false;
			if (child.getAttributes().getNamedItem("hidetabs")!=null && child.getAttributes().getNamedItem("hidetabs").getNodeValue().equals("true")){
				hide = true;
			}
			Slot holder = new Slot(child.getAttributes().getNamedItem("id").getNodeValue(), hide);
			this.idmappings.put(child.getAttributes().getNamedItem("id").getNodeValue(), holder);
		
			panel.add(holder);
		}else if (child.getNodeName().equals("table")){
			parseTableElement(child, panel);
		}else if (child.getNodeName().equals("document_viewer")){
			this.documentViewer = new JPanel();
			panel.add(this.documentViewer);
		}
		
	}

//	/**
//	 * test program
//	 *
//	 */
//	public static void main(String[] args){
//		String test = "<table>" +
//				"			<row>" +
//				"				<cell width=\"25%\" oneTouchExpandable='false'>" +
//				"					<components id='left'/>" +
//				"				</cell>" +
//				"				<cell width=\"50%\">" +
//				"					<table>" +
//				"						<row oneTouchExpandable='false'>" +
//				"							<cell>" +
//				"								<document_viewer />" +
//				"							</cell>" +
//				"						</row>" +
//				"						<row height='300'>" +
//				"							<cell>" +
//				"								<components id='bottom'/>" +
//				"							</cell>" +
//				"						</row>" +
//				"					</table>" +
//				"				</cell>" +
//				"				<cell width=\"15%\">" +
//				"					<components id='right'/>" +
//				"				</cell>" +
//				"				<cell width=\"10%\"><components id='more right'/></cell>"+ 
//				"			</row>" +
//				"		</table>";
//		
////		String test = "<table><row><cell width='500'><components id='left'/></cell><cell><components id='rigth'/></cell></row></table>";
//		
//		final JFrame frame = new JFrame("Table Layout");
//		final TableLayout layout = new TableLayout(test);
////		SwingUtilities.invokeLater(new Runnable() {
////			/* (non-Javadoc)
////			 * @see java.lang.Runnable#run()
////			 */
////			public void run() {
//				frame.setContentPane(layout);
//				frame.setSize(800, 600);
//				frame.setVisible(true);
//				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////			}
////		});
////		SwingUtilities.invokeLater(new Runnable() {
////			/* (non-Javadoc)
////			 * @see java.lang.Runnable#run()
////			 */
////			public void run() {
//				layout.packSplitters();
////			};
////		});
//	}
	public class Slot extends JPanel{
		private static final long serialVersionUID = 1L;
		
		boolean hideTabs;
		private HashMap<JComponent,String> components = new HashMap<JComponent,String>();
		
		private JTabbedPane tabbed = new JTabbedPane();
		private String slotID;
		
		public Slot(String slotID, boolean hideTabs) {
			this.slotID = slotID;
			this.hideTabs = hideTabs;
			this.setLayout(new BorderLayout());
		}
		public void addComponent(String name, JComponent component){
			if (components.containsKey(component)){
				throw new IllegalArgumentException("This component has yet been added to this slot: "+slotID+" component: "+component);
			}
			if (this.components.size()==0){
				if (hideTabs){					
					this.add(component, BorderLayout.CENTER);
					this.validate();
				}
				else{
					this.add(tabbed, BorderLayout.CENTER);
				}
			}
			else if (hideTabs && this.components.size()==1){
				JComponent currentChild = (JComponent) this.getComponent(0);
				this.remove(currentChild);
				this.add(tabbed, BorderLayout.CENTER);
				tabbed.addTab(this.components.get(currentChild), currentChild);
			}
			
			if (!hideTabs || this.components.size()>0){
				tabbed.addTab(name, component);
			}
			this.components.put(component, name);
			
			
		}
		public void removeComponent(JComponent component){
			if (!components.containsKey(component)){				
				return;
			}
			this.components.remove(component);
			if (!hideTabs || this.components.size()>0){
				tabbed.remove(component);
			}
			if (this.components.size()==1){
				if (hideTabs){
					this.remove(tabbed);					
					this.add(component, BorderLayout.CENTER);
					this.validate();
				}
				
			}else if (this.components.size()==0){
				if (hideTabs){
					this.remove(component);
					this.validate();
				}else{
					tabbed.remove(component);
				}
			}
		}
	}

}
