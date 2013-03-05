package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class OpenStructuresTask extends AbstractTask {
	CyNetworkView netView;
	List<CyNode> nodeList;

	public OpenStructuresTask(CyNetworkView netView, List<CyNode> nodeList) {
		this.netView = netView;
		this.nodeList = nodeList;
	}

	public void run(TaskMonitor taskMonitor) {
	}

}
