package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;

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

public class OpenStructuresTaskFactory extends AbstractTaskFactory implements
		NetworkViewTaskFactory, NodeViewTaskFactory {

	private StructureManager structureManager;

	public OpenStructuresTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetworkView netView) {
		// Get all of the selected nodes/edges
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		selectedList.addAll(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		if (structureManager.getChimObjNames(netView.getModel(), selectedList, ModelType.SMILES,
				false).size() > 0
				|| structureManager.getChimObjNames(netView.getModel(), selectedList,
						ModelType.PDB_MODEL, false).size() > 0) {
			return true;
		}
		return false;
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.add(nodeView.getModel());
		selectedList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		if (structureManager.getChimObjNames(netView.getModel(), selectedList, ModelType.SMILES,
				false).size() > 0
				|| structureManager.getChimObjNames(netView.getModel(), selectedList,
						ModelType.PDB_MODEL, false).size() > 0) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		selectedList.addAll(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		return new TaskIterator(new OpenStructuresTask(selectedList, netView, structureManager));
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.add(nodeView.getModel());
		selectedList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		return new TaskIterator(new OpenStructuresTask(selectedList, netView, structureManager));
	}
}
