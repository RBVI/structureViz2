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

import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class OpenStructuresTaskFactory extends AbstractTaskFactory
                                       implements NetworkViewTaskFactory, NodeViewTaskFactory {

	private StructureManager structureManager;
	private ChimeraManager chimeraManager;
	
	public OpenStructuresTaskFactory(StructureManager structureManager, ChimeraManager chimeraManager) {
		this.structureManager = structureManager;
		this.chimeraManager = chimeraManager;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetworkView netView) {
		return true;
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		return true;
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		List<CyNode> nodeList = new ArrayList<CyNode>();
		// Get all of the selected nodes
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), "selected", true));
		return new TaskIterator(new OpenStructuresTask(nodeList, netView, structureManager, chimeraManager));
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		List<CyNode> nodeList = new ArrayList<CyNode>();
		nodeList.add(nodeView.getModel());
		// Get all of the selected nodes
		nodeList.addAll(CyTableUtil.getNodesInState(netView.getModel(), "selected", true));
		return new TaskIterator(new OpenStructuresTask(nodeList, netView, structureManager, chimeraManager));
	}
}
