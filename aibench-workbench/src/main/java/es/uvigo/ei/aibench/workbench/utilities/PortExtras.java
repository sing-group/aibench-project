package es.uvigo.ei.aibench.workbench.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.operation.annotation.Port;

/**
 * A class that provides support for processing {@code extras} parameters in
 * {@code Port} configurations.
 *
 * @author Hugo López-Fernández
 * @see Port
 */
public class PortExtras {
	public static final String EXTRAS_DELIMITER = ",";
	public static final String EXTRAS_ASSIGNMENT = "=";

	private final static Logger LOG = Logger.getLogger(PortExtras.class);
	private Map<String, String> extras;

	public PortExtras(Map<String, String> extras) {
		this.extras = extras;
	}

	/**
	 *
	 * <p>
	 * Parses {@code extrasString} and returns a new {@code PortExtras} object
	 * containing the read properties. Each pair of property and value is
	 * delimited by a {@code ,} and values are assigned to properties using
	 * {@code =}. For example, the string {@code a=A, b=B} contains two
	 * properties {@code a} and {@code b} with values {@code A} and {@code B}
	 * respectively}. It is also possible to define properties without values
	 * associated (e.g. {@code required} creates a property with an empty
	 * value).
	 * </p>
	 *
	 * <p>
	 * Please, note that duplicated properties are ignored and only the first
	 * one is read.
	 * <p>
	 *
	 * @param extrasString a string containing the extra properties.
	 *
	 * @return a {@code PortExtras} object.
	 */
	public static PortExtras parse(String extrasString) {
		Map<String, String> extras = new HashMap<>();
		if (extrasString != null && !extrasString.trim().isEmpty()) {
			final String[] props = extrasString.split(EXTRAS_DELIMITER);

			for (String prop : props) {
				final String[] propValue = prop.trim().split(EXTRAS_ASSIGNMENT);

				if (propValue.length == 2) {
					final String property = propValue[0].trim();
					final String value = propValue[1].trim();
					addProperty(extras, prop, property, value);
				} else if (propValue.length == 1) {
					final String property = propValue[0].trim();
					addProperty(extras, prop, property, "");
				} else {
					LOG.warn("Unable to parse a property in extras: " + prop);
				}
			}
		}

		return new PortExtras(extras);
	}

	private static void addProperty(Map<String, String> extras, String prop, final String property,
		final String value
	) {
		if (extras.containsKey(property)) {
			LOG.warn("Ignoring duplicated property in extras: " + prop);
		} else {
			extras.put(property, value);
		}
	}

	/**
	 * Returns a set with the names of the extra properties.
	 *
	 * @return a set with the names of the extra properties.
	 */
	public Set<String> getProperties() {
		return extras.keySet();
	}

	/**
	 * Returns the value of {@code property}.
	 *
	 * @param property the property.
	 * @return the associated value.
	 */
	public String getPropertyValue(String property) {
		return extras.get(property);
	}

	/**
	 * Returns {@code true} if the specified {@code property} is present ignoring the case.
	 *
	 * @param property the name of the property.
	 * @return {@code true} if the specified {@code property} is present ignoring the case.
	 */
	public boolean containsProperty(String property) {
		return containsProperty(property, true);
	}

	/**
	 * Returns {@code true} if the specified {@code property} is present. The
	 * {@code ignoreCase} parameter allows specifying when case should be
	 * ignored or not.
	 *
	 * @param property the name of the property.
	 * @param ignoreCase {@code true} if case should be ignored and {@code false} otherwise.
	 * @return {@code true} if the specified {@code property} is present.
	 */
	public boolean containsProperty(String property, boolean ignoreCase) {
		return ignoreCase ? containsPropertyIgnoreCase(property) : this.extras.containsKey(property);
	}

	private boolean containsPropertyIgnoreCase(String property) {
		for (String p : getProperties()) {
			if (p.equalsIgnoreCase(property)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if there are properties in extras
	 * ({@link PortExtras#getProperties()}) other than those in
	 * {@code knownProperties} and emits a warning for each one using the
	 * specified {@code logger}.
	 *
	 * @param extras the {@code PortExtras} object
	 * @param logger the {@code Logger} to emit the warnings
	 * @param ignoreCase {@code true} if case should be ignored and {@code false} otherwise
	 * @param knownProperties the array of known properties
	 */
	public static void warnUnknownExtraProperties(PortExtras extras, Logger logger, boolean ignoreCase,
			String... knownProperties) {
		for (String property : extras.getProperties()) {
			boolean known = false;
			for(String knownProprety : knownProperties) {
				if(equals(property, knownProprety, ignoreCase)) {
					known = true;
				}
			}
			if (!known) {
				logger.warn("Unknown extra property: " + property);
			}
		}
	}

	private static boolean equals(String a, String b, boolean ignoreCase) {
		return ignoreCase ? a.equalsIgnoreCase(b) : a.equals(b);
	}
}
