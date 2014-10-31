
package org.platonos.pluginengine;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.platonos.pluginengine.logging.LoggerLevel;
import org.platonos.pluginengine.version.PluginVersion;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Unmarshals a plugin.xml file into a Plugin object.
 * @author Nathan Sweet (misc@n4te.com)
 */
final class PluginXmlParser {
	static private final String PLUGIN = "plugin";
	static private final String EXTENSION = "extension";
	static private final String EXTENSION_POINT = "extensionpoint";
	static private final String DEPENDENCY = "dependency";
	static private final String UID = "uid";
	static private final String VERSION = "version";
	static private final String MIN_VERSION = "minversion";
	static private final String MAX_VERSION = "maxversion";
	static private final String NAME = "name";
	static private final String CLASS = "class";
	static private final String LIFECYCLE_CLASS = "lifecycleclass";
	static private final String INTERFACE = "interface";
	static private final String START = "start";
	static private final String START_ORDER = "startOrder";
	static private final String DEPENDENT_PLUGIN_LOOKUP = "dependentPluginLookup";
	static private final String OPTIONAL = "optional";
	static private final String TRUE = "true";
	static private final String METADATA = "metadata";
	static private final String SCHEMA = "schema";

	static private final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static private final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	static private final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	static private final URL pluginSchemaURL = PluginXmlParser.class.getResource("plugin.xsd");

	static private boolean isValidationSupported = true;
	static boolean isValidationEnabled = true;
	static private SAXParserFactory parserFactory;

	static synchronized boolean isValidationSupported () {
		if (parserFactory == null) getParserFactory();
		return isValidationSupported;
	}

	static synchronized private SAXParserFactory getParserFactory () {
		if (parserFactory == null) {
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			parserFactory.setValidating(true);
			// Create a parser just to set the isValidationSupported boolean to false if it fails.
			try {
				parserFactory.newSAXParser().setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			} catch (SAXNotRecognizedException ex) {
				parserFactory.setValidating(false);
				isValidationSupported = false;
			} catch (Exception ignored) {}
		}
		return parserFactory;
	}

	static synchronized private SAXParser getParser (URL schemaURL) throws PluginEngineException {
		if (schemaURL == null) throw new NullPointerException("Invalid schemaURL argument.");
		SAXParser parser;
		try {
			parser = getParserFactory().newSAXParser();
		} catch (Exception ex) {
			throw new PluginEngineException("Failed to create SAX parser.", ex);
		}

		if (isValidationSupported && isValidationEnabled) {
			try {
				parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				parser.setProperty(JAXP_SCHEMA_SOURCE, schemaURL.openStream());
			} catch (SAXException ex) {
				throw new PluginEngineException("XML schema error: " + schemaURL, ex);
			} catch (IOException ex) {
				throw new PluginEngineException("Error reading schema file: " + schemaURL, ex);
			}
		}
		return parser;
	}

	/**
	 * Unmarshals a plugin.xml file into a Plugin object.
	 */
	static Plugin parse (PluginEngine pluginEngine, URL pluginXmlURL) throws PluginEngineException {
		boolean isArchive = pluginXmlURL.getProtocol().equals("jar");
		URL pluginURL;
		try {
			String pluginURLStr = pluginXmlURL.toString();
			if (isArchive) {
				pluginURL = new URL(pluginURLStr.substring(4, pluginURLStr.length() - 12));
			} else {
				pluginURL = new URL(pluginURLStr.substring(0, pluginURLStr.length() - 10));
			}
		} catch (MalformedURLException ex) {
			throw new PluginEngineException("Error parsing plugin.xml URL.", ex);
		}
		Plugin plugin = new Plugin(pluginEngine, pluginURL);

		try {
			getParser(pluginSchemaURL).parse(pluginXmlURL.openStream(), new PluginXmlHandler(plugin, pluginXmlURL));
		} catch (SAXParseException ex) {
			throw new PluginEngineException("Failed parsing plugin.xml on line " + ex.getLineNumber() + ": " + pluginXmlURL, ex);
		} catch (SAXException ex) {
			throw new PluginEngineException("Failed parsing plugin.xml: " + pluginXmlURL, ex);
		} catch (IOException ex) {
			throw new PluginEngineException("Failed parsing plugin.xml: " + pluginXmlURL, ex);
		}
		return plugin;
	}

