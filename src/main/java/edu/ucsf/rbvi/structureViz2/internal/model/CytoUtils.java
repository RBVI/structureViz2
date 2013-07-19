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
					pairsMap.put(nodeName + " \t" + name, cyObj);
				}
			}
		}
		return pairsMap;
	}

	public static Map<CyIdentifiable, List<String>> getCyChimPairsToMap(List<String> selectedPairs,
			Map<String, CyIdentifiable> allPairs) {
		Map<CyIdentifiable, List<String>> selectedPairsMap = new HashMap<CyIdentifiable, List<String>>();
		for (String selectedPair : selectedPairs) {
			String[] names = selectedPair.split("\t");
			if (names.length != 2) {
				continue;
			}
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

	public static void setDefaultChimeraPath(BundleContext bc, String pathPropertyKey,
			String pathPropertyValue) {

		// Find if the CyProperty already exists, if not create one with default value.
		CyProperty<Properties> chimeraPathProperty = null;
		CySessionManager mySessionManager = (CySessionManager) bc.getService(bc
				.getServiceReference(CySessionManager.class.getName()));
		CySession session = mySessionManager.getCurrentSession();
		if (session == null) {
			return;
		}
		Set<CyProperty<?>> props = session.getProperties();
		if (props == null) {
			return;
		}
		boolean flag = false;
		for (CyProperty<?> prop : props) {
			if (prop.getName() != null) {
				if (prop.getName().equals(pathPropertyKey)) {
					chimeraPathProperty = (CyProperty<Properties>) prop;
					chimeraPathProperty.getProperties().setProperty(pathPropertyKey,
							pathPropertyValue);
					flag = true;
					break;
				}
			}
		}

		// If the property does not exist, create it
		if (!flag) {
			Properties chimeraPathProps = new Properties();
			chimeraPathProps.setProperty(pathPropertyKey, pathPropertyValue);
			chimeraPathProperty = new SimpleCyProperty(pathPropertyKey, chimeraPathProps,
					String.class, CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
		}
		bc.registerService(CyProperty.class.getName(), chimeraPathProperty, new Properties());
	}

	public static String getDefaultChimeraPath(BundleContext bc, String pathPropertyKey) {
		// Find if the CyProperty already exists, if not create one with default value.
		CyProperty<Properties> chimeraPathProperty = null;
		CySessionManager mySessionManager = (CySessionManager) bc.getService(bc
				.getServiceReference(CySessionManager.class.getName()));
		CySession session = mySessionManager.getCurrentSession();
		if (session == null) {
			return "";
		}
		Set<CyProperty<?>> props = session.getProperties();
		if (props == null) {
			return "";
		}
		for (CyProperty<?> prop : props) {
			if (prop.getName() != null) {
				if (prop.getName().equals(pathPropertyKey)) {
					chimeraPathProperty = (CyProperty<Properties>) prop;
					return chimeraPathProperty.getProperties().getProperty(pathPropertyKey);
				}
			}
		}
		return "";
	}

}
