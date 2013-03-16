package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

/**
 * This object maintains the relationship between Chimera objects and Cytoscape
 * objects.
 */
public class StructureManager {
	static final String[] defaultStructureKeys = { "Structure", "pdb", "pdbFileName", "PDB ID",
			"structure", "biopax.xref.PDB", "pdb_ids" };
	static final String[] defaultChemStructKeys = { "Smiles", "smiles", "SMILES" };
	static final String[] defaultResidueKeys = { "FunctionalResidues", "ResidueList" };

	private Map<CyNetwork, StructureSettings> settings = null;
	private ChimeraManager chimeraManager;
	// So far just a list, may become a map of CyIdentifiable <-> Structure?
	Map<CyIdentifiable, Structure> currentStructuresMap = null;

	public StructureManager() {
		settings = new HashMap<CyNetwork, StructureSettings>();
		currentStructuresMap = new HashMap<CyIdentifiable, Structure>();
		// Create the Chimera interface
		chimeraManager = new ChimeraManager(this);
	}

	public void setStructureSettings(CyNetwork network, StructureSettings newSettings) {
		settings.put(network, newSettings);
	}

	public List<String> getAllStructureKeys(CyNetwork network) {
		return Arrays.asList(defaultStructureKeys);
	}

	public List<String> getCurrentStructureKeys(CyNetwork network) {
		if (!settings.containsKey(network))
			return Arrays.asList(defaultStructureKeys);
		return settings.get(network).getStructureColumns().getSelectedValues();
	}

	public List<String> getAllChemStructKeys(CyNetwork network) {
		return Arrays.asList(defaultChemStructKeys);
	}

	public List<String> getCurrentChemStructKeys(CyNetwork network) {
		if (!settings.containsKey(network))
			return Arrays.asList(defaultChemStructKeys);
		return settings.get(network).getChemStructureColumns().getSelectedValues();
	}

	public List<String> getAllResidueKeys(CyNetwork network) {
		return Arrays.asList(defaultResidueKeys);
	}

	public List<String> getCurrentResidueKeys(CyNetwork network) {
		if (!settings.containsKey(network))
			return Arrays.asList(defaultResidueKeys);
		return settings.get(network).getResidueColumns().getSelectedValues();
	}

	public ChimeraManager getChimeraManager() {
		return chimeraManager;
	}

	/**
	 * Return all open structures for a set of CyObjects. Invoked by
	 * CloseStructuresTask.
	 * 
	 * @param cyObjSet
	 * @return
	 */
	public Map<CyIdentifiable, List<String>> getNodeOpenChimObjNames(Collection<CyNode> cyObjSet) {
		Map<CyIdentifiable, List<String>> matchingNames = new HashMap<CyIdentifiable, List<String>>();
		for (CyNode obj : cyObjSet) {
			List<String> nodeMatchingNames = new ArrayList<String>();
			if (currentStructuresMap.containsKey(obj)) {
				Structure structure = currentStructuresMap.get(obj);
				nodeMatchingNames.addAll(structure.getChimeraModelNames());
				if (nodeMatchingNames.size() > 0) {
					matchingNames.put(obj, nodeMatchingNames);
				}
			}
		}
		return matchingNames;
	}

	public boolean hasNodeChimObjNames(CyNetwork network, Collection<CyNode> nodeSet) {
		if (network == null)
			return false;
		CyTable nodeTable = network.getDefaultNodeTable();
		// TODO: return not only the structure keys but also the smiles?
		List<String> attrsFound = CyUtils.getMatchingAttributes(nodeTable,
				getCurrentStructureKeys(network));
		Collection idSet = nodeSet;
		return hasChimObjNames(idSet, nodeTable, attrsFound);
	}

	/**
	 * Return the names of structures or smiles that can be opened in Chimera from
	 * the selected attribute. Invoked by openStructuresTask.
	 * 
	 * @param network
	 * @param nodeSet
	 * @return
	 */
	public Map<CyIdentifiable, List<String>> getNodeChimObjNames(CyNetwork network,
			Collection<CyNode> nodeSet) {
		if (network == null)
			return new HashMap<CyIdentifiable, List<String>>();
		CyTable nodeTable = network.getDefaultNodeTable();
		List<String> attrsFound = CyUtils.getMatchingAttributes(nodeTable,
				getCurrentStructureKeys(network));
		Collection idSet = nodeSet;
		return getChimObjNames(idSet, nodeTable, attrsFound);
	}

