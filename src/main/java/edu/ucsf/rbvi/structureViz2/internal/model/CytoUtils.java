package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CytoUtils {

	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils.class);

	private static CyProperty<Properties> configProperties = null;
	private static CyProperty<Properties> sessionProperties = null;
	private static Pattern pat = null;

	public static List<String> getMatchingAttributes(CyTable table, List<String> columns) {
		Set<String> columnNames = CyTableUtil.getColumnNames(table);

		List<String> columnsFound = new ArrayList<String>();
		for (String attribute : columns) {
			if (columnNames.contains(attribute))
				columnsFound.add(attribute);
		}
		return columnsFound;
	}

	public static String getName(CyNetwork network, CyIdentifiable id) {
		return network.getRow(id).get(CyNetwork.NAME, String.class);
	}

	public static List<String> getStringAttributes(CyTable table) {
		Collection<CyColumn> columns = table.getColumns();

		List<String> columnsFound = new ArrayList<String>();
		for (CyColumn attribute : columns) {
			if (attribute.getType() == String.class ||
			    attribute.getListElementType() == String.class)
				columnsFound.add(attribute.getName());
		}
		return columnsFound;

	}

	public static Map<String, CyIdentifiable> getCyChimPiarsToStrings(CyNetwork network,
			Map<CyIdentifiable, List<String>> pairs) {
		Map<String, CyIdentifiable> pairsMap = new HashMap<String, CyIdentifiable>();
		for (CyIdentifiable cyObj : pairs.keySet()) {
			if (pairs.get(cyObj).size() > 0) {
				String nodeName = network.getRow(cyObj).get(CyNetwork.NAME, String.class);
				for (String name : pairs.get(cyObj)) {
					pairsMap.put(nodeName + "|" + name, cyObj);
				}
			}
		}
		return pairsMap;
	}

	public static Map<CyIdentifiable, List<String>> getCyChimPairsToMap(List<String> selectedPairs,
			Map<String, CyIdentifiable> allPairs) {

		Map<CyIdentifiable, List<String>> selectedPairsMap = 
						new HashMap<CyIdentifiable, List<String>>();
		
		for (String selectedPair : selectedPairs) {
			String[] names = selectedPair.split("\\|");
			// System.out.println("Input: " + selectedPair);
			if (names.length != 2) {
				logger.info("Could not parse node pdb pair: " + selectedPair);
				continue;
			}
			// System.out.println("Names: " + names[0] + ", " + names[1]);
			CyIdentifiable cyObj = allPairs.get(selectedPair);
			if (!selectedPairsMap.containsKey(cyObj)) {
				selectedPairsMap.put(cyObj, new ArrayList<String>());
			}
			selectedPairsMap.get(cyObj).add(names[1].trim());
		}
		return selectedPairsMap;
	}

	public static String getNodeName(CyNetwork network, CyNode node) {
		return getNodeName(network, node, CyNetwork.NAME);
	}

	public static String getNodeName(CyNetwork network, CyNode node, String attr) {
		if (network.containsNode(node) && network.getRow(node).isSet(attr)) {
			return network.getRow(node).get(attr, String.class);
		}
		return "";
	}

	public static String join(List<String> list, String delim) {
		StringBuilder sb = new StringBuilder();
		String loopDelim = "";
		for (String s : list) {
			sb.append(loopDelim);
			sb.append(s);
			loopDelim = delim;
		}
		return sb.toString();
	}

	public static void setDefaultChimeraPath(CyServiceRegistrar registrar, 
		                                       String chimeraPathPropertyKey, 
																					 String path) {
		if (configProperties == null) {
			configProperties = getPropertyService(registrar, SavePolicy.CONFIG_DIR);
		}
		Properties p = configProperties.getProperties();
		p.setProperty(chimeraPathPropertyKey, path); 
	}

	public static String getDefaultChimeraPath(CyServiceRegistrar registrar, 
		                                         String chimeraPathPropertyKey) {
		if (configProperties == null) {
			configProperties = getPropertyService(registrar, SavePolicy.CONFIG_DIR);
		}
		Properties p = configProperties.getProperties();
		String path = p.getProperty(chimeraPathPropertyKey); 
		return path;
	}

	public static void setDefaultColumns(CyServiceRegistrar registrar, 
		                                   String columnPropertyKey,
																			 Map<String,List<String>> columnMap) {
		if (sessionProperties == null) {
			sessionProperties = getPropertyService(registrar, SavePolicy.SESSION_FILE);
		}
		Properties p = sessionProperties.getProperties();
		JSONObject obj = new JSONObject();
		for (String key: columnMap.keySet()) {
			obj.put(key, columnMap.get(key));
		}
		p.setProperty(columnPropertyKey, obj.toString()); 
	}

	public static Map<String,List<String>> getDefaultColumns(CyServiceRegistrar registrar, 
		                                                       String columnPropertyKey) {
		if (sessionProperties == null) {
			sessionProperties = getPropertyService(registrar, SavePolicy.SESSION_FILE);
		}
		Properties p = sessionProperties.getProperties();
		String columns = p.getProperty(columnPropertyKey);
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject)parser.parse(columns);
		} catch(Exception e) {
		}

		Map<String, List<String>> outmap = new HashMap<>();

		if (obj == null)
			return outmap;

		for (Object key: obj.keySet()) {
			JSONArray arr = (JSONArray)obj.get((String)key);
			List<String> columnList = new ArrayList<>();
			for (Object a: arr) {
				columnList.add((String)a);
			}
			outmap.put((String)key, columnList);
		}
		return outmap;
	}

	static CyProperty<Properties> getPropertyService(CyServiceRegistrar registrar,
	                                                 SavePolicy policy) {
		if (policy.equals(SavePolicy.CONFIG_DIR) || policy.equals(SavePolicy.SESSION_FILE_AND_CONFIG_DIR)) {
			String name = "structureViz";
			CyProperty<Properties> service = new ConfigPropsReader(policy, name);
			Properties serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", service.getName());
			registrar.registerAllServices(service, serviceProps);
			return service;
		} else if (policy.equals(SavePolicy.SESSION_FILE)) {
			String name = "structureVizSession";
			// Do we already have a session with our properties
			CySessionManager sessionManager = registrar.getService(CySessionManager.class);
			CySession session = sessionManager.getCurrentSession();
			if (session != null) {
				Set<CyProperty<?>> sessionProperties = session.getProperties();
				for (CyProperty<?> cyProp: sessionProperties) {
					if (cyProp.getName() != null && cyProp.getName().equals(name)) {
						return (CyProperty<Properties>)cyProp;
					}
				}
			}
			// Either we have a null session or our properties aren't in this session
			Properties props = new Properties();
			CyProperty<Properties> service = new SimpleCyProperty(name, props, Properties.class, SavePolicy.SESSION_FILE);
			Properties serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", service.getName());
			registrar.registerAllServices(service, serviceProps);
			return service;
		}
		return null;
	}

	public static class ConfigPropsReader extends AbstractConfigDirPropsReader {
		ConfigPropsReader(SavePolicy policy, String name) {
			super(name, "structureViz.props", policy);
		}
	}

	public static String columnSubstitution(StructureManager manager, CyNetwork network, 
	                                        CyRow row, String originalString) {
		// Find app matching patterns
		if (pat == null)
			pat = Pattern.compile("%.+?%");
		Matcher m = pat.matcher(originalString);
		Set<String> columnNames = CyTableUtil.getColumnNames(network.getDefaultNodeTable());
		StringBuilder sb =  new StringBuilder();
		int pend = 0;
		while (m.find() == true) {
			int start = m.start();
			int end = m.end();
			String key = originalString.substring(start+1, end-1);
			// System.out.println("key = "+key);
			if (!columnNames.contains(key)) {
				sb.append(originalString.substring(pend, end));
				// System.out.println("No match: string = "+sb.toString());
			} else {
				sb.append(originalString.substring(pend, start));

				Object v = row.getRaw(key);
				sb.append(v.toString());
				// System.out.println("Found match: string = "+sb.toString());
			}
			pend = end;
		}

		sb.append(originalString.substring(pend, originalString.length()));
		// System.out.println("final string: "+sb.toString());

		return sb.toString();
	}

}
