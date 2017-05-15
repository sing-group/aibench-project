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
package es.uvigo.ei.aibench.core.operation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.w3c.dom.Element;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.core.operation.execution.EndpointsFactory;
import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.StandardExecutable;
import es.uvigo.ei.pipespecification.InvalidAnnotationsException;

/**
 * Defines an Operation for the AiBench's Core. These objects are created by the
 * core once the operation pluggins has been analyzed.
 *
 * Please Note: A instance of this object represents the operation, but not an
 * instance of these operation. The operation plugins must provide a class
 * annotated with the details of the operations input/output TODO: put some
 * examples here
 *
 *
 * @author Daniel González Peña 23-sep-2006
 *
 */
public class OperationDefinition<T> {
	private String name = "";
	private String id = "";
	private String description = "";
	// NOTE: added by miguel reboiro
	private String help = "";
	private String pluginName = "";
	private String pluginID = "";
	// NOTE: added by paulo maia
	private String shortcut = "";
	private String path = "";
	private String menuName = "";
	private boolean enabledByDefault;
	private List<EndpointsFactory<T>> factories;
	private List<Class<?>> outcomingArgumentTypes;
	private List<Class<?>> incomingArgumentTypes;
	private Port[] ports;

	/**
	 * Should not be used
	 */
	protected OperationDefinition() {

	}

	public String getPluginID() {
		return this.pluginID;
	}

	public void setPluginID(String pluginID) {
		this.pluginID = pluginID;
	}

	public String getPluginName() {
		return this.pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//NOTE: added by paulo maia
	public String getShortcut(){
		return this.shortcut;
	}

	//NOTE: added by paulo maia
	public void setShortcut(String shortcut){
		this.shortcut = shortcut;
	}

	/**
	 * @return the help
	 */
	public String getHelp() {
		return this.help;
	}

	/**
	 * @param help the help to set
	 */
	public void setHelp(String help) {
		this.help = help;
	}

	/**
	 * The path represents the logical type of this operation. For example a
	 * loader operation named "Load CSV..." could have one logical path such as
	 * <code>"/data/loader/csv/"</code> and a filter operation named
	 * <code>"Filter Variables by Name..."</code> could have a logical path
	 * such as <code>"/preprocessing/filter"</code>
	 *
	 * For more flexibility you can specify the order of the location with
	 * <code>number@name</code>. For example, a valid path could be:
	 * <code>"/1@data/2@save"</code> or <code>"/2@edit/1@copy"</code>. In
	 * this case the user interface should place all the "data" operations
	 * before the "edit" operations.
	 *
	 * This could be useful for a gui who wants to deploy operations in menus
	 *
	 * @return the logical paths
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Sets the logical path of the operation
	 *
	 * @param path the logical path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * The name that is used to display in menus and toolbars.
	 *
	 * @return name that is used to display in menus and toolbars
	 */
	public String getMenuName() {
		return menuName.isEmpty() ? name : menuName;
	}

	/**
	 * Sets the menu name.
	 *
	 * @param menuName the new menu name
	 */
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}


	/**
	 * Returns whether the operation is enabled or not.
	 *
	 * @return whether the operation is enabled or not.
	 */
	public boolean isEnabled() {
		return Core.getInstance().isOperationEnabled(this);
	}

	public boolean isEnabled(Core instance) {
		return instance.isOperationEnabled(this);
	}

	public boolean isEnabledByDefault(){
		return this.enabledByDefault;
	}

