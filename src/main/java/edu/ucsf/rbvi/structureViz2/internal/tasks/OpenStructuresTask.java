package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.CyUtils;
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
		structruesMap = CyUtils.getCyChimPiarsToStrings(netView.getModel(),
				structureManager.getChimObjNames(netView.getModel(), nodeList, ModelType.PDB_MODEL));
		chemStructruesMap = CyUtils.getCyChimPiarsToStrings(netView.getModel(),
				structureManager.getChimObjNames(netView.getModel(), nodeList, ModelType.SMILES));
		initTunables();
	}

	public void run(TaskMonitor taskMonitor) {
		// get selected structures from tunable parameter
		Map<CyIdentifiable, List<String>> selectedStructureNames = CyUtils.getCyChimPairsToMap(
				structureTunable.getSelectedValues(), structruesMap);
		System.out.println("selectedStructuresMap: " + selectedStructureNames.size());
		// open structures
		structureManager
				.openStructures(netView.getModel(), selectedStructureNames, ModelType.PDB_MODEL);

		// get selected chem structures from tunable parameter
		 Map<CyIdentifiable, List<String>> selectedChemNames = CyUtils.getCyChimPairsToMap(
		 chemTunable.getSelectedValues(), chemStructruesMap);
		 System.out.println("selectedChemMap: " + selectedChemNames.size());
		 // open structures
		 structureManager.openStructures(netView.getModel(), selectedChemNames, ModelType.SMILES);

		if (structureManager.getModelNavigatorDialog() == null) {
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

	@ProvidesTitle
	public String getTitle() {
		return "Open Structures";
	}
}
