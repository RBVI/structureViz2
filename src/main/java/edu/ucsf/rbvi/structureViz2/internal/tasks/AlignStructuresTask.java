package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AlignStructuresTask extends AbstractTask {

	private StructureManager structureManager;
	private List<CyNode> nodeList;
	private CyNetworkView netView;
	
	public AlignStructuresTask(List<CyNode> nodeList, CyNetworkView netView, StructureManager structureManager) {
		this.nodeList = nodeList;
		this.netView = netView;
		this.structureManager = structureManager;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