	public static <T> OperationDefinition<T> createOperationDefinition(
		Class<T> annotatedClass
	) throws InvalidAnnotationsException {
		Operation pipeElement = annotatedClass.getAnnotation(Operation.class);
		if (pipeElement == null)
			throw new IllegalArgumentException("the class ("
					+ annotatedClass.getName() + ") must be annotated with "
					+ Operation.class.getName());

		ArrayList<Port> portsList = new ArrayList<Port>();
		ArrayList<Method> methodList = new ArrayList<Method>();
		Method progressMethod = null;
		Method cancelMethod = null;
		Method[] methods = annotatedClass.getMethods();
		sortByPorts(methods);
		for (Method method : methods) {

			Port port = method.getAnnotation(Port.class);
			if (port != null) {
				methodList.add(method);
				portsList.add(port);

			}

			Progress progress = method.getAnnotation(Progress.class);
			if (progress != null) {
				progressMethod = method;
			}

			Cancel cancel = method.getAnnotation(Cancel.class);
			if (cancel!=null){
				cancelMethod = method;
			}

		}

		Port[] ports = new Port[portsList.size()];
		int j = 0;
		for (Port port : portsList) {
			ports[j++] = port;
		}

		List<EndpointsFactory<T>> factories = new ArrayList<EndpointsFactory<T>>();
		for (int i = 0; i < ports.length; i++) {
			factories.add(EndpointsFactory.createEndpointsFactory(
					annotatedClass, ports[i], methodList.get(i)
							.getParameterTypes(), methodList.get(i).getName()));
		}
		OperationDefinition<T> toret = new OperationDefinition<T>(
				annotatedClass, Collections.unmodifiableList(factories), ports,
				pipeElement.name(), pipeElement.description(), pipeElement.help());
		toret.enabledByDefault = pipeElement.enabled();
		toret.setMonitorBeanMethod(progressMethod);
		toret.setCancelMethod(cancelMethod);



		return toret;
	}

	/**
	 * @param methods
	 */
	private static void sortByPorts(Method[] methods) {
		Arrays.sort(methods, new Comparator<Method>() {

			public int compare(Method o1, Method o2) {
				Port o1Port = o1.getAnnotation(Port.class);
				Port o2Port = o2.getAnnotation(Port.class);

				if (o1Port == null)
					return -1;
				if (o2Port == null)
					return 1;

				int toret = new Integer(o1Port.order()).compareTo(o2Port.order());

				if (toret == 0) {
					if (o1Port.direction() == o2Port.direction())
						return 0;
					else
						return o1Port.direction() == Direction.INPUT ? -1 : 1;

				} else {
					return toret;
				}
			}
		});
	}

	public static <T> OperationDefinition<T> createOperationDefinition(
			Class<T> annotatedClass, List<Element> ports) {
		List<EndpointsFactory<T>> factoriesList = new ArrayList<EndpointsFactory<T>>();
		for (Element port : ports) {
			factoriesList.add(
				EndpointsFactory.createEndPointsFactory(annotatedClass, port)
			);
		}
		return new OperationDefinition<T>(
			annotatedClass,
			Collections.unmodifiableList(factoriesList), null,
			annotatedClass.getAnnotation(Operation.class).name(),
			annotatedClass.getAnnotation(Operation.class).description(),
			annotatedClass.getAnnotation(Operation.class).help()
		);
	}

	private OperationDefinition(Class<T> annotatedClass,
			List<EndpointsFactory<T>> factories, Port[] ports, String name,
			String description, String help) throws InvalidAnnotationsException {
		this.name = name;
		this.description = description;
		this.help = help;

		/*
		 * if (factories.isEmpty()) throw new IllegalArgumentException( "At
		 * least one port must be defined");
		 */
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

	public Executable makeExecutable(T instance, ExecutorService executor,
			Object... values) {
		return new StandardExecutable<T>(factories, incomingArgumentTypes,
				outcomingArgumentTypes, executor, instance, values);
	}

	public List<Class<?>> getIncomingArgumentTypes() {
		return incomingArgumentTypes;
	}

	public List<Class<?>> getOutcomingArgumentTypes() {
		return outcomingArgumentTypes;
	}

	public List<Port> getPorts() {
		List<Port> v = new Vector<Port>();
		for (Port p : this.ports) {
			v.add(p);
		}
		return v;
	}

	private Method monitorBeanMethod;

	public Method getMonitorBeanMethod() {

		return this.monitorBeanMethod;
	}

	public void setMonitorBeanMethod(Method method) {
		this.monitorBeanMethod = method;
	}

	private Method cancelMethod;

	/**
	 * @return the cancelMethod
	 */
	public Method getCancelMethod() {
		return this.cancelMethod;
	}

	/**
	 * @param cancelMethod the cancelMethod to set
	 */
	public void setCancelMethod(Method cancelMethod) {
		this.cancelMethod = cancelMethod;
	}

	/**
	 * @return the description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
