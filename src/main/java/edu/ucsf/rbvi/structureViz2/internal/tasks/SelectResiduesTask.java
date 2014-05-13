package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class SelectResiduesTask extends AbstractTask {

	private View<CyNode> nodeView;
	private CyNetworkView netView;
	private StructureManager structureManager;

	public SelectResiduesTask(View<CyNode> nodeView, CyNetworkView netView,
			StructureManager structureManager) {
		this.nodeView = nodeView;
		this.netView = netView;
		this.structureManager = structureManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		structureManager.selectFunctResidues(nodeView.getModel(), netView.getModel());
	}

}
