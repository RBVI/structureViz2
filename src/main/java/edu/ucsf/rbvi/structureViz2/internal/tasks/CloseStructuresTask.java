package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CloseStructuresTask extends AbstractTask {
	private StructureManager structureManager;
	private Map<String, CyIdentifiable> openChimObjMap;

	@Tunable(description = "Structures to be closed")
	public ListMultipleSelection<String> structurePairs = new ListMultipleSelection<String>("");

	public CloseStructuresTask(List<CyIdentifiable> nodeList, CyNetworkView netView,
			StructureManager structureManager) {
		this.structureManager = structureManager;
		openChimObjMap = CytoUtils.getCyChimPiarsToStrings(netView.getModel(),
				structureManager.getOpenChimObjNames(nodeList));
		initTunables();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Close Structures Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Closing Structures");
		taskMonitor.setStatusMessage("Closing structures ...");
		// get open models
		Map<CyIdentifiable, List<String>> selectedChimeraObjs = CytoUtils.getCyChimPairsToMap(
				structurePairs.getSelectedValues(), openChimObjMap);
		// System.out.println("selectedChimObjMap: " + selectedChimeraObjs.size());
		// automatically launch a dialog with list of models to close
		// close selected models
		Set<String> models = new HashSet<String>();
		for (CyIdentifiable cyObj : selectedChimeraObjs.keySet()) {
			models.addAll(selectedChimeraObjs.get(cyObj));
		}
		structureManager.closeStructures(models);
	}

	private void initTunables() {
		List<String> availableObjs = new ArrayList<String>(openChimObjMap.keySet());
		if (availableObjs.size() > 0) {
			structurePairs = new ListMultipleSelection<String>(availableObjs);
			structurePairs.setSelectedValues(availableObjs);
		} else {
			structurePairs = new ListMultipleSelection<String>("None");
		}
	}

}
