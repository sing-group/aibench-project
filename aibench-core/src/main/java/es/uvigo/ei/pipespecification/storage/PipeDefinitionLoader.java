/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part the AIBench Project. 

AIBench Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

AIBench Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with AIBench Project.  If not, see <http://www.gnu.org/licenses/>.
*/

/*  
 * PipeDefinitionLoader.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.pipespecification.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.uvigo.ei.aibench.core.operation.execution.IncompatibleContraintsException;

abstract class PipeDefinitionLoader {

	private static Map<String, PipeDefinitionLoader> definitionLoadersMap = new HashMap<String, PipeDefinitionLoader>();

	static {
		PipeDefinitionLoader[] loaders = { new SimplePipeDefinitionLoader(),
				new CompositedPipeDefinitionLoader(),
				new TeeDefinitionLoader(), new IncludeDefinitionLoader(),
				new Parallelizer() };
		for (PipeDefinitionLoader loader : loaders) {
			addLoader(loader);
		}
	}

	private static void addLoader(PipeDefinitionLoader loader1) {
		definitionLoadersMap.put(loader1.getElementName(), loader1);
	}

	static PipeDefinition load(Element element, File file,
			Map<String, Class<?>> argumentsSpecificationContext)
			throws IncompatibleContraintsException,
			InvalidAnnotationsFormatException, ClassNotFoundException,
			SAXException, IOException {
		if (definitionLoadersMap.containsKey(element.getLocalName())) {
			return definitionLoadersMap.get(element.getLocalName())
					.loadFromElement(element, file,
							argumentsSpecificationContext);
		} else
			throw new IllegalArgumentException("The element " + element
					+ " is unsupported");
	}

	static List<Element> getPipeDefinitionElements(NodeList nodeList) {
		List<Element> elements = getElements(nodeList);
		List<Element> result = new ArrayList<Element>();
		for (Element element : elements) {
			if (definitionLoadersMap.containsKey(element.getLocalName()))
				result.add(element);
		}
		return result;

	}

	protected static List<Element> getElements(NodeList childNodes) {
		List<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
				elements.add((Element) node);
		}
		return elements;
	}

	abstract String getElementName();

	abstract PipeDefinition loadFromElement(Element element, File file,
			Map<String, Class<?>> argumentsSpecificationContext)
			throws IncompatibleContraintsException,
			InvalidAnnotationsFormatException, ClassNotFoundException,
			SAXException, IOException;
}

class Parallelizer extends PipeDefinitionLoader {

	@Override
	String getElementName() {
		return "parallelizer";
	}

	@Override
	PipeDefinition loadFromElement(Element element, File file,
			Map<String, Class<?>> argumentsSpecificationContext)
			throws IncompatibleContraintsException,
			InvalidAnnotationsFormatException, ClassNotFoundException,
			SAXException, IOException {
		assert element.getLocalName().equals(getElementName());
		List<Element> definitions = getPipeDefinitionElements(element
				.getChildNodes());
		int i = 0;
		PipeDefinition[] branches = new PipeDefinition[definitions.size()];
		for (Element definition : definitions) {
			branches[i++] = PipeDefinitionLoader.load(definition, file,
					argumentsSpecificationContext);
		}
		assert i == branches.length;
		return new ParallelizerPipeDefinition(branches);
	}

}

class CompositedPipeDefinitionLoader extends PipeDefinitionLoader {

	@Override
	String getElementName() {
		return "composited-pipe";
	}

	@Override
	PipeDefinition loadFromElement(Element parentElement, File file,
			Map<String, Class<?>> argumentsSpecificationContext)
			throws IncompatibleContraintsException,
			InvalidAnnotationsFormatException, ClassNotFoundException,
			SAXException, IOException {
		assert parentElement.getLocalName().equals("composited-pipe");
		NodeList childNodes = parentElement.getChildNodes();
		List<Element> elements = PipeDefinitionLoader
				.getPipeDefinitionElements(childNodes);
		if (elements.size() == 0)
			throw new RuntimeException("composited pipe must have childs");
		PipeDefinition actual = null;
		for (Element element : elements) {
			PipeDefinition newPipeDefinition = PipeDefinitionLoader.load(
					element, file, argumentsSpecificationContext);
			actual = actual == null ? newPipeDefinition : actual
					.join(newPipeDefinition);
		}
		assert actual != null;
		return actual;
	}
}

class SimplePipeDefinitionLoader extends PipeDefinitionLoader {

	private final XPath xPath;

	public SimplePipeDefinitionLoader() {
		try {
			xPath = XPathFactory.newInstance(
					XPathFactory.DEFAULT_OBJECT_MODEL_URI).newXPath();
		} catch (XPathFactoryConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	String getElementName() {
		return "pipe";
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	PipeDefinition loadFromElement(final Element element, File file,
			Map<String, Class<?>> argumentsSpecificationContext)
			throws IncompatibleContraintsException,
			InvalidAnnotationsFormatException, ClassNotFoundException {
		assert element.getLocalName().equals("pipe");
		NodeList args = element.getElementsByTagNameNS("*", "arg");
		List<String> argsNames = new ArrayList<String>();
		List<Class<?>> argsTypes = new ArrayList<Class<?>>();
		process(args, argumentsSpecificationContext, argsNames, argsTypes);
		String classAttribute = element.getAttribute("class");
		if (classAttribute == null || classAttribute.equals(""))
			throw new RuntimeException("The element " + element
					+ " must have the atribute class");
		Class<?> klass = Class.forName(classAttribute);
		try {
			xPath.setNamespaceContext(new NamespaceContext() {
				public String getNamespaceURI(String prefix) {
					return element.getNamespaceURI();
				}

				public String getPrefix(String namespaceURI) {
					return null;
				}

				public Iterator<?> getPrefixes(String namespaceURI) {
					return null;
				}
			});
			NodeList nodes = (NodeList) xPath.evaluate("prefix:port", element,
					XPathConstants.NODESET);
			List<Element> explicitDefinedPorts = PipeDefinitionLoader
					.getElements(nodes);
			ExecutorService executor = Executors.newFixedThreadPool(3);
			return explicitDefinedPorts.isEmpty() ? 
				new StandardPipeDefinition(klass, argsNames.toArray(new String[0]), argsTypes.toArray(new Class<?>[0]), executor)
				: new StandardPipeDefinition(klass, explicitDefinedPorts, argsNames.toArray(new String[0]), argsTypes.toArray(new Class<?>[0]), executor);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}

	}

	private static void process(NodeList args,
			Map<String, Class<?>> argumentsSpecificationContext,
			List<String> argsNames, List<Class<?>> classes) {
		for (int i = 0; i < args.getLength(); i++) {
			Element argElement = (Element) args.item(i);
			String argument = argElement.getAttribute("name");
			if (argument.equals(""))
				throw new RuntimeException(
						"The arg attribute inside a pipe element must have a name attribute");
			if (!argumentsSpecificationContext.containsKey(argument))
				throw new RuntimeException(
						"The name attribute must point to an already defined attribute");
			argsNames.add(argument);
			classes.add(argumentsSpecificationContext.get(argument));
		}
	}

}

class TeeDefinitionLoader extends PipeDefinitionLoader {

	@Override
	String getElementName() {
		return "tee";
	}

	@Override
	PipeDefinition loadFromElement(Element element, File file,
			Map<String, Class<?>> argumentsSpecificationContext)
			throws IncompatibleContraintsException,
			InvalidAnnotationsFormatException, ClassNotFoundException,
			SAXException, IOException {
		NodeList childNodes = element.getChildNodes();
		List<Element> branchesElements = getPipeDefinitionElements(childNodes);
		PipeDefinition[] branches = new PipeDefinition[branchesElements.size()];
		for (int i = 0; i < branches.length; i++) {
			branches[i] = PipeDefinitionLoader.load(branchesElements.get(i),
					file, argumentsSpecificationContext);
		}
		return new TeeDefinition(branches);
	}

}

class IncludeDefinitionLoader extends PipeDefinitionLoader {

	@Override
	String getElementName() {
		return "include";
	}

	@Override
	PipeDefinition loadFromElement(Element element, File file,
			Map<String, Class<?>> argumentsSpecificationContext)
			throws IncompatibleContraintsException,
			InvalidAnnotationsFormatException, ClassNotFoundException,
			SAXException, IOException {
		String fileToInclude = element.getAttribute("file");
		if (fileToInclude == null)
			throw new RuntimeException(
					"The include element must have the file attribute");
		File xmlFile = new File(file.getParentFile(), fileToInclude);
		if (!xmlFile.isFile())
			throw new RuntimeException(
					"The file specified in the include element was not found");
		PipeDefinition importedDefinition = PipeLoader.load(xmlFile);
		NodeList args = element.getElementsByTagNameNS("*", "arg");
		Map<String, String> renamer = new HashMap<String, String>();
		for (int i = 0; i < args.getLength(); i++) {
			String oldName = ((Element) args.item(i)).getAttribute("name");
			if (oldName == null)
				throw new RuntimeException(
						"There must exist the attribute id in the arg element");
			String newName = ((Element) args.item(i)).getAttribute("newName");
			if (newName == null)
				throw new IllegalArgumentException(
						"There must exist the attribute newName in the arg element inside the include element");
			if (!argumentsSpecificationContext.containsKey(oldName))
				throw new IllegalArgumentException(
						"The id attribute must point to an already defined argument");
			renamer.put(oldName, newName);
		}
		PipeDefinition pipeRenamed = new ArgsRenamerPipeDefinition(renamer,
				importedDefinition);
		pipeRenamed.checkArguments(argumentsSpecificationContext);
		return pipeRenamed;
	}

}
