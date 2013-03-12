package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CloseStructuresTask extends AbstractTask {
	List<CyNode> nodeList;
	CyNetworkView netView;
	
	public CloseStructuresTask(List<CyNode> nodeList, CyNetworkView netView) {
		this.nodeList = nodeList;
		this.netView = netView;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

	}

}
