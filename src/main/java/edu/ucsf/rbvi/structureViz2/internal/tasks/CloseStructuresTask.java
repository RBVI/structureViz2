package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraModel;
import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CloseStructuresTask extends AbstractTask {
	private StructureManager structureManager;
	private Map<String, CyIdentifiable> openChimObjMap;
	private List<CyIdentifiable> cyList;
	private CyNetwork net;

	@Tunable(description = "Structures to be closed", context = "gui")
	public ListMultipleSelection<String> structurePairs = new ListMultipleSelection<String>("");

	@Tunable(description = "Network for the selected nodes/edges", context = "nogui")
	public CyNetwork network = null;

	// @Tunable(description = "List of nodes to close structures for", context = "nogui")
	// public NodeList nodeList = new NodeList(null);
	//
	// @Tunable(description = "List of edges to close structures for", context = "nogui")
	// public EdgeList edgeList = new EdgeList(null);

	@Tunable(description = "List of models to close", context = "nogui")
	public ListMultipleSelection<String> modelList = new ListMultipleSelection<String>("");

	public CloseStructuresTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		cyList = new ArrayList<CyIdentifiable>();
		if (network != null) {
			net = network;
		} else {
			net = ((CyApplicationManager) structureManager.getService(CyApplicationManager.class))
					.getCurrentNetwork();
		}
		if (net != null) {
			cyList.addAll(net.getNodeList());
			cyList.addAll(net.getEdgeList());
			initTunables();
		}
		if (structureManager.getChimeraManager().isChimeraLaunched()
				&& structureManager.getChimeraManager().getChimeraModelsCount(false) > 0) {
			List<String> models = new ArrayList<String>();
			Collection<ChimeraModel> current = structureManager.getChimeraManager()
					.getChimeraModels();
			for (ChimeraModel model : current) {
				models.add(model.getModelName());
			}
			modelList = new ListMultipleSelection<String>(models);
			modelList.setSelectedValues(models);
		}
	}

	public CloseStructuresTask(List<CyIdentifiable> cyList, CyNetworkView netView,
			StructureManager structureManager) {
		this.structureManager = structureManager;
		this.cyList = cyList;
		this.net = netView.getModel();
		initTunables();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Close Structures Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Closing Structures");
		// reinitialize tunables
		// cyList.clear();
		// boolean reinitTunables = false;
		// if (nodeList.getValue() != null) {
		// reinitTunables = true;
		// cyList.addAll(nodeList.getValue());
		// } else if (edgeList.getValue() != null) {
		// reinitTunables = true;
		// cyList.addAll(edgeList.getValue());
		// }
		// if (reinitTunables) {
		// initTunables();
		// }
		taskMonitor.setStatusMessage("Closing structures ...");
		// add models selected in gui tunable
		Map<CyIdentifiable, List<String>> selectedChimeraObjs = new HashMap<CyIdentifiable, List<String>>();
		if (structurePairs.getSelectedValues() != null) {
			selectedChimeraObjs.putAll(CytoUtils.getCyChimPairsToMap(
					structurePairs.getSelectedValues(), openChimObjMap));
		}
		// close selected models
		Set<String> models = new HashSet<String>();
		for (CyIdentifiable cyObj : selectedChimeraObjs.keySet()) {
			models.addAll(selectedChimeraObjs.get(cyObj));
		}
		// get models selected in nogui tunable
		if (modelList.getSelectedValues() != null) {
			models = new HashSet<String>(modelList.getSelectedValues());
		}
		if (models.size() > 0) {
			structureManager.closeStructures(models);
		} else {
			taskMonitor.setStatusMessage("No structures could be matched from input.");
		}
	}

	private void initTunables() {
		openChimObjMap = CytoUtils.getCyChimPiarsToStrings(net,
				structureManager.getOpenChimObjNames(cyList));
		List<String> availableObjs = new ArrayList<String>(openChimObjMap.keySet());
		if (availableObjs.size() > 0) {
			structurePairs = new ListMultipleSelection<String>(availableObjs);
			structurePairs.setSelectedValues(availableObjs);
		} else {
			structurePairs = new ListMultipleSelection<String>("None");
		}
	}

}
