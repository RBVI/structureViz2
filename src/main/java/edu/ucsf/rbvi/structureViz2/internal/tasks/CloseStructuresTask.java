package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
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
	private List<CyIdentifiable> cyList;
	private CyNetwork net;

	@Tunable(description = "Structures to be closed", context = "gui")
	public ListMultipleSelection<String> structurePairs = new ListMultipleSelection<String>("");

	@Tunable(description = "Network for the selected nodes/edges", context = "nogui")
	public CyNetwork network = null;

	public NodeList nodeList = new NodeList(null);

	@Tunable(description = "List of nodes to close structures for", context = "nogui")
	public NodeList getnodeList() {
		if (network == null) {
			network = ((CyApplicationManager) structureManager
					.getService(CyApplicationManager.class)).getCurrentNetwork();
		}
		nodeList.setNetwork(network);
		return nodeList;
	}

	public void setnodeList(NodeList setValue) {
	}

	public EdgeList edgeList = new EdgeList(null);

	@Tunable(description = "List of edges to close structures for", context = "nogui")
	public EdgeList getedgeList() {
		if (network == null) {
			network = ((CyApplicationManager) structureManager
					.getService(CyApplicationManager.class)).getCurrentNetwork();
		}
		edgeList.setNetwork(network);
		return edgeList;
	}

	public void setedgeList(EdgeList setValue) {
	}

	public CloseStructuresTask(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public CloseStructuresTask(List<CyIdentifiable> cyList, CyNetworkView netView,
			StructureManager structureManager) {
		this.structureManager = structureManager;
		this.cyList = cyList;
		this.net = netView.getModel();
		initTunables();
	}

	@Tunable(description = "Model name to close", context = "nogui")
	public String modelName = "";

	@ProvidesTitle
	public String getTitle() {
		return "Close Structures Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Closing Structures");
		taskMonitor.setStatusMessage("Closing structures ...");
		if (net == null) {
			if (network != null) {
				net = network;
				cyList = new ArrayList<CyIdentifiable>();
				if (nodeList.getValue() != null) {
					cyList.addAll(nodeList.getValue());
				} else if (edgeList.getValue() != null) {
					cyList.addAll(edgeList.getValue());
				}
				initTunables();
			} else {
				net = ((CyApplicationManager) structureManager
						.getService(CyApplicationManager.class)).getCurrentNetwork();
			}
		}
		// add models selected in gui tunable
		Map<CyIdentifiable, List<String>> selectedChimeraObjs = new HashMap<CyIdentifiable, List<String>>();
		if (structurePairs.getSelectedValues() != null) {
			selectedChimeraObjs.putAll(CytoUtils.getCyChimPairsToMap(
					structurePairs.getSelectedValues(), openChimObjMap));
		}
		// get models selected in nogui tunable
		if (modelName != null && modelName.length() > 0) {
			List<String> structures = new ArrayList<String>();
			structures.add(modelName);
			selectedChimeraObjs.put(net, structures);
		}
		// close selected models
		Set<String> models = new HashSet<String>();
		for (CyIdentifiable cyObj : selectedChimeraObjs.keySet()) {
			models.addAll(selectedChimeraObjs.get(cyObj));
		}
		structureManager.closeStructures(models);
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
