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

public class AlignStructuresTask extends AbstractTask {

	private StructureManager structureManager;
	private CyNetworkView netView;
	private Map<String, CyIdentifiable> availableChimObjMap;

	@Tunable(description = "Structures to be availavle for aligning")
	public ListMultipleSelection<String> availableChimObjTunable;

	public AlignStructuresTask(List<CyIdentifiable> nodeList, CyNetworkView netView,
			StructureManager structureManager) {
		this.netView = netView;
		this.structureManager = structureManager;
		Map<CyIdentifiable, List<String>> mapChimObjNames = new HashMap<CyIdentifiable, List<String>>();
		structureManager.getChimObjNames(mapChimObjNames, netView.getModel(), nodeList,
				ModelType.PDB_MODEL, true);
		availableChimObjMap = CytoUtils
				.getCyChimPiarsToStrings(netView.getModel(), mapChimObjNames);
		initTunables();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Align Structures Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Align structures");
		// get selected structures from tunable parameter
		Map<CyIdentifiable, List<String>> selectedChimeraObjNames = CytoUtils.getCyChimPairsToMap(
				availableChimObjTunable.getSelectedValues(), availableChimObjMap);
		// open structures
		taskMonitor.setStatusMessage("Opening structures ...");
		if (structureManager.openStructures(netView.getModel(), selectedChimeraObjNames,
				ModelType.PDB_MODEL)) {
			structureManager.launchModelNavigatorDialog();
			taskMonitor.setStatusMessage("Aligning structures ...");
			structureManager.launchAlignDialog(false);
		}
	}

	private void initTunables() {
		List<String> availableObjs = new ArrayList<String>(availableChimObjMap.keySet());
		if (availableObjs.size() > 0) {
			availableChimObjTunable = new ListMultipleSelection<String>(availableObjs);
			availableChimObjTunable.setSelectedValues(availableObjs);
		} else {
			availableChimObjTunable = new ListMultipleSelection<String>("None");
		}
	}

}
