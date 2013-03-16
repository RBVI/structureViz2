package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

public abstract class CyUtils {

	public static List<String> getMatchingAttributes(CyTable table, List<String> columns) {
		Set<String> columnNames = CyTableUtil.getColumnNames(table);

		List<String> columnsFound = new ArrayList<String>();
		for (String attribute : columns) {
			if (columnNames.contains(attribute))
				columnsFound.add(attribute);
		}

		return columnsFound;
	}

	// TODO: ugly code
	public static Map<CyIdentifiable, String> getCyChimPiarsToStrings(CyNetwork network,
			Map<CyIdentifiable, List<String>> pairs) {
		Map<CyIdentifiable, String> pairsMap = new HashMap<CyIdentifiable, String>();
		CyTable nodeTable = network.getDefaultNodeTable();
		for (CyIdentifiable cyObj : pairs.keySet()) {
			// TODO: Is there an easier way to get the node name??
			if (pairs.get(cyObj).size() > 0 && nodeTable.rowExists(cyObj.getSUID())) {
				CyRow row = nodeTable.getRow(cyObj.getSUID());
				if (row.isSet("COMMON")) {
					String nodeName = row.get("COMMON", String.class);
					for (String name : pairs.get(cyObj)) {
						pairsMap.put(cyObj, nodeName + ": " + name);
					}
				}
			}
		}
		return pairsMap;
	}

	// TODO: ugly code
	public static Map<CyIdentifiable, List<String>> getCyChimPairsToMap(List<String> selectedPairs,
			Map<CyIdentifiable, String> allPairs) {
		Map<CyIdentifiable, List<String>> selectedPairsMap = new HashMap<CyIdentifiable, List<String>>();
		for (CyIdentifiable cyObj : allPairs.keySet()) {
			if (selectedPairs.contains(allPairs.get(cyObj))) {
				String[] names = allPairs.get(cyObj).split(":");
				if (names.length == 2) {
					if (!selectedPairsMap.containsKey(cyObj)) {
						selectedPairsMap.put(cyObj, new ArrayList<String>());
					}
					selectedPairsMap.get(cyObj).add(names[1].trim());
				}
			}
		}
		return selectedPairsMap;
	}

}
