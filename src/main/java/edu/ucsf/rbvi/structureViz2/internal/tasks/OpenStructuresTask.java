package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class OpenStructuresTask extends AbstractTask {
	// private List<CyNode> nodeList;
	private CyNetwork net = null;
	private List<CyIdentifiable> cyList;
	private StructureManager structureManager;
	private Map<String, CyIdentifiable> structruesMap;
	private Map<String, CyIdentifiable> chemStructruesMap;

	@Tunable(description = "Network for the selected nodes/edges", context = "nogui")
	public CyNetwork network = null;

	@Tunable(description = "List of nodes to open structures for", context = "nogui")
	public NodeList nodeList = new NodeList(null);

	@Tunable(description = "List of edges to open structures for", context = "nogui")
	public EdgeList edgeList = new EdgeList(null);

	@Tunable(description = "Structure file", params = "fileCategory=unspecified;input=true", gravity = 3.0, context = "nogui")
	public File structureFile = null;

	@Tunable(description = "PDB ID to fetch", context = "nogui")
	public String pdbID = "";

	@Tunable(description = "Modbase models to fetch", context = "nogui")
	public String modbaseID = "";

	@Tunable(description = "Show the Moleculra Structure Navigator dialog after opening the structure in Chimera", context = "nogui")
	public boolean showDialog = false;

	@Tunable(description = "Open structures", gravity = 1.0, context = "gui")
	public ListMultipleSelection<String> structurePairs = new ListMultipleSelection<String>("");

	@Tunable(description = "Open chemical structures", gravity = 2.0, context = "gui")
	public ListMultipleSelection<String> chemStructurePairs = new ListMultipleSelection<String>("");

	public OpenStructuresTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		// get user selection from nongui tunables
		cyList = new ArrayList<CyIdentifiable>();
		net = network;
		if (net == null) {
			CyNetwork current = ((CyApplicationManager) structureManager
					.getService(CyApplicationManager.class)).getCurrentNetwork();
			if (current != null) {
				net = current;
			}
		}
		if (net != null) {
			nodeList.setNetwork(net);
			edgeList.setNetwork(net);
			cyList.addAll(net.getNodeList());
			cyList.addAll(net.getEdgeList());
			initTunables();
		}
	}

	public OpenStructuresTask(List<CyIdentifiable> cyList, CyNetwork net,
			StructureManager structureManager) {
		// this.nodeList = nodeList;
		this.cyList = cyList;
		this.net = net;
		this.structureManager = structureManager;
		initTunables();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Open Structures Options";
	}

	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Opening Structures");
		cyList.clear();
		boolean reinitTunables = false;
		if (nodeList.getValue() != null) {
			reinitTunables = true;
			cyList.addAll(nodeList.getValue());
		} else if (edgeList.getValue() != null) {
			reinitTunables = true;
			cyList.addAll(edgeList.getValue());
		} else if (structureFile != null) {
			reinitTunables = true;
		}
		if (reinitTunables) {
			initTunables();
		}

		int openNodes = 0;
		// open PDB models
		Map<CyIdentifiable, List<String>> structuresToOpen = new HashMap<CyIdentifiable, List<String>>();
		// add selected structures from gui tunables
		if (structurePairs.getSelectedValues() != null) {
			structuresToOpen.putAll(CytoUtils.getCyChimPairsToMap(
					structurePairs.getSelectedValues(), structruesMap));
		}
		// add structure file
		List<String> structures = new ArrayList<String>();
		if (structureFile != null && structureFile.isFile()) {
			// taskMonitor.setStatusMessage("Opening structure from file ...");
			structures.add(structureFile.getAbsolutePath());
		}
		// add pdbIDs from nongui tunable
		if (pdbID != null && pdbID.length() > 0) {
			structures.add(pdbID);
		}
		// create artificial mapping
		if (structures.size() > 0) {
			structuresToOpen.put(net, structures);
		}
		// open all PDB models
		if (structuresToOpen.size() > 0) {
			taskMonitor.setStatusMessage("Opening structures ...");
			if (!structureManager.openStructures(net, structuresToOpen, ModelType.PDB_MODEL)) {
				taskMonitor.setStatusMessage("Structures could not be opened.");
			}
			openNodes += structuresToOpen.size();
		}
		structures.clear();
		structuresToOpen.clear();

		// add selected chem structures from gui tunables
		if (chemStructurePairs.getSelectedValues() != null) {
			structuresToOpen.putAll(CytoUtils.getCyChimPairsToMap(
					chemStructurePairs.getSelectedValues(), chemStructruesMap));
		}
		// open chemical structure
		if (structuresToOpen.size() > 0) {
			taskMonitor.setStatusMessage("Opening chemical structures ...");
			// open structures
			if (!structureManager.openStructures(net, structuresToOpen, ModelType.SMILES)) {
				taskMonitor.setStatusMessage("Chemical structures could not be opened.");
			}
			openNodes += structuresToOpen.size();
		}
		structures.clear();
		structuresToOpen.clear();

		// add modbase IDs from nongui tunable
		if (modbaseID != null && modbaseID.length() > 0) {
			structures.add(modbaseID);
		}
		if (structures.size() > 0) {
			structuresToOpen.put(net, structures);
		}

		// open modbase models
		if (structuresToOpen.size() > 0) {
			taskMonitor.setStatusMessage("Opening modbase models ...");
			if (!structureManager.openStructures(net, structuresToOpen, ModelType.MODBASE_MODEL)) {
				taskMonitor.setStatusMessage("ModBase models could not be opened.");
			}
			openNodes += structuresToOpen.size();
		}

		if (openNodes > 0) {
			// open dialog
			if (structureManager.getChimeraManager().isChimeraLaunched()) {
				if (showDialog) {
					structureManager.launchModelNavigatorDialog();
				}
			} else {
				taskMonitor.setStatusMessage("Chimera has not been launched.");
			}
		} else {
			taskMonitor.setStatusMessage("No structures could be matched from input.");
		}
	}

	private void initTunables() {
		// get all structure annotations for the nodes/edges in the list
		Map<CyIdentifiable, List<String>> mapChimObjNames = new HashMap<CyIdentifiable, List<String>>();
		structureManager.getChimObjNames(mapChimObjNames, net, cyList, ModelType.PDB_MODEL, false);
		structruesMap = CytoUtils.getCyChimPiarsToStrings(net, mapChimObjNames);
		mapChimObjNames.clear();
		// get all smiles annotations for the nodes/edges in the list
		structureManager.getChimObjNames(mapChimObjNames, net, cyList, ModelType.SMILES, false);
		chemStructruesMap = CytoUtils.getCyChimPiarsToStrings(net, mapChimObjNames);

		// fill in tunables
		List<String> availableStructures = new ArrayList<String>(structruesMap.keySet());
		if (availableStructures.size() > 0) {
			structurePairs = new ListMultipleSelection<String>(availableStructures);
			structurePairs.setSelectedValues(availableStructures);
		} else {
			structurePairs = new ListMultipleSelection<String>("None");
		}

		List<String> availableChem = new ArrayList<String>(chemStructruesMap.keySet());
		if (availableChem.size() > 0) {
			chemStructurePairs = new ListMultipleSelection<String>(availableChem);
			// chemStructurePairs.setSelectedValues(availableChem);
		} else {
			chemStructurePairs = new ListMultipleSelection<String>("None");
		}
	}

}
