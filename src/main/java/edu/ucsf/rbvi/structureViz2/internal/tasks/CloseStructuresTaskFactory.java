package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CloseStructuresTaskFactory extends AbstractTaskFactory implements NodeViewTaskFactory,
		NetworkViewTaskFactory {

	private StructureManager structureManager;

	public CloseStructuresTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyNode> nodeList = new ArrayList<CyNode>();
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), "selected", true));
		if (structureManager.getNodeOpenChimObjNames(nodeList).size() > 0) {
			return true;
		}
		return false;
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		// Get all of the selected nodes
		List<CyNode> nodeList = new ArrayList<CyNode>();
		nodeList.add(nodeView.getModel());
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), "selected", true));
		if (structureManager.getNodeOpenChimObjNames(nodeList).size() > 0) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		// TODO: Could getModel return null?
		// Get all of the selected nodes
		List<CyNode> nodeList = new ArrayList<CyNode>();
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), "selected", true));
		return new TaskIterator(new CloseStructuresTask(nodeList, netView, structureManager));
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netVeiw) {
		// Get all of the selected nodes
		List<CyNode> nodeList = new ArrayList<CyNode>();
		nodeList.add(nodeView.getModel());
		nodeList.addAll(CyTableUtil.getNodesInState(netVeiw.getModel(), "selected", true));
		return new TaskIterator(new CloseStructuresTask(nodeList, netVeiw, structureManager));
	}

}
