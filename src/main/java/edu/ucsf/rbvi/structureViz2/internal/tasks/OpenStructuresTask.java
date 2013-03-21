package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.CyUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class OpenStructuresTask extends AbstractTask {
	// private List<CyNode> nodeList;
	private CyNetworkView netView;
	private StructureManager structureManager;
	private Map<CyIdentifiable, String> availableChimObjMap;

	@Tunable(description = "Structures to be opened")
	public ListMultipleSelection<String> availableChimObjTunable;

	public OpenStructuresTask(List<CyNode> nodeList, CyNetworkView netView,
			StructureManager structureManager) {
		// this.nodeList = nodeList;
		this.netView = netView;
		this.structureManager = structureManager;
		availableChimObjMap = CyUtils.getCyChimPiarsToStrings(netView.getModel(),
				structureManager.getNodeChimObjNames(netView.getModel(), nodeList));
		initTunables();
	}

	public void run(TaskMonitor taskMonitor) {
		// get selected structures from tunable parameter
		Map<CyIdentifiable, List<String>> selectedChimeraObjNames = CyUtils.getCyChimPairsToMap(
				availableChimObjTunable.getSelectedValues(), availableChimObjMap);
		System.out.println("selectedChimObjMap: " + selectedChimeraObjNames.size());
		// open structures
		structureManager.openStructures(netView.getModel(), selectedChimeraObjNames);
		if (structureManager.getModelNavigatorDialog() == null) {
			structureManager.launchDialog();
		}
	}

	private void initTunables() {
		List<String> availableObjs = new ArrayList<String>(availableChimObjMap.values());
		if (availableObjs.size() > 0) {
			availableChimObjTunable = new ListMultipleSelection<String>(availableObjs);
			availableChimObjTunable.setSelectedValues(availableObjs);
		} else {
			// TODO: How to initialize a tunable when it is empty?
			availableChimObjTunable = new ListMultipleSelection<String>("None");
		}
	}
	
}
