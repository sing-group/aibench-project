package es.uvigo.ei.aibench.core.operation.execution;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.w3c.dom.Element;

import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.pipespecification.InvalidAnnotationsException;

public class Metadata_old<T> {

	
	public static <T> Metadata_old<T> createMetadata(Class<T> annotatedClass)
			throws InvalidAnnotationsException {
		Operation pipeElement = annotatedClass.getAnnotation(Operation.class);
		if (pipeElement == null)
			throw new IllegalArgumentException(
					"the class must be annotated with "
							+ Operation.class.getName());


		ArrayList<Port> portsList = new ArrayList<Port>();
		for (Method method : annotatedClass.getMethods()){
			Port port = method.getAnnotation(Port.class);
			if (port!=null) portsList.add(port);
		}

		Port[] ports = new Port[portsList.size()];
		int j =0;
		for (Port port: portsList){
			ports[j++] = port;
		}

		List<EndpointsFactory<T>> factories = new ArrayList<EndpointsFactory<T>>();
		for (int i = 0; i < ports.length; i++) {
	/*		factories.add(EndpointsFactory.createEndpointsFactory(
					annotatedClass, ports[i]));*/
		}
		return new Metadata_old<T>(annotatedClass, Collections
				.unmodifiableList(factories), ports, pipeElement.name());
	}

	public static <T> Metadata_old<T> createMetadata(Class<T> annotatedClass,
			List<Element> ports) {
		List<EndpointsFactory<T>> factoriesList = new ArrayList<EndpointsFactory<T>>();
		for (Element port : ports) {
			factoriesList.add(EndpointsFactory.createEndPointsFactory(
					annotatedClass, port));
		}
		return new Metadata_old<T>(annotatedClass, Collections
				.unmodifiableList(factoriesList), null, annotatedClass.getAnnotation(Operation.class).name());
	}

	private List<EndpointsFactory<T>> factories;

	private final List<Class<?>> outcomingArgumentTypes;

	private final List<Class<?>> incomingArgumentTypes;

	private final Port[] ports;

	private Metadata_old(Class<T> annotatedClass,
			List<EndpointsFactory<T>> factories, Port[] ports, String name)
			throws InvalidAnnotationsException {
		
		if (factories.isEmpty())
			throw new IllegalArgumentException(
					"At least one port must be defined");
		this.factories = factories;
		List<Class<?>> outcomingTypes = new ArrayList<Class<?>>();
		List<Class<?>> incomingTypes = new ArrayList<Class<?>>();
		for (EndpointsFactory<T> factory : this.factories) {
			factory.addArguments(incomingTypes, outcomingTypes);
		}
		this.outcomingArgumentTypes = outcomingTypes;
		this.incomingArgumentTypes = incomingTypes;
		this.ports = ports;
	}

	public Executable makeExecutable(T instance,
			ExecutorService executor, Object... values) {
		return new StandardExecutable<T>(factories, incomingArgumentTypes,
				outcomingArgumentTypes, executor, instance, values);
	}

	public List<Class<?>> getIncomingArgumentTypes() {
		return  incomingArgumentTypes;
	}

	public List<Class<?>> getOutcomingArgumentTypes() {
		return outcomingArgumentTypes;
	}

	public List<Port> getPorts() {
		List<Port> v = new Vector<Port>();
		for (Port p: this.ports){
			v.add(p);
		}
		return v;
	}

}
