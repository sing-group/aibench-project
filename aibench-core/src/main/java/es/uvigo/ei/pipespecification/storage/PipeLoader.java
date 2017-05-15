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
package es.uvigo.ei.pipespecification.storage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import es.uvigo.ei.aibench.core.operation.execution.IncompatibleConstraintsException;

public class PipeLoader {

	private final static DocumentBuilder docBuilder;

	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		;
		try {
			URL resource = PipeLoader.class.getResource("pipes.xsd");
			File xsdFile = new File(resource.toURI());
			docBuilderFactory.setNamespaceAware(true);
			docBuilderFactory.setValidating(true);
			docBuilderFactory
					.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			docBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, xsdFile);
			docBuilder = docBuilderFactory.newDocumentBuilder();
			docBuilder.setErrorHandler(new ErrorHandler() {

				public void warning(SAXParseException exception)
						throws SAXException {
					throw new InvalidSyntaxPipeSpecificationException(exception);
				}

				public void error(SAXParseException exception)
						throws SAXException {
					throw new InvalidSyntaxPipeSpecificationException(exception);

				}

				public void fatalError(SAXParseException exception)
						throws SAXException {
					throw new InvalidSyntaxPipeSpecificationException(exception);

				}
			});
			assert docBuilder.isNamespaceAware();
			assert docBuilder.isValidating();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static NodeList getSpecificationArgs(Document doc) {
		Element args = (Element) doc.getElementsByTagNameNS("*", "args")
				.item(0);
		return args.getElementsByTagNameNS("*", "arg");
	}

	public static PipeDefinition load(File xmlFile) throws SAXException,
			IOException, ClassNotFoundException,
			IncompatibleConstraintsException, InvalidAnnotationsFormatException {
		Document doc = docBuilder.parse(xmlFile);
		NodeList argsList = getSpecificationArgs(doc);
		Map<String, Class<?>> argsMap = new HashMap<String, Class<?>>();
		for (int i = 0; i < argsList.getLength(); i++) {
			Element arg = (Element) argsList.item(i);
			String argName = arg.getAttribute("name");
			if (!argName.equals("")) {
				argsMap.put(argName, Class.forName(arg.getAttribute("class")));
			}
		}
		return PipeDefinitionLoader.load(doc.getDocumentElement(), xmlFile,
				argsMap);

	}
}
