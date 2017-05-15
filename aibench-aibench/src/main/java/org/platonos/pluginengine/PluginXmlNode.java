/*
 * #%L
 * The AIBench basic runtime and plugin engine
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

package org.platonos.pluginengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an XML element in the plugin.xml file.
 * @see Extension#getExtensionXmlNode()
 * @see Plugin#getMetadataXmlNode()
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public class PluginXmlNode {
	private final List<PluginXmlNode> children = new ArrayList<PluginXmlNode>(10);
	private final Map<String, String> attributes = new HashMap<String, String>(10);
	private PluginXmlNode parent = null;
	private String name = null;
	private String text = null;

	/**
	 * Sets the value of the attribute with the specified name.
	 * 
	 * @param name the name of the attribute.
	 * @param value the value of the attribute.
	 */
	public void setAttribute (String name, String value) {
		attributes.put(name.toLowerCase(), value);
	}

	/**
	 * Returns the value of the attribute with the specified name.
	 * 
	 * @param name the name of the attribute.
	 * @return the value of the attribute.
	 */
	public String getAttribute (String name) {
		return (String)attributes.get(name.toLowerCase());
	}

	/**
	 * Returns the name/value pairs of all attributes on this PluginXmlNode.
	 * 
	 * @return the name/value pairs of all attributes on this PluginXmlNode.
	 */
	public Map<String, String> getAttributes () {
		return attributes;
	}

	/**
	 * Returns the number of attributes on this PluginXmlNode.
	 * 
	 * @return the number of attributes on this PluginXmlNode.
	 */
	public int getAttributeCount () {
		return (null != attributes) ? attributes.size() : 0;
	}

	/**
	 * Returns {@code true} if there is at least one child node, otherwise returns {@code false}.
	 * 
	 * @return {@code true} if there is at least one child node, otherwise returns {@code false}.
	 */
	public boolean hasChildren () {
		return (null != children && children.size() > 0);
	}

	/**
	 * Returns a list of PluginXmlNodes that make up the children of this PluginXmlNode. The list returned is used in the internal
	 * implementation so modifying the list will affect this PluginXmlNode.
	 * 
	 * @return a list of PluginXmlNodes that make up the children of this PluginXmlNode.
	 */
	public List<PluginXmlNode> getChildren () {
		return children;
	}

	/**
	 * Returns the child at the specified index or {@code null} if the index is out of range.
	 * 
	 * @param index the index of the child.
	 * @return the child at the specified index or {@code null} if the index is out of range.
	 */
	public PluginXmlNode getChild (int index) {
		if (index > children.size() - 1) return null;
		return (PluginXmlNode)children.get(index);
	}

	/**
	 * Returns the first child with the specified element name or {@code null} if it could not be found.
	 * 
	 * @param name the name of the child.
	 * @return the first child with the specified element name or {@code null} if it could not be found.
	 */
	public PluginXmlNode getChild (String name) {
		for (PluginXmlNode child:children) {
			if (child.getName().equals(name)) return child;
		}
		return null;
	}

	/**
	 * Returns a list of PluginXmlNodes representing all children with the specified element name. An empty list will be returned
	 * if none are found.
	 * 
	 * @param name the name of the children.
	 * @return a list of PluginXmlNodes representing all children with the specified element name. An empty list will be returned
	 * if none are found.
	 */
	public List<PluginXmlNode> getChildren (String name) {
		List<PluginXmlNode> childrenWithName = new ArrayList<PluginXmlNode>();
		for (PluginXmlNode child:children) {
			if (child.getName().equals(name)) childrenWithName.add(child);
		}
		return childrenWithName;
	}

	/**
	 * Adds an PluginXmlNode as a child of this PluginXmlNode.
	 * 
	 * @param child the child to be added.
	 */
	public void addChild (PluginXmlNode child) {
		child.setParent(this);
		children.add(child);
	}

	/**
	 * Removes an PluginXmlNode from this PluginXmlNode.
	 * 
	 * @param child the child to be removed.
	 * @return {@code true} if the child was removed. {@code false} otherwise.
	 */
	public boolean removeChild (PluginXmlNode child) {
		child.setParent(null);
		return children.remove(child);
	}

	/**
	 * Sets the name of this PluginXmlNode.
	 * 
	 * @param name the name of this PluginXmlNode.
	 */
	public void setName (String name) {
		this.name = name;
	}

	/**
	 * Returns the name of this PluginXmlNode.
	 * 
	 * @return the name of this PluginXmlNode.
	 */
	public String getName () {
		return name;
	}

	/**
	 * Sets the parent PluginXmlNode.
	 * 
	 * @param parent the parent PluginXmlNode.
	 */
	public void setParent (PluginXmlNode parent) {
		this.parent = parent;
	}

	/**
	 * Returns the parent PluginXmlNode or {@code null} if this PluginXmlNode is at the root.
	 * 
	 * @return the parent PluginXmlNode or {@code null} if this PluginXmlNode is at the root.
	 */
	public PluginXmlNode getParent () {
		return parent;
	}

	/**
	 * Sets the text of this PluginXmlNode.
	 * 
	 * @param text the text of this PluginXmlNode.
	 */
	public void setText (String text) {
		this.text = text;
	}

	/**
	 * Returns the text between the beginning and ending XML tags for this PluginXmlNode.
	 * 
	 * @return the text between the beginning and ending XML tags for this PluginXmlNode.
	 */
	public String getText () {
		return text == null ? "" : text;
	}

	/**
	 * Returns the XML of this PluginXmlNode and all of its children.
	 * 
	 * @return the XML of this PluginXmlNode and all of its children.
	 */
	public String toXML () {
		StringBuilder buffer = new StringBuilder(100);
		toXML(buffer);
		return buffer.toString();
	}

	/**
	 * Appends the XML of this PluginXmlNode and all of its children to the specified StringBuffer.
	 * 
	 * @param buffer the buffer to which the XML will be added.
	 */
	void toXML(StringBuilder buffer) {
		buffer.append('<');
		buffer.append(getName());
		for (String name : getAttributes().keySet()) {
			buffer.append(' ');
			buffer.append(name);
			buffer.append("=\"");
			buffer.append(getAttribute(name));
			buffer.append('"');
		}
		List<PluginXmlNode> children = getChildren();
		if (children.isEmpty()) {
			String text = getText();
			if (text.length() == 0) {
				buffer.append("/>");
			} else {
				buffer.append('>');
				buffer.append(text);
				buffer.append("</");
				buffer.append(getName());
				buffer.append('>');
			}
		} else {
			buffer.append('>');
			for (PluginXmlNode child : children) {
				child.toXML(buffer);
			}
			buffer.append("</");
			buffer.append(getName());
			buffer.append('>');
		}
	}

	@Override
	public String toString () {
		return toXML();
	}
}