	/**
	 * Validates the specified XML string using the XML Schema at the specified URL.
	 */
	static void validate (PluginEngine pluginEngine, URL schemaURL, String xml) throws SAXParseException {
		if (!isValidationSupported || !isValidationEnabled) return;
		try {
			getParser(schemaURL).parse(new InputSource(new StringReader(xml)), new ValidatingHandler(pluginEngine, xml));
		} catch (SAXException ex) {
			pluginEngine.getLogger().log(LoggerLevel.SEVERE, "Failed parsing: " + xml, ex);
		} catch (IOException ex) {
			pluginEngine.getLogger().log(LoggerLevel.SEVERE, "Failed parsing: " + xml, ex);
		} catch (PluginEngineException ex) {
			pluginEngine.getLogger().log(LoggerLevel.SEVERE, "Failed parsing: " + xml, ex);
		}
	}

	static class ValidatingHandler extends DefaultHandler {
		protected final PluginEngine pluginEngine;
		private final String parsingMessage;

		ValidatingHandler (PluginEngine pluginEngine, String parsingMessage) {
			this.pluginEngine = pluginEngine;
			this.parsingMessage = parsingMessage;
		}

		public void error (SAXParseException ex) throws SAXException {
			throw ex;
		}

		public void fatalError (SAXParseException ex) throws SAXException {
			throw ex;
		}

		public void warning (SAXParseException ex) throws SAXException {
			pluginEngine.getLogger().log(LoggerLevel.WARNING,
				"Warning during parsing line " + ex.getLineNumber() + ": " + parsingMessage, ex);
		}
	}

	static class PluginXmlHandler extends ValidatingHandler {
		private final Plugin plugin;
//		private final URL pluginXmlURL;
		private final StringBuffer accumulator = new StringBuffer(500);
		private PluginXmlNode currentXmlNode = null;

		PluginXmlHandler (Plugin plugin, URL pluginXmlURL) {
			super(plugin.getPluginEngine(), pluginXmlURL.toString());
			this.plugin = plugin;
//			this.pluginXmlURL = pluginXmlURL;
		}

		public void characters (char[] buffer, int start, int length) {
			accumulator.append(buffer, start, length);
		}