	public boolean hasEdgeChimObjNames(CyNetwork network, Collection<CyEdge> edgeSet) {
		if (network == null)
			return false;
		CyTable edgeTable = network.getDefaultEdgeTable();
		List<String> attrsFound = CyUtils.getMatchingAttributes(edgeTable,
				getCurrentChemStructKeys(network));
		Collection idSet = edgeSet;
		return hasChimObjNames(idSet, edgeTable, attrsFound);
	}

	public void openStructures(CyNetwork network, Map<CyIdentifiable, List<String>> chimObjNames) {
		if (!chimeraManager.isChimeraLaunched()) {
			chimeraManager.launchChimera();
		}
		for (CyIdentifiable cyObj : chimObjNames.keySet()) {
			Structure currentStructure = null;
			if (currentStructuresMap.containsKey(cyObj)) {
				currentStructure = currentStructuresMap.get(cyObj);
			} else {
				currentStructure = new Structure(network, cyObj);
				currentStructuresMap.put(cyObj, currentStructure);
			}
			System.out.println("structures for cyObj " + cyObj.getSUID() + ": "
					+ chimObjNames.get(cyObj).size());
			for (String chimObjName : chimObjNames.get(cyObj)) {
				ChimeraModel currentModel = null;
				if (!chimeraManager.hasChimeraModel(chimObjName)) {
					// map and open
					currentModel = chimeraManager.openModel(chimObjName);
				} else {
					currentModel = chimeraManager.getModel(chimObjName);
				}
				if (currentModel != null) {
					currentStructure.addChimeraModel(currentModel);
				} else {
					System.out.println("model is null");
				}
			}
		}
	}

	public void closeStructures(CyNetwork network, Map<CyIdentifiable, List<String>> chimObjNames) {
		for (CyIdentifiable cyObj : chimObjNames.keySet()) {
			if (!currentStructuresMap.containsKey(cyObj)) {
				// should not be the case
				System.out.println("Could not find structure for cyObj");
				continue;
			}
			Structure currentStructure = currentStructuresMap.get(cyObj);
			System.out.println("structures for cyObj " + cyObj.getSUID() + ": "
					+ chimObjNames.get(cyObj).size());
			for (String chimObjName : chimObjNames.get(cyObj)) {
				ChimeraModel currentModel = currentStructure.getChimeraModel(chimObjName);
				if (currentModel != null) {
					chimeraManager.closeModel(currentModel);
					currentStructure.removeChimeraObject(currentModel);
				}
			}
			// TODO: remove structure if empty?
			if (currentStructure.getChimeraObjects().size() == 0) {
				currentStructuresMap.remove(cyObj);
			}
		}
	}

	public void exitChimera() {
		// exit chimera
		chimeraManager.exitChimera();
		// clear chimera related objects
		chimeraManager.clearOnChimeraExit();
		// clear structures
		currentStructuresMap.clear();
	}

	private boolean hasChimObjNames(Collection<CyIdentifiable> objs, CyTable table,
			List<String> columns) {
		if (getChimObjNames(objs, table, columns).size() > 0) {
			return true;
		}
		return false;
	}

	private Map<CyIdentifiable, List<String>> getChimObjNames(Collection<CyIdentifiable> objs,
			CyTable table, List<String> columns) {
		Map<CyIdentifiable, List<String>> structures = new HashMap<CyIdentifiable, List<String>>();
		if (columns == null || columns.size() == 0 || objs == null)
			return structures;
		for (CyIdentifiable obj : objs) {
			if (table.rowExists(obj.getSUID())) {
				CyRow row = table.getRow(obj.getSUID());
				for (String column : columns) {
					// TODO: consider attributes that contain lists?
					String cell = row.get(column, String.class, "").trim();
					if (!cell.equals("")) {
						if (!structures.containsKey(obj)) {
							structures.put(obj, new ArrayList<String>());
						}
						structures.get(obj).add(cell);
					}
				}
			}
		}
		return structures;
	}

}
