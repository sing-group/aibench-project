package es.uvigo.ei.aibench.workbench.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

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
	 * respectively}.
	 * </p>
	 * 
	 * <p>
	 * Please, note that duplicated properties are ignored and only the first
	 * one is read.
	 * <p>
	 * 
	 * @param extrasString a string containing the extra properties.
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
					if (extras.containsKey(property)) {
						LOG.warn("Ignoring duplicated property in extras: " + prop);
					} else {
						extras.put(property, value);
					}
				} else {
					LOG.warn("Unable to parse a property in extras: " + prop);
				}
			}
		}

		return new PortExtras(extras);
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
}
