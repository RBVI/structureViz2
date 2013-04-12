package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
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

	public static Map<String, CyIdentifiable> getCyChimPiarsToStrings(CyNetwork network,
			Map<CyIdentifiable, List<String>> pairs) {
		Map<String, CyIdentifiable> pairsMap = new HashMap<String, CyIdentifiable>();
		CyTable nodeTable = network.getDefaultNodeTable();
		for (CyIdentifiable cyObj : pairs.keySet()) {
			if (pairs.get(cyObj).size() > 0) {
				String nodeName = nodeTable.getRow(cyObj.getSUID()).get(CyNetwork.NAME, String.class);
				for (String name : pairs.get(cyObj)) {
					pairsMap.put(nodeName + ": " + name, cyObj);
				}
			}
		}
		return pairsMap;
	}

	public static Map<CyIdentifiable, List<String>> getCyChimPairsToMap(List<String> selectedPairs,
			Map<String, CyIdentifiable> allPairs) {
		Map<CyIdentifiable, List<String>> selectedPairsMap = new HashMap<CyIdentifiable, List<String>>();
		for (String selectedPair : selectedPairs) {
			String[] names = selectedPair.split(":");
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

}
