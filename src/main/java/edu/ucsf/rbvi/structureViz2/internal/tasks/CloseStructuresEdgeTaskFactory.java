package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CloseStructuresEdgeTaskFactory extends AbstractTaskFactory implements EdgeViewTaskFactory,
		NetworkViewTaskFactory {

	private StructureManager structureManager;

	public CloseStructuresEdgeTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.addAll(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));
		if (structureManager.getOpenChimObjNames(selectedList).size() > 0) {
			return true;
		}
		return false;
	}

	public boolean isReady(View<CyEdge> edgeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.add(edgeView.getModel());
		selectedList.addAll(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));
		if (structureManager.getOpenChimObjNames(selectedList).size() > 0) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.addAll(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));
		return new TaskIterator(new CloseStructuresTask(selectedList, netView, structureManager));
	}

	public TaskIterator createTaskIterator(View<CyEdge> nodeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.add(nodeView.getModel());
		selectedList.addAll(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));
		return new TaskIterator(new CloseStructuresTask(selectedList, netView, structureManager));
	}

}