		public void startElement (String namespaceURL, String localName, String qname, Attributes attributes) {
			accumulator.setLength(0);

			if (currentXmlNode != null) {
				// Add the node as a child of the current node.
				PluginXmlNode node = createNode(qname, attributes);
				currentXmlNode.addChild(node);
				currentXmlNode = node;
				return; // Bypass normal processing.
			}

			if (qname.equals(PLUGIN)) {
				plugin.setStartWhenResolved(TRUE.equals(attributes.getValue(START)));
				plugin.setDependentPluginLookup(TRUE.equals(attributes.getValue(DEPENDENT_PLUGIN_LOOKUP)));
				
				try {
					String startOrder = attributes.getValue(START_ORDER);
					if (startOrder != null) {
						plugin.setStartOrder(Integer.parseInt(startOrder));
					}
				} catch (NumberFormatException nfe) {
					pluginEngine.getLogger().log(LoggerLevel.SEVERE, "Start order format not valid.", nfe);
				}
			} else if (qname.equals(EXTENSION)) {
				Extension extension = new Extension(plugin, attributes.getValue(UID), attributes.getValue(NAME), attributes
					.getValue(CLASS));
				plugin.addExtension(extension);

				currentXmlNode = createNode(EXTENSION, attributes); // Root extension node.
				extension.setExtensionXmlNode(currentXmlNode);

			} else if (qname.equals(METADATA)) {
				currentXmlNode = createNode(METADATA, attributes); // Root metadata node.
				plugin.setMetadataXmlNode(currentXmlNode);

			} else if (qname.equals(EXTENSION_POINT)) {
				if (plugin.getUID() == null) {
					pluginEngine.getLogger().log(
						LoggerLevel.SEVERE,
						"The Plugin's UID must be set before an \"" + EXTENSION_POINT + "\" element is encountered. Plugin: "
							+ plugin.getPluginURL(), null);
					return;
				}
				ExtensionPoint extensionPoint = new ExtensionPoint(plugin, attributes.getValue(NAME), attributes
					.getValue(INTERFACE));
				if (attributes.getValue(SCHEMA) != null) extensionPoint.setSchemaFilename(attributes.getValue(SCHEMA));
				plugin.addExtensionPoint(extensionPoint);

			} else if (qname.equals(DEPENDENCY)) {
				Dependency dependency = new Dependency(plugin, attributes.getValue(UID));
				try {
					String dependencyVersion = attributes.getValue(VERSION);
					if (dependencyVersion == null) {
						String dependencyMinVersion = attributes.getValue(MIN_VERSION);
						String dependencyMaxVersion = attributes.getValue(MAX_VERSION);
						if (dependencyMinVersion != null && dependencyMaxVersion != null) {
							dependency.requiredVersion = PluginVersion.createDependencyVersion(
								String.format("%s,%s", dependencyMinVersion, dependencyMaxVersion)
							);
						} else if (dependencyMaxVersion != null) {
							dependency.requiredVersion = PluginVersion.createDependencyVersion(dependencyMaxVersion);
						} else if (dependencyMinVersion != null) {
							dependency.requiredVersion = PluginVersion.createDependencyVersion(dependencyMinVersion);
						}
					} else {
						dependency.requiredVersion = PluginVersion.createDependencyVersion(dependencyVersion);
					}
//					dependency.requiredVersion = PluginVersion.create(attributes.getValue(VERSION));
//					dependency.minVersion = PluginVersion.create(attributes.getValue(MIN_VERSION));
//					dependency.maxVersion = PluginVersion.create(attributes.getValue(MAX_VERSION));
					dependency.setOptional(TRUE.equals(attributes.getValue(OPTIONAL)));
				} catch (PluginEngineException ex) {
					pluginEngine.getLogger().log(LoggerLevel.WARNING,
						"Unable to parse version for Dependency \"" + dependency.resolveToPluginUID + "\" in Plugin: " + plugin,
						ex);
				}
				plugin.addDependency(dependency);
			}
		}

		public void endElement (String namespaceURL, String localName, String qname) {
			String value = accumulator.toString().trim();

			if (currentXmlNode != null) {
				currentXmlNode.setText(value);
				if (qname.equals(EXTENSION) || qname.equals(METADATA)) {
					if (currentXmlNode.getParent() == null) {
						currentXmlNode = null;
						return;
					}
				}
				currentXmlNode = currentXmlNode.getParent();
				return;
			}

			if (qname.equals(NAME)) {
				plugin.setName(value);

			} else if (qname.equals(VERSION)) {
				try {
					plugin.setVersion(PluginVersion.createInstanceVersion(value));
				} catch (PluginEngineException ex) {
					pluginEngine.getLogger().log(LoggerLevel.SEVERE, "Error setting PluginVersion for Plugin: " + plugin, ex);
				}

			} else if (qname.equals(UID)) {
				plugin.setUID(value);

			} else if (qname.equals(LIFECYCLE_CLASS)) {
				plugin.setLifecycleClassName(value);
			}
		}

		private PluginXmlNode createNode (String name, Attributes attributes) {
			PluginXmlNode node = new PluginXmlNode();
			node.setName(name);
			for (int i = 0, n = attributes.getLength(); i < n; i++) {
				node.setAttribute(attributes.getQName(i), attributes.getValue(i));
			}
			return node;
		}
	}

	private PluginXmlParser () {
	}
}
