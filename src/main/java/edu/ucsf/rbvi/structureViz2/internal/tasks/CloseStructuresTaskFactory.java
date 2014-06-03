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
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CloseStructuresTaskFactory extends AbstractTaskFactory implements TaskFactory,
		NodeViewTaskFactory, NetworkViewTaskFactory {

	private StructureManager structureManager;

	public CloseStructuresTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		if (structureManager.getOpenChimObjNames(selectedList).size() > 0) {
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
		if (structureManager.getOpenChimObjNames(selectedList).size() > 0) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CloseStructuresTask(structureManager));
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		if (selectedList.size() == 0) {
			selectedList.addAll(netView.getModel().getNodeList());
		}
		return new TaskIterator(new CloseStructuresTask(selectedList, netView, structureManager));
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.add(nodeView.getModel());
		selectedList.addAll(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED,
				true));
		return new TaskIterator(new CloseStructuresTask(selectedList, netView, structureManager));
	}

}
