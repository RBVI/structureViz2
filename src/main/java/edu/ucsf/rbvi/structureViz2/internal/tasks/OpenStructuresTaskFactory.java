package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class OpenStructuresTaskFactory extends AbstractTaskFactory
                                       implements NetworkViewTaskFactory, NodeViewTaskFactory {


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
		return new TaskIterator(new OpenStructuresTask(netView, nodeList));
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		List<CyNode> nodeList = new ArrayList<CyNode>();
		nodeList.add(nodeView.getModel());
		// Get all of the selected nodes

		return new TaskIterator(new OpenStructuresTask(netView, nodeList));
	}
}
