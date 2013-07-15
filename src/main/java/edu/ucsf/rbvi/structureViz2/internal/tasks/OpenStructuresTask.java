package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
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
	private CyNetworkView netView;
	private StructureManager structureManager;
	private Map<String, CyIdentifiable> structruesMap;
	private Map<String, CyIdentifiable> chemStructruesMap;

	@Tunable(description = "Open structures")
	public ListMultipleSelection<String> structureTunable;

	@Tunable(description = "Open chemical structures")
	public ListMultipleSelection<String> chemTunable;

	public OpenStructuresTask(List<CyIdentifiable> nodeList, CyNetworkView netView,
			StructureManager structureManager) {
		// this.nodeList = nodeList;
		this.netView = netView;
		this.structureManager = structureManager;
		Map<CyIdentifiable, List<String>> mapChimObjNames = new HashMap<CyIdentifiable, List<String>>();
		structureManager.getChimObjNames(mapChimObjNames, netView.getModel(), nodeList,
				ModelType.PDB_MODEL, false);
		structruesMap = CytoUtils.getCyChimPiarsToStrings(netView.getModel(), mapChimObjNames);
		mapChimObjNames.clear();
		structureManager.getChimObjNames(mapChimObjNames, netView.getModel(), nodeList,
				ModelType.SMILES, false);
		chemStructruesMap = CytoUtils.getCyChimPiarsToStrings(netView.getModel(), mapChimObjNames);
		initTunables();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Open Structures Options";
	}

	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Open Structures");
		taskMonitor.setStatusMessage("Opening structures ...");
		// get selected structures from tunable parameter
		Map<CyIdentifiable, List<String>> selectedStructureNames = CytoUtils.getCyChimPairsToMap(
				structureTunable.getSelectedValues(), structruesMap);
		// System.out.println("selectedStructuresMap: " + selectedStructureNames.size());
		// open structures
		structureManager.openStructures(netView.getModel(), selectedStructureNames,
				ModelType.PDB_MODEL);

		// get selected chem structures from tunable parameter
		taskMonitor.setStatusMessage("Opening chemical structures ...");
		Map<CyIdentifiable, List<String>> selectedChemNames = CytoUtils.getCyChimPairsToMap(
				chemTunable.getSelectedValues(), chemStructruesMap);
		// System.out.println("selectedChemMap: " + selectedChemNames.size());
		// open structures
		structureManager.openStructures(netView.getModel(), selectedChemNames, ModelType.SMILES);

		// open dialog
		if (structureManager.getChimeraManager().isChimeraLaunched()) {
			structureManager.launchModelNavigatorDialog();
		}
	}

	private void initTunables() {
		List<String> availableStructures = new ArrayList<String>(structruesMap.keySet());
		if (availableStructures.size() > 0) {
			structureTunable = new ListMultipleSelection<String>(availableStructures);
			structureTunable.setSelectedValues(availableStructures);
		} else {
			structureTunable = new ListMultipleSelection<String>("None");
		}

		List<String> availableChem = new ArrayList<String>(chemStructruesMap.keySet());
		if (availableChem.size() > 0) {
			chemTunable = new ListMultipleSelection<String>(availableChem);
		} else {
			chemTunable = new ListMultipleSelection<String>("None");
		}
	}

}
