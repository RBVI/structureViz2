package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class AlignStructuresTaskFactory extends AbstractTaskFactory implements
		NetworkViewTaskFactory, NodeViewTaskFactory {

	private StructureManager structureManager;

	public AlignStructuresTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> nodeList = new ArrayList<CyIdentifiable>();
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		Map<CyIdentifiable, List<String>> mapChimObjNames = new HashMap<CyIdentifiable, List<String>>();
		structureManager.getChimObjNames(mapChimObjNames, netView.getModel(), nodeList,
				ModelType.PDB_MODEL, true);
		if (mapChimObjNames.size() > 1) {
			return true;
		}
		return false;
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> nodeList = new ArrayList<CyIdentifiable>();
		nodeList.add(nodeView.getModel());
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		Map<CyIdentifiable, List<String>> mapChimObjNames = new HashMap<CyIdentifiable, List<String>>();
		structureManager.getChimObjNames(mapChimObjNames, netView.getModel(), nodeList,
				ModelType.PDB_MODEL, true);
		if (mapChimObjNames.size() > 1) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> nodeList = new ArrayList<CyIdentifiable>();
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		// If nothing is selected, add everything to the list
		if (nodeList.size() == 0) {
			nodeList.addAll(netView.getModel().getNodeList());
		}
		return new TaskIterator(new AlignStructuresTask(nodeList, netView, structureManager));
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> nodeList = new ArrayList<CyIdentifiable>();
		nodeList.add(nodeView.getModel());
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		return new TaskIterator(new AlignStructuresTask(nodeList, netView, structureManager));
	}

}
