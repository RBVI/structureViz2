package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.osgi.framework.BundleContext;

public abstract class CytoUtils {

	public static List<String> getMatchingAttributes(CyTable table, List<String> columns) {
		Set<String> columnNames = CyTableUtil.getColumnNames(table);

		List<String> columnsFound = new ArrayList<String>();
		for (String attribute : columns) {
			if (columnNames.contains(attribute))
				columnsFound.add(attribute);
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
		Map<CyIdentifiable, List<String>> selectedPairsMap = new HashMap<CyIdentifiable, List<String>>();
		for (String selectedPair : selectedPairs) {
			String[] names = selectedPair.split("\\|");
			System.out.println("Input: " + selectedPair);
			if (names.length != 2) {
				continue;
			}
			System.out.println("Names: " + names[0] + ", " + names[1]);
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

	public static void setDefaultChimeraPath(BundleContext bc, String chimeraPathPropertyName,
			String chimeraPathPropertyKey, String chimeraPathPropertyValue) {

		// Find if the CyProperty already exists, if not create one with default value.
		boolean flag = false;
		CySessionManager mySessionManager = (CySessionManager) bc.getService(bc
				.getServiceReference(CySessionManager.class.getName()));
		CySession session = mySessionManager.getCurrentSession();
		if (session == null) {
			return;
		}
		Set<CyProperty<?>> sessionProperties = session.getProperties();
		if (sessionProperties == null) {
			return;
		}
		for (CyProperty<?> cyProperty : sessionProperties) {
			if (cyProperty.getName() != null
					&& cyProperty.getName().equals(chimeraPathPropertyName)) {
				Properties props = (Properties) cyProperty.getProperties();
				props.setProperty(chimeraPathPropertyKey, chimeraPathPropertyValue);
				flag = true;
				break;
			}
		}

		// If the property does not exist, create it
		if (!flag) {
			Properties chimeraPathProps = new Properties();
			chimeraPathProps.setProperty(chimeraPathPropertyKey, chimeraPathPropertyValue);
			CyProperty<?> chimeraPathProperty = new SimpleCyProperty(chimeraPathPropertyName,
					chimeraPathProps, Properties.class,
					CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
			bc.registerService(CyProperty.class.getName(), chimeraPathProperty, new Properties());
		}

	}

	public static String getDefaultChimeraPath(BundleContext bc, String chimeraPathPropertyName,
			String chimeraPathPropertyKey) {
		// Find if the CyProperty already exists, if not create one with default value.
		CySessionManager mySessionManager = (CySessionManager) bc.getService(bc
				.getServiceReference(CySessionManager.class.getName()));
		CySession session = mySessionManager.getCurrentSession();
		if (session == null) {
			return "";
		}
		Set<CyProperty<?>> sessionProperties = session.getProperties();
		if (sessionProperties == null) {
			return "";
		}
		for (CyProperty<?> prop : sessionProperties) {
			if (prop.getName() != null && prop.getName().equals(chimeraPathPropertyName)) {
				Properties chimeraPathProperties = (Properties) prop.getProperties();
				return chimeraPathProperties.getProperty(chimeraPathPropertyKey);
			}
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

}
