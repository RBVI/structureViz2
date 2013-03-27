package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.CyUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AlignStructuresTask extends AbstractTask {

	private StructureManager structureManager;
	private CyNetworkView netView;
	private Map<CyIdentifiable, String> availableChimObjMap;

	@Tunable(description = "Structures to be availavle for aligning")
	public ListMultipleSelection<String> availableChimObjTunable;

	public AlignStructuresTask(List<CyNode> nodeList, CyNetworkView netView,
			StructureManager structureManager) {
		this.netView = netView;
		this.structureManager = structureManager;
		availableChimObjMap = CyUtils.getCyChimPiarsToStrings(netView.getModel(),
				structureManager.getNodeChimObjNames(netView.getModel(), nodeList));
		initTunables();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// get selected structures from tunable parameter
		Map<CyIdentifiable, List<String>> selectedChimeraObjNames = CyUtils.getCyChimPairsToMap(
				availableChimObjTunable.getSelectedValues(), availableChimObjMap);
		// open structures
		structureManager.openStructures(netView.getModel(), selectedChimeraObjNames);
		structureManager.launchAlignDialog(false);
	}

	private void initTunables() {
		List<String> availableObjs = new ArrayList<String>(availableChimObjMap.values());
		if (availableObjs.size() > 0) {
			availableChimObjTunable = new ListMultipleSelection<String>(availableObjs);
			availableChimObjTunable.setSelectedValues(availableObjs);
		} else {
			availableChimObjTunable = new ListMultipleSelection<String>("None");
		}
	}

	@ProvidesTitle
	public String getTitle() {
		return "Align Structures";
	}
}
